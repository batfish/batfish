package org.batfish.representation.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_PORT_BANDWIDTH;
import static org.batfish.representation.cumulus.CumulusConversions.SPEED_CONVERSION_FACTOR;
import static org.batfish.representation.cumulus.CumulusConversions.convertBgpProcess;
import static org.batfish.representation.cumulus.CumulusConversions.convertClags;
import static org.batfish.representation.cumulus.CumulusConversions.convertDnsServers;
import static org.batfish.representation.cumulus.CumulusConversions.convertIpAsPathAccessLists;
import static org.batfish.representation.cumulus.CumulusConversions.convertIpCommunityLists;
import static org.batfish.representation.cumulus.CumulusConversions.convertIpPrefixLists;
import static org.batfish.representation.cumulus.CumulusConversions.convertOspfProcess;
import static org.batfish.representation.cumulus.CumulusConversions.convertRouteMaps;
import static org.batfish.representation.cumulus.CumulusConversions.isUsedForBgpUnnumbered;
import static org.batfish.representation.cumulus.CumulusNcluConfiguration.LINK_LOCAL_ADDRESS;
import static org.batfish.representation.cumulus.InterfaceConverter.BRIDGE_NAME;
import static org.batfish.representation.cumulus.InterfaceConverter.DEFAULT_BRIDGE_PORTS;
import static org.batfish.representation.cumulus.InterfaceConverter.DEFAULT_BRIDGE_PVID;
import static org.batfish.representation.cumulus.InterfaceConverter.getSuperInterfaceName;
import static org.batfish.representation.cumulus.InterfacesInterface.isPhysicalInterfaceType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.vendor_family.cumulus.CumulusFamily;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.datamodel.vxlan.Vni;
import org.batfish.representation.cumulus.CumulusPortsConfiguration.PortSettings;
import org.batfish.vendor.VendorConfiguration;

/** A {@link VendorConfiguration} for the Cumulus NCLU configuration language. */
@ParametersAreNonnullByDefault
public class CumulusConcatenatedConfiguration extends VendorConfiguration
    implements CumulusNodeConfiguration {

  private String _hostname;

  @Nonnull private final CumulusInterfacesConfiguration _interfacesConfiguration;

  @Nonnull private final CumulusFrrConfiguration _frrConfiguration;

  @Nonnull private final CumulusPortsConfiguration _portsConfiguration;

  public CumulusConcatenatedConfiguration() {
    this(
        new CumulusInterfacesConfiguration(),
        new CumulusFrrConfiguration(),
        new CumulusPortsConfiguration());
  }

  private CumulusConcatenatedConfiguration(
      CumulusInterfacesConfiguration interfaces,
      CumulusFrrConfiguration frr,
      CumulusPortsConfiguration ports) {
    _interfacesConfiguration = interfaces;
    _frrConfiguration = frr;
    _portsConfiguration = ports;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname == null ? null : hostname.toLowerCase();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  @Nonnull
  @VisibleForTesting
  Configuration toVendorIndependentConfiguration() {
    Configuration c = new Configuration(getHostname(), ConfigurationFormat.CUMULUS_CONCATENATED);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);

    // create default VRF
    getOrCreateVrf(c, DEFAULT_VRF_NAME);

    initializeAllInterfaces(c);
    populateInterfacesInterfaceProperties(c);
    populatePortsInterfaceProperties(c);
    populateFrrInterfaceProperties(c);

    // for interfaces that didn't get an address via either interfaces or FRR, give them a link
    // local address if they are being used for BGP unnumbered
    c.getAllInterfaces()
        .forEach(
            (iname, iface) -> {
              if (iface.getAllAddresses().size() == 0
                  && isUsedForBgpUnnumbered(iface.getName(), _frrConfiguration.getBgpProcess())) {
                iface.setAddress(LINK_LOCAL_ADDRESS);
                iface.setAllAddresses(ImmutableSet.of(LINK_LOCAL_ADDRESS));
              }
            });

    initVrfStaticRoutes(c);

    convertIpAsPathAccessLists(c, _frrConfiguration.getIpAsPathAccessLists());
    convertIpPrefixLists(c, _frrConfiguration.getIpPrefixLists());
    convertIpCommunityLists(c, _frrConfiguration.getIpCommunityLists());
    convertRouteMaps(c, this, _frrConfiguration.getRouteMaps(), _w);
    convertDnsServers(c, _frrConfiguration.getIpv4Nameservers());
    convertClags(c, this, _w);
    convertVxlans(c);
    convertOspfProcess(c, this, _w);
    convertBgpProcess(c, this, _w);

    initVendorFamily(c);
    warnDuplicateClagIds();
    markStructures();

    return c;
  }

  private void populatePortsInterfaceProperties(Configuration c) {
    _portsConfiguration
        .getPortSettings()
        .forEach(
            (ifaceName, portSettings) -> {
              org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(ifaceName);
              if (viIface == null) { // ports file may have undefined interfaces
                return;
              }
              boolean isDisabled = Boolean.FALSE.equals(portSettings.getDisabled());
              viIface.setActive(!isDisabled);

              if (portSettings.getSpeed() != null) {
                double speed = portSettings.getSpeed() * SPEED_CONVERSION_FACTOR;
                viIface.setSpeed(speed);
                viIface.setBandwidth(speed);
              }
            });
  }

  /**
   * Converts {@link Vxlan} into appropriate {@link Vni} for each VRF. Requires VI Vrfs to already
   * be properly initialized
   */
  private void convertVxlans(Configuration c) {

    // Compute explicit VNI -> VRF mappings for L3 VNIs:
    Map<Integer, String> vniToVrf =
        _frrConfiguration.getVrfs().values().stream()
            .filter(vrf -> vrf.getVni() != null)
            .collect(ImmutableMap.toImmutableMap(Vrf::getVni, Vrf::getName));

    // Put all valid VXLAN VNIs into appropriate VRF
    _interfacesConfiguration.getInterfaces().values().stream()
        .filter(InterfaceConverter::isVxlan)
        .forEach(
            vxlan -> {
              if (vxlan.getVxlanId() == null
                  || vxlan.getVxlanLocalTunnelIp() == null
                  || vxlan.getBridgeSettings() == null
                  || vxlan.getBridgeSettings().getAccess() == null) {
                // Not a valid VNI configuration
                return;
              }
              @Nullable String vrfName = vniToVrf.get(vxlan.getVxlanId());
              if (vrfName != null) {
                // This is an L3 VNI.
                Optional.ofNullable(c.getVrfs().get(vrfName))
                    .ifPresent(
                        vrf ->
                            vrf.addLayer3Vni(
                                Layer3Vni.builder()
                                    .setVni(vxlan.getVxlanId())
                                    .setSourceAddress(
                                        firstNonNull(
                                            _interfacesConfiguration
                                                .getLoopback()
                                                .getClagVxlanAnycastIp(),
                                            vxlan.getVxlanLocalTunnelIp()))
                                    .setUdpPort(NamedPort.VXLAN.number())
                                    .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                                    .setSrcVrf(DEFAULT_VRF_NAME)
                                    .build()));
              } else {
                // This is an L2 VNI. Find the VRF by looking up the VLAN
                vrfName = getVrfForVlan(vxlan.getBridgeSettings().getAccess());
                if (vrfName == null) {
                  // This is a workaround until we properly support pure-L2 VNIs (with no IRBs)
                  vrfName = DEFAULT_VRF_NAME;
                }
                Optional.ofNullable(c.getVrfs().get(vrfName))
                    .ifPresent(
                        vrf ->
                            vrf.addLayer2Vni(
                                Layer2Vni.builder()
                                    .setVni(vxlan.getVxlanId())
                                    .setVlan(vxlan.getBridgeSettings().getAccess())
                                    .setSourceAddress(
                                        firstNonNull(
                                            _interfacesConfiguration
                                                .getLoopback()
                                                .getClagVxlanAnycastIp(),
                                            vxlan.getVxlanLocalTunnelIp()))
                                    .setUdpPort(NamedPort.VXLAN.number())
                                    .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
                                    .setSrcVrf(DEFAULT_VRF_NAME)
                                    .build()));
              }
            });
  }

  @Nullable
  private String getVrfForVlan(@Nullable Integer bridgeAccessVlan) {
    if (bridgeAccessVlan == null) {
      return null;
    }
    return _interfacesConfiguration.getInterfaces().values().stream()
        .filter(InterfaceConverter::isVlan)
        .filter(v -> Objects.equals(v.getVlanId(), bridgeAccessVlan))
        .findFirst()
        .map(InterfacesInterface::getVrf)
        .orElse(null);
  }

  @VisibleForTesting
  void initVrfStaticRoutes(Configuration c) {
    // post-up routes from the interfaces file
    _interfacesConfiguration.getInterfaces().values().stream()
        .filter(CumulusConcatenatedConfiguration::isValidVIInterface)
        .forEach(
            iface -> {
              if (!c.getAllInterfaces().get(iface.getName()).getActive()) {
                return;
              }
              org.batfish.datamodel.Vrf vrf = getOrCreateVrf(c, iface.getVrf());
              iface.getPostUpIpRoutes().forEach(sr -> vrf.getStaticRoutes().add(sr.convert()));
            });

    // default vrf static routes from the frr file
    org.batfish.datamodel.Vrf defVrf = c.getVrfs().get(DEFAULT_VRF_NAME);
    _frrConfiguration.getStaticRoutes().forEach(sr -> defVrf.getStaticRoutes().add(sr.convert()));

    // other vrf static routes from the frr file
    _frrConfiguration
        .getVrfs()
        .values()
        .forEach(
            frrVrf -> {
              org.batfish.datamodel.Vrf newVrf = getOrCreateVrf(c, frrVrf.getName());
              frrVrf.getStaticRoutes().forEach(sr -> newVrf.getStaticRoutes().add(sr.convert()));
            });
  }

  private void populateFrrInterfaceProperties(Configuration c) {
    _frrConfiguration
        .getInterfaces()
        .values()
        .forEach(iface -> populateFrrInterfaceProperties(c, iface));
  }

  private static void populateFrrInterfaceProperties(Configuration c, FrrInterface iface) {
    org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(iface.getName());
    checkArgument(
        viIface != null, "VI interface object not found for interface %s", iface.getName());
    if (iface.getAlias() != null) {
      viIface.setDescription(iface.getAlias());
    }
    if (!iface.getIpAddresses().isEmpty()) {
      viIface.setAddress(iface.getIpAddresses().get(0));
      viIface.setAllAddresses(
          ImmutableSet.<InterfaceAddress>builder()
              .addAll(viIface.getAllAddresses())
              .addAll(iface.getIpAddresses())
              .build());
    }
  }

  /** Add interface properties based on what we saw in the interfaces file */
  private void populateInterfacesInterfaceProperties(Configuration c) {
    _interfacesConfiguration.getInterfaces().values().stream()
        .filter(CumulusConcatenatedConfiguration::isValidVIInterface)
        .forEach(iface -> populateInterfaceProperties(c, iface));
    populateLoopbackProperties(c.getAllInterfaces().get(LOOPBACK_INTERFACE_NAME));
  }

  @VisibleForTesting
  static boolean isValidVIInterface(InterfacesInterface iface) {
    return !iface.getName().equals(BRIDGE_NAME) /* not bridge */
        && !InterfaceConverter.isVxlan(iface) /* not vxlans */;
  }

  private void populateInterfaceProperties(Configuration c, InterfacesInterface iface) {

    populateCommonInterfaceProperties(iface, c.getAllInterfaces().get(iface.getName()));

    // bond interfaces
    if (InterfaceConverter.isBond(iface)) {
      populateBondInterfaceProperties(c, iface);
      return;
    }

    @Nullable String superInterfaceName = getSuperInterfaceName(iface.getName());

    // a physical interface
    if (InterfaceConverter.isInterface(iface) && superInterfaceName == null) {
      populatePhysicalInterfaceProperties(c, iface);
      return;
    }

    // a (physical or bond) sub-interface
    if (InterfaceConverter.isInterface(iface) && superInterfaceName != null) {
      populateSubInterfaceProperties(c, iface, superInterfaceName);
      return;
    }

    // vlans
    if (InterfaceConverter.isVlan(iface)) {
      populateVlanInterfaceProperties(c, iface);
      return;
    }

    // vrf loopbacks
    if (InterfaceConverter.isVrf(iface)) {
      populateVrfInterfaceProperties(c, iface);
      return;
    }

    throw new IllegalArgumentException(
        String.format("Unknown type for interface '%s'", iface.getName()));
  }

  @VisibleForTesting
  void populateLoopbackProperties(org.batfish.datamodel.Interface viLoopback) {
    Loopback vsLoopback = _interfacesConfiguration.getLoopback();

    viLoopback.setDescription(vsLoopback.getAlias());

    // addresses in interface configuration, if present, have been transferred via
    // populateCommonProperties. we need to take care of other sources of addresses.

    List<InterfaceAddress> addresses = new LinkedList<>(vsLoopback.getAddresses());

    if (viLoopback.getAddress() == null && !addresses.isEmpty()) {
      viLoopback.setAddress(addresses.get(0));
    }

    if (vsLoopback.getClagVxlanAnycastIp() != null) {
      // Just assume CLAG is correctly configured and comes up
      addresses.add(
          ConcreteInterfaceAddress.create(
              vsLoopback.getClagVxlanAnycastIp(), Prefix.MAX_PREFIX_LENGTH));
    }
    viLoopback.setAllAddresses(
        ImmutableSet.<InterfaceAddress>builder()
            .addAll(viLoopback.getAllAddresses())
            .addAll(addresses)
            .build());

    // set bandwidth
    viLoopback.setBandwidth(firstNonNull(vsLoopback.getBandwidth(), DEFAULT_LOOPBACK_BANDWIDTH));
  }

  private static void populateVlanInterfaceProperties(Configuration c, InterfacesInterface iface) {
    org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(iface.getName());
    viIface.setInterfaceType(InterfaceType.VLAN);
    viIface.setActive(true);
    viIface.setVlan(iface.getVlanId());
    viIface.setDescription(iface.getDescription());

    InterfaceAddress primaryAdress =
        (iface.getAddresses() == null || iface.getAddresses().isEmpty())
            ? null
            : iface.getAddresses().get(0);

    ImmutableSet.Builder<InterfaceAddress> allAddresses = ImmutableSet.builder();
    allAddresses.addAll(firstNonNull(iface.getAddresses(), ImmutableList.of()));
    firstNonNull(iface.getAddressVirtuals(), ImmutableMap.<MacAddress, Set<InterfaceAddress>>of())
        .values()
        .forEach(allAddresses::addAll);
    viIface.setAddress(primaryAdress);
    viIface.setAllAddresses(allAddresses.build());
  }

  /** properties for VRF loopback interfaces */
  private void populateVrfInterfaceProperties(Configuration c, InterfacesInterface iface) {
    org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(iface.getName());
    viIface.setInterfaceType(InterfaceType.LOOPBACK);
    viIface.setActive(true);
    // this loopback should be in its own vrf
    viIface.setVrf(getOrCreateVrf(c, iface.getName()));
  }

  private void populateSubInterfaceProperties(
      Configuration c, InterfacesInterface iface, String superInterfaceName) {
    org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(iface.getName());
    viIface.setInterfaceType(
        isPhysicalInterfaceType(superInterfaceName)
            ? InterfaceType.LOGICAL
            : InterfaceType.AGGREGATE_CHILD);

    viIface.setDependencies(
        ImmutableSet.of(new Dependency(superInterfaceName, DependencyType.BIND)));
    viIface.setEncapsulationVlan(InterfaceConverter.getEncapsulationVlan(iface));
  }

  private void populatePhysicalInterfaceProperties(Configuration c, InterfacesInterface iface) {
    org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(iface.getName());
    viIface.setInterfaceType(InterfaceType.PHYSICAL);
    populateBridgeSettings(iface, viIface);
    viIface.setDescription(iface.getDescription());
  }

  private void populateBondInterfaceProperties(Configuration c, InterfacesInterface iface) {
    org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(iface.getName());
    viIface.setInterfaceType(InterfaceType.AGGREGATED);
    Set<String> slaves = firstNonNull(iface.getBondSlaves(), ImmutableSet.of());
    slaves.forEach(slave -> c.getAllInterfaces().get(slave).setChannelGroup(iface.getName()));
    viIface.setChannelGroupMembers(slaves);
    viIface.setDependencies(
        slaves.stream()
            .map(slave -> new Dependency(slave, DependencyType.AGGREGATE))
            .collect(ImmutableSet.toImmutableSet()));
    viIface.setActive(true);
    populateBridgeSettings(iface, viIface);
    viIface.setMlagId(iface.getClagId());
  }

  @VisibleForTesting
  void populateCommonInterfaceProperties(
      InterfacesInterface vsIface, org.batfish.datamodel.Interface viIface) {
    // addresses
    if (vsIface.getAddresses() != null && !vsIface.getAddresses().isEmpty()) {
      List<ConcreteInterfaceAddress> addresses = vsIface.getAddresses();
      viIface.setAddress(addresses.get(0));
      viIface.setAllAddresses(addresses);
    }

    // description
    viIface.setDescription(vsIface.getDescription());

    // mtu
    if (vsIface.getMtu() != null) {
      viIface.setMtu(vsIface.getMtu());
    }

    // speed
    if (vsIface.getLinkSpeed() != null) {
      double speed = vsIface.getLinkSpeed() * SPEED_CONVERSION_FACTOR;
      viIface.setSpeed(speed);
      viIface.setBandwidth(speed);
    } else {
      viIface.setBandwidth(DEFAULT_PORT_BANDWIDTH);
    }
  }

  private void populateBridgeSettings(
      InterfacesInterface iface, org.batfish.datamodel.Interface newIface) {
    InterfaceBridgeSettings bridge =
        firstNonNull(iface.getBridgeSettings(), new InterfaceBridgeSettings());
    Integer access = bridge.getAccess();
    Integer ifacePvid = bridge.getPvid();
    IntegerSpace ifaceVids = bridge.getVids();
    Bridge mainBridge = getBridge();
    if (!mainBridge.getPorts().contains(iface.getName())) {
      if (access != null || ifacePvid != null || !ifaceVids.isEmpty()) {
        _w.redFlag(
            String.format(
                "No support for VLAN switching options on non-'bridge bridge' port: '%s'",
                iface.getName()));
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
    int nativeVlan = firstNonNull(ifacePvid, mainBridge.getPvid());
    newIface.setNativeVlan(nativeVlan);
    newIface.setAllowedVlans(
        (!ifaceVids.isEmpty()
            ? ifaceVids
            : getBridge().getVids().union(IntegerSpace.of(nativeVlan))));
  }

  @VisibleForTesting
  void initializeAllInterfaces(Configuration c) {
    _interfacesConfiguration.getInterfaces().values().stream()
        .filter(CumulusConcatenatedConfiguration::isValidVIInterface)
        .forEach(
            iface ->
                org.batfish.datamodel.Interface.builder()
                    .setName(iface.getName())
                    .setOwner(c)
                    .setVrf(getOrCreateVrf(c, iface.getVrf()))
                    .build());
    _frrConfiguration.getInterfaces().values().stream()
        .filter(iface -> !c.getAllInterfaces().containsKey(iface.getName()))
        .forEach(
            iface ->
                org.batfish.datamodel.Interface.builder()
                    .setName(iface.getName())
                    .setOwner(c)
                    .setVrf(getOrCreateVrf(c, iface.getVrfName()))
                    .build());

    // initialize super interfaces of sub-interfaces if needed
    Set<String> ifaceNames = ImmutableSet.copyOf(c.getAllInterfaces().keySet());
    ifaceNames.stream()
        .map(InterfaceConverter::getSuperInterfaceName)
        .filter(Objects::nonNull)
        .filter(superName -> !c.getAllInterfaces().containsKey(superName))
        .forEach(
            superName ->
                org.batfish.datamodel.Interface.builder()
                    .setName(superName)
                    .setOwner(c)
                    .setVrf(getOrCreateVrf(c, null))
                    .build());

    if (!c.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME)) {
      org.batfish.datamodel.Interface.builder()
          .setName(LOOPBACK_INTERFACE_NAME)
          .setOwner(c)
          .setVrf(getOrCreateVrf(c, null))
          .build();
    }
  }

  @Nonnull
  private static org.batfish.datamodel.Vrf getOrCreateVrf(
      Configuration c, @Nullable String vrfName) {
    if (vrfName == null) {
      return c.getVrfs().get(DEFAULT_VRF_NAME);
    }
    return c.getVrfs().computeIfAbsent(vrfName, org.batfish.datamodel.Vrf::new);
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
    markAbstractStructure(
        CumulusStructureType.ABSTRACT_INTERFACE,
        CumulusStructureUsage.PORT_SPEED,
        ImmutableSet.of(CumulusStructureType.INTERFACE));
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

  private void initVendorFamily(Configuration c) {
    c.getVendorFamily()
        .setCumulus(
            CumulusFamily.builder()
                .setBridge(getBridge().toDataModel())
                .setInterfaceClagSettings(
                    _interfacesConfiguration.getInterfaces().entrySet().stream()
                        .filter(ifaceEntry -> ifaceEntry.getValue().getClagSettings() != null)
                        .collect(
                            ImmutableSortedMap.toImmutableSortedMap(
                                Comparator.naturalOrder(),
                                Entry::getKey,
                                ifaceEntry ->
                                    ifaceEntry.getValue().getClagSettings().toDataModel())))
                .build());
  }

  private Bridge getBridge() {
    InterfacesInterface bridgeIface = _interfacesConfiguration.getInterfaces().get(BRIDGE_NAME);
    if (bridgeIface == null) {
      return new Bridge();
    }
    Bridge bridge = new org.batfish.representation.cumulus.Bridge();
    bridge.setPorts(firstNonNull(bridgeIface.getBridgePorts(), DEFAULT_BRIDGE_PORTS));
    InterfaceBridgeSettings bridgeSettings = bridgeIface.getBridgeSettings();
    if (bridgeSettings != null) {
      bridge.setVids(bridgeSettings.getVids());
      bridge.setPvid(firstNonNull(bridgeSettings.getPvid(), DEFAULT_BRIDGE_PVID));
    }
    return bridge;
  }

  private void warnDuplicateClagIds() {
    Map<Integer, List<InterfacesInterface>> clagBondsById = new HashMap<>();
    _interfacesConfiguration.getInterfaces().values().stream()
        .filter(bond -> bond.getClagId() != null) // bond interface
        .forEach(
            clagBond ->
                clagBondsById
                    .computeIfAbsent(clagBond.getClagId(), id -> new LinkedList<>())
                    .add(clagBond));
    clagBondsById.forEach(
        (id, clagBonds) -> {
          if (clagBonds.size() > 1) {
            _w.redFlag(
                String.format(
                    "clag-id %d is erroneously configured on more than one bond: %s",
                    id,
                    clagBonds.stream()
                        .map(InterfacesInterface::getName)
                        .collect(ImmutableList.toImmutableList())));
          }
        });
  }

  @Nonnull
  public CumulusInterfacesConfiguration getInterfacesConfiguration() {
    return _interfacesConfiguration;
  }

  @Nonnull
  public CumulusFrrConfiguration getFrrConfiguration() {
    return _frrConfiguration;
  }

  @Nonnull
  public CumulusPortsConfiguration getPortsConfiguration() {
    return _portsConfiguration;
  }

  @Override
  public Map<String, IpCommunityList> getIpCommunityLists() {
    return _frrConfiguration.getIpCommunityLists();
  }

  @Override
  public Map<String, IpPrefixList> getIpPrefixLists() {
    return _frrConfiguration.getIpPrefixLists();
  }

  @Override
  public Map<String, RouteMap> getRouteMaps() {
    return _frrConfiguration.getRouteMaps();
  }

  @Override
  public BgpProcess getBgpProcess() {
    return _frrConfiguration.getBgpProcess();
  }

  @Override
  public Optional<OspfInterface> getOspfInterface(String ifaceName) {
    if (!_frrConfiguration.getInterfaces().containsKey(ifaceName)) {
      return Optional.empty();
    }
    return Optional.ofNullable(_frrConfiguration.getInterfaces().get(ifaceName).getOspf());
  }

  @Override
  @Nullable
  public Vrf getVrf(String vrfName) {
    return _frrConfiguration.getVrfs().get(vrfName);
  }

  @Override
  @Nullable
  public List<Integer> getVxlanIds() {
    return _interfacesConfiguration.getInterfaces().values().stream()
        .filter(InterfaceConverter::isVxlan)
        .map(InterfacesInterface::getVxlanId)
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  @Nonnull
  public Map<String, InterfaceClagSettings> getClagSettings() {
    return _interfacesConfiguration.getInterfaces().values().stream()
        .filter(iface -> iface.getClagSettings() != null)
        .collect(
            ImmutableMap.toImmutableMap(
                InterfacesInterface::getName, InterfacesInterface::getClagSettings));
  }

  @Override
  public OspfProcess getOspfProcess() {
    return _frrConfiguration.getOspfProcess();
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link CumulusConcatenatedConfiguration} */
  public static final class Builder {
    private String _hostname;
    private CumulusInterfacesConfiguration _interfacesConfiguration;
    private CumulusFrrConfiguration _frrConfiguration;
    private CumulusPortsConfiguration _portsConfiguration;

    private Builder() {}

    public Builder setHostname(String hostname) {
      this._hostname = hostname;
      return this;
    }

    Builder setInterfacesConfiguration(CumulusInterfacesConfiguration interfacesConfiguration) {
      this._interfacesConfiguration = interfacesConfiguration;
      return this;
    }

    Builder addInterfaces(Map<String, InterfacesInterface> interfaces) {
      if (this._interfacesConfiguration == null) {
        this._interfacesConfiguration = new CumulusInterfacesConfiguration();
      }
      this._interfacesConfiguration.getInterfaces().putAll(interfaces);
      return this;
    }

    Builder setFrrConfiguration(CumulusFrrConfiguration frrConfiguration) {
      this._frrConfiguration = frrConfiguration;
      return this;
    }

    Builder setPortsConfiguration(CumulusPortsConfiguration portsConfiguration) {
      this._portsConfiguration = portsConfiguration;
      return this;
    }

    public Builder setPorts(Map<String, PortSettings> ports) {
      this._portsConfiguration = new CumulusPortsConfiguration();
      this._portsConfiguration.getPortSettings().putAll(ports);
      return this;
    }

    public CumulusConcatenatedConfiguration build() {
      CumulusConcatenatedConfiguration cumulusConcatenatedConfiguration =
          new CumulusConcatenatedConfiguration(
              firstNonNull(_interfacesConfiguration, new CumulusInterfacesConfiguration()),
              firstNonNull(_frrConfiguration, new CumulusFrrConfiguration()),
              firstNonNull(_portsConfiguration, new CumulusPortsConfiguration()));
      cumulusConcatenatedConfiguration.setHostname(_hostname);
      return cumulusConcatenatedConfiguration;
    }
  }
}
