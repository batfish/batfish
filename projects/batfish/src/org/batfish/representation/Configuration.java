package org.batfish.representation;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.batfish.collections.RoleSet;
import org.batfish.util.NamedStructure;

public class Configuration extends NamedStructure {

   private static final long serialVersionUID = 1L;

   private Set<GeneratedRoute> _aggregateRoutes;
   private Map<String, AsPathAccessList> _asPathAccessLists;
   private BgpProcess _bgpProcess;
   private Set<Long> _communities;
   private Map<String, CommunityList> _communityLists;
   private Set<ConnectedRoute> _connectedRoutes;
   private Map<String, Interface> _interfaces;
   private Map<String, IpAccessList> _ipAccessLists;
   private OspfProcess _ospfProcess;
   private Map<String, PolicyMap> _policyMaps;
   private RoleSet _roles;
   private Map<String, RouteFilterList> _routeFilterLists;
   private Set<StaticRoute> _staticRoutes;

   private String _vendor;

   public Configuration(String hostname) {
      super(hostname);
      _asPathAccessLists = new HashMap<String, AsPathAccessList>();
      _aggregateRoutes = new LinkedHashSet<GeneratedRoute>();
      _bgpProcess = null;
      _communities = new LinkedHashSet<Long>();
      _communityLists = new HashMap<String, CommunityList>();
      _connectedRoutes = new LinkedHashSet<ConnectedRoute>();
      _interfaces = new HashMap<String, Interface>();
      _ipAccessLists = new HashMap<String, IpAccessList>();
      _ospfProcess = null;
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

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public Map<String, IpAccessList> getIpAccessLists() {
      return _ipAccessLists;
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

   public String getVendor() {
      return _vendor;
   }

   public void setBgpProcess(BgpProcess process) {
      _bgpProcess = process;
   }

   public void setOspfProcess(OspfProcess process) {
      _ospfProcess = process;
   }

   public void setRoles(RoleSet roles) {
      _roles = roles;
   }

   public void setVendor(String vendor) {
      _vendor = vendor;
   }

}
