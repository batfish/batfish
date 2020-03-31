parser grammar CiscoXr_routemap;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
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

as_path_set_elem
:
   IOS_REGEX AS_PATH_SET_REGEX
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

as_range_expr
:
   SINGLE_QUOTE
   (
      subranges += rp_subrange
   )+ SINGLE_QUOTE EXACT?
;

boolean_and_rp_stanza
:
   boolean_not_rp_stanza
   | boolean_and_rp_stanza AND boolean_not_rp_stanza
;

boolean_apply_rp_stanza
:
   APPLY name = variable
   (
      PAREN_LEFT varlist = route_policy_params_list PAREN_RIGHT
   )?
;

boolean_as_path_in_rp_stanza
:
   AS_PATH IN expr = as_path_set_expr
;

boolean_as_path_is_local_rp_stanza
:
   AS_PATH IS_LOCAL
;

boolean_as_path_neighbor_is_rp_stanza
:
   AS_PATH NEIGHBOR_IS as_range_expr
;

boolean_as_path_originates_from_rp_stanza
:
   AS_PATH ORIGINATES_FROM as_range_expr
;

boolean_as_path_passes_through_rp_stanza
:
   AS_PATH PASSES_THROUGH as_range_expr
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

boolean_local_preference_rp_stanza
:
   LOCAL_PREFERENCE int_comp rhs = int_expr
;

boolean_med_rp_stanza
:
   MED int_comp rhs = int_expr
;

boolean_next_hop_in_rp_stanza
:
   NEXT_HOP IN rp_prefix_set
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

boolean_route_type_is_rp_stanza
:
   ROUTE_TYPE IS type = rp_route_type
;

boolean_rp_stanza
:
   boolean_and_rp_stanza
   | boolean_rp_stanza OR boolean_and_rp_stanza
;

boolean_simple_rp_stanza
:
   PAREN_LEFT boolean_rp_stanza PAREN_RIGHT
   | boolean_apply_rp_stanza
   | boolean_as_path_in_rp_stanza
   | boolean_as_path_is_local_rp_stanza
   | boolean_as_path_neighbor_is_rp_stanza
   | boolean_as_path_originates_from_rp_stanza
   | boolean_as_path_passes_through_rp_stanza
   | boolean_community_matches_any_rp_stanza
   | boolean_community_matches_every_rp_stanza
   | boolean_destination_rp_stanza
   | boolean_local_preference_rp_stanza
   | boolean_med_rp_stanza
   | boolean_next_hop_in_rp_stanza
   | boolean_rib_has_route_rp_stanza
   | boolean_route_type_is_rp_stanza
   | boolean_tag_is_rp_stanza
;

boolean_tag_is_rp_stanza
:
   TAG int_comp int_expr
;

delete_community_rp_stanza
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
      | UNSUPPRESS_ROUTE
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

isis_level_expr
:
   isis_level
   | RP_VARIABLE
;

origin_expr
:
   origin_expr_literal
   | RP_VARIABLE
;

origin_expr_literal
:
   (
      EGP bgp_asn
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

route_policy_stanza
:
   ROUTE_POLICY name = variable
   (
      PAREN_LEFT varlist = route_policy_params_list PAREN_RIGHT
   )? NEWLINE
   (
         stanzas += rp_stanza
   )*
   END_POLICY NEWLINE
;

route_policy_params_list
:
   params_list += variable
   (
      COMMA params_list += variable
   )*
;

rp_community_set
:
   name = variable
   | PAREN_LEFT elems += community_set_elem
   (
      COMMA elems += community_set_elem
   )* PAREN_RIGHT
;

rp_extcommunity_set_rt
:
   name = variable
   | PAREN_LEFT elems += extcommunity_set_rt_elem
   (
      COMMA elems += extcommunity_set_rt_elem
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

rp_route_type
:
   LOCAL
   | INTERAREA
   | INTERNAL
   | LEVEL_1
   | LEVEL_1_2
   | LEVEL_2
   | LOCAL
   | OSPF_EXTERNAL_TYPE_1
   | OSPF_EXTERNAL_TYPE_2
   | OSPF_INTER_AREA
   | OSPF_INTRA_AREA
   | OSPF_NSSA_TYPE_1
   | OSPF_NSSA_TYPE_2
   | RP_VARIABLE
   | TYPE_1
   | TYPE_2
;

rp_stanza
:
   apply_rp_stanza
   | delete_community_rp_stanza
   | disposition_rp_stanza
   |
   (
      hash_comment NEWLINE
   )
   | if_rp_stanza
   | set_rp_stanza
;

set_community_rp_stanza
:
   SET COMMUNITY rp_community_set ADDITIVE? NEWLINE
;

set_extcommunity_rp_stanza
:
  SET EXTCOMMUNITY
  (
    set_extcommunity_rt
    // TODO all the other extended community types. Until then, the ? prevents NPE on recovery.
  )?
;

set_extcommunity_rt
:
  RT rp_extcommunity_set_rt ADDITIVE? NEWLINE
;

set_isis_metric_rp_stanza
:
   SET ISIS_METRIC int_expr NEWLINE
;

set_level_rp_stanza
:
   SET LEVEL isis_level_expr NEWLINE
;

set_local_preference_rp_stanza
:
   SET LOCAL_PREFERENCE pref = int_expr NEWLINE
;

set_med_rp_stanza
:
   SET MED med = int_expr NEWLINE
;

set_metric_type_rp_stanza
:
   SET METRIC_TYPE type = rp_metric_type NEWLINE
;

set_next_hop_rp_stanza
:
   SET NEXT_HOP
   (
      DISCARD
      | IP_ADDRESS
      | IPV6_ADDRESS
      | PEER_ADDRESS
   ) DESTINATION_VRF? NEWLINE
;

set_next_hop_self_rp_stanza
:
   SET NEXT_HOP SELF NEWLINE
;

set_origin_rp_stanza
:
   SET ORIGIN origin_expr NEWLINE
;

set_path_selection_rp_stanza
:
   SET PATH_SELECTION null_rest_of_line
;

set_tag_rp_stanza
:
   SET TAG tag = int_expr NEWLINE
;

set_weight_rp_stanza
:
   SET WEIGHT weight = int_expr NEWLINE
;

set_rp_stanza
:
   prepend_as_path_rp_stanza
   | set_community_rp_stanza
   | set_extcommunity_rp_stanza
   | set_isis_metric_rp_stanza
   | set_level_rp_stanza
   | set_local_preference_rp_stanza
   | set_med_rp_stanza
   | set_metric_type_rp_stanza
   | set_next_hop_rp_stanza
   | set_next_hop_self_rp_stanza
   | set_origin_rp_stanza
   | set_path_selection_rp_stanza
   | set_tag_rp_stanza
   | set_weight_rp_stanza
;
