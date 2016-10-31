parser grammar Cisco_routemap;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

apply_rp_stanza
:
   APPLY name = variable
   (
      PAREN_LEFT varlist = route_policy_params_list PAREN_RIGHT
   )? NEWLINE
;

as_expr
:
   DEC
   | AUTO
   | RP_VARIABLE
;

as_path_set_expr
:
   inline = as_path_set_inline
   | rpvar = RP_VARIABLE
   | named = variable
;

as_path_set_inline
:
   PAREN_LEFT elems += as_path_set_elem
   (
      COMMA elems += as_path_set_elem
   )* PAREN_RIGHT
;

boolean_and_rp_stanza
:
   boolean_not_rp_stanza
   | boolean_and_rp_stanza AND boolean_not_rp_stanza
;

boolean_as_path_in_rp_stanza
:
   AS_PATH IN expr = as_path_set_expr
;

boolean_as_path_originates_from_rp_stanza
:
   AS_PATH ORIGINATES_FROM SINGLE_QUOTE
   (
      as_list += as_expr
   )+ SINGLE_QUOTE EXACT?
;

boolean_as_path_passes_through_rp_stanza
:
   AS_PATH PASSES_THROUGH SINGLE_QUOTE
   (
      as_list += as_expr
   )+ SINGLE_QUOTE EXACT?
;

boolean_community_matches_any_rp_stanza
:
   COMMUNITY MATCHES_ANY rp_community_set
;

boolean_community_matches_every_rp_stanza
:
   COMMUNITY MATCHES_EVERY rp_community_set
;

boolean_destination_rp_stanza
:
   DESTINATION IN rp_prefix_set
;

boolean_not_rp_stanza
:
   boolean_simple_rp_stanza
   | NOT boolean_simple_rp_stanza
;

boolean_rib_has_route_rp_stanza
:
   RIB_HAS_ROUTE IN rp_prefix_set
;

boolean_rp_stanza
:
   boolean_and_rp_stanza
   | boolean_rp_stanza OR boolean_and_rp_stanza
;

boolean_simple_rp_stanza
:
   PAREN_LEFT boolean_rp_stanza PAREN_RIGHT
   | boolean_as_path_in_rp_stanza
   | boolean_as_path_originates_from_rp_stanza
   | boolean_as_path_passes_through_rp_stanza
   | boolean_community_matches_any_rp_stanza
   | boolean_community_matches_every_rp_stanza
   | boolean_destination_rp_stanza
   | boolean_rib_has_route_rp_stanza
   | boolean_tag_is_rp_stanza
;

boolean_tag_is_rp_stanza
:
   TAG int_comp int_expr
;

delete_rp_stanza
:
   DELETE COMMUNITY
   (
      ALL
      | NOT? IN rp_community_set
   ) NEWLINE
;

disposition_rp_stanza
:
   (
      DONE
      | DROP
      | PASS
   ) NEWLINE
;

elseif_rp_stanza
:
   ELSEIF boolean_rp_stanza THEN NEWLINE rp_stanza*
;

else_rp_stanza
:
   ELSE NEWLINE rp_stanza*
;

if_rp_stanza
:
   IF boolean_rp_stanza THEN NEWLINE rp_stanza* elseif_rp_stanza*
   else_rp_stanza?
   (
      ENDIF
      | EXIT
   ) NEWLINE
;

int_comp
:
   EQ
   | IS
   | GE
   | LE
;

ip_policy_list_stanza
:
   IP POLICY_LIST name = variable access_list_action NEWLINE match_rm_stanza*
;

match_as_path_access_list_rm_stanza
:
   MATCH AS_PATH
   (
      name_list += variable
   )+ NEWLINE
;

match_as_rm_stanza
:
   MATCH AS num = DEC NEWLINE
;

match_community_list_rm_stanza
:
   MATCH COMMUNITY
   (
      name_list += variable
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
   MATCH IP ADDRESS IP? PREFIX_LIST
   (
      name_list += variable
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

match_policy_list_rm_stanza
:
   MATCH POLICY_LIST
   (
      name_list += variable
   )+ NEWLINE
;

match_rm_stanza
:
   match_as_path_access_list_rm_stanza
   | match_as_rm_stanza
   | match_community_list_rm_stanza
   | match_extcommunity_rm_stanza
   | match_interface_rm_stanza
   | match_ip_access_list_rm_stanza
   | match_ip_prefix_list_rm_stanza
   | match_ipv6_rm_stanza
   | match_length_rm_stanza
   | match_policy_list_rm_stanza
   | match_tag_rm_stanza
;

match_tag_rm_stanza
:
   MATCH TAG
   (
      tag_list += DEC
   )+ NEWLINE
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
      | CONTINUE
   ) ~NEWLINE* NEWLINE
;

null_rp_stanza
:
   POUND ~NEWLINE* NEWLINE
;

origin_expr
:
   origin_expr_literal
   | RP_VARIABLE
;

origin_expr_literal
:
   (
      EGP as = DEC
   )
   | IGP
   | INCOMPLETE
;

prepend_as_path_rp_stanza
:
   PREPEND AS_PATH as = as_expr
   (
      number = int_expr
   )? NEWLINE
;

rm_stanza
:
   match_rm_stanza
   | null_rm_stanza
   | set_rm_stanza
;

route_map_stanza
:
   ROUTE_MAP name = variable rmt = access_list_action num = DEC NEWLINE
   rm_stanza*
;

route_policy_stanza
:
   ROUTE_POLICY name = variable
   (
      PAREN_LEFT varlist = route_policy_params_list PAREN_RIGHT
   )? NEWLINE route_policy_tail
;

route_policy_params_list
:
   params_list += variable
   (
      COMMA params_list += variable
   )*
;

route_policy_tail
:
   (
      stanzas += rp_stanza
   )* END_POLICY NEWLINE
;

rp_community_set
:
   name = variable
   | PAREN_LEFT elems += rp_community_set_elem
   (
      COMMA elems += rp_community_set_elem
   )* PAREN_RIGHT
;

rp_isis_metric_type
:
   EXTERNAL
   | INTERNAL
   | RIB_METRIC_AS_EXTERNAL
   | RIB_METRIC_AS_INTERNAL
;

rp_metric_type
:
   rp_isis_metric_type
   | rp_ospf_metric_type
   | RP_VARIABLE
;

rp_ospf_metric_type
:
   TYPE_1
   | TYPE_2
;

rp_prefix_set
:
   name = variable
   | PAREN_LEFT elems += prefix_set_elem
   (
      COMMA elems += prefix_set_elem
   )* PAREN_RIGHT
;

rp_stanza
:
   apply_rp_stanza
   | delete_rp_stanza
   | disposition_rp_stanza
   | if_rp_stanza
   | null_rp_stanza
   | set_rp_stanza
;

set_as_path_prepend_rm_stanza
:
   SET AS_PATH PREPEND LAST_AS?
   (
      as_list += as_expr
   )+ NEWLINE
;

set_as_path_tag_rm_stanza
:
   SET AS_PATH TAG NEWLINE
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
      communities += community
   )+ ADDITIVE NEWLINE
;

set_community_list_additive_rm_stanza
:
   SET COMMUNITY COMMUNITY_LIST
   (
      comm_lists += variable
   )+ ADDITIVE NEWLINE
;

set_community_list_rm_stanza
:
   SET COMMUNITY COMMUNITY_LIST
   (
      comm_lists += variable
   )+ NEWLINE
;

set_community_none_rm_stanza
:
   SET COMMUNITY NONE NEWLINE
;

set_community_rm_stanza
:
   SET COMMUNITY
   (
      communities += community
   )+ NEWLINE
;

set_community_rp_stanza
:
   SET COMMUNITY rp_community_set ADDITIVE? NEWLINE
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
   SET LOCAL_PREFERENCE pref = int_expr NEWLINE
;

set_local_preference_rp_stanza
:
   SET LOCAL_PREFERENCE pref = int_expr NEWLINE
;

set_med_rp_stanza
:
   SET MED med = int_expr NEWLINE
;

set_metric_rm_stanza
:
   SET METRIC metric = int_expr NEWLINE
;

set_metric_type_rm_stanza
:
   SET METRIC_TYPE type = variable NEWLINE
;

set_metric_type_rp_stanza
:
   SET METRIC_TYPE type = rp_metric_type NEWLINE
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
   SET IP? NEXT_HOP
   (
      nexthop_list += IP_ADDRESS
   )+ NEWLINE
;

set_next_hop_rp_stanza
:
   SET NEXT_HOP
   (
      IP_ADDRESS
      | IPV6_ADDRESS
      | PEER_ADDRESS
      | SELF
   ) DESTINATION_VRF? NEWLINE
;

set_origin_rm_stanza
:
   SET ORIGIN origin_expr_literal NEWLINE
;

set_origin_rp_stanza
:
   SET ORIGIN origin_expr NEWLINE
;

set_tag_rm_stanza
:
   SET TAG tag = DEC NEWLINE
;

set_tag_rp_stanza
:
   SET TAG tag = int_expr NEWLINE
;

set_weight_rm_stanza
:
   SET WEIGHT weight = DEC NEWLINE
;

set_weight_rp_stanza
:
   SET WEIGHT weight = int_expr NEWLINE
;

set_rm_stanza
:
   set_as_path_prepend_rm_stanza
   | set_as_path_tag_rm_stanza
   | set_comm_list_delete_rm_stanza
   | set_community_rm_stanza
   | set_community_additive_rm_stanza
   | set_community_list_additive_rm_stanza
   | set_community_list_rm_stanza
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

set_rp_stanza
:
   prepend_as_path_rp_stanza
   | set_community_rp_stanza
   | set_local_preference_rp_stanza
   | set_med_rp_stanza
   | set_metric_type_rp_stanza
   | set_next_hop_rp_stanza
   | set_origin_rp_stanza
   | set_tag_rp_stanza
   | set_weight_rp_stanza
;
