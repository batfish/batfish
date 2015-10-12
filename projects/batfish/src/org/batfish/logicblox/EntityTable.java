package org.batfish.logicblox;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

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

import com.logicblox.connect.Workspace.Relation;
import com.logicblox.connect.Workspace.Relation.EntityColumn;
import com.logicblox.connect.Workspace.Relation.Int64Column;
import com.logicblox.connect.Workspace.Relation.StringColumn;
import com.logicblox.connect.Workspace.Relation.UInt64Column;

public class EntityTable {

   private final Map<BigInteger, AsPath> _asPaths;

   private final Map<BigInteger, BgpAdvertisement> _bgpAdvertisements;

   private final Map<BigInteger, CommunitySet> _communities;

   private final Map<BigInteger, Flow> _flows;

   private final Map<BigInteger, Prefix> _networks;

   private final Map<BigInteger, PrecomputedRoute> _routes;

   public EntityTable(LogicBloxFrontend lbf) {
      _asPaths = new HashMap<BigInteger, AsPath>();
      _bgpAdvertisements = new HashMap<BigInteger, BgpAdvertisement>();
      _communities = new HashMap<BigInteger, CommunitySet>();
      _flows = new HashMap<BigInteger, Flow>();
      _networks = new HashMap<BigInteger, Prefix>();
      _routes = new HashMap<BigInteger, PrecomputedRoute>();
      populateNetworks(lbf);
      populateFlows(lbf);
      populateRoutes(lbf);
      populateAsPaths(lbf);
      populateCommunities(lbf);
      populateBgpAdvertisements(lbf);
   }

   public BgpAdvertisement getBgpAdvertisement(BigInteger index) {
      return _bgpAdvertisements.get(index);
   }

   public Flow getFlow(BigInteger index) {
      return _flows.get(index);
   }

   private BigInteger[] getIndexColumn(Relation relation, int column) {
      EntityColumn ec = (EntityColumn) relation.getColumns().get(column);
      BigInteger[] indexArray = ((UInt64Column) ec.getIndexColumn().unwrap())
            .getRows();
      return indexArray;
   }

   private long[] getIntColumn(Relation relation, int column) {
      long[] longArray = ((Int64Column) relation.getColumns().get(column))
            .getRows();
      return longArray;
   }

   public Prefix getNetwork(BigInteger index) {
      return _networks.get(index);
   }

   public PrecomputedRoute getPrecomputedRoute(BigInteger index) {
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

   private long[] getRefIntColumn(Relation relation, int column) {
      EntityColumn ec = (EntityColumn) relation.getColumns().get(column);
      long[] longArray = ((Int64Column) ec.getRefModeColumn().unwrap())
            .getRows();
      return longArray;
   }

   private String[] getRefStringColumn(Relation relation, int column) {
      EntityColumn ec = (EntityColumn) relation.getColumns().get(column);
      String[] stringArray = ((StringColumn) ec.getRefModeColumn().unwrap())
            .getRows();
      return stringArray;
   }

   public PrecomputedRoute getRoute(BigInteger index) {
      return _routes.get(index);
   }

   private void populateAsPaths(LogicBloxFrontend lbf) {
      Relation advertisementPathSizeRelation = lbf
            .queryPredicate("libbatfish:AsPath:AdvertisementPathSize");
      BigInteger[] sizeAdvertIndices = getIndexColumn(
            advertisementPathSizeRelation, 0);
      long[] sizes = getIntColumn(advertisementPathSizeRelation, 1);
      int numAsPaths = sizeAdvertIndices.length;
      for (int i = 0; i < numAsPaths; i++) {
         BigInteger advertIndex = sizeAdvertIndices[i];
         int size = (int) sizes[i];
         AsPath asPath = new AsPath(size);
         _asPaths.put(advertIndex, asPath);
      }
      Relation advertisementPathRelation = lbf
            .queryPredicate("libbatfish:AsPath:AdvertisementPath");
      BigInteger[] pathAdvertIndices = getIndexColumn(
            advertisementPathRelation, 0);
      long[] pathListIndices = getIntColumn(advertisementPathRelation, 1);
      long[] asPathAses = getRefIntColumn(advertisementPathRelation, 2);
      int numPathListEntries = asPathAses.length;
      for (int i = 0; i < numPathListEntries; i++) {
         BigInteger advertIndex = pathAdvertIndices[i];
         AsPath asPath = _asPaths.get(advertIndex);
         int as = (int) asPathAses[i];
         int pathListIndex = (int) pathListIndices[i];
         asPath.get(pathListIndex).add(as);
      }
   }

   private void populateBgpAdvertisements(LogicBloxFrontend lbf) {
      Relation currentAdvertProperty;
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_type");
      String[] advertTypes = getRefStringColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_network");
      BigInteger[] advertNetworks = getIndexColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_nextHopIp");
      long[] advertNextHopIps = getRefIntColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_srcIp");
      long[] advertSrcIps = getRefIntColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_dstIp");
      long[] advertDstIps = getRefIntColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_srcProtocol");
      String[] advertSrcProtocols = getRefStringColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_srcNode");
      String[] advertSrcNodes = getRefStringColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_dstNode");
      String[] advertDstNodes = getRefStringColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_localPref");
      long[] advertLocalPrefs = getIntColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_med");
      long[] advertMeds = getIntColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_originatorIp");
      long[] advertOriginatorIps = getRefIntColumn(currentAdvertProperty, 1);
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_originType");
      String[] advertOriginTypes = getRefStringColumn(currentAdvertProperty, 1);
      BigInteger[] advertIndices = getIndexColumn(currentAdvertProperty, 0);
      int numAdverts = advertIndices.length;
      for (int i = 0; i < numAdverts; i++) {
         String type = advertTypes[i];
         Prefix network = _networks.get(advertNetworks[i]);
         Ip nextHopIp = new Ip(advertNextHopIps[i]);
         Ip srcIp = new Ip(advertSrcIps[i]);
         Ip dstIp = new Ip(advertDstIps[i]);
         RoutingProtocol srcProtocol = RoutingProtocol
               .fromProtocolName(advertSrcProtocols[i]);
         String srcNode = advertSrcNodes[i];
         String dstNode = advertDstNodes[i];
         int localPref = (int) advertLocalPrefs[i];
         int med = (int) advertMeds[i];
         Ip originatorIp = new Ip(advertOriginatorIps[i]);
         OriginType originType = OriginType.fromString(advertOriginTypes[i]);
         BigInteger advertIndex = advertIndices[i];
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

   private void populateCommunities(LogicBloxFrontend lbf) {
      Relation advertisementCommunityRelation = lbf
            .queryPredicate("libbatfish:CommunityList:AdvertisementCommunity");
      BigInteger[] advertIndices = getIndexColumn(
            advertisementCommunityRelation, 0);
      long[] communities = getIntColumn(advertisementCommunityRelation, 1);
      int numEntries = advertIndices.length;
      for (int i = 0; i < numEntries; i++) {
         BigInteger advertIndex = advertIndices[i];
         CommunitySet communitySet = _communities.get(advertIndex);
         if (communitySet == null) {
            communitySet = new CommunitySet();
            _communities.put(advertIndex, communitySet);
         }
         long community = communities[i];
         communitySet.add(community);
      }
   }

   private void populateFlows(LogicBloxFrontend lbf) {
      Relation currentFlowProperty;
      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_dstIp");
      long[] flowDstIps = getRefIntColumn(currentFlowProperty, 1);

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_dstPort");
      long[] flowDstPorts = getIntColumn(currentFlowProperty, 1);

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_node");
      String[] flowNodes = getRefStringColumn(currentFlowProperty, 1);

      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_ipProtocol");
      long[] flowProtocols = getRefIntColumn(currentFlowProperty, 1);

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_srcIp");
      long[] flowSrcIps = getRefIntColumn(currentFlowProperty, 1);

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_srcPort");
      long[] flowSrcPorts = getIntColumn(currentFlowProperty, 1);

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_tag");
      String[] flowTags = getRefStringColumn(currentFlowProperty, 1);

      // get indices
      BigInteger[] flowIndices = getIndexColumn(currentFlowProperty, 0);

      int numFlows = flowIndices.length;
      for (int i = 0; i < numFlows; i++) {
         BigInteger flowIndex = flowIndices[i];
         Ip dstIp = new Ip(flowDstIps[i]);
         int dstPort = (int) flowDstPorts[i];
         String node = flowNodes[i];
         IpProtocol protocol = IpProtocol.fromNumber((int) flowProtocols[i]);
         Ip srcIp = new Ip(flowSrcIps[i]);
         int srcPort = (int) flowSrcPorts[i];
         String tag = flowTags[i];
         Flow flow = new Flow(node, srcIp, dstIp, srcPort, dstPort, protocol,
               tag);
         _flows.put(flowIndex, flow);
      }
   }

   private void populateNetworks(LogicBloxFrontend lbf) {
      Relation relation = lbf.queryPredicate("libbatfish:Ip:Network_index");
      BigInteger[] networkIndices = getIndexColumn(relation, 0);
      long[] networkAddresses = getIntColumn(relation, 1);
      long[] networkPrefixLengths = getIntColumn(relation, 3);
      int numNetworks = networkIndices.length;
      for (int i = 0; i < numNetworks; i++) {
         BigInteger networkIndex = networkIndices[i];
         Ip networkAddress = new Ip(networkAddresses[i]);
         int prefixLength = (int) networkPrefixLengths[i];
         Prefix prefix = new Prefix(networkAddress, prefixLength);
         _networks.put(networkIndex, prefix);
      }
   }

   private void populateRoutes(LogicBloxFrontend lbf) {
      Relation currentRouteProperty;
      long[] routeAdmins;
      long[] routeCosts;
      String[] routeNextHops = null;
      String[] routeNextHopInts = null;
      long[] routeNextHopIps;
      long[] routeTags;
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_admin");
      routeAdmins = getIntColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_cost");
      routeCosts = getIntColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_nextHop");
      routeNextHops = getRefStringColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_nextHopInt");
      routeNextHopInts = getRefStringColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_nextHopIp");
      routeNextHopIps = getRefIntColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_tag");
      routeTags = getIntColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:Route_network");
      BigInteger[] routeNetworks = getIndexColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf.queryPredicate("libbatfish:Route:Route_node");
      String[] routeNodes = getRefStringColumn(currentRouteProperty, 1);
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:Route_protocol");
      String[] routeProtocols = getRefStringColumn(currentRouteProperty, 1);

      // get indices
      currentRouteProperty = lbf.queryPredicate("libbatfish:Route:Route");
      BigInteger[] routeIndices = getIndexColumn(currentRouteProperty, 0);
      int numRoutes = routeIndices.length;
      for (int i = 0; i < numRoutes; i++) {
         BigInteger routeIndex = routeIndices[i];
         int admin = (int) routeAdmins[i];
         int cost = (int) routeCosts[i];
         Prefix network = _networks.get(routeNetworks[i]);
         String nextHopInterface = routeNextHopInts[i];
         Ip nextHopIp = new Ip(routeNextHopIps[i]);
         String node = routeNodes[i];
         RoutingProtocol routingProtocol = RoutingProtocol
               .fromProtocolName(routeProtocols[i]);
         int tag = (int) routeTags[i];
         String nextHop = routeNextHops[i];
         PrecomputedRoute route = new PrecomputedRoute(node, network,
               nextHopIp, nextHop, nextHopInterface, admin, cost,
               routingProtocol, tag);
         _routes.put(routeIndex, route);
      }
   }

}
