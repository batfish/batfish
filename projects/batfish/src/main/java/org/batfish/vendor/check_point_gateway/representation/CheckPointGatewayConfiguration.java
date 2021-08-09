package org.batfish.vendor.check_point_gateway.representation;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
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
import org.batfish.vendor.check_point_gateway.representation.BondingGroup.Mode;

public class CheckPointGatewayConfiguration extends VendorConfiguration {

  public static final String VRF_NAME = "default";

  public CheckPointGatewayConfiguration() {
    _bondingGroups = new HashMap<>();
    _interfaces = new HashMap<>();
    _staticRoutes = new HashMap<>();
  }

  @Nonnull
  public Map<Integer, BondingGroup> getBondingGroups() {
    return _bondingGroups;
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
    _c.setHumanName(hostname);
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
   * Returns a {@link Stream} of VI {@link org.batfish.datamodel.StaticRoute} corresponding to the
   * specified VS {@link StaticRoute}. Only routes with valid nexthop targets are returned.
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
      if (name.contains(".")) {
        return InterfaceType.LOGICAL;
      }
      return InterfaceType.PHYSICAL;
    } else if (name.startsWith("lo")) {
      return InterfaceType.LOOPBACK;
    } else if (name.startsWith("bond")) {
      if (name.contains(".")) {
        return InterfaceType.AGGREGATE_CHILD;
      }
      return InterfaceType.AGGREGATED;
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
            .setType(getInterfaceType(iface));

    Optional<Integer> parentBondingGroupOpt = getParentBondingGroupNumber(iface);
    if (parentBondingGroupOpt.isPresent()) {
      Integer parentBondingGroup = parentBondingGroupOpt.get();
      newIface.setChannelGroup(parentBondingGroup.toString());
      Interface parentBondInterface = _interfaces.get(getBondInterfaceName(parentBondingGroup));
      assert parentBondInterface != null;
      newIface
          .setChannelGroup(parentBondingGroup.toString())
          // Member interface inherits some configuration from parent bonding group
          .setMtu(parentBondInterface.getMtuEffective());
    } else {
      newIface
          .setAddress(iface.getAddress())
          .setActive(iface.getState())
          .setMtu(iface.getMtuEffective());
    }

    if (iface.getVlanId() != null) {
      newIface.setEncapsulationVlan(iface.getVlanId());
    }
    if (iface.getParentInterface() != null) {
      assert _interfaces.containsKey(iface.getParentInterface());
      newIface.setDependencies(
          ImmutableList.of(new Dependency(iface.getParentInterface(), DependencyType.BIND)));
    }

    getBondingGroup(ifaceName)
        .ifPresent(
            bg -> {
              Set<String> members = getValidMembers(bg);
              newIface.setChannelGroupMembers(members);
              newIface.setDependencies(
                  members.stream()
                      .map(member -> new Dependency(member, DependencyType.AGGREGATE))
                      .collect(ImmutableSet.toImmutableSet()));

              if (bg.getModeEffective() == Mode.ACTIVE_BACKUP) {
                _w.redFlag(
                    String.format(
                        "Bonding group mode active-backup is not yet supported in Batfish."
                            + " Deactivating interface %s.",
                        ifaceName));
                newIface.setActive(false);
              }
            });
    return newIface.build();
  }

  /**
   * Get the {@link BondingGroup} corresponding to the specified bond interface. Returns {@link
   * Optional#empty} if the interface is not a bond interface or if the bonding group does not
   * exist.
   */
  @Nonnull
  private Optional<BondingGroup> getBondingGroup(String ifaceName) {
    Pattern p = Pattern.compile("bond(\\d+)");
    Matcher res = p.matcher(ifaceName);
    if (res.matches()) {
      return Optional.ofNullable(_bondingGroups.get(Integer.valueOf(res.group(1))));
    }
    return Optional.empty();
  }

  /**
   * Returns the parent bonding group number for the specified interface, or {@link Optional#empty}
   * if it is not a member of a bonding group.
   */
  @Nonnull
  private Optional<Integer> getParentBondingGroupNumber(Interface iface) {
    return _bondingGroups.values().stream()
        .filter(bg -> bg.getInterfaces().contains(iface.getName()))
        .findFirst()
        .map(BondingGroup::getNumber);
  }

  /** Get bonding interface name from its bonding group number. */
  @Nonnull
  public static String getBondInterfaceName(int groupNumber) {
    return "bond" + groupNumber;
  }

  /**
   * Returns a {@link Set} of valid members for the specified bonding group. Adds warnings if any
   * member interfaces are invalid.
   *
   * <p>Filters out things like non-existent interfaces.
   */
  @Nonnull
  private Set<String> getValidMembers(BondingGroup bondingGroup) {
    return bondingGroup.getInterfaces().stream()
        .filter(
            m -> {
              boolean exists = _interfaces.containsKey(m);
              if (!exists) {
                _w.redFlag(
                    String.format(
                        "Cannot reference non-existent interface %s in bonding group %d.",
                        m, bondingGroup.getNumber()));
              }
              return exists;
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  @Nonnull private Map<Integer, BondingGroup> _bondingGroups;
  private Configuration _c;
  private String _hostname;

  private Map<String, Interface> _interfaces;
  /** destination prefix -> static route definition */
  private Map<Prefix, StaticRoute> _staticRoutes;

  private ConfigurationFormat _vendor;
}
