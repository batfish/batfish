package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CiscoConfiguration implements Serializable {

   public static final String MANAGEMENT_VRF_NAME = "management";

   public static final String MASTER_VRF_NAME = "__MASTER_VRF__";

   private static final long serialVersionUID = 1L;

   protected final Map<String, IpAsPathAccessList> _asPathAccessLists;

   protected final Map<String, BgpProcess> _bgpProcesses;

   protected final Set<String> _classMapAccessGroups;

   protected final Set<String> _controlPlaneAccessGroups;

   protected final Map<String, ExpandedCommunityList> _expandedCommunityLists;

   protected final Map<String, ExtendedAccessList> _extendedAccessLists;

   protected String _hostname;

   protected final Map<String, Interface> _interfaces;

   protected IsisProcess _isisProcess;

   protected final Set<String> _lineAccessClassLists;

   protected final Set<String> _managementAccessGroups;

   protected final Set<String> _msdpPeerSaLists;

   protected final Set<String> _ntpAccessGroups;

   protected OspfProcess _ospfProcess;

   protected final Set<String> _pimAcls;

   protected final Set<String> _pimRouteMaps;

   protected final Map<String, PrefixList> _prefixLists;

   protected final Map<String, RouteMap> _routeMaps;

   protected final Map<String, RoutePolicy> _routePolicies;

   protected final Map<String, StandardAccessList> _standardAccessLists;

   protected final Map<String, StandardCommunityList> _standardCommunityLists;

   protected final HashSet<StaticRoute> _staticRoutes;

   public CiscoConfiguration() {
      _asPathAccessLists = new TreeMap<String, IpAsPathAccessList>();
      _bgpProcesses = new TreeMap<String, BgpProcess>();
      _classMapAccessGroups = new TreeSet<String>();
      _controlPlaneAccessGroups = new TreeSet<String>();
      _expandedCommunityLists = new TreeMap<String, ExpandedCommunityList>();
      _extendedAccessLists = new TreeMap<String, ExtendedAccessList>();
      _interfaces = new TreeMap<String, Interface>();
      _lineAccessClassLists = new TreeSet<String>();
      _managementAccessGroups = new TreeSet<String>();
      _msdpPeerSaLists = new TreeSet<String>();
      _ntpAccessGroups = new TreeSet<String>();
      _pimAcls = new TreeSet<String>();
      _pimRouteMaps = new TreeSet<String>();
      _prefixLists = new TreeMap<String, PrefixList>();
      _routeMaps = new TreeMap<String, RouteMap>();
      _routePolicies = new TreeMap<String, RoutePolicy>();
      _standardAccessLists = new TreeMap<String, StandardAccessList>();
      _standardCommunityLists = new TreeMap<String, StandardCommunityList>();
      _staticRoutes = new HashSet<StaticRoute>();
   }

   public Map<String, IpAsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
   }

   public final Map<String, BgpProcess> getBgpProcesses() {
      return _bgpProcesses;
   }

   public Set<String> getClassMapAccessGroups() {
      return _classMapAccessGroups;
   }

   public Set<String> getControlPlaneAccessGroups() {
      return _controlPlaneAccessGroups;
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

   public IsisProcess getIsisProcess() {
      return _isisProcess;
   }

   public Set<String> getLineAccessClassLists() {
      return _lineAccessClassLists;
   }

   public Set<String> getManagementAccessGroups() {
      return _managementAccessGroups;
   }

   public Set<String> getMsdpPeerSaLists() {
      return _msdpPeerSaLists;
   }

   public Set<String> getNtpAccessGroups() {
      return _ntpAccessGroups;
   }

   public final OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public Set<String> getPimAcls() {
      return _pimAcls;
   }

   public Set<String> getPimRouteMaps() {
      return _pimRouteMaps;
   }

   public final Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   public final Map<String, RouteMap> getRouteMaps() {
      return _routeMaps;
   }

   public final Map<String, RoutePolicy> getRoutePolicies() {
      return _routePolicies;
   }

   public final Map<String, StandardAccessList> getStandardAcls() {
      return _standardAccessLists;
   }

   public final Map<String, StandardCommunityList> getStandardCommunityLists() {
      return _standardCommunityLists;
   }

   public final Set<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public final void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setIsisProcess(IsisProcess isisProcess) {
      _isisProcess = isisProcess;
   }

   public final void setOspfProcess(OspfProcess proc) {
      _ospfProcess = proc;
   }

}
