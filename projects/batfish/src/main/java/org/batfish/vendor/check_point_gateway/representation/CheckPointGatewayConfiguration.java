package org.batfish.vendor.check_point_gateway.representation;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.FirewallSessionInterfaceInfo.Action.POST_NAT_FIB_LOOKUP;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.aclName;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpAccessLists;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getManualNatRules;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualHideRuleTransformation;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.mergeTransformations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.check_point_gateway.representation.BondingGroup.Mode;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AddressSpace;
import org.batfish.vendor.check_point_management.AddressSpaceToIpSpace;
import org.batfish.vendor.check_point_management.CheckpointManagementConfiguration;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.ManagementDomain;
import org.batfish.vendor.check_point_management.ManagementPackage;
import org.batfish.vendor.check_point_management.ManagementServer;
import org.batfish.vendor.check_point_management.NatMethod;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.batfish.vendor.check_point_management.UnknownTypedManagementObject;

public class CheckPointGatewayConfiguration extends VendorConfiguration {

  public static final String VRF_NAME = "default";
  public static final String INTERFACE_ACL_NAME = "~INTERFACE_ACL~";

  public CheckPointGatewayConfiguration() {
    _bondingGroups = new HashMap<>();
    _interfaces = new HashMap<>();
    _staticRoutes = new HashMap<>();
  }

  public void finalizeStructures() {
    _bondingGroups = toImmutableMap(_bondingGroups);
    _interfaces = toImmutableMap(_interfaces);
    _staticRoutes = toImmutableMap(_staticRoutes);
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

    Optional<CheckpointManagementConfiguration> mgmtConfig =
        Optional.ofNullable(getConversionContext())
            .map(ConversionContext::getCheckpointManagementConfiguration)
            .map(cmc -> (CheckpointManagementConfiguration) cmc);
    mgmtConfig.ifPresent(this::convertManagementConfig);

    // Gateways don't have VRFs, so put everything in a generated default VRF
    Vrf vrf = new Vrf(VRF_NAME);
    _c.setVrfs(ImmutableMap.of(VRF_NAME, vrf));

    _interfaces.forEach((ifaceName, iface) -> convertInterface(iface, vrf));

    vrf.getStaticRoutes()
        .addAll(
            _staticRoutes.values().stream()
                .flatMap(staticRoute -> convertStaticRoute(staticRoute, _interfaces))
                .collect(ImmutableSet.toImmutableSet()));

    return ImmutableList.of(_c);
  }

  /** Converts management server settings applicable to this configuration */
  private void convertManagementConfig(CheckpointManagementConfiguration mgmtConfig) {
    // Find gateway
    Optional<Entry<ManagementDomain, GatewayOrServer>> maybeGatewayAndDomain =
        findGatewayAndDomain(mgmtConfig);
    if (!maybeGatewayAndDomain.isPresent()) {
      return;
    }
    ManagementDomain domain = maybeGatewayAndDomain.get().getKey();
    GatewayOrServer gateway = maybeGatewayAndDomain.get().getValue();
    // Find package
    Optional<ManagementPackage> maybePackage = findAccessPackage(domain, gateway);
    if (!maybePackage.isPresent()) {
      return;
    }
    convertPackage(maybePackage.get(), gateway, domain);
  }

  private void convertAccessLayers(List<AccessLayer> accessLayers) {
    // TODO support matching multiple access layers
    if (accessLayers.size() > 1) {
      _w.redFlag(
          "Batfish currently only supports matching on a single Access Layer, so only the first"
              + " matching Access Rule will be applied.");
    }
    for (AccessLayer al : accessLayers) {
      Map<String, IpAccessList> acl = toIpAccessLists(al);
      _c.getIpAccessLists().putAll(acl);
    }
    IpAccessList interfaceAcl =
        IpAccessList.builder()
            .setName(INTERFACE_ACL_NAME)
            .setLines(
                accessLayers.stream()
                    .map(l -> new AclAclLine(l.getName(), aclName(l)))
                    .collect(ImmutableList.toImmutableList()))
            .build();
    _c.getIpAccessLists().put(interfaceAcl.getName(), interfaceAcl);
  }

  /**
   * Convert specified objects to their VI model equivalent representation(s) if applicable, and add
   * them to the VI configuration. E.g. convert {@link AddressSpace}s to {@link IpSpace}s.
   *
   * <p><b>Note: different objects (e.g. revisions) of the same name/type are not supported. Only
   * the last-encountered object with a particular name (of the same type) will produce a VI model
   * object.</b>
   *
   * <p>Warns about unknown object types.
   */
  private void convertObjects(Map<Uid, TypedManagementObject> objs) {
    AddressSpaceToIpSpace addressSpaceToIpSpace = new AddressSpaceToIpSpace(objs);
    objs.values()
        .forEach(
            obj -> {
              if (obj instanceof UnknownTypedManagementObject) {
                UnknownTypedManagementObject utmo = (UnknownTypedManagementObject) obj;
                _w.redFlag(
                    String.format(
                        "Batfish does not handle converting objects of type %s. These objects will"
                            + " be ignored.",
                        utmo.getType()));
                return;
              }

              if (obj instanceof AddressSpace) {
                AddressSpace addressSpace = (AddressSpace) obj;
                IpSpace ipSpace = addressSpace.accept(addressSpaceToIpSpace);
                _c.getIpSpaces().put(obj.getName(), ipSpace);
              }
            });
  }

  /**
   * Convert constructs in the specified package to their VI model equivalent and add them to the VI
   * configuration.
   */
  private void convertPackage(
      ManagementPackage pakij, GatewayOrServer gateway, ManagementDomain domain) {
    convertObjects(pakij, domain);
    convertAccessLayers(pakij.getAccessLayers());
    Optional.ofNullable(pakij.getNatRulebase()).ifPresent(r -> convertNatRulebase(r, gateway));
  }

  private void convertObjects(ManagementPackage pakij, ManagementDomain domain) {
    Map<Uid, TypedManagementObject> objects = new HashMap<>();
    Optional.ofNullable(pakij.getNatRulebase())
        .map(NatRulebase::getObjectsDictionary)
        .ifPresent(objects::putAll);
    pakij.getAccessLayers().stream()
        .map(AccessLayer::getObjectsDictionary)
        .forEach(objects::putAll);
    domain.getObjects().forEach(object -> objects.put(object.getUid(), object));
    convertObjects(objects);
  }

  /** Converts the given {@link NatRulebase} and applies it to this config. */
  @SuppressWarnings("unused")
  private void convertNatRulebase(NatRulebase natRulebase, GatewayOrServer gateway) {
    List<Transformation> manualHideRuleTransformations =
        getManualNatRules(natRulebase, gateway)
            .filter(rule -> rule.getMethod() == NatMethod.HIDE)
            .map(natRule -> manualHideRuleTransformation(natRulebase, natRule, getWarnings()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableList.toImmutableList());
    if (getManualNatRules(natRulebase, gateway)
        .anyMatch(rule -> rule.getMethod() != NatMethod.HIDE)) {
      _w.redFlag("Non-HIDE NAT rules are unsupported");
    }
    _natTransformation = mergeTransformations(manualHideRuleTransformations).orElse(null);
    // TODO Apply transformations to appropriate interfaces
  }

  private @Nonnull Optional<ManagementPackage> findAccessPackage(
      ManagementDomain domain, GatewayOrServer gateway) {
    String accessPackageName = gateway.getPolicy().getAccessPolicyName();
    if (accessPackageName == null) {
      return Optional.empty();
    }
    // TODO: can be more efficient if we also store map: packageName -> package in ManagementDomain
    Optional<ManagementPackage> maybePackage =
        domain.getPackages().values().stream()
            .filter(p -> p.getPackage().getName().equals(accessPackageName))
            .findFirst();
    if (!maybePackage.isPresent()) {
      _w.redFlag(
          String.format(
              "Gateway or server '%s' access-policy-name refers to non-existent package '%s'",
              gateway.getName(), accessPackageName));
    }
    return maybePackage;
  }

  private @Nonnull Optional<Entry<ManagementDomain, GatewayOrServer>> findGatewayAndDomain(
      CheckpointManagementConfiguration mgmtConfig) {
    // TODO handle linking to secondary IP addresses, if that is allowed
    Set<Ip> ips =
        _interfaces.values().stream()
            .map(Interface::getAddress)
            .filter(Objects::nonNull)
            .map(ConcreteInterfaceAddress::getIp)
            .collect(ImmutableSet.toImmutableSet());
    // TODO: something special where there is IP reuse?
    for (ManagementServer server : mgmtConfig.getServers().values()) {
      for (ManagementDomain domain : server.getDomains().values()) {
        Optional<GatewayOrServer> maybeGateway =
            domain.getGatewaysAndServers().values().stream()
                .filter(gw -> gw.getIpv4Address() != null)
                .filter(gw -> ips.contains(gw.getIpv4Address()))
                .findFirst();
        if (maybeGateway.isPresent()) {
          return Optional.of(Maps.immutableEntry(domain, maybeGateway.get()));
        }
      }
    }
    return Optional.empty();
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
                    .setRecursive(false)
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

  void convertInterface(Interface iface, Vrf vrf) {
    String ifaceName = iface.getName();
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(ifaceName)
            .setOwner(_c)
            .setVrf(vrf)
            .setType(getInterfaceType(iface));

    Optional<Integer> parentBondingGroupOpt = getParentBondingGroupNumber(iface);
    if (parentBondingGroupOpt.isPresent()) {
      String parentBondIfaceName = getBondInterfaceName(parentBondingGroupOpt.get());
      Interface parentBondInterface = _interfaces.get(parentBondIfaceName);
      assert parentBondInterface != null;
      newIface
          .setChannelGroup(parentBondIfaceName)
          // Member interface inherits some configuration from parent bonding group
          .setMtu(parentBondInterface.getMtuEffective());
    } else {
      newIface
          .setAddress(iface.getAddress())
          .setActive(iface.getState())
          .setMtu(iface.getMtuEffective());
    }

    Double speed = iface.getLinkSpeedEffective();
    if (speed != null) {
      newIface.setSpeed(speed);
      newIface.setBandwidth(speed);
    }
    if (iface.getVlanId() != null) {
      newIface.setEncapsulationVlan(iface.getVlanId());
    }
    if (iface.getParentInterface() != null) {
      Interface parent = _interfaces.get(iface.getParentInterface());
      // This is a subinterface. Its speed can't be set explicitly.
      // If its parent is physical, this interface should inherit the parent's speed/bw now.
      // If its parent is a bond interface, then this interface's bandwidth will be set after
      // the parent's bandwidth is calculated post-conversion.
      assert parent != null;
      Double parentSpeed = parent.getLinkSpeedEffective();
      if (parentSpeed != null) {
        newIface.setSpeed(parentSpeed);
        newIface.setBandwidth(parentSpeed);
      }
      newIface.setDependencies(
          ImmutableList.of(new Dependency(iface.getParentInterface(), DependencyType.BIND)));
    }

    getBondingGroup(ifaceName)
        .ifPresent(
            bg -> {
              newIface.setChannelGroupMembers(bg.getInterfaces());
              newIface.setDependencies(
                  bg.getInterfaces().stream()
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

    // TODO confirm AccessRule interaction with NAT
    newIface.setIncomingFilter(_c.getIpAccessLists().get(INTERFACE_ACL_NAME));
    newIface.setIncomingTransformation(_natTransformation);
    newIface.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            POST_NAT_FIB_LOOKUP, ImmutableList.of(ifaceName), null, null));
    newIface.build();
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

  @Nonnull private Map<Integer, BondingGroup> _bondingGroups;
  private Configuration _c;
  private String _hostname;

  private Map<String, Interface> _interfaces;
  /** destination prefix -> static route definition */
  private Map<Prefix, StaticRoute> _staticRoutes;

  private transient Transformation _natTransformation;

  private ConfigurationFormat _vendor;
}
