parser grammar FlatJuniperGrammar_policy_options;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

colort_apply_groups
:
   s_apply_groups
;

colort_color
:
   color = DEC
;

ct_members
:
   MEMBERS
   (
      COMMUNITY_REGEX
      | NO_ADVERTISE
   )
;

fromt_as_path
:
   AS_PATH variable
;

fromt_color
:
   COLOR DEC
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
   INTERFACE name = variable
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
   PROTOCOL protocol
;

fromt_route_filter
:
   fromt_route_filter_header fromt_route_filter_tail
;

fromt_route_filter_header
:
   ROUTE_FILTER
   (
      IP_ADDRESS_WITH_MASK
      | IPV6_ADDRESS_WITH_MASK
   )
;

fromt_route_filter_tail
:
   (
      rft_exact
      | rft_orlonger
      | rft_prefix_length_range
      | rft_upto
   ) ACCEPT?
;

fromt_source_address_filter
:
// reference to router filter tail is intentional
   fromt_source_address_filter_header fromt_route_filter_tail
;

fromt_source_address_filter_header
:
   SOURCE_ADDRESS_FILTER
   (
      IP_ADDRESS_WITH_MASK
      | IPV6_ADDRESS_WITH_MASK
   )
;

fromt_tag
:
   TAG DEC
;

met_metric
:
   METRIC MULTIPLIER multiplier = DEC
   (
      OFFSET offset = DEC
   )?
;

met_metric2
:
   METRIC2 MULTIPLIER multiplier = DEC
   (
      OFFSET offset = DEC
   )?
;

metrict_constant
:
   DEC
;

metrict_expression
:
   EXPRESSION metrict_expression_tail
;

metrict_expression_tail
:
   met_metric
   | met_metric2
;

plt_apply_path
:
   APPLY_PATH path = DOUBLE_QUOTED_STRING
;

plt_network
:
   network = IP_ADDRESS_WITH_MASK
;

plt_network6
:
   network = IPV6_ADDRESS_WITH_MASK
;

pot_as_path
:
   pot_as_path_header pot_as_path_tail
;

pot_as_path_header
:
   AS_PATH variable
;

pot_as_path_tail
:
   regex = DOUBLE_QUOTED_STRING
;

pot_community
:
   pot_community_header pot_community_tail
;

pot_community_header
:
   COMMUNITY name = variable
;

pot_community_tail
:
   ct_members
;

pot_policy_statement
:
   pot_policy_statement_header pot_policy_statement_tail
;

pot_policy_statement_header
:
   POLICY_STATEMENT
   (
      WILDCARD
      | name = variable
   )
;

pot_policy_statement_tail
:
   pst_term
   | pst_term_tail
;

pot_prefix_list
:
   pot_prefix_list_header pot_prefix_list_tail
;

pot_prefix_list_header
:
   PREFIX_LIST name = variable
;

pot_prefix_list_tail
:
// intentional blank

   | plt_apply_path
   | plt_network
   | plt_network6
;

prefix_length_range
:
   FORWARD_SLASH low = DEC DASH FORWARD_SLASH high = DEC
;

pst_term
:
   pst_term_header pst_term_tail
;

pst_term_header
:
   TERM
   (
      WILDCARD
      | name = variable
   )
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
   PREFIX_LENGTH_RANGE prefix_length_range
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
   pot_as_path
   | pot_community
   | pot_policy_statement
   | pot_prefix_list
;

tht_accept
:
   ACCEPT
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
   COMMUNITY ADD variable
;

tht_community_delete
:
   COMMUNITY DELETE variable
;

tht_community_set
:
   COMMUNITY SET variable
;

tht_cos_next_hop_map
:
   COS_NEXT_HOP_MAP variable
;

tht_default_action_accept
:
   DEFAULT_ACTION ACCEPT
;

tht_default_action_reject
:
   DEFAULT_ACTION REJECT
;

tht_local_preference
:
   LOCAL_PREFERENCE localpref = DEC
;

tht_metric
:
   METRIC tht_metric_tail
;

tht_metric_tail
:
   metrict_constant
   | metrict_expression
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
   fromt_as_path
   | fromt_color
   | fromt_community
   | fromt_family
   | fromt_interface
   | fromt_policy
   | fromt_prefix_list
   | fromt_protocol
   | fromt_route_filter
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
   | tht_as_path_prepend
   | tht_color
   | tht_community_add
   | tht_community_delete
   | tht_community_set
   | tht_cos_next_hop_map
   | tht_default_action_accept
   | tht_default_action_reject
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
