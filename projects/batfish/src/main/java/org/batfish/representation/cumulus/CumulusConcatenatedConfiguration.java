package org.batfish.representation.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.representation.cumulus.BgpProcess.BGP_UNNUMBERED_IP;
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
import static org.batfish.representation.cumulus.CumulusConversions.convertVxlans;
import static org.batfish.representation.cumulus.CumulusConversions.isUsedForBgpUnnumbered;
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
import org.batfish.common.runtime.InterfaceRuntimeData;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.vendor_family.cumulus.CumulusFamily;
import org.batfish.representation.cumulus.CumulusPortsConfiguration.PortSettings;
import org.batfish.vendor.VendorConfiguration;

/**
 * A {@link VendorConfiguration} for FRR based on many files (/etc/hostname,
 * /etc/network/interfaces, /etc/frr/frr.conf, etc.).
 */
@ParametersAreNonnullByDefault
public class CumulusConcatenatedConfiguration extends VendorConfiguration {

  public static final String LOOPBACK_INTERFACE_NAME = "lo";
  @VisibleForTesting public static final String CUMULUS_CLAG_DOMAIN_ID = "~CUMULUS_CLAG_DOMAIN~";
  public static final @Nonnull LinkLocalAddress LINK_LOCAL_ADDRESS =
      LinkLocalAddress.of(BGP_UNNUMBERED_IP);

  @Nonnull private String _hostname;

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
  @Nonnull
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(@Nonnull String hostname) {
    _hostname = hostname.toLowerCase();
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
    c.setDeviceModel(DeviceModel.CUMULUS_UNSPECIFIED);
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

    // FRR does not generate local routes for connected routes.
    c.getAllInterfaces()
        .values()
        .forEach(
            i -> {
              ImmutableSortedMap.Builder<ConcreteInterfaceAddress, ConnectedRouteMetadata>
                  metadata = ImmutableSortedMap.naturalOrder();
              for (InterfaceAddress a : i.getAllAddresses()) {
                if (!(a instanceof ConcreteInterfaceAddress)) {
                  continue;
                }
                ConcreteInterfaceAddress address = (ConcreteInterfaceAddress) a;
                metadata.put(
                    address, ConnectedRouteMetadata.builder().setGenerateLocalRoute(false).build());
              }
              i.setAddressMetadata(metadata.build());
            });

    initVrfStaticRoutes(c);

    convertIpAsPathAccessLists(c, _frrConfiguration.getIpAsPathAccessLists());
    convertIpPrefixLists(c, _frrConfiguration.getIpPrefixLists());
    convertIpCommunityLists(c, _frrConfiguration.getIpCommunityLists());
    convertRouteMaps(c, this, _frrConfiguration.getRouteMaps(), _w);
    convertDnsServers(c, _frrConfiguration.getIpv4Nameservers());
    convertClags(c, this, _w);

    // Compute explicit VNI -> VRF mappings for L3 VNIs:
    Map<Integer, String> vniToVrf =
        _frrConfiguration.getVrfs().values().stream()
            .filter(vrf -> vrf.getVni() != null)
            .collect(ImmutableMap.toImmutableMap(Vrf::getVni, Vrf::getName));

    @Nullable
    InterfacesInterface vsLoopback =
        _interfacesConfiguration.getInterfaces().get(LOOPBACK_INTERFACE_NAME);
    @Nullable
    Ip loopbackClagVxlanAnycastIp = vsLoopback == null ? null : vsLoopback.getClagVxlanAnycastIp();
    @Nullable
    Ip loopbackVxlanLocalTunnelIp = vsLoopback == null ? null : vsLoopback.getVxlanLocalTunnelIp();

    convertVxlans(c, this, vniToVrf, loopbackClagVxlanAnycastIp, loopbackVxlanLocalTunnelIp, _w);

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

  @Nullable
  public String getVrfForVlan(@Nullable Integer bridgeAccessVlan) {
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
    if (iface.getShutdown()) {
      viIface.setActive(false);
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
    populateLoopbackProperties(
        _interfacesConfiguration.getInterfaces().get(LOOPBACK_INTERFACE_NAME),
        c.getAllInterfaces().get(LOOPBACK_INTERFACE_NAME));
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
  static void populateLoopbackProperties(
      @Nullable InterfacesInterface vsLoopback, org.batfish.datamodel.Interface viLoopback) {
    if (vsLoopback != null && vsLoopback.getClagVxlanAnycastIp() != null) {
      // Just assume CLAG is correctly configured and comes up
      viLoopback.setAllAddresses(
          ImmutableSet.<InterfaceAddress>builder()
              .addAll(viLoopback.getAllAddresses())
              .add(
                  ConcreteInterfaceAddress.create(
                      vsLoopback.getClagVxlanAnycastIp(), Prefix.MAX_PREFIX_LENGTH))
              .build());
    }
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
  static void populateCommonInterfaceProperties(
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
    // Ensure that frrConfiguration contains every valid interface that is declared in either file.
    _interfacesConfiguration.getInterfaces().values().stream()
        .filter(CumulusConcatenatedConfiguration::isValidVIInterface)
        .forEach(iface -> _frrConfiguration.getOrCreateInterface(iface.getName(), iface.getVrf()));
    _frrConfiguration
        .getInterfaces()
        .values()
        .forEach(
            iface -> initializeInterface(c, iface.getName(), iface.getVrfName(), _runtimeData));

    // initialize super interfaces of sub-interfaces if needed
    Set<String> ifaceNames = ImmutableSet.copyOf(c.getAllInterfaces().keySet());
    ifaceNames.stream()
        .map(InterfaceConverter::getSuperInterfaceName)
        .filter(Objects::nonNull)
        .filter(superName -> !c.getAllInterfaces().containsKey(superName))
        .forEach(superName -> initializeInterface(c, superName, null, _runtimeData));
    if (!c.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME)) {
      initializeInterface(c, LOOPBACK_INTERFACE_NAME, null, _runtimeData);
    }
  }

  private static void initializeInterface(
      Configuration c,
      String ifaceName,
      @Nullable String vrfName,
      SnapshotRuntimeData snapshotRuntimeData) {
    // Either use the provided runtime data to get the interface speed, or else default to guessing
    // based on name.
    Optional<InterfaceRuntimeData> runtimeData =
        Optional.ofNullable(c.getHostname())
            .map(snapshotRuntimeData::getRuntimeData)
            .map(d -> d.getInterface(ifaceName));
    double guessedBandwidth =
        ifaceName.equals(LOOPBACK_INTERFACE_NAME)
            ? DEFAULT_LOOPBACK_BANDWIDTH
            : DEFAULT_PORT_BANDWIDTH;
    double bandwidth = runtimeData.map(InterfaceRuntimeData::getBandwidth).orElse(guessedBandwidth);

    Interface.builder()
        .setName(ifaceName)
        .setOwner(c)
        .setVrf(getOrCreateVrf(c, vrfName))
        .setBandwidth(bandwidth)
        .build();
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

  public Map<String, IpCommunityList> getIpCommunityLists() {
    return _frrConfiguration.getIpCommunityLists();
  }

  public Map<String, IpPrefixList> getIpPrefixLists() {
    return _frrConfiguration.getIpPrefixLists();
  }

  public Map<String, RouteMap> getRouteMaps() {
    return _frrConfiguration.getRouteMaps();
  }

  public BgpProcess getBgpProcess() {
    return _frrConfiguration.getBgpProcess();
  }

  public Optional<OspfInterface> getOspfInterface(String ifaceName) {
    if (!_frrConfiguration.getInterfaces().containsKey(ifaceName)) {
      return Optional.empty();
    }
    return Optional.ofNullable(_frrConfiguration.getInterfaces().get(ifaceName).getOspf());
  }

  @Nullable
  public Vrf getVrf(String vrfName) {
    return _frrConfiguration.getVrfs().get(vrfName);
  }

  @Nonnull
  public Map<String, Vxlan> getVxlans() {
    return _interfacesConfiguration.getInterfaces().values().stream()
        .filter(InterfaceConverter::isVxlan)
        .map(InterfaceConverter::convertVxlan)
        .collect(ImmutableMap.toImmutableMap(Vxlan::getName, vxlan -> vxlan));
  }

  @Nonnull
  public Map<String, InterfaceClagSettings> getClagSettings() {
    return _interfacesConfiguration.getInterfaces().values().stream()
        .filter(iface -> iface.getClagSettings() != null)
        .collect(
            ImmutableMap.toImmutableMap(
                InterfacesInterface::getName, InterfacesInterface::getClagSettings));
  }

  public OspfProcess getOspfProcess() {
    return _frrConfiguration.getOspfProcess();
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link CumulusConcatenatedConfiguration} */
  public static final class Builder {
    private String _hostname;
    private @Nullable SnapshotRuntimeData _snapshotRuntimeData;
    private CumulusInterfacesConfiguration _interfacesConfiguration;
    private CumulusFrrConfiguration _frrConfiguration;
    private CumulusPortsConfiguration _portsConfiguration;

    private Builder() {}

    public Builder setHostname(String hostname) {
      _hostname = hostname;
      return this;
    }

    Builder setInterfacesConfiguration(CumulusInterfacesConfiguration interfacesConfiguration) {
      _interfacesConfiguration = interfacesConfiguration;
      return this;
    }

    Builder addInterfaces(Map<String, InterfacesInterface> interfaces) {
      if (_interfacesConfiguration == null) {
        _interfacesConfiguration = new CumulusInterfacesConfiguration();
      }
      _interfacesConfiguration.getInterfaces().putAll(interfaces);
      return this;
    }

    Builder setBgpProcess(BgpProcess bgpProcess) {
      if (_frrConfiguration == null) {
        setFrrConfiguration(new CumulusFrrConfiguration());
      }
      _frrConfiguration.setBgpProcess(bgpProcess);
      return this;
    }

    Builder setFrrConfiguration(CumulusFrrConfiguration frrConfiguration) {
      _frrConfiguration = frrConfiguration;
      return this;
    }

    Builder setPortsConfiguration(CumulusPortsConfiguration portsConfiguration) {
      _portsConfiguration = portsConfiguration;
      return this;
    }

    public Builder setPorts(Map<String, PortSettings> ports) {
      _portsConfiguration = new CumulusPortsConfiguration();
      _portsConfiguration.getPortSettings().putAll(ports);
      return this;
    }

    public Builder setSnapshotRuntimeData(@Nullable SnapshotRuntimeData data) {
      _snapshotRuntimeData = data;
      return this;
    }

    public CumulusConcatenatedConfiguration build() {
      CumulusConcatenatedConfiguration cumulusConcatenatedConfiguration =
          new CumulusConcatenatedConfiguration(
              firstNonNull(_interfacesConfiguration, new CumulusInterfacesConfiguration()),
              firstNonNull(_frrConfiguration, new CumulusFrrConfiguration()),
              firstNonNull(_portsConfiguration, new CumulusPortsConfiguration()));
      cumulusConcatenatedConfiguration.setHostname(_hostname);
      if (_snapshotRuntimeData != null) {
        cumulusConcatenatedConfiguration.setRuntimeData(_snapshotRuntimeData);
      }
      return cumulusConcatenatedConfiguration;
    }
  }
}
