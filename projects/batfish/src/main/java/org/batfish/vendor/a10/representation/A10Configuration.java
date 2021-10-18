package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.FirewallSessionInterfaceInfo.Action.POST_NAT_FIB_LOOKUP;
import static org.batfish.vendor.a10.representation.A10Conversion.VIRTUAL_TCP_PORT_TYPES;
import static org.batfish.vendor.a10.representation.A10Conversion.VIRTUAL_UDP_PORT_TYPES;
import static org.batfish.vendor.a10.representation.A10Conversion.getEnabledVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolIpsForAllVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIpsForAllVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.isVrrpAEnabled;
import static org.batfish.vendor.a10.representation.A10Conversion.orElseChain;
import static org.batfish.vendor.a10.representation.A10Conversion.toDstTransformationSteps;
import static org.batfish.vendor.a10.representation.A10Conversion.toMatchCondition;
import static org.batfish.vendor.a10.representation.A10Conversion.toSnatTransformationStep;
import static org.batfish.vendor.a10.representation.A10Conversion.toVrrpGroupBuilder;
import static org.batfish.vendor.a10.representation.A10Conversion.toVrrpGroups;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;
import static org.batfish.vendor.a10.representation.StaticRoute.DEFAULT_STATIC_ROUTE_DISTANCE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.ApplyAny;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.VendorConfiguration;

/** Datamodel class representing an A10 device configuration. */
public final class A10Configuration extends VendorConfiguration {

  public A10Configuration() {
    _interfacesEthernet = new HashMap<>();
    _interfacesLoopback = new HashMap<>();
    _interfacesTrunk = new HashMap<>();
    _interfacesVe = new HashMap<>();
    _natPools = new HashMap<>();
    _serviceGroups = new HashMap<>();
    _servers = new HashMap<>();
    _staticRoutes = new HashMap<>();
    _virtualServers = new HashMap<>();
    _vlans = new HashMap<>();
  }

  @Nullable
  public BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  /** Gets the {@link BgpProcess} for this device, creating a new one if none already exists. */
  @Nonnull
  public BgpProcess getOrCreateBgpProcess(long number) {
    if (_bgpProcess == null) {
      _bgpProcess = new BgpProcess(number);
    }
    return _bgpProcess;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<Integer, Interface> getInterfacesEthernet() {
    return _interfacesEthernet;
  }

  public Map<Integer, Interface> getInterfacesLoopback() {
    return _interfacesLoopback;
  }

  public Map<Integer, TrunkInterface> getInterfacesTrunk() {
    return _interfacesTrunk;
  }

  public Map<Integer, Interface> getInterfacesVe() {
    return _interfacesVe;
  }

  public Map<String, NatPool> getNatPools() {
    return _natPools;
  }

  public Map<String, ServiceGroup> getServiceGroups() {
    return ImmutableMap.copyOf(_serviceGroups);
  }

  @Nonnull
  public ServiceGroup getOrCreateServiceGroup(String name, ServerPort.Type type) {
    return _serviceGroups.computeIfAbsent(name, n -> new ServiceGroup(name, type));
  }

  public Map<String, Server> getServers() {
    return _servers;
  }

  @Nonnull
  public Map<String, VirtualServer> getVirtualServers() {
    return Collections.unmodifiableMap(_virtualServers);
  }

  @Nullable
  public VirtualServer getVirtualServer(String name) {
    return _virtualServers.get(name);
  }

  /**
   * Get the {@link VirtualServer} corresponding to the provided name, creating it (with the
   * specified {@link VirtualServerTarget}) if it doesn't already exist.
   */
  @Nonnull
  public VirtualServer getOrCreateVirtualServer(String name, VirtualServerTarget target) {
    return _virtualServers.computeIfAbsent(name, n -> new VirtualServer(n, target));
  }

  /** Map of route {@link Prefix} to {@link StaticRouteManager} for that prefix. */
  public Map<Prefix, StaticRouteManager> getStaticRoutes() {
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

  @VisibleForTesting
  public static boolean getInterfaceEnabledEffective(Interface iface) {
    Boolean enabled = iface.getEnabled();
    if (enabled != null) {
      return enabled;
    }
    switch (iface.getType()) {
      case ETHERNET:
        return false;
      case LOOPBACK:
      case TRUNK:
      case VE:
        return true;
      default:
        assert false;
        return true;
    }
  }

  @VisibleForTesting
  public static int getInterfaceMtuEffective(Interface iface) {
    return firstNonNull(iface.getMtu(), DEFAULT_MTU);
  }

  public Map<Integer, Vlan> getVlans() {
    return _vlans;
  }

  public static InterfaceType getInterfaceType(Interface iface) {
    switch (iface.getType()) {
      case ETHERNET:
        return InterfaceType.PHYSICAL;
      case LOOPBACK:
        return InterfaceType.LOOPBACK;
      case VE:
        return InterfaceType.VLAN;
      case TRUNK:
        return InterfaceType.AGGREGATED;
      default:
        assert false;
        return InterfaceType.UNKNOWN;
    }
  }

  @Nonnull
  public static String getInterfaceName(InterfaceReference ref) {
    return getInterfaceName(ref.getType(), ref.getNumber());
  }

  @Nonnull
  public static String getInterfaceName(Interface iface) {
    return getInterfaceName(iface.getType(), iface.getNumber());
  }

  @Nonnull
  public static String getInterfaceName(Interface.Type type, int num) {
    return getInterfaceHumanName(type, num).replace(" ", "");
  }

  @Nonnull
  public static String getInterfaceHumanName(Interface iface) {
    return getInterfaceHumanName(iface.getType(), iface.getNumber());
  }

  @Nonnull
  public static String getInterfaceHumanName(Interface.Type type, int num) {
    if (type == Interface.Type.VE) {
      return String.format("VirtualEthernet %s", num);
    }

    String typeStr = type.toString();
    // Only the first letter should be capitalized, similar to A10 `show` data
    return String.format(
        "%s%s %s", typeStr.substring(0, 1), typeStr.substring(1).toLowerCase(), num);
  }

  public @Nullable VrrpA getVrrpA() {
    return _vrrpA;
  }

  public @Nonnull VrrpA getOrCreateVrrpA() {
    if (_vrrpA == null) {
      _vrrpA = new VrrpA();
    }
    return _vrrpA;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setHumanName(hostname);
    _c.setDeviceModel(DeviceModel.A10);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    // Generated default VRF
    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    _c.setVrfs(ImmutableMap.of(DEFAULT_VRF_NAME, vrf));

    _ifaceNametoIface = new HashMap<>();
    _interfacesLoopback.forEach(
        (num, iface) -> {
          convertInterface(iface, vrf);
          _ifaceNametoIface.put(getInterfaceName(iface), iface);
        });
    _interfacesEthernet.forEach(
        (num, iface) -> {
          convertInterface(iface, vrf);
          _ifaceNametoIface.put(getInterfaceName(iface), iface);
        });
    _interfacesVe.forEach(
        (num, iface) -> {
          convertInterface(iface, vrf);
          _ifaceNametoIface.put(getInterfaceName(iface), iface);
        });
    _interfacesTrunk.forEach((num, iface) -> convertInterface(iface, vrf));

    _staticRoutes.forEach(
        (prefix, manager) ->
            manager.getVariants().forEach((ip, sr) -> convertStaticRoute(vrf, prefix, sr)));

    // Must be done after interface conversion
    convertVirtualServers();
    convertVrrpA();

    markStructures();
    return ImmutableList.of(_c);
  }

  private void convertVrrpA() {
    // If vrrp-a is disabled, then the device should act as if it owns all addresses that would have
    // been part of vrrp-a.
    // If vrrp-a is enabled, then the device should own all addresses for VRIDs that are enabled.
    // TODO: handle floating-ips
    if (!isVrrpAEnabled(_vrrpA)) {
      convertVrrpADisabled();
    } else {
      convertVrrpAEnabled();
    }
  }

  /**
   * Process vrrp-a vrids in the case vrrp-a is disabled. Causes the device to own all virtual IPs
   * for all vrids.
   */
  private void convertVrrpADisabled() {
    // Overview:
    // - Add all virtual addresses to every inteface with a concrete IPv4 address.
    // - Set address metadata so no connected nor local routes are generated for virtual addresses.
    Set<ConcreteInterfaceAddress> virtualAddresses =
        Stream.concat(
                getNatPoolIpsForAllVrids(_natPools.values()),
                getVirtualServerIpsForAllVrids(_virtualServers.values()))
            .map(ip -> ConcreteInterfaceAddress.create(ip, Prefix.MAX_PREFIX_LENGTH))
            .collect(ImmutableSet.toImmutableSet());
    ConnectedRouteMetadata connectedRouteMetadata =
        ConnectedRouteMetadata.builder()
            .setGenerateConnectedRoute(false)
            .setGenerateLocalRoute(false)
            .build();
    SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata> addressMetadata =
        virtualAddresses.stream()
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Comparator.naturalOrder(),
                    virtualAddress -> virtualAddress,
                    unused -> connectedRouteMetadata));
    _c.getAllInterfaces().values().stream()
        .filter(A10Configuration::vrrpAAppliesToInterface)
        .forEach(
            iface -> {
              iface.setAllAddresses(
                  ImmutableSortedSet.<InterfaceAddress>naturalOrder()
                      .addAll(iface.getAllAddresses())
                      .addAll(virtualAddresses)
                      .build());
              iface.setAddressMetadata(
                  ImmutableSortedMap
                      .<ConcreteInterfaceAddress, ConnectedRouteMetadata>naturalOrder()
                      .putAll(iface.getAddressMetadata())
                      .putAll(addressMetadata)
                      .build());
            });
  }

  /**
   * Process vrrp-a vrids in the case vrrp-a is enabled. Causes the device to own all virtual
   * addresses for each vrid for which it is VRRP master.
   */
  private void convertVrrpAEnabled() {
    // Overview:
    // - Add a VrrpGroup for each enabled vrid on all L3 interfaces with a primary
    //   ConcreteInterfaceAddress.
    // - Each created VrrpGroup contains all the virtual addresses the device should own when it is
    //   master for the corresponding vrid.
    assert _vrrpA != null;
    // vrid -> virtual addresses
    ImmutableSetMultimap.Builder<Integer, Ip> virtualAddressesByEnabledVridBuilder =
        ImmutableSetMultimap.builder();
    // VRID 0 always exists, but may be disabled. Other VRIDs exist only if they are declared, and
    // cannot be disabled independently.
    // Grab the virtual addresses for each VRID from NAT pools and virtual-servers
    getEnabledVrids(_vrrpA)
        .forEach(
            vrid -> {
              Stream.concat(
                      getNatPoolIps(_natPools.values(), vrid),
                      getVirtualServerIps(_virtualServers.values(), vrid))
                  .forEach(ip -> virtualAddressesByEnabledVridBuilder.put(vrid, ip));
            });
    SetMultimap<Integer, Ip> virtualAddressesByEnabledVrid =
        virtualAddressesByEnabledVridBuilder.build();
    // VRID 0 may be used even if it is not configured explicitly.
    assert virtualAddressesByEnabledVrid.keySet().stream()
        .allMatch(vrid -> vrid == 0 || _vrrpA.getVrids().containsKey(vrid));
    // Create VrrpGroup builders for each vrid. We cannot make final VrrpGroups because we are
    // missing source address, which varies per interface.
    ImmutableMap.Builder<Integer, VrrpGroup.Builder> vrrpGroupBuildersBuilder =
        ImmutableMap.builder();
    virtualAddressesByEnabledVrid
        .asMap()
        .forEach(
            (vrid, virtualAddresses) ->
                vrrpGroupBuildersBuilder.put(
                    vrid, toVrrpGroupBuilder(_vrrpA.getVrids().get(vrid), virtualAddresses)));
    // Create and assign the final VRRP groups on each interface with a concrete IPv4 address.
    _c.getAllInterfaces().values().stream()
        .filter(A10Configuration::vrrpAAppliesToInterface)
        .forEach(i -> i.setVrrpGroups(toVrrpGroups(i, vrrpGroupBuildersBuilder.build())));
  }

  @VisibleForTesting
  static boolean vrrpAAppliesToInterface(org.batfish.datamodel.Interface iface) {
    if (iface.getInterfaceType() == InterfaceType.LOOPBACK) {
      return false;
    }
    return iface.getConcreteAddress() != null;
  }

  /**
   * Convert virtual-servers to load-balancing VI constructs and attach resulting transformations to
   * interfaces. Modifies VI interfaces and must be called after those are created.
   */
  private void convertVirtualServers() {
    Optional<Transformation> xform =
        orElseChain(
            _virtualServers.values().stream()
                .filter(A10Conversion::isVirtualServerEnabled)
                .flatMap(vs -> toSimpleTransformations(vs).stream())
                .collect(ImmutableList.toImmutableList()));
    xform.ifPresent(
        x ->
            _c.getAllInterfaces()
                .forEach(
                    (name, iface) -> {
                      iface.setFirewallSessionInterfaceInfo(
                          new FirewallSessionInterfaceInfo(
                              POST_NAT_FIB_LOOKUP, ImmutableList.of(iface.getName()), null, null));
                      iface.setIncomingTransformation(x);
                    }));
  }

  /**
   * Returns the list of intermediate {@link SimpleTransformation}s used to build the VI {@link
   * Transformation}. Each element corresponds to the transformation for a {@link Server} member of
   * the specified {@link VirtualServer}.
   */
  @Nonnull
  private List<SimpleTransformation> toSimpleTransformations(VirtualServer server) {
    return server.getPorts().values().stream()
        .filter(A10Conversion::isVirtualServerPortEnabled)
        .map(
            p ->
                new SimpleTransformation(
                    toMatchCondition(server.getTarget(), p, VirtualServerTargetToIpSpace.INSTANCE),
                    toTransformationStep(p)))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns the full transformation (DNAT and SNAT if applicable) for the specified virtual-server
   * port.
   */
  @Nonnull
  private TransformationStep toTransformationStep(VirtualServerPort port) {
    // No service-group means no load balancing
    String serviceGroupName = port.getServiceGroup();
    if (serviceGroupName == null) {
      return Noop.NOOP_DEST_NAT;
    }
    ServiceGroup serviceGroup = _serviceGroups.get(serviceGroupName);
    assert serviceGroup != null;
    ApplyAny dnatStep = new ApplyAny(toDstTransformationSteps(serviceGroup, _servers));

    String snatName = port.getSourceNat();
    if (snatName == null) {
      return dnatStep;
    }
    NatPool natPool = _natPools.get(snatName);
    TransformationStep snatStep = toSnatTransformationStep(natPool);

    return new ApplyAll(snatStep, dnatStep);
  }

  private void convertStaticRoute(Vrf vrf, Prefix prefix, StaticRoute staticRoute) {
    vrf.getStaticRoutes()
        .add(
            org.batfish.datamodel.StaticRoute.builder()
                .setNetwork(prefix)
                .setNextHop(NextHopIp.of(staticRoute.getForwardingRouterAddress()))
                .setAdministrativeCost(
                    firstNonNull(staticRoute.getDistance(), DEFAULT_STATIC_ROUTE_DISTANCE))
                .setRecursive(false)
                .build());
  }

  private void markStructures() {
    A10StructureType.CONCRETE_STRUCTURES.forEach(this::markConcreteStructure);
    A10StructureType.ABSTRACT_STRUCTURES.asMap().forEach(this::markAbstractStructureAllUsages);
  }

  /**
   * Convert specified VS {@link Interface} in provided {@link Vrf} to a VI model {@link
   * org.batfish.datamodel.Interface} attached to the VI {@link Configuration}.
   */
  private void convertInterface(Interface iface, Vrf vrf) {
    String name = getInterfaceName(iface);
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setActive(getInterfaceEnabledEffective(iface))
            .setMtu(getInterfaceMtuEffective(iface))
            .setType(getInterfaceType(iface))
            .setName(name)
            .setVrf(vrf)
            .setOwner(_c);
    // A10 interface `name` is more like a description than an actual name
    newIface.setDescription(iface.getName());
    newIface.setHumanName(getInterfaceHumanName(iface));
    newIface.setDeclaredNames(ImmutableList.of(name));

    if (iface.getIpAddress() != null) {
      ConcreteInterfaceAddress address = iface.getIpAddress();
      ConnectedRouteMetadata meta =
          ConnectedRouteMetadata.builder().setGenerateLocalRoute(false).build();
      newIface.setAddress(address);
      newIface.setAddressMetadata(ImmutableMap.of(address, meta));
    }

    // VLANs
    boolean vlanIsConfigured = hasVlanSettings(iface);
    if (vlanIsConfigured) {
      setVlanSettings(iface, newIface);
    }

    // Aggregates and members - must happen after initial VLAN settings are set
    if (iface instanceof TrunkInterface) {
      TrunkInterface trunkIface = (TrunkInterface) iface;
      String trunkName = getInterfaceName(iface);
      ImmutableSet<String> memberNames =
          trunkIface.getMembers().stream()
              .map(A10Configuration::getInterfaceName)
              .filter(
                  memberName -> {
                    boolean ifaceExists = _ifaceNametoIface.containsKey(memberName);
                    if (!ifaceExists) {
                      // Cannot tell if this missing member would invalidate other members or not
                      // So, optimistically leave other members
                      _w.redFlag(
                          String.format(
                              "Trunk member %s does not exist, cannot add to %s",
                              memberName, trunkName));
                    }
                    return ifaceExists;
                  })
              .collect(ImmutableSet.toImmutableSet());
      if (memberNames.isEmpty()) {
        _w.redFlag(
            String.format(
                "%s does not contain any member interfaces",
                getInterfaceName(Interface.Type.TRUNK, trunkIface.getNumber())));
      } else {
        newIface.setChannelGroupMembers(memberNames);
        newIface.setDependencies(
            memberNames.stream()
                .map(
                    member ->
                        new org.batfish.datamodel.Interface.Dependency(
                            member, org.batfish.datamodel.Interface.DependencyType.AGGREGATE))
                .collect(ImmutableSet.toImmutableSet()));

        // If this trunk doesn't have VLAN configured directly (e.g. ACOS v2), inherit it
        if (!vlanIsConfigured) {
          if (vlanSettingsDifferent(memberNames)) {
            _w.redFlag(
                String.format(
                    "VLAN settings for members of %s are different, ignoring their VLAN settings",
                    trunkName));
          } else {
            // All members have the same VLAN settings, so just use the first
            String firstMemberName = memberNames.iterator().next();
            setVlanSettings(_ifaceNametoIface.get(firstMemberName), newIface);
          }
        } else {
          if (memberNames.stream()
              .anyMatch(memberName -> hasVlanSettings(_ifaceNametoIface.get(memberName)))) {
            _w.redFlag(
                String.format(
                    "Cannot configure VLAN settings on %s as well as its members. Member VLAN"
                        + " settings will be ignored.",
                    trunkName));
          }
        }
      }
    }
    if (iface.getType() == Interface.Type.ETHERNET) {
      InterfaceReference ifaceRef = new InterfaceReference(iface.getType(), iface.getNumber());
      _interfacesTrunk.values().stream()
          .filter(t -> t.getMembers().contains(ifaceRef))
          .findFirst()
          .ifPresent(
              t -> {
                newIface.setChannelGroup(getInterfaceName(Interface.Type.TRUNK, t.getNumber()));
                // TODO determine if switchport settings need to be propagated to member interfaces
              });
    }

    newIface.build();
  }

  /**
   * Check if any VLAN settings for {@link Interface}s (specified by name) are different.
   *
   * <p>All specified interface names must correspond to existent interfaces, in the {@code
   * _ifaceNameToIface} map.
   */
  private boolean vlanSettingsDifferent(Collection<String> names) {
    org.batfish.datamodel.Interface.Builder baseIface =
        org.batfish.datamodel.Interface.builder().setName("");
    Stream<org.batfish.datamodel.Interface> distinctVlanSettings =
        names.stream()
            .map(
                name -> {
                  setVlanSettings(_ifaceNametoIface.get(name), baseIface);
                  return baseIface.build();
                })
            .distinct();
    return distinctVlanSettings.count() > 1;
  }

  /**
   * Set VLAN settings of the specified VI {@link org.batfish.datamodel.Interface.Builder} based on
   * the specified {@link Interface}.
   */
  private void setVlanSettings(Interface iface, org.batfish.datamodel.Interface.Builder viIface) {
    viIface.setSwitchportMode(SwitchportMode.NONE);
    List<Vlan> taggedVlans = getTaggedVlans(iface);
    Optional<Vlan> untaggedVlan = getUntaggedVlan(iface);
    if (untaggedVlan.isPresent()) {
      viIface.setSwitchportMode(SwitchportMode.TRUNK);
      viIface.setSwitchport(true);
      viIface.setNativeVlan(untaggedVlan.get().getNumber());
    }
    if (!taggedVlans.isEmpty()) {
      viIface.setSwitchportMode(SwitchportMode.TRUNK);
      viIface.setSwitchport(true);
      viIface.setAllowedVlans(
          IntegerSpace.unionOfSubRanges(
              taggedVlans.stream()
                  .map(v -> new SubRange(v.getNumber()))
                  .collect(ImmutableList.toImmutableList())));
    }
    if (iface.getType() == Interface.Type.VE) {
      viIface.setVlan(iface.getNumber());
    }
  }

  /** Returns a boolean indicating if VLAN settings exist for the supplied {@link Interface}. */
  private boolean hasVlanSettings(Interface iface) {
    List<Vlan> taggedVlans = getTaggedVlans(iface);
    Optional<Vlan> untaggedVlan = getUntaggedVlan(iface);
    return untaggedVlan.isPresent()
        || !taggedVlans.isEmpty()
        || iface.getType() == Interface.Type.VE;
  }

  /** Get the untagged VLAN for the specified interface, if one exists. */
  public Optional<Vlan> getUntaggedVlan(Interface iface) {
    InterfaceReference ref = new InterfaceReference(iface.getType(), iface.getNumber());
    return _vlans.values().stream().filter(v -> v.getUntagged().contains(ref)).findFirst();
  }

  /** Returns all VLANs associated with the specified tagged interface. */
  private List<Vlan> getTaggedVlans(Interface iface) {
    InterfaceReference ref = new InterfaceReference(iface.getType(), iface.getNumber());
    return _vlans.values().stream()
        .filter(v -> v.getTagged().contains(ref))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns a boolean indicating if the specified {@link ServerPort.Type} and {@link
   * VirtualServerPort.Type} are compatible.
   */
  public static boolean arePortTypesCompatible(
      ServerPort.Type realType, VirtualServerPort.Type virtualType) {
    if (realType == ServerPort.Type.UDP) {
      return VIRTUAL_UDP_PORT_TYPES.contains(virtualType);
    }
    assert realType == ServerPort.Type.TCP;
    return VIRTUAL_TCP_PORT_TYPES.contains(virtualType);
  }

  /**
   * Finalize configuration after it is finished being built. Does things like making structures
   * immutable.
   *
   * <p>This should only be called once, at the end of parsing and extraction.
   */
  public void finalizeStructures() {
    _interfacesEthernet = ImmutableMap.copyOf(_interfacesEthernet);
    _interfacesLoopback = ImmutableMap.copyOf(_interfacesLoopback);
    _interfacesVe = ImmutableMap.copyOf(_interfacesVe);
    _interfacesTrunk = ImmutableMap.copyOf(_interfacesTrunk);
    _natPools = ImmutableMap.copyOf(_natPools);
    _servers = ImmutableMap.copyOf(_servers);
    _serviceGroups = ImmutableMap.copyOf(_serviceGroups);
    _staticRoutes = ImmutableMap.copyOf(_staticRoutes);
    _virtualServers = ImmutableMap.copyOf(_virtualServers);
    _vlans = ImmutableMap.copyOf(_vlans);
  }

  /** Map of interface names to interface. Used for converting aggregate interfaces. */
  private transient Map<String, Interface> _ifaceNametoIface;

  @Nullable private BgpProcess _bgpProcess;
  private Configuration _c;
  private String _hostname;
  private Map<Integer, Interface> _interfacesEthernet;
  private Map<Integer, Interface> _interfacesLoopback;
  private Map<Integer, TrunkInterface> _interfacesTrunk;
  private Map<Integer, Interface> _interfacesVe;
  private Map<String, NatPool> _natPools;
  private Map<String, Server> _servers;
  private Map<String, ServiceGroup> _serviceGroups;
  private Map<Prefix, StaticRouteManager> _staticRoutes;
  private Map<String, VirtualServer> _virtualServers;
  private @Nullable VrrpA _vrrpA;
  private Map<Integer, Vlan> _vlans;
  private ConfigurationFormat _vendor;
}
