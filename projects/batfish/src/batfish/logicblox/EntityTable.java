package batfish.logicblox;

import java.math.BigInteger;
import java.util.Arrays;

import com.logicblox.connect.Workspace.Relation;
import com.logicblox.connect.Workspace.Relation.EntityColumn;
import com.logicblox.connect.Workspace.Relation.Int64Column;
import com.logicblox.connect.Workspace.Relation.StringColumn;
import com.logicblox.connect.Workspace.Relation.UInt64Column;

import batfish.util.Util;

public class EntityTable {

   private long[] _advertDstIps;
   private String[] _advertDstNodes;
   private BigInteger[] _advertIndices;
   private long[] _advertLocalPrefs;
   private long[] _advertMeds;
   private BigInteger[] _advertNetworks;
   private long[] _advertNextHopIps;
   private long[] _advertOriginatorIps;
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

   public EntityTable(LogicBloxFrontend lbf) {

      EntityColumn ec;
      
      // Network
      Relation networkIndex = lbf
            .queryPredicate("libbatfish:Ip:Network_index");
      ec = (EntityColumn) networkIndex.getColumns().get(0);
      _networkIndices = ((UInt64Column) ec.getIndexColumn().unwrap()).getRows();
      _networkAddresses = ((Int64Column) networkIndex.getColumns().get(1))
            .getRows();
      _networkPrefixLengths = ((Int64Column) networkIndex.getColumns().get(3))
            .getRows();

      // Flow
      Relation currentFlowProperty;
      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_dstIp");
      _flowDstIps = ((Int64Column) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_dstPort");
      _flowDstPorts = ((Int64Column) currentFlowProperty.getColumns().get(1))
            .getRows();

      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_node");
      _flowNodes = ((StringColumn) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_ipProtocol");
      _flowProtocols = ((Int64Column) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_srcIp");
      _flowSrcIps = ((Int64Column) ((EntityColumn) currentFlowProperty
            .getColumns().get(1)).getRefModeColumn().unwrap()).getRows();

      currentFlowProperty = lbf
            .queryPredicate("libbatfish:Flow:Flow_srcPort");
      _flowSrcPorts = ((Int64Column) currentFlowProperty.getColumns().get(1))
            .getRows();

      // get indices
      ec = (EntityColumn) currentFlowProperty.getColumns().get(0);
      _flowIndices = ((UInt64Column) ec.getIndexColumn().unwrap()).getRows();

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

      // get indices
      ec = (EntityColumn) currentAdvertProperty.getColumns().get(0);
      _advertIndices = ((UInt64Column) ec.getIndexColumn().unwrap()).getRows();
   }

   public String getBgpAdvertisement(BigInteger index) {
      int listIndex = Arrays.binarySearch(_advertIndices, index);
      String type = _advertTypes[listIndex];
      String network = getNetwork(_advertNetworks[listIndex]);
      String nextHopIp = Util.longToIp(_advertNextHopIps[listIndex]);
      String srcIp = Util.longToIp(_advertSrcIps[listIndex]);
      String dstIp = Util.longToIp(_advertDstIps[listIndex]);
      String srcProtocol = _advertSrcProtocols[listIndex];
      String srcNode = _advertSrcNodes[listIndex];
      String dstNode = _advertDstNodes[listIndex];
      String localPref = Long.toString(_advertLocalPrefs[listIndex]);
      String med = Long.toString(_advertMeds[listIndex]);
      String originatorIp = Util.longToIp(_advertOriginatorIps[listIndex]);
      return "BgpAdvert<" + type + ", " + network + ", " + nextHopIp + ", "
            + srcIp + ", " + dstIp + ", " + srcProtocol + ", " + srcNode + ", "
            + dstNode + ", " + localPref + ", " + med + ", " + originatorIp
            + ">";
   }

   public String getFlow(BigInteger index) {
      int listIndex = Arrays.binarySearch(_flowIndices, index);
      String srcIp = Util.longToIp(_flowSrcIps[listIndex]);
      String dstIp = Util.longToIp(_flowDstIps[listIndex]);
      String srcPort = Util.getPortName((int) _flowSrcPorts[listIndex]);
      String dstPort = Util.getPortName((int) _flowDstPorts[listIndex]);
      String protocol = Util.getProtocolName((int)_flowProtocols[listIndex]);
      String node = _flowNodes[listIndex];
      return "Flow<" + node + ", " + protocol + ", " + srcIp
            + ", " + dstIp + ", "
            + srcPort + ", " + dstPort 
            + ">";
   }

   public String getNetwork(BigInteger index) {
      int listIndex = Arrays.binarySearch(_networkIndices, index);
      long network = _networkAddresses[listIndex];
      long subnetBits = _networkPrefixLengths[listIndex];
      return Util.longToIp(network) + "/" + subnetBits;
   }

}
