package org.batfish.datamodel;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BfJson;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.ConfigurationFormat;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Configuration extends ComparableStructure<String> {

   private static final String AS_PATH_ACCESS_LISTS_VAR = "asPathAccessLists";

   private static final String BGP_PROCESS_VAR = "bgpProcess";

   private static final String COMMUNITIES_VAR = "communities";

   private static final String COMMUNITY_LISTS_VAR = "communityLists";

   private static final String CONFIGURATION_FORMAT_VAR = "configurationFormat";

   private static final String DEFAULT_CROSS_ZONE_ACTION_VAR = "defaultCrossZoneAction";

   private static final String DEFAULT_INBOUND_ACTION_VAR = "defaultInboundAction";

   private static final String GENERATED_ROUTES_VAR = "aggregateRoutes";

   private static final String IKE_GATEWAYS_VAR = "ikeGateways";

   private static final String IKE_POLICIES_VAR = "ikePolicies";

   private static final String IKE_PROPOSALS_VAR = "ikeProposals";

   private static final String INTERFACES_VAR = "interfaces";

   private static final String IP_ACCESS_LISTS_VAR = "ipAccessLists";

   private static final String IPSEC_POLICIES_VAR = "ipsecPolicies";

   private static final String IPSEC_PROPOSALS_VAR = "ipsecProposals";

   private static final String IPSEC_VPNS_VAR = "ipsecVpns";

   private static final String ISIS_PROCESS_VAR = "isisProcess";

   public static final String NODE_NONE_NAME = "(none)";

   private static final String OSPF_PROCESS_VAR = "ospfProcess";

   private static final String POLICY_MAPS_VAR = "policyMaps";

   private static final String ROLES_VAR = "roles";

   private static final String ROUTE_FILTER_LISTS_VAR = "routeFilterLists";

   private static final long serialVersionUID = 1L;

   private static final String STATIC_ROUTES_VAR = "staticRoutes";

   private static final String ZONES_VAR = "zones";

   private final NavigableMap<String, AsPathAccessList> _asPathAccessLists;

   private transient Set<BgpAdvertisement> _bgpAdvertisements;

   private BgpProcess _bgpProcess;

   private final NavigableSet<Long> _communities;

   private final NavigableMap<String, CommunityList> _communityLists;

   private ConfigurationFormat _configurationFormat;

   private LineAction _defaultCrossZoneAction;

   private LineAction _defaultInboundAction;

   private final NavigableSet<GeneratedRoute> _generatedRoutes;

   private final NavigableMap<String, IkeGateway> _ikeGateways;

   private final NavigableMap<String, IkePolicy> _ikePolicies;

   private final NavigableMap<String, IkeProposal> _ikeProposals;

   private final NavigableMap<String, Interface> _interfaces;

   private final NavigableMap<String, IpAccessList> _ipAccessLists;

   private final NavigableMap<String, IpsecPolicy> _ipsecPolicies;

   private final NavigableMap<String, IpsecProposal> _ipsecProposals;

   private final NavigableMap<String, IpsecVpn> _ipsecVpns;

   private IsisProcess _isisProcess;

   private transient Set<BgpAdvertisement> _originatedAdvertisements;

   private transient Set<BgpAdvertisement> _originatedEbgpAdvertisements;

   private transient Set<BgpAdvertisement> _originatedIbgpAdvertisements;

   private OspfProcess _ospfProcess;

   private final NavigableMap<String, PolicyMap> _policyMaps;

   private transient Set<BgpAdvertisement> _receivedAdvertisements;

   private transient Set<BgpAdvertisement> _receivedEbgpAdvertisements;

   private transient Set<BgpAdvertisement> _receivedIbgpAdvertisements;

   private RoleSet _roles;

   private final NavigableMap<String, RouteFilterList> _routeFilterLists;

   private transient Set<PrecomputedRoute> _routes;

   private transient Set<BgpAdvertisement> _sentAdvertisements;

   private transient Set<BgpAdvertisement> _sentEbgpAdvertisements;

   private transient Set<BgpAdvertisement> _sentIbgpAdvertisements;

   private final NavigableSet<StaticRoute> _staticRoutes;

   private final NavigableMap<String, Zone> _zones;

   @JsonCreator
   public Configuration(
         @JsonProperty(AS_PATH_ACCESS_LISTS_VAR) NavigableMap<String, AsPathAccessList> asPathAccessLists,
         @JsonProperty(BGP_PROCESS_VAR) BgpProcess bgpProcess,
         @JsonProperty(COMMUNITIES_VAR) NavigableSet<Long> communities,
         @JsonProperty(COMMUNITY_LISTS_VAR) NavigableMap<String, CommunityList> communityLists,
         @JsonProperty(CONFIGURATION_FORMAT_VAR) ConfigurationFormat configurationFormat,
         @JsonProperty(DEFAULT_CROSS_ZONE_ACTION_VAR) LineAction defaultCrossZoneAction,
         @JsonProperty(DEFAULT_INBOUND_ACTION_VAR) LineAction defaultInboundAction,
         @JsonProperty(GENERATED_ROUTES_VAR) NavigableSet<GeneratedRoute> generatedRoutes,
         @JsonProperty(NAME_VAR) String hostname,
         @JsonProperty(IKE_GATEWAYS_VAR) NavigableMap<String, IkeGateway> ikeGateways,
         @JsonProperty(IKE_POLICIES_VAR) NavigableMap<String, IkePolicy> ikePolicies,
         @JsonProperty(IKE_PROPOSALS_VAR) NavigableMap<String, IkeProposal> ikeProposals,
         @JsonProperty(INTERFACES_VAR) NavigableMap<String, Interface> interfaces,
         @JsonProperty(IP_ACCESS_LISTS_VAR) NavigableMap<String, IpAccessList> ipAccessLists,
         @JsonProperty(IPSEC_POLICIES_VAR) NavigableMap<String, IpsecPolicy> ipsecPolicies,
         @JsonProperty(IPSEC_PROPOSALS_VAR) NavigableMap<String, IpsecProposal> ipsecProposals,
         @JsonProperty(IPSEC_VPNS_VAR) NavigableMap<String, IpsecVpn> ipsecVpns,
         @JsonProperty(ISIS_PROCESS_VAR) IsisProcess isisProcess,
         @JsonProperty(OSPF_PROCESS_VAR) OspfProcess ospfProcess,
         @JsonProperty(POLICY_MAPS_VAR) NavigableMap<String, PolicyMap> policyMaps,
         @JsonProperty(ROLES_VAR) RoleSet roles,
         @JsonProperty(ROUTE_FILTER_LISTS_VAR) NavigableMap<String, RouteFilterList> routeFilterLists,
         @JsonProperty(STATIC_ROUTES_VAR) NavigableSet<StaticRoute> staticRoutes,
         @JsonProperty(ZONES_VAR) NavigableMap<String, Zone> zones) {
      super(hostname);
      _asPathAccessLists = asPathAccessLists;
      _bgpProcess = bgpProcess;
      _communities = communities;
      _communityLists = communityLists;
      _configurationFormat = configurationFormat;
      _defaultCrossZoneAction = defaultCrossZoneAction;
      _defaultInboundAction = defaultInboundAction;
      _generatedRoutes = generatedRoutes;
      _ikeGateways = ikeGateways;
      _ikePolicies = ikePolicies;
      _ikeProposals = ikeProposals;
      _interfaces = interfaces;
      _ipAccessLists = ipAccessLists;
      _ipsecPolicies = ipsecPolicies;
      _ipsecProposals = ipsecProposals;
      _ipsecVpns = ipsecVpns;
      _isisProcess = isisProcess;
      _ospfProcess = ospfProcess;
      _policyMaps = policyMaps;
      _roles = roles;
      _routeFilterLists = routeFilterLists;
      _staticRoutes = staticRoutes;
      _zones = zones;
   }

   public Configuration(String hostname) {
      super(hostname);
      _asPathAccessLists = new TreeMap<String, AsPathAccessList>();
      _communities = new TreeSet<Long>();
      _communityLists = new TreeMap<String, CommunityList>();
      _generatedRoutes = new TreeSet<GeneratedRoute>();
      _ikeGateways = new TreeMap<String, IkeGateway>();
      _ikePolicies = new TreeMap<String, IkePolicy>();
      _ikeProposals = new TreeMap<String, IkeProposal>();
      _interfaces = new TreeMap<String, Interface>();
      _ipAccessLists = new TreeMap<String, IpAccessList>();
      _ipsecPolicies = new TreeMap<String, IpsecPolicy>();
      _ipsecProposals = new TreeMap<String, IpsecProposal>();
      _ipsecVpns = new TreeMap<String, IpsecVpn>();
      _policyMaps = new TreeMap<String, PolicyMap>();
      _roles = new RoleSet();
      _routeFilterLists = new TreeMap<String, RouteFilterList>();
      _staticRoutes = new TreeSet<StaticRoute>();
      _zones = new TreeMap<String, Zone>();
   }

   @JsonProperty(AS_PATH_ACCESS_LISTS_VAR)
   public NavigableMap<String, AsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
   }

   @JsonIgnore
   public Set<BgpAdvertisement> getBgpAdvertisements() {
      return _bgpAdvertisements;
   }

   @JsonProperty(BGP_PROCESS_VAR)
   public BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   @JsonProperty(COMMUNITIES_VAR)
   public Set<Long> getCommunities() {
      return _communities;
   }

   @JsonProperty(COMMUNITY_LISTS_VAR)
   public NavigableMap<String, CommunityList> getCommunityLists() {
      return _communityLists;
   }

   @JsonProperty(CONFIGURATION_FORMAT_VAR)
   public ConfigurationFormat getConfigurationFormat() {
      return _configurationFormat;
   }

   @JsonProperty(DEFAULT_CROSS_ZONE_ACTION_VAR)
   public LineAction getDefaultCrossZoneAction() {
      return _defaultCrossZoneAction;
   }

   @JsonProperty(DEFAULT_INBOUND_ACTION_VAR)
   public LineAction getDefaultInboundAction() {
      return _defaultInboundAction;
   }

   @JsonProperty(GENERATED_ROUTES_VAR)
   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   @JsonProperty(NAME_VAR)
   public String getHostname() {
      return _key;
   }

   @JsonProperty(IKE_GATEWAYS_VAR)
   public NavigableMap<String, IkeGateway> getIkeGateways() {
      return _ikeGateways;
   }

   @JsonProperty(IKE_POLICIES_VAR)
   public NavigableMap<String, IkePolicy> getIkePolicies() {
      return _ikePolicies;
   }

   @JsonProperty(IKE_PROPOSALS_VAR)
   public NavigableMap<String, IkeProposal> getIkeProposals() {
      return _ikeProposals;
   }

   @JsonProperty(INTERFACES_VAR)
   public NavigableMap<String, Interface> getInterfaces() {
      return _interfaces;
   }

   @JsonProperty(IP_ACCESS_LISTS_VAR)
   public NavigableMap<String, IpAccessList> getIpAccessLists() {
      return _ipAccessLists;
   }

   @JsonProperty(IPSEC_POLICIES_VAR)
   public NavigableMap<String, IpsecPolicy> getIpsecPolicies() {
      return _ipsecPolicies;
   }

   @JsonProperty(IPSEC_PROPOSALS_VAR)
   public NavigableMap<String, IpsecProposal> getIpsecProposals() {
      return _ipsecProposals;
   }

   @JsonProperty(IPSEC_VPNS_VAR)
   public NavigableMap<String, IpsecVpn> getIpsecVpns() {
      return _ipsecVpns;
   }

   @JsonProperty(ISIS_PROCESS_VAR)
   public IsisProcess getIsisProcess() {
      return _isisProcess;
   }

   public Set<BgpAdvertisement> getOriginatedAdvertisements() {
      return _originatedAdvertisements;
   }

   public Set<BgpAdvertisement> getOriginatedEbgpAdvertisements() {
      return _originatedEbgpAdvertisements;
   }

   public Set<BgpAdvertisement> getOriginatedIbgpAdvertisements() {
      return _originatedIbgpAdvertisements;
   }

   @JsonProperty(OSPF_PROCESS_VAR)
   public OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   @JsonProperty(POLICY_MAPS_VAR)
   public NavigableMap<String, PolicyMap> getPolicyMaps() {
      return _policyMaps;
   }

   @JsonIgnore
   public Set<BgpAdvertisement> getReceivedAdvertisements() {
      return _receivedAdvertisements;
   }

   @JsonIgnore
   public Set<BgpAdvertisement> getReceivedEbgpAdvertisements() {
      return _receivedEbgpAdvertisements;
   }

   @JsonIgnore
   public Set<BgpAdvertisement> getReceivedIbgpAdvertisements() {
      return _receivedIbgpAdvertisements;
   }

   @JsonProperty(ROLES_VAR)
   public RoleSet getRoles() {
      return _roles;
   }

   @JsonProperty(ROUTE_FILTER_LISTS_VAR)
   public NavigableMap<String, RouteFilterList> getRouteFilterLists() {
      return _routeFilterLists;
   }

   public Set<PrecomputedRoute> getRoutes() {
      return _routes;
   }

   @JsonIgnore
   public Set<BgpAdvertisement> getSentAdvertisements() {
      return _sentAdvertisements;
   }

   @JsonIgnore
   public Set<BgpAdvertisement> getSentEbgpAdvertisements() {
      return _sentEbgpAdvertisements;
   }

   @JsonIgnore
   public Set<BgpAdvertisement> getSentIbgpAdvertisements() {
      return _sentIbgpAdvertisements;
   }

   @JsonProperty(STATIC_ROUTES_VAR)
   public Set<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   @JsonProperty(ZONES_VAR)
   public NavigableMap<String, Zone> getZones() {
      return _zones;
   }

   public void initBgpAdvertisements() {
      _bgpAdvertisements = new TreeSet<BgpAdvertisement>();
      _originatedAdvertisements = new TreeSet<BgpAdvertisement>();
      _originatedEbgpAdvertisements = new TreeSet<BgpAdvertisement>();
      _originatedIbgpAdvertisements = new TreeSet<BgpAdvertisement>();
      _receivedAdvertisements = new TreeSet<BgpAdvertisement>();
      _receivedEbgpAdvertisements = new TreeSet<BgpAdvertisement>();
      _receivedIbgpAdvertisements = new TreeSet<BgpAdvertisement>();
      _sentAdvertisements = new TreeSet<BgpAdvertisement>();
      _sentEbgpAdvertisements = new TreeSet<BgpAdvertisement>();
      _sentIbgpAdvertisements = new TreeSet<BgpAdvertisement>();
   }

   public void initRoutes() {
      _routes = new TreeSet<PrecomputedRoute>();
   }

   public void setBgpProcess(BgpProcess process) {
      _bgpProcess = process;
   }

   public void setConfigurationFormat(ConfigurationFormat configurationFormat) {
      _configurationFormat = configurationFormat;
   }

   public void setDefaultCrossZoneAction(LineAction defaultCrossZoneAction) {
      _defaultCrossZoneAction = defaultCrossZoneAction;
   }

   public void setDefaultInboundAction(LineAction defaultInboundAction) {
      _defaultInboundAction = defaultInboundAction;
   }

   public void setIsisProcess(IsisProcess process) {
      _isisProcess = process;
   }

   public void setOspfProcess(OspfProcess process) {
      _ospfProcess = process;
   }

   public void setRoles(RoleSet roles) {
      _roles = roles;
   }

   public JSONObject toJson() throws JSONException {
      JSONObject jObj = new JSONObject();
      jObj.put(BfJson.KEY_NODE_NAME, getHostname());
      return jObj;
   }
}
