package org.batfish.nxtnet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NxtnetConstants {

   public static final Set<String> NXTNET_DATA_PLANE_COMPUTATION_FACTS = initNxtnetDataPlaneComputationFacts();

   public static final Set<String> NXTNET_DATA_PLANE_ENTITY_SYMBOLS = initNxtnetDataPlaneEntitySymbols();

   public static final Set<String> NXTNET_DATA_PLANE_OUTPUT_DEBUG_SYMBOLS = initNxtnetDataPlaneOutputDebugSymbols();

   public static final Set<String> NXTNET_DATA_PLANE_OUTPUT_SYMBOLS = initNxtnetDataPlaneOutputSymbols();

   public static final Set<String> NXTNET_TRAFFIC_COMPUTATION_CONTROL_PLANE_FACTS = initNxtnetTrafficComputationControlPlaneFacts();

   public static final Set<String> NXTNET_TRAFFIC_COMPUTATION_FLOW_FACTS = initNxtnetTrafficComputationFlowFacts();

   public static final Set<String> NXTNET_TRAFFIC_ENTITY_SYMBOLS = initNxtnetTrafficEntitySymbols();

   public static final Set<String> NXTNET_TRAFFIC_OUTPUT_DEBUG_SYMBOLS = initNxtnetTrafficOutputDebugSymbols();

   public static final Set<String> NXTNET_TRAFFIC_OUTPUT_SYMBOLS = initNxtnetTrafficOutputSymbols();

   private static Set<String> initNxtnetDataPlaneComputationFacts() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "SamePhysicalSegment",
            "SetActiveInt",
            "SetAsPathLineDeny",
            "SetAsPathLineMatchAs",
            "SetAsPathLineMatchAsAtBeginning",
            "SetAsPathLineMatchAsPair",
            "SetAsPathLineMatchAsPairAtBeginning",
            "SetAsPathLineMatchEmpty",
            "SetAsPathLinePermit",
            "SetBgpAdvertisementCommunity",
            "SetBgpAdvertisement_flat",
            "SetBgpAdvertisementPath",
            "SetBgpAdvertisementPathSize",
            "SetBgpDefaultLocalPref_flat",
            "SetBgpExportPolicy_flat",
            "SetBgpGeneratedRoute_flat",
            "SetBgpGeneratedRoutePolicy_flat",
            "SetBgpImportPolicy_flat",
            "SetBgpMultihopNeighborNetwork_flat",
            "SetBgpNeighborDefaultMetric_flat",
            "SetBgpNeighborGeneratedRoute_flat",
            "SetBgpNeighborGeneratedRoutePolicy_flat",
            "SetBgpNeighborLocalIp_flat",
            "SetBgpNeighborNetwork_flat",
            "SetBgpNeighborSendCommunity_flat",
            "SetBgpOriginationPolicy_flat",
            "SetCommunityListLine",
            "SetCommunityListLinePermit",
            "SetCrossZoneFilter",
            "SetDefaultCrossZoneAccept",
            "SetDefaultInboundAccept",
            "SetExternalBgpRemoteIp",
            "SetFakeInterface",
            "SetFlowSinkInterface",
            "SetGeneratedRouteDiscard_flat",
            "SetGeneratedRoute_flat",
            "SetGeneratedRouteMetric_flat",
            "SetGeneratedRoutePolicy_flat",
            "SetIbgpNeighbors",
            "SetInboundInterfaceFilter",
            "SetInterfaceFilterIn",
            "SetInterfaceFilterOut",
            "SetInterfaceRoutingPolicy",
            "SetInterfaceZone",
            "SetIpAccessListLine_deny",
            "SetIpAccessListLine_dscp",
            "SetIpAccessListLine_dstIpRange",
            "SetIpAccessListLine_dstPortRange",
            "SetIpAccessListLine_ecn",
            "SetIpAccessListLine_icmpCode",
            "SetIpAccessListLine_icmpType",
            "SetIpAccessListLine_permit",
            "SetIpAccessListLine_protocol",
            "SetIpAccessListLine_srcIpRange",
            "SetIpAccessListLine_srcOrDstIpRange",
            "SetIpAccessListLine_srcOrDstPortRange",
            "SetIpAccessListLine_srcPortRange",
            "SetIpAccessListLine_tcpFlags",
            "SetIpAccessListLine_tcpFlagsCWR",
            "SetIpAccessListLine_tcpFlagsECE",
            "SetIpAccessListLine_tcpFlagsURG",
            "SetIpAccessListLine_tcpFlagsACK",
            "SetIpAccessListLine_tcpFlagsPSH",
            "SetIpAccessListLine_tcpFlagsRST",
            "SetIpAccessListLine_tcpFlagsSYN",
            "SetIpAccessListLine_tcpFlagsFIN",
            "SetIpInt",
            "SetIsisArea",
            "SetIsisGeneratedRoute_flat",
            "SetIsisGeneratedRoutePolicy_flat",
            "SetIsisInterfaceCost",
            "SetIsisL1ActiveInterface",
            "SetIsisL1Node",
            "SetIsisL1PassiveInterface",
            "SetIsisL2ActiveInterface",
            "SetIsisL2Node",
            "SetIsisL2PassiveInterface",
            "SetIsisOutboundPolicyMap",
            "SetLinkLoadLimitIn",
            "SetLinkLoadLimitOut",
            "SetLocalAs_flat",
            "SetNetwork",
            "SetNodeRole",
            "SetNodeVendor",
            "SetOspfGeneratedRoute_flat",
            "SetOspfGeneratedRoutePolicy_flat",
            "SetOspfInterface",
            "SetOspfInterfaceCost",
            "SetOspfOutboundPolicyMap",
            "SetOspfRouterId",
            "SetPolicyMapClauseAddCommunity",
            "SetPolicyMapClauseDeleteCommunity",
            "SetPolicyMapClauseDeny",
            "SetPolicyMapClauseMatchAcl",
            "SetPolicyMapClauseMatchAsPath",
            "SetPolicyMapClauseMatchColor",
            "SetPolicyMapClauseMatchCommunityList",
            "SetPolicyMapClauseMatchInterface",
            "SetPolicyMapClauseMatchNeighbor",
            "SetPolicyMapClauseMatchPolicy",
            "SetPolicyMapClauseMatchPolicyConjunction",
            "SetPolicyMapClauseMatchProtocol",
            "SetPolicyMapClauseMatchRouteFilter",
            "SetPolicyMapClauseMatchTag",
            "SetPolicyMapClausePermit",
            "SetPolicyMapClauseSetCommunity",
            "SetPolicyMapClauseSetCommunityNone",
            "SetPolicyMapClauseSetLocalPreference",
            "SetPolicyMapClauseSetMetric",
            "SetPolicyMapClauseSetNextHopIp",
            "SetPolicyMapClauseSetOriginType",
            "SetPolicyMapClauseSetProtocol",
            "SetPolicyMapIsisExternalRouteType",
            "SetPolicyMapOspfExternalRouteType",
            "SetPrecomputedRoute_flat",
            "SetRemoteAs_flat",
            "SetRouteFilterLine",
            "SetRouteFilterPermitLine",
            "SetRouteReflectorClient_flat",
            "SetStaticIntRoute_flat",
            "SetStaticRoute_flat",
            "SetSwitchportAccess",
            "SetSwitchportTrunkAllows",
            "SetSwitchportTrunkEncapsulation",
            "SetSwitchportTrunkNative",
            "SetVlanInterface",
            "SetZoneFromHostFilter",
            "SetZoneToHostFilter" };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static Set<String> initNxtnetDataPlaneEntitySymbols() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "AdvertisementCommunity",
            "AdvertisementPath",
            "AdvertisementPathSize",
            "BgpAdvertisement_dstIp",
            "BgpAdvertisement_dstNode",
            "BgpAdvertisement_localPref",
            "BgpAdvertisement_med",
            "BgpAdvertisement_network",
            "BgpAdvertisement_nextHopIp",
            "BgpAdvertisement_originatorIp",
            "BgpAdvertisement_originType",
            "BgpAdvertisement_srcIp",
            "BgpAdvertisement_srcNode",
            "BgpAdvertisement_srcProtocol",
            "BgpAdvertisement_type",
            "Network_index",
            "Route",
            "RouteDetails_admin",
            "RouteDetails_cost",
            "RouteDetails_nextHop",
            "RouteDetails_nextHopInt",
            "RouteDetails_nextHopIp",
            "RouteDetails_tag",
            "Route_network",
            "Route_node",
            "Route_protocol" };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static final Set<String> initNxtnetDataPlaneOutputDebugSymbols() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "BgpImportPolicy",
            "BgpMultihopNeighborIp",
            "BgpMultihopNeighborTo",
            "BgpNeighborIp",
            "BgpNeighbors",
            "Ip",
            "IpAccessListMatchDscp",
            "IpAccessListMatchEcn",
            "IpAccessListMatchTcpFlags",
            "IpAccessListMatchTcpFlagsCWR",
            "IpAccessListMatchTcpFlagsECE",
            "IpAccessListMatchTcpFlagsURG",
            "IpAccessListMatchTcpFlagsACK",
            "IpAccessListMatchTcpFlagsPSH",
            "IpAccessListMatchTcpFlagsRST",
            "IpAccessListMatchTcpFlagsSYN",
            "IpAccessListMatchTcpFlagsFIN",
            "IsisExport",
            "IsisL2EnabledInterface",
            "LocalAs",
            "NetworkOf",
            "need_PolicyMapMatchAdvert",
            "PolicyMapClauseMatchAdvert",
            "PolicyMapClauseMatchRoute",
            "PolicyMapClauseTransformAdvert",
            "PolicyMapConjunctionDenyAdvert",
            "PolicyMapConjunctionDenyRoute",
            "PolicyMapDenyAdvert",
            "PolicyMapDenyRoute",
            "PolicyMapPermitAdvert",
            "PolicyMapPermitRoute",
            "SetBgpAdvertisement_flat",
            "SetBgpMultihopNeighborNetwork",
            "SetBgpMultihopNeighborNetwork_flat",
            "SetBgpNeighborNetwork",
            "SetBgpNeighborNetwork_flat",
            "SetExternalBgpRemoteIp",
            "SetIpAccessListLine_dscp",
            "SetIpAccessListLine_ecn",
            "SetIpAccessListLine_tcpFlags",
            "SetIpAccessListLine_tcpFlagsCWR",
            "SetIpAccessListLine_tcpFlagsECE",
            "SetIpAccessListLine_tcpFlagsURG",
            "SetIpAccessListLine_tcpFlagsACK",
            "SetIpAccessListLine_tcpFlagsPSH",
            "SetIpAccessListLine_tcpFlagsRST",
            "SetIpAccessListLine_tcpFlagsSYN",
            "SetIpAccessListLine_tcpFlagsFIN",
            "SetIsisOutboundPolicyMap",
            "SetLocalAs",
            "SetLocalAs_flat",
            "SetNetwork",
            "SetPolicyMapClauseMatchInterface",
            "SetPolicyMapClauseMatchPolicy",
            "SetPolicyMapClauseMatchPolicyConjunction",
            "SetPolicyMapClauseMatchProtocol", };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static final Set<String> initNxtnetDataPlaneOutputSymbols() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "AdvertisementCommunity",
            "AdvertisementPath",
            "AdvertisementPathSize",
            "BgpAdvertisement",
            "BgpAdvertisement_dstIp",
            "BgpAdvertisement_dstNode",
            "BgpAdvertisement_localPref",
            "BgpAdvertisement_med",
            "BgpAdvertisement_network",
            "BgpAdvertisement_nextHopIp",
            "BgpAdvertisement_originatorIp",
            "BgpAdvertisement_originType",
            "BgpAdvertisement_srcIp",
            "BgpAdvertisement_srcNode",
            "BgpAdvertisement_srcProtocol",
            "BgpAdvertisement_type",
            "FibForwardPolicyRouteNextHopIp",
            "FibNetwork",
            "InstalledRoute",
            "InterfaceRoute_nextHopInt",
            "Network_index",
            "ReceivedBgpAdvertisement",
            "Route",
            "RouteDetails_admin",
            "RouteDetails_cost",
            "RouteDetails_nextHop",
            "RouteDetails_nextHopInt",
            "RouteDetails_nextHopIp",
            "RouteDetails_tag",
            "Route_network",
            "Route_node",
            "Route_protocol",
            "SetFlowSinkInterface", };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static final Set<String> initNxtnetTrafficComputationControlPlaneFacts() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "SamePhysicalSegment",
            "SetActiveInt",
            "SetAsPathLineDeny",
            "SetAsPathLineMatchAs",
            "SetAsPathLineMatchAsAtBeginning",
            "SetAsPathLineMatchAsPair",
            "SetAsPathLineMatchAsPairAtBeginning",
            "SetAsPathLineMatchEmpty",
            "SetAsPathLinePermit",
            "SetCommunityListLine",
            "SetCommunityListLinePermit",
            "SetCrossZoneFilter",
            "SetDefaultCrossZoneAccept",
            "SetDefaultInboundAccept",
            "SetFakeInterface",
            "SetFlowSinkInterface",
            "SetInboundInterfaceFilter",
            "SetInterfaceFilterIn",
            "SetInterfaceFilterOut",
            "SetInterfaceZone",
            "SetInterfaceRoutingPolicy",
            "SetIpAccessListLine_deny",
            "SetIpAccessListLine_dscp",
            "SetIpAccessListLine_dstIpRange",
            "SetIpAccessListLine_dstPortRange",
            "SetIpAccessListLine_ecn",
            "SetIpAccessListLine_icmpCode",
            "SetIpAccessListLine_icmpType",
            "SetIpAccessListLine_permit",
            "SetIpAccessListLine_protocol",
            "SetIpAccessListLine_srcIpRange",
            "SetIpAccessListLine_srcOrDstIpRange",
            "SetIpAccessListLine_srcOrDstPortRange",
            "SetIpAccessListLine_srcPortRange",
            "SetIpAccessListLine_tcpFlags",
            "SetIpAccessListLine_tcpFlagsCWR",
            "SetIpAccessListLine_tcpFlagsECE",
            "SetIpAccessListLine_tcpFlagsURG",
            "SetIpAccessListLine_tcpFlagsACK",
            "SetIpAccessListLine_tcpFlagsPSH",
            "SetIpAccessListLine_tcpFlagsRST",
            "SetIpAccessListLine_tcpFlagsSYN",
            "SetIpAccessListLine_tcpFlagsFIN",
            "SetIpInt",
            "SetLinkLoadLimitIn",
            "SetLinkLoadLimitOut",
            "SetLocalAs_flat",
            "SetNetwork",
            "SetNodeRole",
            "SetNodeVendor",
            "SetPolicyMapClauseAddCommunity",
            "SetPolicyMapClauseDeleteCommunity",
            "SetPolicyMapClauseDeny",
            "SetPolicyMapClauseMatchAcl",
            "SetPolicyMapClauseMatchAsPath",
            "SetPolicyMapClauseMatchColor",
            "SetPolicyMapClauseMatchCommunityList",
            "SetPolicyMapClauseMatchInterface",
            "SetPolicyMapClauseMatchNeighbor",
            "SetPolicyMapClauseMatchPolicy",
            "SetPolicyMapClauseMatchPolicyConjunction",
            "SetPolicyMapClauseMatchProtocol",
            "SetPolicyMapClauseMatchRouteFilter",
            "SetPolicyMapClauseMatchTag",
            "SetPolicyMapClausePermit",
            "SetPolicyMapClauseSetCommunity",
            "SetPolicyMapClauseSetCommunityNone",
            "SetPolicyMapClauseSetLocalPreference",
            "SetPolicyMapClauseSetMetric",
            "SetPolicyMapClauseSetNextHopIp",
            "SetPolicyMapClauseSetOriginType",
            "SetPolicyMapClauseSetProtocol",
            "SetPolicyMapIsisExternalRouteType",
            "SetPolicyMapOspfExternalRouteType",
            "SetPrecomputedRoute_flat",
            "SetRouteFilterLine",
            "SetRouteFilterPermitLine",
            "SetStaticIntRoute_flat",
            "SetSwitchportAccess",
            "SetSwitchportTrunkAllows",
            "SetSwitchportTrunkEncapsulation",
            "SetSwitchportTrunkNative",
            "SetVlanInterface",
            "SetZoneFromHostFilter",
            "SetZoneToHostFilter" };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static final Set<String> initNxtnetTrafficComputationFlowFacts() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "DuplicateRoleFlows",
            "SetFlowOriginate",
            "SetPrecomputedRoute_flat" };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static final Set<String> initNxtnetTrafficEntitySymbols() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "Flow_dscp",
            "Flow_dstIp",
            "Flow_dstPort",
            "Flow_ecn",
            "Flow_icmpCode",
            "Flow_icmpType",
            "Flow_ipProtocol",
            "Flow_node",
            "Flow_srcIp",
            "Flow_srcPort",
            "Flow_tcpFlagsCWR",
            "Flow_tcpFlagsECE",
            "Flow_tcpFlagsURG",
            "Flow_tcpFlagsACK",
            "Flow_tcpFlagsPSH",
            "Flow_tcpFlagsRST",
            "Flow_tcpFlagsSYN",
            "Flow_tcpFlagsFIN",
            "Flow_tag" };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static final Set<String> initNxtnetTrafficOutputDebugSymbols() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "FlowAccepted",
            "FlowDeniedIn",
            "FlowDeniedInCrossZoneFilter",
            "FlowDeniedInInboundFilter",
            "FlowDeniedInInterfaceAcl",
            "FlowDeniedInToHostFilter",
            "FlowDeniedOut",
            "FlowDeniedOutCrossZone",
            "FlowDeniedOutHostOut",
            "FlowDeniedOutInterfaceAcl",
            "FlowInboundInterface",
            "FlowMatchRoute",
            "FlowNonInboundNullSrcZone",
            "FlowNonInboundSrcInterface",
            "FlowNonInboundSrcZone",
            "FlowReachPostIn",
            "FlowReachPostInboundCrossZoneAcl",
            "FlowReachPostInboundFilter",
            "FlowReachPostIncomingInterfaceAcl",
            "FlowReachPostInInterface",
            "FlowReachPreInInterface",
            "FlowReachPreOutEdge",
            "FlowReachPreOutInterface",
            "FlowReachPostHostInFilter",
            "FlowReachPostOutboundCrossZoneAcl",
            "FlowReachPostOutgoingInterfaceAcl",
            "FlowReachPostOutInterface",
            "FlowUnknown",
            "IpAccessListDeny",
            "IpAccessListMatch" };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

   private static final Set<String> initNxtnetTrafficOutputSymbols() {
      Set<String> factSet = new HashSet<String>();
      String[] facts = new String[] {
            "Flow_dscp",
            "Flow_dstIp",
            "Flow_dstPort",
            "Flow_ecn",
            "Flow_icmpCode",
            "Flow_icmpType",
            "Flow_ipProtocol",
            "Flow_node",
            "Flow_srcIp",
            "Flow_srcPort",
            "Flow_tcpFlagsCWR",
            "Flow_tcpFlagsECE",
            "Flow_tcpFlagsURG",
            "Flow_tcpFlagsACK",
            "Flow_tcpFlagsPSH",
            "Flow_tcpFlagsRST",
            "Flow_tcpFlagsSYN",
            "Flow_tcpFlagsFIN",
            "Flow_tag",
            "FlowPathAcceptedEdge",
            "FlowPathDeniedInEdge",
            "FlowPathDeniedOutEdge",
            "FlowPathHistory",
            "FlowPathIntermediateEdge",
            "FlowPathNeighborUnreachableEdge",
            "FlowPathNoRouteEdge",
            "FlowPathNullRoutedEdge" };
      factSet.addAll(Arrays.asList(facts));
      return Collections.unmodifiableSet(factSet);
   }

}
