package org.batfish.logicblox;

import java.math.BigInteger;
import java.util.Arrays;

import org.batfish.collections.CommunitySet;
import org.batfish.main.BatfishException;
import org.batfish.representation.AsPath;
import org.batfish.representation.BgpAdvertisement;
import org.batfish.representation.Flow;
import org.batfish.representation.Ip;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.OriginType;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

import com.logicblox.connect.Workspace.Relation;
import com.logicblox.connect.Workspace.Relation.EntityColumn;
import com.logicblox.connect.Workspace.Relation.Int64Column;
import com.logicblox.connect.Workspace.Relation.StringColumn;
import com.logicblox.connect.Workspace.Relation.UInt64Column;

public class EntityTable {

   private static final String DYNAMIC_NEXT_HOP_INTERFACE_NAME = "dynamic";

   private static final long IP_NONE_L = -1l;

   private static final long NO_TAG = -1l;

   private static RoutingProtocol getRoutingProtocol(String lbProt) {
      switch (lbProt) {
      case "aggregate":
         return RoutingProtocol.AGGREGATE;
      case "bgp":
         return RoutingProtocol.BGP;
      case "connected":
         return RoutingProtocol.CONNECTED;
      case "egp":
         return RoutingProtocol.EGP;
      case "ibgp":
         return RoutingProtocol.IBGP;
      case "igp":
         return RoutingProtocol.IGP;
      case "isis":
         return RoutingProtocol.ISIS;
      case "isisL1":
         return RoutingProtocol.ISIS_L1;
      case "isisL2":
         return RoutingProtocol.ISIS_L2;
      case "ldp":
         return RoutingProtocol.LDP;
      case "local":
         return RoutingProtocol.LOCAL;
      case "msdp":
         return RoutingProtocol.MSDP;
      case "ospf":
         return RoutingProtocol.OSPF;
      case "ospfE1":
         return RoutingProtocol.OSPF_E1;
      case "ospfE2":
         return RoutingProtocol.OSPF_E2;
      case "ospfIA":
         return RoutingProtocol.OSPF_IA;
      case "rsvp":
         return RoutingProtocol.RSVP;
      case "static":
         return RoutingProtocol.STATIC;
      default:
         throw new BatfishException(
               "Missing conversion for lb routing protocol: \"" + lbProt + "\"");
      }
   }

   private AsPath[] _advertAsPaths;

   private CommunitySet[] _advertCommunitySets;

   private long[] _advertDstIps;

   private String[] _advertDstNodes;

   private BigInteger[] _advertIndices;

   private long[] _advertLocalPrefs;

   private long[] _advertMeds;

   private BigInteger[] _advertNetworks;

   private long[] _advertNextHopIps;

   private long[] _advertOriginatorIps;

   private String[] _advertOriginTypes;

   private long[] _advertSrcIps;

   private String[] _advertSrcNodes;

   private String[] _advertSrcProtocols;

   private String[] _advertTypes;

   private long[] _flowDstIps;

   private long[] _flowDstPorts;

   private BigInteger[] _flowIndices;

   private String[] _flowNodes;

   private long[] _flowProtocols;

   private long[] _flowSrcIps;

   private long[] _flowSrcPorts;

   private String[] _flowTags;

   private long[] _networkAddresses;

   private BigInteger[] _networkIndices;

   private long[] _networkPrefixLengths;

   private long[] _routeAdmins;

   private long[] _routeCosts;

   private BigInteger[] _routeIndices;

   private BigInteger[] _routeNetworks;

   private String[] _routeNextHopInts;

   private long[] _routeNextHopIps;

   private String[] _routeNextHops;

   private String[] _routeNodes;

   private String[] _routeProtocols;

   private long[] _routeTags;

   public EntityTable(LogicBloxFrontend lbf) {

      EntityColumn ec;

      // Network
      Relation networkIndex = lbf.queryPredicate("libbatfish:Ip:Network_index");
      ec = (EntityColumn) networkIndex.getColumns().get(0);
      _networkIndices = ((UInt64Column) ec.getIndexColumn().unwrap()).getRows();
      _networkAddresses = ((Int64Column) networkIndex.getColumns().get(1))
            .getRows();
      _networkPrefixLengths = ((Int64Column) networkIndex.getColumns().get(3))
            .getRows();

      // Flow
      Relation currentFlowProperty;
      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_dstIp");
      _flowDstIps = ((Int64Column) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_dstPort");
      _flowDstPorts = ((Int64Column) currentFlowProperty.getColumns().get(1))
            .getRows();

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_node");
      _flowNodes = ((StringColumn) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_ipProtocol");
      _flowProtocols = ((Int64Column) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_srcIp");
      _flowSrcIps = ((Int64Column) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_srcPort");
      _flowSrcPorts = ((Int64Column) currentFlowProperty.getColumns().get(1))
            .getRows();

      currentFlowProperty = lbf.queryPredicate("libbatfish:Flow:Flow_tag");
      _flowTags = ((StringColumn) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      // get indices
      ec = (EntityColumn) currentFlowProperty.getColumns().get(0);
      _flowIndices = ((UInt64Column) ec.getIndexColumn().unwrap()).getRows();

      // Route
      Relation currentRouteProperty;
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_admin");
      _routeAdmins = ((Int64Column) currentRouteProperty.getColumns().get(1))
            .getRows();
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_cost");
      _routeCosts = ((Int64Column) currentRouteProperty.getColumns().get(1))
            .getRows();
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:Route_network");
      _routeNetworks = ((UInt64Column) ((EntityColumn) currentRouteProperty
            .getColumns().get(1)).getIndexColumn().unwrap()).getRows();
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_nextHop");
      _routeNextHops = ((StringColumn) ((EntityColumn) currentRouteProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_nextHopInt");
      _routeNextHopInts = ((StringColumn) ((EntityColumn) currentRouteProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_nextHopIp");
      _routeNextHopIps = ((Int64Column) ((EntityColumn) currentRouteProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentRouteProperty = lbf.queryPredicate("libbatfish:Route:Route_node");
      _routeNodes = ((StringColumn) ((EntityColumn) currentRouteProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:Route_protocol");
      _routeProtocols = ((StringColumn) ((EntityColumn) currentRouteProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentRouteProperty = lbf
            .queryPredicate("libbatfish:Route:RouteDetails_tag");
      _routeTags = ((Int64Column) currentRouteProperty.getColumns().get(1))
            .getRows();

      // get indices
      currentRouteProperty = lbf.queryPredicate("libbatfish:Route:Route");
      ec = (EntityColumn) currentRouteProperty.getColumns().get(0);
      _routeIndices = ((UInt64Column) ec.getIndexColumn().unwrap()).getRows();

      // BgpAdvertisement
      Relation currentAdvertProperty;
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_type");
      _advertTypes = ((StringColumn) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_network");
      _advertNetworks = ((UInt64Column) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getIndexColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_nextHopIp");
      _advertNextHopIps = ((Int64Column) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_srcIp");
      _advertSrcIps = ((Int64Column) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_dstIp");
      _advertDstIps = ((Int64Column) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_srcProtocol");
      _advertSrcProtocols = ((StringColumn) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_srcNode");
      _advertSrcNodes = ((StringColumn) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_dstNode");
      _advertDstNodes = ((StringColumn) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_localPref");
      _advertLocalPrefs = ((Int64Column) currentAdvertProperty.getColumns()
            .get(1)).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_med");
      _advertMeds = ((Int64Column) currentAdvertProperty.getColumns().get(1))
            .getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_originatorIp");
      _advertOriginatorIps = ((Int64Column) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();
      currentAdvertProperty = lbf
            .queryPredicate("libbatfish:BgpAdvertisement:BgpAdvertisement_originType");
      _advertOriginTypes = ((StringColumn) ((EntityColumn) currentAdvertProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      // get indices
      ec = (EntityColumn) currentAdvertProperty.getColumns().get(0);
      _advertIndices = ((UInt64Column) ec.getIndexColumn().unwrap()).getRows();

      // AsPath
      _advertAsPaths = new AsPath[_advertIndices.length];
      Relation advertisementPathSizeRelation = lbf
            .queryPredicate("libbatfish:AsPath:AdvertisementPathSize");
      ec = (EntityColumn) advertisementPathSizeRelation.getColumns().get(0);
      BigInteger[] asPathSizeAdvertIndices = ((UInt64Column) ec
            .getIndexColumn().unwrap()).getRows();
      long[] asPathSizes = ((Int64Column) advertisementPathSizeRelation
            .getColumns().get(1)).getRows();
      for (int i = 0; i < asPathSizeAdvertIndices.length; i++) {
         BigInteger key = asPathSizeAdvertIndices[i];
         int advertIndex = Arrays.binarySearch(_advertIndices, key);
         int currentPathSize = (int) asPathSizes[i];
         AsPath asPath = new AsPath(currentPathSize);
         _advertAsPaths[advertIndex] = asPath;
      }

      Relation advertisementPathRelation = lbf
            .queryPredicate("libbatfish:AsPath:AdvertisementPath");
      ec = (EntityColumn) advertisementPathRelation.getColumns().get(0);
      BigInteger[] asPathAdvertIndices = ((UInt64Column) ec.getIndexColumn()
            .unwrap()).getRows();
      long[] asPathPathIndices = ((Int64Column) advertisementPathRelation
            .getColumns().get(1)).getRows();
      long[] asPathAses = ((Int64Column) ((EntityColumn) advertisementPathRelation
            .getColumns().get(2)).getRefModeColumn().unwrap()).getRows();
      for (int i = 0; i < asPathAdvertIndices.length; i++) {
         BigInteger key = asPathAdvertIndices[i];
         int advertIndex = Arrays.binarySearch(_advertIndices, key);
         AsPath asPath = _advertAsPaths[advertIndex];
         int currentPathIndex = (int) asPathPathIndices[i];
         int currentAs = (int) asPathAses[i];
         asPath.get(currentPathIndex).add(currentAs);
      }

      // Commmunities
      _advertCommunitySets = new CommunitySet[_advertIndices.length];
      Relation advertisementCommunityRelation = lbf
            .queryPredicate("libbatfish:CommunityList:AdvertisementCommunity");
      ec = (EntityColumn) advertisementCommunityRelation.getColumns().get(0);
      BigInteger[] communityAdvertIndices = ((UInt64Column) ec.getIndexColumn()
            .unwrap()).getRows();
      long[] advertCommunities = ((Int64Column) advertisementCommunityRelation
            .getColumns().get(1)).getRows();
      for (int i = 0; i < communityAdvertIndices.length; i++) {
         BigInteger key = communityAdvertIndices[i];
         int advertIndex = Arrays.binarySearch(_advertIndices, key);
         CommunitySet communitySet = _advertCommunitySets[advertIndex];
         if (communitySet == null) {
            communitySet = new CommunitySet();
            _advertCommunitySets[advertIndex] = communitySet;
         }
         long community = advertCommunities[i];
         communitySet.add(community);
      }
   }

   public String getBgpAdvertisement(BigInteger index) {
      int listIndex = Arrays.binarySearch(_advertIndices, index);
      String type = _advertTypes[listIndex];
      String network = getNetwork(_advertNetworks[listIndex]);
      String nextHopIp = new Ip(_advertNextHopIps[listIndex]).toString();
      String srcIp = new Ip(_advertSrcIps[listIndex]).toString();
      String dstIp = new Ip(_advertDstIps[listIndex]).toString();
      String srcProtocol = _advertSrcProtocols[listIndex];
      String srcNode = _advertSrcNodes[listIndex];
      String dstNode = _advertDstNodes[listIndex];
      String localPref = Long.toString(_advertLocalPrefs[listIndex]);
      String med = Long.toString(_advertMeds[listIndex]);
      String originatorIp = new Ip(_advertOriginatorIps[listIndex]).toString();
      String originType = _advertOriginTypes[listIndex];
      return "BgpAdvert<" + type + ", " + network + ", " + nextHopIp + ", "
            + srcIp + ", " + dstIp + ", " + srcProtocol + ", " + srcNode + ", "
            + dstNode + ", " + localPref + ", " + med + ", " + originatorIp
            + ", " + originType + ">";
   }

   public Flow getFlow(BigInteger index) {
      int listIndex = Arrays.binarySearch(_flowIndices, index);
      String node = _flowNodes[listIndex];
      Ip srcIp = new Ip(_flowSrcIps[listIndex]);
      Ip dstIp = new Ip(_flowDstIps[listIndex]);
      IpProtocol protocol = IpProtocol
            .fromNumber((int) (_flowProtocols[listIndex]));
      int srcPort = (int) _flowSrcPorts[listIndex];
      int dstPort = (int) _flowDstPorts[listIndex];
      String tag = _flowTags[listIndex];
      Flow flow = new Flow(node, srcIp, dstIp, srcPort, dstPort, protocol, tag);
      return flow;
   }

   public String getFlowText(BigInteger index) {
      Flow flow = getFlow(index);
      return flow.toString();
   }

   public String getNetwork(BigInteger index) {
      int listIndex = Arrays.binarySearch(_networkIndices, index);
      long network = _networkAddresses[listIndex];
      long subnetBits = _networkPrefixLengths[listIndex];
      return new Ip(network).toString() + "/" + subnetBits;
   }

   public BgpAdvertisement getPrecomputedBgpAdvertisement(BigInteger index) {
      int listIndex = Arrays.binarySearch(_advertIndices, index);
      String type = _advertTypes[listIndex];
      Prefix network = new Prefix(getNetwork(_advertNetworks[listIndex]));
      Ip nextHopIp = new Ip(_advertNextHopIps[listIndex]);
      String srcNode = _advertSrcNodes[listIndex];
      Ip srcIp = new Ip(_advertSrcIps[listIndex]);
      String dstNode = _advertDstNodes[listIndex];
      Ip dstIp = new Ip(_advertDstIps[listIndex]);
      RoutingProtocol srcProtocol = getRoutingProtocol(_advertSrcProtocols[listIndex]);
      OriginType originType = OriginType
            .fromString(_advertOriginTypes[listIndex]);
      int localPreference = (int) _advertLocalPrefs[listIndex];
      int med = (int) _advertMeds[listIndex];
      Ip originatorIp = new Ip(_advertOriginatorIps[listIndex]);
      AsPath asPath = _advertAsPaths[listIndex];
      CommunitySet communities = _advertCommunitySets[listIndex];
      if (communities == null) {
         communities = new CommunitySet();
      }
      return new BgpAdvertisement(type, network, nextHopIp, srcNode, srcIp,
            dstNode, dstIp, srcProtocol, originType, localPreference, med,
            originatorIp, asPath, communities);
   }

   public PrecomputedRoute getPrecomputedRoute(BigInteger index) {
      int listIndex = Arrays.binarySearch(_routeIndices, index);
      String node = _routeNodes[listIndex];
      Prefix network = new Prefix(getNetwork(_routeNetworks[listIndex]));
      String nextHopInt = _routeNextHopInts[listIndex];
      long nextHopIpAsLong = _routeNextHopIps[listIndex];
      Ip nextHopIpAsIp;
      if (nextHopIpAsLong != IP_NONE_L) {
         nextHopIpAsIp = new Ip(nextHopIpAsLong);
      }
      else {
         return null;
      }
      int admin = (int) _routeAdmins[listIndex];
      int cost = (int) _routeCosts[listIndex];
      int tag = (int) _routeTags[listIndex];
      String lbProtocol = _routeProtocols[listIndex];
      RoutingProtocol protocol = getRoutingProtocol(lbProtocol);
      if (!nextHopInt.equals(DYNAMIC_NEXT_HOP_INTERFACE_NAME)) {
         return null;
      }
      return new PrecomputedRoute(node, network, nextHopIpAsIp, admin, cost,
            protocol, tag);
   }

   public String getRouteAsString(BigInteger index) {
      int listIndex = Arrays.binarySearch(_routeIndices, index);
      String node = _routeNodes[listIndex];
      String network = getNetwork(_routeNetworks[listIndex]);
      String nextHopInt = _routeNextHopInts[listIndex];
      long nextHopIpAsLong = _routeNextHopIps[listIndex];
      String nextHopIp = null;
      if (nextHopIpAsLong != IP_NONE_L) {
         Ip nextHopIpAsIp = new Ip(nextHopIpAsLong);
         nextHopIp = nextHopIpAsIp.toString();
      }
      String nextHop = _routeNextHops[listIndex];
      String admin = Long.toString(_routeAdmins[listIndex]);
      String cost = Long.toString(_routeCosts[listIndex]);
      long tagAsLong = _routeTags[listIndex];
      String tag = Long.toString(tagAsLong);
      String protocol = _routeProtocols[listIndex];

      // extra formatting
      if (!nextHopInt.equals(DYNAMIC_NEXT_HOP_INTERFACE_NAME)) {
         // static interface
         if (nextHopIpAsLong == IP_NONE_L) {
            nextHop = "N/A";
            nextHopIp = "N/A";
         }
      }
      if (tagAsLong == NO_TAG) {
         tag = "none";
      }

      return "Route<" + node + ", " + network + ", " + nextHopIp + ", "
            + nextHop + ", " + nextHopInt + ", " + admin + ", " + cost + ", "
            + tag + ", " + protocol + ">";
   }

}
