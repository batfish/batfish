package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
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

  Ospfv3RoutingProcess(
      Ospfv3Process process,
      String vrfName,
      Configuration configuration) {
    _process = process;
    _vrfName = vrfName;
    _c = configuration;
    _ospfv3Rib = new Ospfv3Rib6();
  }

  /**
   * Reset this process to routes originated locally by this router.
   *
   * <p>Learned routes are intentionally discarded here so a new IGP
   * convergence pass naturally withdraws routes whose adjacencies disappeared.
   */
  void initialize(ConnectedRib6 connectedRib) {
    _ospfv3Rib.clear();
    initializeIntraAreaRoutes();

    if (_process.getRedistributeConnected()) {
      initializeRedistributedConnectedRoutes(connectedRib);
    }
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
                _process.getAdminCost(),
                cost,
                area.getAreaNumber()));
      }
    }
  }

  private void initializeRedistributedConnectedRoutes(
      ConnectedRib6 connectedRib) {
    for (ConnectedRoute6 connected :
        connectedRib.getRoutes()) {

      // A network already originated by this process is internal and must not
      // also be originated by the same process as an external route.
      if (isInternallyOriginatedConnectedRoute(connected)) {
        continue;
      }

      _ospfv3Rib.mergeRoute(
          new Ospfv3ExternalType2Route6(
              connected.getNetwork(),
              connected.getNextHopInterface(),
              _process.getAdminCost(),
              _process.getRedistributionMetric(),
              _process.getRouterId()));
    }
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

          Set<AbstractRoute6> remoteRoutes =
              remoteProcess.getRoutes();

          changed |=
              importIntraAreaRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteRoutes);

          changed |=
              importExternalRoutesFromNeighbor(
                  localIface,
                  remoteIface,
                  localSettings.getAreaName(),
                  remoteRoutes);
        }
      }
    }

    return changed;
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
      if (remoteIface
          .getName()
          .equals(intra.getNextHopInterface())) {
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
                  _process.getAdminCost(),
                  newMetric,
                  area,
                  intra.getTag()));
    }

    return changed;
  }

  /**
   * Import OSPFv3 external type-2 routes from a neighbor.
   *
   * <p>The external metric is not incremented for an E2 route. Instead we
   * separately track the internal cost to the advertising ASBR and use it as
   * the E2 tie breaker, matching the existing IPv4 OSPF model.
   */
  private boolean importExternalRoutesFromNeighbor(
      Interface localIface,
      Interface remoteIface,
      long area,
      Set<AbstractRoute6> remoteRoutes) {
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
          instanceof Ospfv3ExternalType2Route6)) {
        continue;
      }

      Ospfv3ExternalType2Route6 external =
          (Ospfv3ExternalType2Route6) route;

      // Do not accept our own external advertisement back from the network.
      if (external
              .getAdvertiser()
              .equals(_process.getRouterId())
          && external.getCostToAdvertiser() != 0L) {
        continue;
      }

      // Do not immediately send a learned route back toward the neighbor from
      // which it was learned. Resetting the OSPFv3 RIB before convergence
      // handles longer-path withdrawal cleanly.
      if (remoteIface
          .getName()
          .equals(
              external.getNextHopInterface())) {
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
                  _process.getAdminCost(),
                  external.getMetric(),
                  area,
                  newCostToAdvertiser,
                  external.getAdvertiser(),
                  external.getTag()));
    }

    return changed;
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

  @Nonnull
  Set<AbstractRoute6> getRoutes() {
    return _ospfv3Rib.getRoutes();
  }

  int iterationHashCode() {
    return _ospfv3Rib.getRoutes().hashCode();
  }

  private final @Nonnull Configuration _c;
  private final @Nonnull Ospfv3Process _process;
  private final @Nonnull Ospfv3Rib6 _ospfv3Rib;
  private final @Nonnull String _vrfName;
}
