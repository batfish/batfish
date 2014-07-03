package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import batfish.representation.AsPathAccessList;
import batfish.representation.AsPathAccessListLine;
import batfish.representation.BgpNeighbor;
import batfish.representation.CommunityList;
import batfish.representation.CommunityListLine;
import batfish.representation.Configuration;
import batfish.representation.GeneratedRoute;
import batfish.representation.Ip;
import batfish.representation.IpAccessList;
import batfish.representation.IpAccessListLine;
import batfish.representation.LineAction;
import batfish.representation.OspfArea;
import batfish.representation.OspfProcess;
import batfish.representation.PolicyMap;
import batfish.representation.PolicyMapAction;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapMatchAsPathAccessListLine;
import batfish.representation.PolicyMapMatchCommunityListLine;
import batfish.representation.PolicyMapMatchIpAccessListLine;
import batfish.representation.PolicyMapMatchLine;
import batfish.representation.PolicyMapMatchRouteFilterListLine;
import batfish.representation.PolicyMapSetAddCommunityLine;
import batfish.representation.PolicyMapSetCommunityLine;
import batfish.representation.PolicyMapSetDeleteCommunityLine;
import batfish.representation.PolicyMapSetLine;
import batfish.representation.PolicyMapSetLocalPreferenceLine;
import batfish.representation.PolicyMapSetMetricLine;
import batfish.representation.PolicyMapSetNextHopLine;
import batfish.representation.RouteFilterList;
import batfish.representation.VendorConfiguration;
import batfish.util.Util;

public class JuniperVendorConfiguration implements VendorConfiguration {

   private static final String VENDOR_NAME = "juniper";
   private static batfish.representation.AsPathAccessList toAsPathAccessList(
         ASPathAccessList pathList) {
      String name = pathList.getName();
      List<AsPathAccessListLine> lines = new ArrayList<AsPathAccessListLine>();
      for (ASPathAccessListLine fromLine : pathList.getLines()) {
         lines.add(new AsPathAccessListLine(fromLine.getRegex()));
      }
      return new AsPathAccessList(name, lines);
   }
   private static batfish.representation.BgpProcess toBgpProcess(
         final Configuration c, BGPProcess proc, int asNum) {
      batfish.representation.BgpProcess newBgpProcess = new batfish.representation.BgpProcess();
      Map<String, BgpNeighbor> newBgpNeighbors = newBgpProcess.getNeighbors();
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
                     // clusterId = Util.ipToLong(pg.getUpdateSource());
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

               newBgpNeighbors.put(bn.getIP(), newNeighbor);
            }
         }

      }
      return newBgpProcess;
   }
   private static CommunityList toCommunityList(ExpandedCommunityList ecList) {
      List<CommunityListLine> cllList = new ArrayList<CommunityListLine>();
      for (ExpandedCommunityListLine ecll : ecList.getLines()) {
         cllList.add(toCommunityListLine(ecll));
      }
      CommunityList cList = new CommunityList(ecList.getName(), cllList);
      return cList;
   }
   private static CommunityListLine toCommunityListLine(
         ExpandedCommunityListLine eclLine) {
      return new CommunityListLine(eclLine.getAction(), eclLine.getRegex());
   }
   private static GeneratedRoute toGeneratedRoute(final Configuration c,
         GenerateRoute gr) {
      Set<batfish.representation.PolicyMap> newGenerationPolicies = new LinkedHashSet<batfish.representation.PolicyMap>();
      batfish.representation.PolicyMap GenerationPolicy = c
            .getPolicyMaps().get(gr.getPolicy());
      if (GenerationPolicy != null) {
         newGenerationPolicies.add(GenerationPolicy);
      }
      else {
         throw new Error("generation policy not existed");
      }
      GeneratedRoute newGeneratedRoute = new GeneratedRoute(new Ip(
            gr.getPrefix()), gr.getPrefixLength(), gr.getPreference(),
            newGenerationPolicies);
      return newGeneratedRoute;
   }
   private batfish.representation.Interface toInterface(
         Interface iface, int as, Map<String, IpAccessList> ipAccessLists) {
      // System.out.println("Converting Interface: " + iface.getName());
      batfish.representation.Interface newIface = new batfish.representation.Interface(
            iface.getName());
      newIface.setAccessVlan(iface.getAccessVlan());
      newIface.setActive(iface.getActive());
      // no individual ospf area in interface
      newIface.setArea(as);
      newIface.setBandwidth(iface.getBandwidth());
      // System.out.println("Ip: "+iface.getIP());
      if (iface.getIP() != "") {
         newIface.setIP(new Ip(iface.getIP()));
         newIface.setSubnetMask(new Ip(iface.getSubnetMask()));
      }
      newIface.setNativeVlan(iface.getNativeVlan());
      newIface.setOspfCost(iface.getOspfCost());
      newIface.setOspfDeadInterval(iface.getOSPFDeadInterval());
      newIface.setOspfHelloMultiplier(iface.getOSPFHelloMultiplier());
      newIface.setSwitchportMode(iface.getSwitchportMode());
      newIface.setSwitchportTrunkEncapsulation(iface
            .getSwitchportTrunkEncapsulation());
      String incomingFilterName = iface.getIncomingFilter();
      if(incomingFilterName != null){
         IpAccessList incomingFilter = ipAccessLists.get(incomingFilterName);
         if (incomingFilter == null) {
            _conversionWarnings.add("Interface: '" + iface.getName()
                  + "' configured with non-existent incoming acl '"
                  + incomingFilterName + "'");
         }
         newIface.setIncomingFilter(incomingFilter);
      }
      String outgoingFilterName = iface.getOutgoingFilter();
      if(outgoingFilterName != null){
         IpAccessList outgoingFilter = ipAccessLists.get(outgoingFilterName);
         if (outgoingFilter == null) {
            _conversionWarnings.add("Interface: '" + iface.getName()
                  + "' configured with non-existent outgoing acl '"
                  + outgoingFilterName + "'");
         }
         newIface.setOutgoingFilter(outgoingFilter);
      }
      return newIface;
   }
   private static IpAccessList toIpAccessList(ExtendedAccessList eaList) {
      String name = eaList.getId();
      List<IpAccessListLine> lines = new ArrayList<IpAccessListLine>();
      for (ExtendedAccessListLine fromLine : eaList.getLines()) {
         lines.add(new IpAccessListLine(fromLine.getLineAction(), fromLine
               .getProtocol(), new Ip(fromLine.getSourceIP()), new Ip(fromLine
               .getSourceWildcard()), new Ip(fromLine.getDestinationIP()),
               new Ip(fromLine.getDestinationWildcard()), fromLine
                     .getSrcPortRanges(), fromLine.getDstPortRanges()));
      }
      return new IpAccessList(name, lines);
   }
   private static OspfProcess toOSPFProcess(final Configuration c,
         OSPFProcess proc) {
      OspfProcess newProcess = new OspfProcess();

      for (String mapName : proc.getExportPolicyStatements()) {
         PolicyMap map = c.getPolicyMaps().get(mapName);
         newProcess.getOutboundPolicyMaps().add(map);
      }

      Map<Long, OspfArea> areas = newProcess.getAreas();
      List<OSPFNetwork> networks = proc.getNetworks();
      if (networks.get(0).getInterface() == null) {

         Collections.sort(networks, new Comparator<OSPFNetwork>() {
            // sort so longest prefixes are first
            @Override
            public int compare(OSPFNetwork lhs, OSPFNetwork rhs) {
               int lhsPrefixLength = Util.numSubnetBits(lhs.getSubnetMask());
               int rhsPrefixLength = Util.numSubnetBits(rhs.getSubnetMask());
               int result = -Integer.compare(lhsPrefixLength, rhsPrefixLength);
               if (result == 0) {
                  long lhsIp = Util.ipToLong(lhs.getNetworkAddress());
                  long rhsIp = Util.ipToLong(rhs.getNetworkAddress());
                  result = Long.compare(lhsIp, rhsIp);
               }
               return result;
            }
         });

         for (batfish.representation.Interface i : c.getInterfaces()
               .values()) {
            Ip interfaceIp = i.getIP();
            if (interfaceIp == null) {
               continue;
            }
            for (OSPFNetwork network : networks) {
               Ip networkIp = new Ip(network.getNetworkAddress());
               if (interfaceIp.asLong() == networkIp.asLong()) {
                  // we have a longest prefix match
                  long areaNum = network.getArea();
                  OspfArea newArea = areas.get(areaNum);
                  if (newArea == null) {
                     newArea = new OspfArea(areaNum);
                     areas.put(areaNum, newArea);
                  }
                  newArea.getInterfaces().add(i);
                  break;
               }
            }
         }
      }
      else {
         for (batfish.representation.Interface i : c.getInterfaces()
               .values()) {
            // System.out.println("name : "+i.getName());
            Ip interfaceIp = i.getIP();
            if (interfaceIp == null) {
               continue;
            }
            for (OSPFNetwork network : networks) {
               if (network.getInterface() == null) {
                  throw new Error("Inconsistent implementation of OSPF Network");
               }
               if (i.getName().equals(network.getInterface())) {
                  // System.out.println("match : "+i.getName());
                  long areaNum = network.getArea();
                  OspfArea newArea = areas.get(areaNum);
                  if (newArea == null) {
                     newArea = new OspfArea(areaNum);
                     areas.put(areaNum, newArea);
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
   private static PolicyMap toPolicyMap(final Configuration c,
         PolicyStatement map) {
      List<PolicyMapClause> clauses = new ArrayList<PolicyMapClause>();
      for (PolicyStatementClause rmClause : map.getClauseList()) {
         clauses.add(toPolicyMapClause(c, rmClause));
      }
      return new PolicyMap(map.getMapName(), clauses);
   }
   private static PolicyMapClause toPolicyMapClause(final Configuration c,
         PolicyStatementClause clause) {
      Set<PolicyMapMatchLine> matchLines = new LinkedHashSet<PolicyMapMatchLine>();
      for (PolicyStatementMatchLine fromLine : clause.getMatchList()) {
         matchLines.add(toPolicyMapMatchLine(c, fromLine));
      }
      Set<PolicyMapSetLine> setLines = new LinkedHashSet<PolicyMapSetLine>();
      for (PolicyStatementSetLine fromLine : clause.getSetList()) {
         setLines.add(toPolicyMapSetLine(c, fromLine));
      }
      return new PolicyMapClause(PolicyMapAction.fromLineAction(clause
            .getAction()), Integer.toString(clause.getSeqNum()), matchLines,
            setLines);
   }
   private static PolicyMapMatchLine toPolicyMapMatchLine(
         final Configuration c, PolicyStatementMatchLine matchLine) {
      PolicyMapMatchLine newLine = null;
      switch (matchLine.getType()) {
      case AS_PATH_ACCESS_LIST:
         PolicyStatementMatchAsPathAccessListLine pathLine = (PolicyStatementMatchAsPathAccessListLine) matchLine;
         Set<AsPathAccessList> newAsPathMatchSet = new LinkedHashSet<AsPathAccessList>();
         // multiple list names not supported yet
         // for (String pathListName : pathLine.getListNames()) {
         // AsPathAccessList list = c.getAsPathAccessLists().get(pathListName);
         // newAsPathMatchSet.add(list);
         // }
         String pathListName = pathLine.getListName();
         AsPathAccessList aslist = c.getAsPathAccessLists().get(pathListName);
         if (aslist == null) {
            throw new Error("null list");
         }
         newAsPathMatchSet.add(aslist);
         newLine = new PolicyMapMatchAsPathAccessListLine(newAsPathMatchSet);
         break;

      case COMMUNITY_LIST:
         PolicyStatementMatchCommunityListLine communityLine = (PolicyStatementMatchCommunityListLine) matchLine;
         Set<CommunityList> newCommunityMatchSet = new LinkedHashSet<CommunityList>();
         // multiple list names not supported yet
         // for (String listName : communityLine.getListNames()) {
         // CommunityList list = c.getCommunityLists().get(listName);
         // newCommunityMatchSet.add(list);
         // }
         String clistName = communityLine.getListName();
         CommunityList clist = c.getCommunityLists().get(clistName);
         if (clist == null) {
            throw new Error("no such community list");
         }
         newCommunityMatchSet.add(clist);
         newLine = new PolicyMapMatchCommunityListLine(newCommunityMatchSet);
         break;

      case IP_ACCESS_LIST:
         PolicyStatementMatchIpAccessListLine accessLine = (PolicyStatementMatchIpAccessListLine) matchLine;
         Set<IpAccessList> newIpAccessMatchSet = new LinkedHashSet<IpAccessList>();
         for (String iplistName : accessLine.getListNames()) {
            IpAccessList list = c.getIpAccessLists().get(iplistName);
            newIpAccessMatchSet.add(list);
         }
         newLine = new PolicyMapMatchIpAccessListLine(newIpAccessMatchSet);
         break;

      case ROUTE_FILTER:
         PolicyStatementMatchIpPrefixListLine prefixLine = (PolicyStatementMatchIpPrefixListLine) matchLine;
         Set<RouteFilterList> newRouteFilterMatchSet = new LinkedHashSet<RouteFilterList>();
         for (String prefixListName : prefixLine.getListNames()) {
            RouteFilterList list = c.getRouteFilterLists().get(prefixListName);
            if (list == null) {
               // System.out.println("null for route filter: " +
               // prefixListName);
               // TODO : deal with ip v6 route filter
            }
            else {
               newRouteFilterMatchSet.add(list);
            }
         }
         newLine = new PolicyMapMatchRouteFilterListLine(newRouteFilterMatchSet);
         break;

      case NEIGHBOR:
         PolicyStatementMatchNeighborLine neighborLine = (PolicyStatementMatchNeighborLine) matchLine;
         newLine = new batfish.representation.PolicyMapMatchNeighborLine(
               new Ip(neighborLine.getNeighborIp()));
         // TODO: implement
         break;

      case PROTOCOL:
         PolicyStatementMatchProtocolLine protocolLine = (PolicyStatementMatchProtocolLine) matchLine;
         List<batfish.representation.Protocol> newProtocolList = new ArrayList<batfish.representation.Protocol>();
         for (String protocol : protocolLine.getProtocl()) {
            switch (protocol) {
            case "ospf":
               newProtocolList.add(batfish.representation.Protocol.OSPF);
               break;
            case "bgp":
               newProtocolList.add(batfish.representation.Protocol.BGP);
               break;
            case "static":
               newProtocolList.add(batfish.representation.Protocol.STATIC);
               break;
            case "direct":
               newProtocolList
                     .add(batfish.representation.Protocol.CONNECTED);
               break;
            case "aggregate":
               newProtocolList
                     .add(batfish.representation.Protocol.AGGREGATE);
               break;
            default:
               break;

            }
         }
         newLine = new batfish.representation.PolicyMapMatchProtocolLine(
               newProtocolList);
         // TODO: implement
         break;

      default:
         throw new Error("bad type");
      }
      return newLine;
   }
   private static PolicyMapSetLine toPolicyMapSetLine(Configuration c,
         PolicyStatementSetLine setLine) {
      PolicyMapSetLine newLine = null;
      switch (setLine.getSetType()) {
      case ADDITIVE_COMMUNITY:
         PolicyStatementSetAdditiveCommunityLine acLine = (PolicyStatementSetAdditiveCommunityLine) setLine;
         List<Long> addComList = new ArrayList<Long>();
         List<CommunityListLine> aLineList = c.getCommunityLists()
               .get(acLine.getCommunities()).getLines();
         for (CommunityListLine cll : aLineList) {
            String[] temp = cll.getRegex().split(":");
            long part1l = Long.parseLong(temp[0]);
            long part2l = Long.parseLong(temp[1]);
            long l = (part1l << 16) + part2l;
            addComList.add(l);
         }
         newLine = new PolicyMapSetAddCommunityLine(addComList);
         break;

      case COMMUNITY:
         PolicyStatementSetCommunityLine scLine = (PolicyStatementSetCommunityLine) setLine;
         List<Long> comList = new ArrayList<Long>();
         List<CommunityListLine> lineList = c.getCommunityLists()
               .get(scLine.getCommunities()).getLines();
         for (CommunityListLine cll : lineList) {
            String[] temp = cll.getRegex().split(":");
            long part1l = Long.parseLong(temp[0]);
            long part2l = Long.parseLong(temp[1]);
            long l = (part1l << 16) + part2l;
            comList.add(l);
         }
         // newLine = new PolicyMapSetAddCommunityLine(comList);
         newLine = new PolicyMapSetCommunityLine(comList);
         break;

      case DELETE_COMMUNITY:
         PolicyStatementSetDeleteCommunityLine dcLine = (PolicyStatementSetDeleteCommunityLine) setLine;
         CommunityList dcList = c.getCommunityLists().get(dcLine.getListName());
         newLine = new PolicyMapSetDeleteCommunityLine(dcList);
         break;

      case LOCAL_PREFERENCE:
         PolicyStatementSetLocalPreferenceLine slpLine = (PolicyStatementSetLocalPreferenceLine) setLine;
         newLine = new PolicyMapSetLocalPreferenceLine(
               slpLine.getLocalPreference());
         break;

      case METRIC:
         PolicyStatementSetMetricLine smLine = (PolicyStatementSetMetricLine) setLine;
         newLine = new PolicyMapSetMetricLine(smLine.getMetric());
         break;

      case NEXT_HOP:
         PolicyStatementSetNextHopLine snhLine = (PolicyStatementSetNextHopLine) setLine;
         List<String> nextHops = snhLine.getNextHops();
         List<Ip> nextHopsAsIps = new ArrayList<Ip>();
         for (String nextHop : nextHops) {
            nextHopsAsIps.add(new Ip(nextHop));
         }
         newLine = new PolicyMapSetNextHopLine(nextHopsAsIps);
         break;

      default:
         throw new Error("bad type");
      }
      return newLine;
   }
   public static batfish.representation.RouteFilterList toRouteFilterList(
         RouteFilter routeFilter) {
      RouteFilterList newRouteFilterList = new RouteFilterList(
            routeFilter.getName());
      for (RouteFilterLine rfLine : routeFilter.getLines()) {
         batfish.representation.RouteFilterLine newRouteFilterListLine = null;
         switch (rfLine.getType()) {
         case SUBRANGE:
            RouteFilterSubRangeLine rfSubRangeLine = (RouteFilterSubRangeLine) rfLine;
            newRouteFilterListLine = new batfish.representation.RouteFilterLengthRangeLine(
                  LineAction.ACCEPT, new Ip(rfSubRangeLine.getPrefix()),
                  rfSubRangeLine.getPrefixLength(),
                  rfSubRangeLine.getLengthRange());
            newRouteFilterList.addLine(newRouteFilterListLine);
            break;

         case THROUGH:
            RouteFilterThroughLine rfThroughLine = (RouteFilterThroughLine) rfLine;
            // System.out.println(rfThroughLine.getPrefix() + " "
            // + rfThroughLine.getPrefixLength() + " "
            // + rfThroughLine.getSecondPrefix() + " "
            // + rfThroughLine.getSecondPrefixLength());
            newRouteFilterListLine = new batfish.representation.RouteFilterThroughLine(
                  LineAction.ACCEPT, new Ip(rfThroughLine.getPrefix()),
                  rfThroughLine.getPrefixLength(), new Ip(
                        rfThroughLine.getSecondPrefix()),
                  rfThroughLine.getSecondPrefixLength());
            newRouteFilterList.addLine(newRouteFilterListLine);
            break;

         default:
            break;

         }
      }
      return newRouteFilterList;

   }

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
      return new batfish.representation.StaticRoute(prefix, prefixLength,
            nextHopIp, nextHopInterface, staticRoute.getDistance());
   }

   private int _asNum;

   private Map<String, ASPathAccessList> _asPathAccessLists;

   private List<BGPProcess> _bgpProcesses;

   private List<String> _conversionWarnings;

   private Map<String, ExpandedCommunityList> _expandedCommunityLists;

   private Map<String, ExtendedAccessList> _extendedAccessLists;

   private List<GenerateRoute> _generateRoutes;

   private String _hostname;

   private List<Interface> _interfaces;

   private List<OSPFProcess> _ospfProcesses;

   private Map<String, PolicyStatement> _policyStatements;

   private Map<String, RouteFilter> _routeFilters;

   private List<StaticRoute> _staticRoutes;

   public JuniperVendorConfiguration() {
      _asPathAccessLists = new HashMap<String, ASPathAccessList>();
      _bgpProcesses = new ArrayList<BGPProcess>();
      _conversionWarnings = new ArrayList<String>();
      _expandedCommunityLists = new HashMap<String, ExpandedCommunityList>();
      _extendedAccessLists = new HashMap<String, ExtendedAccessList>();
      _generateRoutes = new ArrayList<GenerateRoute>();
      _interfaces = new ArrayList<Interface>();
      _ospfProcesses = new ArrayList<OSPFProcess>();
      _policyStatements = new HashMap<String, PolicyStatement>();
      _routeFilters = new HashMap<String, RouteFilter>();
      _staticRoutes = new ArrayList<StaticRoute>();

   }

   public void addAsPathAccessLists(List<ASPathAccessList> lists) {
      for (ASPathAccessList apl : lists) {
         ASPathAccessList l = _asPathAccessLists.get(apl.getName());
         if (l == null) {
            _asPathAccessLists.put(apl.getName(), apl);
         }
         else {
            throw new Error("duplicate as path lists");
         }
      }

   }

   public void addBGPProcess(BGPProcess process) {
      _bgpProcesses.add(process);
   }

   public void addExpandedCommunityListLine(String name,
         ExpandedCommunityListLine line) {
      ExpandedCommunityList cl = _expandedCommunityLists.get(name);
      if (cl == null) {
         cl = new ExpandedCommunityList(name);
         _expandedCommunityLists.put(name, cl);
      }
      cl.addLine(line);
   }

   public void addExpandedCommunityLists(List<ExpandedCommunityList> lists) {
      for (ExpandedCommunityList ecl : lists) {
         ExpandedCommunityList cl = _expandedCommunityLists.get(ecl.getName());
         if (cl == null) {
            _expandedCommunityLists.put(ecl.getName(), ecl);
         }
         else {
            throw new Error("duplicate community lists");
         }

      }
   }

   public void addExtendedAccessList(ExtendedAccessList eal) {
      if (_extendedAccessLists.containsKey(eal.getId())) {
         throw new Error("duplicate extended access list name");
      }
      _extendedAccessLists.put(eal.getId(), eal);
   }

   public void addExtendedAccessListLine(String id, ExtendedAccessListLine eall) {
      if (!_extendedAccessLists.containsKey(id)) {
         _extendedAccessLists.put(id, new ExtendedAccessList(id));
      }
      _extendedAccessLists.get(id).addLine(eall);
   }

   public void addGenerateRoute(GenerateRoute gr) {
      _generateRoutes.add(gr);
   }

   public void addGenerateRoutes(List<GenerateRoute> gr) {
      _generateRoutes.addAll(gr);
   }

   public void addInterface(Interface interface1) {
      _interfaces.add(interface1);
   }

   public void addOSPFProcess(OSPFProcess process) {
      _ospfProcesses.add(process);
   }

   public void addPolicyStatementClause(PolicyStatementClause clause) {
      if (_policyStatements.containsKey(clause.getMapName())) {
         PolicyStatement ps = _policyStatements.get(clause.getMapName());
         ps.addClause(clause);
      }
      else {
         PolicyStatement ps = new PolicyStatement(clause.getMapName());
         ps.addClause(clause);
         _policyStatements.put(ps.getMapName(), ps);
      }

   }

   public void addPolicyStatements(List<PolicyStatement> ml) {
      for (PolicyStatement ps : ml) {
         _policyStatements.put(ps.getMapName(), ps);
      }
   }

   public void addRouteFilters(List<RouteFilter> fl) {
      for (RouteFilter rf : fl) {
         _routeFilters.put(rf.getName(), rf);
      }
   }

   public void addStaticRoute(StaticRoute staticRoute) {
      _staticRoutes.add(staticRoute);
   }

   public void addStaticRoutes(List<StaticRoute> staticRoutes) {
      _staticRoutes.addAll(staticRoutes);
   }

   public int getASNum() {
      return _asNum;
   }

   public List<BGPProcess> getBGPProcesses() {
      return _bgpProcesses;
   }

   @Override
   public List<String> getConversionWarnings() {
      return _conversionWarnings;
   }

   public Map<String, ExtendedAccessList> getExtendedAccessLists() {
      return _extendedAccessLists;
   }

   public List<GenerateRoute> getGenerateRoutes() {
      return _generateRoutes;
   }

   public String getHostname() {
      return _hostname;
   }

   public List<Interface> getInterfaces() {
      return _interfaces;
   }

   public List<OSPFProcess> getOSPFProcesses() {
      return _ospfProcesses;
   }

   public Map<String, PolicyStatement> getPolicyStatements() {
      return _policyStatements;
   }

   public Map<String, RouteFilter> getRouteFilter() {
      return _routeFilters;
   }

   public List<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public void setASNum(int as) {
      _asNum = as;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
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

      // convert expanded community lists to community lists
      for (ExpandedCommunityList ecl : _expandedCommunityLists.values()) {
         batfish.representation.CommunityList newCommunityList = toCommunityList(ecl);
         c.getCommunityLists()
               .put(newCommunityList.getName(), newCommunityList);
      }

      // convert route filters to route filter lists
      for (RouteFilter rf : _routeFilters.values()) {
         RouteFilterList newRouteFilterList = toRouteFilterList(rf);
         c.getRouteFilterLists().put(newRouteFilterList.getName(),
               newRouteFilterList);
      }

      // convert policy statements to policy maps
      for (PolicyStatement ps : _policyStatements.values()) {
         // TODO: fix this UCLA specific code
         if (ps.getMapName().toLowerCase().endsWith("ipv6")) {
            continue;
         }
         batfish.representation.PolicyMap newPolicyMap = toPolicyMap(c, ps);
         c.getPolicyMaps().put(newPolicyMap.getMapName(), newPolicyMap);
      }

      // convert extended access lists to access lists
      for (ExtendedAccessList eaList : _extendedAccessLists.values()) {
         IpAccessList ipaList = toIpAccessList(eaList);
         c.getIpAccessLists().put(ipaList.getName(), ipaList);
      }

      // convert bgp process
      for (BGPProcess b : _bgpProcesses) {
         batfish.representation.BgpProcess newBGP = toBgpProcess(c, b,
               _asNum);
         c.setBgpProcess(newBGP);
      }

      // convert interfaces
      for (Interface iface : _interfaces) {
         batfish.representation.Interface newInterface = toInterface(
               iface, _asNum, c.getIpAccessLists());
         c.getInterfaces().put(newInterface.getName(), newInterface);
      }

      // convert generate routes
      for (GenerateRoute gr : _generateRoutes) {
         GeneratedRoute newGeneratedRoute = toGeneratedRoute(c, gr);
         c.getGeneratedRoutes().add(newGeneratedRoute);
      }

      // convert ospf
      for (OSPFProcess o : _ospfProcesses) {
         batfish.representation.OspfProcess newOSPF = toOSPFProcess(c, o);
         c.setOspfProcess(newOSPF);
      }

      // convert static routes
      for (StaticRoute staticRoute : _staticRoutes) {
         c.getStaticRoutes().add(toStateRoute(c, staticRoute));
      }

      // get all set and added communities
      for (PolicyMap map : c.getPolicyMaps().values()) {
         for (PolicyMapClause clause : map.getClauses()) {
            for (PolicyMapSetLine setLine : clause.getSetLines()) {
               switch (setLine.getType()) {
               case ADDITIVE_COMMUNITY:
                  PolicyMapSetAddCommunityLine sacLine = (PolicyMapSetAddCommunityLine) setLine;
                  c.getCommunities().addAll(sacLine.getCommunities());
                  break;
               case COMMUNITY:
                  PolicyMapSetCommunityLine scLine = (PolicyMapSetCommunityLine) setLine;
                  c.getCommunities().addAll(scLine.getCommunities());
                  break;
               case AS_PATH_PREPEND:
               case COMMUNITY_NONE:
               case DELETE_COMMUNITY:
               case LOCAL_PREFERENCE:
               case METRIC:
               case NEXT_HOP:
                  break;
               default:
                  throw new Error("bad set type");
               }
            }
         }
      }

      return c;
   }

}
