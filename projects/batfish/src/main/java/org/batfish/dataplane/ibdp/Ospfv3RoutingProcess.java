package org.batfish.dataplane.ibdp;

import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute6;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ospfv3ExternalType2Route6;
import org.batfish.datamodel.Ospfv3IntraAreaRoute6;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.batfish.dataplane.rib.ConnectedRib6;
import org.batfish.dataplane.rib.Ospfv3Rib6;

/**
 * Dataplane OSPFv3 process.
 *
 * <p>This first implementation handles locally originated intra-area
 * IPv6 routes and connected-route redistribution. Neighbor exchange
 * and SPF propagation are intentionally layered on top later.
 */
@ParametersAreNonnullByDefault
final class Ospfv3RoutingProcess {

  Ospfv3RoutingProcess(
      Ospfv3Process process,
      String vrfName,
      Configuration configuration) {
    _process = process;
    _vrfName = vrfName;
    _c = configuration;
    _ospfv3Rib = new Ospfv3Rib6();
  }

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

      // Networks already originated by this OSPFv3 process are internal,
      // not redistributed back into the same process as external routes.
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
   * <p>Explicit interface cost wins. AOS-CX uses 1 Gbps as the
   * calculated link speed for VLAN interfaces. For other interfaces
   * with known bandwidth, reference-bandwidth/link-bandwidth is used.
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
      // We cannot infer physical link speed from configuration alone.
      // Preserve reachability with minimum OSPF cost until speed is
      // available in the VI model.
      return 1L;
    }

    long calculated =
        (long)
            (_process.getReferenceBandwidth()
                / bandwidth);

    return Math.max(1L, Math.min(65535L, calculated));
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
