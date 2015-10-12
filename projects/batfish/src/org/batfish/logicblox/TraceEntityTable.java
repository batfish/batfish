package org.batfish.logicblox;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.batfish.collections.CommunitySet;
import org.batfish.representation.AsPath;
import org.batfish.representation.BgpAdvertisement;
import org.batfish.representation.Flow;
import org.batfish.representation.Ip;
import org.batfish.representation.OriginType;
import org.batfish.representation.PrecomputedRoute;
import org.batfish.representation.PrecomputedRouteBuilder;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

import com.logicblox.connect.Workspace.Relation;
import com.logicblox.connect.Workspace.Relation.EntityColumn;
import com.logicblox.connect.Workspace.Relation.Int64Column;
import com.logicblox.connect.Workspace.Relation.StringColumn;
import com.logicblox.connect.Workspace.Relation.UInt64Column;

public class TraceEntityTable {

   private final Map<Long, Integer> _ases;

   private final Map<Integer, Map<Long, AsPath>> _asPaths;

   private final Map<Integer, Map<Long, BgpAdvertisement>> _bgpAdvertisements;

   private final Map<Long, String> _bgpAdvertisementTypes;

   private final Map<Integer, Map<Long, CommunitySet>> _communities;

   private final EntityTable _entityTable;

   private final Map<Integer, Map<Long, Flow>> _flows;

   private final Map<Long, String> _interfaces;

   private final Map<Long, Ip> _ips;

   private final Map<Long, String> _namedAsPaths;

   private final Map<Long, Prefix> _networks;

   private final Map<Long, String> _nodes;

   private BigInteger _offset;

   private final Map<Long, OriginType> _originTypes;

   private final Map<Long, String> _policyMaps;

   private final Map<Integer, Map<Long, PrecomputedRoute>> _routes;

   private final Map<Long, RoutingProtocol> _routingProtocols;

   public TraceEntityTable(LogicBloxFrontend lbf, EntityTable entityTable) {
      _entityTable = entityTable;
      _asPaths = new HashMap<Integer, Map<Long, AsPath>>();
      _ases = new HashMap<Long, Integer>();
      _bgpAdvertisements = new HashMap<Integer, Map<Long, BgpAdvertisement>>();
      _bgpAdvertisementTypes = new HashMap<Long, String>();
      _communities = new HashMap<Integer, Map<Long, CommunitySet>>();
      _flows = new HashMap<Integer, Map<Long, Flow>>();
      _interfaces = new HashMap<Long, String>();
      _ips = new HashMap<Long, Ip>();
      _namedAsPaths = new HashMap<Long, String>();
      _networks = new HashMap<Long, Prefix>();
      _nodes = new HashMap<Long, String>();
      _originTypes = new HashMap<Long, OriginType>();
      _policyMaps = new HashMap<Long, String>();
      _routes = new HashMap<Integer, Map<Long, PrecomputedRoute>>();
      _routingProtocols = new HashMap<Long, RoutingProtocol>();
      populateAdvertTypes(lbf);
      populateAses(lbf);
      populateInterfaces(lbf);
      populateIps(lbf);
      populateNamedAsPaths(lbf);
      populateNodes(lbf);
      populateOriginTypes(lbf);
      populatePolicyMaps(lbf);
      populateRoutingProtocols(lbf);

      populateAsPaths(lbf);
      populateCommunities(lbf);
      populateFlows(lbf);
      populateNetworks(lbf);

      populateBgpAdvertisements(lbf);
      populateRoutes(lbf);
   }

   public String getAdvertisementType(long index) {
      return _bgpAdvertisementTypes.get(index);
   }

   public int getAutonomousSystem(long index) {
      return _ases.get(index);
   }

   public BgpAdvertisement getBgpAdvertisement(int traceNumber, long index) {
      return _bgpAdvertisements.get(traceNumber).get(index);
   }

   public Map<Integer, Map<Long, BgpAdvertisement>> getBgpAdvertisements() {
      return _bgpAdvertisements;
   }

   public Flow getFlow(int traceNumber, long index) {
      return _flows.get(traceNumber).get(index);
   }

   private long[] getIndexColumn(Relation relation, int column) {
      EntityColumn ec = (EntityColumn) relation.getColumns().get(column);
      BigInteger[] indexArray = ((UInt64Column) ec.getIndexColumn().unwrap())
            .getRows();
      long[] modifiedArray = new long[indexArray.length];
      initOffset(indexArray);
      for (int i = 0; i < indexArray.length; i++) {
         modifiedArray[i] = indexArray[i].longValue() + 1;
      }
      return modifiedArray;
   }

   private long[] getIntColumn(Relation relation, int column) {
      long[] longArray = ((Int64Column) relation.getColumns().get(column))
            .getRows();
      return longArray;
   }

   public String getInterface(long index) {
      return _interfaces.get(index);
   }

   public Ip getIp(long index) {
      return _ips.get(index);
   }

   public String getNamedAsPath(long index) {
      return _namedAsPaths.get(index);
   }

   public Prefix getNetwork(long index) {
      return _networks.get(index);
   }

   public String getNode(long index) {
      return _nodes.get(index);
   }

   public OriginType getOriginType(long index) {
      return _originTypes.get(index);
   }

   public String getPolicyMap(long index) {
      return _policyMaps.get(index);
   }

   private long[] getRefColumn(Relation relation, int column) {
      EntityColumn ec = (EntityColumn) relation.getColumns().get(column);
      BigInteger[] indexArray = ((UInt64Column) ec.getIndexColumn().unwrap())
            .getRows();
      initOffset(indexArray);
      long[] modifiedArray = new long[indexArray.length];
      for (int i = 0; i < indexArray.length; i++) {
         modifiedArray[i] = indexArray[i].longValue() + 1;
      }
      return modifiedArray;
   }

   public PrecomputedRoute getRoute(int traceNumber, long index) {
      PrecomputedRoute val = _routes.get(traceNumber).get(index);
      if (val == null) {
         assert Boolean.TRUE;
      }
      return val;
   }

   public Map<Integer, Map<Long, PrecomputedRoute>> getRoutes() {
      return _routes;
   }

   public RoutingProtocol getRoutingProtocol(long index) {
      return _routingProtocols.get(index);
   }

   private String[] getStringColumn(Relation relation, int column) {
      String[] stringArray = ((StringColumn) relation.getColumns().get(column))
            .getRows();
      return stringArray;
   }

   private void initOffset(BigInteger[] indexArray) {
      if (_offset == null) {
         if (indexArray.length != 0) {
            BigInteger largeValue = indexArray[0];
            long l = largeValue.longValue() + 1;
            BigInteger smallValue = BigInteger.valueOf(l);
            _offset = largeValue.subtract(smallValue);
         }
      }
   }

   private void populateAdvertTypes(LogicBloxFrontend lbf) {
      Relation relation = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:AdvertisementType_name");
      long[] indices = getRefColumn(relation, 0);
      String[] names = getStringColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         String name = names[i];
         _bgpAdvertisementTypes.put(index, name);
      }
   }

   private void populateAses(LogicBloxFrontend lbf) {
      Relation relation = lbf
            .queryPredicate("libbatfish:AsPath:AutonomousSystem_number");
      long[] indices = getRefColumn(relation, 0);
      long[] asNumbers = getIntColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         int asNumber = (int) asNumbers[i];
         _ases.put(index, asNumber);
      }
   }

   private void populateAsPaths(LogicBloxFrontend lbf) {
      try {
         String tracePrefix = "trace:";
         Relation advertisementPathSizeRelation = lbf
               .queryPredicate(tracePrefix
                     + "libbatfish:AsPath:AdvertisementPathSize");
         long[] sizeTraceNumbers = getIntColumn(advertisementPathSizeRelation,
               0);
         long[] sizeAdvertIndices = getIntColumn(advertisementPathSizeRelation,
               1);
         long[] sizes = getIntColumn(advertisementPathSizeRelation, 2);
         int numAsPaths = sizeAdvertIndices.length;
         int traceNumber = -1;
         Map<Long, AsPath> currentTraceAsPaths = null;
         for (int i = 0; i < numAsPaths; i++) {
            int newTraceNumber = (int) sizeTraceNumbers[i];
            if (traceNumber != newTraceNumber) {
               traceNumber = newTraceNumber;
               currentTraceAsPaths = new HashMap<Long, AsPath>();
               _asPaths.put(traceNumber, currentTraceAsPaths);
            }
            long advertIndex = sizeAdvertIndices[i];
            int size = (int) sizes[i];
            AsPath asPath = new AsPath(size);
            currentTraceAsPaths.put(advertIndex, asPath);
         }
         Relation advertisementPathRelation = lbf.queryPredicate(tracePrefix
               + "libbatfish:AsPath:AdvertisementPath");
         long[] pathTraceNumbers = getIntColumn(advertisementPathRelation, 0);
         long[] pathAdvertIndices = getIntColumn(advertisementPathRelation, 1);
         long[] pathListIndices = getIntColumn(advertisementPathRelation, 2);
         long[] asPathAsIndices = getIntColumn(advertisementPathRelation, 3);
         int numPathListEntries = asPathAsIndices.length;
         for (int i = 0; i < numPathListEntries; i++) {
            long advertIndex = pathAdvertIndices[i];
            int pathTraceNumber = (int) pathTraceNumbers[i];
            AsPath asPath = _asPaths.get(pathTraceNumber).get(advertIndex);
            int as = _ases.get(asPathAsIndices[i]);
            int pathListIndex = (int) pathListIndices[i];
            asPath.get(pathListIndex).add(as);
         }
      }
      catch (PredicateNotFoundBatfishException e) {
         return;
      }
   }

   private void populateBgpAdvertisements(LogicBloxFrontend lbf) {
      try {
         String tracePrefix = "trace:";
         Relation currentAdvertProperty;
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_type");
         long[] advertTypes = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_network");
         long[] advertNetworks = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_nextHopIp");
         long[] advertNextHopIps = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_srcIp");
         long[] advertSrcIps = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_dstIp");
         long[] advertDstIps = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_srcProtocol");
         long[] advertSrcProtocols = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_srcNode");
         long[] advertSrcNodes = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_dstNode");
         long[] advertDstNodes = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_localPref");
         long[] advertLocalPrefs = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_med");
         long[] advertMeds = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_originatorIp");
         long[] advertOriginatorIps = getIntColumn(currentAdvertProperty, 2);
         currentAdvertProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:BgpAdvertisement:BgpAdvertisement_originType");
         long[] advertOriginTypes = getIntColumn(currentAdvertProperty, 2);
         long[] advertIndices = getIntColumn(currentAdvertProperty, 1);
         long[] traceNumbers = getIntColumn(currentAdvertProperty, 0);
         int numAdverts = traceNumbers.length;
         int traceNumber = -1;
         Map<Long, BgpAdvertisement> currentTraceAdverts = null;
         for (int i = 0; i < numAdverts; i++) {
            int newTraceNumber = (int) traceNumbers[i];
            if (traceNumber != newTraceNumber) {
               traceNumber = newTraceNumber;
               currentTraceAdverts = new LinkedHashMap<Long, BgpAdvertisement>();
               _bgpAdvertisements.put(traceNumber, currentTraceAdverts);
            }
            String type = getAdvertisementType(advertTypes[i]);
            Prefix network = getNetwork(advertNetworks[i]);
            Ip nextHopIp = getIp(advertNextHopIps[i]);
            Ip srcIp = getIp(advertSrcIps[i]);
            Ip dstIp = getIp(advertDstIps[i]);
            RoutingProtocol srcProtocol = getRoutingProtocol(advertSrcProtocols[i]);
            String srcNode = getNode(advertSrcNodes[i]);
            String dstNode = getNode(advertDstNodes[i]);
            int localPref = (int) advertLocalPrefs[i];
            int med = (int) advertMeds[i];
            Ip originatorIp = getIp(advertOriginatorIps[i]);
            OriginType originType = getOriginType(advertOriginTypes[i]);
            long advertIndex = advertIndices[i];
            // add asPath if present
            AsPath asPath = null;
            Map<Long, AsPath> currentTraceAsPaths = _asPaths.get(traceNumber);
            if (currentTraceAsPaths != null) {
               asPath = currentTraceAsPaths.get(advertIndex);
            }
            // add communities if present
            CommunitySet communities = null;
            Map<Long, CommunitySet> currentTraceCommunities = _communities
                  .get(traceNumber);
            if (currentTraceCommunities != null) {
               communities = currentTraceCommunities.get(advertIndex);
            }
            BgpAdvertisement advert = new BgpAdvertisement(type, network,
                  nextHopIp, srcNode, srcIp, dstNode, dstIp, srcProtocol,
                  originType, localPref, med, originatorIp, asPath, communities);
            currentTraceAdverts.put(advertIndex, advert);
         }
      }
      catch (PredicateNotFoundBatfishException e) {

      }
   }

   private void populateCommunities(LogicBloxFrontend lbf) {
      try {
         String tracePrefix = "trace:";
         Relation advertisementCommunityRelation = lbf
               .queryPredicate(tracePrefix
                     + "libbatfish:CommunityList:AdvertisementCommunity");
         long[] traceNumbers = getIntColumn(advertisementCommunityRelation, 0);
         long[] advertIndices = getIntColumn(advertisementCommunityRelation, 1);
         long[] communities = getIntColumn(advertisementCommunityRelation, 2);
         int numEntries = traceNumbers.length;
         int traceNumber = -1;
         Map<Long, CommunitySet> currentTraceCommunities = null;
         for (int i = 0; i < numEntries; i++) {
            int newTraceNumber = (int) traceNumbers[i];
            if (traceNumber != newTraceNumber) {
               traceNumber = newTraceNumber;
               currentTraceCommunities = new LinkedHashMap<Long, CommunitySet>();
               _communities.put(traceNumber, currentTraceCommunities);
            }
            long advertIndex = advertIndices[i];
            CommunitySet communitySet = currentTraceCommunities
                  .get(advertIndex);
            if (communitySet == null) {
               communitySet = new CommunitySet();
               currentTraceCommunities.put(advertIndex, communitySet);
            }
            long community = communities[i];
            communitySet.add(community);
         }
      }
      catch (PredicateNotFoundBatfishException e) {
      }
   }

   private void populateFlows(LogicBloxFrontend lbf) {
      // ****************
      // trace not supported for now
      if (Boolean.TRUE) {
         return;
      }
      // ****************

      // String tracePrefix = "trace:";
      // Relation currentFlowProperty;
      // currentFlowProperty = lbf.queryPredicate(tracePrefix
      // + "libbatfish:Flow:Flow_dstIp");
      // long[] flowDstIps = getRefIntColumn(currentFlowProperty, 1);
      //
      // currentFlowProperty = lbf.queryPredicate(tracePrefix
      // + "libbatfish:Flow:Flow_dstPort");
      // long[] flowDstPorts = getIntColumn(currentFlowProperty, 1);
      //
      // currentFlowProperty = lbf.queryPredicate(tracePrefix
      // + "libbatfish:Flow:Flow_node");
      // String[] flowNodes = getRefStringColumn(currentFlowProperty, 1);
      //
      // currentFlowProperty = lbf.queryPredicate(tracePrefix
      // + "libbatfish:Flow:Flow_ipProtocol");
      // long[] flowProtocols = getRefIntColumn(currentFlowProperty, 1);
      //
      // currentFlowProperty = lbf.queryPredicate(tracePrefix
      // + "libbatfish:Flow:Flow_srcIp");
      // long[] flowSrcIps = getRefIntColumn(currentFlowProperty, 1);
      //
      // currentFlowProperty = lbf.queryPredicate(tracePrefix
      // + "libbatfish:Flow:Flow_srcPort");
      // long[] flowSrcPorts = getIntColumn(currentFlowProperty, 1);
      //
      // currentFlowProperty = lbf.queryPredicate(tracePrefix
      // + "libbatfish:Flow:Flow_tag");
      // String[] flowTags = getRefStringColumn(currentFlowProperty, 1);
      //
      // // get indices
      // long[] flowTraceNumbers = getTraceColumn(currentFlowProperty);
      // BigInteger[] flowIndices = getIndexColumn(currentFlowProperty, 0);
      //
      // int numFlows = flowIndices.length;
      // int traceNumber = -1;
      // Map<Long, Flow> currentTraceFlows = null;
      // for (int i = 0; i < numFlows; i++) {
      // int newTraceNumber = (int) flowTraceNumbers[i];
      // if (traceNumber != newTraceNumber) {
      // traceNumber = newTraceNumber;
      // currentTraceFlows = new LinkedHashMap<Long, Flow>();
      // _flows.put(traceNumber, currentTraceFlows);
      // }
      // BigInteger flowIndex = flowIndices[i];
      // Ip dstIp = new Ip(flowDstIps[i]);
      // int dstPort = (int) flowDstPorts[i];
      // String node = flowNodes[i];
      // IpProtocol protocol = IpProtocol.fromNumber((int) flowProtocols[i]);
      // Ip srcIp = new Ip(flowSrcIps[i]);
      // int srcPort = (int) flowSrcPorts[i];
      // String tag = flowTags[i];
      // Flow flow = new Flow(node, srcIp, dstIp, srcPort, dstPort, protocol,
      // tag);
      // currentTraceFlows.put(flowIndex, flow);
      // }
   }

   private void populateInterfaces(LogicBloxFrontend lbf) {
      Relation relation = lbf
            .queryPredicate("libbatfish:Interface:Interface_name");
      long[] indices = getRefColumn(relation, 0);
      String[] names = getStringColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         String name = names[i];
         _interfaces.put(index, name);
      }
   }

   private void populateIps(LogicBloxFrontend lbf) {
      Relation relation = lbf.queryPredicate("libbatfish:Ip:Ip_address");
      long[] indices = getRefColumn(relation, 0);
      long[] addresses = getIntColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         Ip ip = new Ip(addresses[i]);
         _ips.put(index, ip);
      }
   }

   private void populateNamedAsPaths(LogicBloxFrontend lbf) {
      Relation relation = lbf.queryPredicate("libbatfish:AsPath:AsPath_name");
      long[] indices = getRefColumn(relation, 0);
      String[] names = getStringColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         String name = names[i];
         _namedAsPaths.put(index, name);
      }
   }

   private void populateNetworks(LogicBloxFrontend lbf) {
      Relation relation = lbf.queryPredicate("libbatfish:Ip:Network_index");
      long[] networkIndices = getIndexColumn(relation, 0);
      long[] networkAddresses = getIntColumn(relation, 1);
      long[] networkPrefixLengths = getIntColumn(relation, 3);
      int numNetworks = networkIndices.length;
      for (int i = 0; i < numNetworks; i++) {
         long networkIndex = networkIndices[i];
         Ip networkAddress = new Ip(networkAddresses[i]);
         int prefixLength = (int) networkPrefixLengths[i];
         Prefix prefix = new Prefix(networkAddress, prefixLength);
         _networks.put(networkIndex, prefix);
      }
   }

   private void populateNodes(LogicBloxFrontend lbf) {
      Relation relation = lbf.queryPredicate("libbatfish:Node:Node_name");
      long[] indices = getRefColumn(relation, 0);
      String[] names = getStringColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         String name = names[i];
         _nodes.put(index, name);
      }
   }

   private void populateOriginTypes(LogicBloxFrontend lbf) {
      Relation relation = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:OriginType_name");
      long[] indices = getRefColumn(relation, 0);
      String[] names = getStringColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         OriginType originType = OriginType.fromString(names[i]);
         _originTypes.put(index, originType);
      }
   }

   private void populatePolicyMaps(LogicBloxFrontend lbf) {
      Relation relation = lbf
            .queryPredicate("libbatfish:PolicyMap:PolicyMap_name");
      long[] indices = getRefColumn(relation, 0);
      String[] names = getStringColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         String name = names[i];
         _policyMaps.put(index, name);
      }
   }

   private void populateRoutes(LogicBloxFrontend lbf) {
      String tracePrefix = "trace:";
      Relation currentRouteProperty;
      currentRouteProperty = lbf.queryPredicate(tracePrefix
            + "libbatfish:Route:Route_node");
      long[] routeNodes = getIntColumn(currentRouteProperty, 2);
      long[] routeProtocols = null;
      try {
         currentRouteProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:Route:Route_protocol");
         routeProtocols = getIntColumn(currentRouteProperty, 2);
      }
      catch (PredicateNotFoundBatfishException e) {
      }

      // get networks and indices
      currentRouteProperty = lbf.queryPredicate(tracePrefix
            + "libbatfish:Route:Route_network");
      long[] routeNetworks = getIntColumn(currentRouteProperty, 2);
      long[] routeIndices = getIntColumn(currentRouteProperty, 1);
      long[] routeTraceNumbers = getIntColumn(currentRouteProperty, 0);

      // first get manadatory Route fields for trace
      int numRoutes = routeIndices.length;
      int traceNumber = -1;
      Map<Long, PrecomputedRouteBuilder> currentTraceRoutes = null;
      Map<Integer, Map<Long, PrecomputedRouteBuilder>> routeBuilders = new LinkedHashMap<Integer, Map<Long, PrecomputedRouteBuilder>>();
      for (int i = 0; i < numRoutes; i++) {
         int newTraceNumber = (int) routeTraceNumbers[i];
         if (traceNumber != newTraceNumber) {
            traceNumber = newTraceNumber;
            currentTraceRoutes = new LinkedHashMap<Long, PrecomputedRouteBuilder>();
            routeBuilders.put(traceNumber, currentTraceRoutes);
         }
         long routeIndex = routeIndices[i];
         BigInteger entityTableRouteIndex = BigInteger.valueOf(routeIndex);
         PrecomputedRoute entityTableRoute = _entityTable
               .getRoute(entityTableRouteIndex.add(_offset));
         Prefix network = getNetwork(routeNetworks[i]);
         String node = getNode(routeNodes[i]);
         RoutingProtocol routingProtocol = routeProtocols != null ? getRoutingProtocol(routeProtocols[i])
               : entityTableRoute.getProtocol();
         PrecomputedRouteBuilder routeBuilder = new PrecomputedRouteBuilder();
         routeBuilder.setNode(node);
         routeBuilder.setNetwork(network);
         routeBuilder.setProtocol(routingProtocol);
         currentTraceRoutes.put(routeIndex, routeBuilder);
      }
      // set admins where they exist
      currentRouteProperty = lbf.queryPredicate(tracePrefix
            + "libbatfish:Route:Route_admin");
      long[] routeAdminTraceNumbers = getIntColumn(currentRouteProperty, 0);
      long[] routeAdminIndices = getIntColumn(currentRouteProperty, 1);
      long[] routeAdmins = getIntColumn(currentRouteProperty, 2);
      for (int i = 0; i < routeAdminTraceNumbers.length; i++) {
         int currentTraceNumber = (int) routeAdminTraceNumbers[i];
         long currentIndex = routeAdminIndices[i];
         int admin = (int) routeAdmins[i];
         routeBuilders.get(currentTraceNumber).get(currentIndex)
               .setAdministrativeCost(admin);
      }

      // set costs where they exist
      currentRouteProperty = lbf.queryPredicate(tracePrefix
            + "libbatfish:Route:Route_cost");
      long[] routeCostTraceNumbers = getIntColumn(currentRouteProperty, 0);
      long[] routeCostIndices = getIntColumn(currentRouteProperty, 1);
      long[] routeCosts = getIntColumn(currentRouteProperty, 2);
      for (int i = 0; i < routeCostTraceNumbers.length; i++) {
         int currentTraceNumber = (int) routeCostTraceNumbers[i];
         long currentIndex = routeCostIndices[i];
         int cost = (int) routeCosts[i];
         routeBuilders.get(currentTraceNumber).get(currentIndex).setCost(cost);
      }

      // set tags where they exist
      try {
         currentRouteProperty = lbf.queryPredicate(tracePrefix
               + "libbatfish:Route:Route_tag");
         long[] routeTagTraceNumbers = getIntColumn(currentRouteProperty, 0);
         long[] routeTagIndices = getIntColumn(currentRouteProperty, 1);
         long[] routeTags = getIntColumn(currentRouteProperty, 2);
         if (routeTagTraceNumbers != null) {
            for (int i = 0; i < routeTagTraceNumbers.length; i++) {
               int currentTraceNumber = (int) routeTagTraceNumbers[i];
               long currentIndex = routeTagIndices[i];
               int tag = (int) routeTags[i];
               routeBuilders.get(currentTraceNumber).get(currentIndex)
                     .setTag(tag);
            }
         }
      }
      catch (PredicateNotFoundBatfishException e) {
         for (Map<Long, PrecomputedRouteBuilder> builders : routeBuilders
               .values()) {
            for (Entry<Long, PrecomputedRouteBuilder> entry : builders
                  .entrySet()) {
               BigInteger key = BigInteger.valueOf(entry.getKey());
               PrecomputedRouteBuilder builder = entry.getValue();
               BigInteger entityTableKey = key.add(_offset);
               PrecomputedRoute entityTableRoute = _entityTable
                     .getRoute(entityTableKey);
               int tag = entityTableRoute.getTag();
               builder.setTag(tag);
            }
         }
      }

      // set next hop ips where they exist
      currentRouteProperty = lbf.queryPredicate(tracePrefix
            + "libbatfish:Route:Route_nextHopIp");
      long[] routeNextHopIpTraceNumbers = getIntColumn(currentRouteProperty, 0);
      long[] routeNextHopIpIndices = getIntColumn(currentRouteProperty, 1);
      long[] routeNextHopIps = getIntColumn(currentRouteProperty, 2);
      for (int i = 0; i < routeNextHopIpTraceNumbers.length; i++) {
         int currentTraceNumber = (int) routeNextHopIpTraceNumbers[i];
         long currentIndex = routeNextHopIpIndices[i];
         Ip nextHopIp = getIp(routeNextHopIps[i]);
         routeBuilders.get(currentTraceNumber).get(currentIndex)
               .setNextHopIp(nextHopIp);
      }

      // set next hop interfaces where they exist
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:InterfaceRoute_nextHopInt");
      long[] routeNextHopInterfaceRouteIndices = getIndexColumn(
            currentRouteProperty, 0);
      long[] routeNextHopInterfaces = getRefColumn(currentRouteProperty, 1);
      for (int i = 0; i < routeNextHopInterfaceRouteIndices.length; i++) {
         long currentIndex = routeNextHopInterfaceRouteIndices[i];
         String nextHopInterface = getInterface(routeNextHopInterfaces[i]);
         for (Map<Long, PrecomputedRouteBuilder> currentRouteBuilders : routeBuilders
               .values()) {
            PrecomputedRouteBuilder builder = currentRouteBuilders
                  .get(currentIndex);
            if (builder != null) {
               builder.setNextHopInterface(nextHopInterface);
            }
         }
      }

      // populate routes from builders
      for (Entry<Integer, Map<Long, PrecomputedRouteBuilder>> e1 : routeBuilders
            .entrySet()) {
         int currentTraceNumber = e1.getKey();
         Map<Long, PrecomputedRoute> currentRoutes = new HashMap<Long, PrecomputedRoute>();
         _routes.put(currentTraceNumber, currentRoutes);
         for (Entry<Long, PrecomputedRouteBuilder> e2 : e1.getValue()
               .entrySet()) {
            long index = e2.getKey();
            PrecomputedRouteBuilder builder = e2.getValue();
            PrecomputedRoute route = builder.build();
            currentRoutes.put(index, route);
         }
      }

   }

   private void populateRoutingProtocols(LogicBloxFrontend lbf) {
      Relation relation = lbf
            .queryPredicate("libbatfish:Route:RoutingProtocol_name");
      long[] indices = getRefColumn(relation, 0);
      String[] names = getStringColumn(relation, 1);
      for (int i = 0; i < indices.length; i++) {
         long index = indices[i];
         RoutingProtocol protocol = RoutingProtocol.fromProtocolName(names[i]);
         _routingProtocols.put(index, protocol);
      }
   }

}
