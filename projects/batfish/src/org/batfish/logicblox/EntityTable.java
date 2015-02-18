package org.batfish.logicblox;

import java.math.BigInteger;
import java.util.Arrays;

import org.batfish.representation.Ip;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.NamedPort;

import com.logicblox.connect.Workspace.Relation;
import com.logicblox.connect.Workspace.Relation.EntityColumn;
import com.logicblox.connect.Workspace.Relation.Int64Column;
import com.logicblox.connect.Workspace.Relation.StringColumn;
import com.logicblox.connect.Workspace.Relation.UInt64Column;

public class EntityTable {

   private static final String DYNAMIC_NEXT_HOP_INTERFACE_NAME = "dynamic";
   private static final long NO_TAG = -1l;

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

   public String getFlow(BigInteger index) {
      int listIndex = Arrays.binarySearch(_flowIndices, index);
      String node = _flowNodes[listIndex];
      String srcIp = new Ip(_flowSrcIps[listIndex]).toString();
      String dstIp = new Ip(_flowDstIps[listIndex]).toString();
      IpProtocol protocol = IpProtocol
            .fromNumber((int) (_flowProtocols[listIndex]));
      boolean tcp = protocol == IpProtocol.TCP;
      boolean udp = protocol == IpProtocol.UDP;
      StringBuilder sb = new StringBuilder();
      sb.append("Flow<" + node + ", " + protocol + ", " + srcIp + ", " + dstIp + ", ");
      if (tcp || udp) {
         String srcPort = NamedPort
               .nameFromNumber((int) _flowSrcPorts[listIndex]);
         String dstPort = NamedPort
               .nameFromNumber((int) _flowDstPorts[listIndex]);
         sb.append(srcPort + ", " + dstPort);
      }
      else {
         sb.append("N/A, N/A");
      }
      sb.append(">");
      String output = sb.toString();
      return output;
   }

   public String getNetwork(BigInteger index) {
      int listIndex = Arrays.binarySearch(_networkIndices, index);
      long network = _networkAddresses[listIndex];
      long subnetBits = _networkPrefixLengths[listIndex];
      return new Ip(network).toString() + "/" + subnetBits;
   }

   public String getRoute(BigInteger index) {
      int listIndex = Arrays.binarySearch(_routeIndices, index);
      String node = _routeNodes[listIndex];
      String network = getNetwork(_routeNetworks[listIndex]);
      String nextHopInt = _routeNextHopInts[listIndex];
      Ip nextHopIpAsIp = new Ip(_routeNextHopIps[listIndex]);
      String nextHopIp = nextHopIpAsIp.toString();
      String nextHop = _routeNextHops[listIndex];
      String admin = Long.toString(_routeAdmins[listIndex]);
      String cost = Long.toString(_routeCosts[listIndex]);
      long tagAsLong = _routeTags[listIndex];
      String tag = Long.toString(tagAsLong);
      String protocol = _routeProtocols[listIndex];

      // extra formatting
      if (!nextHopInt.equals(DYNAMIC_NEXT_HOP_INTERFACE_NAME)) {
         // static interface
         if (nextHopIpAsIp.asLong() == 0L) {
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
