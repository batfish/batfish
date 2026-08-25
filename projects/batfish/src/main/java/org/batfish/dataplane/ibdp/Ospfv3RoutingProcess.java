package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
import org.batfish.datamodel.Ospfv3NssaExternalType1Route6;
import org.batfish.datamodel.Ospfv3NssaExternalType2Route6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.PrefixList6;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RouteMap6;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3AreaRange;
import org.batfish.datamodel.ospf.Ospfv3ExternalSummary;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.batfish.datamodel.ospf.Ospfv3VirtualLink;
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
   * Maximum 16-bit Router-LSA link metric used by RFC stub-router
   * behavior.
   */
  private static final long
      MAX_ROUTER_LSA_LINK_METRIC =
          0xFFFFL;

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

  /** One directed physical OSPF adjacency used by a virtual-link transit path. */
  private static final class PhysicalAdjacency {

    private PhysicalAdjacency(
        Interface localIface,
        Interface remoteIface,
        long cost) {

      _localIface = localIface;
      _remoteIface = remoteIface;
      _cost = cost;
    }

    private final long _cost;
    private final @Nonnull Interface _localIface;
    private final @Nonnull Interface _remoteIface;
  }

  /** Resolved virtual-link transit path from this router toward its peer. */
  private static final class VirtualLinkPath {

    private VirtualLinkPath(
        long cost,
        String nextHopInterface,
        @Nullable Ip6 nextHopIp) {

      _cost = cost;
      _nextHopInterface = nextHopInterface;
      _nextHopIp = nextHopIp;
    }

    private final long _cost;
    private final @Nonnull String _nextHopInterface;
    private final @Nullable Ip6 _nextHopIp;
  }

  /**
   * One locally originated external advertisement after redistribution
   * policy but before ASBR summary-address processing.
   */
  private static final class ExternalAdvertisementSpec {

    private ExternalAdvertisementSpec(
        Prefix6 network,
        long metric,
        long tag,
        OspfMetricType metricType) {

      _network = network;
      _metric = metric;
      _tag = tag;
      _metricType = metricType;
    }

    @Override
    public boolean equals(
        @Nullable Object o) {

      if (this == o) {
        return true;
      }

      if (!(o instanceof ExternalAdvertisementSpec)) {
        return false;
      }

      ExternalAdvertisementSpec rhs =
          (ExternalAdvertisementSpec) o;

      return _metric == rhs._metric
          && _tag == rhs._tag
          && _metricType == rhs._metricType
          && _network.equals(rhs._network);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          _network,
          _metric,
          _tag,
          _metricType);
    }

    private final long _metric;
    private final @Nonnull OspfMetricType _metricType;
    private final @Nonnull Prefix6 _network;
    private final long _tag;
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
    _crossProcessExternalPrefixes =
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
    _virtualBackboneOperational =
        false;

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
   * <p>Redistribution and route-map policy are evaluated first. ASBR
   * summary-address processing is then applied to the resulting external
   * advertisements before Type-5 or Type-7 LSAs are exposed to neighbors.
   *
   * @return true iff the local advertisement set changed
   */
  boolean refreshLocalExternalAdvertisements(
      ConnectedRib6 connectedRib,
      Set<StaticRoute6> staticRoutes,
      Map<String, Ospfv3RoutingProcess> ospfv3Processes,
      boolean nonOspfv3DefaultRoutePresent) {

    if (!_process.getEnabled()) {

      boolean changed =
          !_localExternalAdvertisements.isEmpty()
              || !_crossProcessExternalPrefixes.isEmpty();

      _localExternalAdvertisements =
          ImmutableSet.of();

      _crossProcessExternalPrefixes =
          ImmutableSet.of();

      return changed;
    }

    Set<ExternalAdvertisementSpec> sources =
        new HashSet<>();

    Set<ExternalAdvertisementSpec>
        crossProcessSources =
            new HashSet<>();

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
                Route.UNSET_ROUTE_TAG,
                RoutingProtocol.CONNECTED);

        if (transformed.isEmpty()) {
          continue;
        }

        RouteMap6.Result result =
            transformed.get();

        sources.add(
            new ExternalAdvertisementSpec(
                connected.getNetwork(),
                result.getMetric(),
                result.getTag(),
                result.getOspfMetricType()));
      }
    }

    if (_process.getRedistributeLocalLoopback()) {

      for (Interface iface :
          _c.getAllInterfaces().values()) {

        if (!iface.getActive()
            || !_vrfName.equals(
                iface.getVrfName())
            || iface.getInterfaceType()
                != InterfaceType.LOOPBACK) {

          continue;
        }

        for (ConcreteInterfaceAddress6 address :
            iface.getAllConcreteAddresses6()) {

          Prefix6 localPrefix =
              address.getIp().toPrefix6();

          if (!permitsOutboundRedistribution(
              localPrefix)) {
            continue;
          }

          Optional<RouteMap6.Result> transformed =
              applyRedistributionRouteMap(
                  _process
                      .getRedistributeLocalLoopbackRouteMap(),
                  localPrefix,
                  _process.getRedistributionMetric(),
                  Route.UNSET_ROUTE_TAG,
                  RoutingProtocol.CONNECTED);

          if (transformed.isEmpty()) {
            continue;
          }

          RouteMap6.Result result =
              transformed.get();

          sources.add(
              new ExternalAdvertisementSpec(
                  localPrefix,
                  result.getMetric(),
                  result.getTag(),
                  result.getOspfMetricType()));
        }
      }
    }

    if (_process.getRedistributeStatic()) {

      for (StaticRoute6 route :
          staticRoutes) {

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
                route.getTag(),
                RoutingProtocol.STATIC);

        if (transformed.isEmpty()) {
          continue;
        }

        RouteMap6.Result result =
            transformed.get();

        sources.add(
            new ExternalAdvertisementSpec(
                route.getNetwork(),
                result.getMetric(),
                result.getTag(),
                result.getOspfMetricType()));
      }
    }

    for (String sourceProcessId :
        _process
            .getRedistributeOspfProcesses()) {

      /*
       * Self-redistribution has no useful forwarding meaning and can create
       * a local feedback loop in a snapshot computation.
       */
      if (_process
          .getProcessId()
          .equals(sourceProcessId)) {
        continue;
      }

      Ospfv3RoutingProcess sourceProcess =
          ospfv3Processes.get(
              sourceProcessId);

      if (sourceProcess == null
          || !sourceProcess
              ._process
              .getEnabled()) {
        continue;
      }

      RouteMap6 routeMap =
          _process
              .getRedistributeOspfRouteMaps()
              .get(
                  sourceProcessId);

      for (AbstractRoute6 route :
          sourceProcess
              .getRoutesForProcessRedistribution()) {

        if (!permitsOutboundRedistribution(
            route.getNetwork())) {
          continue;
        }

        Optional<RouteMap6.Result> transformed =
            applyRedistributionRouteMap(
                routeMap,
                route.getNetwork(),
                _process.getRedistributionMetric(),
                route.getTag(),
                route.getProtocol());

        if (transformed.isEmpty()) {
          continue;
        }

        RouteMap6.Result result =
            transformed.get();

        ExternalAdvertisementSpec source =
            new ExternalAdvertisementSpec(
                route.getNetwork(),
                result.getMetric(),
                result.getTag(),
                result.getOspfMetricType());

        sources.add(
            source);

        crossProcessSources.add(
            source);
      }
    }

    if (_process.getDefaultInformationOriginate()
        && (_process
                .getDefaultInformationOriginateAlways()
            || nonOspfv3DefaultRoutePresent)) {

      sources.add(
          new ExternalAdvertisementSpec(
              Prefix6.ZERO,
              _process.getDefaultInformationMetric(),
              Route.UNSET_ROUTE_TAG,
              OspfMetricType.E2));
    }

    Set<AbstractRoute6> desired =
        new HashSet<>();

    addSummarizedExternalAdvertisements(
        desired,
        sources);

    Set<AbstractRoute6> immutableDesired =
        ImmutableSet.copyOf(desired);

    Set<Prefix6> immutableCrossProcessPrefixes =
        ImmutableSet.copyOf(
            computeCrossProcessExternalPrefixes(
                crossProcessSources));

    boolean changed =
        !_localExternalAdvertisements.equals(
            immutableDesired)
            || !_crossProcessExternalPrefixes.equals(
                immutableCrossProcessPrefixes);

    _localExternalAdvertisements =
        immutableDesired;

    _crossProcessExternalPrefixes =
        immutableCrossProcessPrefixes;

    return changed;
  }

  /**
   * Return the advertised prefixes whose local Type-5/Type-7 origination
   * derives from local OSPFv3 process redistribution.
   *
   * <p>This tracks provenance independently of route attributes so reciprocal
   * local process redistribution cannot recursively re-export a route that
   * was itself created by local process redistribution. When summary-address
   * combines cross-process and non-cross-process contributors, the aggregate
   * is conservatively considered cross-process for loop-prevention purposes.
   */
  private Set<Prefix6> computeCrossProcessExternalPrefixes(
      Set<ExternalAdvertisementSpec> crossProcessSources) {

    Set<Prefix6> prefixes =
        new HashSet<>();

    for (ExternalAdvertisementSpec source :
        crossProcessSources) {

      Ospfv3ExternalSummary summary =
          getMatchingExternalSummary(
              source._network);

      if (summary == null) {

        prefixes.add(
            source._network);

        continue;
      }

      if (summary.getAdvertise()) {

        prefixes.add(
            summary.getPrefix());
      }
    }

    return prefixes;
  }

  /**
   * Apply configured ASBR summary-address rules and build the corresponding
   * Type-5/Type-7 local advertisements.
   *
   * <p>The aggregate metric is the lowest metric among its contributing
   * redistributed routes. If any contributor is external type-1, the
   * aggregate is type-1; otherwise it is type-2. A configured aggregate tag
   * replaces component tags. Without a configured aggregate tag, the
   * aggregate has no route tag.
   */
  private void addSummarizedExternalAdvertisements(
      Set<AbstractRoute6> advertisements,
      Set<ExternalAdvertisementSpec> sources) {

    if (_process
        .getExternalSummaries()
        .isEmpty()) {

      for (ExternalAdvertisementSpec source :
          sources) {

        addLocallyOriginatedExternalAdvertisements(
            advertisements,
            source._network,
            source._metric,
            source._tag,
            source._metricType);
      }

      return;
    }

    Set<ExternalAdvertisementSpec> summarized =
        new HashSet<>();

    for (Ospfv3ExternalSummary summary :
        _process.getExternalSummaries()) {

      List<ExternalAdvertisementSpec> contributors =
          new ArrayList<>();

      for (ExternalAdvertisementSpec source :
          sources) {

        Ospfv3ExternalSummary bestSummary =
            getMatchingExternalSummary(
                source._network);

        if (summary.equals(bestSummary)) {
          contributors.add(source);
        }
      }

      if (contributors.isEmpty()) {
        continue;
      }

      summarized.addAll(contributors);

      /*
       * no-advertise suppresses both the aggregate and all contributing
       * specifics.
       */
      if (!summary.getAdvertise()) {
        continue;
      }

      long metric =
          contributors
              .stream()
              .mapToLong(
                  contributor ->
                      contributor._metric)
              .min()
              .orElseThrow();

      boolean hasType1 =
          contributors
              .stream()
              .anyMatch(
                  contributor ->
                      contributor._metricType
                          == OspfMetricType.E1);

      OspfMetricType metricType =
          hasType1
              ? OspfMetricType.E1
              : OspfMetricType.E2;

      long tag =
          summary.getTag() == null
              ? Route.UNSET_ROUTE_TAG
              : summary.getTag();

      addLocallyOriginatedExternalAdvertisements(
          advertisements,
          summary.getPrefix(),
          metric,
          tag,
          metricType);
    }

    /*
     * Routes not covered by a configured summary retain their specific
     * external advertisements.
     */
    for (ExternalAdvertisementSpec source :
        sources) {

      if (summarized.contains(source)) {
        continue;
      }

      addLocallyOriginatedExternalAdvertisements(
          advertisements,
          source._network,
          source._metric,
          source._tag,
          source._metricType);
    }
  }

  /**
   * Return the most-specific configured external summary containing
   * {@code network}.
   */
  private @Nullable Ospfv3ExternalSummary
      getMatchingExternalSummary(
          Prefix6 network) {

    Ospfv3ExternalSummary best =
        null;

    for (Ospfv3ExternalSummary summary :
        _process.getExternalSummaries()) {

      if (!prefixContains(
          summary.getPrefix(),
          network)) {
        continue;
      }

      if (best == null
          || summary
                  .getPrefix()
                  .getPrefixLength()
              > best
                  .getPrefix()
                  .getPrefixLength()) {

        best =
            summary;
      }
    }

    return best;
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

    _process
        .getAreas()
        .values()
        .stream()
        .filter(Ospfv3Area::getNssa)
        .forEach(
            area -> {
              if (metricType == OspfMetricType.E1) {
                advertisements.add(
                    new Ospfv3NssaExternalType1Route6(
                        network,
                        Route.UNSET_NEXT_HOP_INTERFACE,
                        _process.getExternalAdminCost(),
                        metric,
                        area.getAreaNumber(),
                        _process.getRouterId(),
                        tag));
              } else {
                advertisements.add(
                    new Ospfv3NssaExternalType2Route6(
                        network,
                        Route.UNSET_NEXT_HOP_INTERFACE,
                        _process.getExternalAdminCost(),
                        metric,
                        area.getAreaNumber(),
                        _process.getRouterId(),
                        tag));
              }
            });
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
          long initialTag,
          RoutingProtocol sourceProtocol) {

    Optional<RouteMap6.Result> result =
        routeMap == null
            ? Optional.of(
                new RouteMap6.Result(
                    initialMetric,
                    initialTag))
            : routeMap.process(
                prefix,
                initialMetric,
                initialTag,
                OspfMetricType.E2,
                sourceProtocol);

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

    boolean newVirtualBackboneOperational =
        hasOperationalVirtualLink(
            allNodes,
            l3Adjacencies);

    boolean changed =
        newVirtualBackboneOperational
            != _virtualBackboneOperational;

    _virtualBackboneOperational =
        newVirtualBackboneOperational;

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
                  remoteProcess,
                  allNodes,
                  l3Adjacencies);

          changed |=
              importIntraAreaRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteProcess,
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
                  remoteProcess,
                  remoteRoutes);

          changed |=
              importNssaExternalRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteProcess,
                  remoteRoutes);
        }
      }
    }

    changed |=
        propagateRoutesAcrossVirtualLinks(
            allNodes,
            l3Adjacencies);

    changed |=
        refreshTranslatedNssaAdvertisements(
            allNodes,
            l3Adjacencies);

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

  private boolean hasBackboneAttachment() {
    return _process.getAreas().containsKey(0L)
        || _virtualBackboneOperational;
  }

  private boolean isAreaBorderRouterFor(
      long area) {
    return area != 0L
        && hasBackboneAttachment()
        && _process.getAreas().containsKey(area);
  }

  private boolean hasOperationalVirtualLink(
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    List<Ospfv3RoutingProcess> processes =
        getAllOspfv3Processes(allNodes);

    for (Ospfv3VirtualLink link :
        _process.getVirtualLinks()) {

      Ospfv3RoutingProcess peer =
          findVirtualLinkPeer(
              link,
              processes);

      if (peer == null) {
        continue;
      }

      VirtualLinkPath path =
          findVirtualLinkTransitPath(
              this,
              peer,
              link.getTransitArea(),
              processes,
              allNodes,
              l3Adjacencies);

      if (path != null) {
        return true;
      }
    }

    return false;
  }

  private @Nullable Ospfv3RoutingProcess
      findVirtualLinkPeer(
          Ospfv3VirtualLink link,
          List<Ospfv3RoutingProcess> processes) {

    if (!isValidVirtualLinkTransitArea(
        link.getTransitArea())) {
      return null;
    }

    Ospfv3RoutingProcess match =
        null;

    for (Ospfv3RoutingProcess candidate :
        processes) {

      if (candidate == this
          || !candidate
              ._process
              .getEnabled()
          || !_vrfName.equals(
              candidate._vrfName)
          || !candidate
              ._process
              .getRouterId()
              .equals(
                  link.getPeerRouterId())
          || !candidate
              .isValidVirtualLinkTransitArea(
                  link.getTransitArea())
          || !candidate.hasConfiguredVirtualLink(
              link.getTransitArea(),
              _process.getRouterId(),
              link)) {

        continue;
      }

      /*
       * Duplicate router IDs make the peer ambiguous. Refuse to form
       * a virtual adjacency rather than choosing one arbitrarily.
       */
      if (match != null) {
        return null;
      }

      match =
          candidate;
    }

    return match;
  }

  private boolean hasConfiguredVirtualLink(
      long transitArea,
      Ip peerRouterId,
      Ospfv3VirtualLink oppositeLink) {

    return _process
        .getVirtualLinks()
        .stream()
        .anyMatch(
            link ->
                link.getTransitArea()
                        == transitArea
                    && link
                        .getPeerRouterId()
                        .equals(peerRouterId)
                    && areVirtualLinkSecuritySettingsCompatible(
                        link,
                        oppositeLink));
  }

  @VisibleForTesting
  static boolean
      areVirtualLinkAuthenticationsCompatible(
          Ospfv3VirtualLink lhs,
          Ospfv3VirtualLink rhs) {

    return Objects.equals(
        lhs.getAuthentication(),
        rhs.getAuthentication());
  }

  @VisibleForTesting
  static boolean
      areVirtualLinkSecuritySettingsCompatible(
          Ospfv3VirtualLink lhs,
          Ospfv3VirtualLink rhs) {

    return areVirtualLinkAuthenticationsCompatible(
            lhs,
            rhs)
        && Objects.equals(
            lhs.getEncryption(),
            rhs.getEncryption());
  }

  private boolean isValidVirtualLinkTransitArea(
      long transitArea) {

    if (transitArea == 0L) {
      return false;
    }

    Ospfv3Area area =
        _process
            .getAreas()
            .get(transitArea);

    return area != null
        && !area.getStub()
        && !area.getNssa();
  }

  /**
   * Resolve the lowest-cost physical intra-area path used as the virtual-link
   * transit path.
   *
   * <p>The OSPF cost is directional. The first physical adjacency on the path
   * supplies the real forwarding next hop used by routes learned across the
   * logical backbone adjacency.
   */
  private static @Nullable VirtualLinkPath
      findVirtualLinkTransitPath(
          Ospfv3RoutingProcess source,
          Ospfv3RoutingProcess target,
          long transitArea,
          List<Ospfv3RoutingProcess> processes,
          Map<String, Node> allNodes,
          L3Adjacencies l3Adjacencies) {

    if (source == target
        || !source
            .isValidVirtualLinkTransitArea(
                transitArea)
        || !target
            .isValidVirtualLinkTransitArea(
                transitArea)) {
      return null;
    }

    Map<Ospfv3RoutingProcess, Long> distances =
        new HashMap<>();

    Map<Ospfv3RoutingProcess, PhysicalAdjacency>
        firstAdjacencies =
            new HashMap<>();

    Set<Ospfv3RoutingProcess> visited =
        new HashSet<>();

    distances.put(
        source,
        0L);

    while (true) {

      Ospfv3RoutingProcess current =
          null;

      long currentDistance =
          Long.MAX_VALUE;

      for (Map.Entry<Ospfv3RoutingProcess, Long> entry :
          distances.entrySet()) {

        if (visited.contains(
                entry.getKey())
            || entry.getValue()
                >= currentDistance) {
          continue;
        }

        current =
            entry.getKey();

        currentDistance =
            entry.getValue();
      }

      if (current == null) {
        return null;
      }

      if (current == target) {

        PhysicalAdjacency first =
            firstAdjacencies.get(target);

        if (first == null) {
          return null;
        }

        return new VirtualLinkPath(
            currentDistance,
            first._localIface.getName(),
            findPeerNextHopIp(
                    first._localIface,
                    first._remoteIface)
                .orElse(null));
      }

      visited.add(current);

      for (Ospfv3RoutingProcess next :
          processes) {

        if (next == current
            || visited.contains(next)
            || !current
                ._vrfName
                .equals(next._vrfName)) {
          continue;
        }

        PhysicalAdjacency adjacency =
            findBestPhysicalAdjacencyInArea(
                current,
                next,
                transitArea,
                allNodes,
                l3Adjacencies);

        if (adjacency == null
            || adjacency._cost
                >= LS_INFINITY
            || currentDistance
                >= LS_INFINITY
                    - adjacency._cost) {
          continue;
        }

        long newDistance =
            currentDistance
                + adjacency._cost;

        Long oldDistance =
            distances.get(next);

        if (oldDistance != null
            && oldDistance <= newDistance) {
          continue;
        }

        distances.put(
            next,
            newDistance);

        firstAdjacencies.put(
            next,
            current == source
                ? adjacency
                : firstAdjacencies.get(current));
      }
    }
  }

  private static @Nullable PhysicalAdjacency
      findBestPhysicalAdjacencyInArea(
          Ospfv3RoutingProcess lhs,
          Ospfv3RoutingProcess rhs,
          long area,
          Map<String, Node> allNodes,
          L3Adjacencies l3Adjacencies) {

    if (lhs == rhs
        || !lhs._process.getEnabled()
        || !rhs._process.getEnabled()
        || !lhs.isValidVirtualLinkTransitArea(area)
        || !rhs.isValidVirtualLinkTransitArea(area)
        || !lhs.areAreaTypesCompatible(
            area,
            rhs)) {
      return null;
    }

    PhysicalAdjacency best =
        null;

    for (Interface lhsIface :
        lhs._c
            .getAllInterfaces()
            .values()) {

      if (!lhs.isAdjacencyInterface(
          lhsIface)) {
        continue;
      }

      Ospfv3InterfaceSettings lhsSettings =
          lhsIface.getOspfv3Settings();

      if (lhsSettings == null
          || lhsSettings.getAreaName()
              == null
          || lhsSettings.getAreaName()
              != area) {
        continue;
      }

      NodeInterfacePair lhsId =
          NodeInterfacePair.of(
              lhs._c.getHostname(),
              lhsIface.getName());

      BroadcastElection election =
          lhsSettings.getNetworkType()
                  == OspfNetworkType.BROADCAST
              ? lhs.electBroadcastDesignatedRouters(
                  lhsIface,
                  lhsId,
                  allNodes,
                  l3Adjacencies)
              : BroadcastElection.empty();

      for (Interface rhsIface :
          rhs._c
              .getAllInterfaces()
              .values()) {

        if (!rhs.isAdjacencyInterface(
            rhsIface)) {
          continue;
        }

        Ospfv3InterfaceSettings rhsSettings =
            rhsIface.getOspfv3Settings();

        if (rhsSettings == null
            || rhsSettings.getAreaName()
                == null
            || rhsSettings.getAreaName()
                != area
            || !areInterfaceSettingsCompatible(
                lhsSettings,
                rhsSettings)) {
          continue;
        }

        NodeInterfacePair rhsId =
            NodeInterfacePair.of(
                rhs._c.getHostname(),
                rhsIface.getName());

        if (!areTopologicallyAdjacent(
            lhsId,
            lhsIface,
            rhsId,
            rhsIface,
            l3Adjacencies)) {
          continue;
        }

        if (!shouldFormFullAdjacency(
            lhsSettings,
            rhsSettings,
            lhsId,
            rhsId,
            election)) {
          continue;
        }

        long cost =
            lhs._process
                    .getMaxMetricRouterLsa()
                ? MAX_ROUTER_LSA_LINK_METRIC
                : lhs.computeInterfaceCost(
                    lhsIface);

        if (best == null
            || cost < best._cost) {

          best =
              new PhysicalAdjacency(
                  lhsIface,
                  rhsIface,
                  cost);
        }
      }
    }

    return best;
  }

  /**
   * Return whether this process is the elected default-Candidate NSSA
   * Type-7 translator for {@code area}.
   *
   * <p>RFC 3101 elects the highest router ID among NSSA border routers that
   * are reachable both through the NSSA and through the AS transit topology.
   * In this snapshot dataplane, area 0 is the modeled transit topology.
   */
  @VisibleForTesting
  boolean isElectedNssaTranslator(
      long area,
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    if (!isOperationalNssaBorderRouterFor(area)) {
      return false;
    }

    List<Ospfv3RoutingProcess> processes =
        getAllOspfv3Processes(allNodes);

    Ospfv3RoutingProcess elected =
        this;

    for (Ospfv3RoutingProcess candidate :
        processes) {

      if (candidate == this
          || !candidate
              .isOperationalNssaBorderRouterFor(
                  area)) {
        continue;
      }

      /*
       * A candidate must be reachable from us through both the NSSA and
       * area 0. If the NSSA is partitioned, each partition can therefore
       * elect its own translator.
       */
      if (!isReachableInArea(
              this,
              candidate,
              area,
              processes,
              allNodes,
              l3Adjacencies)
          || !isReachableInArea(
              this,
              candidate,
              0L,
              processes,
              allNodes,
              l3Adjacencies)) {
        continue;
      }

      if (compareTranslatorCandidates(
              candidate,
              elected)
          > 0) {
        elected =
            candidate;
      }
    }

    return elected == this;
  }

  private boolean isOperationalNssaBorderRouterFor(
      long area) {

    return _process.getEnabled()
        && isNssaArea(area)
        && isAreaBorderRouterFor(area)
        && hasActiveAdjacencyInterfaceInArea(area)
        && hasActiveAdjacencyInterfaceInArea(0L);
  }

  private boolean hasActiveAdjacencyInterfaceInArea(
      long area) {

    for (Interface iface :
        _c.getAllInterfaces().values()) {

      if (!isAdjacencyInterface(iface)) {
        continue;
      }

      Ospfv3InterfaceSettings settings =
          iface.getOspfv3Settings();

      if (settings != null
          && settings.getAreaName() != null
          && settings.getAreaName() == area) {
        return true;
      }
    }

    return false;
  }

  private static List<Ospfv3RoutingProcess>
      getAllOspfv3Processes(
          Map<String, Node> allNodes) {

    List<Ospfv3RoutingProcess> processes =
        new ArrayList<>();

    for (Node node :
        allNodes.values()) {

      for (VirtualRouter vr :
          node.getVirtualRouters()) {

        processes.addAll(
            vr.getOspfv3Processes()
                .values());
      }
    }

    return processes;
  }

  private static boolean isReachableInArea(
      Ospfv3RoutingProcess source,
      Ospfv3RoutingProcess target,
      long area,
      List<Ospfv3RoutingProcess> processes,
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    if (source == target) {
      return true;
    }

    Set<Ospfv3RoutingProcess> visited =
        new HashSet<>();

    ArrayDeque<Ospfv3RoutingProcess> queue =
        new ArrayDeque<>();

    visited.add(source);
    queue.add(source);

    while (!queue.isEmpty()) {

      Ospfv3RoutingProcess current =
          queue.removeFirst();

      for (Ospfv3RoutingProcess next :
          processes) {

        if (visited.contains(next)
            || !haveFullAdjacencyInArea(
                current,
                next,
                area,
                allNodes,
                l3Adjacencies)) {
          continue;
        }

        if (next == target) {
          return true;
        }

        visited.add(next);
        queue.addLast(next);
      }
    }

    return false;
  }

  private static boolean haveFullAdjacencyInArea(
      Ospfv3RoutingProcess lhs,
      Ospfv3RoutingProcess rhs,
      long area,
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    if (lhs == rhs
        || !lhs._process.getEnabled()
        || !rhs._process.getEnabled()
        || !lhs.areAreaTypesCompatible(
            area,
            rhs)) {
      return false;
    }

    for (Interface lhsIface :
        lhs._c
            .getAllInterfaces()
            .values()) {

      if (!lhs.isAdjacencyInterface(lhsIface)) {
        continue;
      }

      Ospfv3InterfaceSettings lhsSettings =
          lhsIface.getOspfv3Settings();

      if (lhsSettings == null
          || lhsSettings.getAreaName() == null
          || lhsSettings.getAreaName() != area) {
        continue;
      }

      NodeInterfacePair lhsId =
          NodeInterfacePair.of(
              lhs._c.getHostname(),
              lhsIface.getName());

      BroadcastElection election =
          lhsSettings.getNetworkType()
                  == OspfNetworkType.BROADCAST
              ? lhs.electBroadcastDesignatedRouters(
                  lhsIface,
                  lhsId,
                  allNodes,
                  l3Adjacencies)
              : BroadcastElection.empty();

      for (Interface rhsIface :
          rhs._c
              .getAllInterfaces()
              .values()) {

        if (!rhs.isAdjacencyInterface(rhsIface)) {
          continue;
        }

        Ospfv3InterfaceSettings rhsSettings =
            rhsIface.getOspfv3Settings();

        if (rhsSettings == null
            || rhsSettings.getAreaName() == null
            || rhsSettings.getAreaName() != area
            || !areInterfaceSettingsCompatible(
                lhsSettings,
                rhsSettings)) {
          continue;
        }

        NodeInterfacePair rhsId =
            NodeInterfacePair.of(
                rhs._c.getHostname(),
                rhsIface.getName());

        if (!areTopologicallyAdjacent(
            lhsId,
            lhsIface,
            rhsId,
            rhsIface,
            l3Adjacencies)) {
          continue;
        }

        if (!shouldFormFullAdjacency(
            lhsSettings,
            rhsSettings,
            lhsId,
            rhsId,
            election)) {
          continue;
        }

        return true;
      }
    }

    return false;
  }

  /**
   * Compare default-Candidate translators.
   *
   * <p>Router ID is the RFC election key. Remaining fields only provide
   * deterministic behavior for invalid snapshots containing duplicate
   * router IDs.
   */
  private static int compareTranslatorCandidates(
      Ospfv3RoutingProcess lhs,
      Ospfv3RoutingProcess rhs) {

    int routerIdComparison =
        lhs._process
            .getRouterId()
            .compareTo(
                rhs._process
                    .getRouterId());

    if (routerIdComparison != 0) {
      return routerIdComparison;
    }

    int hostnameComparison =
        lhs._c
            .getHostname()
            .compareTo(
                rhs._c.getHostname());

    if (hostnameComparison != 0) {
      return hostnameComparison;
    }

    int vrfComparison =
        lhs._vrfName.compareTo(
            rhs._vrfName);

    if (vrfComparison != 0) {
      return vrfComparison;
    }

    return lhs._process
        .getProcessId()
        .compareTo(
            rhs._process
                .getProcessId());
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
   * Return whether this ABR currently has an active backbone for purposes of
   * originating the default summary into a restricted area.
   *
   * <p>AOS-CX always considers an operational area-0 neighbor or a configured
   * passive area-0 interface sufficient. The active-backbone
   * stub-default-route knob additionally allows an active area-0 loopback to
   * qualify the backbone when neither of those conditions exists.
   */
  private boolean hasActiveBackboneForRestrictedDefault(
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    if (!_process.getEnabled()) {
      return false;
    }

    /*
     * An operational virtual link is an area-0 adjacency.
     */
    if (_virtualBackboneOperational) {
      return true;
    }

    if (hasPassiveBackboneInterface()) {
      return true;
    }

    List<Ospfv3RoutingProcess> processes =
        getAllOspfv3Processes(
            allNodes);

    for (Ospfv3RoutingProcess candidate :
        processes) {

      if (candidate == this) {
        continue;
      }

      if (haveFullAdjacencyInArea(
          this,
          candidate,
          0L,
          allNodes,
          l3Adjacencies)) {

        return true;
      }
    }

    return _process
            .getActiveBackboneStubDefaultRoute()
        && hasActiveBackboneLoopback();
  }

  /**
   * Return whether this process has an active passive interface configured in
   * area 0.
   */
  private boolean hasPassiveBackboneInterface() {

    for (Interface iface :
        _c.getAllInterfaces().values()) {

      if (!iface.getActive()
          || !_vrfName.equals(
              iface.getVrfName())
          || !isEnabledForThisProcess(
              iface)) {

        continue;
      }

      Ospfv3InterfaceSettings settings =
          iface.getOspfv3Settings();

      if (settings != null
          && settings.getAreaName() != null
          && settings.getAreaName() == 0L
          && settings.getPassive()) {

        return true;
      }
    }

    return false;
  }

  /**
   * Return whether this process has an active OSPFv3 loopback in area 0.
   */
  private boolean hasActiveBackboneLoopback() {

    for (Interface iface :
        _c.getAllInterfaces().values()) {

      if (!iface.getActive()
          || iface.getInterfaceType()
              != InterfaceType.LOOPBACK
          || !_vrfName.equals(
              iface.getVrfName())
          || !isEnabledForThisProcess(
              iface)) {

        continue;
      }

      Ospfv3InterfaceSettings settings =
          iface.getOspfv3Settings();

      if (settings != null
          && settings.getAreaName() != null
          && settings.getAreaName() == 0L) {

        return true;
      }
    }

    return false;
  }

  /**
   * Install the default summary originated by an ABR toward a stub area.
   */
  private boolean importRestrictedAreaDefaultFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Ospfv3RoutingProcess remoteProcess,
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    if (!isRestrictedArea(area)
        || !remoteProcess
            .isAreaBorderRouterFor(area)
        || !remoteProcess
            .hasActiveBackboneForRestrictedDefault(
                allNodes,
                l3Adjacencies)) {

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
            != remote.getDeadInterval()
        || !Objects.equals(
            local.getAuthentication(),
            remote.getAuthentication())
        || !Objects.equals(
            local.getEncryption(),
            remote.getEncryption())) {

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

  /**
   * Return whether an intra-area route was originated directly by this
   * process rather than learned through another OSPFv3 neighbor.
   */
  private boolean isLocallyOriginatedIntraAreaRoute(
      Ospfv3IntraAreaRoute6 route) {

    Interface iface =
        _c.getAllInterfaces().get(
            route.getNextHopInterface());

    if (iface == null
        || !isEnabledForThisProcess(
            iface)) {
      return false;
    }

    return iface
        .getAllConcreteAddresses6()
        .stream()
        .anyMatch(
            address ->
                getAdvertisedNetwork(
                        iface,
                        address)
                    .equals(
                        route.getNetwork())
                    && Objects.equals(
                        address.getIp(),
                        route.getNextHopIp()));
  }

  /**
   * Return whether this route is locally originated for stub-router
   * advertisement purposes.
   *
   * <p>Locally originated intra-area prefixes and local Type-5/Type-7
   * externals remain reachable at their normal cost. Routes whose next hop
   * is another OSPF router are transit routes and receive the maximized
   * Router-LSA link metric.
   */
  private boolean isLocallyOriginatedForMaxMetric(
      AbstractRoute6 route) {

    if (route
        instanceof Ospfv3IntraAreaRoute6) {

      return isLocallyOriginatedIntraAreaRoute(
          (Ospfv3IntraAreaRoute6) route);
    }

    if (route
        instanceof Ospfv3ExternalType1Route6) {

      Ospfv3ExternalType1Route6 external =
          (Ospfv3ExternalType1Route6) route;

      return external
              .getAdvertiser()
              .equals(
                  _process.getRouterId())
          && external.getCostToAdvertiser()
              == 0L;
    }

    if (route
        instanceof Ospfv3ExternalType2Route6) {

      Ospfv3ExternalType2Route6 external =
          (Ospfv3ExternalType2Route6) route;

      return external
              .getAdvertiser()
              .equals(
                  _process.getRouterId())
          && external.getCostToAdvertiser()
              == 0L;
    }

    if (route
        instanceof Ospfv3NssaExternalType1Route6) {

      Ospfv3NssaExternalType1Route6 external =
          (Ospfv3NssaExternalType1Route6) route;

      return external
              .getAdvertiser()
              .equals(
                  _process.getRouterId())
          && external.getCostToAdvertiser()
              == 0L;
    }

    if (route
        instanceof Ospfv3NssaExternalType2Route6) {

      Ospfv3NssaExternalType2Route6 external =
          (Ospfv3NssaExternalType2Route6) route;

      return external
              .getAdvertiser()
              .equals(
                  _process.getRouterId())
          && external.getCostToAdvertiser()
              == 0L;
    }

    return false;
  }

  /**
   * Return the metric that a neighbor should observe when the advertising
   * router has permanent max-metric router-lsa enabled.
   *
   * <p>The local RIB retains normal SPF metrics. Only advertisement through
   * the stub router is transformed. When the route's outbound OSPF interface
   * is known, its ordinary link cost is replaced by the maximum 16-bit
   * Router-LSA cost instead of simply being added a second time.
   */
  private static long advertisedMetricForNeighbor(
      Ospfv3RoutingProcess remoteProcess,
      AbstractRoute6 route,
      long metric) {

    if (!remoteProcess
            ._process
            .getMaxMetricRouterLsa()
        || remoteProcess
            .isLocallyOriginatedForMaxMetric(
                route)) {

      return metric;
    }

    long baseMetric =
        metric;

    Interface outbound =
        remoteProcess
            ._c
            .getAllInterfaces()
            .get(
                route.getNextHopInterface());

    if (outbound != null
        && remoteProcess
            .isEnabledForThisProcess(
                outbound)) {

      long ordinaryCost =
          remoteProcess
              .computeInterfaceCost(
                  outbound);

      if (baseMetric >= ordinaryCost) {
        baseMetric -=
            ordinaryCost;
      }
    }

    if (baseMetric
        >= LS_INFINITY
            - MAX_ROUTER_LSA_LINK_METRIC) {

      return LS_INFINITY;
    }

    return baseMetric
        + MAX_ROUTER_LSA_LINK_METRIC;
  }

  private boolean importIntraAreaRoutesFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Ospfv3RoutingProcess remoteProcess,
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

      long advertisedMetric =
          advertisedMetricForNeighbor(
              remoteProcess,
              intra,
              intra.getMetric());

      if (advertisedMetric >= LS_INFINITY
          || incrementalCost
              >= LS_INFINITY
                  - advertisedMetric) {
        continue;
      }

      long newMetric =
          advertisedMetric
              + incrementalCost;

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
   * Exchange the backbone LSDB with reciprocal virtual-link peers whose
   * transit path is currently reachable.
   */
  private boolean propagateRoutesAcrossVirtualLinks(
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    if (_process.getVirtualLinks().isEmpty()) {
      return false;
    }

    boolean changed =
        false;

    List<Ospfv3RoutingProcess> processes =
        getAllOspfv3Processes(
            allNodes);

    for (Ospfv3VirtualLink link :
        _process.getVirtualLinks()) {

      Ospfv3RoutingProcess peer =
          findVirtualLinkPeer(
              link,
              processes);

      if (peer == null) {
        continue;
      }

      VirtualLinkPath path =
          findVirtualLinkTransitPath(
              this,
              peer,
              link.getTransitArea(),
              processes,
              allNodes,
              l3Adjacencies);

      if (path == null) {
        continue;
      }

      Set<AbstractRoute6> remoteRoutes =
          peer.getRoutes();

      changed |=
          importBackboneIntraAreaRoutesFromVirtualNeighbor(
              path,
              peer,
              remoteRoutes);

      changed |=
          importInterAreaRoutesFromVirtualNeighbor(
              path,
              peer,
              remoteRoutes);

      changed |=
          importExternalRoutesFromVirtualNeighbor(
              path,
              peer,
              remoteRoutes);
    }

    return changed;
  }

  /**
   * A virtual link is a logical point-to-point interface in area 0. Therefore
   * area-0 intra-area routes remain intra-area when exchanged between the two
   * endpoints.
   */
  private boolean importBackboneIntraAreaRoutesFromVirtualNeighbor(
      VirtualLinkPath path,
      Ospfv3RoutingProcess remoteProcess,
      Set<AbstractRoute6> remoteRoutes) {

    boolean changed =
        false;

    for (AbstractRoute6 route :
        remoteRoutes) {

      if (!(route
          instanceof Ospfv3IntraAreaRoute6)) {
        continue;
      }

      Ospfv3IntraAreaRoute6 intra =
          (Ospfv3IntraAreaRoute6) route;

      if (intra.getArea() != 0L) {
        continue;
      }

      long remoteMetric =
          advertisedMetricForNeighbor(
              remoteProcess,
              intra,
              intra.getMetric());

      if (remoteMetric
              >= LS_INFINITY
          || path._cost
              >= LS_INFINITY
                  - remoteMetric) {
        continue;
      }

      changed |=
          _ospfv3Rib.mergeRoute(
              new Ospfv3IntraAreaRoute6(
                  intra.getNetwork(),
                  path._nextHopInterface,
                  path._nextHopIp,
                  _process
                      .getIntraAreaAdminCost(),
                  remoteMetric
                      + path._cost,
                  0L,
                  intra.getTag()));
    }

    return changed;
  }

  /**
   * Import summaries across the logical area-0 adjacency.
   *
   * <p>Routes native to a non-backbone area on the peer become inter-area
   * routes in area 0. Existing area-0 summaries continue flooding through the
   * virtual backbone.
   */
  private boolean importInterAreaRoutesFromVirtualNeighbor(
      VirtualLinkPath path,
      Ospfv3RoutingProcess remoteProcess,
      Set<AbstractRoute6> remoteRoutes) {

    boolean changed =
        false;

    for (AbstractRoute6 route :
        remoteRoutes) {

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

        if (sourceArea == 0L) {
          continue;
        }

        if (!remoteProcess
            .canAdvertiseBetweenAreas(
                sourceArea,
                0L)) {
          continue;
        }

        remoteMetric =
            intra.getMetric();

        tag =
            intra.getTag();

        @Nullable Ospfv3AreaRange range =
            remoteProcess
                .getMatchingAreaRange(
                    sourceArea,
                    intra.getNetwork(),
                    Ospfv3AreaRange.Type
                        .INTER_AREA);

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

        if (sourceArea != 0L
            && !remoteProcess
                .canAdvertiseBetweenAreas(
                    sourceArea,
                    0L)) {
          continue;
        }

        remoteMetric =
            inter.getMetric();

        tag =
            inter.getTag();

      } else {
        continue;
      }

      remoteMetric =
          advertisedMetricForNeighbor(
              remoteProcess,
              route,
              remoteMetric);

      if (remoteMetric >= LS_INFINITY
          || path._cost
              >= LS_INFINITY
                  - remoteMetric) {
        continue;
      }

      changed |=
          _ospfv3Rib.mergeRoute(
              new Ospfv3InterAreaRoute6(
                  advertisedNetwork,
                  path._nextHopInterface,
                  path._nextHopIp,
                  _process
                      .getInterAreaAdminCost(),
                  remoteMetric
                      + path._cost,
                  0L,
                  tag));
    }

    return changed;
  }

  /**
   * Type-5 LSAs also flood across an operational virtual backbone link.
   */
  private boolean importExternalRoutesFromVirtualNeighbor(
      VirtualLinkPath path,
      Ospfv3RoutingProcess remoteProcess,
      Set<AbstractRoute6> remoteRoutes) {

    boolean changed =
        false;

    for (AbstractRoute6 route :
        remoteRoutes) {

      if (route
          instanceof Ospfv3ExternalType1Route6) {

        Ospfv3ExternalType1Route6 external =
            (Ospfv3ExternalType1Route6) route;

        if (external
                .getAdvertiser()
                .equals(
                    _process.getRouterId())
            && external.getCostToAdvertiser()
                != 0L) {
          continue;
        }

        long advertisedMetric =
            advertisedMetricForNeighbor(
                remoteProcess,
                external,
                external.getMetric());

        long advertisedCostToAdvertiser =
            advertisedMetricForNeighbor(
                remoteProcess,
                external,
                external.getCostToAdvertiser());

        if (advertisedMetric
                >= LS_INFINITY
            || advertisedCostToAdvertiser
                >= LS_INFINITY
            || path._cost
                >= LS_INFINITY
                    - advertisedMetric
            || path._cost
                >= LS_INFINITY
                    - advertisedCostToAdvertiser) {
          continue;
        }

        changed |=
            _ospfv3Rib.mergeRoute(
                new Ospfv3ExternalType1Route6(
                    external.getNetwork(),
                    path._nextHopInterface,
                    path._nextHopIp,
                    _process
                        .getExternalAdminCost(),
                    advertisedMetric
                        + path._cost,
                    external.getLsaMetric(),
                    0L,
                    advertisedCostToAdvertiser
                        + path._cost,
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

      if (external
              .getAdvertiser()
              .equals(
                  _process.getRouterId())
          && external.getCostToAdvertiser()
              != 0L) {
        continue;
      }

      long advertisedCostToAdvertiser =
          advertisedMetricForNeighbor(
              remoteProcess,
              external,
              external.getCostToAdvertiser());

      if (advertisedCostToAdvertiser
              >= LS_INFINITY
          || path._cost
              >= LS_INFINITY
                  - advertisedCostToAdvertiser) {
        continue;
      }

      changed |=
          _ospfv3Rib.mergeRoute(
              new Ospfv3ExternalType2Route6(
                  external.getNetwork(),
                  path._nextHopInterface,
                  path._nextHopIp,
                  _process
                      .getExternalAdminCost(),
                  external.getMetric(),
                  0L,
                  advertisedCostToAdvertiser
                      + path._cost,
                  external.getAdvertiser(),
                  external.getTag()));
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

      remoteMetric =
          advertisedMetricForNeighbor(
              remoteProcess,
              route,
              remoteMetric);

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

    boolean backboneAttached =
        hasBackboneAttachment();

    boolean sourceAttached =
        sourceArea == 0L
            ? backboneAttached
            : _process
                .getAreas()
                .containsKey(sourceArea);

    boolean targetAttached =
        targetArea == 0L
            ? backboneAttached
            : _process
                .getAreas()
                .containsKey(targetArea);

    return backboneAttached
        && sourceAttached
        && targetAttached
        && (sourceArea == 0L
            || targetArea == 0L);
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
      Ospfv3RoutingProcess remoteProcess,
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

        long advertisedMetric =
            advertisedMetricForNeighbor(
                remoteProcess,
                external,
                external.getMetric());

        long advertisedCostToAdvertiser =
            advertisedMetricForNeighbor(
                remoteProcess,
                external,
                external.getCostToAdvertiser());

        if (advertisedMetric
                >= LS_INFINITY
            || advertisedCostToAdvertiser
                >= LS_INFINITY
            || incrementalCost
                >= LS_INFINITY
                    - advertisedMetric
            || incrementalCost
                >= LS_INFINITY
                    - advertisedCostToAdvertiser) {
          continue;
        }

        long newMetric =
            advertisedMetric
                + incrementalCost;

        long newCostToAdvertiser =
            advertisedCostToAdvertiser
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

      long advertisedCostToAdvertiser =
          advertisedMetricForNeighbor(
              remoteProcess,
              external,
              external.getCostToAdvertiser());

      if (advertisedCostToAdvertiser
              >= LS_INFINITY
          || incrementalCost
              >= LS_INFINITY
                  - advertisedCostToAdvertiser) {
        continue;
      }

      long newCostToAdvertiser =
          advertisedCostToAdvertiser
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
   * <p>N1 routes accumulate the receiving-interface cost into both total
   * route metric and cost-to-advertiser. N2 routes retain their external
   * metric and accumulate cost-to-advertiser only.
   */
  private boolean importNssaExternalRoutesFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Ospfv3RoutingProcess remoteProcess,
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

    for (AbstractRoute6 route :
        remoteRoutes) {

      if (route
          instanceof Ospfv3NssaExternalType1Route6) {

        Ospfv3NssaExternalType1Route6 external =
            (Ospfv3NssaExternalType1Route6) route;

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

        long advertisedMetric =
            advertisedMetricForNeighbor(
                remoteProcess,
                external,
                external.getMetric());

        long advertisedCostToAdvertiser =
            advertisedMetricForNeighbor(
                remoteProcess,
                external,
                external.getCostToAdvertiser());

        if (advertisedMetric
                >= LS_INFINITY
            || advertisedCostToAdvertiser
                >= LS_INFINITY
            || incrementalCost
                >= LS_INFINITY
                    - advertisedMetric
            || incrementalCost
                >= LS_INFINITY
                    - advertisedCostToAdvertiser) {
          continue;
        }

        long newMetric =
            advertisedMetric
                + incrementalCost;

        long newCostToAdvertiser =
            advertisedCostToAdvertiser
                + incrementalCost;

        changed |=
            _ospfv3Rib.mergeRoute(
                new Ospfv3NssaExternalType1Route6(
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

      long advertisedCostToAdvertiser =
          advertisedMetricForNeighbor(
              remoteProcess,
              external,
              external.getCostToAdvertiser());

      if (advertisedCostToAdvertiser
              >= LS_INFINITY
          || incrementalCost
              >= LS_INFINITY
                  - advertisedCostToAdvertiser) {
        continue;
      }

      long newCostToAdvertiser =
          advertisedCostToAdvertiser
              + incrementalCost;

      changed |=
          _ospfv3Rib.mergeRoute(
              new Ospfv3NssaExternalType2Route6(
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
   * Rebuild Type-7 to Type-5 translations from the ABR's complete
   * currently visible NSSA state.
   *
   * <p>RFC 3101 aggregation rules are modeled for both N1 and N2. If any
   * Type-2 contributor best-matches a range, the translated range is E2 with
   * metric highest-Type-2 + 1. Otherwise it is E1 with the highest Type-1
   * path cost.
   */
  private boolean refreshTranslatedNssaAdvertisements(
      Map<String, Node> allNodes,
      L3Adjacencies l3Adjacencies) {

    Set<AbstractRoute6> type7Routes =
        new HashSet<>();

    for (AbstractRoute6 route :
        _ospfv3Rib.getRoutes()) {

      if (isNssaExternalRoute(route)) {
        type7Routes.add(route);
      }
    }

    /*
     * An ABR may also be an ASBR.
     */
    for (AbstractRoute6 route :
        _localExternalAdvertisements) {

      if (isNssaExternalRoute(route)) {
        type7Routes.add(route);
      }
    }

    Set<AbstractRoute6> desired =
        new HashSet<>();

    Set<String> emittedRanges =
        new HashSet<>();

    for (AbstractRoute6 external :
        type7Routes) {

      long area =
          getNssaExternalArea(external);

      if (!isElectedNssaTranslator(
          area,
          allNodes,
          l3Adjacencies)) {
        continue;
      }

      @Nullable Ospfv3AreaRange range =
          getMatchingAreaRange(
              area,
              external.getNetwork(),
              Ospfv3AreaRange.Type.NSSA);

      if (range == null) {
        desired.add(
            toTranslatedType5(external));
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

      @Nullable AbstractRoute6 aggregate =
          computeNssaRangeTranslation(
              area,
              range,
              type7Routes);

      if (aggregate != null) {
        desired.add(aggregate);
      }
    }

    Set<AbstractRoute6> immutableDesired =
        ImmutableSet.copyOf(desired);

    if (_translatedNssaExternalAdvertisements
        .equals(immutableDesired)) {
      return false;
    }

    _translatedNssaExternalAdvertisements =
        immutableDesired;

    return true;
  }

  private static boolean isNssaExternalRoute(
      AbstractRoute6 route) {

    return route
            instanceof Ospfv3NssaExternalType1Route6
        || route
            instanceof Ospfv3NssaExternalType2Route6;
  }

  private static long getNssaExternalArea(
      AbstractRoute6 route) {

    if (route
        instanceof Ospfv3NssaExternalType1Route6) {
      return ((Ospfv3NssaExternalType1Route6) route)
          .getArea();
    }

    if (route
        instanceof Ospfv3NssaExternalType2Route6) {
      return ((Ospfv3NssaExternalType2Route6) route)
          .getArea();
    }

    throw new IllegalArgumentException(
        "Not an OSPFv3 NSSA external route: "
            + route.getClass().getName());
  }

  private @Nullable AbstractRoute6
      computeNssaRangeTranslation(
          long area,
          Ospfv3AreaRange range,
          Set<AbstractRoute6> type7Routes) {

    int contributors =
        0;

    @Nullable AbstractRoute6 soleContributor =
        null;

    boolean hasType2 =
        false;

    long maximumType1Cost =
        -1L;

    long maximumType2Metric =
        -1L;

    for (AbstractRoute6 external :
        type7Routes) {

      if (!isNssaExternalRoute(external)
          || getNssaExternalArea(external)
              != area) {
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

      if (external
          instanceof Ospfv3NssaExternalType2Route6) {

        hasType2 =
            true;

        maximumType2Metric =
            Math.max(
                maximumType2Metric,
                external.getMetric());

      } else {

        maximumType1Cost =
            Math.max(
                maximumType1Cost,
                external.getMetric());
      }
    }

    if (contributors == 0) {
      return null;
    }

    /*
     * A range that exactly matches a lone Type-7 route behaves as the
     * specific translation rather than an aggregate.
     */
    if (contributors == 1
        && soleContributor != null
        && soleContributor
            .getNetwork()
            .equals(range.getPrefix())) {

      return toTranslatedType5(
          soleContributor);
    }

    if (hasType2) {

      if (maximumType2Metric < 0L
          || maximumType2Metric
              >= Ospfv3Process.MAX_METRIC) {
        return null;
      }

      return new Ospfv3ExternalType2Route6(
          range.getPrefix(),
          Route.UNSET_NEXT_HOP_INTERFACE,
          _process.getExternalAdminCost(),
          maximumType2Metric + 1L,
          _process.getRouterId(),
          Route.UNSET_ROUTE_TAG);
    }

    if (maximumType1Cost < 0L
        || maximumType1Cost
            > Ospfv3Process.MAX_METRIC) {
      return null;
    }

    /*
     * An aggregated Type-7 range has no forwarding-address state in this
     * VI model. Encode the complete highest N1 path cost as the E1
     * aggregate metric at the translating ABR.
     */
    return new Ospfv3ExternalType1Route6(
        range.getPrefix(),
        Route.UNSET_NEXT_HOP_INTERFACE,
        _process.getExternalAdminCost(),
        maximumType1Cost,
        _process.getRouterId(),
        Route.UNSET_ROUTE_TAG);
  }

  /**
   * Translate one specific Type-7 route to Type-5.
   *
   * <p>The model does not yet carry an explicit OSPF forwarding-address
   * field. For N1, retain the already accumulated NSSA internal cost in the
   * translated VI route so downstream E1 total cost remains correct while
   * preserving the original external LSA metric separately.
   */
  private AbstractRoute6 toTranslatedType5(
      AbstractRoute6 external) {

    if (external
        instanceof Ospfv3NssaExternalType1Route6) {

      Ospfv3NssaExternalType1Route6 n1 =
          (Ospfv3NssaExternalType1Route6) external;

      return new Ospfv3ExternalType1Route6(
          n1.getNetwork(),
          Route.UNSET_NEXT_HOP_INTERFACE,
          null,
          _process.getExternalAdminCost(),
          n1.getMetric(),
          n1.getLsaMetric(),
          0L,
          n1.getCostToAdvertiser(),
          _process.getRouterId(),
          n1.getTag());
    }

    Ospfv3NssaExternalType2Route6 n2 =
        (Ospfv3NssaExternalType2Route6) external;

    return new Ospfv3ExternalType2Route6(
        n2.getNetwork(),
        Route.UNSET_NEXT_HOP_INTERFACE,
        _process.getExternalAdminCost(),
        n2.getMetric(),
        _process.getRouterId(),
        n2.getTag());
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
   * Return routes eligible to be redistributed into another OSPFv3 process
   * on this router.
   *
   * <p>Locally originated external routes that were themselves created by
   * cross-process redistribution are omitted. This preserves genuine routes
   * from this process while preventing reciprocal redistribute-ospf
   * configuration from endlessly re-originating the same prefix.
   */
  private Set<AbstractRoute6>
      getRoutesForProcessRedistribution() {

    ImmutableSet.Builder<AbstractRoute6> routes =
        ImmutableSet.builder();

    for (AbstractRoute6 route :
        getRoutes()) {

      if (isLocallyOriginatedCrossProcessExternal(
          route)) {
        continue;
      }

      routes.add(
          route);
    }

    return routes.build();
  }

  private boolean isLocallyOriginatedCrossProcessExternal(
      AbstractRoute6 route) {

    if (!_crossProcessExternalPrefixes.contains(
        route.getNetwork())) {

      return false;
    }

    Ip localRouterId =
        _process.getRouterId();

    if (route instanceof
        Ospfv3ExternalType1Route6) {

      Ospfv3ExternalType1Route6 external =
          (Ospfv3ExternalType1Route6) route;

      return external
              .getAdvertiser()
              .equals(localRouterId)
          && external
              .getCostToAdvertiser()
              == 0L;
    }

    if (route instanceof
        Ospfv3ExternalType2Route6) {

      Ospfv3ExternalType2Route6 external =
          (Ospfv3ExternalType2Route6) route;

      return external
              .getAdvertiser()
              .equals(localRouterId)
          && external
              .getCostToAdvertiser()
              == 0L;
    }

    if (route instanceof
        Ospfv3NssaExternalType1Route6) {

      Ospfv3NssaExternalType1Route6 external =
          (Ospfv3NssaExternalType1Route6) route;

      return external
              .getAdvertiser()
              .equals(localRouterId)
          && external
              .getCostToAdvertiser()
              == 0L;
    }

    if (route instanceof
        Ospfv3NssaExternalType2Route6) {

      Ospfv3NssaExternalType2Route6 external =
          (Ospfv3NssaExternalType2Route6) route;

      return external
              .getAdvertiser()
              .equals(localRouterId)
          && external
              .getCostToAdvertiser()
              == 0L;
    }

    return false;
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
        _crossProcessExternalPrefixes,
        _translatedNssaExternalAdvertisements,
        _virtualBackboneOperational);
  }

  private final @Nonnull Configuration _c;
  private Set<AbstractRoute6>
      _localExternalAdvertisements;
  private Set<Prefix6>
      _crossProcessExternalPrefixes;
  private Set<AbstractRoute6>
      _translatedNssaExternalAdvertisements;
  private final @Nonnull Ospfv3Process _process;
  private final @Nonnull Ospfv3Rib6 _ospfv3Rib;
  private boolean _virtualBackboneOperational;
  private final @Nonnull String _vrfName;
}
