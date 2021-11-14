package org.batfish.representation.cumulus_concatenated;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.representation.cumulus_concatenated.InterfaceConverter.BRIDGE_NAME;
import static org.batfish.representation.cumulus_concatenated.InterfaceConverter.DEFAULT_BRIDGE_PORTS;
import static org.batfish.representation.cumulus_concatenated.InterfaceConverter.DEFAULT_BRIDGE_PVID;
import static org.batfish.representation.cumulus_concatenated.InterfacesInterface.isPhysicalInterfaceType;
import static org.batfish.representation.frr.FrrConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.batfish.representation.frr.FrrConversions.DEFAULT_LOOPBACK_MTU;
import static org.batfish.representation.frr.FrrConversions.DEFAULT_PORT_BANDWIDTH;
import static org.batfish.representation.frr.FrrConversions.DEFAULT_PORT_MTU;
import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.SetMultimap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.runtime.InterfaceRuntimeData;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.vendor_family.cumulus.CumulusFamily;
import org.batfish.representation.cumulus_concatenated.CumulusPortsConfiguration.PortSettings;
import org.batfish.representation.frr.BgpProcess;
import org.batfish.representation.frr.Bridge;
import org.batfish.representation.frr.FrrConfiguration;
import org.batfish.representation.frr.FrrContainer;
import org.batfish.representation.frr.FrrConversions;
import org.batfish.representation.frr.FrrInterface;
import org.batfish.representation.frr.FrrStructureType;
import org.batfish.representation.frr.FrrStructureUsage;
import org.batfish.representation.frr.InterfaceBridgeSettings;
import org.batfish.representation.frr.InterfaceClagSettings;
import org.batfish.representation.frr.IpCommunityList;
import org.batfish.representation.frr.IpPrefixList;
import org.batfish.representation.frr.OspfProcess;
import org.batfish.representation.frr.RouteMap;
import org.batfish.representation.frr.Vrf;
import org.batfish.representation.frr.Vxlan;
import org.batfish.vendor.VendorConfiguration;

/**
 * A {@link VendorConfiguration} for FRR based on many files (/etc/hostname,
 * /etc/network/interfaces, /etc/frr/frr.conf, etc.).
 */
@ParametersAreNonnullByDefault
public class CumulusConcatenatedConfiguration extends FrrContainer {

  public static final Pattern BOND_INTERFACE_PATTERN = Pattern.compile("^(bond[0-9]+)");
  public static final Pattern PHYSICAL_INTERFACE_PATTERN =
      Pattern.compile("^(swp[0-9]+(s[0-9])?)|(eth[0-9]+)");
  public static final Pattern VLAN_INTERFACE_PATTERN = Pattern.compile("^vlan([0-9]+)$");
  public static final Pattern VXLAN_INTERFACE_PATTERN = Pattern.compile("^vxlan([0-9]+)$");
  public static final Pattern SUBINTERFACE_PATTERN = Pattern.compile("^(.*)\\.([0-9]+)$");

  @Nonnull private String _hostname;

  @Nonnull private final CumulusInterfacesConfiguration _interfacesConfiguration;

  @Nonnull private final FrrConfiguration _frrConfiguration;

  @Nonnull private final CumulusPortsConfiguration _portsConfiguration;

  private transient SetMultimap<String, Prefix> _ownedPrefixesByVrf;

  public CumulusConcatenatedConfiguration() {
    this(
        new CumulusInterfacesConfiguration(),
        new FrrConfiguration(),
        new CumulusPortsConfiguration());
  }

  private CumulusConcatenatedConfiguration(
      CumulusInterfacesConfiguration interfaces,
      FrrConfiguration frr,
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
    c.setExportBgpFromBgpRib(true);

    // create default VRF
    getOrCreateVrf(c, DEFAULT_VRF_NAME);

    initializeAllInterfaces(c);
    populateInterfacesInterfaceProperties(c);
    populatePortsInterfaceProperties(c);
    populateFrrInterfaceProperties(c);
    ensureInterfacesHaveTypes(c);

    initStaticRoutes(c);

    FrrConversions.convert(_frrConfiguration, this, c, _w);

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
  void initStaticRoutes(Configuration c) {
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
  }

  private void populateFrrInterfaceProperties(Configuration c) {
    _frrConfiguration.getInterfaceInitOrder().stream()
        .map(_frrConfiguration.getInterfaces()::get)
        .forEach(iface -> populateFrrInterfaceProperties(c, iface));
  }

  private void populateFrrInterfaceProperties(Configuration c, FrrInterface iface) {
    org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(iface.getName());
    checkArgument(
        viIface != null, "VI interface object not found for interface %s", iface.getName());
    if (iface.getAlias() != null) {
      viIface.setDescription(iface.getAlias());
    }
    if (iface.getShutdown()) {
      viIface.setActive(false);
    }

    // ip addresses
    String vrf = iface.getVrfName();
    ImmutableList.Builder<ConcreteInterfaceAddress> ownedIpAddressesBuilder =
        ImmutableList.builder();
    for (ConcreteInterfaceAddress address : iface.getIpAddresses()) {
      Prefix prefix = address.getPrefix();
      if (ownedPrefixesByVrf().containsEntry(vrf, prefix)) {
        viIface.setAdditionalArpIps(
            AclIpSpace.union(viIface.getAdditionalArpIps(), address.getIp().toIpSpace()));
        continue;
      }
      ownedPrefixesByVrf().put(vrf, prefix);
      ownedIpAddressesBuilder.add(address);
    }
    List<ConcreteInterfaceAddress> ownedIpAddresses = ownedIpAddressesBuilder.build();
    if (!ownedIpAddresses.isEmpty()) {
      viIface.setAddress(ownedIpAddresses.get(0));
      viIface.setAllAddresses(
          ImmutableSet.<InterfaceAddress>builder()
              .addAll(viIface.getAllAddresses())
              .addAll(ownedIpAddresses)
              .build());
    }
  }

  /** Add interface properties based on what we saw in the interfaces file */
  private void populateInterfacesInterfaceProperties(Configuration c) {
    _interfacesConfiguration.getInterfaceInitOrder().stream()
        .map(_interfacesConfiguration.getInterfaces()::get)
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
    viLoopback.setInterfaceType(InterfaceType.LOOPBACK);
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

  private void ensureInterfacesHaveTypes(Configuration c) {
    c.getAllInterfaces().values().forEach(this::ensureInterfaceHasType);
  }

  private void ensureInterfaceHasType(Interface i) {
    if (i.getInterfaceType() != InterfaceType.UNKNOWN) {
      // Already done.
      return;
    }

    if (VLAN_INTERFACE_PATTERN.matcher(i.getName()).matches()) {
      i.setInterfaceType(InterfaceType.VLAN);
      return;
    }

    if (PHYSICAL_INTERFACE_PATTERN.matcher(i.getName()).matches()) {
      // Physical interface or subinterface.
      Matcher parentMatcher = SUBINTERFACE_PATTERN.matcher(i.getName());
      if (parentMatcher.matches()) {
        i.setInterfaceType(InterfaceType.LOGICAL);
        return;
      }
      i.setInterfaceType(InterfaceType.PHYSICAL);
      return;
    }

    if (BOND_INTERFACE_PATTERN.matcher(i.getName()).matches()) {
      // Aggregate interface or subinterface.
      Matcher parentMatcher = SUBINTERFACE_PATTERN.matcher(i.getName());
      if (parentMatcher.matches()) {
        i.setInterfaceType(InterfaceType.AGGREGATE_CHILD);
        return;
      }
      i.setInterfaceType(InterfaceType.AGGREGATED);
      return;
    }

    _w.redFlag(String.format("Unable to determine interface type for %s", i.getName()));
    // Leave as UNKNOWN.
  }

  private @Nonnull SetMultimap<String, Prefix> ownedPrefixesByVrf() {
    if (_ownedPrefixesByVrf == null) {
      _ownedPrefixesByVrf = HashMultimap.create();
    }
    return _ownedPrefixesByVrf;
  }

  @VisibleForTesting
  void populateCommonInterfaceProperties(
      InterfacesInterface vsIface, org.batfish.datamodel.Interface viIface) {
    // addresses
    if (vsIface.getAddresses() != null && !vsIface.getAddresses().isEmpty()) {
      List<ConcreteInterfaceAddress> addresses = vsIface.getAddresses();
      ImmutableList.Builder<ConcreteInterfaceAddress> ownedAddressesBuilder =
          ImmutableList.builder();
      String vrf = firstNonNull(vsIface.getVrf(), DEFAULT_VRF_NAME);
      for (ConcreteInterfaceAddress address : addresses) {
        Prefix prefix = address.getPrefix();
        if (ownedPrefixesByVrf().containsEntry(vrf, prefix)) {
          viIface.setAdditionalArpIps(
              AclIpSpace.union(viIface.getAdditionalArpIps(), address.getIp().toIpSpace()));
          continue;
        }
        ownedPrefixesByVrf().put(vrf, prefix);
        ownedAddressesBuilder.add(address);
      }
      List<ConcreteInterfaceAddress> ownedAddresses = ownedAddressesBuilder.build();
      if (!ownedAddresses.isEmpty()) {
        viIface.setAddress(ownedAddresses.get(0));
        viIface.setAllAddresses(ownedAddresses);
      }
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
            iface -> initializeInterface(c, iface.getName(), iface.getVrfName(), _runtimeData, _w));

    // initialize super interfaces of sub-interfaces if needed
    Set<String> ifaceNames = ImmutableSet.copyOf(c.getAllInterfaces().keySet());
    ifaceNames.stream()
        .map(InterfaceConverter::getSuperInterfaceName)
        .filter(Objects::nonNull)
        .filter(superName -> !c.getAllInterfaces().containsKey(superName))
        .forEach(superName -> initializeInterface(c, superName, null, _runtimeData, _w));
    if (!c.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME)) {
      initializeInterface(c, LOOPBACK_INTERFACE_NAME, null, _runtimeData, _w);
    }
  }

  /** Sanity check user provided bandwidth values. */
  private static boolean validateBandwidth(Double value, String iface, Warnings w) {
    if (value <= 0) {
      w.redFlag(
          String.format(
              "Ignoring provided runtime bandwidth value %f for interface %s: not positive",
              value, iface));
      return false;
    } else if (value > 1000e9) {
      w.redFlag(
          String.format(
              "Ignoring provided runtime bandwidth value %f for interface %s: bigger than 1Tbps",
              value, iface));
      return false;
    }
    return true;
  }

  private static void initializeInterface(
      Configuration c,
      String ifaceName,
      @Nullable String vrfName,
      SnapshotRuntimeData snapshotRuntimeData,
      Warnings w) {
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
    double bandwidth =
        runtimeData
            .map(InterfaceRuntimeData::getBandwidth)
            .filter(bw -> validateBandwidth(bw, ifaceName, w))
            .orElse(guessedBandwidth);

    Interface.builder()
        .setName(ifaceName)
        .setOwner(c)
        .setVrf(getOrCreateVrf(c, vrfName))
        .setBandwidth(bandwidth)
        .setMtu(ifaceName.equals(LOOPBACK_INTERFACE_NAME) ? DEFAULT_LOOPBACK_MTU : DEFAULT_PORT_MTU)
        .setType(InterfaceType.UNKNOWN)
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
        FrrStructureType.ABSTRACT_INTERFACE,
        FrrStructureUsage.BGP_NEIGHBOR_INTERFACE,
        ImmutableSet.of(
            FrrStructureType.BOND,
            FrrStructureType.INTERFACE,
            FrrStructureType.LOOPBACK,
            FrrStructureType.VLAN,
            FrrStructureType.VRF));
    markAbstractStructure(
        FrrStructureType.ABSTRACT_INTERFACE,
        FrrStructureUsage.BRIDGE_PORT,
        ImmutableSet.of(
            FrrStructureType.BOND,
            FrrStructureType.INTERFACE,
            FrrStructureType.LOOPBACK,
            FrrStructureType.VLAN,
            FrrStructureType.VRF,
            FrrStructureType.VXLAN));
    markAbstractStructure(
        FrrStructureType.ABSTRACT_INTERFACE,
        FrrStructureUsage.ROUTE_MAP_MATCH_INTERFACE,
        ImmutableSet.of(
            FrrStructureType.BOND,
            FrrStructureType.INTERFACE,
            FrrStructureType.LOOPBACK,
            FrrStructureType.VLAN,
            FrrStructureType.VRF));
    markAbstractStructure(
        FrrStructureType.ABSTRACT_INTERFACE,
        FrrStructureUsage.PORT_SPEED,
        ImmutableSet.of(FrrStructureType.INTERFACE));
    markConcreteStructure(FrrStructureType.BOND);
    markConcreteStructure(FrrStructureType.INTERFACE);
    markConcreteStructure(FrrStructureType.IP_AS_PATH_ACCESS_LIST);
    markConcreteStructure(FrrStructureType.IP_COMMUNITY_LIST);
    markConcreteStructure(FrrStructureType.IP_PREFIX_LIST);
    markConcreteStructure(FrrStructureType.LOOPBACK);
    markConcreteStructure(FrrStructureType.ROUTE_MAP);
    markConcreteStructure(FrrStructureType.ROUTE_MAP_ENTRY);
    markConcreteStructure(FrrStructureType.VLAN);
    markConcreteStructure(FrrStructureType.VRF);
    markConcreteStructure(FrrStructureType.VXLAN);
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
    Bridge bridge = new org.batfish.representation.frr.Bridge();
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
  public FrrConfiguration getFrrConfiguration() {
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
    private FrrConfiguration _frrConfiguration;
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
        setFrrConfiguration(new FrrConfiguration());
      }
      _frrConfiguration.setBgpProcess(bgpProcess);
      return this;
    }

    Builder setFrrConfiguration(FrrConfiguration frrConfiguration) {
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
              firstNonNull(_frrConfiguration, new FrrConfiguration()),
              firstNonNull(_portsConfiguration, new CumulusPortsConfiguration()));
      cumulusConcatenatedConfiguration.setHostname(_hostname);
      if (_snapshotRuntimeData != null) {
        cumulusConcatenatedConfiguration.setRuntimeData(_snapshotRuntimeData);
      }
      return cumulusConcatenatedConfiguration;
    }
  }
}
