package batfish.representation;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import batfish.util.NamedStructure;

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
   private Map<String, RouteFilterList> _routeFilterLists;
   private Set<StaticRoute> _staticRoutes;
   private String _vendor;

   public Configuration(String hostname) {
      super(hostname);
      _asPathAccessLists = new HashMap<String, AsPathAccessList>();
      _aggregateRoutes = new LinkedHashSet<GeneratedRoute>();
      _bgpProcess = null;
      _communityLists = new HashMap<String, CommunityList>();
      _connectedRoutes = new LinkedHashSet<ConnectedRoute>();
      _interfaces = new HashMap<String, Interface>();
      _ipAccessLists = new HashMap<String, IpAccessList>();
      _ospfProcess = null;
      _policyMaps = new HashMap<String, PolicyMap>();
      _routeFilterLists = new HashMap<String, RouteFilterList>();
      _staticRoutes = new LinkedHashSet<StaticRoute>();
      _communities = new LinkedHashSet<Long>();
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

   public Map<String, RouteFilterList> getRouteFilterLists() {
      return _routeFilterLists;
   }

   public Set<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public String getVendor() {
      return _vendor;
   }

   public boolean sameParseTree(Configuration c, String prefix) {
      boolean res = this.equals(c);
      boolean finalRes = res;
      if (res == false) {
         System.out.println("Name " + prefix);
         finalRes = res;
      }

      if (_aggregateRoutes.size() != c._aggregateRoutes.size()) {
         System.out.println("AggRoute:Size " + prefix);
         finalRes = res;
      }
      else {
         for (GeneratedRoute lhs : _aggregateRoutes) {
            boolean found = false;
            for (GeneratedRoute rhs : c._aggregateRoutes) {
               if (lhs.equals(rhs)) {
                  res = lhs.sameParseTree(rhs, "AggRoute " + prefix);
                  if (res == false) {
                     finalRes = res;
                  }
                  found = true;
                  break;
               }
            }
            if (found == false) {
               System.out.println("AggRoute:NotFound " + prefix);
               finalRes = found;
            }
         }
      }

      if (_asPathAccessLists.size() != c._asPathAccessLists.size()) {
         System.out.println("AsPathAccessList:Size " + prefix);
         finalRes = false;
      }
      else {
         for (AsPathAccessList lhs : _asPathAccessLists.values()) {
            AsPathAccessList rhs = c._asPathAccessLists.get(lhs.getName());
            if (rhs == null) {
               System.out.println("AsPathAccessList:NullRhs " + prefix);
               finalRes = false;
            }
            else {
               res = lhs.sameParseTree(rhs, "AsPathAccessList " + prefix);
               if (res == false) {
                  finalRes = res;
               }
            }
         }
      }

      if ((_bgpProcess != null) && (c._bgpProcess != null)) {
         res = _bgpProcess.sameParseTree(c._bgpProcess, "BGPProcess " + prefix);
      }
      else {
         res = (_bgpProcess == null) && (c._bgpProcess == null);
         if (res == false) {
            System.out.println("BGPProcess " + prefix);
            finalRes = res;
         }
      }
      if (res == false) {
         finalRes = res;
      }

      res = _communities.equals(c._communities);
      if (res == false) {
         System.out.println("Communities " + prefix);
         finalRes = res;
      }

      if (_communityLists.size() != c._communityLists.size()) {
         System.out.println("CommList:Size " + prefix);
         finalRes = false;
      }
      else {
         for (CommunityList lhs : _communityLists.values()) {
            CommunityList rhs = c._communityLists.get(lhs.getName());
            if (rhs == null) {
               System.out.println("CommList:NullRhs " + prefix);
               finalRes = false;
            }
            else {
               res = lhs.sameParseTree(rhs, "CommList " + prefix);
               if (res == false) {
                  finalRes = res;
               }
            }
         }
      }

      if (_connectedRoutes.size() != c._connectedRoutes.size()) {
         System.out.println("ConnRoute:Size " + prefix);
         finalRes = false;
      }
      else {
         for (ConnectedRoute lhs : _connectedRoutes) {
            boolean found = false;
            for (ConnectedRoute rhs : c._connectedRoutes) {
               if (lhs.equals(rhs)) {
                  found = true;
                  break;
               }
            }
            if (found == false) {
               System.out.println("ConnRoute:NotFound " + prefix);
               finalRes = false;
            }
         }
      }

      if (_interfaces.size() != c._interfaces.size()) {
         System.out.println("Interfaces:Size " + prefix);
         finalRes = false;
      }
      else {
         for (Interface lhs : _interfaces.values()) {
            Interface rhs = c._interfaces.get(lhs.getName());
            if (rhs == null) {
               System.out.println("Interfaces:NullRhs " + prefix);
               finalRes = false;
            }
            else {
               res = lhs.sameParseTree(rhs, this, c, "Interfaces " + prefix);
               if (res == false) {
                  finalRes = res;
               }
            }
         }
      }

      /*
       * if(_ipAccessLists.size() != c._ipAccessLists.size()){
       * System.out.print("IpAccessList:Size "); return false; }
       * for(IpAccessList lhs : _ipAccessLists.values()){ IpAccessList rhs =
       * c._ipAccessLists.get(lhs.getName()); if(rhs == null){
       * System.out.print("IpAccessList:NullRhs "); return false; }else{ res =
       * res && lhs.sameParseTree(rhs); if(res == false){
       * System.out.print("IpAccessList "); return res; } } }
       */

      if ((_ospfProcess != null) && (c._ospfProcess != null)) {
         res = _ospfProcess.sameParseTree(c._ospfProcess, "OspfProcess "
               + prefix);
      }
      else {
         res = ((_ospfProcess == null) && (c._ospfProcess == null));
         if (res == false) {
            System.out.println("OspfProcess " + prefix);
            finalRes = res;
         }
      }
      if (res == false) {
         finalRes = res;
      }

      /*
       * if(_policyMaps.size() != c._policyMaps.size()){
       * System.out.print("PolicyMaps:Size "); return false; } for(PolicyMap lhs
       * : _policyMaps.values()){ PolicyMap rhs =
       * c._policyMaps.get(lhs.getMapName()); if(rhs == null){
       * System.out.print("PolicyMaps:NullRhs "); return false; }else{ res = res
       * && lhs.sameParseTree(rhs); if(res == false){
       * System.out.print("PolicyMaps "); return res; } } }
       */

      if (_routeFilterLists.size() != c._routeFilterLists.size()) {
         System.out.println("RouteFilterLists:Size " + prefix);
         finalRes = false;
      }
      else {
         for (RouteFilterList lhs : _routeFilterLists.values()) {
            RouteFilterList rhs = c._routeFilterLists.get(lhs.getName());
            if (rhs == null) {
               System.out.println("RouteFilterLists:NullRhs " + prefix);
               finalRes = false;
            }
            else {
               res = lhs.sameParseTree(rhs, "RouteFilterLists " + prefix);
               if (res == false) {
                  finalRes = res;
               }
            }
         }
      }

      if (_staticRoutes.size() != c._staticRoutes.size()) {
         System.out.println("StatRoute:Size " + prefix);
         finalRes = false;
      }
      else {
         for (StaticRoute lhs : _staticRoutes) {
            boolean found = false;
            for (StaticRoute rhs : c._staticRoutes) {
               if (lhs.equals(rhs)) {
                  res = lhs.sameParseTree(rhs);
                  found = true;
                  if (res == false) {
                     System.out.println("StatRoute " + prefix);
                     finalRes = false;
                  }
                  break;
               }
            }
            if (found == false) {
               System.out.println("StatRoute:NotFound " + prefix);
               finalRes = false;
            }
         }
      }

      return finalRes;
   }

   public void setBgpProcess(BgpProcess process) {
      _bgpProcess = process;
   }

   public void setOspfProcess(OspfProcess process) {
      _ospfProcess = process;
   }

   public void setVendor(String vendor) {
      _vendor = vendor;
   }

}
