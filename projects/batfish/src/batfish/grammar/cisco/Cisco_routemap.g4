parser grammar Cisco_routemap;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

match_as_path_access_list_rm_stanza
:
   MATCH AS_PATH
   (
      name_list += variable
   )+ NEWLINE
;

match_community_list_rm_stanza
:
   MATCH COMMUNITY
   (
      name_list +=
      (
         VARIABLE
         | DEC
      )
   )+ NEWLINE
;

match_extcommunity_rm_stanza
:
   MATCH EXTCOMMUNITY
   (
      name_list +=
      (
         VARIABLE
         | DEC
      )
   )+ NEWLINE
;

match_interface_rm_stanza
:
   MATCH INTERFACE interface_name+ NEWLINE
;

match_ip_access_list_rm_stanza
:
   MATCH IP ADDRESS
   (
      name_list +=
      (
         VARIABLE
         | DEC
      )
   )+ NEWLINE
;

match_ip_prefix_list_rm_stanza
:
   MATCH IP ADDRESS PREFIX_LIST
   (
      name_list +=
      (
         VARIABLE
         | DEC
      )
   )+ NEWLINE
;

match_ipv6_rm_stanza
:
   MATCH IPV6 ~NEWLINE* NEWLINE
;

match_length_rm_stanza
:
   MATCH LENGTH ~NEWLINE* NEWLINE
;

match_rm_stanza
:
   match_as_path_access_list_rm_stanza
   | match_community_list_rm_stanza
   | match_extcommunity_rm_stanza
   | match_interface_rm_stanza
   | match_ip_access_list_rm_stanza
   | match_ip_prefix_list_rm_stanza
   | match_ipv6_rm_stanza
   | match_length_rm_stanza
   | match_tag_rm_stanza
;

match_tag_rm_stanza
:
   MATCH TAG
   (
      tag_list += DEC
   )+ NEWLINE
;

null_rm_stanza
:
   NO?
   (
      DESCRIPTION
   ) ~NEWLINE* NEWLINE
;

rm_stanza
:
   match_rm_stanza
   | null_rm_stanza
   | set_rm_stanza
;

route_map_named_stanza
locals [boolean again]
:
   ROUTE_MAP name = ~NEWLINE route_map_tail
   {
		$again = _input.LT(1).getType() == ROUTE_MAP &&
		_input.LT(2).getType() != NEWLINE &&
		_input.LT(2).getText().equals($name.text);
	}

   (
      {$again}?

      route_map_named_stanza
      |
      {!$again}?

   )
;

route_map_stanza
:
   named = route_map_named_stanza
;

route_map_tail
:
   rmt = access_list_action num = DEC NEWLINE route_map_tail_tail
;

route_map_tail_tail
:
   (
      rms_list += rm_stanza
   )*
;

set_as_path_prepend_rm_stanza
:
   SET AS_PATH PREPEND LAST_AS?
   (
      as_list += DEC
   )+ NEWLINE
;

set_comm_list_delete_rm_stanza
:
   SET COMM_LIST
   (
      name = DEC
      | name = VARIABLE
   ) DELETE NEWLINE
;

set_community_additive_rm_stanza
:
   SET COMMUNITY
   (
      comm_list += community
   )+ ADDITIVE NEWLINE
;

set_community_none_rm_stanza
:
   SET COMMUNITY NONE NEWLINE
;

set_community_rm_stanza
:
   SET COMMUNITY
   (
      comm_list += community
   )+ NEWLINE
;

set_extcomm_list_rm_stanza
:
   SET EXTCOMM_LIST
   (
      comm_list += community
   )+ DELETE NEWLINE
;

set_extcommunity_rm_stanza
:
   SET EXTCOMMUNITY
   (
      COST
      | RT
   ) extended_community NEWLINE
;

set_interface_rm_stanza
:
   SET INTERFACE ~NEWLINE* NEWLINE
;

set_ip_df_rm_stanza
:
   SET IP DF ~NEWLINE* NEWLINE
;

set_ipv6_rm_stanza
:
   SET IPV6 ~NEWLINE* NEWLINE
;

set_local_preference_rm_stanza
:
   SET LOCAL_PREFERENCE pref = DEC NEWLINE
;

set_metric_rm_stanza
:
   SET METRIC metric = DEC NEWLINE
;

set_metric_type_rm_stanza
:
   SET METRIC_TYPE type = VARIABLE NEWLINE
;

set_mpls_label_rm_stanza
:
   SET MPLS_LABEL NEWLINE
;

set_next_hop_peer_address_stanza
:
   SET IP NEXT_HOP PEER_ADDRESS NEWLINE
;

set_next_hop_rm_stanza
:
   SET IP NEXT_HOP
   (
      nexthop_list += IP_ADDRESS
   )+ NEWLINE
;

set_origin_rm_stanza
:
   SET ORIGIN
   (
      (
         EGP as = DEC
      )
      | IGP
      | INCOMPLETE
   ) NEWLINE
;

set_tag_rm_stanza
:
   SET TAG tag = DEC NEWLINE
;

set_weight_rm_stanza
:
   SET WEIGHT weight = DEC NEWLINE
;

set_rm_stanza
:
   set_as_path_prepend_rm_stanza
   | set_comm_list_delete_rm_stanza
   | set_community_rm_stanza
   | set_community_additive_rm_stanza
   | set_community_none_rm_stanza
   | set_extcomm_list_rm_stanza
   | set_extcommunity_rm_stanza
   | set_interface_rm_stanza
   | set_ip_df_rm_stanza
   | set_ipv6_rm_stanza
   | set_local_preference_rm_stanza
   | set_metric_rm_stanza
   | set_metric_type_rm_stanza
   | set_mpls_label_rm_stanza
   | set_next_hop_peer_address_stanza
   | set_next_hop_rm_stanza
   | set_origin_rm_stanza
   | set_tag_rm_stanza
   | set_weight_rm_stanza
;

