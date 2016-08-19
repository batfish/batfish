package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IsisInterfaceMode;
import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchAsPathAccessListLine;
import org.batfish.datamodel.PolicyMapMatchCommunityListLine;
import org.batfish.datamodel.PolicyMapMatchIpAccessListLine;
import org.batfish.datamodel.PolicyMapMatchLine;
import org.batfish.datamodel.PolicyMapMatchPolicyLine;
import org.batfish.datamodel.PolicyMapMatchProtocolLine;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.PolicyMapMatchTagLine;
import org.batfish.datamodel.PolicyMapSetAddCommunityLine;
import org.batfish.datamodel.PolicyMapSetCommunityLine;
import org.batfish.datamodel.PolicyMapSetLevelLine;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.PolicyMapSetMetricLine;
import org.batfish.datamodel.PolicyMapSetType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public final class CiscoVendorConfiguration extends CiscoConfiguration {

   protected static final String AS_PATH_ACCESS_LIST = "as-path acl";

   private static final String BGP_AGGREGATE_NETWORKS_FILTER_NAME = "~BGP_AGGREGATE_NETWORKS_FILTER:~";

   private static final String BGP_COMMON_EXPORT_POLICY_NAME = "~BGP_COMMON_EXPORT_POLICY~";

   private static final String BGP_NETWORK_NETWORKS_FILTER_NAME = "~BGP_NETWORK_NETWORKS_FILTER~";

   protected static final String BGP_PEER_GROUP = "bgp group";

   private static final int CISCO_AGGREGATE_ROUTE_ADMIN_COST = 200;

   protected static final String COMMUNITY_LIST = "community-list";

   private static final String DEFAULT_ROUTE_FILTER_NAME = "~DEFAULT_ROUTE_FILTER~";

   protected static final String INTERFACE = "interface";

   protected static final String IP_ACCESS_LIST = "ip acl";

   private static final String ISIS_ALLOW_SUMMARY_ROUTE_FILTER_NAME = "~ISIS_ALLOW_SUMMARY_FILTER~";

   private static final String ISIS_EXPORT_CONNECTED_POLICY_NAME = "~ISIS_EXPORT_CONNECTED_POLICY~";

   private static final String ISIS_EXPORT_STATIC_POLICY_NAME = "~ISIS_EXPORT_STATIC_POLICY~";

   private static final String ISIS_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME = "~ISIS_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER~";

   private static final String ISIS_LEAK_L1_ROUTES_POLICY_NAME = "~ISIS_LEAK_L1_ROUTES_POLICY~";

   private static final String ISIS_SUPPRESS_SUMMARIZED_ROUTE_FILTER_NAME = "~ISIS_SUPPRESS_SUMMARIZED_FILTER~";

   private static final int MAX_ADMINISTRATIVE_COST = 32767;

   private static final String OSPF_EXPORT_BGP_POLICY_NAME = "~OSPF_EXPORT_BGP_POLICY~";

   private static final String OSPF_EXPORT_BGP_REJECT_DEFAULT_ROUTE_FILTER_NAME = "~OSPF_EXPORT_BGP_REJECT_DEFAULT_ROUTE_FILTER~";

   private static final String OSPF_EXPORT_CONNECTED_POLICY_NAME = "~OSPF_EXPORT_CONNECTED_POLICY~";

   private static final String OSPF_EXPORT_DEFAULT_POLICY_NAME = "~OSPF_EXPORT_DEFAULT_ROUTE_POLICY~";

   private static final String OSPF_EXPORT_POLICY_NAME = "~OSPF_EXPORT_POLICY~";

   private static final String OSPF_EXPORT_STATIC_POLICY_NAME = "~OSPF_EXPORT_STATIC_POLICY~";

   private static final String OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME = "~OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER~";

   protected static final String PREFIX_LIST = "prefix-list";

   protected static final String ROUTE_MAP = "route-map";

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
      c.getPolicyMaps().put(output.getName(), output);
      return output;
   }

   private static RouteFilterList makeRouteFilter(String name, Prefix prefix,
         SubRange prefixRange, LineAction prefixAction) {
      RouteFilterList list = new RouteFilterList(name);
      RouteFilterLine line = new RouteFilterLine(prefixAction, prefix,
            prefixRange);
      list.addLine(line);
      // TODO: FIX TERRIBLE HACK!!!
      if (prefixAction == LineAction.REJECT) {
         RouteFilterLine acceptLine = new RouteFilterLine(LineAction.ACCEPT,
               Prefix.ZERO, new SubRange(0, 32));
         list.addLine(acceptLine);
      }
      return list;
   }

   private static String toJavaRegex(String ciscoRegex) {
      String underscoreReplacement = "(,|\\\\{|\\\\}|^|\\$| )";
      String output = ciscoRegex.replaceAll("_", underscoreReplacement);
      return output;
   }

   private final RoleSet _roles;

   private transient Set<String> _unimplementedFeatures;

   private ConfigurationFormat _vendor;

   public CiscoVendorConfiguration(Set<String> unimplementedFeatures) {
      _roles = new RoleSet();
      _unimplementedFeatures = unimplementedFeatures;
   }

   private boolean containsIpAccessList(String eaListName, String mapName) {
      if (mapName != null) {
         RouteMap currentMap = _routeMaps.get(mapName);
         if (currentMap == null) {
            undefined("Reference to undefined to route-map: " + mapName,
                  ROUTE_MAP, mapName);
         }
         else {
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
                  RouteMapMatchIpAccessListLine matchIpAccessListLine = (RouteMapMatchIpAccessListLine) line;
                  matchIpAccessListLine.setRouting(true);
               }
            }
         }
      }
   }

   private Ip getBgpRouterId(final Configuration c, BgpProcess proc) {
      Ip routerId;
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
            for (org.batfish.datamodel.Interface currentInterface : c
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
      routerId = processRouterId;
      return routerId;
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

   private void markAcls(Set<String> acls, String type, Configuration c) {
      for (String listName : acls) {
         boolean exists = _extendedAccessLists.containsKey(listName)
               || _standardAccessLists.containsKey(listName);
         if (exists) {
            String msg = type;
            ExtendedAccessList extendedAccessList = _extendedAccessLists
                  .get(listName);
            if (extendedAccessList != null) {
               extendedAccessList.getReferers().put(this, msg);
            }
            StandardAccessList standardAccessList = _standardAccessLists
                  .get(listName);
            if (standardAccessList != null) {
               standardAccessList.getReferers().put(this, msg);
            }
         }
         else {
            undefined("Reference to undefined " + type + ": " + listName, type,
                  listName);
         }
      }
   }

   private void markRouteMaps(Set<String> routeMaps, String type,
         Configuration c) {
      for (String routeMapName : routeMaps) {
         RouteMap routeMap = _routeMaps.get(routeMapName);
         if (routeMap != null) {
            String msg = type;
            routeMap.getReferers().put(this, msg);
         }
         else {
            undefined("Reference to undefined " + type + ": " + routeMapName,
                  type, routeMapName);
         }
      }
   }

   private boolean modifyRejectDefault(PolicyMapClause clause) {
      boolean containsRouteFilterList = false;
      for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
         switch (matchLine.getType()) {
         case ROUTE_FILTER_LIST:
            PolicyMapMatchRouteFilterListLine rLine = (PolicyMapMatchRouteFilterListLine) matchLine;
            for (RouteFilterList list : rLine.getLists()) {
               containsRouteFilterList = true;
               list.getLines().add(
                     0,
                     new RouteFilterLine(LineAction.REJECT, Prefix.ZERO,
                           new SubRange(0, 0)));
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
         case COLOR:
         case INTERFACE:
         case POLICY_CONJUNCTION:
         default:
            // note: don't allow ip access lists in policies that
            // are for prefix matching
            // i.e. convert them, or throw error if they are used
            // ambiguously
            throw new VendorConversionException("Unexpected match line type");
         }
      }
      return containsRouteFilterList;
   }

   private void processFailoverSettings() {
      if (_failover) {
         Interface commIface;
         Prefix commPrefix;
         Interface sigIface;
         Prefix sigPrefix;
         if (_failoverSecondary) {
            commIface = _interfaces.get(_failoverCommunicationInterface);
            commPrefix = _failoverStandbyPrefixes
                  .get(_failoverCommunicationInterfaceAlias);
            sigIface = _interfaces.get(_failoverStatefulSignalingInterface);
            sigPrefix = _failoverStandbyPrefixes
                  .get(_failoverStatefulSignalingInterfaceAlias);
            for (Interface iface : _interfaces.values()) {
               iface.setPrefix(iface.getStandbyPrefix());
            }
         }
         else {
            commIface = _interfaces.get(_failoverCommunicationInterface);
            commPrefix = _failoverPrimaryPrefixes
                  .get(_failoverCommunicationInterfaceAlias);
            sigIface = _interfaces.get(_failoverStatefulSignalingInterface);
            sigPrefix = _failoverPrimaryPrefixes
                  .get(_failoverStatefulSignalingInterfaceAlias);
         }
         commIface.setPrefix(commPrefix);
         commIface.setActive(true);
         sigIface.setPrefix(sigPrefix);
         sigIface.setActive(true);
      }
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      _vendor = format;
   }

   private AsPathAccessList toAsPathAccessList(IpAsPathAccessList pathList) {
      String name = pathList.getName();
      AsPathAccessList newList = new AsPathAccessList(name);
      for (IpAsPathAccessListLine fromLine : pathList.getLines()) {
         fromLine.applyTo(newList);
      }
      return newList;
   }

   private org.batfish.datamodel.BgpProcess toBgpProcess(final Configuration c,
         BgpProcess proc) {
      org.batfish.datamodel.BgpProcess newBgpProcess = new org.batfish.datamodel.BgpProcess();
      Map<Prefix, BgpNeighbor> newBgpNeighbors = newBgpProcess.getNeighbors();
      int defaultMetric = proc.getDefaultMetric();
      Ip bgpRouterId = getBgpRouterId(c, proc);
      newBgpProcess.setRouterId(bgpRouterId);
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
         PolicyMap generationPolicyMap = makeRouteExportPolicy(c,
               generationPolicyName, filterName, prefix, prefixRange,
               prefixAction, null, null, PolicyMapAction.PERMIT);
         RoutingPolicy currentGeneratedRoutePolicy = new RoutingPolicy(
               generationPolicyName);
         If currentGeneratedRouteConditional = new If();
         currentGeneratedRoutePolicy.getStatements().add(
               currentGeneratedRouteConditional);
         currentGeneratedRouteConditional.setGuard(new MatchPrefixSet(
               new ExplicitPrefixSet(new PrefixSpace(Collections
                     .singleton(new PrefixRange(prefix, prefixRange))))));
         currentGeneratedRouteConditional.getTrueStatements().add(
               Statements.ReturnTrue.toStaticStatement());
         c.getRoutingPolicies().put(generationPolicyName,
               currentGeneratedRoutePolicy);
         Set<PolicyMap> generationPolicies = new HashSet<PolicyMap>();
         generationPolicies.add(generationPolicyMap);
         GeneratedRoute gr = new GeneratedRoute(prefix,
               CISCO_AGGREGATE_ROUTE_ADMIN_COST, generationPolicies);
         gr.setGenerationPolicy(generationPolicyName);
         gr.setDiscard(true);
         c.getGeneratedRoutes().add(gr);

         // set attribute map for aggregate network
         String attributeMapName = aggNet.getAttributeMap();
         if (attributeMapName != null) {
            RouteMap attributeMap = _routeMaps.get(attributeMapName);
            if (attributeMap != null) {
               attributeMap.getReferers().put(aggNet,
                     "attribute-map of aggregate route: " + prefix.toString());
               PolicyMap attributePolicy = c.getPolicyMaps().get(
                     attributeMapName);
               RoutingPolicy attributeRoutingPolicy = c.getRoutingPolicies()
                     .get(attributeMapName);
               gr.getAttributePolicies().put(attributeMapName, attributePolicy);
               gr.setAttributePolicy(attributeRoutingPolicy);
            }
            else {
               undefined(
                     "Reference to undefined route-map used as attribute-map: '"
                           + attributeMapName + "'", ROUTE_MAP,
                     attributeMapName);
            }
         }
      }

      /*
       * Create common bgp export policy. This policy encompasses network
       * statements, aggregate-address with/without summary-only, redistribution
       * from other protocols, and default-origination
       */
      RoutingPolicy bgpCommonExportPolicy = new RoutingPolicy(
            BGP_COMMON_EXPORT_POLICY_NAME);
      c.getRoutingPolicies().put(BGP_COMMON_EXPORT_POLICY_NAME,
            bgpCommonExportPolicy);
      List<Statement> bgpCommonExportStatements = bgpCommonExportPolicy
            .getStatements();

      // create policy for denying suppressed summary-only networks
      PolicyMap suppressSummaryOnlyPolicyMap = null;
      PolicyMap suppressSummaryOnlyDenyOnMatchPolicyMap = null;
      if (summaryOnlyNetworks.size() > 0) {
         If suppressSummaryOnly = new If();
         bgpCommonExportStatements.add(suppressSummaryOnly);
         suppressSummaryOnly
               .setComment("Suppress summarized of summary-only aggregate-address networks");
         String suppressSummaryOnlyName = "~SUPRESS_SUMMARY_ONLY~";
         String suppressSummaryOnlyDenyOnMatchName = "~SUPRESS_SUMMARY_ONLY_DENY_ON_MATCH~";
         suppressSummaryOnlyPolicyMap = new PolicyMap(suppressSummaryOnlyName);
         suppressSummaryOnlyDenyOnMatchPolicyMap = new PolicyMap(
               suppressSummaryOnlyDenyOnMatchName);
         c.getPolicyMaps().put(suppressSummaryOnlyName,
               suppressSummaryOnlyPolicyMap);
         c.getPolicyMaps().put(suppressSummaryOnlyDenyOnMatchName,
               suppressSummaryOnlyDenyOnMatchPolicyMap);
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
         suppressSummaryOnly.setGuard(new MatchPrefixSet(new NamedPrefixSet(
               matchSuppressedSummaryOnlyRoutesName)));
         suppressSummaryOnly.getTrueStatements().add(
               Statements.ReturnFalse.toStaticStatement());
         PolicyMapClause suppressSummaryOnlyClause = new PolicyMapClause();
         suppressSummaryOnlyClause.setAction(PolicyMapAction.PERMIT);
         suppressSummaryOnlyClause.getMatchLines().add(matchLine);
         suppressSummaryOnlyPolicyMap.getClauses().add(
               suppressSummaryOnlyClause);
         PolicyMapClause suppressSummaryOnlyDenyOnMatchDenyClause = new PolicyMapClause();
         suppressSummaryOnlyDenyOnMatchDenyClause
               .setAction(PolicyMapAction.DENY);
         suppressSummaryOnlyDenyOnMatchDenyClause.getMatchLines()
               .add(matchLine);
         suppressSummaryOnlyDenyOnMatchPolicyMap.getClauses().add(
               suppressSummaryOnlyDenyOnMatchDenyClause);
         PolicyMapClause suppressSummaryOnlyDenyOnMatchPermitClause = new PolicyMapClause();
         suppressSummaryOnlyDenyOnMatchPermitClause
               .setAction(PolicyMapAction.PERMIT);
         suppressSummaryOnlyDenyOnMatchPolicyMap.getClauses().add(
               suppressSummaryOnlyDenyOnMatchPermitClause);
      }

      If preFilter = new If();
      bgpCommonExportStatements.add(preFilter);
      bgpCommonExportStatements.add(Statements.ReturnFalse.toStaticStatement());
      Disjunction preFilterConditions = new Disjunction();
      preFilter.setGuard(preFilterConditions);
      preFilter.getTrueStatements().add(
            Statements.ReturnTrue.toStaticStatement());

      // create redistribution origination policies
      PolicyMap redistributeStaticPolicyMap = null;
      BgpRedistributionPolicy redistributeStaticPolicy = proc
            .getRedistributionPolicies().get(RoutingProtocol.STATIC);
      if (redistributeStaticPolicy != null) {
         Conjunction exportStaticConditions = new Conjunction();
         exportStaticConditions
               .setComment("Redistribute static routes into BGP");
         exportStaticConditions.getConjuncts().add(
               new MatchProtocol(RoutingProtocol.STATIC));
         String mapName = redistributeStaticPolicy.getMap();
         if (mapName != null) {
            RouteMap redistributeStaticRouteMap = _routeMaps.get(mapName);
            if (redistributeStaticRouteMap != null) {
               redistributeStaticRouteMap.getReferers().put(proc,
                     "static redistribution route-map");
               exportStaticConditions.getConjuncts().add(new CallExpr(mapName));
               redistributeStaticPolicyMap = c.getPolicyMaps().get(mapName);
            }
            else {
               undefined(
                     "Reference to undefined route-map for static-to-bgp route redistribution: '"
                           + mapName + "'", ROUTE_MAP, mapName);
            }
         }
         else {
            redistributeStaticPolicyMap = makeRouteExportPolicy(c,
                  "~BGP_REDISTRIBUTE_STATIC_ORIGINATION_POLICY~", null, null,
                  null, null, null, RoutingProtocol.STATIC,
                  PolicyMapAction.PERMIT);
         }
         preFilterConditions.getDisjuncts().add(exportStaticConditions);
      }

      // cause ip peer groups to inherit unset fields from owning named peer
      // group if it exists, and then always from process master peer group
      Set<NamedBgpPeerGroup> namedGroups = new LinkedHashSet<NamedBgpPeerGroup>();
      namedGroups.addAll(proc.getNamedPeerGroups().values());
      namedGroups.addAll(proc.getPeerSessions().values());
      for (NamedBgpPeerGroup namedPeerGroup : namedGroups) {
         namedPeerGroup.inheritUnsetFields(proc, this);
      }
      Set<LeafBgpPeerGroup> leafGroups = new LinkedHashSet<LeafBgpPeerGroup>();
      leafGroups.addAll(proc.getIpPeerGroups().values());
      leafGroups.addAll(proc.getDynamicPeerGroups().values());
      for (LeafBgpPeerGroup lpg : leafGroups) {
         lpg.inheritUnsetFields(proc, this);
      }

      // create origination prefilter from listed advertised networks
      RouteFilterList localFilter = new RouteFilterList(
            BGP_NETWORK_NETWORKS_FILTER_NAME);
      for (Prefix prefix : proc.getNetworks()) {
         int prefixLen = prefix.getPrefixLength();
         RouteFilterLine line = new RouteFilterLine(LineAction.ACCEPT, prefix,
               new SubRange(prefixLen, prefixLen));
         localFilter.addLine(line);
      }
      c.getRouteFilterLists().put(localFilter.getName(), localFilter);

      // add prefilter policy for explicitly advertised networks
      Set<RouteFilterList> localRfLists = new LinkedHashSet<RouteFilterList>();
      localRfLists.add(localFilter);
      PolicyMapMatchRouteFilterListLine localRfLine = new PolicyMapMatchRouteFilterListLine(
            localRfLists);
      PolicyMapClause localAdvertisedNetworksClause = new PolicyMapClause();
      localAdvertisedNetworksClause.setName("");
      localAdvertisedNetworksClause.setAction(PolicyMapAction.PERMIT);
      localAdvertisedNetworksClause.getMatchLines().add(localRfLine);
      preFilterConditions.getDisjuncts().add(
            new MatchPrefixSet(new NamedPrefixSet(
                  BGP_NETWORK_NETWORKS_FILTER_NAME)));

      // create origination prefilter from listed aggregate advertiseed
      // networks
      RouteFilterList aggregateFilter = new RouteFilterList(
            BGP_AGGREGATE_NETWORKS_FILTER_NAME);
      for (Prefix prefix : proc.getAggregateNetworks().keySet()) {
         int prefixLen = prefix.getPrefixLength();
         RouteFilterLine line = new RouteFilterLine(LineAction.ACCEPT, prefix,
               new SubRange(prefixLen, prefixLen));
         aggregateFilter.addLine(line);
      }
      c.getRouteFilterLists().put(aggregateFilter.getName(), aggregateFilter);
      preFilterConditions.getDisjuncts().add(
            new MatchPrefixSet(new NamedPrefixSet(
                  BGP_AGGREGATE_NETWORKS_FILTER_NAME)));

      Set<RouteFilterList> aggregateRfLists = new LinkedHashSet<RouteFilterList>();
      aggregateRfLists.add(aggregateFilter);
      PolicyMapMatchRouteFilterListLine aggregateRfLine = new PolicyMapMatchRouteFilterListLine(
            aggregateRfLists);
      PolicyMapClause aggregatedAdvertisedNetworksClause = new PolicyMapClause();
      aggregatedAdvertisedNetworksClause.setName("");
      aggregatedAdvertisedNetworksClause.setAction(PolicyMapAction.PERMIT);
      aggregatedAdvertisedNetworksClause.getMatchLines().add(aggregateRfLine);
      aggregatedAdvertisedNetworksClause.getMatchLines().add(
            new PolicyMapMatchProtocolLine(RoutingProtocol.AGGREGATE));
      PolicyMap explicitOriginationPolicyMap = new PolicyMap(
            "~BGP_ADVERTISED_NETWORKS_POLICY~");
      explicitOriginationPolicyMap.getClauses().add(
            localAdvertisedNetworksClause);
      explicitOriginationPolicyMap.getClauses().add(
            aggregatedAdvertisedNetworksClause);
      c.getPolicyMaps().put(explicitOriginationPolicyMap.getName(),
            explicitOriginationPolicyMap);

      for (LeafBgpPeerGroup lpg : leafGroups) {
         // update source
         String updateSourceInterface = lpg.getUpdateSource();
         Ip updateSource = null;
         if (updateSourceInterface != null) {
            org.batfish.datamodel.Interface sourceInterface = c.getInterfaces()
                  .get(updateSourceInterface);
            if (sourceInterface != null) {
               Prefix prefix = c.getInterfaces().get(updateSourceInterface)
                     .getPrefix();
               if (prefix != null) {
                  Ip sourceIp = prefix.getAddress();
                  updateSource = sourceIp;
               }
               else {
                  _w.redFlag("bgp update source interface: '"
                        + updateSourceInterface
                        + "' not assigned an ip address");
               }
            }
            else {
               undefined("reference to undefined update source interface: '"
                     + updateSourceInterface + "'", INTERFACE,
                     updateSourceInterface);
            }
         }
         else {
            Ip neighborAddress = lpg.getNeighborPrefix().getAddress();
            for (Interface iface : _interfaces.values()) {
               for (Prefix ifacePrefix : iface.getAllPrefixes()) {
                  if (ifacePrefix.contains(neighborAddress)) {
                     Ip ifaceAddress = ifacePrefix.getAddress();
                     updateSource = ifaceAddress;
                  }
               }
            }
         }
         if (updateSource == null) {
            _w.redFlag("Could not determine update source for BGP neighbor: '"
                  + lpg.getName() + "'");
         }
         PolicyMap newInboundPolicyMap = null;
         RoutingPolicy importPolicy = null;
         String inboundRouteMapName = lpg.getInboundRouteMap();
         if (inboundRouteMapName != null) {
            newInboundPolicyMap = c.getPolicyMaps().get(inboundRouteMapName);
            importPolicy = c.getRoutingPolicies().get(inboundRouteMapName);
            if (importPolicy == null) {
               String msg = "neighbor: '" + lpg.getName() + "': ";
               String groupName = lpg.getGroupName();
               if (groupName != null) {
                  msg += "group: '" + groupName + "': ";
               }
               msg += "undefined reference to inbound route-map: '"
                     + inboundRouteMapName + "'";
               undefined(msg, ROUTE_MAP, inboundRouteMapName);
            }
            else {
               RouteMap inboundRouteMap = _routeMaps.get(inboundRouteMapName);
               inboundRouteMap.getReferers().put(lpg,
                     "inbound route-map for leaf peer-group: " + lpg.getName());
            }
         }
         PolicyMap newOutboundPolicyMap = null;
         String peerExportPolicyName = "~BGP_PEER_EXPORT_POLICY:"
               + lpg.getName() + "~";
         RoutingPolicy peerExportPolicy = new RoutingPolicy(
               peerExportPolicyName);
         c.getRoutingPolicies().put(peerExportPolicyName, peerExportPolicy);
         If peerExportConditional = new If();
         peerExportPolicy.getStatements().add(peerExportConditional);
         Conjunction peerExportConditions = new Conjunction();
         peerExportConditional.setGuard(peerExportConditions);
         peerExportConditional.getTrueStatements().add(
               Statements.ExitAccept.toStaticStatement());
         peerExportConditional.getFalseStatements().add(
               Statements.ExitReject.toStaticStatement());
         Disjunction localOrCommonOrigination = new Disjunction();
         peerExportConditions.getConjuncts().add(localOrCommonOrigination);
         localOrCommonOrigination.getDisjuncts().add(
               new CallExpr(BGP_COMMON_EXPORT_POLICY_NAME));
         String outboundRouteMapName = lpg.getOutboundRouteMap();
         if (outboundRouteMapName != null) {
            PolicyMap outboundPolicyMap = c.getPolicyMaps().get(
                  outboundRouteMapName);
            if (outboundPolicyMap == null) {
               String msg = "neighbor: '" + lpg.getName() + "': ";
               String groupName = lpg.getGroupName();
               if (groupName != null) {
                  msg += "group: '" + groupName + "': ";
               }
               msg += "undefined reference to outbound policy map: '"
                     + outboundRouteMapName + "'";
               undefined(msg, ROUTE_MAP, outboundRouteMapName);
            }
            else {
               RouteMap outboundRouteMap = _routeMaps.get(outboundRouteMapName);
               outboundRouteMap.getReferers()
                     .put(lpg,
                           "outbound route-map for leaf peer-group: "
                                 + lpg.getName());
               peerExportConditions.getConjuncts().add(
                     new CallExpr(outboundRouteMapName));
            }
            if (suppressSummaryOnlyPolicyMap == null) {
               newOutboundPolicyMap = outboundPolicyMap;
            }
            else {
               String outboundPolicyName = "~COMPOSITE_OUTBOUND_POLICY:"
                     + lpg.getName() + "~";
               newOutboundPolicyMap = new PolicyMap(outboundPolicyName);
               c.getPolicyMaps().put(outboundPolicyName, newOutboundPolicyMap);
               PolicyMapClause denyClause = new PolicyMapClause();
               PolicyMapMatchPolicyLine matchSuppressPolicyLine = new PolicyMapMatchPolicyLine(
                     suppressSummaryOnlyPolicyMap);
               denyClause.getMatchLines().add(matchSuppressPolicyLine);
               denyClause.setAction(PolicyMapAction.DENY);
               newOutboundPolicyMap.getClauses().add(denyClause);
               PolicyMapClause permitClause = new PolicyMapClause();
               permitClause.setAction(PolicyMapAction.PERMIT);
               PolicyMapMatchPolicyLine matchOutboundPolicyLine = new PolicyMapMatchPolicyLine(
                     outboundPolicyMap);
               permitClause.getMatchLines().add(matchOutboundPolicyLine);
               newOutboundPolicyMap.getClauses().add(permitClause);
            }
         }
         else {
            newOutboundPolicyMap = suppressSummaryOnlyDenyOnMatchPolicyMap;
         }

         Set<PolicyMap> originationPolicies = new LinkedHashSet<PolicyMap>();
         originationPolicies.add(explicitOriginationPolicyMap);

         // add redistribution origination policies
         if (proc.getRedistributionPolicies().containsKey(
               RoutingProtocol.STATIC)) {
            originationPolicies.add(redistributeStaticPolicyMap);
         }

         // set up default export policy for this peer group
         GeneratedRoute defaultRoute = null;
         PolicyMap defaultOriginationPolicyMap = null;
         if (lpg.getDefaultOriginate()) {
            MatchPrefixSet matchDefaultRoute = new MatchPrefixSet(
                  new ExplicitPrefixSet(new PrefixSpace(
                        Collections.singleton(new PrefixRange(Prefix.ZERO,
                              new SubRange(0, 0))))));
            localOrCommonOrigination.getDisjuncts().add(matchDefaultRoute);
            matchDefaultRoute.setComment("match default route");
            defaultRoute = new GeneratedRoute(Prefix.ZERO,
                  MAX_ADMINISTRATIVE_COST, new LinkedHashSet<PolicyMap>());
            defaultOriginationPolicyMap = makeRouteExportPolicy(
                  c,
                  "~BGP_DEFAULT_ROUTE_ORIGINATION_POLICY:" + lpg.getName()
                        + "~",
                  "BGP_DEFAULT_ROUTE_ORIGINATION_FILTER:" + lpg.getName() + "~",
                  Prefix.ZERO, new SubRange(0, 0), LineAction.ACCEPT, 0,
                  RoutingProtocol.AGGREGATE, PolicyMapAction.PERMIT);
            originationPolicies.add(defaultOriginationPolicyMap);

            String defaultOriginateMapName = lpg.getDefaultOriginateMap();
            if (defaultOriginateMapName != null) { // originate contingent on
                                                   // generation policy
               PolicyMap defaultRouteGenerationPolicyMap = c.getPolicyMaps()
                     .get(defaultOriginateMapName);
               RoutingPolicy defaultRouteGenerationPolicy = c
                     .getRoutingPolicies().get(defaultOriginateMapName);
               if (defaultRouteGenerationPolicy == null) {
                  undefined("undefined reference to generated route policy: "
                        + defaultOriginateMapName, ROUTE_MAP,
                        defaultOriginateMapName);
               }
               else {
                  RouteMap defaultRouteGenerationRouteMap = _routeMaps
                        .get(defaultOriginateMapName);
                  defaultRouteGenerationRouteMap.getReferers().put(
                        lpg,
                        "default route generation policy for leaf peer-group: "
                              + lpg.getName());
                  defaultRoute.setGenerationPolicy(defaultOriginateMapName);
               }
               defaultRoute.getGenerationPolicies().add(
                     defaultRouteGenerationPolicyMap);
            }
            else {
               String defaultRouteGenerationPolicyName = "~BGP_DEFAULT_ROUTE_GENERATION_POLICY:"
                     + lpg.getName() + "~";
               RoutingPolicy defaultRouteGenerationPolicy = new RoutingPolicy(
                     defaultRouteGenerationPolicyName);
               If defaultRouteGenerationConditional = new If();
               defaultRouteGenerationPolicy.getStatements().add(
                     defaultRouteGenerationConditional);
               defaultRouteGenerationConditional.setGuard(matchDefaultRoute);
               defaultRouteGenerationConditional.getTrueStatements().add(
                     Statements.ReturnTrue.toStaticStatement());
               c.getRoutingPolicies().put(defaultRouteGenerationPolicyName,
                     defaultRouteGenerationPolicy);
               defaultRoute
                     .setGenerationPolicy(defaultRouteGenerationPolicyName);
            }
         }

         Ip clusterId = lpg.getClusterId();
         boolean routeReflectorClient = lpg.getRouteReflectorClient();
         if (routeReflectorClient) {
            if (clusterId == null) {
               clusterId = updateSource;
            }
         }
         boolean sendCommunity = lpg.getSendCommunity();
         boolean advertiseInactive = lpg.getAdvertiseInactive();
         boolean ebgpMultihop = lpg.getEbgpMultihop();
         boolean allowasIn = lpg.getAllowAsIn();
         boolean disablePeerAsCheck = lpg.getDisablePeerAsCheck();
         String inboundPrefixListName = lpg.getInboundPrefixList();
         if (inboundPrefixListName != null) {
            PrefixList inboundPrefixList = _prefixLists
                  .get(inboundPrefixListName);
            if (inboundPrefixList != null) {
               inboundPrefixList.getReferers().put(
                     lpg,
                     "inbound prefix-list for neighbor: '" + lpg.getName()
                           + "'");
            }
            else {
               undefined(
                     "Reference to undefined inbound prefix-list: '"
                           + inboundPrefixListName + "' at neighbor: '"
                           + lpg.getName() + "'", PREFIX_LIST,
                     inboundPrefixListName);
            }
         }
         String outboundPrefixListName = lpg.getOutboundPrefixList();
         if (outboundPrefixListName != null) {
            PrefixList outboundPrefixList = _prefixLists
                  .get(outboundPrefixListName);
            if (outboundPrefixList != null) {
               outboundPrefixList.getReferers().put(
                     lpg,
                     "outbound prefix-list for neighbor: '" + lpg.getName()
                           + "'");
            }
            else {
               undefined(
                     "Reference to undefined outbound prefix-list: '"
                           + outboundPrefixListName + "' at neighbor: '"
                           + lpg.getName() + "'", PREFIX_LIST,
                     outboundPrefixListName);
            }
         }
         String description = lpg.getDescription();
         if (lpg.getActive() && !lpg.getShutdown()) {
            if (lpg.getRemoteAs() == null) {
               _w.redFlag("No remote-as set for peer: " + lpg.getName());
               continue;
            }

            BgpNeighbor newNeighbor;
            if (lpg instanceof IpBgpPeerGroup) {
               IpBgpPeerGroup ipg = (IpBgpPeerGroup) lpg;
               Ip neighborAddress = ipg.getIp();
               newNeighbor = new BgpNeighbor(neighborAddress, c);
            }
            else if (lpg instanceof DynamicBgpPeerGroup) {
               DynamicBgpPeerGroup dpg = (DynamicBgpPeerGroup) lpg;
               Prefix neighborAddressRange = dpg.getPrefix();
               newNeighbor = new BgpNeighbor(neighborAddressRange, c);
            }
            else {
               throw new VendorConversionException(
                     "Invalid BGP leaf neighbor type");
            }
            newBgpNeighbors.put(newNeighbor.getPrefix(), newNeighbor);

            newNeighbor.setAdvertiseInactive(advertiseInactive);
            newNeighbor.setAllowLocalAsIn(allowasIn);
            newNeighbor.setAllowRemoteAsOut(disablePeerAsCheck);
            if (routeReflectorClient) {
               newNeighbor.setClusterId(clusterId.asLong());
            }
            newNeighbor.setDefaultMetric(defaultMetric);
            newNeighbor.setDescription(description);
            newNeighbor.setEbgpMultihop(ebgpMultihop);
            if (defaultRoute != null) {
               newNeighbor.getGeneratedRoutes().add(defaultRoute);
            }
            newNeighbor.setGroup(lpg.getGroupName());
            if (newInboundPolicyMap != null) {
               newNeighbor.getInboundPolicyMaps().add(newInboundPolicyMap);
            }
            if (importPolicy != null) {
               newNeighbor.setImportPolicy(inboundRouteMapName);
            }
            newNeighbor.setLocalAs(proc.getName());
            newNeighbor.setLocalIp(updateSource);
            newNeighbor.getOriginationPolicies().addAll(originationPolicies);
            newNeighbor.setExportPolicy(peerExportPolicyName);
            if (newOutboundPolicyMap != null) {
               newNeighbor.getOutboundPolicyMaps().add(newOutboundPolicyMap);
               if (defaultOriginationPolicyMap != null) {
                  newNeighbor.getOutboundPolicyMaps().add(
                        defaultOriginationPolicyMap);
               }
            }
            newNeighbor.setRemoteAs(lpg.getRemoteAs());
            newNeighbor.setSendCommunity(sendCommunity);
         }
      }
      return newBgpProcess;
   }

   private CommunityList toCommunityList(ExpandedCommunityList ecList) {
      List<CommunityListLine> cllList = new ArrayList<CommunityListLine>();
      for (ExpandedCommunityListLine ecll : ecList.getLines()) {
         cllList.add(toCommunityListLine(ecll));
      }
      CommunityList cList = new CommunityList(ecList.getName(), cllList);
      return cList;
   }

   private CommunityListLine toCommunityListLine(
         ExpandedCommunityListLine eclLine) {
      String regex = eclLine.getRegex();
      String javaRegex = toJavaRegex(regex);
      return new CommunityListLine(eclLine.getAction(), javaRegex);
   }

   private org.batfish.datamodel.Interface toInterface(Interface iface,
         Map<String, IpAccessList> ipAccessLists,
         Map<String, PolicyMap> policyMaps, Configuration c) {
      org.batfish.datamodel.Interface newIface = new org.batfish.datamodel.Interface(
            iface.getName(), c);
      newIface.setDescription(iface.getDescription());
      newIface.setActive(iface.getActive());
      newIface.setBandwidth(iface.getBandwidth());
      if (iface.getPrefix() != null) {
         newIface.setPrefix(iface.getPrefix());
         newIface.getAllPrefixes().add(iface.getPrefix());
      }
      newIface.getAllPrefixes().addAll(iface.getSecondaryPrefixes());
      boolean level1 = false;
      boolean level2 = false;
      if (_isisProcess != null) {
         switch (_isisProcess.getLevel()) {
         case LEVEL_1:
            level1 = true;
            break;
         case LEVEL_1_2:
            level1 = true;
            level2 = true;
            break;
         case LEVEL_2:
            level2 = true;
            break;
         default:
            throw new VendorConversionException("Invalid IS-IS level");
         }
      }
      if (level1) {
         newIface.setIsisL1InterfaceMode(iface.getIsisInterfaceMode());
      }
      else {
         newIface.setIsisL1InterfaceMode(IsisInterfaceMode.UNSET);
      }
      if (level2) {
         newIface.setIsisL2InterfaceMode(iface.getIsisInterfaceMode());
      }
      else {
         newIface.setIsisL2InterfaceMode(IsisInterfaceMode.UNSET);
      }
      newIface.setIsisCost(iface.getIsisCost());
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
         else {
            String msg = "incoming acl for interface: " + iface.getName();
            ExtendedAccessList incomingExtendedAccessList = _extendedAccessLists
                  .get(incomingFilterName);
            if (incomingExtendedAccessList != null) {
               incomingExtendedAccessList.getReferers().put(iface, msg);
            }
            StandardAccessList incomingStandardAccessList = _standardAccessLists
                  .get(incomingFilterName);
            if (incomingStandardAccessList != null) {
               incomingStandardAccessList.getReferers().put(iface, msg);
            }
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
         else {
            String msg = "outgoing acl for interface: " + iface.getName();
            ExtendedAccessList outgoingExtendedAccessList = _extendedAccessLists
                  .get(outgoingFilterName);
            if (outgoingExtendedAccessList != null) {
               outgoingExtendedAccessList.getReferers().put(iface, msg);
            }
            StandardAccessList outgoingStandardAccessList = _standardAccessLists
                  .get(outgoingFilterName);
            if (outgoingStandardAccessList != null) {
               outgoingStandardAccessList.getReferers().put(iface, msg);
            }
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
         else {
            RouteMap routingPolicyRouteMap = _routeMaps.get(routingPolicyName);
            routingPolicyRouteMap.getReferers().put(iface,
                  "routing policy for interface: " + iface.getName());
         }
         newIface.setRoutingPolicy(routingPolicy);
      }
      return newIface;
   }

   private IpAccessList toIpAccessList(ExtendedAccessList eaList) {
      String name = eaList.getName();
      List<IpAccessListLine> lines = new ArrayList<IpAccessListLine>();
      for (ExtendedAccessListLine fromLine : eaList.getLines()) {
         IpAccessListLine newLine = new IpAccessListLine();
         newLine.setName(fromLine.getName());
         newLine.setAction(fromLine.getAction());
         IpWildcard srcIpWildcard = fromLine.getSourceIpWildcard();
         if (srcIpWildcard != null) {
            newLine.getSrcIps().add(srcIpWildcard);
         }
         IpWildcard dstIpWildcard = fromLine.getDestinationIpWildcard();
         if (dstIpWildcard != null) {
            newLine.getDstIps().add(dstIpWildcard);
         }
         // TODO: src/dst address group
         IpProtocol protocol = fromLine.getProtocol();
         if (protocol != IpProtocol.IP) {
            newLine.getIpProtocols().add(protocol);
         }
         newLine.getDstPorts().addAll(fromLine.getDstPorts());
         newLine.getSrcPorts().addAll(fromLine.getSrcPorts());
         Integer icmpType = fromLine.getIcmpType();
         if (icmpType != null) {
            newLine.setIcmpTypes(new TreeSet<SubRange>(Collections
                  .singleton(new SubRange(icmpType))));
         }
         Integer icmpCode = fromLine.getIcmpCode();
         if (icmpCode != null) {
            newLine.setIcmpCodes(new TreeSet<SubRange>(Collections
                  .singleton(new SubRange(icmpCode))));
         }
         Set<State> states = fromLine.getStates();
         newLine.getStates().addAll(states);
         List<TcpFlags> tcpFlags = fromLine.getTcpFlags();
         newLine.getTcpFlags().addAll(tcpFlags);
         Set<Integer> dscps = fromLine.getDscps();
         newLine.getDscps().addAll(dscps);
         Set<Integer> ecns = fromLine.getEcns();
         newLine.getEcns().addAll(ecns);
         lines.add(newLine);
      }
      return new IpAccessList(name, lines);
   }

   private org.batfish.datamodel.IsisProcess toIsisProcess(Configuration c,
         CiscoConfiguration oldConfig) {
      IsisProcess proc = oldConfig.getIsisProcess();
      org.batfish.datamodel.IsisProcess newProcess = new org.batfish.datamodel.IsisProcess();

      newProcess.setNetAddress(proc.getNetAddress());
      newProcess.setLevel(proc.getLevel());

      if (proc.getLevel() == IsisLevel.LEVEL_1_2) {
         PolicyMap leakL1Policy = new PolicyMap(ISIS_LEAK_L1_ROUTES_POLICY_NAME);
         c.getPolicyMaps().put(ISIS_LEAK_L1_ROUTES_POLICY_NAME, leakL1Policy);
         for (Entry<RoutingProtocol, IsisRedistributionPolicy> e : proc
               .getRedistributionPolicies().entrySet()) {
            if (!e.getKey().equals(RoutingProtocol.ISIS_L1)) {
               continue;
            }
            IsisRedistributionPolicy rp = e.getValue();
            Prefix summaryPrefix = rp.getSummaryPrefix();
            // add clause suppressing l1 summarized routes, and also add
            // aggregates for summarized addresses
            PolicyMapClause suppressClause = new PolicyMapClause();
            PolicyMapClause allowSummaryClause = new PolicyMapClause();
            leakL1Policy.getClauses().add(suppressClause);
            leakL1Policy.getClauses().add(allowSummaryClause);
            suppressClause.setAction(PolicyMapAction.DENY);
            allowSummaryClause.setAction(PolicyMapAction.PERMIT);
            String summarizedFilterName = ISIS_SUPPRESS_SUMMARIZED_ROUTE_FILTER_NAME
                  + ":" + summaryPrefix.toString();
            RouteFilterList summarizedFilter = new RouteFilterList(
                  summarizedFilterName);
            c.getRouteFilterLists().put(summarizedFilterName, summarizedFilter);
            String summaryFilterName = ISIS_ALLOW_SUMMARY_ROUTE_FILTER_NAME
                  + ":" + summaryPrefix.toString();
            RouteFilterList summaryFilter = new RouteFilterList(
                  summaryFilterName);
            c.getRouteFilterLists().put(summaryFilterName, summaryFilter);
            PolicyMapMatchRouteFilterListLine matchSummarized = new PolicyMapMatchRouteFilterListLine(
                  Collections.singleton(summarizedFilter));
            PolicyMapMatchRouteFilterListLine matchSummary = new PolicyMapMatchRouteFilterListLine(
                  Collections.singleton(summaryFilter));
            suppressClause.getMatchLines().add(matchSummarized);
            suppressClause.getMatchLines().add(
                  new PolicyMapMatchProtocolLine(RoutingProtocol.ISIS_L1));
            allowSummaryClause.getMatchLines().add(matchSummary);
            allowSummaryClause.getMatchLines().add(
                  new PolicyMapMatchProtocolLine(RoutingProtocol.AGGREGATE));
            Integer summaryMetric = rp.getMetric();
            if (summaryMetric == null) {
               summaryMetric = org.batfish.datamodel.IsisProcess.DEFAULT_ISIS_INTERFACE_COST;
            }
            allowSummaryClause.getSetLines().add(
                  new PolicyMapSetMetricLine(summaryMetric));
            IsisLevel summaryLevel = rp.getLevel();
            if (summaryLevel == null) {
               summaryLevel = IsisRedistributionPolicy.DEFAULT_LEVEL;
            }
            allowSummaryClause.getSetLines().add(
                  new PolicyMapSetLevelLine(summaryLevel));
            int length = summaryPrefix.getPrefixLength();
            int rejectLowerBound = length + 1;
            if (rejectLowerBound > 32) {
               throw new VendorConversionException("Invalid summary prefix: "
                     + summaryPrefix.toString());
            }
            SubRange summarizedRange = new SubRange(rejectLowerBound, 32);
            RouteFilterLine summarized = new RouteFilterLine(LineAction.ACCEPT,
                  summaryPrefix, summarizedRange);
            RouteFilterLine summary = new RouteFilterLine(LineAction.ACCEPT,
                  summaryPrefix, new SubRange(length, length));
            summarizedFilter.addLine(summarized);
            summaryFilter.addLine(summary);

            String filterName = "~ISIS_MATCH_SUMMARIZED_OF:"
                  + summaryPrefix.toString() + "~";
            String generationPolicyName = "~ISIS_AGGREGATE_ROUTE_GEN:"
                  + summaryPrefix.toString() + "~";
            PolicyMap generationPolicy = makeRouteExportPolicy(c,
                  generationPolicyName, filterName, summaryPrefix,
                  summarizedRange, LineAction.ACCEPT, null, null,
                  PolicyMapAction.PERMIT);
            Set<PolicyMap> generationPolicies = new HashSet<PolicyMap>();
            generationPolicies.add(generationPolicy);
            GeneratedRoute gr = new GeneratedRoute(summaryPrefix,
                  MAX_ADMINISTRATIVE_COST, generationPolicies);
            gr.setDiscard(true);
            newProcess.getGeneratedRoutes().add(gr);
         }
         // add clause allowing remaining l1 routes
         PolicyMapClause leakL1Clause = new PolicyMapClause();
         leakL1Clause.setAction(PolicyMapAction.PERMIT);
         leakL1Clause.getMatchLines().add(
               new PolicyMapMatchProtocolLine(RoutingProtocol.ISIS_L1));
         leakL1Policy.getClauses().add(leakL1Clause);
         newProcess.getOutboundPolicyMaps().add(leakL1Policy);
         leakL1Clause.getSetLines().add(
               new PolicyMapSetLevelLine(IsisLevel.LEVEL_2));

         // generate routes, policies for summary addresses
      }

      // policy map for redistributing connected routes
      // TODO: honor subnets option
      IsisRedistributionPolicy rcp = proc.getRedistributionPolicies().get(
            RoutingProtocol.CONNECTED);
      if (rcp != null) {
         Integer metric = rcp.getMetric();
         IsisLevel exportLevel = rcp.getLevel();
         boolean explicitMetric = metric != null;
         boolean routeMapMetric = false;
         if (!explicitMetric) {
            metric = IsisRedistributionPolicy.DEFAULT_REDISTRIBUTE_CONNECTED_METRIC;
         }
         // add default export map with metric
         PolicyMap exportConnectedPolicy;
         String mapName = rcp.getMap();
         if (mapName != null) {
            exportConnectedPolicy = c.getPolicyMaps().get(mapName);
            if (exportConnectedPolicy == null) {
               undefined("undefined reference to route-map: " + mapName,
                     ROUTE_MAP, mapName);
            }
            else {
               RouteMap exportConnectedRouteMap = _routeMaps.get(mapName);
               exportConnectedRouteMap.getReferers().put(proc,
                     "is-is export connected route-map");

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
               newProcess.getPolicyExportLevels().put(
                     exportConnectedPolicy.getName(), exportLevel);
            }
         }
         else {
            exportConnectedPolicy = makeRouteExportPolicy(c,
                  ISIS_EXPORT_CONNECTED_POLICY_NAME, null, null, null, null,
                  metric, RoutingProtocol.CONNECTED, PolicyMapAction.PERMIT);
            newProcess.getOutboundPolicyMaps().add(exportConnectedPolicy);
            newProcess.getPolicyExportLevels().put(
                  exportConnectedPolicy.getName(), exportLevel);
            c.getPolicyMaps().put(exportConnectedPolicy.getName(),
                  exportConnectedPolicy);
         }
      }

      // policy map for redistributing static routes
      // TODO: honor subnets option
      IsisRedistributionPolicy rsp = proc.getRedistributionPolicies().get(
            RoutingProtocol.STATIC);
      if (rsp != null) {
         Integer metric = rsp.getMetric();
         IsisLevel exportLevel = rsp.getLevel();
         boolean explicitMetric = metric != null;
         boolean routeMapMetric = false;
         if (!explicitMetric) {
            metric = IsisRedistributionPolicy.DEFAULT_REDISTRIBUTE_STATIC_METRIC;
         }
         // add export map with metric
         PolicyMap exportStaticPolicy;
         String mapName = rsp.getMap();
         if (mapName != null) {
            exportStaticPolicy = c.getPolicyMaps().get(mapName);
            if (exportStaticPolicy == null) {
               undefined("undefined reference to route-map: " + mapName,
                     ROUTE_MAP, mapName);
            }
            else {
               RouteMap exportStaticRouteMap = _routeMaps.get(mapName);
               exportStaticRouteMap.getReferers().put(proc,
                     "is-is static redistribution route-map");
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
                  boolean containsRouteFilterList = modifyRejectDefault(clause);
                  if (!containsRouteFilterList) {
                     RouteFilterList generatedRejectDefaultRouteList = c
                           .getRouteFilterLists()
                           .get(ISIS_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME);
                     if (generatedRejectDefaultRouteList == null) {
                        generatedRejectDefaultRouteList = makeRouteFilter(
                              ISIS_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                              Prefix.ZERO, new SubRange(0, 0),
                              LineAction.REJECT);
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
               newProcess.getPolicyExportLevels().put(
                     exportStaticPolicy.getName(), exportLevel);

            }
         }
         else { // export static routes without named policy
            exportStaticPolicy = makeRouteExportPolicy(c,
                  ISIS_EXPORT_STATIC_POLICY_NAME,
                  ISIS_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, new SubRange(0, 0), LineAction.REJECT, metric,
                  RoutingProtocol.STATIC, PolicyMapAction.PERMIT);
            newProcess.getOutboundPolicyMaps().add(exportStaticPolicy);
            newProcess.getPolicyExportLevels().put(
                  exportStaticPolicy.getName(), exportLevel);
         }
      }
      return newProcess;
   }

   private org.batfish.datamodel.OspfProcess toOspfProcess(Configuration c,
         CiscoConfiguration oldConfig) {
      OspfProcess proc = oldConfig.getOspfProcess();
      org.batfish.datamodel.OspfProcess newProcess = new org.batfish.datamodel.OspfProcess();

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
      for (org.batfish.datamodel.Interface i : c.getInterfaces().values()) {
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
               i.setOspfArea(newArea);
               i.setOspfEnabled(true);
               boolean passive = proc.getInterfaceBlacklist().contains(
                     i.getName())
                     || (proc.getPassiveInterfaceDefault() && !proc
                           .getInterfaceWhitelist().contains(i.getName()));
               i.setOspfPassive(passive);
               break;
            }
         }
      }

      RoutingPolicy ospfExportPolicy = new RoutingPolicy(
            OSPF_EXPORT_POLICY_NAME);
      c.getRoutingPolicies().put(OSPF_EXPORT_POLICY_NAME, ospfExportPolicy);
      List<Statement> ospfExportStatements = ospfExportPolicy.getStatements();
      newProcess.setExportPolicy(OSPF_EXPORT_POLICY_NAME);

      // policy map for default information
      if (proc.getDefaultInformationOriginate()) {
         If ospfExportDefault = new If();
         ospfExportStatements.add(ospfExportDefault);
         ospfExportDefault.setComment("OSPF export default route");
         Conjunction ospfExportDefaultConditions = new Conjunction();
         List<Statement> ospfExportDefaultStatements = ospfExportDefault
               .getTrueStatements();
         ospfExportDefaultConditions.getConjuncts().add(
               new MatchPrefixSet(new ExplicitPrefixSet(new PrefixSpace(
                     Collections.singleton(new PrefixRange(Prefix.ZERO,
                           new SubRange(0, 0)))))));
         SubRange defaultPrefixRange = new SubRange(0, 0);
         int metric = proc.getDefaultInformationMetric();
         ospfExportDefaultStatements.add(new SetMetric(metric));
         OspfMetricType metricType = proc.getDefaultInformationMetricType();
         ospfExportDefaultStatements.add(new SetOspfMetricType(metricType));
         // add default export map with metric
         PolicyMap exportDefaultPolicy;
         String mapName = proc.getDefaultInformationOriginateMap();
         Set<PolicyMap> generationPolicies = new LinkedHashSet<PolicyMap>();
         boolean useAggregateDefaultOnly;
         if (mapName != null) {
            useAggregateDefaultOnly = true;
            PolicyMap generationPolicy = c.getPolicyMaps().get(mapName);
            RoutingPolicy ospfDefaultGenerationPolicy = c.getRoutingPolicies()
                  .get(mapName);
            if (ospfDefaultGenerationPolicy == null) {
               undefined("undefined reference to generation route-map: "
                     + mapName, ROUTE_MAP, mapName);
            }
            else {
               RouteMap generationRouteMap = _routeMaps.get(mapName);
               generationRouteMap.getReferers().put(proc,
                     "ospf default-originate route-map");
               exportDefaultPolicy = makeRouteExportPolicy(c,
                     OSPF_EXPORT_DEFAULT_POLICY_NAME,
                     DEFAULT_ROUTE_FILTER_NAME, Prefix.ZERO,
                     defaultPrefixRange, LineAction.ACCEPT, metric,
                     RoutingProtocol.AGGREGATE, PolicyMapAction.PERMIT);
               newProcess.getOutboundPolicyMaps().add(exportDefaultPolicy);
               newProcess.getPolicyMetricTypes().put(
                     exportDefaultPolicy.getName(), metricType);
               generationPolicies.add(generationPolicy);
               GeneratedRoute route = new GeneratedRoute(Prefix.ZERO,
                     MAX_ADMINISTRATIVE_COST, generationPolicies);
               route.setGenerationPolicy(mapName);
               newProcess.getGeneratedRoutes().add(route);
            }
         }
         else if (proc.getDefaultInformationOriginateAlways()) {
            useAggregateDefaultOnly = true;
            // add generated aggregate with no precondition
            exportDefaultPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_DEFAULT_POLICY_NAME, DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, defaultPrefixRange, LineAction.ACCEPT, metric,
                  RoutingProtocol.AGGREGATE, PolicyMapAction.PERMIT);
            c.getPolicyMaps().put(exportDefaultPolicy.getName(),
                  exportDefaultPolicy);
            newProcess.getOutboundPolicyMaps().add(exportDefaultPolicy);
            newProcess.getPolicyMetricTypes().put(
                  exportDefaultPolicy.getName(), metricType);
            GeneratedRoute route = new GeneratedRoute(Prefix.ZERO,
                  MAX_ADMINISTRATIVE_COST, null);
            newProcess.getGeneratedRoutes().add(route);
         }
         else {
            // do not generate an aggregate default route;
            // just redistribute any existing default route with the new metric
            useAggregateDefaultOnly = false;
            exportDefaultPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_DEFAULT_POLICY_NAME, DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, defaultPrefixRange, LineAction.ACCEPT, metric,
                  null, PolicyMapAction.PERMIT);
            c.getPolicyMaps().put(exportDefaultPolicy.getName(),
                  exportDefaultPolicy);
            newProcess.getOutboundPolicyMaps().add(exportDefaultPolicy);
            newProcess.getPolicyMetricTypes().put(
                  exportDefaultPolicy.getName(), metricType);
         }
         if (useAggregateDefaultOnly) {
            ospfExportDefaultConditions.getConjuncts().add(
                  new MatchProtocol(RoutingProtocol.AGGREGATE));
         }
         ospfExportDefaultStatements.add(Statements.ExitAccept
               .toStaticStatement());
         ospfExportDefault.setGuard(ospfExportDefaultConditions);
      }

      // policy for redistributing connected routes
      // TODO: honor subnets option
      OspfRedistributionPolicy rcp = proc.getRedistributionPolicies().get(
            RoutingProtocol.CONNECTED);
      if (rcp != null) {
         If ospfExportConnected = new If();
         ospfExportStatements.add(ospfExportConnected);
         ospfExportConnected.setComment("OSPF export connected routes");
         Conjunction ospfExportConnectedConditions = new Conjunction();
         ospfExportConnectedConditions.getConjuncts().add(
               new MatchProtocol(RoutingProtocol.CONNECTED));
         List<Statement> ospfExportConnectedStatements = ospfExportConnected
               .getTrueStatements();

         Integer metric = rcp.getMetric();
         OspfMetricType metricType = rcp.getMetricType();
         ospfExportConnectedStatements.add(new SetOspfMetricType(metricType));
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
               undefined("undefined reference to route-map: " + mapName,
                     ROUTE_MAP, mapName);
            }
            else {
               RouteMap exportConnectedRouteMap = _routeMaps.get(mapName);
               exportConnectedRouteMap.getReferers().put(proc,
                     "ospf redistribute connected route-map");
               ospfExportConnectedConditions.getConjuncts().add(
                     new CallExpr(mapName));
               // crash if both an explicit metric is set and one exists in the
               // route map
               for (PolicyMapClause clause : exportConnectedPolicy.getClauses()) {
                  for (PolicyMapSetLine line : clause.getSetLines()) {
                     if (line.getType() == PolicyMapSetType.METRIC) {
                        if (explicitMetric) {
                           throw new BatfishException(
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
                  ospfExportConnectedStatements.add(new SetMetric(metric));
               }
               for (PolicyMapClause clause : exportConnectedPolicy.getClauses()) {
                  clause.getMatchLines().add(matchConnectedLine);
                  if (!routeMapMetric) {
                     clause.getSetLines().add(setMetricLine);
                  }
               }
               newProcess.getOutboundPolicyMaps().add(exportConnectedPolicy);
               newProcess.getPolicyMetricTypes().put(
                     exportConnectedPolicy.getName(), metricType);
            }
         }
         else {
            ospfExportConnectedStatements.add(new SetMetric(metric));
            exportConnectedPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_CONNECTED_POLICY_NAME, null, null, null, null,
                  metric, RoutingProtocol.CONNECTED, PolicyMapAction.PERMIT);
            newProcess.getOutboundPolicyMaps().add(exportConnectedPolicy);
            newProcess.getPolicyMetricTypes().put(
                  exportConnectedPolicy.getName(), metricType);
            c.getPolicyMaps().put(exportConnectedPolicy.getName(),
                  exportConnectedPolicy);
         }
         ospfExportConnectedStatements.add(Statements.ExitAccept
               .toStaticStatement());
         ospfExportConnected.setGuard(ospfExportConnectedConditions);
      }

      // policy map for redistributing static routes
      // TODO: honor subnets option
      OspfRedistributionPolicy rsp = proc.getRedistributionPolicies().get(
            RoutingProtocol.STATIC);
      if (rsp != null) {
         If ospfExportStatic = new If();
         ospfExportStatements.add(ospfExportStatic);
         ospfExportStatic.setComment("OSPF export static routes");
         Conjunction ospfExportStaticConditions = new Conjunction();
         ospfExportStaticConditions.getConjuncts().add(
               new MatchProtocol(RoutingProtocol.STATIC));
         List<Statement> ospfExportStaticStatements = ospfExportStatic
               .getTrueStatements();
         ospfExportStaticConditions.getConjuncts().add(
               new Not(new MatchPrefixSet(new ExplicitPrefixSet(
                     new PrefixSpace(Collections.singleton(new PrefixRange(
                           Prefix.ZERO, new SubRange(0, 0))))))));

         Integer metric = rsp.getMetric();
         OspfMetricType metricType = rsp.getMetricType();
         ospfExportStaticStatements.add(new SetOspfMetricType(metricType));
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
               undefined("undefined reference to route-map: " + mapName,
                     ROUTE_MAP, mapName);
            }
            else {
               RouteMap exportStaticRouteMap = _routeMaps.get(mapName);
               exportStaticRouteMap.getReferers().put(proc,
                     "ospf redistribute static route-map");
               ospfExportStaticConditions.getConjuncts().add(
                     new CallExpr(mapName));
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
                  ospfExportStaticStatements.add(new SetMetric(metric));
               }

               PolicyMapMatchLine matchStaticLine = new PolicyMapMatchProtocolLine(
                     RoutingProtocol.STATIC);
               for (PolicyMapClause clause : exportStaticPolicy.getClauses()) {
                  boolean containsRouteFilterList = modifyRejectDefault(clause);
                  if (!containsRouteFilterList) {
                     RouteFilterList generatedRejectDefaultRouteList = c
                           .getRouteFilterLists()
                           .get(OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME);
                     if (generatedRejectDefaultRouteList == null) {
                        generatedRejectDefaultRouteList = makeRouteFilter(
                              OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                              Prefix.ZERO, new SubRange(0, 0),
                              LineAction.REJECT);
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
               newProcess.getPolicyMetricTypes().put(
                     exportStaticPolicy.getName(), metricType);

            }
         }
         else { // export static routes without named policy
            ospfExportStaticStatements.add(new SetMetric(metric));
            exportStaticPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_STATIC_POLICY_NAME,
                  OSPF_EXPORT_STATIC_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, new SubRange(0, 0), LineAction.REJECT, metric,
                  RoutingProtocol.STATIC, PolicyMapAction.PERMIT);
            newProcess.getOutboundPolicyMaps().add(exportStaticPolicy);
            newProcess.getPolicyMetricTypes().put(exportStaticPolicy.getName(),
                  metricType);
         }
         ospfExportStaticStatements.add(Statements.ExitAccept
               .toStaticStatement());
         ospfExportStatic.setGuard(ospfExportStaticConditions);
      }

      // policy map for redistributing bgp routes
      // TODO: honor subnets option
      OspfRedistributionPolicy rbp = proc.getRedistributionPolicies().get(
            RoutingProtocol.BGP);
      if (rbp != null) {
         If ospfExportBgp = new If();
         ospfExportStatements.add(ospfExportBgp);
         ospfExportBgp.setComment("OSPF export bgp routes");
         Conjunction ospfExportBgpConditions = new Conjunction();
         ospfExportBgpConditions.getConjuncts().add(
               new MatchProtocol(RoutingProtocol.BGP));
         List<Statement> ospfExportBgpStatements = ospfExportBgp
               .getTrueStatements();
         ospfExportBgpConditions.getConjuncts().add(
               new Not(new MatchPrefixSet(new ExplicitPrefixSet(
                     new PrefixSpace(Collections.singleton(new PrefixRange(
                           Prefix.ZERO, new SubRange(0, 0))))))));

         Integer metric = rbp.getMetric();
         OspfMetricType metricType = rbp.getMetricType();
         ospfExportBgpStatements.add(new SetOspfMetricType(metricType));
         boolean explicitMetric = metric != null;
         boolean routeMapMetric = false;
         if (!explicitMetric) {
            metric = OspfRedistributionPolicy.DEFAULT_REDISTRIBUTE_BGP_METRIC;
         }
         // add export map with metric
         PolicyMap exportBgpPolicy;
         String mapName = rbp.getMap();
         if (mapName != null) {
            exportBgpPolicy = c.getPolicyMaps().get(mapName);
            if (exportBgpPolicy == null) {
               undefined("undefined reference to route-map: " + mapName,
                     ROUTE_MAP, mapName);
            }
            else {
               RouteMap exportBgpRouteMap = _routeMaps.get(mapName);
               exportBgpRouteMap.getReferers().put(proc,
                     "ospf redistribute bgp route-map");
               ospfExportBgpConditions.getConjuncts()
                     .add(new CallExpr(mapName));
               // crash if both an explicit metric is set and one exists in the
               // route map
               for (PolicyMapClause clause : exportBgpPolicy.getClauses()) {
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
                  ospfExportBgpStatements.add(new SetMetric(metric));
               }

               PolicyMapMatchLine matchBgpLine = new PolicyMapMatchProtocolLine(
                     RoutingProtocol.BGP);
               for (PolicyMapClause clause : exportBgpPolicy.getClauses()) {
                  boolean containsRouteFilterList = modifyRejectDefault(clause);
                  if (!containsRouteFilterList) {
                     RouteFilterList generatedRejectDefaultRouteList = c
                           .getRouteFilterLists()
                           .get(OSPF_EXPORT_BGP_REJECT_DEFAULT_ROUTE_FILTER_NAME);
                     if (generatedRejectDefaultRouteList == null) {
                        generatedRejectDefaultRouteList = makeRouteFilter(
                              OSPF_EXPORT_BGP_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                              Prefix.ZERO, new SubRange(0, 0),
                              LineAction.REJECT);
                     }
                     Set<RouteFilterList> lists = new HashSet<RouteFilterList>();
                     lists.add(generatedRejectDefaultRouteList);
                     PolicyMapMatchLine line = new PolicyMapMatchRouteFilterListLine(
                           lists);
                     clause.getMatchLines().add(line);
                  }
                  Set<PolicyMapSetLine> setList = clause.getSetLines();
                  clause.getMatchLines().add(matchBgpLine);
                  if (!routeMapMetric) {
                     setList.add(setMetricLine);
                  }
               }
               newProcess.getOutboundPolicyMaps().add(exportBgpPolicy);
               newProcess.getPolicyMetricTypes().put(exportBgpPolicy.getName(),
                     metricType);

            }
         }
         else { // export bgp routes without named policy
            ospfExportBgpStatements.add(new SetMetric(metric));
            exportBgpPolicy = makeRouteExportPolicy(c,
                  OSPF_EXPORT_BGP_POLICY_NAME,
                  OSPF_EXPORT_BGP_REJECT_DEFAULT_ROUTE_FILTER_NAME,
                  Prefix.ZERO, new SubRange(0, 0), LineAction.REJECT, metric,
                  RoutingProtocol.BGP, PolicyMapAction.PERMIT);
            newProcess.getOutboundPolicyMaps().add(exportBgpPolicy);
            newProcess.getPolicyMetricTypes().put(exportBgpPolicy.getName(),
                  metricType);
         }
         ospfExportBgpStatements.add(Statements.ExitAccept.toStaticStatement());
         ospfExportBgp.setGuard(ospfExportBgpConditions);
      }

      newProcess.setReferenceBandwidth(proc.getReferenceBandwidth());
      Ip routerId = proc.getRouterId();
      if (routerId == null) {
         Map<String, Interface> interfacesToCheck;
         Map<String, Interface> allInterfaces = oldConfig.getInterfaces();
         Map<String, Interface> loopbackInterfaces = new HashMap<String, Interface>();
         for (Entry<String, Interface> e : allInterfaces.entrySet()) {
            String ifaceName = e.getKey();
            Interface iface = e.getValue();
            if (ifaceName.toLowerCase().startsWith("loopback")) {
               loopbackInterfaces.put(ifaceName, iface);
            }
         }
         if (loopbackInterfaces.isEmpty()) {
            interfacesToCheck = allInterfaces;
         }
         else {
            interfacesToCheck = loopbackInterfaces;
         }
         Ip highestIp = Ip.ZERO;
         for (Interface iface : interfacesToCheck.values()) {
            Prefix prefix = iface.getPrefix();
            if (prefix == null) {
               continue;
            }
            Ip ip = prefix.getAddress();
            if (highestIp.asLong() < ip.asLong()) {
               highestIp = ip;
            }
         }
         if (highestIp == Ip.ZERO) {
            throw new VendorConversionException(
                  "No candidates for OSPF router-id");
         }
         routerId = highestIp;
      }
      newProcess.setRouterId(routerId);
      return newProcess;
   }

   private PolicyMap toPolicyMap(final Configuration c, RouteMap map) {
      PolicyMap output = new PolicyMap(map.getName());
      for (RouteMapClause rmClause : map.getClauses().values()) {
         output.getClauses().add(toPolicyMapClause(c, rmClause));
      }
      return output;
   }

   private PolicyMapClause toPolicyMapClause(final Configuration c,
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
         setLines.add(rmSetLine.toPolicyMapSetLine(this, c, _w));
      }
      return pmClause;
   }

   private PolicyMapMatchLine toPolicyMapMatchLine(final Configuration c,
         RouteMapMatchLine matchLine) {
      PolicyMapMatchLine newLine = null;
      switch (matchLine.getType()) {
      case AS_PATH_ACCESS_LIST:
         RouteMapMatchAsPathAccessListLine pathLine = (RouteMapMatchAsPathAccessListLine) matchLine;
         Set<AsPathAccessList> newAsPathMatchSet = new LinkedHashSet<AsPathAccessList>();
         for (String pathListName : pathLine.getListNames()) {
            AsPathAccessList list = c.getAsPathAccessLists().get(pathListName);
            if (list == null) {
               undefined("Reference to undefined as-path access-list: "
                     + pathListName, AS_PATH_ACCESS_LIST, pathListName);
            }
            else {
               IpAsPathAccessList ipAsPathAccessList = _asPathAccessLists
                     .get(pathListName);
               ipAsPathAccessList.getReferers().put(pathLine,
                     "route-map match ip as-path access-list");
               newAsPathMatchSet.add(list);
            }
         }
         newLine = new PolicyMapMatchAsPathAccessListLine(newAsPathMatchSet);
         break;

      case COMMUNITY_LIST:
         RouteMapMatchCommunityListLine communityLine = (RouteMapMatchCommunityListLine) matchLine;
         Set<CommunityList> newCommunityMatchSet = new LinkedHashSet<CommunityList>();
         for (String listName : communityLine.getListNames()) {
            CommunityList list = c.getCommunityLists().get(listName);
            if (list == null) {
               undefined("Reference to undefined community list: " + listName,
                     COMMUNITY_LIST, listName);
            }
            else {
               String msg = "match community line";
               StandardCommunityList standardCommunityList = _standardCommunityLists
                     .get(listName);
               if (standardCommunityList != null) {
                  standardCommunityList.getReferers().put(communityLine, msg);
               }
               ExpandedCommunityList expandedCommunityList = _expandedCommunityLists
                     .get(listName);
               if (expandedCommunityList != null) {
                  expandedCommunityList.getReferers().put(communityLine, msg);
               }
               newCommunityMatchSet.add(list);
            }
         }
         newLine = new PolicyMapMatchCommunityListLine(newCommunityMatchSet);
         break;

      case IP_ACCESS_LIST: {
         RouteMapMatchIpAccessListLine accessLine = (RouteMapMatchIpAccessListLine) matchLine;
         Set<IpAccessList> newIpAccessMatchSet = new LinkedHashSet<IpAccessList>();
         Set<RouteFilterList> newRouteFilterMatchSet = new LinkedHashSet<RouteFilterList>();
         boolean routing = accessLine.getRouting();
         for (String listName : accessLine.getListNames()) {
            Object list;
            IpAccessList ipAccessList = null;
            RouteFilterList routeFilterList = null;
            if (routing) {
               routeFilterList = c.getRouteFilterLists().get(listName);
               list = routeFilterList;
            }
            else {
               ipAccessList = c.getIpAccessLists().get(listName);
               list = ipAccessList;
            }
            if (list == null) {
               undefined("Reference to undefined ip access list: " + listName,
                     IP_ACCESS_LIST, listName);
            }
            else {
               String msg = "route-map match ip access-list line";
               ExtendedAccessList extendedAccessList = _extendedAccessLists
                     .get(listName);
               if (extendedAccessList != null) {
                  extendedAccessList.getReferers().put(accessLine, msg);
               }
               StandardAccessList standardAccessList = _standardAccessLists
                     .get(listName);
               if (standardAccessList != null) {
                  standardAccessList.getReferers().put(accessLine, msg);
               }
               if (routing) {
                  newRouteFilterMatchSet.add(routeFilterList);
               }
               else {
                  newIpAccessMatchSet.add(ipAccessList);
               }
            }
         }
         if (routing) {
            newLine = new PolicyMapMatchRouteFilterListLine(
                  newRouteFilterMatchSet);
         }
         else {
            newLine = new PolicyMapMatchIpAccessListLine(newIpAccessMatchSet);
         }
         break;
      }

      case IP_PREFIX_LIST:
         RouteMapMatchIpPrefixListLine prefixLine = (RouteMapMatchIpPrefixListLine) matchLine;
         Set<RouteFilterList> newRouteFilterMatchSet = new LinkedHashSet<RouteFilterList>();
         for (String prefixListName : prefixLine.getListNames()) {
            RouteFilterList list = c.getRouteFilterLists().get(prefixListName);
            if (list == null) {
               undefined("undefined reference to prefix-list: "
                     + prefixListName, PREFIX_LIST, prefixListName);
            }
            else {
               PrefixList prefixList = _prefixLists.get(prefixListName);
               prefixList.getReferers().put(prefixLine,
                     "route-map match prefix-list");
               newRouteFilterMatchSet.add(list);
            }
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

   private RouteFilterLine toRouteFilterLine(ExtendedAccessListLine fromLine) {
      LineAction action = fromLine.getAction();
      Ip ip = fromLine.getSourceIpWildcard().getIp();
      long minSubnet = fromLine.getDestinationIpWildcard().getIp().asLong();
      long maxSubnet = minSubnet
            | fromLine.getDestinationIpWildcard().getWildcard().asLong();
      int minPrefixLength = fromLine.getDestinationIpWildcard().getIp()
            .numSubnetBits();
      int maxPrefixLength = new Ip(maxSubnet).numSubnetBits();
      int statedPrefixLength = fromLine.getSourceIpWildcard().getWildcard()
            .inverted().numSubnetBits();
      int prefixLength = Math.min(statedPrefixLength, minPrefixLength);
      Prefix prefix = new Prefix(ip, prefixLength);
      return new RouteFilterLine(action, prefix, new SubRange(minPrefixLength,
            maxPrefixLength));
   }

   private RouteFilterList toRouteFilterList(ExtendedAccessList eaList) {
      String name = eaList.getName();
      RouteFilterList newList = new RouteFilterList(name);
      List<RouteFilterLine> lines = new ArrayList<RouteFilterLine>();
      for (ExtendedAccessListLine fromLine : eaList.getLines()) {
         RouteFilterLine newLine = toRouteFilterLine(fromLine);
         lines.add(newLine);
      }
      newList.getLines().addAll(lines);
      return newList;

   }

   private RouteFilterList toRouteFilterList(PrefixList list) {
      RouteFilterList newRouteFilterList = new RouteFilterList(list.getName());
      for (PrefixListLine prefixListLine : list.getLines()) {
         RouteFilterLine newRouteFilterListLine = new RouteFilterLine(
               prefixListLine.getAction(), prefixListLine.getPrefix(),
               prefixListLine.getLengthRange());
         newRouteFilterList.addLine(newRouteFilterListLine);
      }
      return newRouteFilterList;
   }

   private RoutingPolicy toRoutingPolicy(final Configuration c, RouteMap map) {
      RoutingPolicy output = new RoutingPolicy(map.getName());
      List<Statement> statements = output.getStatements();
      for (RouteMapClause rmClause : map.getClauses().values()) {
         Conjunction conj = new Conjunction();
         for (RouteMapMatchLine rmMatch : rmClause.getMatchList()) {
            BooleanExpr matchExpr = rmMatch.toBooleanExpr(c, this, _w);
            conj.getConjuncts().add(matchExpr);
         }
         If ifExpr = new If();
         ifExpr.setGuard(conj);
         List<Statement> matchStatements = ifExpr.getTrueStatements();
         for (RouteMapSetLine rmSet : rmClause.getSetList()) {
            rmSet.applyTo(matchStatements, this, c, _w);
         }
         switch (rmClause.getAction()) {
         case ACCEPT:
            matchStatements.add(Statements.ExitAccept.toStaticStatement());
            break;
         case REJECT:
            matchStatements.add(Statements.ExitReject.toStaticStatement());
            break;
         default:
            throw new BatfishException("Invalid action");
         }
         statements.add(ifExpr);
      }
      statements.add(Statements.ExitReject.toStaticStatement());
      return output;
   }

   private RoutingPolicy toRoutingPolicy(Configuration c,
         RoutePolicy routePolicy) {
      String name = routePolicy.getName();
      RoutingPolicy rp = new RoutingPolicy(name);
      List<Statement> statements = rp.getStatements();
      for (RoutePolicyStatement routePolicyStatement : routePolicy
            .getStatements()) {
         routePolicyStatement.applyTo(statements, this, c, _w);
      }
      If endPolicy = new If();
      If nonBoolean = new If();
      endPolicy.setGuard(BooleanExprs.CallExprContext.toStaticBooleanExpr());
      endPolicy.setTrueStatements(Collections
            .singletonList(Statements.ReturnLocalDefaultAction
                  .toStaticStatement()));
      endPolicy.setFalseStatements(Collections.singletonList(nonBoolean));
      nonBoolean.setGuard(BooleanExprs.CallStatementContext
            .toStaticBooleanExpr());
      nonBoolean.setTrueStatements(Collections.singletonList(Statements.Return
            .toStaticStatement()));
      nonBoolean.setFalseStatements(Collections
            .singletonList(Statements.DefaultAction.toStaticStatement()));
      return rp;
   }

   private org.batfish.datamodel.StaticRoute toStaticRoute(Configuration c,
         StaticRoute staticRoute) {
      Ip nextHopIp = staticRoute.getNextHopIp();
      Prefix prefix = staticRoute.getPrefix();
      String nextHopInterface = staticRoute.getNextHopInterface();
      Integer oldTag = staticRoute.getTag();
      int tag;
      tag = oldTag != null ? oldTag : -1;
      return new org.batfish.datamodel.StaticRoute(prefix, nextHopIp,
            nextHopInterface, staticRoute.getDistance(), tag);
   }

   @Override
   public Configuration toVendorIndependentConfiguration() {
      final Configuration c = new Configuration(_hostname);
      c.setConfigurationFormat(_vendor);
      c.setRoles(_roles);
      c.setDefaultInboundAction(LineAction.ACCEPT);
      c.setDefaultCrossZoneAction(LineAction.ACCEPT);

      processFailoverSettings();

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
            String msg = "used for routing";
            StandardAccessList parent = eaList.getParent();
            if (parent != null) {
               parent.getReferers().put(this, msg);
            }
            else {
               eaList.getReferers().put(this, msg);
            }
            RouteFilterList rfList = toRouteFilterList(eaList);
            c.getRouteFilterLists().put(rfList.getName(), rfList);
         }
         IpAccessList ipaList = toIpAccessList(eaList);
         c.getIpAccessLists().put(ipaList.getName(), ipaList);
      }

      // convert route maps to policy maps
      Set<RouteMap> routingRouteMaps = getRoutingRouteMaps();
      for (RouteMap map : _routeMaps.values()) {
         if (map.getIpv6()) {
            continue;
         }
         convertForPurpose(routingRouteMaps, map);
         PolicyMap newMap = toPolicyMap(c, map);
         c.getPolicyMaps().put(newMap.getName(), newMap);
         // convert route maps to RoutingPolicy objects
         RoutingPolicy newPolicy = toRoutingPolicy(c, map);
         c.getRoutingPolicies().put(newPolicy.getName(), newPolicy);
      }

      // convert RoutePolicy to RoutingPolicy
      for (RoutePolicy routePolicy : _routePolicies.values()) {
         RoutingPolicy routingPolicy = toRoutingPolicy(c, routePolicy);
         c.getRoutingPolicies().put(routingPolicy.getName(), routingPolicy);
      }

      // convert interfaces
      for (Interface iface : _interfaces.values()) {
         // TODO: implement vrf forwarding instead of skipping interface
         if (!iface.getVrf().equals(MASTER_VRF_NAME)) {
            continue;
         }
         org.batfish.datamodel.Interface newInterface = toInterface(iface,
               c.getIpAccessLists(), c.getPolicyMaps(), c);
         c.getInterfaces().put(newInterface.getName(), newInterface);
      }

      // convert static routes
      for (StaticRoute staticRoute : _staticRoutes) {
         c.getStaticRoutes().add(toStaticRoute(c, staticRoute));
      }

      // convert ospf process
      if (_ospfProcess != null) {
         org.batfish.datamodel.OspfProcess newOspfProcess = toOspfProcess(c,
               this);
         c.setOspfProcess(newOspfProcess);
      }

      // convert isis process
      if (_isisProcess != null) {
         org.batfish.datamodel.IsisProcess newIsisProcess = toIsisProcess(c,
               this);
         c.setIsisProcess(newIsisProcess);
      }

      // convert bgp process
      // TODO: process vrf bgp processes
      BgpProcess bgpProcess = _bgpProcesses.get(MASTER_VRF_NAME);
      if (bgpProcess != null) {
         org.batfish.datamodel.BgpProcess newBgpProcess = toBgpProcess(c,
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
               case LEVEL:
                  break;
               default:
                  throw new BatfishException("bad set type");
               }
            }
         }
      }
      markAcls(_lineAccessClassLists, "line access-class list", c);
      markAcls(_classMapAccessGroups, "class-map access-group", c);
      markAcls(_ntpAccessGroups, "ntp access-group", c);
      markAcls(_pimAcls, "pim acl", c);
      markAcls(_controlPlaneAccessGroups, "control-plane ip access-group", c);
      markAcls(_managementAccessGroups, "management ip access-group", c);
      markAcls(_msdpPeerSaLists, "msdp peer sa-list", c);
      markRouteMaps(_pimRouteMaps, "pim route-map", c);
      // warn about unreferenced data structures
      warnUnusedRouteMaps();
      warnUnusedIpAccessLists();
      warnUnusedPrefixLists();
      warnUnusedIpAsPathAccessLists();
      warnUnusedCommunityLists();
      c.simplifyRoutingPolicies();
      return c;
   }

   private boolean usedForRouting(ExtendedAccessList eaList) {
      String eaListName = eaList.getName();
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
      }
      return false;
   }

   private void warnUnusedCommunityLists() {
      for (Entry<String, ExpandedCommunityList> e : _expandedCommunityLists
            .entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         ExpandedCommunityList list = e.getValue();
         if (list.isUnused()) {
            unused("Unused expanded community-list: '" + name + "'",
                  COMMUNITY_LIST, name);
         }
      }
      for (Entry<String, StandardCommunityList> e : _standardCommunityLists
            .entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         StandardCommunityList list = e.getValue();
         if (list.isUnused()) {
            unused("Unused standard community-list: '" + name + "'",
                  COMMUNITY_LIST, name);
         }
      }
   }

   private void warnUnusedIpAccessLists() {
      for (Entry<String, ExtendedAccessList> e : _extendedAccessLists
            .entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         ExtendedAccessList acl = e.getValue();
         if (!acl.getIpv6() && acl.isUnused()) {
            unused("Unused extended access-list: '" + name + "'",
                  IP_ACCESS_LIST, name);
         }
      }
      for (Entry<String, StandardAccessList> e : _standardAccessLists
            .entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         StandardAccessList acl = e.getValue();
         if (acl.isUnused()) {
            unused("Unused standard access-list: '" + name + "'",
                  IP_ACCESS_LIST, name);
         }
      }
   }

   private void warnUnusedIpAsPathAccessLists() {
      for (Entry<String, IpAsPathAccessList> e : _asPathAccessLists.entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         IpAsPathAccessList asPathAccessList = e.getValue();
         if (asPathAccessList.isUnused()) {
            unused("Unused as-path access-list: '" + name + "'",
                  AS_PATH_ACCESS_LIST, name);
         }
      }
   }

   private void warnUnusedPrefixLists() {
      for (Entry<String, PrefixList> e : _prefixLists.entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         PrefixList prefixList = e.getValue();
         if (prefixList.isUnused()) {
            unused("Unused prefix-list: '" + name + "'", PREFIX_LIST, name);
         }
      }
   }

   private void warnUnusedRouteMaps() {
      for (Entry<String, RouteMap> e : _routeMaps.entrySet()) {
         String name = e.getKey();
         if (name.startsWith("~")) {
            continue;
         }
         RouteMap routeMap = e.getValue();
         if (!routeMap.getIpv6() && routeMap.isUnused()) {
            unused("Unused route-map: '" + name + "'", ROUTE_MAP, name);
         }
      }
   }

}
