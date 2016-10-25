package org.batfish.representation.cisco;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.aaa.Aaa;
import org.batfish.representation.VendorConfiguration;

public abstract class CiscoConfiguration extends VendorConfiguration {

   public static final String MANAGEMENT_VRF_NAME = "management";

   public static final String MASTER_VRF_NAME = "__MASTER_VRF__";

   private static final long serialVersionUID = 1L;

   protected Aaa _aaaSettings;

   protected final Map<String, IpAsPathAccessList> _asPathAccessLists;

   protected final Map<String, BgpProcess> _bgpProcesses;

   protected final Set<String> _classMapAccessGroups;

   protected final Set<String> _controlPlaneAccessGroups;

   protected final Map<String, ExpandedCommunityList> _expandedCommunityLists;

   protected final Map<String, ExtendedAccessList> _extendedAccessLists;

   protected boolean _failover;

   protected String _failoverCommunicationInterface;

   protected String _failoverCommunicationInterfaceAlias;

   protected final Map<String, String> _failoverInterfaces;

   protected final Map<String, Prefix> _failoverPrimaryPrefixes;

   protected boolean _failoverSecondary;

   protected final Map<String, Prefix> _failoverStandbyPrefixes;

   protected String _failoverStatefulSignalingInterface;

   protected String _failoverStatefulSignalingInterfaceAlias;

   protected String _hostname;

   protected final Map<String, Interface> _interfaces;

   protected final Set<String> _ipv6PeerGroups;

   protected IsisProcess _isisProcess;

   protected final Set<String> _lineAccessClassLists;

   protected final Set<String> _managementAccessGroups;

   protected final Set<String> _msdpPeerSaLists;

   protected final Set<String> _ntpAccessGroups;

   protected OspfProcess _ospfProcess;

   protected final Set<String> _pimAcls;

   protected final Set<String> _pimRouteMaps;

   protected final Map<String, PrefixList> _prefixLists;

   protected final Set<String> _referencedRouteMaps;

   protected final Map<String, RouteMap> _routeMaps;

   protected final Map<String, RoutePolicy> _routePolicies;

   protected final Map<String, StandardAccessList> _standardAccessLists;

   protected final Map<String, StandardCommunityList> _standardCommunityLists;

   protected final HashSet<StaticRoute> _staticRoutes;

   public CiscoConfiguration() {
      _asPathAccessLists = new TreeMap<>();
      _bgpProcesses = new TreeMap<>();
      _classMapAccessGroups = new TreeSet<>();
      _controlPlaneAccessGroups = new TreeSet<>();
      _expandedCommunityLists = new TreeMap<>();
      _extendedAccessLists = new TreeMap<>();
      _failoverInterfaces = new TreeMap<>();
      _failoverPrimaryPrefixes = new TreeMap<>();
      _failoverStandbyPrefixes = new TreeMap<>();
      _interfaces = new TreeMap<>();
      _ipv6PeerGroups = new TreeSet<>();
      _lineAccessClassLists = new TreeSet<>();
      _managementAccessGroups = new TreeSet<>();
      _msdpPeerSaLists = new TreeSet<>();
      _ntpAccessGroups = new TreeSet<>();
      _pimAcls = new TreeSet<>();
      _pimRouteMaps = new TreeSet<>();
      _prefixLists = new TreeMap<>();
      _referencedRouteMaps = new TreeSet<>();
      _routeMaps = new TreeMap<>();
      _routePolicies = new TreeMap<>();
      _standardAccessLists = new TreeMap<>();
      _standardCommunityLists = new TreeMap<>();
      _staticRoutes = new HashSet<>();
   }

   public Aaa getAaaSettings() {
      return _aaaSettings;
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

   public boolean getFailover() {
      return _failover;
   }

   public String getFailoverCommunicationInterface() {
      return _failoverCommunicationInterface;
   }

   public String getFailoverCommunicationInterfaceAlias() {
      return _failoverCommunicationInterfaceAlias;
   }

   public Map<String, String> getFailoverInterfaces() {
      return _failoverInterfaces;
   }

   public Map<String, Prefix> getFailoverPrimaryPrefixes() {
      return _failoverPrimaryPrefixes;
   }

   public boolean getFailoverSecondary() {
      return _failoverSecondary;
   }

   public Map<String, Prefix> getFailoverStandbyPrefixes() {
      return _failoverStandbyPrefixes;
   }

   public String getFailoverStatefulSignalingInterface() {
      return _failoverStatefulSignalingInterface;
   }

   public String getFailoverStatefulSignalingInterfaceAlias() {
      return _failoverStatefulSignalingInterfaceAlias;
   }

   @Override
   public final String getHostname() {
      return _hostname;
   }

   public final Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public Set<String> getIpv6PeerGroups() {
      return _ipv6PeerGroups;
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

   public Set<String> getReferencedRouteMaps() {
      return _referencedRouteMaps;
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

   public void setAaaSettings(Aaa aaaSettings) {
      _aaaSettings = aaaSettings;
   }

   public void setFailover(boolean failover) {
      _failover = failover;
   }

   public void setFailoverCommunicationInterface(
         String failoverCommunicationInterface) {
      _failoverCommunicationInterface = failoverCommunicationInterface;
   }

   public void setFailoverCommunicationInterfaceAlias(
         String failoverCommunicationInterfaceAlias) {
      _failoverCommunicationInterfaceAlias = failoverCommunicationInterfaceAlias;
   }

   public void setFailoverSecondary(boolean failoverSecondary) {
      _failoverSecondary = failoverSecondary;
   }

   public void setFailoverStatefulSignalingInterface(
         String failoverStatefulSignalingInterface) {
      _failoverStatefulSignalingInterface = failoverStatefulSignalingInterface;
   }

   public void setFailoverStatefulSignalingInterfaceAlias(
         String failoverStatefulSignalingInterfaceAlias) {
      _failoverStatefulSignalingInterfaceAlias = failoverStatefulSignalingInterfaceAlias;
   }

   @Override
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
