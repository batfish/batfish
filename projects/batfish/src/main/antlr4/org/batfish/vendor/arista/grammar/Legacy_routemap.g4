parser grammar Legacy_routemap;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

as_expr
:
   asn = bgp_asn
   | AUTO
;

continue_rm_stanza
:
   CONTINUE dec? NEWLINE
;

ip_policy_list_stanza
:
   IP POLICY_LIST name = variable access_list_action NEWLINE match_rm_stanza*
;

match_as_number_rm_stanza
:
   AS_NUMBER num = dec NEWLINE
;

match_as_path_access_list_rm_stanza
:
   AS_PATH
   (
      name_list += variable
   )+ NEWLINE
;

match_as_rm_stanza
:
   MATCH AS num = dec NEWLINE
;

match_community_list_rm_stanza
:
   COMMUNITY
   (
      name_list += variable
   )+ NEWLINE
;

match_extcommunity_rm_stanza
:
   EXTCOMMUNITY
   (
      name_list += variable
   )+ NEWLINE
;

match_interface_rm_stanza
:
   INTERFACE interface_name+ NEWLINE
;

match_ip_access_list_rm_stanza
:
   IP ADDRESS
   (
      name_list += variable_access_list
   )+ NEWLINE
;

match_ipv6_access_list_rm_stanza
:
   IPV6 ADDRESS
   (
      name_list += variable_access_list
   )+ NEWLINE
;

match_ip_multicast_rm_stanza
:
   IP MULTICAST null_rest_of_line
;

match_ip_next_hop_rm_stanza_null
:
   IP NEXT_HOP
   (
      NULL
   ) NEWLINE
;

match_ip_prefix_list_rm_stanza
:
   IP ADDRESS IP? PREFIX_LIST
   (
      name_list += variable
   )+ NEWLINE
;

match_ip_route_source_rm_stanza
:
   IP ROUTE_SOURCE src = dec NEWLINE
;

match_ipv6_prefix_list_rm_stanza
:
   IPV6 ADDRESS PREFIX_LIST
   (
      name_list += variable
   )+ NEWLINE
;

match_length_rm_stanza
:
   LENGTH null_rest_of_line
;

match_policy_list_rm_stanza
:
   POLICY_LIST
   (
      name_list += variable
   )+ NEWLINE
;

match_route_type_rm_stanza
:
   ROUTE_TYPE variable NEWLINE
;

match_source_protocol_rm_stanza
:
   SOURCE_PROTOCOL
   (
       BGP (bgpasn = bgp_asn)?
       | CONNECTED
       | ISIS
       | OSPF (area = dec)?
       | RIP
       | STATIC
   )+ NEWLINE
;

match_rm_stanza
:
   MATCH
   (
      match_as_number_rm_stanza
      | match_as_path_access_list_rm_stanza
      | match_as_rm_stanza
      | match_community_list_rm_stanza
      | match_extcommunity_rm_stanza
      | match_interface_rm_stanza
      | match_ip_access_list_rm_stanza
      | match_ip_multicast_rm_stanza
      | match_ip_next_hop_rm_stanza_null
      | match_ip_prefix_list_rm_stanza
      | match_ip_route_source_rm_stanza
      | match_ipv6_access_list_rm_stanza
      | match_ipv6_prefix_list_rm_stanza
      | match_length_rm_stanza
      | match_policy_list_rm_stanza
      | match_route_type_rm_stanza
      | match_source_protocol_rm_stanza
      | match_tag_rm_stanza
   )
;

match_tag_rm_stanza
:
   TAG (tag_list += uint32)+ NEWLINE
;

no_route_map_stanza
:
   NO ROUTE_MAP name = variable NEWLINE
;

null_rm_stanza
:
   NO?
   (
      DESCRIPTION
      | SUB_ROUTE_MAP
   ) null_rest_of_line
;

origin_expr_literal
:
   (
      EGP bgp_asn
   )
   | IGP
   | INCOMPLETE
;

rm_stanza
:
   continue_rm_stanza
   | match_rm_stanza
   | null_rm_stanza
   | rm_set
;

route_map_stanza
:
   // Both action and number are optional but number must come with action
   ROUTE_MAP name = variable (rmt = access_list_action (num = dec)?)? NEWLINE
   rm_stanza*
;


set_as_path_prepend_rm_stanza
:
   AS_PATH PREPEND LAST_AS?
   (
      as_list += as_expr
   )+ NEWLINE
;

set_as_path_tag_rm_stanza
:
   AS_PATH TAG NEWLINE
;

set_extcommunity_rm_stanza
:
   EXTCOMMUNITY
   (
      COST
      | RT
   ) extended_community NEWLINE
;

set_interface_rm_stanza
:
   INTERFACE null_rest_of_line
;

set_ip_default_nexthop_stanza
:
   IP DEFAULT NEXT_HOP nhip = IP_ADDRESS NEWLINE
;

set_ip_df_rm_stanza
:
   IP DF null_rest_of_line
;

set_ip_precedence_stanza
:
   IP PRECEDENCE
   (
      val = DIGIT
      | CRITICAL
      | FLASH
      | FLASH_OVERRIDE
      | IMMEDIATE
      | INTERNET
      | NETWORK
      | PRIORITY
      | ROUTE
   ) NEWLINE
;

set_ipv6_rm_stanza
:
   IPV6 null_rest_of_line
;

set_local_preference_rm_stanza
:
   LOCAL_PREFERENCE pref = int_expr NEWLINE
;

set_metric_rm_stanza
:
   METRIC metric = int_expr NEWLINE
;

set_metric_type_rm_stanza
:
   METRIC_TYPE type = variable NEWLINE
;

set_mpls_label_rm_stanza
:
   MPLS_LABEL NEWLINE
;

set_next_hop_peer_address_stanza
:
   IP NEXT_HOP PEER_ADDRESS NEWLINE
;

set_next_hop_rm_stanza
:
   IP? NEXT_HOP
   (
      nexthop_list += IP_ADDRESS
   )+ NEWLINE
;

set_nlri_rm_stanza_null
:
   NLRI
   (
      UNICAST
      | MULTICAST
   )+ NEWLINE
;

set_origin_rm_stanza
:
   ORIGIN origin_expr_literal NEWLINE
;

set_tag_rm_stanza
:
   TAG tag = uint32 NEWLINE
;

set_traffic_index_rm_stanza_null
:
   TRAFFIC_INDEX index = dec NEWLINE
;

set_weight_rm_stanza
:
   WEIGHT weight = dec NEWLINE
;

rm_set
:
  SET
  (
    // EOS up here
    rms_community
    | rms_distance
    // Legacy below
    | set_as_path_prepend_rm_stanza
    | set_as_path_tag_rm_stanza
    | set_extcommunity_rm_stanza
    | set_interface_rm_stanza
    | set_ip_default_nexthop_stanza
    | set_ip_df_rm_stanza
    | set_ip_precedence_stanza
    | set_ipv6_rm_stanza
    | set_local_preference_rm_stanza
    | set_metric_rm_stanza
    | set_metric_type_rm_stanza
    | set_mpls_label_rm_stanza
    | set_next_hop_peer_address_stanza
    | set_next_hop_rm_stanza
    | set_nlri_rm_stanza_null
    | set_origin_rm_stanza
    | set_tag_rm_stanza
    | set_traffic_index_rm_stanza_null
    | set_weight_rm_stanza
  )
;

rms_community
:
  COMMUNITY
  (
    rmsc_communities
    | rmsc_community_list
    | rmsc_none
  )
;

rmsc_communities
:
  communities += literal_standard_community+ (ADDITIVE | DELETE)? NEWLINE
;

rmsc_community_list
:
  COMMUNITY_LIST names += community_list_name+ (ADDITIVE | DELETE)? NEWLINE
;

community_list_name
:
  ~(ADDITIVE | DELETE | NEWLINE)
;

rmsc_none: NONE NEWLINE;

rms_distance
:
// 1-255
  DISTANCE distance = dec NEWLINE
;

variable_access_list
:
   ~( IP | IPV6 | NEWLINE | PREFIX_LIST )
;
