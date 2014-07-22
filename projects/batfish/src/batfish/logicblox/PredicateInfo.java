package batfish.logicblox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PredicateInfo {
   private Set<String> _functions;
   private Map<String, String> _predicateSemantics;
   private Map<String, List<LBValueType>> _predicateValueTypes;
   private Map<String, String> _qualifiedNameMap;

   public PredicateInfo(Map<String, String> predicateSemantics) {
      _functions = new LinkedHashSet<String>();
      _predicateSemantics = predicateSemantics;
      _predicateValueTypes = new HashMap<String, List<LBValueType>>();
      _qualifiedNameMap = new LinkedHashMap<String, String>();
      addPredicates();
   }

   private void addPredicates() {
      List<String> currentNames = new ArrayList<String>();
      ArrayList<LBValueType> valueTypeList;

      currentNames.add("libbatfish:IpAccessList:AclDeny");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:AdvertisementClusterId");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.IP); // clusterId
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:CommunityList:AdvertisementCommunity");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_BGP_ADVERTISEMENT); // advert
      valueTypeList.add(LBValueType.INT); // community
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:AdvertisementPath");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.INT); // index
      valueTypeList.add(LBValueType.ENTITY_REF_INT); // as
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:AdvertisementPathSize");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.INT); // size
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:BestBgpAdvertisement");
      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement");
      currentNames.add("libbatfish:Bgp:MaxLocalPrefBgpAdvertisement");
      currentNames.add("libbatfish:Bgp:MinAsPathLengthBgpAdvertisement");
      currentNames.add("libbatfish:Bgp:ReceivedBgpAdvertisement");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_BGP_ADVERTISEMENT); // advert
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Route:BestConnectedRoute");
      currentNames.add("libbatfish:GeneratedRoute:BestGeneratedRoute");
      currentNames.add("libbatfish:Ospf:BestOspfRoute");
      currentNames.add("libbatfish:Ospf:BestOspfIARoute");
      currentNames.add("libbatfish:Ospf:BestOspfE2Route");
      currentNames.add("libbatfish:Route:BestPerProtocolRoute");
      currentNames.add("libbatfish:Route:BestStaticRoute");
      currentNames.add("libbatfish:Route:InstalledRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // nextHop
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.INT); // admin
      valueTypeList.add(LBValueType.INT); // cost
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // protocol
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_constructor");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // type
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // destIpBlock
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // srcIp
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // dstIp
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_details");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // type
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // destIpBlock
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // srcIp
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // dstIp
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // srcProtocol
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // srcNode
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // dstNode
      valueTypeList.add(LBValueType.INT); // localPref
      valueTypeList.add(LBValueType.INT); // med
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // originatorIp
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();      
      
      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_dstNode");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_BGP_ADVERTISEMENT); // advert
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      _functions.addAll(currentNames);
      currentNames.clear();      
      
      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_localPref");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.INT); // localPref
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_network");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_nextHopIp");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_originatorIp");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // originatorIp
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_srcNode");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // srcNode
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_srcProtocol");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // srcProtocol
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:BgpAdvertisement_type");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // type
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:BgpGeneratedRoute");
      currentNames.add("libbatfish:Bgp:SetBgpGeneratedRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:BgpNeighborGeneratedRoute");
      currentNames.add("libbatfish:Bgp:SetBgpNeighborGeneratedRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // neighborIp
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:BgpNeighbors");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING);// node1
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP);// ip1
      valueTypeList.add(LBValueType.ENTITY_REF_STRING);// node2
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP);// ip2
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Route:ConnectedRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Reachability:DifferentNextHops");
      currentNames.add("libbatfish:Reachability:DifferentRoutes");
      currentNames.add("libbatfish:Reachability:SameNextHops");
      currentNames.add("libbatfish:Reachability:SameRoutes");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node1
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node2
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:DataPlane:FibAccept");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
      currentNames.add("libbatfish:DataPlane:FibForward");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
      currentNames.add("libbatfish:DataPlane:FibNetworkForward");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
      currentNames.add("libbatfish:DataPlane:FibLongestPrefixNetworkMatch");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // matchNet
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
      currentNames.add("libbatfish:DataPlane:FibLongestPrefixNetworkMatchPrefixLength");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.INT); // maxLength
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:DataPlane:FibNetworkMatch");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // matchNet
      valueTypeList.add(LBValueType.INT); // matchLength
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
      currentNames.add("libbatfish:Flow:Flow");
      currentNames.add("libbatfish:Traffic:FlowLost");
      currentNames.add("libbatfish:Traffic:FlowUnknown");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Flow:Flow_dstIp");
      currentNames.add("libbatfish:Flow:Flow_srcIp");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // src/dstIp
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      _functions.addAll(currentNames);
      currentNames.clear();

      currentNames.add("libbatfish:Flow:Flow_dstPort");
      currentNames.add("libbatfish:Flow:Flow_srcPort");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.INT); // src/dstPort
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      _functions.addAll(currentNames);
      currentNames.clear();

      currentNames.add("libbatfish:Flow:Flow_ipProtocol");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // protocol
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      _functions.addAll(currentNames);
      currentNames.clear();

      currentNames.add("libbatfish:Traffic:FlowAccepted");
      currentNames.add("libbatfish:Traffic:FlowDropped");
      currentNames.add("libbatfish:Traffic:FlowNoRoute");
      currentNames.add("libbatfish:Traffic:FlowReachPostIn");
      currentNames.add("libbatfish:Traffic:FlowReachPreOut");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Traffic:FlowAllowedIn");
      currentNames.add("libbatfish:Traffic:FlowAllowedOut");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // filter
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Traffic:FlowDeniedIn");
      currentNames.add("libbatfish:Traffic:FlowDeniedOut");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // filter
      valueTypeList.add(LBValueType.INT); // line
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Traffic:FlowDenyIn");
      currentNames.add("libbatfish:Traffic:FlowDenyOut");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Traffic:FlowInconsistent");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Traffic:FlowLoop");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // inInt
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();      
      
      currentNames.add("libbatfish:Traffic:FlowReach");
      currentNames.add("libbatfish:Traffic:FlowReachStep");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // srcNode
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // srcInInt
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // dstNode
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // dstInInt
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();      
      
      currentNames.add("libbatfish:Traffic:FlowReachPostInInterface");
      currentNames.add("libbatfish:Traffic:FlowReachPostOutInterface");
      currentNames.add("libbatfish:Traffic:FlowReachPreInInterface");
      currentNames.add("libbatfish:Traffic:FlowReachPreOutInterface");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:GeneratedRoute:GeneratedRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // contributingNetwork
      valueTypeList.add(LBValueType.INT); // admin
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:IbgpNeighbors");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING);// node1
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP);// ip1
      valueTypeList.add(LBValueType.ENTITY_REF_STRING);// node2
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP);// ip2
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ip:Ip_ZERO");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // ip
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:IpAccessListDeny");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.INT); // line
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:IpAccessListFirstMatch");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      valueTypeList.add(LBValueType.INT); // line
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      _functions.addAll(currentNames);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:IpAccessListLine_dstIp_end");
      currentNames.add("libbatfish:IpAccessList:IpAccessListLine_dstIp_start");
      currentNames.add("libbatfish:IpAccessList:IpAccessListLine_srcIp_end");
      currentNames.add("libbatfish:IpAccessList:IpAccessListLine_srcIp_start");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.INT); // line
      valueTypeList.add(LBValueType.IP); // dst/srcIp_end/start
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      _functions.addAll(currentNames);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:IpAccessListLine_protocol");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.INT); // line
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // protocol
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      _functions.addAll(currentNames);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:IpAccessListMatch");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.INT); // line
      valueTypeList.add(LBValueType.ENTITY_INDEX_FLOW); // flow
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Interface:IpReadyInt");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Node_name
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // ip
      valueTypeList.add(LBValueType.INT); // prefix length
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Layer2:LanAdjacent");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:PolicyMap:need_PolicyMapClauseMatchAdvert");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      valueTypeList.add(LBValueType.ENTITY_INDEX_BGP_ADVERTISEMENT); // advert
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ip:Network");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:BgpAdvertisement:OriginType_EGP");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // originType
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:OspfE2Route");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // nextHop
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.INT); // cost
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // advertiser
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // advertiserIp
      valueTypeList.add(LBValueType.INT); // costToAdvertiser
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:OspfExport");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.INT); // cost
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:OspfGeneratedRoute");
      currentNames.add("libbatfish:Ospf:SetOspfGeneratedRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:OspfIARoute");
      currentNames.add("libbatfish:Ospf:OspfRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // nextHop
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.INT); // cost
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:OspfNeighbors");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node1
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // int1
      valueTypeList.add(LBValueType.INT); // cost1
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node2
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // int2
      valueTypeList.add(LBValueType.INT); // cost2
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.INT); // area
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:OspfNode");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // int
      valueTypeList.add(LBValueType.INT); // cost
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.INT); // area
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:ParentAdvertisement");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // parent
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // child
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:PolicyMap:PolicyMapClauseMatchAdvert");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      valueTypeList.add(LBValueType.INT); // clause
      valueTypeList.add(LBValueType.ENTITY_INDEX_BGP_ADVERTISEMENT); // advert
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:PolicyMap:need_PolicyMapClauseMatchRoute");
      currentNames.add("libbatfish:PolicyMap:PolicyMapClauseMatchRoute");
      currentNames.add("libbatfish:PolicyMap:PolicyMapPermitRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      valueTypeList.add(LBValueType.INT); // clause
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // nextHop
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.INT); // admin
      valueTypeList.add(LBValueType.INT); // cost
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // protocol
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();      
      
      // currentNames.add("need_PolicyMapClauseMatchAdvertPredicate");
      // currentNames.add("PolicyMapClauseMatchAdvertPredicate");
      currentNames.add("libbatfish:PolicyMap:PolicyMapPermitAdvert");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      valueTypeList.add(LBValueType.INT); // clause
      valueTypeList.add(LBValueType.ENTITY_INDEX_INT); // advert
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:RouteFilter:need_RouteFilterMatch");
      currentNames.add("libbatfish:RouteFilter:RouteFilterMatch");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.INT); // line
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:RouteFilter:need_RouteFilterPermitNetwork");
      currentNames.add("libbatfish:RouteFilter:RouteFilterPermitNetwork");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Layer2:SamePhysicalSegment");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Node1
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Interface1
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Node2
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Interface2
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:SetBgpExportPolicy");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // ip
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
      currentNames.add("libbatfish:Bgp:SetBgpGeneratedRoutePolicy");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:SetBgpNeighborGeneratedRoutePolicy");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // neighborIp
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Bgp:SetBgpNeighborIp");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // ip
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
      currentNames.add("libbatfish:Bgp:SetBgpOriginationPolicy");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Node
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // neighborIp
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:GeneratedRoute:SetGeneratedRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.INT); // admin
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:GeneratedRoute:SetGeneratedRoutePolicy");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:DataPlane:SetInterfaceFilterIn");
      currentNames.add("libbatfish:DataPlane:SetInterfaceFilterOut");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // interface
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:SetIpAccessListDenyLine");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.INT); // line
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:SetIpAccessListLine");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.INT); // line
      valueTypeList.add(LBValueType.ENTITY_REF_INT); // protocol
      valueTypeList.add(LBValueType.IP); // srcIp_start
      valueTypeList.add(LBValueType.IP); // srcIp_end
      valueTypeList.add(LBValueType.IP); // dstIp_start
      valueTypeList.add(LBValueType.IP); // dstIp_end
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:IpAccessList:SetIpAccessListLine_dstPortRange");
      currentNames.add("libbatfish:IpAccessList:SetIpAccessListLine_srcPortRange");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // list
      valueTypeList.add(LBValueType.INT); // line
      valueTypeList.add(LBValueType.INT); // dst/srcPort_start
      valueTypeList.add(LBValueType.INT); // dst/srcPort_end
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Node:SetNodeVendor");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // vendor
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:SetOspfGeneratedRoutePolicy");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:SetOspfInterfaceCost");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // Interface
      valueTypeList.add(LBValueType.INT); // area
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Ospf:SetOspfOutboundPolicyMap");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:PolicyMap:SetPolicyMapClauseMatchNeighbor");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      valueTypeList.add(LBValueType.INT);// clause
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // neighborIp
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:PolicyMap:SetPolicyMapClauseMatchProtocol");
      currentNames.add("libbatfish:PolicyMap:SetPolicyMapClauseMatchRouteFilter");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      valueTypeList.add(LBValueType.INT);// clause
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // filter / protocol
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:PolicyMap:SetPolicyMapClauseSetMetric");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // map
      valueTypeList.add(LBValueType.INT);// clause
      valueTypeList.add(LBValueType.INT); // metric
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:RouteFilter:SetRouteFilterLine");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // routefilter
      valueTypeList.add(LBValueType.INT);// line
      valueTypeList.add(LBValueType.IP);// network_start
      valueTypeList.add(LBValueType.IP);// network_end
      valueTypeList.add(LBValueType.INT);// min_prefix
      valueTypeList.add(LBValueType.INT);// max_prefix
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:RouteFilter:SetRouteFilterPermitLine");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // routefilter
      valueTypeList.add(LBValueType.INT);// line
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Route:SetStaticRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.INT); // admin
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();

      currentNames.add("libbatfish:Route:StaticRoute");
      valueTypeList = new ArrayList<LBValueType>();
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // node
      valueTypeList.add(LBValueType.ENTITY_INDEX_NETWORK); // network
      valueTypeList.add(LBValueType.ENTITY_REF_STRING); // nextHop
      valueTypeList.add(LBValueType.ENTITY_INDEX_IP); // nextHopIp
      valueTypeList.add(LBValueType.INT); // admin
      updateQualifiedNameMap(currentNames);
      addValueTypes(currentNames, valueTypeList);
      currentNames.clear();
      
   }

   private void addValueTypes(List<String> names,
         ArrayList<LBValueType> valueTypeList) {
      for (String name : names) {
         _predicateValueTypes.put(name, valueTypeList);
      }
   }

   public Map<String, String> getPredicateNames() {
      return _qualifiedNameMap;
   }

   public Map<String, String> getPredicateSemantics() {
      return _predicateSemantics;
   }

   public String getPredicateSemantics(String unqualifiedPredicateName) {
      return _predicateSemantics.get(unqualifiedPredicateName);
   }

   public List<LBValueType> getPredicateValueTypes(String unqualifiedPredicateName) {
      return _predicateValueTypes.get(_qualifiedNameMap.get(unqualifiedPredicateName));
   }

   public boolean isFunction(String relationName) {
      return _functions.contains(relationName);
   }

   private void updateQualifiedNameMap(List<String> qualifiedNames) {
      for (String qualifiedName : qualifiedNames) {
         String unqualifiedName = qualifiedName.substring(qualifiedName
               .lastIndexOf(":") + 1);
         _qualifiedNameMap.put(unqualifiedName, qualifiedName);
      }
   }

}
