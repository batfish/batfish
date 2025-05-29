package org.batfish.representation.fortios;

import static org.batfish.representation.fortios.FortiosBgpConversions.convertBgp;
import static org.batfish.representation.fortios.FortiosBgpConversions.convertRouteMap;
import static org.batfish.representation.fortios.FortiosPolicyConversions.computeOutgoingFilterName;
import static org.batfish.representation.fortios.FortiosPolicyConversions.convertPolicies;
import static org.batfish.representation.fortios.FortiosPolicyConversions.generateCrossZoneFilters;
import static org.batfish.representation.fortios.FortiosPolicyConversions.generateOutgoingFilters;
import static org.batfish.representation.fortios.FortiosPolicyConversions.getZonesAndUnzonedInterfaces;
import static org.batfish.representation.fortios.FortiosPolicyConversions.toIpSpace;
import static org.batfish.representation.fortios.FortiosPolicyConversions.toIpSpaceMetadata;
import static org.batfish.representation.fortios.FortiosPolicyConversions.toMatchExpr;
import static org.batfish.representation.fortios.FortiosRouteConversions.convertStaticRoutes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.collections.InsertOrderedMap;
import org.batfish.representation.fortios.Interface.Type;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.VendorStructureId;

public class FortiosConfiguration extends VendorConfiguration {

  public FortiosConfiguration() {
    _accessLists = new HashMap<>();
    _addresses = new HashMap<>();
    _addrgrps = new HashMap<>();
    _interfaces = new HashMap<>();
    _internetServiceNames = new HashMap<>();
    _policies = new InsertOrderedMap<>();
    _renameableObjects = new HashMap<>();
    _replacemsgs = new HashMap<>();
    _routeMaps = new HashMap<>();
    _services = new HashMap<>();
    _serviceGroups = new HashMap<>();
    _staticRoutes = new HashMap<>();
    _zones = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  public @Nonnull Map<String, AccessList> getAccessLists() {
    return _accessLists;
  }

  public @Nonnull Map<String, Address> getAddresses() {
    return _addresses;
  }

  public @Nonnull Map<String, Addrgrp> getAddrgrps() {
    return _addrgrps;
  }

  public @Nullable BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public @Nonnull Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  /** name -> internet-service-name */
  public @Nonnull Map<String, InternetServiceName> getInternetServiceNames() {
    return _internetServiceNames;
  }

  /** name -> policy */
  public @Nonnull InsertOrderedMap<String, Policy> getPolicies() {
    return _policies;
  }

  /** majorType -> minorType -> replacemsg config */
  public @Nonnull Map<String, Map<String, Replacemsg>> getReplacemsgs() {
    return _replacemsgs;
  }

  /** UUID -> renameable object */
  public @Nonnull Map<BatfishUUID, FortiosRenameableObject> getRenameableObjects() {
    return _renameableObjects;
  }

  /** name -> route-map */
  public @Nonnull Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  /** name -> service */
  public @Nonnull Map<String, Service> getServices() {
    return _services;
  }

  /** name -> service group */
  public @Nonnull Map<String, ServiceGroup> getServiceGroups() {
    return _serviceGroups;
  }

  /** route seq num -> static route */
  public @Nonnull Map<String, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  /** name -> zone */
  public @Nonnull Map<String, Zone> getZones() {
    return _zones;
  }

  /** Initializes configuration's {@link BgpProcess} if it isn't already initialized */
  public void initBgpProcess() {
    if (_bgpProcess == null) {
      _bgpProcess = new BgpProcess();
    }
  }

  private String _hostname;
  private String _rawHostname;
  private final @Nonnull Map<String, AccessList> _accessLists;
  private final @Nonnull Map<String, Address> _addresses;
  private final @Nonnull Map<String, Addrgrp> _addrgrps;
  private final @Nonnull Map<String, Interface> _interfaces;
  private final @Nonnull Map<String, InternetServiceName> _internetServiceNames;
  // Note: using InsertOrderedMap to preserve insertion order and permit reordering policies
  private final @Nonnull InsertOrderedMap<String, Policy> _policies;
  private final @Nonnull Map<BatfishUUID, FortiosRenameableObject> _renameableObjects;
  private final @Nonnull Map<String, Map<String, Replacemsg>> _replacemsgs;
  private final @Nonnull Map<String, RouteMap> _routeMaps;
  private final @Nonnull Map<String, Service> _services;
  private final @Nonnull Map<String, ServiceGroup> _serviceGroups;
  private final @Nonnull Map<String, StaticRoute> _staticRoutes;
  private final @Nonnull Map<String, Zone> _zones;

  private @Nullable BgpProcess _bgpProcess;

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    Configuration c = new Configuration(_hostname, ConfigurationFormat.FORTIOS);
    c.setHumanName(_rawHostname);
    c.setDeviceModel(DeviceModel.FORTIOS_UNSPECIFIED);
    // TODO: verify
    c.setDefaultCrossZoneAction(LineAction.DENY);
    // TODO: verify
    c.setDefaultInboundAction(LineAction.DENY);

    // Convert addresses
    _addresses.values().forEach(address -> addIpSpaceForAddress(c, address, _w, _filename));
    _addrgrps.values().forEach(addrgrp -> addIpSpaceForAddrgrp(c, addrgrp, _w, _filename));

    // Convert policies. Must happen after c._ipSpaces is populated (addresses are converted)
    // Convert each policy to an AclLine
    Map<String, AclLine> convertedPolicies = getConvertedPolicies(c.getIpSpaces().keySet());
    // Identify the structures for which cross-zone filters are needed (zones and unzoned ifaces)
    List<InterfaceOrZone> zonesAndUnzonedInterfaces =
        getZonesAndUnzonedInterfaces(_zones.values(), _interfaces.values());
    // Generate a cross-zone filter (IpAccessList) for every pair of structures that need them
    generateCrossZoneFilters(zonesAndUnzonedInterfaces, convertedPolicies, this, c);
    // Generate an outgoing IpAccessList for every zone and every unzoned interface
    generateOutgoingFilters(zonesAndUnzonedInterfaces, c);

    // Convert interfaces. Must happen after converting policies
    _interfaces.values().forEach(iface -> convertInterface(iface, c));

    // Convert zones
    c.setZones(
        _zones.values().stream()
            .collect(
                ImmutableMap.toImmutableMap(Zone::getName, FortiosConfiguration::convertZone)));

    // Convert access-lists
    _accessLists.forEach(
        (name, accessList) ->
            c.getRouteFilterLists().put(name, convertAccessList(accessList, _filename)));

    // Convert route-maps. Must happen after access-list conversion (and prefix-list conversion once
    // they are supported)
    _routeMaps.values().forEach(routeMap -> convertRouteMap(routeMap, c, _w));

    // Convert BGP. Must happen after interface conversion
    if (_bgpProcess != null) {
      convertBgp(_bgpProcess, c, _w);
    }

    // TODO Are FortiOS static routes really global? Can't set their VRFs. Perhaps they should
    //  only exist in their device's VRF.
    // Convert static routes and add them to every VRF. Must happen after all VRFs are created
    // (interfaces must be converted).
    SortedSet<org.batfish.datamodel.StaticRoute> viStaticRoutes =
        convertStaticRoutes(_staticRoutes.values());
    c.getVrfs().values().forEach(vrf -> vrf.setStaticRoutes(viStaticRoutes));

    // Count structure references
    markConcreteStructure(FortiosStructureType.ROUTE_MAP);
    markConcreteStructure(FortiosStructureType.ADDRESS);
    markConcreteStructure(FortiosStructureType.ADDRGRP);
    markConcreteStructure(FortiosStructureType.SERVICE_CUSTOM);
    markConcreteStructure(FortiosStructureType.SERVICE_GROUP);
    markConcreteStructure(FortiosStructureType.INTERFACE);
    markConcreteStructure(FortiosStructureType.ZONE);
    markConcreteStructure(FortiosStructureType.ACCESS_LIST);
    markConcreteStructure(FortiosStructureType.POLICY);
    return c;
  }

  @VisibleForTesting
  public @Nonnull Map<String, AclLine> getConvertedPolicies(Set<String> viIpSpaces) {
    Stream<Service> services = _services.values().stream();
    Stream<ServiceGroup> serviceGroups = _serviceGroups.values().stream();
    Map<String, ServiceGroupMember> serviceGroupMembers =
        Stream.concat(services, serviceGroups)
            .collect(ImmutableMap.toImmutableMap(ServiceGroupMember::getName, Function.identity()));
    Map<String, AclLineMatchExpr> convertedServices =
        serviceGroupMembers.values().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    ServiceGroupMember::getName,
                    sgm -> toMatchExpr(sgm, serviceGroupMembers, _filename)));

    Map<String, AddrgrpMember> addrgrpMembers =
        Stream.concat(_addresses.values().stream(), _addrgrps.values().stream())
            .collect(ImmutableMap.toImmutableMap(AddrgrpMember::getName, Function.identity()));

    // Convert each policy to an AclLine
    return convertPolicies(_policies, convertedServices, addrgrpMembers, viIpSpaces, _filename, _w);
  }

  private void convertInterface(Interface iface, Configuration c) {
    Interface.Type parentType = iface.getParent() != null ? iface.getParent().getType() : null;
    InterfaceType type = toViType(iface.getTypeEffective(), parentType);
    if (type == null) {
      _w.redFlagf(
          "Interface %s has unsupported type %s and will not be converted",
          iface.getName(), iface.getTypeEffective());
      return;
    }
    String vdom = iface.getVdom();
    assert vdom != null; // An interface with no VDOM set should fail in extraction
    String vrfName = computeVrfName(vdom, iface.getVrfEffective());
    // Referencing a VRF in an interface implicitly creates it
    Vrf vrf = c.getVrfs().computeIfAbsent(vrfName, name -> Vrf.builder().setName(name).build());
    org.batfish.datamodel.Interface.Builder viIface =
        org.batfish.datamodel.Interface.builder()
            .setOwner(c)
            .setName(iface.getName())
            .setVrf(vrf)
            .setDescription(iface.getDescription())
            .setAdminUp(iface.getStatusEffective())
            .setMtu(iface.getMtuEffective())
            .setSpeed(toSpeed(iface.getSpeedEffective()))
            .setType(type);

    List<InterfaceAddress> secondaryAddresses =
        iface.getSecondaryIpEffective()
            ? iface.getSecondaryip().values().stream()
                .map(SecondaryIp::getIp)
                .filter(Objects::nonNull)
                .collect(ImmutableList.toImmutableList())
            : ImmutableList.of();
    viIface.setAddresses(iface.getIp(), secondaryAddresses);

    if (iface.getTypeEffective() == Type.VLAN) {
      // Handled by extraction
      assert iface.getVlanid() != null && iface.getInterface() != null;

      viIface.setEncapsulationVlan(iface.getVlanid());
    }

    if (iface.getInterface() != null) {
      viIface.setDependencies(
          ImmutableSet.of(new Dependency(iface.getInterface(), DependencyType.BIND)));
    }

    Set<String> members = iface.getMembers();
    if (!members.isEmpty()) {
      // AGGREGATE and REDUNDANT
      viIface.setDependencies(
          members.stream()
              .map(member -> new Dependency(member, DependencyType.AGGREGATE))
              .collect(ImmutableSet.toImmutableSet()));
    }

    // TODO Is this the right VI field for interface alias?
    Optional.ofNullable(iface.getAlias())
        .ifPresent(alias -> viIface.setDeclaredNames(ImmutableList.of(iface.getAlias())));
    InterfaceOrZone parentIfaceOrZone = findParentInterfaceOrZone(iface);
    if (parentIfaceOrZone instanceof Zone) {
      viIface.setZoneName(parentIfaceOrZone.getName());
    }
    // TODO Check whether FortiOS should use outgoing filter or outgoing original flow filter (i.e.
    //  whether policies act on post-NAT or original flows)
    String outgoingFilterName = computeOutgoingFilterName(parentIfaceOrZone);
    viIface.setOutgoingFilter(c.getIpAccessLists().get(outgoingFilterName));
    viIface.build();
  }

  /** Convert interface speed setting into bits per second. */
  private static double toSpeed(Interface.Speed speed) {
    return switch (speed) {
      case TEN_FULL, TEN_HALF -> 10e6;
      case HUNDRED_FULL, HUNDRED_HALF -> 100e6;
      case THOUSAND_FULL, THOUSAND_HALF -> 1000e6;
      case TEN_THOUSAND_FULL, TEN_THOUSAND_HALF -> 10000e6;
      case HUNDRED_GFULL, HUNDRED_GHALF -> 100e9;
      case AUTO ->
          // Assume 10Gbps default
          10000e6;
    };
  }

  /**
   * Returns the {@link Zone} that contains the given {@code iface}, or {@code iface} itself if it
   * doesn't belong to a zone.
   */
  private @Nonnull InterfaceOrZone findParentInterfaceOrZone(Interface iface) {
    // extraction guarantees no interface is owned by more than one zone
    return _zones.values().stream()
        .filter(zone -> zone.getInterface().contains(iface.getName()))
        .map(InterfaceOrZone.class::cast)
        .findAny()
        .orElse(iface);
  }

  private @Nullable InterfaceType toViType(
      Interface.Type vsType, @Nullable Interface.Type parentType) {
    return switch (vsType) {
      case AGGREGATE -> InterfaceType.AGGREGATED;
      case LOOPBACK -> InterfaceType.LOOPBACK;
      case PHYSICAL -> {
        if (parentType == Interface.Type.AGGREGATE) {
          yield InterfaceType.AGGREGATE_CHILD;
        } else if (parentType == Interface.Type.REDUNDANT) {
          yield InterfaceType.REDUNDANT_CHILD;
        }
        yield InterfaceType.PHYSICAL;
      }
      case REDUNDANT -> InterfaceType.REDUNDANT;
      case TUNNEL -> InterfaceType.TUNNEL;
      case VLAN -> InterfaceType.LOGICAL;
      // TODO Support this type
      case WL_MESH,
          // TODO Support this type
          EMAC_VLAN ->
          null;
    };
  }

  @VisibleForTesting
  static @Nonnull RouteFilterList convertAccessList(
      AccessList accessList, String vendorConfigFilename) {
    List<RouteFilterLine> lines =
        accessList.getRules().values().stream()
            .map(
                rule -> {
                  LineAction action =
                      rule.getActionEffective() == AccessListRule.Action.PERMIT
                          ? LineAction.PERMIT
                          : LineAction.DENY;
                  if (rule.getPrefix() != null) {
                    int prefixLength = rule.getPrefix().getPrefixLength();
                    SubRange lengthRange =
                        rule.getExactMatchEffective()
                            ? SubRange.singleton(prefixLength)
                            : new SubRange(prefixLength, Prefix.MAX_PREFIX_LENGTH);
                    return new RouteFilterLine(action, rule.getPrefix(), lengthRange);
                  } else {
                    assert rule.getWildcard() != null;
                    // Can match network of any length as long as the IP matches the wildcard
                    return new RouteFilterLine(
                        action, rule.getWildcard(), new SubRange(0, Prefix.MAX_PREFIX_LENGTH));
                  }
                })
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        accessList.getName(),
        lines,
        new VendorStructureId(
            vendorConfigFilename,
            FortiosStructureType.ACCESS_LIST.getDescription(),
            accessList.getName()));
  }

  private static @Nonnull org.batfish.datamodel.Zone convertZone(Zone zone) {
    org.batfish.datamodel.Zone viZone = new org.batfish.datamodel.Zone(zone.getName());
    viZone.setInterfaces(zone.getInterface());
    return viZone;
  }

  /** Computes the VI name for a VRF in the given VDOM with the given VRF number. */
  @VisibleForTesting
  public static @Nonnull String computeVrfName(String vdom, int vrf) {
    return String.format("%s:%s", vdom, vrf);
  }

  private static void addIpSpaceForAddress(
      Configuration c, Address address, Warnings w, String filename) {
    c.getIpSpaces().put(address.getName(), toIpSpace(address, w));
    c.getIpSpaceMetadata().put(address.getName(), toIpSpaceMetadata(address, filename));
  }

  private static void addIpSpaceForAddrgrp(
      Configuration c, Addrgrp addrgrp, Warnings w, String filename) {
    c.getIpSpaces().put(addrgrp.getName(), toIpSpace(addrgrp, w));
    c.getIpSpaceMetadata().put(addrgrp.getName(), toIpSpaceMetadata(addrgrp, filename));
  }
}
