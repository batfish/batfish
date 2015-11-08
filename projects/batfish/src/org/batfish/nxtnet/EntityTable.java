package org.batfish.nxtnet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.batfish.collections.CommunitySet;
import org.batfish.representation.AsPath;
import org.batfish.representation.BgpAdvertisement;
import org.batfish.representation.Flow;
import org.batfish.representation.Ip;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.OriginType;
import org.batfish.representation.PrecomputedRoute;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class EntityTable {

   private final Map<Long, AsPath> _asPaths;

   private final Map<Long, BgpAdvertisement> _bgpAdvertisements;

   private final Map<Long, CommunitySet> _communities;

   private final Map<Long, Flow> _flows;

   private final Map<Long, Prefix> _networks;

   private final PredicateInfo _predicateInfo;

   private final Map<String, Relation> _relations;

   private final Map<Long, PrecomputedRoute> _routes;

   public EntityTable(Map<String, String> nxtnetPredicateContents,
         PredicateInfo predicateInfo) {
      _predicateInfo = predicateInfo;
      _relations = buildRelations(nxtnetPredicateContents);
      _asPaths = new HashMap<Long, AsPath>();
      _bgpAdvertisements = new HashMap<Long, BgpAdvertisement>();
      _communities = new HashMap<Long, CommunitySet>();
      _flows = new HashMap<Long, Flow>();
      _networks = new HashMap<Long, Prefix>();
      _routes = new HashMap<Long, PrecomputedRoute>();
      populateNetworks();
      populateFlows();
      populateRoutes();
      populateAsPaths();
      populateCommunities();
      populateBgpAdvertisements();
   }

   private Map<String, Relation> buildRelations(
         Map<String, String> nxtnetPredicateContents) {
      Map<String, Relation> relations = new HashMap<String, Relation>();
      for (Entry<String, String> e : nxtnetPredicateContents.entrySet()) {
         String relationName = e.getKey();
         String text = e.getValue();
         Relation relation = new Relation.Builder(relationName).build(
               _predicateInfo, text);
         relations.put(relationName, relation);
      }
      return relations;
   }

   public BgpAdvertisement getBgpAdvertisement(Long index) {
      return _bgpAdvertisements.get(index);
   }

   public Flow getFlow(Long index) {
      return _flows.get(index);
   }

   private List<Long> getLongColumn(Relation relation, int column) {
      return ((LongColumn) relation.getColumns().get(column)).getRows();
   }

   public Prefix getNetwork(Long index) {
      return _networks.get(index);
   }

   public PrecomputedRoute getPrecomputedRoute(Long index) {
      PrecomputedRoute route = _routes.get(index);
      if (route.getNextHopIp().equals(PrecomputedRoute.UNSET_ROUTE_NEXT_HOP_IP)) {
         return null;
      }
      if (!route.getNextHopInterface().equals(
            PrecomputedRoute.UNSET_NEXT_HOP_INTERFACE)) {
         return null;
      }
      return route;
   }

   public PrecomputedRoute getRoute(Long index) {
      return _routes.get(index);
   }

   private List<String> getStringColumn(Relation relation, int column) {
      return ((StringColumn) relation.getColumns().get(column)).getRows();
   }

   private void populateAsPaths() {
      Relation advertisementPathSizeRelation = _relations
            .get("AdvertisementPathSize");
      List<Long> sizeAdvertIndices = getLongColumn(
            advertisementPathSizeRelation, 0);
      List<Long> sizes = getLongColumn(advertisementPathSizeRelation, 1);
      int numAsPaths = sizeAdvertIndices.size();
      for (int i = 0; i < numAsPaths; i++) {
         Long advertIndex = sizeAdvertIndices.get(i);
         int size = sizes.get(i).intValue();
         AsPath asPath = new AsPath(size);
         _asPaths.put(advertIndex, asPath);
      }
      Relation advertisementPathRelation = _relations.get("AdvertisementPath");
      List<Long> pathAdvertIndices = getLongColumn(advertisementPathRelation, 0);
      List<Long> pathListIndices = getLongColumn(advertisementPathRelation, 1);
      List<Long> asPathAses = getLongColumn(advertisementPathRelation, 2);
      int numPathListEntries = asPathAses.size();
      for (int i = 0; i < numPathListEntries; i++) {
         Long advertIndex = pathAdvertIndices.get(i);
         AsPath asPath = _asPaths.get(advertIndex);
         int as = asPathAses.get(i).intValue();
         int pathListIndex = pathListIndices.get(i).intValue();
         asPath.get(pathListIndex).add(as);
      }
   }

   private void populateBgpAdvertisements() {
      Relation currentAdvertProperty;
      currentAdvertProperty = _relations.get("BgpAdvertisement_type");
      List<String> advertTypes = getStringColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_network");
      List<Long> advertNetworks = getLongColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_nextHopIp");
      List<Long> advertNextHopIps = getLongColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_srcIp");
      List<Long> advertSrcIps = getLongColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_dstIp");
      List<Long> advertDstIps = getLongColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_srcProtocol");
      List<String> advertSrcProtocols = getStringColumn(currentAdvertProperty,
            1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_srcNode");
      List<String> advertSrcNodes = getStringColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_dstNode");
      List<String> advertDstNodes = getStringColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_localPref");
      List<Long> advertLocalPrefs = getLongColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_med");
      List<Long> advertMeds = getLongColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_originatorIp");
      List<Long> advertOriginatorIps = getLongColumn(currentAdvertProperty, 1);
      currentAdvertProperty = _relations.get("BgpAdvertisement_originType");
      List<String> advertOriginTypes = getStringColumn(currentAdvertProperty, 1);
      List<Long> advertIndices = getLongColumn(currentAdvertProperty, 0);
      int numAdverts = advertIndices.size();
      for (int i = 0; i < numAdverts; i++) {
         String type = advertTypes.get(i);
         Prefix network = _networks.get(advertNetworks.get(i));
         Ip nextHopIp = new Ip(advertNextHopIps.get(i));
         Ip srcIp = new Ip(advertSrcIps.get(i));
         Ip dstIp = new Ip(advertDstIps.get(i));
         RoutingProtocol srcProtocol = RoutingProtocol
               .fromProtocolName(advertSrcProtocols.get(i));
         String srcNode = advertSrcNodes.get(i);
         String dstNode = advertDstNodes.get(i);
         int localPref = advertLocalPrefs.get(i).intValue();
         int med = advertMeds.get(i).intValue();
         Ip originatorIp = new Ip(advertOriginatorIps.get(i));
         OriginType originType = OriginType
               .fromString(advertOriginTypes.get(i));
         Long advertIndex = advertIndices.get(i);
         AsPath asPath = _asPaths.get(advertIndex);
         CommunitySet communities = _communities.get(advertIndex);
         if (communities == null) {
            communities = new CommunitySet();
         }
         BgpAdvertisement advert = new BgpAdvertisement(type, network,
               nextHopIp, srcNode, srcIp, dstNode, dstIp, srcProtocol,
               originType, localPref, med, originatorIp, asPath, communities);
         _bgpAdvertisements.put(advertIndex, advert);
      }
   }

   private void populateCommunities() {
      Relation advertisementCommunityRelation = _relations
            .get("AdvertisementCommunity");
      List<Long> advertIndices = getLongColumn(advertisementCommunityRelation,
            0);
      List<Long> communities = getLongColumn(advertisementCommunityRelation, 1);
      int numEntries = advertIndices.size();
      for (int i = 0; i < numEntries; i++) {
         Long advertIndex = advertIndices.get(i);
         CommunitySet communitySet = _communities.get(advertIndex);
         if (communitySet == null) {
            communitySet = new CommunitySet();
            _communities.put(advertIndex, communitySet);
         }
         long community = communities.get(i);
         communitySet.add(community);
      }
   }

   private void populateFlows() {
      Relation currentFlowProperty;
      currentFlowProperty = _relations.get("Flow_dstIp");
      if (currentFlowProperty == null) {
         return;
      }
      List<Long> flowDstIps = getLongColumn(currentFlowProperty, 1);

      currentFlowProperty = _relations.get("Flow_dstPort");
      List<Long> flowDstPorts = getLongColumn(currentFlowProperty, 1);

      currentFlowProperty = _relations.get("Flow_node");
      List<String> flowNodes = getStringColumn(currentFlowProperty, 1);

      currentFlowProperty = _relations.get("Flow_ipProtocol");
      List<Long> flowProtocols = getLongColumn(currentFlowProperty, 1);

      currentFlowProperty = _relations.get("Flow_srcIp");
      List<Long> flowSrcIps = getLongColumn(currentFlowProperty, 1);

      currentFlowProperty = _relations.get("Flow_srcPort");
      List<Long> flowSrcPorts = getLongColumn(currentFlowProperty, 1);

      currentFlowProperty = _relations.get("Flow_tag");
      List<String> flowTags = getStringColumn(currentFlowProperty, 1);

      // get indices
      List<Long> flowIndices = getLongColumn(currentFlowProperty, 0);

      int numFlows = flowIndices.size();
      for (int i = 0; i < numFlows; i++) {
         Long flowIndex = flowIndices.get(i);
         Ip dstIp = new Ip(flowDstIps.get(i));
         int dstPort = flowDstPorts.get(i).intValue();
         String node = flowNodes.get(i);
         IpProtocol protocol = IpProtocol.fromNumber(flowProtocols.get(i)
               .intValue());
         Ip srcIp = new Ip(flowSrcIps.get(i));
         int srcPort = flowSrcPorts.get(i).intValue();
         String tag = flowTags.get(i);
         Flow flow = new Flow(node, srcIp, dstIp, srcPort, dstPort, protocol,
               tag);
         _flows.put(flowIndex, flow);
      }
   }

   private void populateNetworks() {
      Relation relation = _relations.get("Network_index");
      List<Long> networkIndices = getLongColumn(relation, 0);
      List<Long> networkAddresses = getLongColumn(relation, 1);
      List<Long> networkPrefixLengths = getLongColumn(relation, 3);
      int numNetworks = networkIndices.size();
      for (int i = 0; i < numNetworks; i++) {
         Long networkIndex = networkIndices.get(i);
         Ip networkAddress = new Ip(networkAddresses.get(i));
         int prefixLength = networkPrefixLengths.get(i).intValue();
         Prefix prefix = new Prefix(networkAddress, prefixLength);
         _networks.put(networkIndex, prefix);
      }
   }

   private void populateRoutes() {
      Relation currentRouteProperty;
      List<Long> routeAdmins;
      List<Long> routeCosts;
      List<String> routeNextHops = null;
      List<String> routeNextHopInts = null;
      List<Long> routeNextHopIps;
      List<Long> routeTags;
      currentRouteProperty = _relations.get("RouteDetails_admin");
      routeAdmins = getLongColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("RouteDetails_cost");
      routeCosts = getLongColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("RouteDetails_nextHop");
      routeNextHops = getStringColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("RouteDetails_nextHopInt");
      routeNextHopInts = getStringColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("RouteDetails_nextHopIp");
      routeNextHopIps = getLongColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("RouteDetails_tag");
      routeTags = getLongColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("Route_network");
      List<Long> routeNetworks = getLongColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("Route_node");
      List<String> routeNodes = getStringColumn(currentRouteProperty, 1);
      currentRouteProperty = _relations.get("Route_protocol");
      List<String> routeProtocols = getStringColumn(currentRouteProperty, 1);

      // get indices
      currentRouteProperty = _relations.get("Route");
      List<Long> routeIndices = getLongColumn(currentRouteProperty, 0);
      int numRoutes = routeIndices.size();
      for (int i = 0; i < numRoutes; i++) {
         Long routeIndex = routeIndices.get(i);
         int admin = routeAdmins.get(i).intValue();
         int cost = routeCosts.get(i).intValue();
         Prefix network = _networks.get(routeNetworks.get(i));
         String nextHopInterface = routeNextHopInts.get(i);
         Ip nextHopIp = new Ip(routeNextHopIps.get(i));
         String node = routeNodes.get(i);
         RoutingProtocol routingProtocol = RoutingProtocol
               .fromProtocolName(routeProtocols.get(i));
         int tag = routeTags.get(i).intValue();
         String nextHop = routeNextHops.get(i);
         PrecomputedRoute route = new PrecomputedRoute(node, network,
               nextHopIp, nextHop, nextHopInterface, admin, cost,
               routingProtocol, tag);
         _routes.put(routeIndex, route);
      }
   }

}
