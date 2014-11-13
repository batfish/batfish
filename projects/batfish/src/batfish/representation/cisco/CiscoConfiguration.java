package batfish.representation.cisco;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CiscoConfiguration implements Serializable {

   private static final long serialVersionUID = 1L;
   public static final String MASTER_VRF_NAME = "__MASTER_VRF__";

   protected final Map<String, IpAsPathAccessList> _asPathAccessLists;
   protected final Map<String, BgpProcess> _bgpProcesses;
   protected final Map<String, ExpandedCommunityList> _expandedCommunityLists;
   protected final Map<String, ExtendedAccessList> _extendedAccessLists;
   protected String _hostname;
   protected final Map<String, Interface> _interfaces;
   protected OspfProcess _ospfProcess;
   protected final Map<String, PrefixList> _prefixLists;
   protected final Map<String, RouteMap> _routeMaps;
   protected final Map<String, StandardAccessList> _standardAccessLists;
   protected final Map<String, StandardCommunityList> _standardCommunityLists;
   protected final Map<String, StaticRoute> _staticRoutes;

   public CiscoConfiguration() {
      _asPathAccessLists = new HashMap<String, IpAsPathAccessList>();
      _bgpProcesses = new HashMap<String, BgpProcess>();
      _expandedCommunityLists = new HashMap<String, ExpandedCommunityList>();
      _extendedAccessLists = new HashMap<String, ExtendedAccessList>();
      _interfaces = new HashMap<String, Interface>();
      _prefixLists = new HashMap<String, PrefixList>();
      _routeMaps = new HashMap<String, RouteMap>();
      _standardAccessLists = new HashMap<String, StandardAccessList>();
      _standardCommunityLists = new HashMap<String, StandardCommunityList>();
      _staticRoutes = new HashMap<String, StaticRoute>();
   }

   public Map<String, IpAsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
   }

   public final Map<String, BgpProcess> getBgpProcesses() {
      return _bgpProcesses;
   }

   public final Map<String, ExpandedCommunityList> getExpandedCommunityLists() {
      return _expandedCommunityLists;
   }

   public final Map<String, ExtendedAccessList> getExtendedAcls() {
      return _extendedAccessLists;
   }

   public final String getHostname() {
      return _hostname;
   }

   public final Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public final OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public final Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   public final Map<String, RouteMap> getRouteMaps() {
      return _routeMaps;
   }

   public final Map<String, StandardAccessList> getStandardAcls() {
      return _standardAccessLists;
   }

   public final Map<String, StandardCommunityList> getStandardCommunityLists() {
      return _standardCommunityLists;
   }

   public final Map<String, StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public final void setHostname(String hostname) {
      _hostname = hostname;
   }

   public final void setOspfProcess(OspfProcess proc) {
      _ospfProcess = proc;
   }

}
