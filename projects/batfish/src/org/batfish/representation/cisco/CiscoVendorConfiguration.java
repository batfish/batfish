package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.collections.RoleSet;
import org.batfish.main.Warnings;
import org.batfish.representation.AsPathAccessList;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.CommunityList;
import org.batfish.representation.CommunityListLine;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;
import org.batfish.representation.Ip;
import org.batfish.representation.IpAccessList;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.LineAction;
import org.batfish.representation.OspfArea;
import org.batfish.representation.OspfMetricType;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchAsPathAccessListLine;
import org.batfish.representation.PolicyMapMatchCommunityListLine;
import org.batfish.representation.PolicyMapMatchIpAccessListLine;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchPolicyLine;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PolicyMapMatchTagLine;
import org.batfish.representation.PolicyMapSetAddCommunityLine;
import org.batfish.representation.PolicyMapSetCommunityLine;
import org.batfish.representation.PolicyMapSetLine;
import org.batfish.representation.PolicyMapSetMetricLine;
import org.batfish.representation.PolicyMapSetType;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.representation.SwitchportEncapsulationType;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.VendorConversionException;
import org.batfish.util.SubRange;
import org.batfish.util.Util;

public final class CiscoVendorConfiguration extends CiscoConfiguration
      implements VendorConfiguration {

   private static final int CISCO_AGGREGATE_ROUTE_ADMIN_COST = 200;

   private static final String DEFAULT_ROUTE_FILTER_NAME = "~DEFAULT_ROUTE_FILTER~";

   private static final int MAX_ADMINISTRATIVE_COST = 32767;

   private static final String OSPF_EXPORT_CONNECTED_POLICY_NAME = "~OSPF_EXPORT_CONNECTED_POLICY~";

   private static final String OSPF_EXPORT_DEFAULT_POLICY_NAME = "~OSPF_EXPORT_DEFAULT_ROUTE_POLICY~";

   private static final String OSPF_EXPORT_STATIC_POLICY_NAME = "~OSPF_EXPORT_STATIC_POLICY~";

   private static final String OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME = "~OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER~";

   private static final long serialVersionUID = 1L;

   public static final String VENDOR_NAME = "cisco";

   private static PolicyMap makeRouteExportPolicy(Configuration c, String name,
         String prefixListName, Prefix prefix, SubRange prefixRange,
         LineAction prefixAction, Integer metric, RoutingProtocol protocol,
         PolicyMapAction policyAction) {
      PolicyMapClause clause = new PolicyMapClause();
      Set<PolicyMapMatchLine> matchLines = clause.getMatchLines();
      if (protocol != null) {
         PolicyMapMatchProtocolLine matchProtocolLine = new PolicyMapMatchProtocolLine(
               protocol);
         matchLines.add(matchProtocolLine);
      }
      if (prefixListName != null) {
         RouteFilterList newRouteFilter = c.getRouteFilterLists().get(
               prefixListName);
         if (newRouteFilter == null) {
            newRouteFilter = makeRouteFilter(prefixListName, prefix,
                  prefixRange, prefixAction);
            c.getRouteFilterLists().put(newRouteFilter.getName(),
                  newRouteFilter);
         }
         PolicyMapMatchRouteFilterListLine matchRouteLine = new PolicyMapMatchRouteFilterListLine(
               Collections.singleton(newRouteFilter));
         matchLines.add(matchRouteLine);
      }
      Set<PolicyMapSetLine> setLines = clause.getSetLines();
      if (metric != null) {
         PolicyMapSetMetricLine setMetricLine = new PolicyMapSetMetricLine(
               metric);
         setLines.add(setMetricLine);
      }
      clause.setAction(policyAction);
      clause.setName("");
      PolicyMap output = new PolicyMap(name);
      output.getClauses().add(clause);
      c.getPolicyMaps().put(output.getMapName(), output);
      return output;
   }

   private static RouteFilterList makeRouteFilter(String name, Prefix prefix,
         SubRange prefixRange, LineAction prefixAction) {
      RouteFilterList list = new RouteFilterList(name);
      RouteFilterLine line = new RouteFilterLine(prefixAction, prefix,
            prefixRange);
      list.addLine(line);
      return list;
   }

   private static AsPathAccessList toAsPathAccessList(
         IpAsPathAccessList pathList) {
      String name = pathList.getName();
      AsPathAccessList newList = new AsPathAccessList(name);
      for (IpAsPathAccessListLine fromLine : pathList.getLines()) {
         fromLine.applyTo(newList);
      }
      return newList;
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
      return new CommunityListLine(eclLine.getAction(),
            toJavaRegex(eclLine.getRegex()));
   }

   private static IpAccessList toIpAccessList(ExtendedAccessList eaList) {
      String name = eaList.getId();
      List<IpAccessListLine> lines = new ArrayList<IpAccessListLine>();
      for (ExtendedAccessListLine fromLine : eaList.getLines()) {
         IpAccessListLine newLine = new IpAccessListLine();
         newLine.setAction(fromLine.getAction());
         Ip srcIp = fromLine.getSourceIp();
         if (srcIp != null) {
            Ip srcWildcard = fromLine.getSourceWildcard();
            if (Util.isValidWildcard(srcWildcard)) {
               int srcPrefixLength = 32 - srcWildcard.numWildcardBits();
               if (srcPrefixLength > 0) {
                  Prefix srcPrefix = new Prefix(srcIp, srcPrefixLength);
                  newLine.getSourceIpRanges().add(srcPrefix);
               }
            }
            else {
               newLine.setInvalidMessage("Unsupported ip wildcard format");
            }
         }
         Ip dstIp = fromLine.getDestinationIp();
         if (dstIp != null) {
            Ip dstWildcard = fromLine.getDestinationWildcard();
            if (Util.isValidWildcard(dstWildcard)) {
               int dstPrefixLength = 32 - dstWildcard.numWildcardBits();
               if (dstPrefixLength > 0) {
                  Prefix dstPrefix = new Prefix(dstIp, dstPrefixLength);
                  newLine.getDestinationIpRanges().add(dstPrefix);
               }
            }
            else {
               newLine.setInvalidMessage("Unsupported ip wildcard format");
            }
         }
         IpProtocol protocol = fromLine.getProtocol();
         if (protocol != IpProtocol.IP) {
            newLine.getProtocols().add(protocol);
         }
         newLine.getDstPortRanges().addAll(fromLine.getDstPortRanges());
         newLine.getSrcPortRanges().addAll(fromLine.getSrcPortRanges());
         lines.add(newLine);
      }
      return new IpAccessList(name, lines);
   }

   private static String toJavaRegex(String ciscoRegex) {
      String underscoreReplacement = "(,|\\\\{|\\\\}|^|\\$| )";
      String output = ciscoRegex.replaceAll("_", underscoreReplacement);
      return output;
   }

   private static org.batfish.representation.OspfProcess toOspfProcess(
         Configuration c, OspfProcess proc) {
      org.batfish.representation.OspfProcess newProcess = new org.batfish.representation.OspfProcess();

      // establish areas and associated interfaces
      Map<Long, OspfArea> areas = newProcess.getAreas();
      List<OspfNetwork> networks = new ArrayList<OspfNetwork>();
      networks.addAll(proc.getNetworks());
      Collections.sort(networks, new Comparator<OspfNetwork>() {
         // sort so longest prefixes are first
         @Override
         public int compare(OspfNetwork lhs, OspfNetwork rhs) {
            int lhsPrefixLength = lhs.getPrefix().getPrefixLength();
            int rhsPrefixLength = rhs.getPrefix().getPrefixLength();
            int result = -Integer.compare(lhsPrefixLength, rhsPrefixLength);
            if (result == 0) {
               long lhsIp = lhs.getPrefix().getAddress().asLong();
               long rhsIp = rhs.getPrefix().getAddress().asLong();
               result = Long.compare(lhsIp, rhsIp);
            }
            return result;
         }
      });
      for (org.batfish.representation.Interface i : c.getInterfaces().values()) {
         Prefix interfacePrefix = i.getPrefix();
         if (interfacePrefix == null) {
            continue;
         }
         for (OspfNetwork network : networks) {
            Prefix networkPrefix = network.getPrefix();
            Ip networkAddress = networkPrefix.getAddress();
            Ip maskedInterfaceAddress = interfacePrefix.getAddress()
                  .getNetworkAddress(networkPrefix.getPrefixLength());
            if (maskedInterfaceAddress.equals(networkAddress)) {
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

      // policy map for default information
      if (proc.getDefaultInformationOriginate()) {
         SubRange defaultPrefixRange = new SubRange(0, 0);
         int metric = proc.getDefaultInformationMetric();
         OspfMetricType metricType = proc.getDefaultInformationMetricType();
         // add default export map with metric
         PolicyMap exportDefaultPolicy;
         String mapName = proc.getDefaultInformationOriginateMap();
         Set<PolicyMap> generationPolicies = new LinkedHashSet<PolicyMap>();
         if (mapName != null) {
            PolicyMap generationPolicy = c.getPolicyMaps().get(mapName);
            if (generationPolicy == null) {
               throw new VendorConversionException(
                     "undefined reference to generation policy map: " + mapName);
            }
            else {
               exportDefaultPolicy = makeRouteExportPolicy(c,
                     OSPF_EXPORT_DEFAULT_POLICY_NAME,
                     DEFAULT_ROUTE_FILTER_NAME, Prefix.ZERO,
                     defaultPrefixRange, LineAction.ACCEPT, metric,
                     RoutingProtocol.AGGREGATE, PolicyMapAction.PERMIT);
               newProcess.getOutboundPolicyMaps().add(exportDefaultPolicy);
               newProcess.getPolicyMetricTypes().put(exportDefaultPolicy,
                     metricType);
               generationPolicies.add(generationPolicy);
               GeneratedRoute route = new GeneratedRoute(Prefix.ZERO,
                     MAX_ADMINISTRATIVE_COST, generationPolicies);
               newProcess.getGeneratedRoutes().add(route);
            }
         }
         else if (proc.getDefaultInformationOriginateAlways()) {
            // add generated aggregate with no precondition
            exportDefaultPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_DEFAULT_POLICY_NAME, DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, defaultPrefixRange, LineAction.ACCEPT, metric,
                  RoutingProtocol.AGGREGATE, PolicyMapAction.PERMIT);
            c.getPolicyMaps().put(exportDefaultPolicy.getMapName(),
                  exportDefaultPolicy);
            newProcess.getOutboundPolicyMaps().add(exportDefaultPolicy);
            newProcess.getPolicyMetricTypes().put(exportDefaultPolicy,
                  metricType);
            GeneratedRoute route = new GeneratedRoute(Prefix.ZERO,
                  MAX_ADMINISTRATIVE_COST, null);
            newProcess.getGeneratedRoutes().add(route);
         }
         else {
            // do not generate an aggregate default route;
            // just redistribute any existing default route with the new metric
            exportDefaultPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_DEFAULT_POLICY_NAME, DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, defaultPrefixRange, LineAction.ACCEPT, metric,
                  null, PolicyMapAction.PERMIT);
            c.getPolicyMaps().put(exportDefaultPolicy.getMapName(),
                  exportDefaultPolicy);
            newProcess.getOutboundPolicyMaps().add(exportDefaultPolicy);
            newProcess.getPolicyMetricTypes().put(exportDefaultPolicy,
                  metricType);
         }
      }

      // policy map for redistributing connected routes
      // TODO: honor subnets option
      OspfRedistributionPolicy rcp = proc.getRedistributionPolicies().get(
            RoutingProtocol.CONNECTED);
      if (rcp != null) {
         Integer metric = rcp.getMetric();
         OspfMetricType metricType = rcp.getMetricType();
         boolean explicitMetric = metric != null;
         boolean routeMapMetric = false;
         if (!explicitMetric) {
            metric = OspfRedistributionPolicy.DEFAULT_REDISTRIBUTE_CONNECTED_METRIC;
         }
         // add default export map with metric
         PolicyMap exportConnectedPolicy;
         String mapName = rcp.getMap();
         if (mapName != null) {
            exportConnectedPolicy = c.getPolicyMaps().get(mapName);
            if (exportConnectedPolicy == null) {
               throw new VendorConversionException(
                     "undefined reference to policy map: " + mapName);
            }
            // crash if both an explicit metric is set and one exists in the
            // route map
            for (PolicyMapClause clause : exportConnectedPolicy.getClauses()) {
               for (PolicyMapSetLine line : clause.getSetLines()) {
                  if (line.getType() == PolicyMapSetType.METRIC) {
                     if (explicitMetric) {
                        throw new Error(
                              "Explicit redistribution metric set while route map also contains set metric line");
                     }
                     else {
                        routeMapMetric = true;
                        break;
                     }
                  }
               }
            }
            PolicyMapMatchLine matchConnectedLine = new PolicyMapMatchProtocolLine(
                  RoutingProtocol.CONNECTED);
            PolicyMapSetLine setMetricLine = null;
            // add a set metric line if no metric provided by route map
            if (!routeMapMetric) {
               // use default metric if no explicit metric is set
               setMetricLine = new PolicyMapSetMetricLine(metric);
            }
            for (PolicyMapClause clause : exportConnectedPolicy.getClauses()) {
               clause.getMatchLines().add(matchConnectedLine);
               if (!routeMapMetric) {
                  clause.getSetLines().add(setMetricLine);
               }
            }
            newProcess.getOutboundPolicyMaps().add(exportConnectedPolicy);
            newProcess.getPolicyMetricTypes().put(exportConnectedPolicy,
                  metricType);
         }
         else {
            exportConnectedPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_CONNECTED_POLICY_NAME, null, null, null, null,
                  metric, RoutingProtocol.CONNECTED, PolicyMapAction.PERMIT);
            newProcess.getOutboundPolicyMaps().add(exportConnectedPolicy);
            newProcess.getPolicyMetricTypes().put(exportConnectedPolicy,
                  metricType);
            c.getPolicyMaps().put(exportConnectedPolicy.getMapName(),
                  exportConnectedPolicy);
         }
      }

      // policy map for redistributing static routes
      // TODO: honor subnets option
      OspfRedistributionPolicy rsp = proc.getRedistributionPolicies().get(
            RoutingProtocol.STATIC);
      if (rsp != null) {
         Integer metric = rsp.getMetric();
         OspfMetricType metricType = rsp.getMetricType();
         boolean explicitMetric = metric != null;
         boolean routeMapMetric = false;
         if (!explicitMetric) {
            metric = OspfRedistributionPolicy.DEFAULT_REDISTRIBUTE_STATIC_METRIC;
         }
         // add export map with metric
         PolicyMap exportStaticPolicy;
         String mapName = rsp.getMap();
         if (mapName != null) {
            exportStaticPolicy = c.getPolicyMaps().get(mapName);
            if (exportStaticPolicy == null) {
               throw new VendorConversionException(
                     "undefined reference to policy map: " + mapName);
            }
            // crash if both an explicit metric is set and one exists in the
            // route map
            for (PolicyMapClause clause : exportStaticPolicy.getClauses()) {
               for (PolicyMapSetLine line : clause.getSetLines()) {
                  if (line.getType() == PolicyMapSetType.METRIC) {
                     if (explicitMetric) {
                        throw new Error(
                              "Explicit redistribution metric set while route map also contains set metric line");
                     }
                     else {
                        routeMapMetric = true;
                        break;
                     }
                  }
               }
            }
            PolicyMapSetLine setMetricLine = null;
            // add a set metric line if no metric provided by route map
            if (!routeMapMetric) {
               // use default metric if no explicit metric is set
               setMetricLine = new PolicyMapSetMetricLine(metric);
            }

            PolicyMapMatchLine matchStaticLine = new PolicyMapMatchProtocolLine(
                  RoutingProtocol.STATIC);
            for (PolicyMapClause clause : exportStaticPolicy.getClauses()) {
               boolean containsRouteFilterList = false;
               for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                  switch (matchLine.getType()) {
                  case ROUTE_FILTER_LIST:
                     PolicyMapMatchRouteFilterListLine rLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                     for (RouteFilterList list : rLine.getLists()) {
                        containsRouteFilterList = true;
                        list.getLines().add(
                              0,
                              new RouteFilterLine(LineAction.REJECT,
                                    Prefix.ZERO, new SubRange(0, 0)));
                     }
                     break;
                  // allowed match lines
                  case PROTOCOL:
                  case TAG:
                     break;

                  // disallowed match lines
                  case AS_PATH_ACCESS_LIST:
                  case COMMUNITY_LIST:
                  case IP_ACCESS_LIST:
                  case NEIGHBOR:
                  case POLICY:
                  default:
                     // note: don't allow ip access lists in policies that
                     // are for prefix matching
                     // i.e. convert them, or throw error if they are used
                     // ambiguously
                     throw new VendorConversionException(
                           "Unexpected match line type");
                  }
               }
               if (!containsRouteFilterList) {
                  RouteFilterList generatedRejectDefaultRouteList = c
                        .getRouteFilterLists()
                        .get(OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME);
                  if (generatedRejectDefaultRouteList == null) {
                     generatedRejectDefaultRouteList = makeRouteFilter(
                           OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                           Prefix.ZERO, new SubRange(0, 0), LineAction.REJECT);
                  }
                  Set<RouteFilterList> lists = new HashSet<RouteFilterList>();
                  lists.add(generatedRejectDefaultRouteList);
                  PolicyMapMatchLine line = new PolicyMapMatchRouteFilterListLine(
                        lists);
                  clause.getMatchLines().add(line);
               }
               Set<PolicyMapSetLine> setList = clause.getSetLines();
               clause.getMatchLines().add(matchStaticLine);
               if (!routeMapMetric) {
                  setList.add(setMetricLine);
               }
            }
            newProcess.getOutboundPolicyMaps().add(exportStaticPolicy);
            newProcess.getPolicyMetricTypes().put(exportStaticPolicy,
                  metricType);

         }
         else { // export static routes without named policy
            exportStaticPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_STATIC_POLICY_NAME,
                  OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, new SubRange(0, 0), LineAction.REJECT, metric,
                  RoutingProtocol.STATIC, PolicyMapAction.PERMIT);
            newProcess.getOutboundPolicyMaps().add(exportStaticPolicy);
            newProcess.getPolicyMetricTypes().put(exportStaticPolicy,
                  metricType);
         }
      }
      newProcess.setReferenceBandwidth(proc.getReferenceBandwidth());
      Ip routerId = proc.getRouterId();
      newProcess.setRouterId(routerId);
      return newProcess;
   }

   private static PolicyMap toPolicyMap(final Configuration c, RouteMap map) {
      PolicyMap output = new PolicyMap(map.getMapName());
      for (RouteMapClause rmClause : map.getClauses().values()) {
         output.getClauses().add(toPolicyMapClause(c, rmClause));
      }
      return output;
   }

   private static PolicyMapClause toPolicyMapClause(final Configuration c,
         RouteMapClause clause) {
      PolicyMapClause pmClause = new PolicyMapClause();
      pmClause.setAction(PolicyMapAction.fromLineAction(clause.getAction()));
      pmClause.setName(Integer.toString(clause.getSeqNum()));
      Set<PolicyMapMatchLine> matchLines = pmClause.getMatchLines();
      for (RouteMapMatchLine rmMatchLine : clause.getMatchList()) {
         PolicyMapMatchLine matchLine = toPolicyMapMatchLine(c, rmMatchLine);
         if (matchLine == null) {
            throw new Error("error converting route map match line");
         }
         matchLines.add(matchLine);
      }
      Set<PolicyMapSetLine> setLines = pmClause.getSetLines();
      for (RouteMapSetLine rmSetLine : clause.getSetList()) {
         setLines.add(rmSetLine.toPolicyMapSetLine(c));
      }
      return pmClause;
   }

   private static PolicyMapMatchLine toPolicyMapMatchLine(
         final Configuration c, RouteMapMatchLine matchLine) {
      PolicyMapMatchLine newLine = null;
      switch (matchLine.getType()) {
      case AS_PATH_ACCESS_LIST:
         RouteMapMatchAsPathAccessListLine pathLine = (RouteMapMatchAsPathAccessListLine) matchLine;
         Set<AsPathAccessList> newAsPathMatchSet = new LinkedHashSet<AsPathAccessList>();
         for (String pathListName : pathLine.getListNames()) {
            AsPathAccessList list = c.getAsPathAccessLists().get(pathListName);
            if (list == null) {
               throw new Error("null list");
            }
            newAsPathMatchSet.add(list);
         }
         newLine = new PolicyMapMatchAsPathAccessListLine(newAsPathMatchSet);
         break;

      case COMMUNITY_LIST:
         RouteMapMatchCommunityListLine communityLine = (RouteMapMatchCommunityListLine) matchLine;
         Set<CommunityList> newCommunityMatchSet = new LinkedHashSet<CommunityList>();
         for (String listName : communityLine.getListNames()) {
            CommunityList list = c.getCommunityLists().get(listName);
            if (list == null) {
               throw new VendorConversionException(
                     "Reference to nonexistent community list: " + listName);
            }
            newCommunityMatchSet.add(list);
         }
         newLine = new PolicyMapMatchCommunityListLine(newCommunityMatchSet);
         break;

      case IP_ACCESS_LIST:
         RouteMapMatchIpAccessListLine accessLine = (RouteMapMatchIpAccessListLine) matchLine;
         Set<IpAccessList> newIpAccessMatchSet = new LinkedHashSet<IpAccessList>();
         for (String listName : accessLine.getListNames()) {
            IpAccessList list = c.getIpAccessLists().get(listName);
            if (list == null) {
               throw new VendorConversionException(
                     "Reference to nonexistent ip access list: " + listName);
            }
            newIpAccessMatchSet.add(list);
         }
         newLine = new PolicyMapMatchIpAccessListLine(newIpAccessMatchSet);
         break;

      case IP_PREFIX_LIST:
         RouteMapMatchIpPrefixListLine prefixLine = (RouteMapMatchIpPrefixListLine) matchLine;
         Set<RouteFilterList> newRouteFilterMatchSet = new LinkedHashSet<RouteFilterList>();
         for (String prefixListName : prefixLine.getListNames()) {
            RouteFilterList list = c.getRouteFilterLists().get(prefixListName);
            if (list == null) {
               throw new VendorConversionException(
                     "undefined reference to route filter list: "
                           + prefixListName);
            }
            newRouteFilterMatchSet.add(list);
         }
         newLine = new PolicyMapMatchRouteFilterListLine(newRouteFilterMatchSet);
         break;

      case TAG:
         RouteMapMatchTagLine tagLine = (RouteMapMatchTagLine) matchLine;
         newLine = new PolicyMapMatchTagLine(tagLine.getTags());
         break;

      case NEIGHBOR:
         // TODO: implement
         // break;

      case PROTOCOL:
         // TODO: implement
         // break;

      default:
         throw new Error("bad type");
      }
      return newLine;
   }

   private static RouteFilterLine toRouteFilterLine(
         ExtendedAccessListLine fromLine) {
      LineAction action = fromLine.getAction();
      Ip ip = fromLine.getSourceIp();
      int prefixLength = fromLine.getSourceWildcard().inverted()
            .numSubnetBits();
      Prefix prefix = new Prefix(ip, prefixLength);
      long minSubnet = fromLine.getDestinationIp().asLong();
      long maxSubnet = minSubnet | fromLine.getDestinationWildcard().asLong();
      int minPrefixLength = fromLine.getDestinationIp().numSubnetBits();
      int maxPrefixLength = new Ip(maxSubnet).numSubnetBits();
      return new RouteFilterLine(action, prefix, new SubRange(minPrefixLength,
            maxPrefixLength));
   }

   private static RouteFilterList toRouteFilterList(ExtendedAccessList eaList) {
      String name = eaList.getId();
      RouteFilterList newList = new RouteFilterList(name);
      List<RouteFilterLine> lines = new ArrayList<RouteFilterLine>();
      for (ExtendedAccessListLine fromLine : eaList.getLines()) {
         RouteFilterLine newLine = toRouteFilterLine(fromLine);
         lines.add(newLine);
      }
      newList.addLines(lines);
      return newList;

   }

   private static RouteFilterList toRouteFilterList(PrefixList list) {
      RouteFilterList newRouteFilterList = new RouteFilterList(list.getName());
      for (PrefixListLine prefixListLine : list.getLines()) {
         RouteFilterLine newRouteFilterListLine = new RouteFilterLine(
               prefixListLine.getAction(), prefixListLine.getPrefix(),
               prefixListLine.getLengthRange());
         newRouteFilterList.addLine(newRouteFilterListLine);
      }
      return newRouteFilterList;
   }

   private static org.batfish.representation.StaticRoute toStaticRoute(
         Configuration c, StaticRoute staticRoute) {
      Ip nextHopIp = staticRoute.getNextHopIp();
      Prefix prefix = staticRoute.getPrefix();
      String nextHopInterface = staticRoute.getNextHopInterface();
      Integer oldTag = staticRoute.getTag();
      int tag;
      tag = oldTag != null ? oldTag : -1;
      return new org.batfish.representation.StaticRoute(prefix, nextHopIp,
            nextHopInterface, staticRoute.getDistance(), tag);
   }

   private final RoleSet _roles;

   private transient Set<String> _unimplementedFeatures;

   private transient Warnings _w;

   public CiscoVendorConfiguration(Set<String> unimplementedFeatures) {
      _roles = new RoleSet();
      _unimplementedFeatures = unimplementedFeatures;
   }

   private boolean containsIpAccessList(String eaListName, String mapName) {
      if (mapName != null) {
         RouteMap currentMap = _routeMaps.get(mapName);
         if (currentMap == null) {
            throw new VendorConversionException(
                  "undefined reference to routemap: " + mapName);
         }
         for (RouteMapClause clause : currentMap.getClauses().values()) {
            for (RouteMapMatchLine matchLine : clause.getMatchList()) {
               if (matchLine.getType() == RouteMapMatchType.IP_ACCESS_LIST) {
                  RouteMapMatchIpAccessListLine ipall = (RouteMapMatchIpAccessListLine) matchLine;
                  for (String listName : ipall.getListNames()) {
                     if (eaListName.equals(listName)) {
                        return true;
                     }
                  }
               }
            }
         }
      }
      return false;
   }

   private void convertForPurpose(Set<RouteMap> routingRouteMaps, RouteMap map) {
      if (routingRouteMaps.contains(map)) {
         for (RouteMapClause clause : map.getClauses().values()) {
            List<RouteMapMatchLine> matchList = clause.getMatchList();
            for (int i = 0; i < matchList.size(); i++) {
               RouteMapMatchLine line = matchList.get(i);
               if (line.getType() == RouteMapMatchType.IP_ACCESS_LIST) {
                  RouteMapMatchIpAccessListLine oldLine = (RouteMapMatchIpAccessListLine) line;
                  matchList.remove(i);
                  RouteMapMatchIpPrefixListLine newLine = new RouteMapMatchIpPrefixListLine(
                        oldLine.getListNames());
                  matchList.add(newLine);
               }
            }
         }
      }
   }

   @Override
   public RoleSet getRoles() {
      return _roles;
   }

   private Set<RouteMap> getRoutingRouteMaps() {
      Set<RouteMap> maps = new LinkedHashSet<RouteMap>();
      String currentMapName;
      RouteMap currentMap;
      // check ospf policies
      if (_ospfProcess != null) {
         OspfProcess oproc = _ospfProcess;
         for (OspfRedistributionPolicy rp : oproc.getRedistributionPolicies()
               .values()) {
            currentMapName = rp.getMap();
            if (currentMapName != null) {
               currentMap = _routeMaps.get(currentMapName);
               if (currentMap != null) {
                  maps.add(currentMap);
               }
            }
         }
         currentMapName = oproc.getDefaultInformationOriginateMap();
         if (currentMapName != null) {
            currentMap = _routeMaps.get(currentMapName);
            if (currentMap != null) {
               maps.add(currentMap);
            }
         }
      }
      // check bgp policies
      for (BgpProcess bgpProcess : _bgpProcesses.values()) {
         for (BgpRedistributionPolicy rp : bgpProcess
               .getRedistributionPolicies().values()) {
            currentMapName = rp.getMap();
            if (currentMapName != null) {
               currentMap = _routeMaps.get(currentMapName);
               if (currentMap != null) {
                  maps.add(currentMap);
               }
            }
         }
         for (BgpPeerGroup pg : bgpProcess.getAllPeerGroups()) {
            currentMapName = pg.getInboundRouteMap();
            if (currentMapName != null) {
               currentMap = _routeMaps.get(currentMapName);
               if (currentMap != null) {
                  maps.add(currentMap);
               }
            }
            currentMapName = pg.getOutboundRouteMap();
            if (currentMapName != null) {
               currentMap = _routeMaps.get(currentMapName);
               if (currentMap != null) {
                  maps.add(currentMap);
               }
            }
         }
      }
      return maps;
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public Warnings getWarnings() {
      return _w;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   private org.batfish.representation.BgpProcess toBgpProcess(
         final Configuration c, BgpProcess proc) {
      org.batfish.representation.BgpProcess newBgpProcess = new org.batfish.representation.BgpProcess();
      Map<Prefix, BgpNeighbor> newBgpNeighbors = newBgpProcess.getNeighbors();
      int defaultMetric = proc.getDefaultMetric();

      Set<BgpAggregateNetwork> summaryOnlyNetworks = new HashSet<BgpAggregateNetwork>();

      // add generated routes for aggregate addresses
      for (Entry<Prefix, BgpAggregateNetwork> e : proc.getAggregateNetworks()
            .entrySet()) {
         Prefix prefix = e.getKey();
         BgpAggregateNetwork aggNet = e.getValue();
         boolean summaryOnly = aggNet.getSummaryOnly();
         int prefixLength = prefix.getPrefixLength();
         SubRange prefixRange = new SubRange(prefixLength + 1, 32);
         LineAction prefixAction = LineAction.ACCEPT;
         String filterName = "~MATCH_SUMMARIZED_OF:" + prefix.toString() + "~";
         if (summaryOnly) {
            summaryOnlyNetworks.add(aggNet);
         }

         // create generation policy for aggregate network
         String generationPolicyName = "~AGGREGATE_ROUTE_GEN:"
               + prefix.toString() + "~";
         PolicyMap generationPolicy = makeRouteExportPolicy(c,
               generationPolicyName, filterName, prefix, prefixRange,
               prefixAction, null, null, PolicyMapAction.PERMIT);
         Set<PolicyMap> generationPolicies = new HashSet<PolicyMap>();
         generationPolicies.add(generationPolicy);
         GeneratedRoute gr = new GeneratedRoute(prefix,
               CISCO_AGGREGATE_ROUTE_ADMIN_COST, generationPolicies);
         gr.setDiscard(true);
         c.getGeneratedRoutes().add(gr);
      }

      // create policy for denying suppressed summary-only networks
      PolicyMap suppressSummaryOnly = null;
      PolicyMap suppressSummaryOnlyDenyOnMatch = null;
      if (summaryOnlyNetworks.size() > 0) {
         String suppressSummaryOnlyName = "~SUPRESS_SUMMARY_ONLY~";
         String suppressSummaryOnlyDenyOnMatchName = "~SUPRESS_SUMMARY_ONLY_DENY_ON_MATCH~";
         suppressSummaryOnly = new PolicyMap(suppressSummaryOnlyName);
         suppressSummaryOnlyDenyOnMatch = new PolicyMap(
               suppressSummaryOnlyDenyOnMatchName);
         c.getPolicyMaps().put(suppressSummaryOnlyName, suppressSummaryOnly);
         c.getPolicyMaps().put(suppressSummaryOnlyDenyOnMatchName,
               suppressSummaryOnlyDenyOnMatch);
         String matchSuppressedSummaryOnlyRoutesName = "~MATCH_SUPPRESSED_SUMMARY_ONLY~";
         RouteFilterList matchSuppressedSummaryOnlyRoutes = new RouteFilterList(
               matchSuppressedSummaryOnlyRoutesName);
         c.getRouteFilterLists().put(matchSuppressedSummaryOnlyRoutesName,
               matchSuppressedSummaryOnlyRoutes);
         for (BgpAggregateNetwork summaryOnlyNetwork : summaryOnlyNetworks) {
            Prefix prefix = summaryOnlyNetwork.getPrefix();
            int prefixLength = prefix.getPrefixLength();
            RouteFilterLine line = new RouteFilterLine(LineAction.ACCEPT,
                  prefix, new SubRange(prefixLength + 1, 32));
            matchSuppressedSummaryOnlyRoutes.addLine(line);
         }
         PolicyMapMatchRouteFilterListLine matchLine = new PolicyMapMatchRouteFilterListLine(
               Collections.singleton(matchSuppressedSummaryOnlyRoutes));
         PolicyMapClause suppressSummaryOnlyClause = new PolicyMapClause();
         suppressSummaryOnlyClause.setAction(PolicyMapAction.PERMIT);
         suppressSummaryOnlyClause.getMatchLines().add(matchLine);
         suppressSummaryOnly.getClauses().add(suppressSummaryOnlyClause);
         PolicyMapClause suppressSummaryOnlyDenyOnMatchDenyClause = new PolicyMapClause();
         suppressSummaryOnlyDenyOnMatchDenyClause
               .setAction(PolicyMapAction.DENY);
         suppressSummaryOnlyDenyOnMatchDenyClause.getMatchLines()
               .add(matchLine);
         suppressSummaryOnlyDenyOnMatch.getClauses().add(
               suppressSummaryOnlyDenyOnMatchDenyClause);
         PolicyMapClause suppressSummaryOnlyDenyOnMatchPermitClause = new PolicyMapClause();
         suppressSummaryOnlyDenyOnMatchPermitClause
               .setAction(PolicyMapAction.PERMIT);
         suppressSummaryOnlyDenyOnMatch.getClauses().add(
               suppressSummaryOnlyDenyOnMatchPermitClause);
      }

      // create redistribution origination policies
      PolicyMap redistributeStaticPolicyMap = null;
      BgpRedistributionPolicy redistributeStaticPolicy = proc
            .getRedistributionPolicies().get(RoutingProtocol.STATIC);
      if (redistributeStaticPolicy != null) {
         String mapName = redistributeStaticPolicy.getMap();
         if (mapName != null) {
            redistributeStaticPolicyMap = c.getPolicyMaps().get(mapName);
         }
         else {
            redistributeStaticPolicyMap = makeRouteExportPolicy(c,
                  "~BGP_REDISTRIBUTE_STATIC_ORIGINATION_POLICY~", null, null,
                  null, null, null, RoutingProtocol.STATIC,
                  PolicyMapAction.PERMIT);
         }
      }

      // cause ip peer groups to inherit unset fields from owning named peer
      // group if it exists, and then always from process master peer group
      for (Entry<String, NamedBgpPeerGroup> e : proc.getNamedPeerGroups()
            .entrySet()) {
         String namedPeerGroupName = e.getKey();
         NamedBgpPeerGroup namedPeerGroup = e.getValue();
         String peerSessionName = namedPeerGroup.getPeerSession();
         if (peerSessionName != null) {
            NamedBgpPeerGroup peerSession = proc.getPeerSessions().get(
                  peerSessionName);
            if (peerSession == null) {
               _w.redFlag("peer group \"" + namedPeerGroupName
                     + "\" inherits from non-existent peer-session: \""
                     + peerSessionName + "\"");
            }
            else {
               namedPeerGroup.inheritUnsetFields(peerSession);
            }
         }
      }
      Set<LeafBgpPeerGroup> leafGroups = new LinkedHashSet<LeafBgpPeerGroup>();
      leafGroups.addAll(proc.getIpPeerGroups().values());
      leafGroups.addAll(proc.getDynamicPeerGroups().values());
      for (LeafBgpPeerGroup lpg : leafGroups) {
         String groupName = lpg.getGroupName();
         if (groupName != null) {
            NamedBgpPeerGroup parentPeerGroup = proc.getNamedPeerGroups().get(
                  groupName);
            lpg.inheritUnsetFields(parentPeerGroup);
         }
         lpg.inheritUnsetFields(proc.getMasterBgpPeerGroup());
      }

      for (LeafBgpPeerGroup lpg : leafGroups) {
         // update source
         String updateSourceInterface = lpg.getUpdateSource();
         String updateSource = null;
         if (updateSourceInterface == null) {
            Ip processRouterId = proc.getRouterId();
            if (processRouterId == null) {
               processRouterId = new Ip(0l);
               for (String iname : c.getInterfaces().keySet()) {
                  if (iname.startsWith("Loopback")) {
                     Prefix prefix = c.getInterfaces().get(iname).getPrefix();
                     if (prefix != null) {
                        Ip currentIp = prefix.getAddress();
                        if (currentIp.asLong() > processRouterId.asLong()) {
                           processRouterId = currentIp;
                        }
                     }
                  }
               }
               if (processRouterId.asLong() == 0) {
                  for (org.batfish.representation.Interface currentInterface : c
                        .getInterfaces().values()) {
                     Prefix prefix = currentInterface.getPrefix();
                     if (prefix != null) {
                        Ip currentIp = prefix.getAddress();
                        if (currentIp.asLong() > processRouterId.asLong()) {
                           processRouterId = currentIp;
                        }
                     }
                  }
               }
            }
            updateSource = processRouterId.toString();
         }
         else {
            org.batfish.representation.Interface sourceInterface = c
                  .getInterfaces().get(updateSourceInterface);
            if (sourceInterface != null) {
               Prefix prefix = c.getInterfaces().get(updateSourceInterface)
                     .getPrefix();
               if (prefix != null) {
                  Ip sourceIp = prefix.getAddress();
                  updateSource = sourceIp.toString();
               }
               else {
                  throw new VendorConversionException(
                        "bgp update source interface: \""
                              + updateSourceInterface
                              + "\" not assigned an ip address");
               }
            }
            else {
               throw new VendorConversionException(
                     "reference to undefined interface: \""
                           + updateSourceInterface + "\"");
            }
         }

         PolicyMap newInboundPolicyMap = null;
         String inboundRouteMapName = lpg.getInboundRouteMap();
         if (inboundRouteMapName != null) {
            newInboundPolicyMap = c.getPolicyMaps().get(inboundRouteMapName);
            if (newInboundPolicyMap == null) {
               throw new VendorConversionException(
                     "undefined reference to inbound policy map: "
                           + inboundRouteMapName);
            }
         }
         PolicyMap newOutboundPolicyMap = null;
         String outboundRouteMapName = lpg.getOutboundRouteMap();
         if (outboundRouteMapName != null) {
            PolicyMap outboundRouteMap = c.getPolicyMaps().get(
                  outboundRouteMapName);
            if (outboundRouteMap == null) {
               throw new VendorConversionException(
                     "undefined reference to outbound policy map: "
                           + outboundRouteMapName);
            }
            if (suppressSummaryOnly == null) {
               newOutboundPolicyMap = outboundRouteMap;
            }
            else {
               String outboundPolicyName = "~COMPOSITE_OUTBOUND_POLICY:"
                     + lpg.getName() + "~";
               newOutboundPolicyMap = new PolicyMap(outboundPolicyName);
               c.getPolicyMaps().put(outboundPolicyName, newOutboundPolicyMap);
               PolicyMapClause denyClause = new PolicyMapClause();
               PolicyMapMatchPolicyLine matchSuppressPolicyLine = new PolicyMapMatchPolicyLine(
                     suppressSummaryOnly);
               denyClause.getMatchLines().add(matchSuppressPolicyLine);
               denyClause.setAction(PolicyMapAction.DENY);
               newOutboundPolicyMap.getClauses().add(denyClause);
               PolicyMapClause permitClause = new PolicyMapClause();
               permitClause.setAction(PolicyMapAction.PERMIT);
               PolicyMapMatchPolicyLine matchOutboundPolicyLine = new PolicyMapMatchPolicyLine(
                     outboundRouteMap);
               permitClause.getMatchLines().add(matchOutboundPolicyLine);
               newOutboundPolicyMap.getClauses().add(permitClause);
            }
         }
         else {
            newOutboundPolicyMap = suppressSummaryOnlyDenyOnMatch;
         }

         Set<PolicyMap> originationPolicies = new LinkedHashSet<PolicyMap>();
         // create origination prefilter from listed advertised networks
         RouteFilterList filter = new RouteFilterList("~BGP_PRE_FILTER:"
               + lpg.getName() + "~");
         for (Prefix prefix : proc.getNetworks()) {
            int prefixLen = prefix.getPrefixLength();
            RouteFilterLine line = new RouteFilterLine(LineAction.ACCEPT,
                  prefix, new SubRange(prefixLen, prefixLen));
            filter.addLine(line);
         }
         c.getRouteFilterLists().put(filter.getName(), filter);

         // add prefilter policy for explicitly advertised networks
         Set<RouteFilterList> rfLines = new LinkedHashSet<RouteFilterList>();
         rfLines.add(filter);
         PolicyMapMatchRouteFilterListLine rfLine = new PolicyMapMatchRouteFilterListLine(
               rfLines);
         PolicyMapClause clause = new PolicyMapClause();
         clause.setName("");
         clause.setAction(PolicyMapAction.PERMIT);
         Set<PolicyMapMatchLine> matchLines = clause.getMatchLines();
         matchLines.add(rfLine);
         PolicyMap explicitOriginationPolicyMap = new PolicyMap(
               "~BGP_ADVERTISED_NETWORKS_POLICY:" + lpg.getName() + "~");
         explicitOriginationPolicyMap.getClauses().add(clause);
         c.getPolicyMaps().put(explicitOriginationPolicyMap.getMapName(),
               explicitOriginationPolicyMap);
         originationPolicies.add(explicitOriginationPolicyMap);

         // add redistribution origination policies
         if (proc.getRedistributionPolicies().containsKey(
               RoutingProtocol.STATIC)) {
            originationPolicies.add(redistributeStaticPolicyMap);
         }

         // set up default export policy for this peer group
         GeneratedRoute defaultRoute = null;
         PolicyMap defaultOriginationPolicy = null;
         if (lpg.getDefaultOriginate()) {
            defaultRoute = new GeneratedRoute(Prefix.ZERO,
                  MAX_ADMINISTRATIVE_COST, new LinkedHashSet<PolicyMap>());
            defaultOriginationPolicy = makeRouteExportPolicy(
                  c,
                  "~BGP_DEFAULT_ROUTE_ORIGINATION_POLICY:" + lpg.getName()
                        + "~",
                  "BGP_DEFAULT_ROUTE_ORIGINATION_FILTER:" + lpg.getName() + "~",
                  Prefix.ZERO, new SubRange(0, 0), LineAction.ACCEPT, 0,
                  RoutingProtocol.AGGREGATE, PolicyMapAction.PERMIT);
            originationPolicies.add(defaultOriginationPolicy);
            String defaultOriginateMapName = lpg.getDefaultOriginateMap();
            if (defaultOriginateMapName != null) { // originate contingent on
                                                   // generation policy
               PolicyMap defaultRouteGenerationPolicy = c.getPolicyMaps().get(
                     defaultOriginateMapName);
               if (defaultRouteGenerationPolicy == null) {
                  throw new VendorConversionException(
                        "undefined reference to generated route policy map: "
                              + defaultOriginateMapName);
               }
               defaultRoute.getGenerationPolicies().add(
                     defaultRouteGenerationPolicy);
            }
         }

         Ip clusterId = lpg.getClusterId();
         boolean routeReflectorClient = lpg.getRouteReflectorClient();
         if (routeReflectorClient) {
            if (clusterId == null) {
               clusterId = new Ip(updateSource);
            }
         }
         boolean sendCommunity = lpg.getSendCommunity();
         if (lpg.getActive() && !lpg.getShutdown()) {
            if (lpg.getRemoteAS() == null) {
               _w.redFlag("No remote-as set for peer: " + lpg.getName());
               continue;
            }

            BgpNeighbor newNeighbor;
            if (lpg instanceof IpBgpPeerGroup) {
               IpBgpPeerGroup ipg = (IpBgpPeerGroup) lpg;
               Ip neighborAddress = ipg.getIp();
               newNeighbor = new BgpNeighbor(neighborAddress);
            }
            else if (lpg instanceof DynamicBgpPeerGroup) {
               DynamicBgpPeerGroup dpg = (DynamicBgpPeerGroup) lpg;
               Prefix neighborAddressRange = dpg.getPrefix();
               newNeighbor = new BgpNeighbor(neighborAddressRange);
            }
            else {
               throw new VendorConversionException(
                     "Invalid BGP leaf neighbor type");
            }
            newBgpNeighbors.put(newNeighbor.getPrefix(), newNeighbor);

            if (newInboundPolicyMap != null) {
               newNeighbor.addInboundPolicyMap(newInboundPolicyMap);
            }
            if (newOutboundPolicyMap != null) {
               newNeighbor.addOutboundPolicyMap(newOutboundPolicyMap);
               if (defaultOriginationPolicy != null) {
                  newNeighbor.addOutboundPolicyMap(defaultOriginationPolicy);
               }
            }
            newNeighbor.setGroupName(lpg.getGroupName());
            if (routeReflectorClient) {
               newNeighbor.setClusterId(clusterId.asLong());
            }
            if (defaultRoute != null) {
               newNeighbor.getGeneratedRoutes().add(defaultRoute);
            }
            newNeighbor.setRemoteAs(lpg.getRemoteAS());
            newNeighbor.setLocalAs(proc.getPid());
            newNeighbor.setUpdateSource(updateSource);
            newNeighbor.getOriginationPolicies().addAll(originationPolicies);
            newNeighbor.setSendCommunity(sendCommunity);
            newNeighbor.setDefaultMetric(defaultMetric);
         }
      }
      return newBgpProcess;
   }

   private org.batfish.representation.Interface toInterface(Interface iface,
         Map<String, IpAccessList> ipAccessLists,
         Map<String, PolicyMap> policyMaps) {
      org.batfish.representation.Interface newIface = new org.batfish.representation.Interface(
            iface.getName());
      newIface.setDescription(iface.getDescription());
      newIface.setActive(iface.getActive());
      newIface.setArea(iface.getArea());
      newIface.setBandwidth(iface.getBandwidth());
      if (iface.getPrefix() != null) {
         newIface.setPrefix(iface.getPrefix());
      }
      newIface.getSecondaryPrefixes().addAll(iface.getSecondaryPrefixes());
      newIface.setOspfCost(iface.getOspfCost());
      newIface.setOspfDeadInterval(iface.getOspfDeadInterval());
      newIface.setOspfHelloMultiplier(iface.getOspfHelloMultiplier());

      // switch settings
      newIface.setAccessVlan(iface.getAccessVlan());
      newIface.setNativeVlan(iface.getNativeVlan());
      newIface.setSwitchportMode(iface.getSwitchportMode());
      SwitchportEncapsulationType encapsulation = iface
            .getSwitchportTrunkEncapsulation();
      if (encapsulation == null) { // no encapsulation set, so use default..
                                   // TODO: check if this is OK
         encapsulation = SwitchportEncapsulationType.DOT1Q;
      }
      newIface.setSwitchportTrunkEncapsulation(encapsulation);
      newIface.addAllowedRanges(iface.getAllowedVlans());

      String incomingFilterName = iface.getIncomingFilter();
      if (incomingFilterName != null) {
         IpAccessList incomingFilter = ipAccessLists.get(incomingFilterName);
         if (incomingFilter == null) {
            _w.redFlag("Interface: '" + iface.getName()
                  + "' configured with non-existent incoming acl '"
                  + incomingFilterName + "'");
         }
         newIface.setIncomingFilter(incomingFilter);
      }
      String outgoingFilterName = iface.getOutgoingFilter();
      if (outgoingFilterName != null) {
         IpAccessList outgoingFilter = ipAccessLists.get(outgoingFilterName);
         if (outgoingFilter == null) {
            _w.redFlag("Interface: '" + iface.getName()
                  + "' configured with non-existent outgoing acl '"
                  + outgoingFilterName + "'");
         }
         newIface.setOutgoingFilter(outgoingFilter);
      }
      String routingPolicyName = iface.getRoutingPolicy();
      if (routingPolicyName != null) {
         PolicyMap routingPolicy = policyMaps.get(routingPolicyName);
         if (routingPolicy == null) {
            _w.redFlag("Interface: '" + iface.getName()
                  + "' configured with non-existent policy-routing route-map '"
                  + routingPolicyName + "'");
         }
         newIface.setRoutingPolicy(routingPolicy);
      }
      return newIface;
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings) {
      _w = warnings;
      final Configuration c = new Configuration(_hostname);
      c.setVendor(VENDOR_NAME);
      c.setRoles(_roles);

      // convert as path access lists to vendor independent format
      for (IpAsPathAccessList pathList : _asPathAccessLists.values()) {
         AsPathAccessList apList = toAsPathAccessList(pathList);
         c.getAsPathAccessLists().put(apList.getName(), apList);
      }

      // convert standard/expanded community lists to community lists
      for (StandardCommunityList scList : _standardCommunityLists.values()) {
         ExpandedCommunityList ecList = scList.toExpandedCommunityList();
         CommunityList cList = toCommunityList(ecList);
         c.getCommunityLists().put(cList.getName(), cList);
      }
      for (ExpandedCommunityList ecList : _expandedCommunityLists.values()) {
         CommunityList cList = toCommunityList(ecList);
         c.getCommunityLists().put(cList.getName(), cList);
      }

      // convert prefix lists to route filter lists
      for (PrefixList prefixList : _prefixLists.values()) {
         RouteFilterList newRouteFilterList = toRouteFilterList(prefixList);
         c.getRouteFilterLists().put(newRouteFilterList.getName(),
               newRouteFilterList);
      }

      // convert standard/extended access lists to access lists or route filter
      // lists
      List<ExtendedAccessList> allACLs = new ArrayList<ExtendedAccessList>();
      for (StandardAccessList saList : _standardAccessLists.values()) {
         ExtendedAccessList eaList = saList.toExtendedAccessList();
         allACLs.add(eaList);
      }
      allACLs.addAll(_extendedAccessLists.values());
      for (ExtendedAccessList eaList : allACLs) {
         if (usedForRouting(eaList)) {
            RouteFilterList rfList = toRouteFilterList(eaList);
            c.getRouteFilterLists().put(rfList.getName(), rfList);
         }
         else {
            IpAccessList ipaList = toIpAccessList(eaList);
            c.getIpAccessLists().put(ipaList.getName(), ipaList);
         }
      }

      // convert route maps to policy maps
      Set<RouteMap> routingRouteMaps = getRoutingRouteMaps();
      for (RouteMap map : _routeMaps.values()) {
         if (map.getIgnore()) {
            continue;
         }
         // TODO: replace UCLA-SPECIFIC code
         if (map.getMapName().toLowerCase().endsWith("ipv6")) {
            continue;
         }

         convertForPurpose(routingRouteMaps, map);
         PolicyMap newMap = toPolicyMap(c, map);
         c.getPolicyMaps().put(newMap.getMapName(), newMap);
      }

      // convert interfaces
      for (Interface iface : _interfaces.values()) {
         // TODO: implement vrf forwarding instead of skipping interface
         if (!iface.getVrf().equals(MASTER_VRF_NAME)) {
            continue;
         }
         org.batfish.representation.Interface newInterface = toInterface(iface,
               c.getIpAccessLists(), c.getPolicyMaps());
         c.getInterfaces().put(newInterface.getName(), newInterface);
      }

      // convert static routes
      for (StaticRoute staticRoute : _staticRoutes) {
         c.getStaticRoutes().add(toStaticRoute(c, staticRoute));
      }

      // convert ospf process
      if (_ospfProcess != null) {
         OspfProcess firstOspfProcess = _ospfProcess;
         org.batfish.representation.OspfProcess newOspfProcess = toOspfProcess(
               c, firstOspfProcess);
         c.setOspfProcess(newOspfProcess);
      }

      // convert bgp process
      // TODO: process vrf bgp processes
      BgpProcess bgpProcess = _bgpProcesses.get(MASTER_VRF_NAME);
      if (bgpProcess != null) {
         org.batfish.representation.BgpProcess newBgpProcess = toBgpProcess(c,
               bgpProcess);
         c.setBgpProcess(newBgpProcess);
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
               case ORIGIN_TYPE:
                  break;
               default:
                  throw new Error("bad set type");
               }
            }
         }
      }

      return c;
   }

   private boolean usedForRouting(ExtendedAccessList eaList) {
      String eaListName = eaList.getId();
      String currentMapName;
      // check ospf policies
      if (_ospfProcess != null) {
         OspfProcess oproc = _ospfProcess;
         for (OspfRedistributionPolicy rp : oproc.getRedistributionPolicies()
               .values()) {
            currentMapName = rp.getMap();
            if (containsIpAccessList(eaListName, currentMapName)) {
               return true;
            }
         }
         currentMapName = oproc.getDefaultInformationOriginateMap();
         if (containsIpAccessList(eaListName, currentMapName)) {
            return true;
         }
      }
      // check bgp policies
      for (BgpProcess bgpProcess : _bgpProcesses.values()) {
         for (BgpRedistributionPolicy rp : bgpProcess
               .getRedistributionPolicies().values()) {
            currentMapName = rp.getMap();
            if (containsIpAccessList(eaListName, currentMapName)) {
               return true;
            }
         }
         for (BgpPeerGroup pg : bgpProcess.getAllPeerGroups()) {
            currentMapName = pg.getInboundRouteMap();
            if (containsIpAccessList(eaListName, currentMapName)) {
               return true;
            }
            currentMapName = pg.getOutboundRouteMap();
            if (containsIpAccessList(eaListName, currentMapName)) {
               return true;
            }
            currentMapName = pg.getDefaultOriginateMap();
            if (containsIpAccessList(eaListName, currentMapName)) {
               return true;
            }
         }
         /*
          * currentMapName = _bgpProcess.getDefaultInformationOriginateMap(); if
          * (containsIpAccessList(eaListName, currentMapName)) { return true; }
          */
      }
      return false;
   }

}
