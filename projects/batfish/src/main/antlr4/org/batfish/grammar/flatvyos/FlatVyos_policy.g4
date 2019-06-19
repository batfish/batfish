parser grammar FlatVyos_policy;

import FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

plt_description
:
   description
;

plt_rule
:
   RULE num = DEC plt_rule_tail
;

plt_rule_tail
:
   plrt_action
   | plt_description
   | plrt_ge
   | plrt_le
   | plrt_prefix
;

plrt_action
:
   ACTION action = line_action
;

plrt_description
:
   description
;

plrt_ge
:
   GE num = DEC
;

plrt_le
:
   LE num = DEC
;

plrt_prefix
:
   PREFIX prefix = IP_PREFIX
;

pt_prefix_list
:
   PREFIX_LIST name = variable pt_prefix_list_tail
;

pt_prefix_list_tail
:
   plt_description
   | plt_rule
;

pt_route_map
:
   ROUTE_MAP name = variable pt_route_map_tail
;

pt_route_map_tail
:
   rmt_description
   | rmt_rule
;

rmmt_ip_address_prefix_list
:
   IP ADDRESS PREFIX_LIST name = variable
;

rmrt_action
:
   ACTION action = line_action
;

rmrt_description
:
   description
;

rmrt_match
:
   MATCH rmrt_match_tail
;

rmrt_match_tail
:
   rmmt_ip_address_prefix_list
;

rmt_description
:
   description
;

rmt_rule
:
   RULE num = DEC rmt_rule_tail
;

rmt_rule_tail
:
   rmrt_action
   | rmrt_description
   | rmrt_match
;

s_policy
:
   POLICY s_policy_tail
;

s_policy_tail
:
   pt_prefix_list
   | pt_route_map
;