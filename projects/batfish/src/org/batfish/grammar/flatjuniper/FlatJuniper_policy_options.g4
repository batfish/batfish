parser grammar FlatJuniper_policy_options;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

base_community_regex
:
   ~( COLON | NEWLINE )+ COLON ~( COLON | NEWLINE )+
;

colort_apply_groups
:
   s_apply_groups
;

colort_color
:
   color = DEC
;

community_regex
:
   (
      base_community_regex PIPE
   )* base_community_regex
;

ct_members
:
   MEMBERS
   (
      extended_community
      | named_community
      // community_regex intentionally on bottom

      | community_regex
   )
;

fromt_area
:
   AREA area = IP_ADDRESS
;

fromt_as_path
:
   AS_PATH name = variable
;

fromt_color
:
   COLOR color = DEC
;

fromt_community
:
   COMMUNITY name = variable
;

fromt_family
:
   FAMILY
   (
      INET
      | INET6
   )
;

fromt_interface
:
   INTERFACE id = interface_id
;

fromt_neighbor
:
   NEIGHBOR
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   )
;

fromt_null
:
   PREFIX_LIST_FILTER s_null_filler
;

fromt_policy
:
   POLICY name = variable
;

fromt_prefix_list
:
   PREFIX_LIST name = variable
;

fromt_protocol
:
   PROTOCOL protocol = routing_protocol
;

fromt_route_filter
:
   ROUTE_FILTER
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) fromt_route_filter_tail then = fromt_route_filter_then?
;

fromt_route_filter_tail
:
   rft_exact
   | rft_orlonger
   | rft_prefix_length_range
   | rft_through
   | rft_upto
;

fromt_route_filter_then
:
   tt_then_tail
;

fromt_route_type
:
   ROUTE_TYPE EXTERNAL
;

fromt_source_address_filter
:
// reference to router filter tail is intentional
   SOURCE_ADDRESS_FILTER
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) fromt_route_filter_tail
;

fromt_tag
:
   TAG DEC
;

metric_expression
:
   MULTIPLIER multiplier = DEC
   (
      OFFSET offset = DEC
   )?
;

named_community
:
   NO_ADVERTISE
;

plt_apply_path
:
   APPLY_PATH path = DOUBLE_QUOTED_STRING
;

plt_network
:
   network = IP_PREFIX
;

plt_network6
:
   network = IPV6_PREFIX
;

pot_apply_groups
:
   s_apply_groups
;

pot_as_path
:
   AS_PATH name = variable pot_as_path_tail
;

pot_as_path_tail
:
   regex = AS_PATH_REGEX
;

pot_community
:
   COMMUNITY name = variable pot_community_tail
;

pot_community_tail
:
   ct_members
;

pot_policy_statement
:
   POLICY_STATEMENT
   (
      WILDCARD
      | name = variable
   ) pot_policy_statement_tail
;

pot_policy_statement_tail
:
   pst_term
   | pst_term_tail
;

pot_prefix_list
:
   PREFIX_LIST name = variable pot_prefix_list_tail
;

pot_prefix_list_tail
:
// intentional blank

   | plt_apply_path
   | plt_network
   | plt_network6
;

pst_term
:
   TERM
   (
      WILDCARD
      | name = variable
   ) pst_term_tail
;

pst_term_tail
:
   tt_apply_groups
   | tt_from
   | tt_then
;

rft_exact
:
   EXACT
;

rft_orlonger
:
   ORLONGER
;

rft_prefix_length_range
:
   PREFIX_LENGTH_RANGE FORWARD_SLASH low = DEC DASH FORWARD_SLASH high = DEC
;

rft_through
:
   THROUGH
   (
      IP_PREFIX
      | IPV6_PREFIX
   )
;

rft_upto
:
   UPTO FORWARD_SLASH high = DEC
;

s_policy_options
:
   POLICY_OPTIONS s_policy_options_tail
;

s_policy_options_tail
:
   pot_apply_groups
   | pot_as_path
   | pot_community
   | pot_policy_statement
   | pot_prefix_list
;

tht_accept
:
   ACCEPT
;

tht_as_path_expand
:
   AS_PATH_EXPAND LAST_AS COUNT DEC
;

tht_as_path_prepend
:
   AS_PATH_PREPEND
   (
      DEC
      | DOUBLE_QUOTED_STRING
   )
;

tht_color
:
   COLOR tht_color_tail
;

tht_color_tail
:
   colort_apply_groups
   | colort_color
;

tht_community_add
:
   COMMUNITY ADD name = variable
;

tht_community_delete
:
   COMMUNITY DELETE name = variable
;

tht_community_set
:
   COMMUNITY SET name = variable
;

tht_cos_next_hop_map
:
   COS_NEXT_HOP_MAP name = variable
;

tht_default_action_accept
:
   DEFAULT_ACTION ACCEPT
;

tht_default_action_reject
:
   DEFAULT_ACTION REJECT
;

tht_external
:
   EXTERNAL TYPE DEC
;

tht_local_preference
:
   LOCAL_PREFERENCE localpref = DEC
;

tht_metric
:
   METRIC metric = DEC
;

tht_metric2
:
   METRIC2 metric2 = DEC
;

tht_metric_expression
:
   METRIC EXPRESSION metric_expression
;

tht_metric2_expression
:
   METRIC2 EXPRESSION metric_expression
;

tht_next_policy
:
   NEXT POLICY
;

tht_next_term
:
   NEXT TERM
;

tht_next_hop
:
   NEXT_HOP
   (
      IP_ADDRESS
      | IPV6_ADDRESS
      | PEER_ADDRESS
      | SELF
   )
;

tht_null
:
   LOAD_BALANCE s_null_filler
;

tht_origin
:
   ORIGIN IGP
;

tht_reject
:
   REJECT
;

tht_tag
:
   TAG DEC
;

tt_apply_groups
:
   s_apply_groups
;

tt_from
:
   FROM tt_from_tail
;

tt_from_tail
:
   fromt_area
   | fromt_as_path
   | fromt_color
   | fromt_community
   | fromt_family
   | fromt_interface
   | fromt_neighbor
   | fromt_null
   | fromt_policy
   | fromt_prefix_list
   | fromt_protocol
   | fromt_route_filter
   | fromt_route_type
   | fromt_source_address_filter
   | fromt_tag
;

tt_then
:
   THEN tt_then_tail
;

tt_then_tail
:
   tht_accept
   | tht_as_path_expand
   | tht_as_path_prepend
   | tht_color
   | tht_community_add
   | tht_community_delete
   | tht_community_set
   | tht_cos_next_hop_map
   | tht_default_action_accept
   | tht_default_action_reject
   | tht_external
   | tht_local_preference
   | tht_metric
   | tht_next_hop
   | tht_next_policy
   | tht_next_term
   | tht_null
   | tht_origin
   | tht_reject
   | tht_tag
;
