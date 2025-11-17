package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.FirewallSessionInterfaceInfo.Action.POST_NAT_FIB_LOOKUP;
import static org.batfish.datamodel.Prefix.MAX_PREFIX_LENGTH;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIcmp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.tracking.TrackMethods.negatedReference;
import static org.batfish.datamodel.tracking.TrackMethods.reachability;
import static org.batfish.vendor.a10.representation.A10Conversion.VIRTUAL_TCP_PORT_TYPES;
import static org.batfish.vendor.a10.representation.A10Conversion.VIRTUAL_UDP_PORT_TYPES;
import static org.batfish.vendor.a10.representation.A10Conversion.computeAclName;
import static org.batfish.vendor.a10.representation.A10Conversion.convertAccessList;
import static org.batfish.vendor.a10.representation.A10Conversion.createBgpProcess;
import static org.batfish.vendor.a10.representation.A10Conversion.findHaSourceAddress;
import static org.batfish.vendor.a10.representation.A10Conversion.findVrrpAEnabledSourceAddress;
import static org.batfish.vendor.a10.representation.A10Conversion.generatedFailedTrackMethodName;
import static org.batfish.vendor.a10.representation.A10Conversion.generatedServerTrackMethodName;
import static org.batfish.vendor.a10.representation.A10Conversion.getEnabledVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.getFloatingIpKernelRoutes;
import static org.batfish.vendor.a10.representation.A10Conversion.getFloatingIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getFloatingIpsByHaGroup;
import static org.batfish.vendor.a10.representation.A10Conversion.getFloatingIpsForAllVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.getInterfaceEnabledEffective;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolIpsByHaGroup;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolIpsForAllVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolKernelRoutes;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIpsByHaGroup;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIpsForAllVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerKernelRoutes;
import static org.batfish.vendor.a10.representation.A10Conversion.isVrrpAEnabled;
import static org.batfish.vendor.a10.representation.A10Conversion.toDstTransformationSteps;
import static org.batfish.vendor.a10.representation.A10Conversion.toMatchExpr;
import static org.batfish.vendor.a10.representation.A10Conversion.toSnatTransformationStep;
import static org.batfish.vendor.a10.representation.A10Conversion.toVrrpGroup;
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
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Collections;
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
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.packet_policy.ApplyFilter;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.IngressInterfaceVrf;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.Statement;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.TrackAction;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.ApplyAny;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.a10.representation.A10Conversion.VirtualServerTargetVirtualAddressExtractor;
import org.batfish.vendor.a10.representation.Interface.Type;

/** Datamodel class representing an A10 device configuration. */
public final class A10Configuration extends VendorConfiguration {

  @VisibleForTesting
  public static final String VIRTUAL_SERVERS_PACKET_POLICY_NAME = "~VIRTUAL_SERVERS_PACKET_POLICY~";

  public A10Configuration() {
    _accessLists = new HashMap<>();
    _floatingIps = new HashMap<>();
    _healthMonitors = new HashMap<>();
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

  public @Nullable BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  /** Gets the {@link BgpProcess} for this device, creating a new one if none already exists. */
  public @Nonnull BgpProcess getOrCreateBgpProcess(long number) {
    if (_bgpProcess == null) {
      _bgpProcess = new BgpProcess(number);
    }
    return _bgpProcess;
  }

  public @Nonnull AccessList getOrCreateAccessList(String name) {
    if (!_accessLists.containsKey(name)) {
      _accessLists.put(name, new AccessList(name));
    }
    return _accessLists.get(name);
  }

  public @Nonnull Map<String, AccessList> getAccessLists() {
    return _accessLists;
  }

  /** ACOSv2 {@code floating-ip}s. */
  public @Nonnull Map<Ip, FloatingIp> getV2FloatingIps() {
    return _floatingIps;
  }

  /** ACOSv2 {@code ha} configuration. */
  public @Nullable Ha getHa() {
    return _ha;
  }

  public @Nonnull Ha getOrCreateHa() {
    if (_ha == null) {
      _ha = new Ha();
    }
    return _ha;
  }

  public @Nonnull Map<String, HealthMonitor> getHealthMonitors() {
    return _healthMonitors;
  }

  public void createHealthMonitorIfAbsent(String name) {
    if (_healthMonitors.get(name) == null) {
      _healthMonitors.put(name, new HealthMonitor(name));
    }
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public @Nonnull Map<Integer, Interface> getInterfacesEthernet() {
    return _interfacesEthernet;
  }

  public @Nonnull Map<Integer, Interface> getInterfacesLoopback() {
    return _interfacesLoopback;
  }

  public @Nonnull Map<Integer, TrunkInterface> getInterfacesTrunk() {
    return _interfacesTrunk;
  }

  public @Nonnull Map<Integer, Interface> getInterfacesVe() {
    return _interfacesVe;
  }

  public @Nonnull Map<String, NatPool> getNatPools() {
    return _natPools;
  }

  public @Nonnull Map<String, ServiceGroup> getServiceGroups() {
    return ImmutableMap.copyOf(_serviceGroups);
  }

  public @Nonnull ServiceGroup getOrCreateServiceGroup(String name, ServerPort.Type type) {
    return _serviceGroups.computeIfAbsent(name, n -> new ServiceGroup(name, type));
  }

  public @Nonnull Map<String, Server> getServers() {
    return _servers;
  }

  public @Nonnull Map<String, VirtualServer> getVirtualServers() {
    return Collections.unmodifiableMap(_virtualServers);
  }

  public @Nullable VirtualServer getVirtualServer(String name) {
    return _virtualServers.get(name);
  }

  /**
   * Get the {@link VirtualServer} corresponding to the provided name, creating it (with the
   * specified {@link VirtualServerTarget}) if it doesn't already exist.
   */
  public @Nonnull VirtualServer getOrCreateVirtualServer(String name, VirtualServerTarget target) {
    return _virtualServers.computeIfAbsent(name, n -> new VirtualServer(n, target));
  }

  /** Map of route {@link Prefix} to {@link StaticRouteManager} for that prefix. */
  public @Nonnull Map<Prefix, StaticRouteManager> getStaticRoutes() {
    return _staticRoutes;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @VisibleForTesting
  public static int getInterfaceMtuEffective(Interface iface) {
    return firstNonNull(iface.getMtu(), DEFAULT_MTU);
  }

  public @Nonnull Map<Integer, Vlan> getVlans() {
    return _vlans;
  }

  public static @Nonnull InterfaceType getInterfaceType(Interface iface) {
    return switch (iface.getType()) {
      case ETHERNET -> InterfaceType.PHYSICAL;
      case LOOPBACK -> InterfaceType.LOOPBACK;
      case VE -> InterfaceType.VLAN;
      case TRUNK -> InterfaceType.AGGREGATED;
    };
  }

  public static @Nonnull String getInterfaceName(InterfaceReference ref) {
    return getInterfaceName(ref.getType(), ref.getNumber());
  }

  public static @Nonnull String getInterfaceName(Interface iface) {
    return getInterfaceName(iface.getType(), iface.getNumber());
  }

  public static @Nonnull String getInterfaceName(Interface.Type type, int num) {
    return getInterfaceHumanName(type, num).replace(" ", "");
  }

  public static @Nonnull String getInterfaceHumanName(Interface iface) {
    return getInterfaceHumanName(iface.getType(), iface.getNumber());
  }

  public static @Nonnull String getInterfaceHumanName(Interface.Type type, int num) {
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
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setHumanName(_rawHostname);
    _c.setDeviceModel(DeviceModel.A10);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    _c.setExportBgpFromBgpRib(true);

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

    convertAccessLists();

    // Must be done after interface conversion
    convertVirtualServers();
    convertHealthChecks();
    convertVrrpA();
    convertHa();
    createKernelRoutes();
    convertBgp();

    markStructures();

    generateReferenceBook();
    generateNatPoolIpSpaces();

    return ImmutableList.of(_c);
  }

  /**
   * Creates {@link TrackMethod}s for each enabled IPv4 server health check, searchable by server
   * IPv4 address.
   */
  private void convertHealthChecks() {
    for (Server server : _servers.values()) {
      if (!(server.getTarget() instanceof ServerTargetAddress)) {
        // Not an IPv4 server
        continue;
      }
      if (!firstNonNull(server.getEnable(), true)) {
        // server is disabled
        continue;
      }
      if (firstNonNull(server.getHealthCheckDisable(), false)) {
        // health check is disabled
        continue;
      }
      String healthMonitorName = server.getHealthCheck();
      if (healthMonitorName == null) {
        // no associated health monitor
        continue;
      }
      HealthMonitor healthMonitor = _healthMonitors.get(healthMonitorName);
      if (healthMonitor == null) {
        // undefined health monitor
        continue;
      }
      Ip ip = ((ServerTargetAddress) server.getTarget()).getAddress();
      // TODO: Use configured health monitor method (e.g. ICMP, TCP/123, etc.)
      //       instead of forcing ICMP (which is the default)
      TrackMethod method = reachability(ip, DEFAULT_VRF_NAME);
      String methodName = generatedServerTrackMethodName(ip);
      _c.getTrackingGroups().put(methodName, method);
    }
  }

  /** Returns map: template name -> generated track method name -> action */
  private @Nonnull Map<String, Map<String, TrackAction>> convertFailOverPolicyTemplates() {
    if (_vrrpA == null) {
      return ImmutableMap.of();
    }
    ImmutableMap.Builder<String, Map<String, TrackAction>> builder = ImmutableMap.builder();
    _vrrpA
        .getFailOverPolicyTemplates()
        .forEach(
            (templateName, template) -> {
              ImmutableMap.Builder<String, TrackAction> actionsBuilder = ImmutableMap.builder();
              template
                  .getGateways()
                  .forEach(
                      (ip, decrement) -> {
                        String trackMethodName = generatedServerTrackMethodName(ip);
                        if (!_c.getTrackingGroups().containsKey(trackMethodName)) {
                          // unusable gateway health check
                          return;
                        }
                        TrackAction action = new DecrementPriority(decrement);
                        String failedTrackMethodName = createFailedTrackIfNeeded(trackMethodName);
                        actionsBuilder.put(failedTrackMethodName, action);
                      });
              builder.put(templateName, actionsBuilder.build());
            });
    return builder.build();
  }

  /** Creates and puts a {@link ReferenceBook} for virtual servers defined in the configuration */
  private void generateReferenceBook() {
    String virtualAddressesBookname =
        GeneratedRefBookUtils.getName(_hostname, BookType.VirtualAddresses);
    _c.getGeneratedReferenceBooks()
        .put(
            virtualAddressesBookname,
            ReferenceBook.builder(virtualAddressesBookname)
                .setAddressGroups(
                    _virtualServers.values().stream()
                        .filter(A10Conversion::isIpv4VirtualServer)
                        .map(
                            vServer ->
                                new AddressGroup(
                                    ImmutableSortedSet.of(
                                        VirtualServerTargetVirtualAddressExtractor.INSTANCE
                                            .visit(vServer.getTarget())
                                            .toString()),
                                    vServer.getName()))
                        .collect(ImmutableList.toImmutableList()))
                .build());
  }

  /** Creates named IpSpaces from configured NAT pools. */
  private void generateNatPoolIpSpaces() {
    _natPools.forEach(
        (name, pool) ->
            _c.getIpSpaces()
                .put(ipSpaceNameForNatPool(name), IpRange.range(pool.getStart(), pool.getEnd())));
  }

  @VisibleForTesting
  static String ipSpaceNameForNatPool(String natPoolName) {
    return String.format("NatPool~%s", natPoolName);
  }

  private void convertBgp() {
    if (_bgpProcess == null) {
      return;
    }
    createBgpProcess(_bgpProcess, _c, _w);
  }

  /**
   * Create {@link org.batfish.datamodel.KernelRoute}s from {@code ip nat pool} networks, {@code slb
   * virtual-server}s, ACOSv5 {@code vrrp-a vrid floating-ip}s, and ACOSv2 {@code floating-ip}s.
   */
  private void createKernelRoutes() {
    _c.getDefaultVrf()
        .setKernelRoutes(
            Streams.concat(
                    getNatPoolKernelRoutes(_natPools.values()),
                    getVirtualServerKernelRoutes(_virtualServers.values()),
                    _vrrpA != null ? getFloatingIpKernelRoutes(_vrrpA) : Stream.of(),
                    getFloatingIpKernelRoutes(_floatingIps.keySet()))
                .collect(ImmutableSortedSet.toImmutableSortedSet(naturalOrder())));
  }

  private void convertVrrpA() {
    Map<String, Map<String, TrackAction>> failOverPolicyTemplateActions =
        convertFailOverPolicyTemplates();
    // If vrrp-a is disabled, then the device should act as if it owns all addresses that would have
    // been part of vrrp-a.
    // If vrrp-a is enabled, then the device should own all addresses for VRIDs that are enabled.
    if (isVrrpAEnabled(_vrrpA)) {
      convertVrrpAEnabled(failOverPolicyTemplateActions);
    } else if (_ha == null) {
      convertVrrpADisabled();
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
        Streams.concat(
                getNatPoolIpsForAllVrids(_natPools.values()),
                getVirtualServerIpsForAllVrids(_virtualServers.values()),
                _vrrpA != null ? getFloatingIpsForAllVrids(_vrrpA) : Stream.of())
            .map(ip -> ConcreteInterfaceAddress.create(ip, MAX_PREFIX_LENGTH))
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
                    naturalOrder(),
                    virtualAddress -> virtualAddress,
                    unused -> connectedRouteMetadata));
    _c.getAllInterfaces().values().stream()
        .filter(A10Conversion::vrrpADisabledAppliesToInterface)
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
   * addresses for each vrid for which it is converted VRRP master.
   */
  private void convertVrrpAEnabled(
      // template name -> generated track method name -> action
      Map<String, Map<String, TrackAction>> failOverPolicyTemplateActions) {
    // Overview:
    // - Add a VrrpGroup for each enabled vrid on the first found L3 interface owning a subnet
    //   containing any vrrp-a peer-group ip
    //   - abort if no peer-group ips are set
    // - Each VrrpGroup should assign the addresses to every OTHER L3 interface with a primary
    //   ConcreteInterfaceAddress.
    // - Each created VrrpGroup contains all the virtual addresses the device should own when it is
    //   master for the corresponding vrid.
    assert _vrrpA != null;
    Set<Ip> peerIps = _vrrpA.getPeerGroup();
    if (peerIps.isEmpty()) {
      _w.redFlag("Batfish does not support vrrp-a without at least one peer-group peer-ip");
      return;
    }
    ConcreteInterfaceAddress sourceAddress = null;
    org.batfish.datamodel.Interface peerInterface = null;
    for (org.batfish.datamodel.Interface iface : _c.getAllInterfaces().values()) {
      Optional<ConcreteInterfaceAddress> maybeSourceAddress =
          findVrrpAEnabledSourceAddress(iface, peerIps);
      if (maybeSourceAddress.isPresent()) {
        sourceAddress = maybeSourceAddress.get();
        peerInterface = iface;
        break;
      }
    }
    if (peerInterface == null) {
      _w.redFlagf(
          "Could not find any interface in a subnet containing any of the peer IPs: %s", peerIps);
      return;
    }
    // vrid -> virtual addresses
    ImmutableSetMultimap.Builder<Integer, Ip> virtualAddressesByEnabledVridBuilder =
        ImmutableSetMultimap.builder();
    // VRID 0 always exists, but may be disabled. Other VRIDs exist only if they are declared, and
    // cannot be disabled independently.
    // Grab the virtual addresses for each VRID from NAT pools and virtual-servers
    getEnabledVrids(_vrrpA)
        .forEach(
            vrid -> {
              Streams.concat(
                      getNatPoolIps(_natPools.values(), vrid),
                      getVirtualServerIps(_virtualServers.values(), vrid),
                      getFloatingIps(_vrrpA, vrid))
                  .forEach(ip -> virtualAddressesByEnabledVridBuilder.put(vrid, ip));
            });
    SetMultimap<Integer, Ip> virtualAddressesByEnabledVrid =
        virtualAddressesByEnabledVridBuilder.build();
    // VRID 0 may be used even if it is not configured explicitly.
    assert virtualAddressesByEnabledVrid.keySet().stream()
        .allMatch(vrid -> vrid == 0 || _vrrpA.getVrids().containsKey(vrid));
    // Create VrrpGroup builders for each vrid. We cannot make final VrrpGroups because we are
    // missing source address, which varies per interface.
    ImmutableSortedMap.Builder<Integer, VrrpGroup> vrrpGroupsBuilder =
        ImmutableSortedMap.naturalOrder();

    // Addresses should be assigned to all non-loopback L3 interfaces other than the peer interface.
    final org.batfish.datamodel.Interface finalPeerInterface = peerInterface;
    List<String> ipOwnerInterfaces =
        _c.getAllInterfaces().values().stream()
            .filter(
                i ->
                    i != finalPeerInterface
                        && i.getInterfaceType() != InterfaceType.LOOPBACK
                        && !i.getAllConcreteAddresses().isEmpty())
            .map(org.batfish.datamodel.Interface::getName)
            .collect(ImmutableList.toImmutableList());

    final ConcreteInterfaceAddress finalSourceAddress = sourceAddress;
    virtualAddressesByEnabledVrid
        .asMap()
        .forEach(
            (vrid, virtualAddresses) ->
                vrrpGroupsBuilder.put(
                    vrid,
                    toVrrpGroup(
                        _vrrpA.getVrids().get(vrid),
                        finalSourceAddress,
                        virtualAddresses,
                        ipOwnerInterfaces,
                        failOverPolicyTemplateActions)));
    // Assign the VRRP groups to the peer interface
    peerInterface.setVrrpGroups(vrrpGroupsBuilder.build());
  }

  /** Convert ha configuration for ACOSv2. */
  private void convertHa() {
    // TODO: Support virtual-addresses in case ha is disabled on ACOSv2.
    //       May need different behavior than convertVrrpADisabled(), which happens now in that
    //       case.
    if (_ha != null) {
      convertHaEnabled();
    }
  }

  /**
   * Process ha groups in the case ha is enabled. Causes the device to own all virtual addresses for
   * each ha-group for which it is converted VRRP master.
   */
  private void convertHaEnabled() {
    // Overview:
    // - Add a VrrpGroup for each enabled ha-group on the L3 interface owning subnet containing
    //   conn-mirror ip (the HA heartbeat interface)
    //   - abort if no conn-mirror ip is set
    // - Each created VrrpGroup contains all the virtual addresses the device should own when it is
    //   master for the corresponding vrid (using ha group id as vrid).
    // - Each created VrrpGroup should set the owned IPs on all L3 interfaces EXCEPT the heartbeat
    //   interface.
    assert _ha != null;
    Ip connMirror = _ha.getConnMirror();
    if (connMirror == null) {
      _w.redFlag("Batfish does not support ha without explicit conn-mirror");
      return;
    }
    Map<String, TrackAction> trackActions = convertHaChecks();
    // Find the ha heartbeat interface and source IP
    org.batfish.datamodel.Interface heartbeatInterface = null;
    ConcreteInterfaceAddress sourceAddress = null;
    for (org.batfish.datamodel.Interface iface : _c.getAllInterfaces().values()) {
      Optional<ConcreteInterfaceAddress> maybeSourceAddress =
          findHaSourceAddress(iface, connMirror);
      if (maybeSourceAddress.isPresent()) {
        heartbeatInterface = iface;
        sourceAddress = maybeSourceAddress.get();
        break;
      }
    }
    if (heartbeatInterface == null) {
      // Abort, since we couldn't find the heartbeat interface.
      _w.redFlagf(
          "Could not find any interface with address in subnet of ha conn-mirror IP %s",
          connMirror);
      return;
    }
    // ha group id -> virtual addresses
    ImmutableSetMultimap.Builder<Integer, Ip> virtualAddressesByEnabledHaGroupBuilder =
        ImmutableSetMultimap.builder();
    // VRID 0 always exists, but may be disabled. Other VRIDs exist only if they are declared, and
    // cannot be disabled independently.
    // Grab the virtual addresses for each VRID from NAT pools and virtual-servers
    _ha.getGroups()
        .forEach(
            (haGroupId, haGroup) -> {
              Streams.concat(
                      getNatPoolIpsByHaGroup(_natPools.values(), haGroupId),
                      getVirtualServerIpsByHaGroup(_virtualServers.values(), haGroupId),
                      getFloatingIpsByHaGroup(_floatingIps, haGroupId))
                  .forEach(ip -> virtualAddressesByEnabledHaGroupBuilder.put(haGroupId, ip));
            });
    SetMultimap<Integer, Ip> virtualAddressesByEnabledHaGroup =
        virtualAddressesByEnabledHaGroupBuilder.build();

    // Addresses should be assigned to all non-loopback L3 interfaces other than the heartbeat
    // interface
    final org.batfish.datamodel.Interface finalHeartbeatInterface = heartbeatInterface;
    List<String> ipOwnerInterfaces =
        _c.getAllInterfaces().values().stream()
            .filter(
                i ->
                    i != finalHeartbeatInterface
                        && i.getInterfaceType() != InterfaceType.LOOPBACK
                        && !i.getAllConcreteAddresses().isEmpty())
            .map(org.batfish.datamodel.Interface::getName)
            .collect(ImmutableList.toImmutableList());

    // Create VrrpGroups for each ha group, with addresses assigned to all other L3 interfaces..
    ImmutableSortedMap.Builder<Integer, VrrpGroup> vrrpGroupsBuilder =
        ImmutableSortedMap.naturalOrder();
    final ConcreteInterfaceAddress finalSourceAddress = sourceAddress;
    virtualAddressesByEnabledHaGroup
        .asMap()
        .forEach(
            (haGroupId, virtualAddresses) ->
                vrrpGroupsBuilder.put(
                    haGroupId,
                    toVrrpGroup(
                        haGroupId,
                        _ha,
                        finalSourceAddress,
                        virtualAddresses,
                        ipOwnerInterfaces,
                        trackActions)));
    // Assign the VRRP groups to the heartbeat interface
    heartbeatInterface.setVrrpGroups(vrrpGroupsBuilder.build());
  }

  /** Returns map: trackMethodName -> action */
  private @Nonnull Map<String, TrackAction> convertHaChecks() {
    assert _ha != null;
    ImmutableMap.Builder<String, TrackAction> builder = ImmutableMap.builder();
    for (Ip ip : _ha.getCheckGateways()) {
      String trackMethodName = generatedServerTrackMethodName(ip);
      if (!_c.getTrackingGroups().containsKey(trackMethodName)) {
        // unusable gateway health check
        continue;
      }
      // TODO: Docs say this device should no longer participate in HA if check fails, but we don't
      //       currently have an action for that. For now, best we can do is reduce priority to
      //       minimum.
      String failedTrackMethodName = createFailedTrackIfNeeded(trackMethodName);
      builder.put(failedTrackMethodName, new DecrementPriority(255));
    }

    // TODO: other check types
    return builder.build();
  }

  private @Nonnull String createFailedTrackIfNeeded(String trackMethodName) {
    String failedTrackMethodName = generatedFailedTrackMethodName(trackMethodName);
    if (!_c.getTrackingGroups().containsKey(failedTrackMethodName)) {
      _c.getTrackingGroups().put(failedTrackMethodName, negatedReference(trackMethodName));
    }
    return failedTrackMethodName;
  }

  private void convertAccessLists() {
    _accessLists.forEach((name, acl) -> convertAccessList(acl, _c, _filename));
  }

  /**
   * Convert virtual-servers to load-balancing VI constructs and attach resulting ACLs and
   * transformations to interfaces. Modifies VI interfaces and must be called after those are
   * created.
   */
  private void convertVirtualServers() {
    // Build transformation statements
    Return returnFibLookup = new Return(new FibLookup(IngressInterfaceVrf.instance()));
    List<Statement> transformationStatements = getTransformationStatements(returnFibLookup);

    // Apply transformations at each interface, along with incoming access list if present
    _c.getAllInterfaces()
        .forEach(
            (name, iface) -> {
              iface.setFirewallSessionInterfaceInfo(
                  new FirewallSessionInterfaceInfo(
                      POST_NAT_FIB_LOOKUP, ImmutableList.of(iface.getName()), null, null));
              String incomingFilter =
                  // TODO Can trunk interfaces configure access-list?
                  //  They are not included in _ifaceNametoIface.
                  Optional.ofNullable(_ifaceNametoIface.get(name))
                      .map(Interface::getAccessListIn)
                      .orElse(null);
              String policyName = packetPolicyName(incomingFilter);

              // Create packet policy if it doesn't already exist
              if (!_c.getPacketPolicies().containsKey(policyName)) {
                PacketPolicy policy;
                if (incomingFilter == null) {
                  policy = new PacketPolicy(policyName, transformationStatements, returnFibLookup);
                } else {
                  List<Statement> policyStatements =
                      ImmutableList.<Statement>builder()
                          .add(
                              new If(
                                  new PacketMatchExpr(
                                      new DeniedByAcl(computeAclName(incomingFilter))),
                                  ImmutableList.of(new Return(Drop.instance()))))
                          .addAll(transformationStatements)
                          .build();
                  policy = new PacketPolicy(policyName, policyStatements, returnFibLookup);
                }
                _c.getPacketPolicies().put(policyName, policy);
              }

              iface.setPacketPolicy(policyName);
            });
  }

  @VisibleForTesting
  public static String packetPolicyName(@Nullable String incomingFilter) {
    if (incomingFilter == null) {
      return VIRTUAL_SERVERS_PACKET_POLICY_NAME;
    }
    return String.format("~PACKET_POLICY_%s~", incomingFilter);
  }

  /**
   * Generate {@link Statement packet policy statements} encoding the NAT transformations for this
   * device.
   */
  private List<Statement> getTransformationStatements(Return returnFibLookup) {
    ImmutableList.Builder<Statement> statements = ImmutableList.builder();

    // Apply transformations to traffic matching virtual servers. These statements return FibLookup
    // after applying the transformation.
    _virtualServers.values().stream()
        .filter(A10Conversion::isIpv4VirtualServer)
        .filter(A10Conversion::isVirtualServerEnabled)
        .forEach(vs -> statements.add(toStatement(vs, returnFibLookup)));

    // Drop any remaining non-ping traffic destined to a VIP.
    Set<Ip> vips =
        _virtualServers.values().stream()
            .filter(A10Conversion::isIpv4VirtualServer)
            .filter(A10Conversion::isVirtualServerEnabled)
            .map(VirtualServer::getTarget)
            .map(VirtualServerTargetVirtualAddressExtractor.INSTANCE::visit)
            .collect(ImmutableSet.toImmutableSet());
    if (!vips.isEmpty()) {
      AclLineMatchExpr matchesVip =
          matchDst(
              AclIpSpace.union(
                  vips.stream().map(Ip::toIpSpace).collect(ImmutableSet.toImmutableSet())));
      AclLineMatchExpr ping =
          and(matchIpProtocol(IpProtocol.ICMP), matchIcmp(IcmpCode.ECHO_REQUEST));
      statements.add(
          new If(
              new PacketMatchExpr(and(matchesVip, not(ping))),
              ImmutableList.of(new Return(Drop.instance()))));
    }

    return statements.build();
  }

  /**
   * Convert specified {@link VirtualServer}'s transformations into a packet policy {@link
   * Statement}.
   */
  private Statement toStatement(VirtualServer vs, Return returnFibLookup) {
    return new If(
        new PacketMatchExpr(toMatchExpr(vs, _filename)),
        vs.getPorts().values().stream()
            .filter(A10Conversion::isVirtualServerPortEnabled)
            .map(vsPort -> toStatement(vsPort, returnFibLookup))
            .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Convert specified {@link VirtualServerPort}'s transformations into a packet policy {@link
   * Statement}. This statement returns a {@link FibLookup} for matching traffic, or a {@link Drop}
   * for matching traffic not permitted by the virtual server's (optional) ACL.
   */
  private Statement toStatement(VirtualServerPort port, Return returnFibLookup) {
    ImmutableList.Builder<Statement> trueStatements = ImmutableList.builder();
    String aclName = port.getAccessList();
    if (aclName != null) {
      String viAclName = computeAclName(aclName);
      assert _c.getIpAccessLists().containsKey(viAclName);
      trueStatements.add(new ApplyFilter(viAclName));
    }
    trueStatements.add(
        new ApplyTransformation(
            new Transformation(
                TrueExpr.INSTANCE, ImmutableList.of(toTransformationStep(port)), null, null)));
    trueStatements.add(returnFibLookup);
    return new If(new PacketMatchExpr(toMatchExpr(port)), trueStatements.build());
  }

  /**
   * Returns the full transformation (DNAT and SNAT if applicable) for the specified virtual-server
   * port.
   */
  private @Nonnull TransformationStep toTransformationStep(VirtualServerPort port) {
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
    boolean enabledEffective = getInterfaceEnabledEffective(iface, _majorVersionNumber);
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setAdminUp(enabledEffective)
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
      if (iface.getType() != Type.LOOPBACK) {
        newIface.setProxyArp(true);
      }
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
                      _w.redFlagf(
                          "Trunk member %s does not exist, cannot add to %s",
                          memberName, trunkName);
                    }
                    return ifaceExists;
                  })
              .collect(ImmutableSet.toImmutableSet());
      if (memberNames.isEmpty()) {
        _w.redFlagf(
            "%s does not contain any member interfaces",
            getInterfaceName(Type.TRUNK, trunkIface.getNumber()));
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
            _w.redFlagf(
                "VLAN settings for members of %s are different, ignoring their VLAN settings",
                trunkName);
          } else {
            // All members have the same VLAN settings, so just use the first
            String firstMemberName = memberNames.iterator().next();
            setVlanSettings(_ifaceNametoIface.get(firstMemberName), newIface);
          }
        } else {
          if (memberNames.stream()
              .anyMatch(memberName -> hasVlanSettings(_ifaceNametoIface.get(memberName)))) {
            _w.redFlagf(
                "Cannot configure VLAN settings on %s as well as its members. Member VLAN"
                    + " settings will be ignored.",
                trunkName);
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
    Stream<org.batfish.datamodel.Interface> distinctVlanSettings =
        names.stream()
            .map(
                name -> {
                  org.batfish.datamodel.Interface.Builder baseIface =
                      org.batfish.datamodel.Interface.builder()
                          .setName("")
                          .setType(InterfaceType.PHYSICAL);
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
    IntegerSpace.Builder allVlans = IntegerSpace.builder();
    if (untaggedVlan.isPresent()) {
      viIface.setSwitchportMode(SwitchportMode.TRUNK);
      viIface.setSwitchport(true);
      viIface.setNativeVlan(untaggedVlan.get().getNumber());
      allVlans.including(untaggedVlan.get().getNumber());
    }
    if (!taggedVlans.isEmpty()) {
      viIface.setSwitchportMode(SwitchportMode.TRUNK);
      viIface.setSwitchport(true);
      taggedVlans.forEach(vlan -> allVlans.including(vlan.getNumber()));
    }
    viIface.setAllowedVlans(allVlans.build());
    if (iface.getType() == Interface.Type.VE) {
      int vlanNumber = iface.getNumber();
      viIface.setVlan(vlanNumber);
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
  public @Nonnull Optional<Vlan> getUntaggedVlan(Interface iface) {
    InterfaceReference ref = new InterfaceReference(iface.getType(), iface.getNumber());
    return _vlans.values().stream().filter(v -> v.getUntagged().contains(ref)).findFirst();
  }

  /** Returns all VLANs associated with the specified tagged interface. */
  private @Nonnull List<Vlan> getTaggedVlans(Interface iface) {
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
   * Returns the major version number determined for this configuration. Returns {@code null} if no
   * version number could be determined.
   */
  public @Nullable Integer getMajorVersionNumber() {
    return _majorVersionNumber;
  }

  public void setMajorVersionNumber(@Nullable Integer majorVersionNumber) {
    _majorVersionNumber = majorVersionNumber;
  }

  /**
   * Finalize configuration after it is finished being built. Does things like making structures
   * immutable.
   *
   * <p>This should only be called once, at the end of parsing and extraction.
   */
  public void finalizeStructures() {
    _accessLists = ImmutableMap.copyOf(_accessLists);
    _floatingIps = ImmutableMap.copyOf(_floatingIps);
    _healthMonitors = ImmutableMap.copyOf(_healthMonitors);
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
  private @Nullable transient Map<String, Interface> _ifaceNametoIface;

  private @Nonnull Map<String, AccessList> _accessLists;
  private @Nullable BgpProcess _bgpProcess;
  private Configuration _c;
  private @Nonnull Map<Ip, FloatingIp> _floatingIps;
  private @Nullable Ha _ha;
  private @Nonnull Map<String, HealthMonitor> _healthMonitors;
  private String _hostname;

  /** Hostname as it appears in the config, uncanonicalized */
  private String _rawHostname;

  private @Nonnull Map<Integer, Interface> _interfacesEthernet;
  private @Nonnull Map<Integer, Interface> _interfacesLoopback;
  private @Nonnull Map<Integer, TrunkInterface> _interfacesTrunk;
  private @Nonnull Map<Integer, Interface> _interfacesVe;
  private @Nullable Integer _majorVersionNumber;
  private @Nonnull Map<String, NatPool> _natPools;
  private @Nonnull Map<String, Server> _servers;
  private @Nonnull Map<String, ServiceGroup> _serviceGroups;
  private @Nonnull Map<Prefix, StaticRouteManager> _staticRoutes;
  private @Nonnull Map<String, VirtualServer> _virtualServers;
  private @Nullable VrrpA _vrrpA;
  private @Nonnull Map<Integer, Vlan> _vlans;
  private ConfigurationFormat _vendor;
}
