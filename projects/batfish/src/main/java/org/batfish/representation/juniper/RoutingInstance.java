package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public class RoutingInstance implements Serializable {

  public static final long OSPF_INTERNAL_SUMMARY_DISCARD_METRIC = 0x00FFFFFFL;
  private static final double DEFAULT_OSPF_REFERENCE_BANDWIDTH = 1E9;
  static final String MASTER_INTERFACE_NAME = "MASTER_INTERFACE";

  private @Nullable Long _as;
  private AggregateRoute _aggregateRouteDefaults;
  private @Nonnull Map<RoutingProtocol, String> _appliedRibGroups;
  private boolean _bgpAlwaysCompareMed;
  private boolean _bgpExternalRouterId;
  private boolean _bgpMedPlusIgp;
  private @Nullable Integer _bgpMedPlusIgpIgpMultiplier;
  private @Nullable Integer _bgpMedPlusIgpMedMultiplier;
  private Long _confederation;
  private Set<Long> _confederationMembers;
  private final SortedMap<String, DhcpRelayGroup> _dhcpRelayGroups;
  private final SortedMap<String, DhcpRelayServerGroup> _dhcpRelayServerGroups;
  private String _domainName;
  private boolean _exportLocalRoutesLan;
  private boolean _exportLocalRoutesPointToPoint;
  private String _forwardingTableExportPolicy;
  private GeneratedRoute _generatedRouteDefaults;
  private final Interface _globalMasterInterface;
  private String _hostname;
  private final List<String> _instanceExports;
  private final List<String> _instanceImports;
  private final Map<String, Interface> _interfaces;
  private boolean _independentDomain;
  private boolean _independentDomainNoAttrset;
  private Map<Prefix, IpBgpGroup> _ipBgpGroups;
  private final @Nonnull IsisSettings _isisSettings;
  private @Nullable IsisInterfaceSettings _interfaceAllIsisSettings;
  private @Nullable Integer _loops;
  private BgpGroup _masterBgpGroup;
  private final @Nonnull String _name;
  private Map<String, NamedBgpGroup> _namedBgpGroups;
  private final Map<String, NodeDevice> _nodeDevices;
  private @Nullable Long _ospf3DomainVpnTag;
  private final Map<String, String> _ospf3RibGroups;
  private Map<Long, OspfArea> _ospfAreas;
  private @Nullable Long _ospfDomainVpnTag;
  private List<String> _ospfExportPolicies;
  private final @Nonnull Set<String> _ospfOverloadedTopologies;
  private @Nullable Boolean _ospfDisable;
  private @Nullable Long _ospfExternalPreference;
  private @Nullable Long _ospfPreference;
  private double _ospfReferenceBandwidth;
  private final Map<String, String> _ospfRibGroups;
  private @Nullable OspfInterfaceSettings _interfaceAllOspfSettings;
  private final Map<String, RoutingInformationBase> _ribs;
  private Ip _routerId;
  private SnmpServer _snmpServer;
  private final JuniperSystem _system;
  private @Nullable Resolution _resolution;
  private @Nonnull Map<String, BridgeDomain> _bridgeDomains;
  private @Nullable EvpnIpPrefixRoutes _evpnIpPrefixRoutes;
  private @Nullable RouteDistinguisher _routeDistinguisher;
  private @Nullable Ip _routeDistinguisherId;
  private @Nullable ExtendedCommunity _vrfTargetCommunity;
  private @Nullable ExtendedCommunity _vrfTargetImport;
  private @Nullable ExtendedCommunity _vrfTargetExport;
  private @Nullable String _vrfImportPolicy;
  private @Nullable Boolean _vrfPropagateTtl;
  private boolean _vrfTableLabel;
  private boolean _vrfTableLabelSourceClassUsage;

  public RoutingInstance(@Nonnull String name) {
    _aggregateRouteDefaults = initAggregateRouteDefaults();
    _appliedRibGroups = new EnumMap<>(RoutingProtocol.class);
    _confederationMembers = new TreeSet<>();
    _dhcpRelayGroups = new TreeMap<>();
    _dhcpRelayServerGroups = new TreeMap<>();
    _generatedRouteDefaults = initGeneratedRouteDefaults();
    _isisSettings = new IsisSettings();
    _instanceExports = new LinkedList<>();
    _instanceImports = new LinkedList<>();
    _interfaces = new TreeMap<>();
    _ipBgpGroups = new TreeMap<>();
    _masterBgpGroup = new BgpGroup();
    _masterBgpGroup.setMultipath(false);
    _masterBgpGroup.setMultipathMultipleAs(false);
    _globalMasterInterface = new Interface(MASTER_INTERFACE_NAME);
    _globalMasterInterface.setRoutingInstance(this);
    _name = name;
    _namedBgpGroups = new TreeMap<>();
    _nodeDevices = new TreeMap<>();
    _ospf3RibGroups = new TreeMap<>();
    _ospfAreas = new TreeMap<>();
    _ospfExportPolicies = new LinkedList<>();
    _ospfOverloadedTopologies = new TreeSet<>();
    _ospfReferenceBandwidth = DEFAULT_OSPF_REFERENCE_BANDWIDTH;
    _ospfRibGroups = new TreeMap<>();
    _ribs = new TreeMap<>();
    _ribs.put(
        RoutingInformationBase.RIB_IPV4_UNICAST,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_UNICAST));
    _ribs.put(
        RoutingInformationBase.RIB_IPV4_MULTICAST,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_MULTICAST));
    _ribs.put(
        RoutingInformationBase.RIB_IPV4_MPLS,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV4_MPLS));
    _ribs.put(
        RoutingInformationBase.RIB_IPV6_UNICAST,
        new RoutingInformationBase(RoutingInformationBase.RIB_IPV6_UNICAST));
    _ribs.put(
        RoutingInformationBase.RIB_MPLS,
        new RoutingInformationBase(RoutingInformationBase.RIB_MPLS));
    _ribs.put(
        RoutingInformationBase.RIB_ISIS,
        new RoutingInformationBase(RoutingInformationBase.RIB_ISIS));
    _system = new JuniperSystem();
    _bridgeDomains = ImmutableMap.of();
  }

  public @Nullable Long getAs() {
    return _as;
  }

  public @Nonnull Map<RoutingProtocol, String> getAppliedRibGroups() {
    return _appliedRibGroups;
  }

  public boolean getBgpAlwaysCompareMed() {
    return _bgpAlwaysCompareMed;
  }

  public boolean getBgpExternalRouterId() {
    return _bgpExternalRouterId;
  }

  public boolean getBgpMedPlusIgp() {
    return _bgpMedPlusIgp;
  }

  public @Nullable Integer getBgpMedPlusIgpIgpMultiplier() {
    return _bgpMedPlusIgpIgpMultiplier;
  }

  public @Nullable Integer getBgpMedPlusIgpMedMultiplier() {
    return _bgpMedPlusIgpMedMultiplier;
  }

  public void applyRibGroup(RoutingProtocol protocol, String ribGroupName) {
    _appliedRibGroups.put(protocol, ribGroupName);
  }

  public SortedMap<String, DhcpRelayGroup> getDhcpRelayGroups() {
    return _dhcpRelayGroups;
  }

  public SortedMap<String, DhcpRelayServerGroup> getDhcpRelayServerGroups() {
    return _dhcpRelayServerGroups;
  }

  public String getDomainName() {
    return _domainName;
  }

  public boolean getExportLocalRoutesLan() {
    return _exportLocalRoutesLan;
  }

  public boolean getExportLocalRoutesPointToPoint() {
    return _exportLocalRoutesPointToPoint;
  }

  public String getForwardingTableExportPolicy() {
    return _forwardingTableExportPolicy;
  }

  public Interface getGlobalMasterInterface() {
    return _globalMasterInterface;
  }

  /**
   * Returns OSPF settings configured for "interface all" in this routing instance.
   *
   * <p>Returns null if "interface all" wasn't used in the configuration.
   */
  public @Nullable OspfInterfaceSettings getInterfaceAllOspfSettings() {
    return _interfaceAllOspfSettings;
  }

  /**
   * Returns ISIS settings configured for "interface all" in this routing instance.
   *
   * <p>Returns null if "interface all" wasn't used in the configuration.
   */
  public @Nullable IsisInterfaceSettings getInterfaceAllIsisSettings() {
    return _interfaceAllIsisSettings;
  }

  public String getHostname() {
    return _hostname;
  }

  public List<String> getInstanceExports() {
    return _instanceExports;
  }

  public List<String> getInstanceImports() {
    return _instanceImports;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public boolean getIndependentDomain() {
    return _independentDomain;
  }

  public boolean getIndependentDomainNoAttrset() {
    return _independentDomainNoAttrset;
  }

  public Map<Prefix, IpBgpGroup> getIpBgpGroups() {
    return _ipBgpGroups;
  }

  public @Nonnull IsisSettings getIsisSettings() {
    return _isisSettings;
  }

  public @Nullable Integer getLoops() {
    return _loops;
  }

  public BgpGroup getMasterBgpGroup() {
    return _masterBgpGroup;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public Map<String, NamedBgpGroup> getNamedBgpGroups() {
    return _namedBgpGroups;
  }

  public Map<String, NodeDevice> getNodeDevices() {
    return _nodeDevices;
  }

  public @Nullable Long getOspf3DomainVpnTag() {
    return _ospf3DomainVpnTag;
  }

  public Map<String, String> getOspf3RibGroups() {
    return _ospf3RibGroups;
  }

  public Map<Long, OspfArea> getOspfAreas() {
    return _ospfAreas;
  }

  public @Nullable Long getOspfDomainVpnTag() {
    return _ospfDomainVpnTag;
  }

  public List<String> getOspfExportPolicies() {
    return _ospfExportPolicies;
  }

  public @Nonnull Set<String> getOspfOverloadedTopologies() {
    return _ospfOverloadedTopologies;
  }

  public @Nullable Boolean getOspfDisable() {
    return _ospfDisable;
  }

  public @Nullable Long getOspfExternalPreference() {
    return _ospfExternalPreference;
  }

  public @Nullable Long getOspfPreference() {
    return _ospfPreference;
  }

  public double getOspfReferenceBandwidth() {
    return _ospfReferenceBandwidth;
  }

  public Map<String, String> getOspfRibGroups() {
    return _ospfRibGroups;
  }

  public Map<String, RoutingInformationBase> getRibs() {
    return _ribs;
  }

  public @Nullable Boolean getVrfPropagateTtl() {
    return _vrfPropagateTtl;
  }

  public Ip getRouterId() {
    return _routerId;
  }

  public SnmpServer getSnmpServer() {
    return _snmpServer;
  }

  public JuniperSystem getSystem() {
    return _system;
  }

  public void setAs(@Nullable Long as) {
    _as = as;
  }

  public void setDomainName(String domainName) {
    _domainName = domainName;
  }

  public void setExportLocalRoutesLan(boolean exportLocalRoutesLan) {
    _exportLocalRoutesLan = exportLocalRoutesLan;
  }

  public void setExportLocalRoutesPointToPoint(boolean exportLocalRoutesPointToPoint) {
    _exportLocalRoutesPointToPoint = exportLocalRoutesPointToPoint;
  }

  public void setForwardingTableExportPolicy(String forwardingTableExportPolicy) {
    _forwardingTableExportPolicy = forwardingTableExportPolicy;
  }

  public void setBgpAlwaysCompareMed(boolean bgpAlwaysCompareMed) {
    _bgpAlwaysCompareMed = bgpAlwaysCompareMed;
  }

  public void setBgpExternalRouterId(boolean bgpExternalRouterId) {
    _bgpExternalRouterId = bgpExternalRouterId;
  }

  public void setBgpMedPlusIgp(boolean bgpMedPlusIgp) {
    _bgpMedPlusIgp = bgpMedPlusIgp;
  }

  public void setBgpMedPlusIgpIgpMultiplier(int bgpMedPlusIgpIgpMultiplier) {
    _bgpMedPlusIgpIgpMultiplier = bgpMedPlusIgpIgpMultiplier;
  }

  public void setBgpMedPlusIgpMedMultiplier(int bgpMedPlusIgpMedMultiplier) {
    _bgpMedPlusIgpMedMultiplier = bgpMedPlusIgpMedMultiplier;
  }

  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  public void setIndependentDomain(boolean independentDomain) {
    _independentDomain = independentDomain;
  }

  public void setIndependentDomainNoAttrset(boolean independentDomainNoAttrset) {
    _independentDomainNoAttrset = independentDomainNoAttrset;
  }

  /** Sets the OSPF settings configures for "interface all" */
  public void setInterfaceAllOspfSettings(OspfInterfaceSettings interfaceAllOspfSettings) {
    _interfaceAllOspfSettings = interfaceAllOspfSettings;
  }

  /** Sets the ISIS settings configures for "interface all" */
  public void setInterfaceAllIsisSettings(IsisInterfaceSettings interfaceAllIsisSettings) {
    _interfaceAllIsisSettings = interfaceAllIsisSettings;
  }

  public void setOspf3DomainVpnTag(@Nullable Long ospf3DomainVpnTag) {
    _ospf3DomainVpnTag = ospf3DomainVpnTag;
  }

  public void setOspf3RibGroup(String family, String ribGroup) {
    _ospf3RibGroups.put(family, ribGroup);
  }

  public void setOspfDisable(boolean ospfDisable) {
    _ospfDisable = ospfDisable;
  }

  public void setOspfDomainVpnTag(@Nullable Long ospfDomainVpnTag) {
    _ospfDomainVpnTag = ospfDomainVpnTag;
  }

  public void setOspfExternalPreference(long ospfExternalPreference) {
    _ospfExternalPreference = ospfExternalPreference;
  }

  public void setOspfPreference(long ospfPreference) {
    _ospfPreference = ospfPreference;
  }

  public void setOspfReferenceBandwidth(double ospfReferenceBandwidth) {
    _ospfReferenceBandwidth = ospfReferenceBandwidth;
  }

  public void setOspfRibGroup(String family, String ribGroup) {
    _ospfRibGroups.put(family, ribGroup);
  }

  public void setLoops(@Nullable Integer loops) {
    _loops = loops;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  public void setSnmpServer(SnmpServer snmpServer) {
    _snmpServer = snmpServer;
  }

  public void setVrfPropagateTtl(boolean vrfPropagateTtl) {
    _vrfPropagateTtl = vrfPropagateTtl;
  }

  public void setConfederation(@Nullable Long confederation) {
    _confederation = confederation;
  }

  public @Nullable Long getConfederation() {
    return _confederation;
  }

  public @Nonnull Set<Long> getConfederationMembers() {
    return _confederationMembers;
  }

  public AggregateRoute getAggregateRouteDefaults() {
    return _aggregateRouteDefaults;
  }

  public GeneratedRoute getGeneratedRouteDefaults() {
    return _generatedRouteDefaults;
  }

  /** Helper to initialize aggregated/generated route defaults, which happen to be the same */
  private static void initAbstractAggregateRouteDefaults(@Nonnull AbstractAggregateRoute route) {
    route.setActive(true);
    route.setAsPath(null);
    route.setMetric(AggregateRoute.DEFAULT_AGGREGATE_ROUTE_COST);
    route.setPreference(AggregateRoute.DEFAULT_AGGREGATE_ROUTE_PREFERENCE);
  }

  /**
   * Initialize defaults for aggregate routes and return in an {@link AggregateRoute} whose fields
   * can be inherited.
   */
  private static @Nonnull AggregateRoute initAggregateRouteDefaults() {
    AggregateRoute route = new AggregateRoute(Prefix.ZERO);
    initAbstractAggregateRouteDefaults(route);
    return route;
  }

  /**
   * Initialize defaults for generated routes and return in a {@link GeneratedRoute} whose fields
   * can be inherited.
   */
  private static @Nonnull GeneratedRoute initGeneratedRouteDefaults() {
    GeneratedRoute route = new GeneratedRoute(Prefix.ZERO);
    initAbstractAggregateRouteDefaults(route);
    return route;
  }

  public @Nullable Resolution getResolution() {
    return _resolution;
  }

  public @Nonnull Resolution getOrCreateResolution() {
    if (_resolution == null) {
      _resolution = new Resolution();
    }
    return _resolution;
  }

  public @Nonnull Map<String, BridgeDomain> getBridgeDomains() {
    return _bridgeDomains;
  }

  public @Nonnull BridgeDomain getOrAddBridgeDomain(String name) {
    BridgeDomain bd = _bridgeDomains.get(name);
    if (bd == null) {
      bd = new BridgeDomain();
      _bridgeDomains =
          ImmutableMap.<String, BridgeDomain>builderWithExpectedSize(_bridgeDomains.size() + 1)
              .putAll(_bridgeDomains)
              .put(name, bd)
              .build();
    }
    return bd;
  }

  public @Nullable EvpnIpPrefixRoutes getEvpnIpPrefixRoutes() {
    return _evpnIpPrefixRoutes;
  }

  public @Nonnull EvpnIpPrefixRoutes getOrCreateEvpnIpPrefixRoutes() {
    if (_evpnIpPrefixRoutes == null) {
      _evpnIpPrefixRoutes = new EvpnIpPrefixRoutes();
    }
    return _evpnIpPrefixRoutes;
  }

  public void setEvpnIpPrefixRoutes(@Nullable EvpnIpPrefixRoutes evpnIpPrefixRoutes) {
    _evpnIpPrefixRoutes = evpnIpPrefixRoutes;
  }

  public @Nullable RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setRouteDistinguisher(@Nullable RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }

  public @Nullable Ip getRouteDistinguisherId() {
    return _routeDistinguisherId;
  }

  public void setRouteDistinguisherId(@Nullable Ip routeDistinguisherId) {
    _routeDistinguisherId = routeDistinguisherId;
  }

  public @Nullable ExtendedCommunity getVrfTargetCommunity() {
    return _vrfTargetCommunity;
  }

  public void setVrfTargetCommunity(@Nullable ExtendedCommunity vrfTargetCommunity) {
    _vrfTargetCommunity = vrfTargetCommunity;
  }

  public @Nullable ExtendedCommunity getVrfTargetImport() {
    return _vrfTargetImport;
  }

  public void setVrfTargetImport(@Nullable ExtendedCommunity vrfTargetImport) {
    _vrfTargetImport = vrfTargetImport;
  }

  public @Nullable ExtendedCommunity getVrfTargetExport() {
    return _vrfTargetExport;
  }

  public void setVrfTargetExport(@Nullable ExtendedCommunity vrfTargetExport) {
    _vrfTargetExport = vrfTargetExport;
  }

  public @Nullable String getVrfImportPolicy() {
    return _vrfImportPolicy;
  }

  public void setVrfImportPolicy(@Nullable String vrfImportPolicy) {
    _vrfImportPolicy = vrfImportPolicy;
  }

  public boolean getVrfTableLabel() {
    return _vrfTableLabel;
  }

  public void setVrfTableLabel(boolean vrfTableLabel) {
    _vrfTableLabel = vrfTableLabel;
  }

  public boolean getVrfTableLabelSourceClassUsage() {
    return _vrfTableLabelSourceClassUsage;
  }

  public void setVrfTableLabelSourceClassUsage(boolean vrfTableLabelSourceClassUsage) {
    _vrfTableLabelSourceClassUsage = vrfTableLabelSourceClassUsage;
  }
}
