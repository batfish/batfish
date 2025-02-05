package org.batfish.representation.cumulus_nclu;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.representation.cumulus_nclu.BgpProcess.BGP_UNNUMBERED_IP;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.DEFAULT_PORT_BANDWIDTH;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.SPEED_CONVERSION_FACTOR;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertBgpProcess;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertClags;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertDnsServers;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertIpAsPathAccessLists;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertIpCommunityLists;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertIpPrefixLists;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertOspfProcess;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertRouteMaps;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.convertVxlans;
import static org.batfish.representation.cumulus_nclu.CumulusConversions.isUsedForBgpUnnumbered;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.vendor_family.cumulus.CumulusFamily;
import org.batfish.vendor.VendorConfiguration;

/** A {@link VendorConfiguration} for the Cumulus NCLU configuration language. */
public class CumulusNcluConfiguration extends VendorConfiguration {

  public static final String LOOPBACK_INTERFACE_NAME = "lo";

  public static final Pattern PHYSICAL_INTERFACE_PATTERN =
      Pattern.compile("^(swp[0-9]+(s[0-9])?)|(eth[0-9]+)$");

  public static final Pattern VLAN_INTERFACE_PATTERN = Pattern.compile("^vlan([0-9]+)$");

  public static final Pattern VXLAN_INTERFACE_PATTERN = Pattern.compile("^vxlan([0-9]+)$");

  public static final Pattern SUBINTERFACE_PATTERN = Pattern.compile("^(.*)\\.([0-9]+)$");
  @VisibleForTesting public static final String CUMULUS_CLAG_DOMAIN_ID = "~CUMULUS_CLAG_DOMAIN~";

  private @Nullable BgpProcess _bgpProcess;
  private @Nonnull Map<String, Bond> _bonds;
  private @Nonnull Bridge _bridge;
  private transient Configuration _c;
  private @Nullable String _hostname;
  private @Nullable OspfProcess _ospfProcess;
  private @Nonnull Map<String, Interface> _interfaces;
  private final @Nonnull List<Ip> _ipv4Nameservers;
  private final @Nonnull List<Ip6> _ipv6Nameservers;
  private final @Nonnull Loopback _loopback;
  private final @Nonnull Map<String, RouteMap> _routeMaps;
  private final @Nonnull Set<StaticRoute> _staticRoutes;
  private @Nonnull Map<String, Vlan> _vlans;
  private @Nonnull Map<String, Vrf> _vrfs;
  private @Nonnull Map<String, Vxlan> _vxlans;
  private final @Nonnull Map<String, IpAsPathAccessList> _ipAsPathAccessLists;
  private final @Nonnull Map<String, IpPrefixList> _ipPrefixLists;
  private final @Nonnull Map<String, IpCommunityList> _ipCommunityLists;

  public static final @Nonnull LinkLocalAddress LINK_LOCAL_ADDRESS =
      LinkLocalAddress.of(BGP_UNNUMBERED_IP);

  public CumulusNcluConfiguration() {
    _bonds = new HashMap<>();
    _bridge = new Bridge();
    _interfaces = new HashMap<>();
    _ipAsPathAccessLists = new HashMap<>();
    _ipPrefixLists = new HashMap<>();
    _ipCommunityLists = new HashMap<>();
    _ipv4Nameservers = new LinkedList<>();
    _ipv6Nameservers = new LinkedList<>();
    _loopback = new Loopback();
    _routeMaps = new HashMap<>();
    _staticRoutes = new HashSet<>();
    _vlans = new HashMap<>();
    _vrfs = new HashMap<>();
    _vxlans = new HashMap<>();
  }

  private void applyBridgeSettings(
      InterfaceBridgeSettings bridge, org.batfish.datamodel.Interface newIface) {
    String name = newIface.getName();
    Integer access = bridge.getAccess();
    Integer ifacePvid = bridge.getPvid();
    IntegerSpace ifaceVids = bridge.getVids();
    if (!_bridge.getPorts().contains(name)) {
      if (access != null || ifacePvid != null || !ifaceVids.isEmpty()) {
        _w.redFlagf(
            "No support for VLAN switching options on non-'bridge bridge' port: '%s'", name);
      }
      return;
    }
    newIface.setSwitchport(true);
    if (access != null) {
      // access
      newIface.setSwitchportMode(SwitchportMode.ACCESS);
      newIface.setAccessVlan(access);
      return;
    }
    // trunk
    newIface.setSwitchportMode(SwitchportMode.TRUNK);
    int nativeVlan = firstNonNull(ifacePvid, _bridge.getPvid());
    newIface.setNativeVlan(nativeVlan);
    newIface.setAllowedVlans(
        (!ifaceVids.isEmpty() ? ifaceVids : _bridge.getVids()).union(IntegerSpace.of(nativeVlan)));
  }

  private void applyCommonInterfaceSettings(
      Interface iface, org.batfish.datamodel.Interface newIface) {
    if (!iface.getIpAddresses().isEmpty()) {
      newIface.setAddress(iface.getIpAddresses().get(0));
    }
    newIface.setAllAddresses(iface.getIpAddresses());
    if (iface.getIpAddresses().isEmpty() && isUsedForBgpUnnumbered(iface.getName(), _bgpProcess)) {
      newIface.setAddress(LINK_LOCAL_ADDRESS);
      newIface.setAllAddresses(ImmutableSet.of(LINK_LOCAL_ADDRESS));
    }
  }

  /**
   * Create bidirectional associate between {@code vrf} and interface-like entities with vrf
   * assigned to {@code vrfName}.
   */
  private void assignInterfacesToVrf(
      org.batfish.datamodel.Vrf vrf, @Nonnull String assignedVrfName) {
    Stream<String> matchingBondNames =
        _bonds.entrySet().stream()
            .filter(bondEntry -> Objects.equals(bondEntry.getValue().getVrf(), assignedVrfName))
            .map(Entry::getKey);
    Stream<String> matchingInterfaceNames =
        _interfaces.entrySet().stream()
            .filter(
                interfaceEntry ->
                    Objects.equals(interfaceEntry.getValue().getVrf(), assignedVrfName))
            .map(Entry::getKey);
    Stream<String> matchingVlanNames =
        _vlans.entrySet().stream()
            .filter(
                interfaceEntry ->
                    Objects.equals(interfaceEntry.getValue().getVrf(), assignedVrfName))
            .map(Entry::getKey);
    Stream<String> matchingVrfLoopbackNames = Stream.of(assignedVrfName);
    Streams.concat(
            matchingBondNames, matchingInterfaceNames, matchingVlanNames, matchingVrfLoopbackNames)
        .map(_c.getAllInterfaces()::get)
        .forEach(
            iface -> {
              iface.setVrf(vrf);
            });
  }

  private void convertBondInterfaces() {
    _bonds.forEach((name, bond) -> _c.getAllInterfaces().put(name, toInterface(bond)));
  }

  private void convertDefaultVrf() {
    org.batfish.datamodel.Vrf defaultVrf = new org.batfish.datamodel.Vrf(DEFAULT_VRF_NAME);
    defaultVrf.setStaticRoutes(
        _staticRoutes.stream()
            .map(StaticRoute::convert)
            .collect(ImmutableSortedSet.toImmutableSortedSet(naturalOrder())));
    // Add all unassigned interfaces to default VRF
    _c.getAllInterfaces()
        .forEach(
            (ifaceName, iface) -> {
              if (iface.getVrf() == null) {
                iface.setVrf(defaultVrf);
              }
            });
    _c.getVrfs().put(DEFAULT_VRF_NAME, defaultVrf);
  }

  @VisibleForTesting
  static void populateLoInInterfacesToLoopback(Interface iface, Loopback loopback) {
    checkArgument(
        iface.getType() == CumulusInterfaceType.LOOPBACK,
        String.format(
            "cannot populate interface with type %s to loopback", iface.getType().name()));
    loopback.getAddresses().addAll(iface.getIpAddresses());
  }

  private void convertLoopback() {
    Optional.ofNullable(_interfaces.get(LOOPBACK_INTERFACE_NAME))
        .ifPresent(iface -> populateLoInInterfacesToLoopback(iface, _loopback));

    org.batfish.datamodel.Interface newIface = createVIInterfaceForLo();

    if (!_loopback.getAddresses().isEmpty()) {
      newIface.setAddress(_loopback.getAddresses().get(0));
    }
    Builder<ConcreteInterfaceAddress> allAddresses = ImmutableSet.builder();
    allAddresses.addAll(_loopback.getAddresses());
    if (_loopback.getClagVxlanAnycastIp() != null) {
      // Just assume CLAG is correctly configured and comes up
      allAddresses.add(
          ConcreteInterfaceAddress.create(
              _loopback.getClagVxlanAnycastIp(), Prefix.MAX_PREFIX_LENGTH));
    }
    newIface.setAllAddresses(allAddresses.build());
    _c.getAllInterfaces().put(LOOPBACK_INTERFACE_NAME, newIface);
  }

  @VisibleForTesting
  org.batfish.datamodel.Interface createVIInterfaceForLo() {
    return org.batfish.datamodel.Interface.builder()
        .setName(LOOPBACK_INTERFACE_NAME)
        .setOwner(_c)
        .setType(InterfaceType.LOOPBACK)
        .setBandwidth(
            Optional.ofNullable(_loopback.getBandwidth()).orElse(DEFAULT_LOOPBACK_BANDWIDTH))
        .build();
  }

  private void convertPhysicalInterfaces() {
    // create physical interfaces
    _interfaces.values().stream()
        .filter(i -> i.getType() == CumulusInterfaceType.PHYSICAL)
        .forEach(
            physicalInterface ->
                _c.getAllInterfaces()
                    .put(physicalInterface.getName(), toInterface(physicalInterface)));
    // create VI interface for every superinterface of a physical subinterface not explicitly
    // configured
    _interfaces.values().stream()
        .filter(i -> i.getType() == CumulusInterfaceType.PHYSICAL_SUBINTERFACE)
        .map(Interface::getSuperInterfaceName)
        .filter(Predicates.not(_interfaces::containsKey))
        .forEach(
            superInterfaceName ->
                _c.getAllInterfaces()
                    .put(
                        superInterfaceName,
                        toInterface(
                            new Interface(
                                superInterfaceName, CumulusInterfaceType.PHYSICAL, null, null))));
  }

  private void convertSubinterfaces() {
    _interfaces.values().stream()
        .filter(
            i ->
                i.getType() == CumulusInterfaceType.BOND_SUBINTERFACE
                    || i.getType() == CumulusInterfaceType.PHYSICAL_SUBINTERFACE)
        .map(i -> toInterface(i, i.getSuperInterfaceName()))
        .forEach(newIface -> _c.getAllInterfaces().put(newIface.getName(), newIface));
  }

  private void convertVlanInterfaces() {
    _vlans.forEach((name, vlan) -> _c.getAllInterfaces().put(name, toInterface(vlan)));
  }

  private org.batfish.datamodel.Interface convertVrfLoopbackInterface(Vrf vrf) {
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(vrf.getName())
            .setOwner(_c)
            .setType(InterfaceType.LOOPBACK)
            .build();
    if (!vrf.getAddresses().isEmpty()) {
      newIface.setAddress(vrf.getAddresses().get(0));
    }
    newIface.setAllAddresses(vrf.getAddresses());
    return newIface;
  }

  private void convertVrfLoopbackInterfaces() {
    _vrfs.forEach((name, vrf) -> _c.getAllInterfaces().put(name, convertVrfLoopbackInterface(vrf)));
  }

  private void convertVrfs() {
    _vrfs.forEach(this::initVrf);
  }

  public @Nullable String getVrfForVlan(@Nullable Integer bridgeAccessVlan) {
    if (bridgeAccessVlan == null) {
      return null;
    }
    return _vlans.values().stream()
        .filter(v -> Objects.equals(v.getVlanId(), bridgeAccessVlan))
        .findFirst()
        .map(Vlan::getVrf)
        .orElse(null);
  }

  public @Nullable BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public @Nonnull Map<String, Bond> getBonds() {
    return _bonds;
  }

  public @Nonnull Bridge getBridge() {
    return _bridge;
  }

  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  public @Nonnull Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull List<Ip> getIpv4Nameservers() {
    return _ipv4Nameservers;
  }

  public @Nonnull List<Ip6> getIpv6Nameservers() {
    return _ipv6Nameservers;
  }

  public @Nonnull Loopback getLoopback() {
    return _loopback;
  }

  public @Nullable OspfProcess getOspfProcess() {
    return _ospfProcess;
  }

  public @Nonnull Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  public @Nonnull Set<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public @Nonnull Map<String, Vlan> getVlans() {
    return _vlans;
  }

  public @Nonnull Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  public @Nonnull Map<String, Vxlan> getVxlans() {
    return _vxlans;
  }

  public @Nonnull Map<String, IpAsPathAccessList> getIpAsPathAccessLists() {
    return _ipAsPathAccessLists;
  }

  public @Nonnull Map<String, IpPrefixList> getIpPrefixLists() {
    return _ipPrefixLists;
  }

  public @Nonnull Map<String, IpCommunityList> getIpCommunityLists() {
    return _ipCommunityLists;
  }

  private void initVendorFamily() {
    _c.getVendorFamily()
        .setCumulus(
            CumulusFamily.builder()
                .setBridge(_bridge.toDataModel())
                .setInterfaceClagSettings(
                    _interfaces.entrySet().stream()
                        .filter(ifaceEntry -> ifaceEntry.getValue().getClag() != null)
                        .collect(
                            ImmutableSortedMap.toImmutableSortedMap(
                                Comparator.naturalOrder(),
                                Entry::getKey,
                                ifaceEntry -> ifaceEntry.getValue().getClag().toDataModel())))
                .build());
  }

  private void initVrf(String name, Vrf vrf) {
    org.batfish.datamodel.Vrf newVrf = new org.batfish.datamodel.Vrf(name);
    initVrfStaticRoutes(vrf, newVrf);
    assignInterfacesToVrf(newVrf, name);
    _c.getVrfs().put(name, newVrf);
  }

  @VisibleForTesting
  void initVrfStaticRoutes(Vrf oldVrf, org.batfish.datamodel.Vrf newVrf) {
    newVrf.setStaticRoutes(
        Streams.concat(
                oldVrf.getStaticRoutes().stream(),
                _interfaces.values().stream()
                    .filter(iface -> Objects.equals(iface.getVrf(), oldVrf.getName()))
                    .filter(iface -> !iface.isDisabled())
                    .flatMap(iface -> iface.getPostUpIpRoutes().stream()))
            .map(StaticRoute::convert)
            .collect(ImmutableSortedSet.toImmutableSortedSet(naturalOrder())));
  }

  private void markStructures() {
    markAbstractStructure(
        CumulusStructureType.ABSTRACT_INTERFACE,
        CumulusStructureUsage.BGP_NEIGHBOR_INTERFACE,
        ImmutableSet.of(
            CumulusStructureType.BOND,
            CumulusStructureType.INTERFACE,
            CumulusStructureType.LOOPBACK,
            CumulusStructureType.VLAN,
            CumulusStructureType.VRF));
    markAbstractStructure(
        CumulusStructureType.ABSTRACT_INTERFACE,
        CumulusStructureUsage.BRIDGE_PORT,
        ImmutableSet.of(
            CumulusStructureType.BOND,
            CumulusStructureType.INTERFACE,
            CumulusStructureType.LOOPBACK,
            CumulusStructureType.VLAN,
            CumulusStructureType.VRF,
            CumulusStructureType.VXLAN));
    markAbstractStructure(
        CumulusStructureType.ABSTRACT_INTERFACE,
        CumulusStructureUsage.ROUTE_MAP_MATCH_INTERFACE,
        ImmutableSet.of(
            CumulusStructureType.BOND,
            CumulusStructureType.INTERFACE,
            CumulusStructureType.LOOPBACK,
            CumulusStructureType.VLAN,
            CumulusStructureType.VRF));
    markConcreteStructure(CumulusStructureType.BOND);
    markConcreteStructure(CumulusStructureType.INTERFACE);
    markConcreteStructure(CumulusStructureType.IP_AS_PATH_ACCESS_LIST);
    markConcreteStructure(CumulusStructureType.IP_COMMUNITY_LIST);
    markConcreteStructure(CumulusStructureType.IP_PREFIX_LIST);
    markConcreteStructure(CumulusStructureType.LOOPBACK);
    markConcreteStructure(CumulusStructureType.ROUTE_MAP);
    markConcreteStructure(CumulusStructureType.VLAN);
    markConcreteStructure(CumulusStructureType.VRF);
    markConcreteStructure(CumulusStructureType.VXLAN);
  }

  public void setBgpProcess(@Nullable BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  /** For testing conversion methods. */
  @VisibleForTesting
  void setConfiguration(Configuration c) {
    _c = c;
  }

  @Override
  public void setHostname(@Nullable String hostname) {
    _hostname = hostname == null ? null : hostname.toLowerCase();
  }

  public void setOspfProcess(@Nullable OspfProcess ospfProcess) {
    _ospfProcess = ospfProcess;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  public void setBonds(@Nonnull Map<String, Bond> bonds) {
    _bonds = ImmutableMap.copyOf(bonds);
  }

  public void setBridge(@Nonnull Bridge bridge) {
    _bridge = bridge;
  }

  public void setInterfaces(@Nonnull Map<String, Interface> interfaces) {
    _interfaces = ImmutableMap.copyOf(interfaces);
  }

  public void setVlans(@Nonnull Map<String, Vlan> vlans) {
    _vlans = ImmutableMap.copyOf(vlans);
  }

  public void setVrfs(@Nonnull Map<String, Vrf> vrfs) {
    _vrfs = ImmutableMap.copyOf(vrfs);
  }

  public void setVxlans(Map<String, Vxlan> vxlans) {
    _vxlans = ImmutableMap.copyOf(vxlans);
  }

  private @Nonnull org.batfish.datamodel.Interface toInterface(Bond bond) {
    String name = bond.getName();
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(name)
            .setOwner(_c)
            .setType(InterfaceType.AGGREGATED)
            .build();
    Set<String> validSlaves =
        bond.getSlaves().stream()
            .filter(slave -> isValidSlave(name, slave))
            .collect(ImmutableSet.toImmutableSet());
    validSlaves.forEach(slave -> _c.getAllInterfaces().get(slave).setChannelGroup(name));
    newIface.setChannelGroupMembers(validSlaves);
    newIface.setDependencies(
        validSlaves.stream()
            .map(slave -> new Dependency(slave, DependencyType.AGGREGATE))
            .collect(ImmutableSet.toImmutableSet()));

    applyBridgeSettings(bond.getBridge(), newIface);

    if (!bond.getIpAddresses().isEmpty()) {
      newIface.setAddress(bond.getIpAddresses().get(0));
    }
    newIface.setAllAddresses(bond.getIpAddresses());

    newIface.setMlagId(bond.getClagId());

    return newIface;
  }

  private boolean isValidSlave(String bondName, String slaveName) {
    Interface slave = _interfaces.get(slaveName);
    if (!slave.getIpAddresses().isEmpty()) {
      _w.redFlagf(
          "Refusing to add slave %s to bond %s because it has an L3 address", slaveName, bondName);
      return false;
    }
    if (isUsedForBgpUnnumbered(slave.getName(), _bgpProcess)) {
      _w.redFlagf(
          "Refusing to add slave %s to bond %s because it is used for BGP unnumbered",
          slaveName, bondName);
      return false;
    }
    for (Interface i : _interfaces.values()) {
      if (slaveName.equals(i.getSuperInterfaceName())) {
        _w.redFlagf(
            "Refusing to add slave %s to bond %s because it has a subinterface %s",
            slaveName, bondName, i.getName());
        return false;
      }
    }
    return true;
  }

  @VisibleForTesting
  @Nonnull
  org.batfish.datamodel.Interface toInterface(Interface iface) {
    String name = iface.getName();
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(name)
            .setOwner(_c)
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(!iface.isDisabled())
            .build();
    applyCommonInterfaceSettings(iface, newIface);

    applyBridgeSettings(iface.getBridge(), newIface);

    newIface.setDescription(iface.getAlias());
    if (iface.getSpeed() != null) {
      double speed = iface.getSpeed() * SPEED_CONVERSION_FACTOR;
      newIface.setSpeed(speed);
      newIface.setBandwidth(speed);
    } else {
      newIface.setBandwidth(DEFAULT_PORT_BANDWIDTH);
    }

    return newIface;
  }

  @VisibleForTesting
  @Nonnull
  org.batfish.datamodel.Interface toInterface(Interface iface, String superInterfaceName) {
    String name = iface.getName();
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(name)
            .setOwner(_c)
            .setType(
                iface.getType() == CumulusInterfaceType.BOND_SUBINTERFACE
                    ? InterfaceType.AGGREGATE_CHILD
                    : InterfaceType.LOGICAL)
            .setAdminUp(!iface.isDisabled())
            .build();
    newIface.setDependencies(
        ImmutableSet.of(new Dependency(superInterfaceName, DependencyType.BIND)));
    newIface.setEncapsulationVlan(iface.getEncapsulationVlan());
    applyCommonInterfaceSettings(iface, newIface);
    return newIface;
  }

  private org.batfish.datamodel.Interface toInterface(Vlan vlan) {
    Integer vlanId = vlan.getVlanId();
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(vlan.getName())
            .setOwner(_c)
            .setType(InterfaceType.VLAN)
            .build();
    newIface.setVlan(vlanId);
    if (vlanId == null) {
      // TODO: perhaps there should be a default based on the name?
      _w.redFlagf("Deactivating vlan %s which has no vlan-id set", vlan.getName());
      newIface.deactivate(InactiveReason.INCOMPLETE);
    }

    // Interface addreses
    if (!vlan.getAddresses().isEmpty()) {
      newIface.setAddress(vlan.getAddresses().get(0));
    }
    ImmutableSet.Builder<InterfaceAddress> allAddresses = ImmutableSet.builder();
    allAddresses.addAll(vlan.getAddresses());
    vlan.getAddressVirtuals().values().forEach(allAddresses::addAll);
    newIface.setAllAddresses(allAddresses.build());
    newIface.setDescription(vlan.getAlias());

    return newIface;
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    _c = new Configuration(getHostname(), ConfigurationFormat.CUMULUS_NCLU);
    _c.setDeviceModel(DeviceModel.CUMULUS_UNSPECIFIED);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    _c.setExportBgpFromBgpRib(true);

    convertPhysicalInterfaces();
    convertSubinterfaces();
    convertVlanInterfaces();
    convertLoopback();
    convertBondInterfaces();
    convertVrfLoopbackInterfaces();
    convertVrfs();
    convertDefaultVrf();
    convertIpAsPathAccessLists(_c, _ipAsPathAccessLists);
    convertIpPrefixLists(_c, _ipPrefixLists);
    convertIpCommunityLists(_c, _ipCommunityLists);
    convertRouteMaps(_c, this, _routeMaps, _w);
    convertDnsServers(_c, _ipv4Nameservers);
    convertClags(_c, this, _w);

    // Compute explicit VNI -> VRF mappings for L3 VNIs:
    Map<Integer, String> vniToVrf =
        _vrfs.values().stream()
            .filter(vrf -> vrf.getVni() != null)
            .collect(ImmutableMap.toImmutableMap(Vrf::getVni, Vrf::getName));

    convertVxlans(
        _c,
        this,
        vniToVrf,
        _loopback.getClagVxlanAnycastIp(),
        _loopback.getVxlanLocalTunnelip(),
        _w);
    convertOspfProcess(_c, this, _w);
    convertBgpProcess(_c, this, _w);

    initVendorFamily();

    markStructures();

    warnDuplicateClagIds();

    return _c;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  private void warnDuplicateClagIds() {
    Map<Integer, List<Bond>> clagBondsById = new HashMap<>();
    _bonds.values().stream()
        .filter(bond -> bond.getClagId() != null)
        .forEach(
            clagBond ->
                clagBondsById
                    .computeIfAbsent(clagBond.getClagId(), id -> new LinkedList<>())
                    .add(clagBond));
    clagBondsById.forEach(
        (id, clagBonds) -> {
          if (clagBonds.size() > 1) {
            _w.redFlagf(
                "clag-id %d is erroneously configured on more than one bond: %s",
                id, clagBonds.stream().map(Bond::getName).collect(ImmutableList.toImmutableList()));
          }
        });
  }

  public @Nullable Vrf getVrf(String vrfName) {
    return _vrfs.get(vrfName);
  }

  public @Nonnull Map<String, InterfaceClagSettings> getClagSettings() {
    return _interfaces.values().stream()
        .filter(iface -> iface.getClag() != null)
        .collect(ImmutableMap.toImmutableMap(Interface::getName, Interface::getClag));
  }

  public Optional<OspfInterface> getOspfInterface(String ifaceName) {
    if (!_interfaces.containsKey(ifaceName)) {
      return Optional.empty();
    }
    return Optional.ofNullable(_interfaces.get(ifaceName).getOspf());
  }
}
