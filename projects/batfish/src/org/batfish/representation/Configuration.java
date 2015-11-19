package org.batfish.representation;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.batfish.collections.RoleSet;
import org.batfish.main.ConfigurationFormat;
import org.batfish.util.NamedStructure;

public final class Configuration extends NamedStructure {

   private static final long serialVersionUID = 1L;

   private final Set<GeneratedRoute> _aggregateRoutes;

   private final Map<String, AsPathAccessList> _asPathAccessLists;

   private BgpProcess _bgpProcess;

   private final Set<Long> _communities;

   private final Map<String, CommunityList> _communityLists;

   private final Set<ConnectedRoute> _connectedRoutes;

   private final Map<String, IkeGateway> _ikeGateways;

   private final Map<String, IkePolicy> _ikePolicies;

   private final Map<String, IkeProposal> _ikeProposals;

   private final Map<String, Interface> _interfaces;

   private final Map<String, IpAccessList> _ipAccessLists;

   private final Map<String, IpsecPolicy> _ipsecPolicies;

   private final Map<String, IpsecProposal> _ipsecProposals;

   private final Map<String, IpsecVpn> _ipsecVpns;

   private IsisProcess _isisProcess;

   private OspfProcess _ospfProcess;

   private final Map<String, PolicyMap> _policyMaps;

   private RoleSet _roles;

   private final Map<String, RouteFilterList> _routeFilterLists;

   private final Set<StaticRoute> _staticRoutes;

   private ConfigurationFormat _vendor;

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
   }

   public Map<String, AsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
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

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _aggregateRoutes;
   }

   public String getHostname() {
      return _name;
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

   public OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public Map<String, PolicyMap> getPolicyMaps() {
      return _policyMaps;
   }

   public RoleSet getRoles() {
      return _roles;
   }

   public Map<String, RouteFilterList> getRouteFilterLists() {
      return _routeFilterLists;
   }

   public Set<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public ConfigurationFormat getVendor() {
      return _vendor;
   }

   public void setBgpProcess(BgpProcess process) {
      _bgpProcess = process;
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

}
