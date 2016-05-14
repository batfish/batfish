package org.batfish.datamodel;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BfJson;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.ConfigurationFormat;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public final class Configuration extends ComparableStructure<String> {

   public static final String NODE_NONE_NAME = "(none)";

   private static final long serialVersionUID = 1L;

   private final Set<GeneratedRoute> _aggregateRoutes;

   private final Map<String, AsPathAccessList> _asPathAccessLists;

   private transient Set<BgpAdvertisement> _bgpAdvertisements;

   private BgpProcess _bgpProcess;

   private final Set<Long> _communities;

   private final Map<String, CommunityList> _communityLists;

   private final Set<ConnectedRoute> _connectedRoutes;

   private LineAction _defaultCrossZoneAction;

   private LineAction _defaultInboundAction;

   private final Map<String, IkeGateway> _ikeGateways;

   private final Map<String, IkePolicy> _ikePolicies;

   private final Map<String, IkeProposal> _ikeProposals;

   private final Map<String, Interface> _interfaces;

   private final Map<String, IpAccessList> _ipAccessLists;

   private final Map<String, IpsecPolicy> _ipsecPolicies;

   private final Map<String, IpsecProposal> _ipsecProposals;

   private final Map<String, IpsecVpn> _ipsecVpns;

   private IsisProcess _isisProcess;

   private transient Set<BgpAdvertisement> _originatedAdvertisements;

   private transient Set<BgpAdvertisement> _originatedEbgpAdvertisements;

   private transient Set<BgpAdvertisement> _originatedIbgpAdvertisements;

   private OspfProcess _ospfProcess;

   private final Map<String, PolicyMap> _policyMaps;

   private transient Set<BgpAdvertisement> _receivedAdvertisements;

   private transient Set<BgpAdvertisement> _receivedEbgpAdvertisements;

   private transient Set<BgpAdvertisement> _receivedIbgpAdvertisements;

   private RoleSet _roles;

   private final Map<String, RouteFilterList> _routeFilterLists;

   private transient Set<PrecomputedRoute> _routes;

   private transient Set<BgpAdvertisement> _sentAdvertisements;

   private transient Set<BgpAdvertisement> _sentEbgpAdvertisements;

   private transient Set<BgpAdvertisement> _sentIbgpAdvertisements;

   private final Set<StaticRoute> _staticRoutes;

   private ConfigurationFormat _vendor;

   private final Map<String, Zone> _zones;

   public Configuration(String hostname) {
      super(hostname);
      _aggregateRoutes = new LinkedHashSet<GeneratedRoute>();
      _asPathAccessLists = new HashMap<String, AsPathAccessList>();
      _communities = new LinkedHashSet<Long>();
      _communityLists = new HashMap<String, CommunityList>();
      _connectedRoutes = new LinkedHashSet<ConnectedRoute>();
      _ikeGateways = new TreeMap<String, IkeGateway>();
      _ikePolicies = new TreeMap<String, IkePolicy>();
      _ikeProposals = new TreeMap<String, IkeProposal>();
      _interfaces = new HashMap<String, Interface>();
      _ipAccessLists = new HashMap<String, IpAccessList>();
      _ipsecPolicies = new TreeMap<String, IpsecPolicy>();
      _ipsecProposals = new TreeMap<String, IpsecProposal>();
      _ipsecVpns = new TreeMap<String, IpsecVpn>();
      _policyMaps = new HashMap<String, PolicyMap>();
      _roles = new RoleSet();
      _routeFilterLists = new HashMap<String, RouteFilterList>();
      _staticRoutes = new LinkedHashSet<StaticRoute>();
      _zones = new TreeMap<String, Zone>();
   }

   public Map<String, AsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
   }

   public Set<BgpAdvertisement> getBgpAdvertisements() {
      return _bgpAdvertisements;
   }

   public BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   public Set<Long> getCommunities() {
      return _communities;
   }

   public Map<String, CommunityList> getCommunityLists() {
      return _communityLists;
   }

   public Set<ConnectedRoute> getConnectedRoutes() {
      return _connectedRoutes;
   }

   public LineAction getDefaultCrossZoneAction() {
      return _defaultCrossZoneAction;
   }

   public LineAction getDefaultInboundAction() {
      return _defaultInboundAction;
   }

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _aggregateRoutes;
   }

   public String getHostname() {
      return _key;
   }

   public Map<String, IkeGateway> getIkeGateways() {
      return _ikeGateways;
   }

   public Map<String, IkePolicy> getIkePolicies() {
      return _ikePolicies;
   }

   public Map<String, IkeProposal> getIkeProposals() {
      return _ikeProposals;
   }

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public Map<String, IpAccessList> getIpAccessLists() {
      return _ipAccessLists;
   }

   public Map<String, IpsecPolicy> getIpsecPolicies() {
      return _ipsecPolicies;
   }

   public Map<String, IpsecProposal> getIpsecProposals() {
      return _ipsecProposals;
   }

   public Map<String, IpsecVpn> getIpsecVpns() {
      return _ipsecVpns;
   }

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

   public OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public Map<String, PolicyMap> getPolicyMaps() {
      return _policyMaps;
   }

   public Set<BgpAdvertisement> getReceivedAdvertisements() {
      return _receivedAdvertisements;
   }

   public Set<BgpAdvertisement> getReceivedEbgpAdvertisements() {
      return _receivedEbgpAdvertisements;
   }

   public Set<BgpAdvertisement> getReceivedIbgpAdvertisements() {
      return _receivedIbgpAdvertisements;
   }

   public RoleSet getRoles() {
      return _roles;
   }

   public Map<String, RouteFilterList> getRouteFilterLists() {
      return _routeFilterLists;
   }

   public Set<PrecomputedRoute> getRoutes() {
      return _routes;
   }

   public Set<BgpAdvertisement> getSentAdvertisements() {
      return _sentAdvertisements;
   }

   public Set<BgpAdvertisement> getSentEbgpAdvertisements() {
      return _sentEbgpAdvertisements;
   }

   public Set<BgpAdvertisement> getSentIbgpAdvertisements() {
      return _sentIbgpAdvertisements;
   }

   public Set<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public ConfigurationFormat getVendor() {
      return _vendor;
   }

   public Map<String, Zone> getZones() {
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

   public void setVendor(ConfigurationFormat vendor) {
      _vendor = vendor;
   }

   public JSONObject toJson() throws JSONException {
      JSONObject jObj = new JSONObject();
      jObj.put(BfJson.KEY_NODE_NAME, getHostname());
      return jObj;
   }
}
