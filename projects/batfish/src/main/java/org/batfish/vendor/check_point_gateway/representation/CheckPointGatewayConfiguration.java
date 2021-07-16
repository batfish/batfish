package org.batfish.vendor.check_point_gateway.representation;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.vendor.VendorConfiguration;

public class CheckPointGatewayConfiguration extends VendorConfiguration {

  public static final String VRF_NAME = "default";

  public CheckPointGatewayConfiguration() {
    _interfaces = new HashMap<>();
    _staticRoutes = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public Map<Prefix, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setDeviceModel(DeviceModel.CHECK_POINT_GATEWAY);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Gateways don't have VRFs, so put everything in a generated default VRF
    Vrf vrf = new Vrf(VRF_NAME);
    _c.setVrfs(ImmutableMap.of(VRF_NAME, vrf));

    _interfaces.forEach((ifaceName, iface) -> toInterface(iface, vrf));

    vrf.getStaticRoutes()
        .addAll(
            _staticRoutes.values().stream()
                .flatMap(staticRoute -> convertStaticRoute(staticRoute, _interfaces))
                .collect(ImmutableSet.toImmutableSet()));
    return ImmutableList.of(_c);
  }

  /**
   * Returns a {@link Stream of VI {@link org.batfish.datamodel.StaticRoute} corresponding to the
   * specified VS {@link StaticRoute}. Only addresses corresponding to connected routes are
   * considered and only nexthops with the lowest {@code priority} value are returned.
   */
  private static Stream<org.batfish.datamodel.StaticRoute> convertStaticRoute(
      StaticRoute route, Map<String, Interface> interfaces) {
    return route.getNexthops().values().stream()
        .filter(nh -> nexthopIsValid(nh.getNexthopTarget(), interfaces))
        .map(
            nh ->
                org.batfish.datamodel.StaticRoute.builder()
                    .setNetwork(route.getDestination())
                    .setNextHop(toNextHop(nh.getNexthopTarget()))
                    // Unset priority is preferred over other priorities
                    .setAdministrativeCost(firstNonNull(nh.getPriority(), 0))
                    .build());
  }

  /**
   * Returns {@code boolean} indicating if {@link NexthopTarget} is valid, given a map of all {@link
   * Interface}.
   */
  private static boolean nexthopIsValid(NexthopTarget target, Map<String, Interface> interfaces) {
    if (target instanceof NexthopAddress) {
      Ip addr = ((NexthopAddress) target).getAddress();
      return interfaces.values().stream().anyMatch(i -> ifaceContainsAddress(i, addr));
    } else if (target instanceof NexthopLogical) {
      String targetInterface = ((NexthopLogical) target).getInterface();
      // Guaranteed by extraction
      assert interfaces.containsKey(targetInterface);
    }
    return true;
  }

  private static boolean ifaceContainsAddress(Interface iface, Ip address) {
    ConcreteInterfaceAddress addr = iface.getAddress();
    if (addr == null) {
      return false;
    }
    return addr.getPrefix().containsIp(address);
  }

  /** Convert specified VS {@link NexthopTarget} into a VI {@link NextHop}. */
  private static NextHop toNextHop(NexthopTarget target) {
    if (target instanceof NexthopAddress) {
      return NextHopIp.of(((NexthopAddress) target).getAddress());
    } else if (target instanceof NexthopLogical) {
      return NextHopInterface.of(((NexthopLogical) target).getInterface());
    }
    assert target instanceof NexthopBlackhole || target instanceof NexthopReject;
    return NextHopDiscard.instance();
  }

  InterfaceType getInterfaceType(Interface iface) {
    String name = iface.getName();
    if (name.startsWith("eth")) {
      return InterfaceType.PHYSICAL;
    } else if (name.startsWith("lo")) {
      return InterfaceType.LOOPBACK;
    }
    return InterfaceType.UNKNOWN;
  }

  org.batfish.datamodel.Interface toInterface(Interface iface, Vrf vrf) {
    String ifaceName = iface.getName();
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(ifaceName)
            .setOwner(_c)
            .setVrf(vrf)
            .setActive(iface.getState())
            .setAddress(iface.getAddress())
            .setType(getInterfaceType(iface))
            .setMtu(iface.getMtuEffective());
    return newIface.build();
  }

  private Configuration _c;
  private String _hostname;
  private Map<String, Interface> _interfaces;
  /** destination prefix -> static route definition */
  private Map<Prefix, StaticRoute> _staticRoutes;

  private ConfigurationFormat _vendor;
}
