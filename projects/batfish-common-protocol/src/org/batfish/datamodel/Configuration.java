package org.batfish.datamodel;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.batfish.common.BfJson;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.vendor_family.VendorFamily;
import org.batfish.datamodel.ConfigurationFormat;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("A Configuration represents an autonomous network device, such as a router, host, switch, or firewall.")
public final class Configuration extends ComparableStructure<String> {

   private static final String AS_PATH_ACCESS_LISTS_VAR = "asPathAccessLists";

   private static final String COMMUNITY_LISTS_VAR = "communityLists";

   private static final String CONFIGURATION_FORMAT_VAR = "configurationFormat";

   private static final String DEFAULT_CROSS_ZONE_ACTION_VAR = "defaultCrossZoneAction";

   private static final String DEFAULT_INBOUND_ACTION_VAR = "defaultInboundAction";

   public static final String DEFAULT_VRF_NAME = "default";

   private static final String IKE_GATEWAYS_VAR = "ikeGateways";

   private static final String IKE_POLICIES_VAR = "ikePolicies";

   private static final String IKE_PROPOSALS_VAR = "ikeProposals";

   private static final String IP_ACCESS_LISTS_VAR = "ipAccessLists";

   private static final String IPSEC_POLICIES_VAR = "ipsecPolicies";

   private static final String IPSEC_PROPOSALS_VAR = "ipsecProposals";

   private static final String IPSEC_VPNS_VAR = "ipsecVpns";

   public static final String NODE_NONE_NAME = "(none)";

   private static final String ROLES_VAR = "roles";

   private static final String ROUTE_FILTER_LISTS_VAR = "routeFilterLists";

   private static final String ROUTING_POLICIES_VAR = "routingPolicies";

   private static final long serialVersionUID = 1L;

   private static final String ZONES_VAR = "zones";

   private NavigableMap<String, AsPathAccessList> _asPathAccessLists;

   private transient NavigableSet<BgpAdvertisement> _bgpAdvertisements;

   private NavigableMap<String, CommunityList> _communityLists;

   private ConfigurationFormat _configurationFormat;

   private LineAction _defaultCrossZoneAction;

   private LineAction _defaultInboundAction;

   private String _domainName;

   private NavigableMap<String, IkeGateway> _ikeGateways;

   private NavigableMap<String, IkePolicy> _ikePolicies;

   private NavigableMap<String, IkeProposal> _ikeProposals;

   private NavigableMap<String, Interface> _interfaces;

   private NavigableMap<String, Ip6AccessList> _ip6AccessLists;

   private NavigableMap<String, IpAccessList> _ipAccessLists;

   private NavigableMap<String, IpsecPolicy> _ipsecPolicies;

   private NavigableMap<String, IpsecProposal> _ipsecProposals;

   private NavigableMap<String, IpsecVpn> _ipsecVpns;

   private transient NavigableSet<BgpAdvertisement> _originatedAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _originatedEbgpAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _originatedIbgpAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _receivedAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _receivedEbgpAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _receivedIbgpAdvertisements;

   private transient boolean _resolved;

   private RoleSet _roles;

   private NavigableMap<String, Route6FilterList> _route6FilterLists;

   private NavigableMap<String, RouteFilterList> _routeFilterLists;

   private transient NavigableSet<Route> _routes;

   private NavigableMap<String, RoutingPolicy> _routingPolicies;

   private transient NavigableSet<BgpAdvertisement> _sentAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _sentEbgpAdvertisements;

   private transient NavigableSet<BgpAdvertisement> _sentIbgpAdvertisements;

   private VendorFamily _vendorFamily;

   private Map<String, Vrf> _vrfs;

   private NavigableMap<String, Zone> _zones;

   @JsonCreator
   public Configuration(@JsonProperty(NAME_VAR) String hostname) {
      super(hostname);
      _asPathAccessLists = new TreeMap<>();
      _communityLists = new TreeMap<>();
      _ikeGateways = new TreeMap<>();
      _ikePolicies = new TreeMap<>();
      _ikeProposals = new TreeMap<>();
      _interfaces = new TreeMap<>();
      _ipAccessLists = new TreeMap<>();
      _ip6AccessLists = new TreeMap<>();
      _ipsecPolicies = new TreeMap<>();
      _ipsecProposals = new TreeMap<>();
      _ipsecVpns = new TreeMap<>();
      _roles = new RoleSet();
      _routeFilterLists = new TreeMap<>();
      _route6FilterLists = new TreeMap<>();
      _routingPolicies = new TreeMap<>();
      _vendorFamily = new VendorFamily();
      _vrfs = new TreeMap<>();
      _zones = new TreeMap<>();
   }

   @JsonProperty(AS_PATH_ACCESS_LISTS_VAR)
   @JsonPropertyDescription("Dictionary of all AS-path access-lists for this node.")
   public NavigableMap<String, AsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getBgpAdvertisements() {
      return _bgpAdvertisements;
   }

   @JsonProperty(COMMUNITY_LISTS_VAR)
   @JsonPropertyDescription("Dictionary of all community-lists for this node.")
   public NavigableMap<String, CommunityList> getCommunityLists() {
      return _communityLists;
   }

   @JsonProperty(CONFIGURATION_FORMAT_VAR)
   @JsonPropertyDescription("Best guess at vendor configuration format. Used for setting default values, protocol costs, etc.")
   public ConfigurationFormat getConfigurationFormat() {
      return _configurationFormat;
   }

   @JsonProperty(DEFAULT_CROSS_ZONE_ACTION_VAR)
   @JsonPropertyDescription("Default forwarding action to take for traffic that crosses firewall zones.")
   public LineAction getDefaultCrossZoneAction() {
      return _defaultCrossZoneAction;
   }

   @JsonProperty(DEFAULT_INBOUND_ACTION_VAR)
   @JsonPropertyDescription("Default forwarding action to take for traffic destined for this device.")
   public LineAction getDefaultInboundAction() {
      return _defaultInboundAction;
   }

   @JsonIgnore
   public Vrf getDefaultVrf() {
      return _vrfs.get(DEFAULT_VRF_NAME);
   }

   @JsonPropertyDescription("Domain name of this node.")
   public String getDomainName() {
      return _domainName;
   }

   @JsonProperty(NAME_VAR)
   @JsonPropertyDescription("Hostname of this node.")
   public String getHostname() {
      return _key;
   }

   @JsonProperty(IKE_GATEWAYS_VAR)
   @JsonPropertyDescription("Dictionary of all IKE gateways for this node.")
   public NavigableMap<String, IkeGateway> getIkeGateways() {
      return _ikeGateways;
   }

   @JsonProperty(IKE_POLICIES_VAR)
   @JsonPropertyDescription("Dictionary of all IKE policies for this node.")
   public NavigableMap<String, IkePolicy> getIkePolicies() {
      return _ikePolicies;
   }

   @JsonProperty(IKE_PROPOSALS_VAR)
   @JsonPropertyDescription("Dictionary of all IKE proposals for this node.")
   public NavigableMap<String, IkeProposal> getIkeProposals() {
      return _ikeProposals;
   }

   @JsonPropertyDescription("Dictionary of all interfaces across all VRFs for this node.")
   public NavigableMap<String, Interface> getInterfaces() {
      return _interfaces;
   }

   @JsonPropertyDescription("Dictionary of all IPV6 access-lists for this node.")
   public NavigableMap<String, Ip6AccessList> getIp6AccessLists() {
      return _ip6AccessLists;
   }

   @JsonProperty(IP_ACCESS_LISTS_VAR)
   @JsonPropertyDescription("Dictionary of all IPV4 access-lists for this node.")
   public NavigableMap<String, IpAccessList> getIpAccessLists() {
      return _ipAccessLists;
   }

   @JsonProperty(IPSEC_POLICIES_VAR)
   @JsonPropertyDescription("Dictionary of all IPSEC policies for this node.")
   public NavigableMap<String, IpsecPolicy> getIpsecPolicies() {
      return _ipsecPolicies;
   }

   @JsonProperty(IPSEC_PROPOSALS_VAR)
   @JsonPropertyDescription("Dictionary of all IPSEC proposals for this node.")
   public NavigableMap<String, IpsecProposal> getIpsecProposals() {
      return _ipsecProposals;
   }

   @JsonProperty(IPSEC_VPNS_VAR)
   @JsonPropertyDescription("Dictionary of all IPSEC VPNs for this node.")
   public NavigableMap<String, IpsecVpn> getIpsecVpns() {
      return _ipsecVpns;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getOriginatedAdvertisements() {
      return _originatedAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getOriginatedEbgpAdvertisements() {
      return _originatedEbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getOriginatedIbgpAdvertisements() {
      return _originatedIbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getReceivedAdvertisements() {
      return _receivedAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getReceivedEbgpAdvertisements() {
      return _receivedEbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getReceivedIbgpAdvertisements() {
      return _receivedIbgpAdvertisements;
   }

   @JsonProperty(ROLES_VAR)
   @JsonPropertyDescription("Set of all roles in which this node serves.")
   public RoleSet getRoles() {
      return _roles;
   }

   @JsonPropertyDescription("Dictionary of all IPV6 route filter lists for this node.")
   public NavigableMap<String, Route6FilterList> getRoute6FilterLists() {
      return _route6FilterLists;
   }

   @JsonProperty(ROUTE_FILTER_LISTS_VAR)
   @JsonPropertyDescription("Dictionary of all IPV4 route filter lists for this node.")
   public NavigableMap<String, RouteFilterList> getRouteFilterLists() {
      return _routeFilterLists;
   }

   @JsonIgnore
   public NavigableSet<Route> getRoutes() {
      return _routes;
   }

   @JsonProperty(ROUTING_POLICIES_VAR)
   @JsonPropertyDescription("Dictionary of all routing policies for this node.")
   public NavigableMap<String, RoutingPolicy> getRoutingPolicies() {
      return _routingPolicies;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getSentAdvertisements() {
      return _sentAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getSentEbgpAdvertisements() {
      return _sentEbgpAdvertisements;
   }

   @JsonIgnore
   public NavigableSet<BgpAdvertisement> getSentIbgpAdvertisements() {
      return _sentIbgpAdvertisements;
   }

   @JsonPropertyDescription("Object containing vendor-specific information for this node.")
   public VendorFamily getVendorFamily() {
      return _vendorFamily;
   }

   @JsonPropertyDescription("Dictionary of all VRFs for this node.")
   public Map<String, Vrf> getVrfs() {
      return _vrfs;
   }

   @JsonProperty(ZONES_VAR)
   @JsonPropertyDescription("Dictionary of all firewall zones for this node.")
   public NavigableMap<String, Zone> getZones() {
      return _zones;
   }

   public void initBgpAdvertisements() {
      _bgpAdvertisements = new TreeSet<>();
      _originatedAdvertisements = new TreeSet<>();
      _originatedEbgpAdvertisements = new TreeSet<>();
      _originatedIbgpAdvertisements = new TreeSet<>();
      _receivedAdvertisements = new TreeSet<>();
      _receivedEbgpAdvertisements = new TreeSet<>();
      _receivedIbgpAdvertisements = new TreeSet<>();
      _sentAdvertisements = new TreeSet<>();
      _sentEbgpAdvertisements = new TreeSet<>();
      _sentIbgpAdvertisements = new TreeSet<>();
      for (Vrf vrf : _vrfs.values()) {
         vrf.initBgpAdvertisements();
      }
   }

   public void initRoutes() {
      _routes = new TreeSet<>();
      for (Vrf vrf : _vrfs.values()) {
         vrf.initRoutes();
      }
   }

   public void resolveReferences() {
      if (_resolved) {
         return;
      }
      _resolved = true;
      for (IkeGateway gateway : _ikeGateways.values()) {
         gateway.resolveReferences(this);
      }
      for (IkePolicy ikePolicy : _ikePolicies.values()) {
         ikePolicy.resolveReferences(this);
      }
      for (Interface iface : _interfaces.values()) {
         iface.resolveReferences(this);
      }
      for (IpsecPolicy ipsecPolicy : _ipsecPolicies.values()) {
         ipsecPolicy.resolveReferences(this);
      }
      for (IpsecVpn ipsecVpn : _ipsecVpns.values()) {
         ipsecVpn.resolveReferences(this);
      }
      for (Vrf vrf : _vrfs.values()) {
         vrf.resolveReferences(this);
         BgpProcess bgpProc = vrf.getBgpProcess();
         if (bgpProc != null) {
            for (BgpNeighbor neighbor : bgpProc.getNeighbors().values()) {
               neighbor.resolveReferences(this);
            }
         }
         OspfProcess ospfProc = vrf.getOspfProcess();
         if (ospfProc != null) {
            for (OspfArea area : ospfProc.getAreas().values()) {
               area.resolveReferences(this);
            }
         }
      }
   }

   @JsonProperty(AS_PATH_ACCESS_LISTS_VAR)
   public void setAsPathAccessLists(
         NavigableMap<String, AsPathAccessList> asPathAccessLists) {
      _asPathAccessLists = asPathAccessLists;
   }

   @JsonProperty(COMMUNITY_LISTS_VAR)
   public void setCommunityLists(
         NavigableMap<String, CommunityList> communityLists) {
      _communityLists = communityLists;
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

   public void setDomainName(String domainName) {
      _domainName = domainName;
   }

   @JsonProperty(IKE_GATEWAYS_VAR)
   public void setIkeGateways(NavigableMap<String, IkeGateway> ikeGateways) {
      _ikeGateways = ikeGateways;
   }

   @JsonProperty(IKE_POLICIES_VAR)
   public void setIkePolicies(NavigableMap<String, IkePolicy> ikePolicies) {
      _ikePolicies = ikePolicies;
   }

   @JsonProperty(IKE_PROPOSALS_VAR)
   public void setIkeProposals(NavigableMap<String, IkeProposal> ikeProposals) {
      _ikeProposals = ikeProposals;
   }

   public void setInterfaces(NavigableMap<String, Interface> interfaces) {
      _interfaces = interfaces;
   }

   public void setIp6AccessLists(
         NavigableMap<String, Ip6AccessList> ip6AccessLists) {
      _ip6AccessLists = ip6AccessLists;
   }

   @JsonProperty(IP_ACCESS_LISTS_VAR)
   public void setIpAccessLists(
         NavigableMap<String, IpAccessList> ipAccessLists) {
      _ipAccessLists = ipAccessLists;
   }

   @JsonProperty(IPSEC_POLICIES_VAR)
   public void setIpsecPolicies(
         NavigableMap<String, IpsecPolicy> ipsecPolicies) {
      _ipsecPolicies = ipsecPolicies;
   }

   @JsonProperty(IPSEC_PROPOSALS_VAR)
   public void setIpsecProposals(
         NavigableMap<String, IpsecProposal> ipsecProposals) {
      _ipsecProposals = ipsecProposals;
   }

   @JsonProperty(IPSEC_VPNS_VAR)
   public void setIpsecVpns(NavigableMap<String, IpsecVpn> ipsecVpns) {
      _ipsecVpns = ipsecVpns;
   }

   public void setRoles(RoleSet roles) {
      _roles = roles;
   }

   public void setRoute6FilterLists(
         NavigableMap<String, Route6FilterList> route6FilterLists) {
      _route6FilterLists = route6FilterLists;
   }

   @JsonProperty(ROUTE_FILTER_LISTS_VAR)
   public void setRouteFilterLists(
         NavigableMap<String, RouteFilterList> routeFilterLists) {
      _routeFilterLists = routeFilterLists;
   }

   @JsonProperty(ROUTING_POLICIES_VAR)
   public void setRoutingPolicies(
         NavigableMap<String, RoutingPolicy> routingPolicies) {
      _routingPolicies = routingPolicies;
   }

   public void setVendorFamily(VendorFamily vendorFamily) {
      _vendorFamily = vendorFamily;
   }

   public void setVrfs(Map<String, Vrf> vrfs) {
      _vrfs = vrfs;
   }

   @JsonProperty(ZONES_VAR)
   public void setZones(NavigableMap<String, Zone> zones) {
      _zones = zones;
   }

   public void simplifyRoutingPolicies() {
      NavigableMap<String, RoutingPolicy> simpleRoutingPolicies = new TreeMap<>();
      simpleRoutingPolicies.putAll(_routingPolicies.entrySet().stream()
            .collect(Collectors
                  .<Entry<String, RoutingPolicy>, String, RoutingPolicy> toMap(
                        e -> e.getKey(), e -> e.getValue().simplify())));
      _routingPolicies = simpleRoutingPolicies;
   }

   public JSONObject toJson() throws JSONException {
      JSONObject jObj = new JSONObject();
      jObj.put(BfJson.KEY_NODE_NAME, getHostname());
      return jObj;
   }

}
