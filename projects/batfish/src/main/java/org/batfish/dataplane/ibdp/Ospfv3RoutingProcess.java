package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute6;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3ExternalType1Route6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3InterAreaRoute6;
import org.batfish.datamodel.Ospfv3NssaExternalType2Route6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.PrefixList6;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RouteMap6;
import org.batfish.datamodel.StaticRoute6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3AreaRange;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.batfish.dataplane.rib.ConnectedRib6;
import org.batfish.dataplane.rib.Ospfv3Rib6;

/**
 * Dataplane OSPFv3 process.
 *
 * <p>Supports locally originated intra-area IPv6 routes, connected-route
 * redistribution, and intra-area route exchange between compatible OSPFv3
 * interfaces. Inter-area routing and external LSA propagation are layered on
 * top separately.
 */
@ParametersAreNonnullByDefault
final class Ospfv3RoutingProcess {

  /** RFC 2328/5340 LSInfinity. */
  private static final long LS_INFINITY = 0xFFFFFFL;

  /**
   * Result of a deterministic OSPFv3 broadcast-network election.
   *
   * <p>The dataplane is computed from a configuration snapshot rather than
   * from an existing neighbor state machine, so this models a cold-start
   * election. Priority wins first, followed by router ID.
   */
  @VisibleForTesting
  static final class BroadcastElection {
    private BroadcastElection(
        @Nullable NodeInterfacePair dr,
        @Nullable NodeInterfacePair bdr) {
      _dr = dr;
      _bdr = bdr;
    }

    static BroadcastElection empty() {
      return new BroadcastElection(
          null,
          null);
    }

    @Nullable NodeInterfacePair getDr() {
      return _dr;
    }

    @Nullable NodeInterfacePair getBdr() {
      return _bdr;
    }

    boolean isDrOrBdr(
        NodeInterfacePair id) {
      return id.equals(_dr)
          || id.equals(_bdr);
    }

    private final @Nullable NodeInterfacePair _dr;
    private final @Nullable NodeInterfacePair _bdr;
  }

  private static final class BroadcastElectionCandidate {
    private BroadcastElectionCandidate(
        NodeInterfacePair id,
        int priority,
        Ip routerId) {
      _id = id;
      _priority = priority;
      _routerId = routerId;
    }

    private final @Nonnull NodeInterfacePair _id;
    private final int _priority;
    private final @Nonnull Ip _routerId;
  }

  Ospfv3RoutingProcess(
      Ospfv3Process process,
      String vrfName,
      Configuration configuration) {
    _process = process;
    _vrfName = vrfName;
    _c = configuration;
    _ospfv3Rib = new Ospfv3Rib6();
    _localExternalAdvertisements =
        ImmutableSet.of();
    _translatedNssaExternalAdvertisements =
        ImmutableSet.of();
  }

  /**
   * Reset this process to routes originated locally by this router.
   *
   * <p>Learned routes are intentionally discarded here so a new IGP
   * convergence pass naturally withdraws routes whose adjacencies disappeared.
   */
  void initialize(ConnectedRib6 connectedRib) {
    _ospfv3Rib.clear();
    _translatedNssaExternalAdvertisements =
        ImmutableSet.of();

    if (!_process.getEnabled()) {
      return;
    }

    initializeIntraAreaRoutes();
  }

  private void initializeIntraAreaRoutes() {
    _process
        .getAreas()
        .values()
        .forEach(this::initializeRoutesByArea);
  }

  private void initializeRoutesByArea(Ospfv3Area area) {
    for (String ifaceName : area.getInterfaces()) {
      Interface iface = _c.getAllInterfaces().get(ifaceName);

      if (iface == null
          || !iface.getActive()
          || !_vrfName.equals(iface.getVrfName())
          || !isEnabledForThisProcess(iface)) {
        continue;
      }

      Ospfv3InterfaceSettings settings =
          iface.getOspfv3Settings();

      if (settings.getAreaName() == null
          || settings.getAreaName()
              != area.getAreaNumber()) {
        continue;
      }

      long cost = computeInterfaceCost(iface);

      for (ConcreteInterfaceAddress6 address :
          iface.getAllConcreteAddresses6()) {
        _ospfv3Rib.mergeRoute(
            new Ospfv3IntraAreaRoute6(
                getAdvertisedNetwork(iface, address),
                iface.getName(),
                address.getIp(),
                _process.getIntraAreaAdminCost(),
                cost,
                area.getAreaNumber()));
      }
    }
  }

  /**
   * Recompute locally originated OSPFv3 external advertisements.
   *
   * <p>These advertisements are intentionally separate from the local
   * OSPFv3 routing RIB. A router must advertise redistributed/static/default
   * routes to neighbors without preferring its own OSPF copy over the source
   * route that caused the advertisement.
   *
   * @return true iff the local advertisement set changed
   */
  boolean refreshLocalExternalAdvertisements(
      ConnectedRib6 connectedRib,
      Set<StaticRoute6> staticRoutes,
      boolean nonOspfv3DefaultRoutePresent) {

    Set<AbstractRoute6> desired =
        new HashSet<>();

    if (!_process.getEnabled()) {
      if (_localExternalAdvertisements.isEmpty()) {
        return false;
      }

      _localExternalAdvertisements =
          ImmutableSet.of();
      return true;
    }

    if (_process.getRedistributeConnected()) {
      for (ConnectedRoute6 connected :
          connectedRib.getRoutes()) {

        if (isInternallyOriginatedConnectedRoute(
            connected)) {
          continue;
        }

        if (!permitsOutboundRedistribution(
            connected.getNetwork())) {
          continue;
        }

        Optional<RouteMap6.Result> transformed =
            applyRedistributionRouteMap(
                _process
                    .getRedistributeConnectedRouteMap(),
                connected.getNetwork(),
                _process.getRedistributionMetric(),
                Route.UNSET_ROUTE_TAG);

        if (transformed.isEmpty()) {
          continue;
        }

        RouteMap6.Result result =
            transformed.get();

        addLocallyOriginatedExternalAdvertisements(
            desired,
            connected.getNetwork(),
            result.getMetric(),
            result.getTag(),
            result.getOspfMetricType());
      }
    }

    if (_process.getRedistributeStatic()) {
      for (StaticRoute6 route : staticRoutes) {
        if (!permitsOutboundRedistribution(
            route.getNetwork())) {
          continue;
        }

        Optional<RouteMap6.Result> transformed =
            applyRedistributionRouteMap(
                _process
                    .getRedistributeStaticRouteMap(),
                route.getNetwork(),
                _process.getRedistributionMetric(),
                route.getTag());

        if (transformed.isEmpty()) {
          continue;
        }

        RouteMap6.Result result =
            transformed.get();

        addLocallyOriginatedExternalAdvertisements(
            desired,
            route.getNetwork(),
            result.getMetric(),
            result.getTag(),
            result.getOspfMetricType());
      }
    }

    if (_process.getDefaultInformationOriginate()
        && (_process
                .getDefaultInformationOriginateAlways()
            || nonOspfv3DefaultRoutePresent)) {
      addLocallyOriginatedExternalAdvertisements(
          desired,
          Prefix6.ZERO,
          _process.getDefaultInformationMetric(),
          Route.UNSET_ROUTE_TAG,
          OspfMetricType.E2);
    }

    Set<AbstractRoute6> immutableDesired =
        ImmutableSet.copyOf(desired);

    if (_localExternalAdvertisements.equals(
        immutableDesired)) {
      return false;
    }

    _localExternalAdvertisements =
        immutableDesired;

    return true;
  }

  /**
   * Originate Type-5 advertisements toward normal areas and Type-7
   * advertisements toward each NSSA.
   *
   * <p>Ordinary stub areas receive neither external LSA type.
   */
  private void addLocallyOriginatedExternalAdvertisements(
      Set<AbstractRoute6> advertisements,
      Prefix6 network,
      long metric,
      long tag,
      OspfMetricType metricType) {

    boolean hasNormalArea =
        _process
            .getAreas()
            .values()
            .stream()
            .anyMatch(
                area ->
                    !area.getStub()
                        && !area.getNssa());

    if (hasNormalArea) {
      if (metricType == OspfMetricType.E1) {
        advertisements.add(
            new Ospfv3ExternalType1Route6(
                network,
                Route.UNSET_NEXT_HOP_INTERFACE,
                _process.getExternalAdminCost(),
                metric,
                _process.getRouterId(),
                tag));
      } else {
        advertisements.add(
            new Ospfv3ExternalType2Route6(
                network,
                Route.UNSET_NEXT_HOP_INTERFACE,
                _process.getExternalAdminCost(),
                metric,
                _process.getRouterId(),
                tag));
      }
    }

    /*
     * N1 support is intentionally separate from Type-5 E1 support.
     * Preserve the existing NSSA N2 behavior until N1 is modeled.
     */
    _process
        .getAreas()
        .values()
        .stream()
        .filter(Ospfv3Area::getNssa)
        .forEach(
            area ->
                advertisements.add(
                    new Ospfv3NssaExternalType2Route6(
                        network,
                        Route.UNSET_NEXT_HOP_INTERFACE,
                        _process.getExternalAdminCost(),
                        metric,
                        area.getAreaNumber(),
                        _process.getRouterId(),
                        tag)));
  }

  private boolean permitsOutboundRedistribution(
      Prefix6 prefix) {
    PrefixList6 distributeList =
        _process.getOutboundDistributeList();

    return distributeList == null
        || distributeList.permits(prefix);
  }

  private static Optional<RouteMap6.Result>
      applyRedistributionRouteMap(
          @Nullable RouteMap6 routeMap,
          Prefix6 prefix,
          long initialMetric,
          long initialTag) {

    Optional<RouteMap6.Result> result =
        routeMap == null
            ? Optional.of(
                new RouteMap6.Result(
                    initialMetric,
                    initialTag))
            : routeMap.process(
                prefix,
                initialMetric,
                initialTag);

    if (result.isEmpty()) {
      return Optional.empty();
    }

    RouteMap6.Result transformed =
        result.get();

    if (transformed.getMetric() < 0L
        || transformed.getMetric()
            > Ospfv3Process.MAX_METRIC) {
      return Optional.empty();
    }

    long tag =
        transformed.getTag();

    if (tag != Route.UNSET_ROUTE_TAG
        && (tag < 0L
            || tag > AbstractRoute6.MAX_TAG)) {
      return Optional.empty();
    }

    return result;
  }

  private boolean isInternallyOriginatedConnectedRoute(
      ConnectedRoute6 route) {
    Interface iface =
        _c.getAllInterfaces().get(
            route.getNextHopInterface());

    if (iface == null || !isEnabledForThisProcess(iface)) {
      return false;
    }

    return iface.getAllConcreteAddresses6().stream()
        .map(
            address ->
                getAdvertisedNetwork(iface, address))
        .anyMatch(route.getNetwork()::equals);
  }

  /**
   * Import active intra-area routes from currently adjacent OSPFv3 neighbors.
   *
   * @return true iff this process's active OSPFv3 route set changed
   */
  boolean propagateRoutes(
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {
    if (!_process.getEnabled()) {
      return false;
    }

    boolean changed = false;

    for (Interface localIface :
        _c.getAllInterfaces().values()) {

      if (!isAdjacencyInterface(localIface)) {
        continue;
      }

      Ospfv3InterfaceSettings localSettings =
          localIface.getOspfv3Settings();

      assert localSettings != null;
      assert localSettings.getAreaName() != null;

      NodeInterfacePair localId =
          NodeInterfacePair.of(
              _c.getHostname(), localIface.getName());

      BroadcastElection broadcastElection =
          localSettings.getNetworkType()
                  == OspfNetworkType.BROADCAST
              ? electBroadcastDesignatedRouters(
                  localIface,
                  localId,
                  allNodes,
                  l3Adjacencies)
              : BroadcastElection.empty();

      for (Node remoteNode : allNodes.values()) {
        Configuration remoteConfig =
            remoteNode.getConfiguration();

        // An OSPF router never establishes an adjacency with itself.
        if (_c.getHostname()
            .equals(remoteConfig.getHostname())) {
          continue;
        }

        for (Interface remoteIface :
            remoteConfig.getAllInterfaces().values()) {

          if (!remoteIface.getActive()) {
            continue;
          }

          Ospfv3InterfaceSettings remoteSettings =
              remoteIface.getOspfv3Settings();

          if (!areInterfaceSettingsCompatible(
              localSettings, remoteSettings)) {
            continue;
          }

          NodeInterfacePair remoteId =
              NodeInterfacePair.of(
                  remoteConfig.getHostname(),
                  remoteIface.getName());

          if (!areTopologicallyAdjacent(
              localId,
              localIface,
              remoteId,
              remoteIface,
              l3Adjacencies)) {
            continue;
          }

          if (remoteSettings == null
              || remoteSettings.getProcess() == null) {
            continue;
          }

          VirtualRouter remoteVr =
              remoteNode
                  .getVirtualRouter(
                      remoteIface.getVrfName())
                  .orElse(null);

          if (remoteVr == null) {
            continue;
          }

          Ospfv3RoutingProcess remoteProcess =
              remoteVr
                  .getOspfv3Processes()
                  .get(remoteSettings.getProcess());

          if (remoteProcess == null) {
            continue;
          }

          long localArea =
              localSettings.getAreaName();

          /*
           * The OSPF stub capability must agree between neighbors.
           * no-summary is intentionally not compared: that is an
           * ABR advertisement policy, not a Hello compatibility bit.
           */
          if (!areAreaTypesCompatible(
              localArea,
              remoteProcess)) {
            continue;
          }

          if (!shouldFormFullAdjacency(
              localSettings,
              remoteSettings,
              localId,
              remoteId,
              broadcastElection)) {
            continue;
          }

          Set<AbstractRoute6> remoteRoutes =
              remoteProcess.getRoutes();

          changed |=
              importRestrictedAreaDefaultFromNeighbor(
                  localIface,
                  remoteIface,
                  localArea,
                  remoteProcess);

          changed |=
              importIntraAreaRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteRoutes);

          changed |=
              importInterAreaRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteProcess,
                  remoteRoutes);

          changed |=
              importExternalRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteRoutes);

          changed |=
              importNssaExternalRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteRoutes);
        }
      }
    }

    changed |=
        refreshTranslatedNssaAdvertisements();

    return changed;
  }

  /**
   * Elect DR and BDR for the broadcast segment containing {@code localIface}.
   *
   * <p>Interfaces with priority zero participate in OSPF but are ineligible
   * for DR/BDR election. Eligible candidates are ordered by priority, then
   * router ID. A final interface-ID comparison provides deterministic behavior
   * for invalid configurations containing duplicate router IDs.
   */
  @VisibleForTesting
  BroadcastElection electBroadcastDesignatedRouters(
      Interface localIface,
      NodeInterfacePair localId,
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    Ospfv3InterfaceSettings localSettings =
        localIface.getOspfv3Settings();

    if (localSettings == null
        || localSettings.getNetworkType()
            != OspfNetworkType.BROADCAST
        || localSettings.getAreaName() == null) {
      return BroadcastElection.empty();
    }

    List<BroadcastElectionCandidate> candidates =
        new ArrayList<>();

    if (localSettings.getPriority() > 0) {
      candidates.add(
          new BroadcastElectionCandidate(
              localId,
              localSettings.getPriority(),
              _process.getRouterId()));
    }

    long localArea =
        localSettings.getAreaName();

    for (Node remoteNode :
        allNodes.values()) {

      Configuration remoteConfig =
          remoteNode.getConfiguration();

      if (_c.getHostname()
          .equals(remoteConfig.getHostname())) {
        continue;
      }

      for (Interface remoteIface :
          remoteConfig
              .getAllInterfaces()
              .values()) {

        if (!remoteIface.getActive()) {
          continue;
        }

        Ospfv3InterfaceSettings remoteSettings =
            remoteIface.getOspfv3Settings();

        if (remoteSettings == null
            || remoteSettings.getNetworkType()
                != OspfNetworkType.BROADCAST
            || remoteSettings.getPriority() == 0
            || !areInterfaceSettingsCompatible(
                localSettings,
                remoteSettings)
            || remoteSettings.getProcess()
                == null) {
          continue;
        }

        NodeInterfacePair remoteId =
            NodeInterfacePair.of(
                remoteConfig.getHostname(),
                remoteIface.getName());

        if (!areTopologicallyAdjacent(
            localId,
            localIface,
            remoteId,
            remoteIface,
            l3Adjacencies)) {
          continue;
        }

        VirtualRouter remoteVr =
            remoteNode
                .getVirtualRouter(
                    remoteIface.getVrfName())
                .orElse(null);

        if (remoteVr == null) {
          continue;
        }

        Ospfv3RoutingProcess remoteProcess =
            remoteVr
                .getOspfv3Processes()
                .get(
                    remoteSettings.getProcess());

        if (remoteProcess == null
            || !remoteProcess
                ._process
                .getEnabled()
            || !areAreaTypesCompatible(
                localArea,
                remoteProcess)) {
          continue;
        }

        candidates.add(
            new BroadcastElectionCandidate(
                remoteId,
                remoteSettings.getPriority(),
                remoteProcess
                    ._process
                    .getRouterId()));
      }
    }

    candidates.sort(
        (lhs, rhs) -> {
          int priority =
              Integer.compare(
                  rhs._priority,
                  lhs._priority);

          if (priority != 0) {
            return priority;
          }

          int routerId =
              rhs._routerId.compareTo(
                  lhs._routerId);

          if (routerId != 0) {
            return routerId;
          }

          return rhs
              ._id
              .toString()
              .compareTo(
                  lhs._id.toString());
        });

    NodeInterfacePair dr =
        candidates.isEmpty()
            ? null
            : candidates.get(0)._id;

    NodeInterfacePair bdr =
        candidates.size() < 2
            ? null
            : candidates.get(1)._id;

    return new BroadcastElection(
        dr,
        bdr);
  }

  /**
   * Return whether two compatible neighbors exchange the full OSPF database.
   *
   * <p>Point-to-point neighbors remain fully adjacent. On a broadcast
   * network, DR and BDR form full adjacencies with all routers, while two
   * DROTHER routers remain in 2-Way state and do not exchange LSAs directly.
   */
  private static boolean shouldFormFullAdjacency(
      Ospfv3InterfaceSettings localSettings,
      Ospfv3InterfaceSettings remoteSettings,
      NodeInterfacePair localId,
      NodeInterfacePair remoteId,
      BroadcastElection election) {

    if (localSettings.getNetworkType()
            != OspfNetworkType.BROADCAST
        || remoteSettings.getNetworkType()
            != OspfNetworkType.BROADCAST) {
      return true;
    }

    return election.isDrOrBdr(localId)
        || election.isDrOrBdr(remoteId);
  }

  private boolean areAreaTypesCompatible(
      long area,
      Ospfv3RoutingProcess remoteProcess) {
    Ospfv3Area localArea =
        _process.getAreas().get(area);

    Ospfv3Area remoteArea =
        remoteProcess
            ._process
            .getAreas()
            .get(area);

    return localArea != null
        && remoteArea != null
        && localArea.getStub()
            == remoteArea.getStub()
        && localArea.getNssa()
            == remoteArea.getNssa();
  }

  private boolean isStubArea(long area) {
    Ospfv3Area settings =
        _process.getAreas().get(area);

    return settings != null
        && settings.getStub();
  }

  private boolean isNssaArea(long area) {
    Ospfv3Area settings =
        _process.getAreas().get(area);

    return settings != null
        && settings.getNssa();
  }

  private boolean isRestrictedArea(long area) {
    return isStubArea(area)
        || isNssaArea(area);
  }

  private boolean isAreaBorderRouterFor(
      long area) {
    return area != 0L
        && _process.getAreas().containsKey(0L)
        && _process.getAreas().containsKey(area);
  }

  private boolean suppressesInterAreaInto(
      long area) {
    Ospfv3Area settings =
        _process.getAreas().get(area);

    return settings != null
        && (settings.getStub()
            || settings.getNssa())
        && settings.getSuppressInterArea()
        && isAreaBorderRouterFor(area);
  }

  private boolean shouldSuppressInterAreaRoute(
      long area,
      Ospfv3RoutingProcess remoteProcess) {
    Ospfv3Area localArea =
        _process.getAreas().get(area);

    boolean localSuppression =
        localArea != null
            && (localArea.getStub()
                || localArea.getNssa())
            && localArea.getSuppressInterArea();

    return localSuppression
        || remoteProcess
            .suppressesInterAreaInto(area);
  }

  /**
   * Install the default summary originated by an ABR toward a stub area.
   */
  private boolean importRestrictedAreaDefaultFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Ospfv3RoutingProcess remoteProcess) {

    if (!isRestrictedArea(area)
        || !remoteProcess
            .isAreaBorderRouterFor(area)) {
      return false;
    }

    Ospfv3Area remoteArea =
        remoteProcess
            ._process
            .getAreas()
            .get(area);

    if (remoteArea == null) {
      return false;
    }

    long defaultMetric =
        remoteArea.getDefaultMetric();

    long incrementalCost =
        computeInterfaceCost(localIface);

    if (defaultMetric >= LS_INFINITY
        || incrementalCost
            >= LS_INFINITY - defaultMetric) {
      return false;
    }

    @Nullable Ip6 peerIp =
        findPeerNextHopIp(
                localIface,
                remoteIface)
            .orElse(null);

    return _ospfv3Rib.mergeRoute(
        new Ospfv3InterAreaRoute6(
            Prefix6.ZERO,
            localIface.getName(),
            peerIp,
            _process.getInterAreaAdminCost(),
            incrementalCost
                + defaultMetric,
            area));
  }

  private boolean isAdjacencyInterface(Interface iface) {
    if (!iface.getActive()
        || !_vrfName.equals(iface.getVrfName())
        || !isEnabledForThisProcess(iface)) {
      return false;
    }

    Ospfv3InterfaceSettings settings =
        iface.getOspfv3Settings();

    return settings != null
        && !settings.getPassive()
        && settings.getAreaName() != null;
  }

  @VisibleForTesting
  static boolean areInterfaceSettingsCompatible(
      Ospfv3InterfaceSettings local,
      @Nullable Ospfv3InterfaceSettings remote) {
    if (remote == null
        || !local.getEnabled()
        || !remote.getEnabled()
        || local.getPassive()
        || remote.getPassive()
        || local.getAreaName() == null
        || remote.getAreaName() == null
        || !Objects.equals(
            local.getAreaName(), remote.getAreaName())
        || local.getHelloInterval()
            != remote.getHelloInterval()
        || local.getDeadInterval()
            != remote.getDeadInterval()) {
      return false;
    }

    OspfNetworkType localType =
        local.getNetworkType();
    OspfNetworkType remoteType =
        remote.getNetworkType();

    return localType == null
        || remoteType == null
        || localType == remoteType;
  }

  private static boolean areTopologicallyAdjacent(
      NodeInterfacePair localId,
      Interface localIface,
      NodeInterfacePair remoteId,
      Interface remoteIface,
      L3Adjacencies l3Adjacencies) {

    // When L1/L2 information proves a physical point-to-point pairing, no
    // global IPv6 address is required. This is important for OSPFv3 links
    // configured with link-local addressing only.
    if (l3Adjacencies.inSamePointToPointDomain(
        localId, remoteId)) {
      return true;
    }

    if (!l3Adjacencies.inSameBroadcastDomain(
        localId, remoteId)) {
      return false;
    }

    // Without an explicit point-to-point pairing, require a matching concrete
    // IPv6 network. This prevents GlobalBroadcastNoPointToPoint from turning
    // every OSPFv3 interface in the network into a neighbor.
    return haveMatchingIpv6Network(
        localIface, remoteIface);
  }

  private static boolean haveMatchingIpv6Network(
      Interface lhs, Interface rhs) {
    for (ConcreteInterfaceAddress6 left :
        lhs.getAllConcreteAddresses6()) {
      for (ConcreteInterfaceAddress6 right :
          rhs.getAllConcreteAddresses6()) {
        if (left.getPrefix().equals(right.getPrefix())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return whether a route learned through the sender's interface should be
   * suppressed when received back from that sender.
   *
   * <p>Traditional split horizon remains useful for this route-level
   * approximation on point-to-point links. Broadcast OSPF is different:
   * a DR/BDR must be able to flood an LSA received on a broadcast interface
   * back onto that same interface toward other fully adjacent routers.
   */
  private static boolean shouldApplySplitHorizon(
      Interface remoteIface,
      AbstractRoute6 route) {

    if (!remoteIface
        .getName()
        .equals(
            route.getNextHopInterface())) {
      return false;
    }

    Ospfv3InterfaceSettings settings =
        remoteIface.getOspfv3Settings();

    return settings == null
        || settings.getNetworkType()
            != OspfNetworkType.BROADCAST;
  }

  private boolean importIntraAreaRoutesFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Set<AbstractRoute6> remoteRoutes) {
    boolean changed = false;
    long incrementalCost =
        computeInterfaceCost(localIface);

    @Nullable Ip6 peerIp =
        findPeerNextHopIp(
                localIface, remoteIface)
            .orElse(null);

    for (AbstractRoute6 route : remoteRoutes) {
      if (!(route
          instanceof Ospfv3IntraAreaRoute6)) {
        continue;
      }

      Ospfv3IntraAreaRoute6 intra =
          (Ospfv3IntraAreaRoute6) route;

      if (intra.getArea() != area) {
        continue;
      }

      // Split horizon. A route whose next hop on the remote router is the
      // interface facing us was learned from us and should not be sent back.
      // This also suppresses the shared transit prefix, which is already
      // directly connected on our side.
      if (shouldApplySplitHorizon(
          remoteIface,
          intra)) {
        continue;
      }

      if (intra.getMetric() >= LS_INFINITY
          || incrementalCost
              >= LS_INFINITY - intra.getMetric()) {
        continue;
      }

      long newMetric =
          intra.getMetric() + incrementalCost;

      changed |=
          _ospfv3Rib.mergeRoute(
              new Ospfv3IntraAreaRoute6(
                  intra.getNetwork(),
                  localIface.getName(),
                  peerIp,
                  _process.getIntraAreaAdminCost(),
                  newMetric,
                  area,
                  intra.getTag()));
    }

    return changed;
  }

  /**
   * Import OSPFv3 inter-area routes.
   *
   * <p>Within an area, summary routes accumulate the local cost toward the
   * advertising ABR. An ABR may translate routes between an attached
   * non-backbone area and area 0. Traffic between two non-backbone areas must
   * therefore cross the backbone rather than being leaked directly.
   *
   * <p>When the advertising ABR has an inter-area range on the source area,
   * matching intra-area routes are replaced by the configured summary. The
   * summary metric is the largest active component metric at that ABR.
   * A no-advertise range suppresses both the summary and its component
   * specifics across the area boundary.
   */
  private boolean importInterAreaRoutesFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long localArea,
      Ospfv3RoutingProcess remoteProcess,
      Set<AbstractRoute6> remoteRoutes) {

    boolean changed = false;

    long incrementalCost =
        computeInterfaceCost(localIface);

    @Nullable Ip6 peerIp =
        findPeerNextHopIp(
                localIface,
                remoteIface)
            .orElse(null);

    for (AbstractRoute6 route :
        remoteRoutes) {

      /*
       * A totally-stubby or no-summary NSSA ABR suppresses ordinary
       * inter-area summaries other than the injected default.
       */
      if (!route.getNetwork().equals(
              Prefix6.ZERO)
          && shouldSuppressInterAreaRoute(
              localArea,
              remoteProcess)) {
        continue;
      }

      long sourceArea;
      long remoteMetric;
      long tag;

      Prefix6 advertisedNetwork =
          route.getNetwork();

      if (route
          instanceof Ospfv3IntraAreaRoute6) {

        Ospfv3IntraAreaRoute6 intra =
            (Ospfv3IntraAreaRoute6) route;

        sourceArea =
            intra.getArea();

        /*
         * Same-area routes remain intra-area and were handled by
         * importIntraAreaRoutesFromNeighbor().
         */
        if (sourceArea == localArea) {
          continue;
        }

        if (!remoteProcess
            .canAdvertiseBetweenAreas(
                sourceArea,
                localArea)) {
          continue;
        }

        remoteMetric =
            intra.getMetric();

        tag =
            intra.getTag();

        /*
         * Area range summarization is applied by the advertising ABR
         * when an intra-area route crosses out of its source area.
         */
        @Nullable Ospfv3AreaRange range =
            remoteProcess
                .getMatchingAreaRange(
                    sourceArea,
                    intra.getNetwork(),
                    Ospfv3AreaRange.Type.INTER_AREA);

        if (range != null) {

          if (!range.getAdvertise()) {
            continue;
          }

          long summaryMetric =
              computeInterAreaSummaryMetric(
                  sourceArea,
                  range,
                  remoteRoutes);

          if (summaryMetric < 0L) {
            continue;
          }

          advertisedNetwork =
              range.getPrefix();

          remoteMetric =
              summaryMetric;

          /*
           * A range summarizes potentially unrelated component route
           * metadata, so do not copy a component tag onto the summary.
           */
          tag =
              Route.UNSET_ROUTE_TAG;
        }

      } else if (
          route
              instanceof Ospfv3InterAreaRoute6) {

        Ospfv3InterAreaRoute6 inter =
            (Ospfv3InterAreaRoute6) route;

        sourceArea =
            inter.getArea();

        /*
         * Propagation inside the area does not require the remote router
         * to be an ABR. Crossing into a different area does.
         */
        if (sourceArea != localArea
            && !remoteProcess
                .canAdvertiseBetweenAreas(
                    sourceArea,
                    localArea)) {
          continue;
        }

        remoteMetric =
            inter.getMetric();

        tag =
            inter.getTag();

      } else {
        continue;
      }

      if (shouldApplySplitHorizon(
          remoteIface,
          route)) {
        continue;
      }

      if (remoteMetric >= LS_INFINITY
          || incrementalCost
              >= LS_INFINITY
                  - remoteMetric) {
        continue;
      }

      changed |=
          _ospfv3Rib.mergeRoute(
              new Ospfv3InterAreaRoute6(
                  advertisedNetwork,
                  localIface.getName(),
                  peerIp,
                  _process
                      .getInterAreaAdminCost(),
                  remoteMetric
                      + incrementalCost,
                  localArea,
                  tag));
    }

    return changed;
  }

  /**
   * Return the most-specific configured inter-area range that contains a
   * source-area route.
   */
  /**
   * Return the most-specific configured range of {@code type} that contains
   * the supplied route prefix.
   */
  private @Nullable Ospfv3AreaRange
      getMatchingAreaRange(
          long areaNumber,
          Prefix6 network,
          Ospfv3AreaRange.Type type) {

    Ospfv3Area area =
        _process
            .getAreas()
            .get(areaNumber);

    if (area == null) {
      return null;
    }

    Ospfv3AreaRange best =
        null;

    for (Ospfv3AreaRange range :
        area.getRanges()) {

      if (range.getType() != type
          || !prefixContains(
              range.getPrefix(),
              network)) {
        continue;
      }

      if (best == null
          || range
                  .getPrefix()
                  .getPrefixLength()
              > best
                  .getPrefix()
                  .getPrefixLength()) {

        best =
            range;
      }
    }

    return best;
  }

  private static boolean prefixContains(
      Prefix6 container,
      Prefix6 contained) {

    return contained.getPrefixLength()
            >= container.getPrefixLength()
        && container.contains(
            contained.getNetworkAddress());
  }

  /**
   * Compute the range metric from active component intra-area routes.
   *
   * @return the largest matching metric, or -1 when no component exists
   */
  private static long computeInterAreaSummaryMetric(
      long sourceArea,
      Ospfv3AreaRange range,
      Set<AbstractRoute6> routes) {

    long maximumMetric =
        -1L;

    for (AbstractRoute6 route :
        routes) {

      if (!(route
          instanceof Ospfv3IntraAreaRoute6)) {
        continue;
      }

      Ospfv3IntraAreaRoute6 intra =
          (Ospfv3IntraAreaRoute6) route;

      if (intra.getArea()
              != sourceArea
          || !prefixContains(
              range.getPrefix(),
              intra.getNetwork())) {
        continue;
      }

      maximumMetric =
          Math.max(
              maximumMetric,
              intra.getMetric());
    }

    return maximumMetric;
  }

  /**
   * Return whether this process can act as the ABR transition between two
   * areas.
   *
   * <p>Inter-area OSPF traffic must traverse area 0. A router attached only to
   * two non-backbone areas is therefore not treated as a valid transit ABR.
   */
  @VisibleForTesting
  boolean canAdvertiseBetweenAreas(
      long sourceArea,
      long targetArea) {

    if (sourceArea == targetArea) {
      return false;
    }

    return _process.getAreas().containsKey(0L)
        && _process.getAreas().containsKey(sourceArea)
        && _process.getAreas().containsKey(targetArea)
        && (sourceArea == 0L || targetArea == 0L);
  }

  /**
   * Import OSPFv3 Type-5 external routes from a neighbor.
   *
   * <p>E1 routes add the receiving interface cost to both the total route
   * metric and the separately tracked internal cost to the originating ASBR.
   * E2 routes keep their external metric constant and accumulate only the
   * cost-to-advertiser tie breaker.
   */
  private boolean importExternalRoutesFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Set<AbstractRoute6> remoteRoutes) {

    // Type-5 external LSAs are not flooded into stub or NSSA areas.
    if (isRestrictedArea(area)) {
      return false;
    }

    boolean changed = false;

    long incrementalCost =
        computeInterfaceCost(localIface);

    @Nullable Ip6 peerIp =
        findPeerNextHopIp(
                localIface,
                remoteIface)
            .orElse(null);

    for (AbstractRoute6 route :
        remoteRoutes) {

      if (route
          instanceof Ospfv3ExternalType1Route6) {

        Ospfv3ExternalType1Route6 external =
            (Ospfv3ExternalType1Route6) route;

        // Do not accept our own external advertisement back.
        if (external
                .getAdvertiser()
                .equals(_process.getRouterId())
            && external.getCostToAdvertiser() != 0L) {
          continue;
        }

        if (shouldApplySplitHorizon(
            remoteIface,
            external)) {
          continue;
        }

        if (external.getMetric()
                >= LS_INFINITY
            || external.getCostToAdvertiser()
                >= LS_INFINITY
            || incrementalCost
                >= LS_INFINITY
                    - external.getMetric()
            || incrementalCost
                >= LS_INFINITY
                    - external.getCostToAdvertiser()) {
          continue;
        }

        long newMetric =
            external.getMetric()
                + incrementalCost;

        long newCostToAdvertiser =
            external.getCostToAdvertiser()
                + incrementalCost;

        changed |=
            _ospfv3Rib.mergeRoute(
                new Ospfv3ExternalType1Route6(
                    external.getNetwork(),
                    localIface.getName(),
                    peerIp,
                    _process.getExternalAdminCost(),
                    newMetric,
                    external.getLsaMetric(),
                    area,
                    newCostToAdvertiser,
                    external.getAdvertiser(),
                    external.getTag()));

        continue;
      }

      if (!(route
          instanceof Ospfv3ExternalType2Route6)) {
        continue;
      }

      Ospfv3ExternalType2Route6 external =
          (Ospfv3ExternalType2Route6) route;

      // Do not accept our own external advertisement back.
      if (external
              .getAdvertiser()
              .equals(_process.getRouterId())
          && external.getCostToAdvertiser() != 0L) {
        continue;
      }

      if (shouldApplySplitHorizon(
          remoteIface,
          external)) {
        continue;
      }

      if (external.getCostToAdvertiser()
              >= LS_INFINITY
          || incrementalCost
              >= LS_INFINITY
                  - external.getCostToAdvertiser()) {
        continue;
      }

      long newCostToAdvertiser =
          external.getCostToAdvertiser()
              + incrementalCost;

      changed |=
          _ospfv3Rib.mergeRoute(
              new Ospfv3ExternalType2Route6(
                  external.getNetwork(),
                  localIface.getName(),
                  peerIp,
                  _process.getExternalAdminCost(),
                  external.getMetric(),
                  area,
                  newCostToAdvertiser,
                  external.getAdvertiser(),
                  external.getTag()));
    }

    return changed;
  }

  /**
   * Import NSSA Type-7 external routes inside their originating NSSA.
   *
   * <p>An ABR attached to area 0 translates a received Type-7 advertisement
   * into a locally originated Type-5 advertisement for normal areas. The
   * translated advertisement is control-plane state only and is not installed
   * as an additional local forwarding candidate.
   */
  private boolean importNssaExternalRoutesFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Set<AbstractRoute6> remoteRoutes) {

    if (!isNssaArea(area)) {
      return false;
    }

    boolean changed = false;

    long incrementalCost =
        computeInterfaceCost(localIface);

    @Nullable Ip6 peerIp =
        findPeerNextHopIp(
                localIface,
                remoteIface)
            .orElse(null);

    for (AbstractRoute6 route : remoteRoutes) {
      if (!(route
          instanceof Ospfv3NssaExternalType2Route6)) {
        continue;
      }

      Ospfv3NssaExternalType2Route6 external =
          (Ospfv3NssaExternalType2Route6) route;

      if (external.getArea() != area) {
        continue;
      }

      if (external
              .getAdvertiser()
              .equals(_process.getRouterId())
          && external.getCostToAdvertiser() != 0L) {
        continue;
      }

      if (shouldApplySplitHorizon(
          remoteIface,
          external)) {
        continue;
      }

      if (external.getCostToAdvertiser()
              >= LS_INFINITY
          || incrementalCost
              >= LS_INFINITY
                  - external.getCostToAdvertiser()) {
        continue;
      }

      long newCostToAdvertiser =
          external.getCostToAdvertiser()
              + incrementalCost;

      Ospfv3NssaExternalType2Route6 learned =
          new Ospfv3NssaExternalType2Route6(
              external.getNetwork(),
              localIface.getName(),
              peerIp,
              _process.getExternalAdminCost(),
              external.getMetric(),
              area,
              newCostToAdvertiser,
              external.getAdvertiser(),
              external.getTag());

      changed |=
          _ospfv3Rib.mergeRoute(learned);

    }

    return changed;
  }

  /**
   * Rebuild Type-7 to Type-5 translations from the ABR's complete
   * currently visible NSSA state.
   */
  private boolean refreshTranslatedNssaAdvertisements() {

    Set<Ospfv3NssaExternalType2Route6>
        type7Routes =
            new HashSet<>();

    for (AbstractRoute6 route :
        _ospfv3Rib.getRoutes()) {

      if (route
          instanceof Ospfv3NssaExternalType2Route6) {

        type7Routes.add(
            (Ospfv3NssaExternalType2Route6)
                route);
      }
    }

    /*
     * An ABR may also be an ASBR.
     */
    for (AbstractRoute6 route :
        _localExternalAdvertisements) {

      if (route
          instanceof Ospfv3NssaExternalType2Route6) {

        type7Routes.add(
            (Ospfv3NssaExternalType2Route6)
                route);
      }
    }

    Set<Ospfv3ExternalType2Route6> desired =
        new HashSet<>();

    Set<String> emittedRanges =
        new HashSet<>();

    for (Ospfv3NssaExternalType2Route6 external :
        type7Routes) {

      long area =
          external.getArea();

      if (!isAreaBorderRouterFor(area)) {
        continue;
      }

      @Nullable Ospfv3AreaRange range =
          getMatchingAreaRange(
              area,
              external.getNetwork(),
              Ospfv3AreaRange.Type.NSSA);

      if (range == null) {

        desired.add(
            toTranslatedType5(
                external.getNetwork(),
                external.getMetric(),
                external.getTag()));

        continue;
      }

      if (!range.getAdvertise()) {
        continue;
      }

      String rangeKey =
          Long.toUnsignedString(area)
              + "|"
              + range.getPrefix();

      if (!emittedRanges.add(rangeKey)) {
        continue;
      }

      @Nullable Ospfv3ExternalType2Route6 aggregate =
          computeNssaRangeTranslation(
              area,
              range,
              type7Routes);

      if (aggregate != null) {
        desired.add(aggregate);
      }
    }

    Set<Ospfv3ExternalType2Route6>
        immutableDesired =
            ImmutableSet.copyOf(desired);

    if (_translatedNssaExternalAdvertisements
        .equals(immutableDesired)) {
      return false;
    }

    _translatedNssaExternalAdvertisements =
        immutableDesired;

    return true;
  }

  private @Nullable Ospfv3ExternalType2Route6
      computeNssaRangeTranslation(
          long area,
          Ospfv3AreaRange range,
          Set<Ospfv3NssaExternalType2Route6>
              type7Routes) {

    int contributors =
        0;

    long maximumMetric =
        -1L;

    Ospfv3NssaExternalType2Route6
        soleContributor =
            null;

    for (Ospfv3NssaExternalType2Route6 external :
        type7Routes) {

      if (external.getArea() != area) {
        continue;
      }

      Ospfv3AreaRange bestRange =
          getMatchingAreaRange(
              area,
              external.getNetwork(),
              Ospfv3AreaRange.Type.NSSA);

      if (!range.equals(bestRange)) {
        continue;
      }

      contributors++;

      soleContributor =
          external;

      maximumMetric =
          Math.max(
              maximumMetric,
              external.getMetric());
    }

    if (contributors == 0) {
      return null;
    }

    if (contributors == 1
        && soleContributor != null
        && soleContributor
            .getNetwork()
            .equals(range.getPrefix())) {

      return toTranslatedType5(
          range.getPrefix(),
          soleContributor.getMetric(),
          soleContributor.getTag());
    }

    if (maximumMetric < 0L
        || maximumMetric
            >= Ospfv3Process.MAX_METRIC) {
      return null;
    }

    return toTranslatedType5(
        range.getPrefix(),
        maximumMetric + 1L,
        Route.UNSET_ROUTE_TAG);
  }

  private Ospfv3ExternalType2Route6
      toTranslatedType5(
          Prefix6 network,
          long metric,
          long tag) {

    return new Ospfv3ExternalType2Route6(
        network,
        Route.UNSET_NEXT_HOP_INTERFACE,
        _process.getExternalAdminCost(),
        metric,
        _process.getRouterId(),
        tag);
  }

  /**
   * Return the remote IPv6 address on the common numbered link when one is
   * modeled. OSPFv3 normally uses a link-local next hop; until explicit IPv6
   * link-local addresses are represented, the peer's concrete address is the
   * best available next-hop identity. Interface-only next hops are retained
   * for link-local-only links.
   */
  private static Optional<Ip6> findPeerNextHopIp(
      Interface localIface, Interface remoteIface) {

    for (ConcreteInterfaceAddress6 localAddress :
        localIface.getAllConcreteAddresses6()) {
      for (ConcreteInterfaceAddress6 remoteAddress :
          remoteIface.getAllConcreteAddresses6()) {
        if (localAddress
            .getPrefix()
            .equals(remoteAddress.getPrefix())) {
          return Optional.of(remoteAddress.getIp());
        }
      }
    }

    return Optional.empty();
  }

  private boolean isEnabledForThisProcess(Interface iface) {
    Ospfv3InterfaceSettings settings =
        iface.getOspfv3Settings();

    return settings != null
        && settings.getEnabled()
        && _process
            .getProcessId()
            .equals(settings.getProcess());
  }

  /**
   * Compute effective interface cost.
   *
   * <p>Explicit interface cost wins. AOS-CX uses 1 Gbps as the calculated
   * link speed for VLAN interfaces. For other interfaces with known bandwidth,
   * reference-bandwidth/link-bandwidth is used.
   */
  @VisibleForTesting
  long computeInterfaceCost(Interface iface) {
    Ospfv3InterfaceSettings settings =
        iface.getOspfv3Settings();

    assert settings != null;

    if (settings.getCost() != null) {
      return settings.getCost();
    }

    if (iface.isLoopback()) {
      return 1L;
    }

    Double bandwidth =
        iface.getInterfaceType() == InterfaceType.VLAN
            ? 1_000_000_000D
            : iface.getBandwidth();

    if (bandwidth == null || bandwidth <= 0D) {
      // Link speed is not available in the VI model. Preserve reachability
      // with minimum OSPF cost until that speed can be inferred.
      return 1L;
    }

    long calculated =
        (long)
            (_process.getReferenceBandwidth()
                / bandwidth);

    return Math.max(
        1L, Math.min(65535L, calculated));
  }

  private static @Nonnull Prefix6 getAdvertisedNetwork(
      Interface iface,
      ConcreteInterfaceAddress6 address) {
    Ospfv3InterfaceSettings settings =
        iface.getOspfv3Settings();

    if (iface.isLoopback()
        && settings != null
        && settings.getNetworkType()
            != OspfNetworkType.POINT_TO_POINT) {
      return Prefix6.create(address.getIp(), 128);
    }

    return address.getPrefix();
  }

  /**
   * Routes visible to OSPFv3 neighbors.
   *
   * <p>This includes local external advertisements, which are control-plane
   * advertisements but are not installed as local routing candidates.
   */
  @Nonnull
  Set<AbstractRoute6> getRoutes() {
    return ImmutableSet
        .<AbstractRoute6>builder()
        .addAll(_ospfv3Rib.getRoutes())
        .addAll(_localExternalAdvertisements)
        .addAll(
            _translatedNssaExternalAdvertisements)
        .build();
  }

  /**
   * Routes that should actually be installed into this router's main RIB.
   *
   * <p>An inbound OSPFv3 distribute-list filters routing-table installation
   * only. The unfiltered OSPFv3 route set remains available through
   * {@link #getRoutes()} so advertisements continue to propagate.
   */
  @Nonnull
  Set<AbstractRoute6> getRoutingRoutes() {
    return selectRoutingRoutes(
        _ospfv3Rib.getRoutes(),
        _process.getInboundDistributeList(),
        _process.getMaximumPaths());
  }

  /**
   * Apply routing-table-only OSPFv3 policy.
   *
   * <p>The inbound distribute-list filters installation without changing
   * OSPF control-plane state. maximum-paths then limits the number of
   * equally preferred OSPFv3 routes installed for each destination prefix.
   */
  @VisibleForTesting
  static @Nonnull Set<AbstractRoute6>
      selectRoutingRoutes(
          Set<AbstractRoute6> routes,
          @Nullable PrefixList6 distributeList,
          int maximumPaths) {

    if (maximumPaths < 1) {
      throw new IllegalArgumentException(
          "maximumPaths must be positive");
    }

    Map<Prefix6, List<AbstractRoute6>>
        routesByPrefix =
            new TreeMap<>();

    for (AbstractRoute6 route : routes) {
      if (distributeList != null
          && !distributeList.permits(
              route.getNetwork())) {
        continue;
      }

      routesByPrefix
          .computeIfAbsent(
              route.getNetwork(),
              ignored ->
                  new ArrayList<>())
          .add(route);
    }

    Comparator<AbstractRoute6> routeOrder =
        Comparator
            .comparing(
                AbstractRoute6::getNextHopInterface)
            .thenComparing(
                AbstractRoute6::getNextHopIp,
                Comparator.nullsFirst(
                    Comparator.naturalOrder()))
            .thenComparing(
                route ->
                    route.getClass().getName())
            .thenComparingLong(
                AbstractRoute6::getAdministrativeCost)
            .thenComparingLong(
                AbstractRoute6::getMetric)
            .thenComparingLong(
                AbstractRoute6::getTag)
            .thenComparingInt(
                AbstractRoute6::hashCode);

    ImmutableSet.Builder<AbstractRoute6> selected =
        ImmutableSet.builder();

    for (List<AbstractRoute6> candidates :
        routesByPrefix.values()) {

      candidates.sort(routeOrder);

      int count =
          Math.min(
              maximumPaths,
              candidates.size());

      for (int i = 0; i < count; i++) {
        selected.add(
            candidates.get(i));
      }
    }

    return selected.build();
  }

  int iterationHashCode() {
    return Objects.hash(
        _ospfv3Rib.getRoutes(),
        _localExternalAdvertisements,
        _translatedNssaExternalAdvertisements);
  }

  private final @Nonnull Configuration _c;
  private Set<AbstractRoute6>
      _localExternalAdvertisements;
  private Set<Ospfv3ExternalType2Route6>
      _translatedNssaExternalAdvertisements;
  private final @Nonnull Ospfv3Process _process;
  private final @Nonnull Ospfv3Rib6 _ospfv3Rib;
  private final @Nonnull String _vrfName;
}
