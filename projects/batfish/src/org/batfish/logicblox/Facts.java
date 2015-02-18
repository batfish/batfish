package org.batfish.logicblox;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Facts {

   public static final Map<String, String> CONTROL_PLANE_FACT_COLUMN_HEADERS = getControlPlaneFactColumnHeaders();
   public static final Map<String, String> TRAFFIC_FACT_COLUMN_HEADERS = getTrafficFactColumnHeaders();

   private static Map<String, String> getControlPlaneFactColumnHeaders() {
      Map<String, String> map = new TreeMap<String, String>();
      map.put("SetFakeInterface", "NODE|INTERFACE");
      map.put("SetFlowSinkInterface", "NODE|INTERFACE");
      map.put("GuessTopology", "DUMMY");
      map.put("SamePhysicalSegment", "NODE1|INTERFACE1|NODE2|INTERFACE2");
      map.put("SetSwitchportAccess", "SWITCH|INTERFACE|VLAN");
      map.put("SetSwitchportTrunkAllows", "SWITCH|INTERFACE|VLANSTART|VLANEND");
      map.put("SetSwitchportTrunkEncapsulation",
            "SWITCH|INTERFACE|ENCAPSULATION");
      map.put("SetSwitchportTrunkNative", "SWITCH|INTERFACE|VLAN");
      map.put("SetVlanInterface", "NODE|INTERFACE|VLAN");
      map.put("SetInterfaceFilterIn", "NODE|INTERFACE|FILTER");
      map.put("SetInterfaceFilterOut", "NODE|INTERFACE|FILTER");
      map.put("SetInterfaceRoutingPolicy", "NODE|INTERFACE|POLICY");
      map.put("SetNetwork", "STARTIP|START|END|PREFIXLENGTH");
      map.put("SetIpAccessListLine_deny", "LIST|LINE");
      map.put("SetIpAccessListLine_dstIpRange", "LIST|LINE|DSTIPSTART|DSTIPEND");
      map.put("SetIpAccessListLine_dstPortRange",
            "LIST|LINE|DSTPORTSTART|DSTPORTEND");
      map.put("SetIpAccessListLine_permit", "LIST|LINE");
      map.put("SetIpAccessListLine_protocol", "LIST|LINE|PROTOCOL");
      map.put("SetIpAccessListLine_srcIpRange", "LIST|LINE|SRCIPSTART|SRCIPEND");
      map.put("SetIpAccessListLine_srcPortRange",
            "LIST|LINE|SRCPORTSTART|SRCPORTEND");
      map.put("SetActiveInt", "NODE|INTERFACE");
      map.put("SetIpInt", "NODE|INTERFACE|IP|PREFIXLENGTH");
      map.put("SetLinkLoadLimitIn", "NODE|INTERFACE|LIMIT");
      map.put("SetLinkLoadLimitOut", "NODE|INTERFACE|LIMIT");
      map.put("SetGeneratedRoute_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH|ADMIN");
      map.put("SetGeneratedRouteDiscard_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH");
      map.put("SetGeneratedRouteMetric_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH|METRIC");
      map.put("SetGeneratedRoutePolicy_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH|MAP");
      map.put("SetStaticRoute_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH|NEXTHOPIP|ADMIN|TAG");
      map.put("SetStaticIntRoute_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH|NEXTHOPIP|NEXTHOPINT|ADMIN|TAG");
      map.put("SetOspfGeneratedRoute_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH");
      map.put("SetOspfGeneratedRoutePolicy_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH|MAP");
      map.put("SetOspfInterface", "NODE|INTERFACE|AREA");
      map.put("SetOspfInterfaceCost", "NODE|INTERFACE|COST");
      map.put("SetOspfOutboundPolicyMap", "NODE|MAP");
      map.put("SetOspfRouterId", "NODE|IP");
      map.put("SetCommunityListLine", "LIST|LINE|COMMUNITY");
      map.put("SetCommunityListLinePermit", "LIST|LINE");
      map.put("SetRouteFilterLine",
            "LIST|LINE|NETWORKSTART|NETWORKEND|MINPREFIX|MAXPREFIX");
      map.put("SetRouteFilterPermitLine", "LIST|LINE");
      map.put("SetPolicyMapClauseAddCommunity", "MAP|CLAUSE|COMMUNITY");
      map.put("SetPolicyMapClauseDeleteCommunity", "MAP|CLAUSE|LIST");
      map.put("SetPolicyMapClauseDeny", "MAP|CLAUSE");
      map.put("SetPolicyMapClauseMatchAcl", "MAP|CLAUSE|ACL");
      map.put("SetPolicyMapClauseMatchAsPath", "MAP|CLAUSE|ASPATH");
      map.put("SetPolicyMapClauseMatchCommunityList", "MAP|CLAUSE|LIST");
      map.put("SetPolicyMapClauseMatchNeighbor", "MAP|CLAUSE|NEIGHBORIP");
      map.put("SetPolicyMapClauseMatchPolicy", "MAP|CLAUSE|POLICY");
      map.put("SetPolicyMapClauseMatchProtocol", "MAP|CLAUSE|PROTOCOL");
      map.put("SetPolicyMapClauseMatchRouteFilter", "MAP|CLAUSE|LIST");
      map.put("SetPolicyMapClauseMatchTag", "MAP|CLAUSE|TAG");
      map.put("SetPolicyMapClausePermit", "MAP|CLAUSE");
      map.put("SetPolicyMapClauseSetCommunity", "MAP|CLAUSE|COMMUNITY");
      map.put("SetPolicyMapClauseSetLocalPreference", "MAP|CLAUSE|LOCALPREF");
      map.put("SetPolicyMapClauseSetMetric", "MAP|CLAUSE|METRIC");
      map.put("SetPolicyMapClauseSetNextHopIp", "MAP|CLAUSE|NEXTHOPIP");
      map.put("SetPolicyMapClauseSetOriginType", "MAP|CLAUSE|ORIGINTYPE");
      map.put("SetPolicyMapOspfExternalRouteType", "MAP|PROTOCOL");
      map.put("SetBgpDefaultLocalPref", "NODE|NEIGHBORIP|LOCALPREF");
      map.put("SetBgpExportPolicy", "NODE|NEIGHBORIP|MAP");
      map.put("SetBgpGeneratedRoute_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH");
      map.put("SetBgpGeneratedRoutePolicy_flat",
            "NODE|NETWORKSTART|NETWORKEND|PREFIXLENGTH|MAP");
      map.put("SetBgpImportPolicy", "NODE|NEIGHBORIP|MAP");
      map.put("SetBgpNeighborDefaultMetric", "NODE|NEIGHBORIP|METRIC");
      map.put("SetBgpNeighborGeneratedRoute_flat",
            "NODE|NEIGHBORIP|NETWORKSTART|NETWORKEND|PREFIXLENGTH");
      map.put("SetBgpNeighborGeneratedRoutePolicy_flat",
            "NODE|NEIGHBORIP|NETWORKSTART|NETWORKEND|PREFIXLENGTH|MAP");
      map.put("SetBgpNeighborIp", "NODE|NEIGHBORIP");
      map.put("SetBgpNeighborSendCommunity", "NODE|NEIGHBORIP");
      map.put("SetBgpOriginationPolicy", "NODE|NEIGHBORIP|MAP");
      map.put("SetLocalAs", "NODE|NEIGHBORIP|LOCALAS");
      map.put("SetRemoteAs", "NODE|NEIGHBORIP|REMOTEAS");
      map.put("SetRouteReflectorClient", "NODE|NEIGHBORIP|CLUSTERID");
      map.put("SetNodeVendor", "NODE|VENDOR");
      map.put("SetNodeRole", "NODE|ROLE");
      map.put("SetAsPathLineDeny", "ASPATH|LINE");
      map.put("SetAsPathLineMatchAs", "ASPATH|LINE|ASLOW|ASHIGH");
      map.put("SetAsPathLineMatchAsAtBeginning", "ASPATH|LINE|ASLOW|ASHIGH");
      map.put("SetAsPathLineMatchAsPair",
            "ASPATH|LINE|AS1LOW|AS1HIGH|AS2LOW|AS2HIGH");
      map.put("SetAsPathLineMatchAsPairAtBeginning",
            "ASPATH|LINE|AS1LOW|AS1HIGH|AS2LOW|AS2HIGH");
      map.put("SetAsPathLineMatchEmpty", "ASPATH|LINE");
      map.put("SetAsPathLinePermit", "ASPATH|LINE");
      return Collections.unmodifiableMap(map);
   }

   private static Map<String, String> getTrafficFactColumnHeaders() {
      Map<String, String> map = new TreeMap<String, String>();
      map.put("DuplicateRoleFlows", "DUMMY");
      map.put("SetFlowOriginate", "NODE|SRCIP|DSTIP|SRCPORT|DSTPORT|IPPROTOCOL");
      return Collections.unmodifiableMap(map);
   }

   private Facts() {
   }

}
