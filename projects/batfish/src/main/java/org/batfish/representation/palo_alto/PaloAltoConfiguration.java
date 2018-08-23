package org.batfish.representation.palo_alto;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.apache.commons.collections4.list.TreeList;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.vendor.StructureUsage;
import org.batfish.vendor.VendorConfiguration;

public final class PaloAltoConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  /** This is the name of an application that matches all traffic */
  public static final String CATCHALL_APPLICATION_NAME = "any";

  /** This is the name of a service that matches all traffic */
  public static final String CATCHALL_SERVICE_NAME = "any";

  /** This is the name of the zone that matches traffic in all zones (but not unzoned traffic) */
  public static final String CATCHALL_ZONE_NAME = "any";

  public static final String DEFAULT_VSYS_NAME = "vsys1";

  public static final String NULL_VRF_NAME = "~NULL_VRF~";

  public static final String SHARED_VSYS_NAME = "~SHARED_VSYS~";

  private Configuration _c;

  private String _dnsServerPrimary;

  private String _dnsServerSecondary;

  private String _hostname;

  private final SortedMap<String, Interface> _interfaces;

  private String _ntpServerPrimary;

  private String _ntpServerSecondary;

  private ConfigurationFormat _vendor;

  private final SortedMap<String, VirtualRouter> _virtualRouters;

  private final SortedMap<String, Vsys> _virtualSystems;

  public PaloAltoConfiguration() {
    _interfaces = new TreeMap<>();
    _virtualRouters = new TreeMap<>();
    _virtualSystems = new TreeMap<>();
  }

  private NavigableSet<String> getDnsServers() {
    NavigableSet<String> servers = new TreeSet<>();
    if (_dnsServerPrimary != null) {
      servers.add(_dnsServerPrimary);
    }
    if (_dnsServerSecondary != null) {
      servers.add(_dnsServerSecondary);
    }
    return servers;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public SortedMap<String, Interface> getInterfaces() {
    return _interfaces;
  }

  private NavigableSet<String> getNtpServers() {
    NavigableSet<String> servers = new TreeSet<>();
    if (_ntpServerPrimary != null) {
      servers.add(_ntpServerPrimary);
    }
    if (_ntpServerSecondary != null) {
      servers.add(_ntpServerSecondary);
    }
    return servers;
  }

  public SortedMap<String, VirtualRouter> getVirtualRouters() {
    return _virtualRouters;
  }

  public SortedMap<String, Vsys> getVirtualSystems() {
    return _virtualSystems;
  }

  public void setDnsServerPrimary(String dnsServerPrimary) {
    _dnsServerPrimary = dnsServerPrimary;
  }

  public void setDnsServerSecondary(String dnsServerSecondary) {
    _dnsServerSecondary = dnsServerSecondary;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  public void setNtpServerPrimary(String ntpServerPrimary) {
    _ntpServerPrimary = ntpServerPrimary;
  }

  public void setNtpServerSecondary(String ntpServerSecondary) {
    _ntpServerSecondary = ntpServerSecondary;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  // Visible for testing

  /**
   * Generate unique object name (no collision across vsys namespaces) given a vsys name and
   * original object name
   */
  public static String computeObjectName(String vsysName, String objectName) {
    return String.format("%s~%s", vsysName, objectName);
  }

  /** Generate egress IpAccessList name given an interface or zone name */
  public static String computeOutgoingFilterName(String interfaceOrZoneName) {
    return String.format("~%s~OUTGOING_FILTER~", interfaceOrZoneName);
  }

  /**
   * Extract object name from a name with an embedded namespace. For example: nameWithNamespace
   * might be `vsys1~SERVICE1`, where `SERVICE1` is the object name extracted and returned.
   */
  private static String extractObjectName(String nameWithNamespace) {
    String[] parts = nameWithNamespace.split("~", -1);
    return parts[parts.length - 1];
  }

  // Visible for testing

  /**
   * Generate IpAccessList name for the specified serviceGroupMemberName in the specified vsysName
   */
  public static String computeServiceGroupMemberAclName(
      String vsysName, String serviceGroupMemberName) {
    return String.format("~%s~SERVICE_GROUP_MEMBER~%s~", vsysName, serviceGroupMemberName);
  }

  /** Convert vsys components to vendor independent model */
  private void convertVirtualSystems() {
    NavigableSet<String> loggingServers = new TreeSet<>();

    for (Vsys vsys : _virtualSystems.values()) {
      loggingServers.addAll(vsys.getSyslogServerAddresses());
      String vsysName = vsys.getName();

      // Convert PAN zones and create their corresponding outgoing ACLs
      for (Entry<String, Zone> zoneEntry : vsys.getZones().entrySet()) {
        Zone zone = zoneEntry.getValue();
        String zoneName = computeObjectName(vsysName, zone.getName());
        _c.getZones().put(zoneName, toZone(zoneName, zone));

        String aclName = computeOutgoingFilterName(zoneName);
        _c.getIpAccessLists().put(aclName, generateOutgoingFilter(aclName, zone));
      }

      // Services
      for (Service service : vsys.getServices().values()) {
        String serviceGroupAclName = computeServiceGroupMemberAclName(vsysName, service.getName());
        _c.getIpAccessLists()
            .put(serviceGroupAclName, service.toIpAccessList(LineAction.PERMIT, this, vsys));
      }

      // Service groups
      for (ServiceGroup serviceGroup : vsys.getServiceGroups().values()) {
        String serviceGroupAclName =
            computeServiceGroupMemberAclName(vsysName, serviceGroup.getName());
        _c.getIpAccessLists()
            .put(serviceGroupAclName, serviceGroup.toIpAccessList(LineAction.PERMIT, this, vsys));
      }
    }
    _c.setLoggingServers(loggingServers);
  }

  /** Generate outgoing IpAccessList for the specified zone */
  private IpAccessList generateOutgoingFilter(String name, Zone toZone) {
    List<IpAccessListLine> lines = new TreeList<>();
    SortedMap<String, Rule> rules = toZone.getVsys().getRules();

    for (Rule rule : rules.values()) {
      if (!rule.getDisabled()
          && (rule.getTo().contains(toZone.getName())
              || rule.getTo().contains(CATCHALL_ZONE_NAME))) {
        lines.add(toIpAccessListLine(rule));
      }
    }

    // Intrazone traffic is allowed by default
    lines.add(
        IpAccessListLine.builder()
            .accepting()
            .setMatchCondition(new MatchSrcInterface(toZone.getInterfaceNames()))
            .build());

    return IpAccessList.builder().setName(name).setLines(lines).build();
  }

  /** Convert specified firewall rule into an IpAccessListLine */
  private IpAccessListLine toIpAccessListLine(Rule rule) {
    List<AclLineMatchExpr> conjuncts = new TreeList<>();
    IpAccessListLine.Builder ipAccessListLineBuilder =
        IpAccessListLine.builder().setName(rule.getName());
    if (rule.getAction() == LineAction.PERMIT) {
      ipAccessListLineBuilder.accepting();
    } else {
      ipAccessListLineBuilder.rejecting();
    }

    // TODO(https://github.com/batfish/batfish/issues/2097): need to handle matching specified
    // applications

    // Construct headerspace match expression
    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();
    for (IpSpace source : rule.getSource()) {
      headerSpaceBuilder.addSrcIp(source);
    }
    for (IpSpace destination : rule.getDestination()) {
      headerSpaceBuilder.addDstIp(destination);
    }
    conjuncts.add(new MatchHeaderSpace(headerSpaceBuilder.build()));

    // Construct source zone (source interface) match expression
    SortedSet<String> ruleFroms = rule.getFrom();
    if (!ruleFroms.isEmpty()) {
      List<String> srcInterfaces = new TreeList<>();
      for (String zoneName : ruleFroms) {
        if (zoneName.equals(CATCHALL_ZONE_NAME)) {
          for (Zone zone : rule.getVsys().getZones().values()) {
            srcInterfaces.addAll(zone.getInterfaceNames());
          }
          break;
        }
        srcInterfaces.addAll(rule.getVsys().getZones().get(zoneName).getInterfaceNames());
      }
      conjuncts.add(new MatchSrcInterface(srcInterfaces));
    }

    // Construct service match expression
    SortedSet<ServiceOrServiceGroupReference> ruleServices = rule.getService();
    if (!ruleServices.isEmpty()) {
      List<AclLineMatchExpr> serviceDisjuncts = new TreeList<>();
      for (ServiceOrServiceGroupReference service : ruleServices) {
        if (service.getName().equals(CATCHALL_SERVICE_NAME)) {
          serviceDisjuncts.add(TrueExpr.INSTANCE);
          break;
        } else {
          serviceDisjuncts.add(
              new PermittedByAcl(
                  computeServiceGroupMemberAclName(
                      service.getVsysName(this, rule.getVsys()), service.getName())));
        }
      }
      conjuncts.add(new OrMatchExpr(serviceDisjuncts));
    }

    return ipAccessListLineBuilder
        .setName(rule.getName())
        .setMatchCondition(new AndMatchExpr(conjuncts))
        .build();
  }

  /** Convert Palo Alto specific interface into vendor independent model interface */
  private org.batfish.datamodel.Interface toInterface(Interface iface) {
    String name = iface.getName();
    org.batfish.datamodel.Interface newIface =
        new org.batfish.datamodel.Interface(name, _c, InterfaceType.PHYSICAL);
    Integer mtu = iface.getMtu();
    if (mtu != null) {
      newIface.setMtu(mtu);
    }
    newIface.setAddress(iface.getAddress());
    newIface.setAllAddresses(iface.getAllAddresses());
    newIface.setActive(iface.getActive());
    newIface.setDescription(iface.getComment());

    String ifAclName = computeOutgoingFilterName(iface.getName());
    Zone zone = iface.getZone();
    if (zone != null) {
      newIface.setZoneName(zone.getName());
      newIface.setOutgoingFilter(
          IpAccessList.builder()
              .setOwner(_c)
              .setName(ifAclName)
              .setLines(
                  ImmutableList.of(
                      IpAccessListLine.accepting()
                          .setMatchCondition(
                              new PermittedByAcl(
                                  computeOutgoingFilterName(
                                      computeObjectName(zone.getVsys().getName(), zone.getName()))))
                          .build()))
              .build());
    } else {
      // Do not allow any traffic exiting an unzoned interface
      newIface.setOutgoingFilter(
          IpAccessList.builder()
              .setOwner(_c)
              .setName(ifAclName)
              .setLines(
                  ImmutableList.of(
                      IpAccessListLine.builder()
                          .rejecting()
                          .setMatchCondition(TrueExpr.INSTANCE)
                          .build()))
              .build());
    }
    return newIface;
  }

  /** Convert Palo Alto specific virtual router into vendor independent model Vrf */
  private Vrf toVrf(VirtualRouter vr) {
    Vrf vrf = new Vrf(vr.getName());

    // Static routes
    for (Entry<String, StaticRoute> e : vr.getStaticRoutes().entrySet()) {
      StaticRoute sr = e.getValue();
      // Can only construct a static route if it has a destination
      if (sr.getDestination() != null) {
        vrf.getStaticRoutes()
            .add(
                org.batfish.datamodel.StaticRoute.builder()
                    .setNextHopInterface(sr.getNextHopInterface())
                    .setNextHopIp(sr.getNextHopIp())
                    .setAdministrativeCost(sr.getAdminDistance())
                    .setMetric(sr.getMetric())
                    .setNetwork(sr.getDestination())
                    .build());
      } else {
        _w.redFlag(
            String.format(
                "Cannot convert static route %s, as it does not have a destination.", e.getKey()));
      }
    }

    // Interfaces
    NavigableMap<String, org.batfish.datamodel.Interface> map = new TreeMap<>();
    for (String interfaceName : vr.getInterfaceNames()) {
      org.batfish.datamodel.Interface iface = _c.getInterfaces().get(interfaceName);
      if (iface != null) {
        map.put(interfaceName, iface);
        iface.setVrf(vrf);
      }
    }
    vrf.setInterfaces(map);

    return vrf;
  }

  /** Convert Palo Alto zone to vendor independent model zone */
  private org.batfish.datamodel.Zone toZone(String name, Zone zone) {
    org.batfish.datamodel.Zone newZone = new org.batfish.datamodel.Zone(name);
    newZone.setInterfaces(zone.getInterfaceNames());
    return newZone;
  }

  @Override
  public Configuration toVendorIndependentConfiguration() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    _c.setDnsServers(getDnsServers());
    _c.setNtpServers(getNtpServers());

    // Handle converting items within virtual systems
    convertVirtualSystems();

    for (Entry<String, Interface> i : _interfaces.entrySet()) {
      _c.getInterfaces().put(i.getKey(), toInterface(i.getValue()));
    }

    // Vrf conversion uses interfaces, so must be done after interface exist in VI model
    for (Entry<String, VirtualRouter> vr : _virtualRouters.entrySet()) {
      _c.getVrfs().put(vr.getKey(), toVrf(vr.getValue()));
    }

    // Batfish cannot handle interfaces without a Vrf
    // So put orphaned interfaces in a constructed Vrf and shut them down
    Vrf nullVrf = new Vrf(NULL_VRF_NAME);
    NavigableMap<String, org.batfish.datamodel.Interface> orphanedInterfaces = new TreeMap<>();
    for (Entry<String, org.batfish.datamodel.Interface> i : _c.getInterfaces().entrySet()) {
      org.batfish.datamodel.Interface iface = i.getValue();
      if (iface.getVrf() == null) {
        orphanedInterfaces.put(iface.getName(), iface);
        iface.setVrf(nullVrf);
        iface.setActive(false);
        _w.redFlag(
            String.format(
                "Interface %s is not in a virtual-router, placing in %s and shutting it down.",
                iface.getName(), nullVrf.getName()));
      }
    }
    if (orphanedInterfaces.size() > 0) {
      nullVrf.setInterfaces(orphanedInterfaces);
      _c.getVrfs().put(nullVrf.getName(), nullVrf);
    }

    // Handle converting items within virtual systems
    convertVirtualSystems();

    // Count and mark simple structure usages and identify undefined references
    markConcreteStructure(
        PaloAltoStructureType.INTERFACE,
        PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE,
        PaloAltoStructureUsage.ZONE_INTERFACE);
    markConcreteStructure(PaloAltoStructureType.RULE, PaloAltoStructureUsage.RULE_SELF_REF);
    markConcreteStructure(
        PaloAltoStructureType.ZONE,
        PaloAltoStructureUsage.RULE_FROM_ZONE,
        PaloAltoStructureUsage.RULE_TO_ZONE);

    // Handle marking for structures that may exist in one of a couple namespaces
    markAbstractStructureFromUnknownNamespace(
        PaloAltoStructureType.SERVICE_OR_SERVICE_GROUP,
        ImmutableList.of(PaloAltoStructureType.SERVICE, PaloAltoStructureType.SERVICE_GROUP),
        PaloAltoStructureUsage.SERVICE_GROUP_MEMBER,
        PaloAltoStructureUsage.RULEBASE_SERVICE);
    return _c;
  }

  /**
   * Helper method to return DefinedStructureInfo for the structure with the specified name that
   * could be any of the specified structureTypesToCheck, return null if no match is found
   */
  private @Nullable DefinedStructureInfo findDefinedStructure(
      String name, Collection<PaloAltoStructureType> structureTypesToCheck) {
    for (PaloAltoStructureType typeToCheck : structureTypesToCheck) {
      Map<String, DefinedStructureInfo> matchingDefinitions =
          _structureDefinitions.get(typeToCheck.getDescription());
      if (matchingDefinitions != null && !matchingDefinitions.isEmpty()) {
        DefinedStructureInfo definition = matchingDefinitions.get(name);
        if (definition != null) {
          return definition;
        }
      }
    }
    return null;
  }

  /**
   * Update referrers and/or warn for undefined structures based on references to an abstract
   * structure type existing in either the reference's namespace or shared namespace
   */
  private void markAbstractStructureFromUnknownNamespace(
      PaloAltoStructureType type,
      Collection<PaloAltoStructureType> structureTypesToCheck,
      PaloAltoStructureUsage... usages) {
    Map<String, SortedMap<StructureUsage, SortedMultiset<Integer>>> references =
        firstNonNull(_structureReferences.get(type), Collections.emptyMap());
    for (PaloAltoStructureUsage usage : usages) {
      references.forEach(
          (nameWithNamespace, byUsage) -> {
            String name = extractObjectName(nameWithNamespace);
            Multiset<Integer> lines = firstNonNull(byUsage.get(usage), TreeMultiset.create());
            // Check this namespace first
            DefinedStructureInfo info =
                findDefinedStructure(nameWithNamespace, structureTypesToCheck);
            // Check shared namespace if there was no match
            if (info == null) {
              info =
                  findDefinedStructure(
                      computeObjectName(SHARED_VSYS_NAME, name), structureTypesToCheck);
            }

            // Now update reference count if applicable
            if (info != null) {
              info.setNumReferrers(
                  info.getNumReferrers() == DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                      ? DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                      : info.getNumReferrers() + lines.size());
            } else {
              for (int line : lines) {
                undefined(type, name, usage, line);
              }
            }
          });
    }
  }
}
