package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import batfish.collections.RoleSet;
import batfish.main.BatfishException;
import batfish.representation.AsPathAccessList;
import batfish.representation.AsPathAccessListLine;
import batfish.representation.BgpNeighbor;
import batfish.representation.CommunityList;
import batfish.representation.CommunityListLine;
import batfish.representation.Configuration;
import batfish.representation.Ip;
import batfish.representation.LineAction;
import batfish.representation.OspfArea;
import batfish.representation.OspfMetricType;
import batfish.representation.OspfProcess;
import batfish.representation.PolicyMap;
import batfish.representation.PolicyMapAction;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapMatchAsPathAccessListLine;
import batfish.representation.PolicyMapMatchCommunityListLine;
import batfish.representation.PolicyMapMatchLine;
import batfish.representation.PolicyMapSetAddCommunityLine;
import batfish.representation.PolicyMapSetCommunityLine;
import batfish.representation.PolicyMapSetLine;
import batfish.representation.PolicyMapMatchNeighborLine;
import batfish.representation.PolicyMapSetLocalPreferenceLine;
import batfish.representation.PolicyMapSetMetricLine;
import batfish.representation.PolicyMapSetNextHopLine;
import batfish.representation.RouteFilterList;
import batfish.representation.VendorConfiguration;
import batfish.util.Util;

public class JuniperVendorConfiguration implements VendorConfiguration {

   private static final long serialVersionUID = 1L;
   private static final String VENDOR_NAME = "juniper";

   private String _hostname;
   private int _asNum;
   private List<String> _conversionWarnings;

   private Map<String, ASPathAccessList> _asPathAccessLists;
   private Map<String, CommunityMemberList> _communities;
   private Map<String, PrefixList> _prefixLists;
   private Map<String, PolicyStatement> _policyStatements;

   private List<BGPProcess> _bgpProcesses;
   private List<OSPFProcess> _ospfProcesses;
   private List<Interface> _interfaces;
   private Map<String, List<StaticOptions>> _staticRoutes;
   private RoleSet _roles;

   public RoleSet getRoles() {
      return _roles;
   }

   // private Map<String, ExtendedAccessList> _extendedAccessLists; // TODO: No
   // firewall stuff in Internet2
   // private List<GenerateRoute> _generateRoutes; // TODO: can't figure out
   // what these are in internet2
   // private Map<String, RouteFilter> _routeFilters; // TODO: can't figure out
   // what these are in internet2

   public JuniperVendorConfiguration() {
      _conversionWarnings = new ArrayList<String>();
      _asPathAccessLists = new HashMap<String, ASPathAccessList>();
      _communities = new HashMap<String, CommunityMemberList>();
      _prefixLists = new HashMap<String, PrefixList>();
      _policyStatements = new HashMap<String, PolicyStatement>();

      _ospfProcesses = new ArrayList<OSPFProcess>();
      _bgpProcesses = new ArrayList<BGPProcess>();
      _interfaces = new ArrayList<Interface>();
      _staticRoutes = new HashMap<String, List<StaticOptions>>();

      /*
       * _extendedAccessLists = new HashMap<String, ExtendedAccessList>();
       * _generateRoutes = new ArrayList<GenerateRoute>(); _routeFilters = new
       * HashMap<String, RouteFilter>();
       */

   }

   /*
    * -------------------------------------- General Constructs
    * -------------------------------------
    */
   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public String getHostname() {
      return _hostname;
   }

   /*
    * ---------------------------------- Policy Options Constructs
    * ----------------------------------
    */
   private static batfish.representation.AsPathAccessList toAsPathAccessList(
         ASPathAccessList jPathList) {
      List<AsPathAccessListLine> lines = new ArrayList<AsPathAccessListLine>();
      lines.add(new AsPathAccessListLine(jPathList.get_regex()));
      return new AsPathAccessList(jPathList.get_name(), lines);
   }

   private static batfish.representation.CommunityList toCommunityList(
         CommunityMemberList jCommunity) {
      List<CommunityListLine> cs = new ArrayList<CommunityListLine>();
      for (CommunityMemberListLine c : jCommunity.get_communityIds()) {
         cs.add(toCommunityListLine(c));
      }
      CommunityList cList = new CommunityList(jCommunity.get_name(), cs);
      return cList;
   }

   private static batfish.representation.CommunityListLine toCommunityListLine(
         CommunityMemberListLine jCommLine) {
      // TODO [Ask Ari]: Communities dont have actions and aren't regexes in
      // Juniper...
      return new CommunityListLine(LineAction.ACCEPT, "*");
   }

   // TODO [Ask Ari]: What to do with prefix lists?

   private static batfish.representation.PolicyMap toPolicyMap(
         final Configuration c, PolicyStatement jMap) {
      List<PolicyMapClause> clauses = new ArrayList<PolicyMapClause>();
      for (PolicyStatement_Term t : jMap.get_terms()) {
         clauses.add(toPolicyMapClause(c, t));
      }
      return new PolicyMap(jMap.get_name(), clauses);
   }

   private static batfish.representation.PolicyMapClause toPolicyMapClause(
         final Configuration c, PolicyStatement_Term jTerm) {
      Set<PolicyMapMatchLine> matchLines = new LinkedHashSet<PolicyMapMatchLine>();
      for (PolicyStatement_MatchLine fromLine : jTerm.get_matchList()) {
         matchLines.add(toPolicyMapMatchLine(c, fromLine));
      }
      Set<PolicyMapSetLine> setLines = new LinkedHashSet<PolicyMapSetLine>();
      for (PolicyStatement_SetLine fromLine : jTerm.get_setList()) {
         setLines.add(toPolicyMapSetLine(c, fromLine));
      }
      return new PolicyMapClause(toPolicyMapAction(jTerm.get_lineAction()),
            jTerm.get_name(), matchLines, setLines);
   }

   private static batfish.representation.PolicyMapAction toPolicyMapAction(
         PolicyStatement_LineAction la) {
      switch (la) {
      case ACCEPT:
         return PolicyMapAction.PERMIT;
      case REJECT:
         return PolicyMapAction.DENY;
      case NEXT_POLICY:
         // TODO [Ask Ari]: Where should I handle these?
         break;
      case NEXT_TERM:
         // TODO [Ask Ari]: Where should I handle these?
         break;
      }
      // TODO [P0]: Remove
      return PolicyMapAction.DENY;
   }

   private static batfish.representation.PolicyMapMatchLine toPolicyMapMatchLine(
         final Configuration c, PolicyStatement_MatchLine jMatchLine) {

      PolicyMapMatchLine mLine = null;

      switch (jMatchLine.getType()) {

      case AS_PATH:
         PolicyStatementMatchAsPathAccessListLine asPathLine = (PolicyStatementMatchAsPathAccessListLine) jMatchLine;

         Set<AsPathAccessList> newAsPathMatchSet = new LinkedHashSet<AsPathAccessList>();

         String pathListName = asPathLine.get_listName();
         AsPathAccessList aslist = c.getAsPathAccessLists().get(pathListName);
         if (aslist == null) {
            throw new Error("null list");
         }
         newAsPathMatchSet.add(aslist);
         mLine = new PolicyMapMatchAsPathAccessListLine(newAsPathMatchSet);
         // TODO [Ask Ari]: I don't follow this
         break;

      case COMMUNITY:
         PolicyStatementMatchCommunityListLine communityLine = (PolicyStatementMatchCommunityListLine) jMatchLine;
         Set<CommunityList> newCommunityMatchSet = new LinkedHashSet<CommunityList>();

         String clistName = communityLine.get_listName();
         CommunityList clist = c.getCommunityLists().get(clistName);
         if (clist == null) {
            throw new Error("no such community list");
         }
         newCommunityMatchSet.add(clist);
         mLine = new PolicyMapMatchCommunityListLine(newCommunityMatchSet);
         // TODO [Ask Ari]: I don't follow this
         break;

      case FAMILY:
         PolicyStatementMatchFamilyLine familyLine = (PolicyStatementMatchFamilyLine) jMatchLine;
         // TODO [P0]: need to fill in
         break;

      case INTERFACE:
         PolicyStatementMatchInterfaceListLine interfaceLine = (PolicyStatementMatchInterfaceListLine) jMatchLine;
         // TODO [P0]: need to fill in
         break;

      case NEIGHBOR:
         PolicyStatementMatchNeighborLine neighborLine = (PolicyStatementMatchNeighborLine) jMatchLine;
         mLine = new PolicyMapMatchNeighborLine(new Ip(
               neighborLine.get_neighborIp()));
         // TODO [P0]: need to fill in
         break;

      case PREFIX_LIST:
         PolicyStatementMatchPrefixListLine prefixListLine = (PolicyStatementMatchPrefixListLine) jMatchLine;
         // TODO [P0]: need to fill in
         break;

      case PREFIX_LIST_FILTER:
         PolicyStatementMatchPrefixListFilterLine prefixListFilterLine = (PolicyStatementMatchPrefixListFilterLine) jMatchLine;
         // TODO [P0]: need to fill in
         break;

      case PROTOCOL:
         PolicyStatementMatchProtocolListLine protocolLine = (PolicyStatementMatchProtocolListLine) jMatchLine;
         List<batfish.representation.Protocol> protocolList = new ArrayList<batfish.representation.Protocol>();
         for (ProtocolType prot : protocolLine.get_protocls()) {
            switch (prot) {
            case AGGREGATE:
               protocolList.add(batfish.representation.Protocol.AGGREGATE);
               break;
            case BGP:
               protocolList.add(batfish.representation.Protocol.BGP);
               break;
            case DIRECT:
               protocolList.add(batfish.representation.Protocol.CONNECTED);
            case ISIS:
               protocolList.add(batfish.representation.Protocol.ISIS);
               break;
            case MSDP:
               protocolList.add(batfish.representation.Protocol.MSDP);
               break;
            case OSPF:
               protocolList.add(batfish.representation.Protocol.OSPF);
               break;
            case STATIC:
               protocolList.add(batfish.representation.Protocol.STATIC);
               break;
            default:
               throw new BatfishException("Bad Protocol Type");
            }
         }
         mLine = new batfish.representation.PolicyMapMatchProtocolLine(
               protocolList);
         break;

      case RIB_FROM:
         PolicyStatementMatchRibFromLine ribFromLine = (PolicyStatementMatchRibFromLine) jMatchLine;
         // TODO [P0]: need to fill in
         break;

      case RIB_TO:
         PolicyStatementMatchRibToLine ribToLine = (PolicyStatementMatchRibToLine) jMatchLine;
         // TODO [P0]: need to fill in
         break;

      case ROUTE_FILTER:
         PolicyStatementMatchRouteFilterLine routeFilterLine = (PolicyStatementMatchRouteFilterLine) jMatchLine;
         Set<RouteFilterList> newRouteFilterMatchSet = new LinkedHashSet<RouteFilterList>();
         // TODO [P0]: need to fill in
         // TODO [Ask Ari]: How to convert FilterMatch Types to what
         // intermediate form expects
         break;

      case SOURCE_ADDRESS_FILTER:
         PolicyStatementMatchSourceAddressFilterLine sourceAddressFilterLine = (PolicyStatementMatchSourceAddressFilterLine) jMatchLine;
         // TODO [P0]: need to fill in
         break;

      default:
         throw new Error("Bad Match Line Type");
      }
      return mLine;
   }

   private static batfish.representation.PolicyMapSetLine toPolicyMapSetLine(
         Configuration c, PolicyStatement_SetLine jSetLine) {

      PolicyMapSetLine sLine = null;

      switch (jSetLine.getType()) {

      case AS_PATH_PREPEND:
         PolicyStatementSetAsPathPrepend AsPathPrependLine = (PolicyStatementSetAsPathPrepend) jSetLine;
         // TODO [P0]: need to fill in
         break;
      case COMMUNITY_ADD:
         PolicyStatementSetCommunityAddLine commAddLine = (PolicyStatementSetCommunityAddLine) jSetLine;

         List<Long> addComList = new ArrayList<Long>();

         List<CommunityListLine> currCommunitiesAdd = c.getCommunityLists()
               .get(commAddLine.get_communities()).getLines();
         for (CommunityListLine cll : currCommunitiesAdd) {
            String[] splitComm = cll.getRegex().split(":");
            long part1l = Long.parseLong(splitComm[0]);
            long part2l = Long.parseLong(splitComm[1]);
            long l = (part1l << 16) + part2l;
            addComList.add(l);
         }
         sLine = new PolicyMapSetAddCommunityLine(addComList);

         break;
      case COMMUNITY_DELETE:
         PolicyStatementSetCommunityDeleteLine commDeleteLine = (PolicyStatementSetCommunityDeleteLine) jSetLine;

         List<Long> delComList = new ArrayList<Long>();

         List<CommunityListLine> currCommunitiesDel = c.getCommunityLists()
               .get(commDeleteLine.get_communities()).getLines();
         for (CommunityListLine cll : currCommunitiesDel) {
            String[] splitComm = cll.getRegex().split(":");
            long part1l = Long.parseLong(splitComm[0]);
            long part2l = Long.parseLong(splitComm[1]);
            long l = (part1l << 16) + part2l;
            delComList.add(l);
         }
         // TODO [Ask Ari]: How do communities get deleted
         // sLine = new PolicyMapSetDeleteCommunityLine(delComList);
         break;
      case COMMUNITY_SET:
         PolicyStatementSetCommunitySetLine commSetLine = (PolicyStatementSetCommunitySetLine) jSetLine;

         List<Long> setComList = new ArrayList<Long>();

         List<CommunityListLine> currCommunitiesSet = c.getCommunityLists()
               .get(commSetLine.get_communities()).getLines();
         for (CommunityListLine cll : currCommunitiesSet) {
            String[] splitComm = cll.getRegex().split(":");
            long part1l = Long.parseLong(splitComm[0]);
            long part2l = Long.parseLong(splitComm[1]);
            long l = (part1l << 16) + part2l;
            setComList.add(l);
         }
         sLine = new PolicyMapSetCommunityLine(setComList);
         break;
      case INSTALL_NEXT_HOP:
         PolicyStatementSetInstallNextHopLine installNextHopLine = (PolicyStatementSetInstallNextHopLine) jSetLine;
         // TODO [P0]: need to fill in
         break;
      case LOCAL_PREF:
         PolicyStatementSetLocalPreferenceLine localPrefLine = (PolicyStatementSetLocalPreferenceLine) jSetLine;
         sLine = new PolicyMapSetLocalPreferenceLine(
               localPrefLine.get_LocalPreference());
         break;
      case METRIC:
         PolicyStatementSetMetricLine metricLine = (PolicyStatementSetMetricLine) jSetLine;
         sLine = new PolicyMapSetMetricLine(metricLine.get_metric());
         break;
      case NEXT_HOP:
         PolicyStatementSetNextHopLine nextHopLine = (PolicyStatementSetNextHopLine) jSetLine;

         switch (nextHopLine.get_hopType()) {
         case NEXTHOP_SELF:
            // TODO [Ask Ari]: What to do here?
            break;
         case NEXTHOP_DISCARD:
            // TODO [Ask Ari]: What to do here?
            break;
         case NEXTHOP_NAME:
            List<Ip> nextHops = new ArrayList<Ip>();
            nextHops.add(new Ip(nextHopLine.get_hopName()));
            sLine = new PolicyMapSetNextHopLine(nextHops);
            break;
         }
         break;
      default:
         throw new Error("Bad Set line Type");
      }
      return sLine;
   }

   /*
    * -------------------------------- Policy Options Getters/Setters
    * -------------------------------
    */
   public void addAsPathAccessLists(List<ASPathAccessList> asPathLists) {
      for (ASPathAccessList apl : asPathLists) {
         ASPathAccessList dupAsPathList = _asPathAccessLists
               .get(apl.get_name());
         if (dupAsPathList == null) {
            _asPathAccessLists.put(apl.get_name(), apl);
         }
         else {
            throw new Error("duplicate as path lists");
         }
      }
   }

   public void addCommunities(List<CommunityMemberList> commLists) {
      for (CommunityMemberList c : commLists) {
         CommunityMemberList dupCommList = _communities.get(c.get_name());
         if (dupCommList == null) {
            _communities.put(c.get_name(), c);
         }
         else {
            throw new Error("duplicate community lists");
         }

      }
   }

   public void addPrefixLists(List<PrefixList> prefixLists) {
      for (PrefixList p : prefixLists) {
         PrefixList dupPrefixList = _prefixLists.get(p.get_name());
         if (dupPrefixList == null) {
            _prefixLists.put(p.get_name(), p);
         }
         else {
            throw new Error("duplicate prefix lists");
         }

      }
   }

   public void addPolicyStatements(List<PolicyStatement> policyStatements) {
      for (PolicyStatement ps : policyStatements) {
         PolicyStatement dupPolicyStatement = _policyStatements.get(ps
               .get_name());
         if (dupPolicyStatement == null) {
            _policyStatements.put(ps.get_name(), ps);
         }
         else {
            throw new Error("duplicate policy statements");
         }

      }
   }

   public Map<String, PolicyStatement> getPolicyStatements() {
      return _policyStatements;
   }

   /*
    * ---------------------------------------- BGP Constructs
    * ---------------------------------------
    */

   private static batfish.representation.BgpProcess toBgpProcess(
         final Configuration c, BGPProcess proc, int asNum) {

      batfish.representation.BgpProcess newBgpProcess = new batfish.representation.BgpProcess();
      Map<Ip, BgpNeighbor> newBgpNeighbors = newBgpProcess.getNeighbors();
      Set<String> activeNeighbors = new LinkedHashSet<String>(
            proc.getActivatedNeighbors());
      for (BGPGroup pg : proc.getAllPeerGroups().values()) {
         Long clusterId = pg.getClusterId();
         boolean routeReflectorClient = pg.getRouteReflectorClient();
         for (BGPNeighbor bn : pg.getNeighbors()) {
            if (activeNeighbors.contains(bn.getIP())) {
               BgpNeighbor newNeighbor = new BgpNeighbor(new Ip(bn.getIP()));
               newNeighbor.setDefaultMetric(0); // TODO: maybe put this
                                                // somewhere more appropriate.
               PolicyMap newInboundPolicyMap = null;

               // Default behavior for Juniper
               newNeighbor.setSendCommunity(true);

               // update source
               String updateSource = pg.getUpdateSource();
               if (updateSource == null) {
                  updateSource = proc.getRouterID();
               }
               newNeighbor.setUpdateSource(updateSource);

               for (String inboundPSName : bn.getInboundPolicyStatement()) {
                  newInboundPolicyMap = c.getPolicyMaps().get(inboundPSName);
                  newNeighbor.addInboundPolicyMap(newInboundPolicyMap);
               }
               PolicyMap newOutboundPolicyMap = null;
               for (String outboundPSName : bn.getOutboundPolicyStatement()) {
                  newOutboundPolicyMap = c.getPolicyMaps().get(outboundPSName);
                  newNeighbor.addOutboundPolicyMap(newOutboundPolicyMap);
               }

               newNeighbor.setGroupName(pg.getName());
               if (routeReflectorClient) {
                  if (clusterId != null) {
                     newNeighbor.setClusterId(clusterId);
                  }
                  else {
                     throw new Error(
                           " cluster id missing in juniper configuration");
                  }
               }

               // Remote AS
               if (!pg.getIsExternal()) {
                  if ((pg.getLocalAS() != null) && (pg.getLocalAS() >= 0)) {
                     newNeighbor.setRemoteAs(pg.getLocalAS());
                  }
                  else {
                     newNeighbor.setRemoteAs(asNum);
                  }

               }
               else if (bn.getRemoteAS() != null) {
                  newNeighbor.setRemoteAs(bn.getRemoteAS());
               }
               else if (pg.getRemoteAS() != null) {
                  newNeighbor.setRemoteAs(pg.getRemoteAS());
               }
               else {
                  throw new Error("Remote AS not provided");
               }

               // Local AS
               if ((bn.getLocalAS() != null) && (bn.getLocalAS() >= 0)) {
                  newNeighbor.setLocalAs(bn.getLocalAS());
               }
               else if ((pg.getLocalAS() != null) && (pg.getLocalAS() >= 0)) {
                  newNeighbor.setLocalAs(pg.getLocalAS());
               }
               else {
                  newNeighbor.setLocalAs(asNum);
               }

               newBgpNeighbors.put(new Ip(bn.getIP()), newNeighbor);
            }
         }

      }
      return newBgpProcess;
   }

   /*
    * ------------------------------------- BGP Getters/Setters
    * -------------------------------------
    */
   public void addBGPProcess(BGPProcess process) {
      _bgpProcesses.add(process);
   }

   public List<BGPProcess> getBGPProcesses() {
      return _bgpProcesses;
   }

   /*
    * --------------------------------------- OSFP Constructs
    * ---------------------------------------
    */
   private static batfish.representation.OspfProcess toOSPFProcess(
         final Configuration c, OSPFProcess proc) {
      OspfProcess newProcess = new OspfProcess();

      for (String mapName : proc.getExportPolicyStatements()) {
         PolicyMap map = c.getPolicyMaps().get(mapName);
         newProcess.getOutboundPolicyMaps().add(map);

         // TODO: support E1 external ospf routes for juniper
         newProcess.getPolicyMetricTypes().put(map, OspfMetricType.E2);
      }

      Map<Long, OspfArea> areas = newProcess.getAreas();
      List<OSPFNetwork> networks = proc.getNetworks();
      if (networks.get(0).get_interface() == null) {

         Collections.sort(networks, new Comparator<OSPFNetwork>() {
            // sort so longest prefixes are first
            @Override
            public int compare(OSPFNetwork lhs, OSPFNetwork rhs) {
               int lhsPrefixLength = Util.numSubnetBits(lhs.get_subnetMask());
               int rhsPrefixLength = Util.numSubnetBits(rhs.get_subnetMask());
               int result = -Integer.compare(lhsPrefixLength, rhsPrefixLength);
               if (result == 0) {
                  long lhsIp = Util.ipToLong(lhs.get_networkAddress());
                  long rhsIp = Util.ipToLong(rhs.get_networkAddress());
                  result = Long.compare(lhsIp, rhsIp);
               }
               return result;
            }
         });

         for (batfish.representation.Interface i : c.getInterfaces().values()) {
            Ip interfaceIp = i.getIP();
            if (interfaceIp == null) {
               continue;
            }
            for (OSPFNetwork network : networks) {
               Ip networkIp = new Ip(network.get_networkAddress());
               if (interfaceIp.asLong() == networkIp.asLong()) {
                  // we have a longest prefix match
                  int areaNum = network.get_areaNum();
                  OspfArea newArea = areas.get(areaNum);
                  if (newArea == null) {
                     newArea = new OspfArea(areaNum);
                     areas.put((long) areaNum, newArea);
                  }
                  newArea.getInterfaces().add(i);
                  break;
               }
            }
         }
      }
      else {
         for (batfish.representation.Interface i : c.getInterfaces().values()) {
            Ip interfaceIp = i.getIP();
            if (interfaceIp == null) {
               continue;
            }
            for (OSPFNetwork n : networks) {
               if (n.get_interface() == null) {
                  throw new Error("Inconsistent implementation of OSPF Network");
               }
               if (i.getName().equals(n.get_interface())) {
                  int areaNum = n.get_areaNum();
                  OspfArea newArea = areas.get(areaNum);
                  if (newArea == null) {
                     newArea = new OspfArea(areaNum);
                     areas.put((long) areaNum, newArea);
                  }
                  newArea.getInterfaces().add(i);
                  break;
               }
            }
         }
      }

      newProcess.setRouterId(proc.getRouterId());
      newProcess.setReferenceBandwidth(proc.getReferenceBandwidth());

      return newProcess;
   }

   /*
    * ------------------------------------- OSPF Getters/Setters
    * ------------------------------------
    */
   public List<OSPFProcess> getOSPFProcesses() {
      return _ospfProcesses;
   }

   public void addOSPFProcess(OSPFProcess process) {
      _ospfProcesses.add(process);
   }

   /*
    * ---------------------------------- Routing Options Constructs
    * ---------------------------------
    */

   public static batfish.representation.StaticRoute toStateRoute(
         final Configuration c, StaticRoute staticRoute) {
      String nextHopIpStr = staticRoute.getNextHopIp();
      Ip nextHopIp = null;
      if (nextHopIpStr != null) {
         nextHopIp = new Ip(nextHopIpStr);
      }
      Ip prefix = new Ip(staticRoute.getPrefix());
      String nextHopInterface = staticRoute.getNextHopInterface();
      int prefixLength = Util.numSubnetBits(staticRoute.getMask());
      // TODO [Ask Ari]: Added tag=0
      return new batfish.representation.StaticRoute(prefix, prefixLength,
            nextHopIp, nextHopInterface, staticRoute.getDistance(), 0);
   }

   /*
    * ------------------------------- Routing Options Getters/Setters
    * -------------------------------
    */
   public void addStaticRoutes(Map<String, List<StaticOptions>> staticRoutes) {
      _staticRoutes.putAll(staticRoutes);
   }

   /*
    * ------------------------------------- Interfaces Constructs
    * -----------------------------------
    */
   private static batfish.representation.Interface toInterface(Interface iface,
         int as) {
      batfish.representation.Interface newIface = new batfish.representation.Interface(
            iface.getName());
      newIface.setAccessVlan(iface.getAccessVlan());
      newIface.setActive(iface.getActive());
      // no individual ospf area in interface
      newIface.setArea(as);
      newIface.setBandwidth(iface.getBandwidth());
      if (iface.getIP() != "") {
         newIface.setIP(new Ip(iface.getIP()));
         newIface.setSubnetMask(new Ip(iface.getSubnetMask()));
      }
      newIface.setNativeVlan(iface.getNativeVlan());
      newIface.setOspfCost(iface.getOspfCost());
      newIface.setOspfDeadInterval(iface.getOSPFDeadInterval());
      newIface.setOspfHelloMultiplier(iface.getOSPFHelloMultiplier());
      newIface.setSwitchportTrunkEncapsulation(iface
            .getSwitchportTrunkEncapsulation());
      return newIface;
   }

   /*
    * ---------------------------------- Interfaces Getters/Setters
    * ---------------------------------
    */
   public List<Interface> getInterfaces() {
      return _interfaces;
   }

   @Override
   public List<String> getConversionWarnings() {
      return _conversionWarnings;
   }

   @Override
   public batfish.representation.Configuration toVendorIndependentConfiguration() {
      final Configuration c = new Configuration(_hostname);
      c.setVendor(VENDOR_NAME);

      // convert as path access lists to vendor independent format
      for (ASPathAccessList pathList : _asPathAccessLists.values()) {
         AsPathAccessList apList = toAsPathAccessList(pathList);
         c.getAsPathAccessLists().put(apList.getName(), apList);
      }

      // convert community lists to vendor independent format
      for (CommunityMemberList ecl : _communities.values()) {
         batfish.representation.CommunityList newCommunityList = toCommunityList(ecl);
         c.getCommunityLists()
               .put(newCommunityList.getName(), newCommunityList);
      }

      // TODO [Ask Ari]: Convert Prefix Lists

      // convert policy statements to policy maps
      for (PolicyStatement ps : _policyStatements.values()) {
         batfish.representation.PolicyMap newPolicyMap = toPolicyMap(c, ps);
         c.getPolicyMaps().put(newPolicyMap.getMapName(), newPolicyMap);
      }

      // convert bgp process
      for (BGPProcess b : _bgpProcesses) {
         batfish.representation.BgpProcess newBGP = toBgpProcess(c, b, _asNum);
         c.setBgpProcess(newBGP);
      }

      // convert interfaces
      for (Interface iface : _interfaces) {
         batfish.representation.Interface newInterface = toInterface(iface,
               _asNum);
         c.getInterfaces().put(newInterface.getName(), newInterface);
      }

      // convert ospf
      for (OSPFProcess o : _ospfProcesses) {
         batfish.representation.OspfProcess newOSPF = toOSPFProcess(c, o);
         c.setOspfProcess(newOSPF);
      }

      // convert static routes
      // TODO: THIS NEEDS TO BE FIXED!
      /*
       * for (StaticRoute staticRoute : _staticRoutes) {
       * c.getStaticRoutes().add(toStateRoute(c, staticRoute)); }
       */
      /*
       * // convert route filters to route filter lists for (RouteFilter rf :
       * _routeFilters.values()) { RouteFilterList newRouteFilterList =
       * toRouteFilterList(rf);
       * c.getRouteFilterLists().put(newRouteFilterList.getName(),
       * newRouteFilterList); }
       *
       * // convert extended access lists to access lists for
       * (ExtendedAccessList eaList : _extendedAccessLists.values()) {
       * IpAccessList ipaList = toIpAccessList(eaList);
       * c.getIpAccessLists().put(ipaList.getName(), ipaList); }
       *
       * // convert generate routes for (GenerateRoute gr : _generateRoutes) {
       * GeneratedRoute newGeneratedRoute = toGeneratedRoute(c, gr);
       * c.getGeneratedRoutes().add(newGeneratedRoute); }
       */
      return c;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles = roles;
   }

}

/*
 * public static batfish.representation.RouteFilterList toRouteFilterList(
 * RouteFilter routeFilter) { RouteFilterList newRouteFilterList = new
 * RouteFilterList( routeFilter.getName()); for (RouteFilterLine rfLine :
 * routeFilter.getLines()) { batfish.representation.RouteFilterLine
 * newRouteFilterListLine = null; switch (rfLine.getType()) { case SUBRANGE:
 * RouteFilterSubRangeLine rfSubRangeLine = (RouteFilterSubRangeLine) rfLine;
 * newRouteFilterListLine = new
 * batfish.representation.RouteFilterLengthRangeLine( LineAction.ACCEPT, new
 * Ip(rfSubRangeLine.getPrefix()), rfSubRangeLine.getPrefixLength(),
 * rfSubRangeLine.getLengthRange());
 * newRouteFilterList.addLine(newRouteFilterListLine); break;
 *
 * case THROUGH: RouteFilterThroughLine rfThroughLine = (RouteFilterThroughLine)
 * rfLine; // System.out.println(rfThroughLine.getPrefix() + " " // +
 * rfThroughLine.getPrefixLength() + " " // + rfThroughLine.getSecondPrefix() +
 * " "prefixLists // + rfThroughLine.getSecondPrefixLength());
 * newRouteFilterListLine = new batfish.representation.RouteFilterThroughLine(
 * LineAction.ACCEPT, new Ip(rfThroughLine.getPrefix()),
 * rfThroughLine.getPrefixLength(), new Ip( rfThroughLine.getSecondPrefix()),
 * rfThroughLine.getSecondPrefixLength());
 * newRouteFilterList.addLine(newRouteFilterListLine); break;
 *
 * default: break;
 *
 * } } return newRouteFilterList;
 *
 * } public void addRouteFilters(List<RouteFilter> fl) { for (RouteFilter rf :
 * fl) { _routeFilters.put(rf.getName(), rf); } } public Map<String,
 * RouteFilter> getRouteFilter() { return _routeFilters; }
 */

/*
 * private static GeneratedRoute toGeneratedRoute(final Configuration c,
 * GenerateRoute gr) { Set<batfish.representation.PolicyMap>
 * newGenerationPolicies = new
 * LinkedHashSet<batfish.representation.PolicyMap>();
 * batfish.representation.PolicyMap GenerationPolicy = c
 * .getPolicyMaps().get(gr.getPolicy()); if (GenerationPolicy != null) {
 * newGenerationPolicies.add(GenerationPolicy); } else { throw new
 * Error("generation policy not existed"); } GeneratedRoute newGeneratedRoute =
 * new GeneratedRoute(new Ip( gr.getPrefix()), gr.getPrefixLength(),
 * gr.getPreference(), newGenerationPolicies); return newGeneratedRoute; }
 */

/*
 * public void addGenerateRoute(GenerateRoute gr) { _generateRoutes.add(gr); }
 *
 * public void addGenerateRoutes(List<GenerateRoute> gr) {
 * _generateRoutes.addAll(gr); } public void addStaticRoute(StaticRoute
 * staticRoute) { _staticRoutes.add(staticRoute); }
 *
 *
 * public List<GenerateRoute> getGenerateRoutes() { return _generateRoutes; }
 *
 * public List<StaticRoute> getStaticRoutes() { return _staticRoutes; }
 */
/*
 * public void addInterface(Interface interface1) { _interfaces.add(interface1);
 * }
 */

/*
 * private static IpAccessList toIpAccessList(ExtendedAccessList eaList) {
 * String name = eaList.getId(); List<IpAccessListLine> lines = new
 * ArrayList<IpAccessListLine>(); for (ExtendedAccessListLine fromLine :
 * eaList.getLines()) { lines.add(new IpAccessListLine(fromLine.getLineAction(),
 * fromLine .getProtocol(), new Ip(fromLine.getSourceIP()), new Ip(fromLine
 * .getSourceWildcard()), new Ip(fromLine.getDestinationIP()), new
 * Ip(fromLine.getDestinationWildcard()), fromLine .getSrcPortRanges(),
 * fromLine.getDstPortRanges())); } return new IpAccessList(name, lines); }
 *
 * public void setASNum(int as) { _asNum = as; }
 *
 *
 *
 * public int getASNum() { return _asNum; }
 *
 *
 *
 * public void addExtendedAccessList(ExtendedAccessList eal) { if
 * (_extendedAccessLists.containsKey(eal.getId())) { throw new
 * Error("duplicate extended access list name"); }
 * _extendedAccessLists.put(eal.getId(), eal); }
 *
 * public void addExtendedAccessListLine(String id, ExtendedAccessListLine eall)
 * { if (!_extendedAccessLists.containsKey(id)) { _extendedAccessLists.put(id,
 * new ExtendedAccessList(id)); } _extendedAccessLists.get(id).addLine(eall); }
 * public Map<String, ExtendedAccessList> getExtendedAccessLists() { return
 * _extendedAccessLists; }
 */
