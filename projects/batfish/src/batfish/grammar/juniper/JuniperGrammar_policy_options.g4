parser grammar JuniperGrammar_policy_options;

import JuniperGrammarCommonParser;

options {
   tokenVocab = JuniperGrammarLexer;
}

policy_options_stanza
:
   POLICY_OPTIONS OPEN_BRACE po_stanza_list CLOSE_BRACE
;

po_stanza_list
:
   (
      po_stanza
      | inactive_po_stanza
   )+
;

inactive_po_stanza
:
   INACTIVE COLON po_stanza
;

po_stanza
:
   as_path_po_stanza
   | community_po_stanza
   | policy_statement_po_stanza
   | prefix_list_po_stanza
   | null_po_stanza
;

as_path_po_stanza
:
   AS_PATH
   (
      name = VARIABLE
   )
   (
      string_in_double_quotes
      | ARI_CHANGETHIS3
   ) SEMICOLON
;

community_po_stanza
:
   COMMUNITY name = VARIABLE MEMBERS community_literal_list SEMICOLON
;

policy_statement_po_stanza
:
   POLICY_STATEMENT name = VARIABLE OPEN_BRACE
   (
      (
         inactive_term_ps_po_stanza
         | term_ps_po_stanza
      )* anon_term_ps_po_stanza?
   ) CLOSE_BRACE
;

prefix_list_po_stanza
:
   PREFIX_LIST
   (
      name = VARIABLE
      | name = IP_ADDRESS_WITH_MASK
   )
   (
      (
         OPEN_BRACE
         (
            (
               (
                  ipmask = IP_ADDRESS_WITH_MASK
                  | ipmask = IPV6_ADDRESS_WITH_MASK
               ) SEMICOLON
            )+
            |
            (
               APPLY_PATH path_str = string_in_double_quotes SEMICOLON
            )
         ) CLOSE_BRACE
      )
      | SEMICOLON
   )
;

null_po_stanza
:
   removed_stanza
;

anon_term_ps_po_stanza
:
   subterm_ps_po_stanza
;

inactive_term_ps_po_stanza
:
   INACTIVE COLON term_ps_po_stanza
;

term_ps_po_stanza
:
   TERM // TODO [P3]: it seems bad that these have to be spelled out

   (
      name = ACCEPT
      | name = ALLOW
      | name = BGP
      | name = DIRECT
      | name = DISCARD
      | name = IMPORT
      | name = INPUT
      | name = NEXT
      | name = NO_EXPORT
      | name = REJECT
      | name = VARIABLE
      | name = DEC
   ) OPEN_BRACE subterm_ps_po_stanza CLOSE_BRACE
;

subterm_ps_po_stanza
:
   (
      (
         FROM
         (
            from_t_ps_stanza
            | inactive_from_t_ps_stanza
            |
            (
               OPEN_BRACE fl = from_t_ps_stanza_list CLOSE_BRACE
            )
         )
      )
      |
      (
         TO
         (
            inactive_to_t_ps_stanza
            |
            (
               OPEN_BRACE tol = to_t_ps_stanza_list CLOSE_BRACE
            )
            | to_t_ps_stanza
         )
      )
      |
      (
         THEN
         (
            th = inactive_then_t_ps_stanza
            |
            (
               OPEN_BRACE thl = then_t_ps_stanza_list CLOSE_BRACE
            )
            | th = then_t_ps_stanza
         )
      )
   )+
;

from_t_ps_stanza_list
:
   (
      from_t_ps_stanza
      | inactive_from_t_ps_stanza
   )+
;

inactive_from_t_ps_stanza
:
   INACTIVE COLON from_t_ps_stanza
;

from_t_ps_stanza
:
   as_path_from_t_ps_stanza
   | community_from_t_ps_stanza
   | family_from_t_ps_stanza
   | interface_from_t_ps_stanza
   | neighbor_from_t_ps_stanza
   | origin_from_t_ps_stanza
   | prefix_list_from_t_ps_stanza
   | prefix_list_filter_from_t_ps_stanza
   | protocol_from_t_ps_stanza
   | rib_from_t_ps_stanza
   | route_filter_from_t_ps_stanza
   | source_address_filter_from_t_ps_stanza
   | tag_from_t_ps_stanza
;

then_t_ps_stanza_list
:
   (
      inactive_then_t_ps_stanza
      | then_t_ps_stanza
   )+
;

inactive_then_t_ps_stanza
:
   INACTIVE COLON then_t_ps_stanza
;

then_t_ps_stanza
:
   accept_then_t_ps_stanza
   | as_path_prepend_then_t_ps_stanza
   | community_then_t_ps_stanza
   | install_next_hop_then_t_ps_stanza
   | local_preference_then_t_ps_stanza
   | metric_then_t_ps_stanza
   | next_hop_then_t_ps_stanza
   | next_policy_then_t_ps_stanza
   | next_term_then_t_ps_stanza
   | reject_then_t_ps_stanza
   | null_then_t_ps_stanza
;

to_t_ps_stanza_list
:
   (
      inactive_to_t_ps_stanza
      | to_t_ps_stanza
   )+
;

inactive_to_t_ps_stanza
:
   INACTIVE COLON to_t_ps_stanza
;

to_t_ps_stanza
:
   instance_to_t_ps_stanza
   | rib_to_t_ps_stanza
;

as_path_from_t_ps_stanza
:
   AS_PATH
   (
      (
         OPEN_BRACKET
         (
            name += VARIABLE
         )+ CLOSE_BRACKET
      )
      | name += VARIABLE
   ) SEMICOLON
;

community_from_t_ps_stanza
:
   COMMUNITY
   (
      (
         OPEN_BRACKET
         (
            name += VARIABLE
         )+ CLOSE_BRACKET
      )
      | name += VARIABLE
   ) SEMICOLON
;

family_from_t_ps_stanza
:
   FAMILY
   (
      fam = BRIDGE
      | fam = CCC
      | fam = ETHERNET_SWITCHING
      | fam = INET
      | fam = INET_VPN
      | fam = INET6
      | fam = INET6_VPN
      | fam = ISO
      | fam = L2_VPN
      | fam = MPLS
      | fam = VPLS
   ) SEMICOLON
;

interface_from_t_ps_stanza
:
   INTERFACE variable_list SEMICOLON
;

neighbor_from_t_ps_stanza
:
   NEIGHBOR
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) SEMICOLON
;

origin_from_t_ps_stanza
:
   ORIGIN VARIABLE SEMICOLON
;

prefix_list_filter_from_t_ps_stanza
:
   (
      PREFIX_LIST_FILTER name = VARIABLE
   ) match_type = match_type_filter_from_t_ps_stanza?
   (
      SEMICOLON
      | prefix_action += action_filter_from_t_ps_stanza
      |
      (
         OPEN_BRACE
         (
            prefix_action += action_filter_from_t_ps_stanza
         )* CLOSE_BRACE
      )
   )
;

prefix_list_from_t_ps_stanza
:
   PREFIX_LIST name = VARIABLE SEMICOLON
;

protocol_from_t_ps_stanza
:
   PROTOCOL
   (
      (
      )
      |
      (
         l = protocol_list
      )
   ) SEMICOLON
;

rib_from_t_ps_stanza
:
   rib_common_stanza
;

route_filter_from_t_ps_stanza
:
   ROUTE_FILTER
   (
      IP_ADDRESS_WITH_MASK
      | IPV6_ADDRESS_WITH_MASK
   ) match_type = match_type_filter_from_t_ps_stanza?
   (
      SEMICOLON
      | prefix_action += action_filter_from_t_ps_stanza
      |
      (
         OPEN_BRACE
         (
            prefix_action += action_filter_from_t_ps_stanza
         )* CLOSE_BRACE
      )
   )
;

source_address_filter_from_t_ps_stanza
:
   SOURCE_ADDRESS_FILTER
   (
      IP_ADDRESS_WITH_MASK
      | IPV6_ADDRESS_WITH_MASK
   ) match_type = match_type_filter_from_t_ps_stanza?
   (
      SEMICOLON
      | prefix_action += action_filter_from_t_ps_stanza
      |
      (
         OPEN_BRACE
         (
            prefix_action += action_filter_from_t_ps_stanza
         )* CLOSE_BRACE
      )
   )
;

tag_from_t_ps_stanza
:
   TAG DEC SEMICOLON
;

accept_then_t_ps_stanza
:
   ACCEPT SEMICOLON
;

as_path_prepend_then_t_ps_stanza
:
   AS_PATH_PREPEND
   (
      asnum = DEC
      | string_in_double_quotes
   ) SEMICOLON
;

community_then_t_ps_stanza
:
   COMMUNITY
   (
      (
         ADD
         | DELETE
         | SET
      ) variable_list
   ) SEMICOLON
;

install_next_hop_then_t_ps_stanza
:
   INSTALL_NEXTHOP LSP
   (
      name = VARIABLE
      |
      (
         i = DEC DASH name = VARIABLE
      )
   ) SEMICOLON
;

local_preference_then_t_ps_stanza
:
   LOCAL_PREFERENCE
   (
      ADD
      | SUBTRACT
   )? DEC SEMICOLON
;

metric_then_t_ps_stanza
:
   METRIC
   (
      (
         OPEN_BRACE IGP DEC SEMICOLON CLOSE_BRACE
      )
      |
      (
         DEC SEMICOLON
      )
   )
;

next_hop_then_t_ps_stanza
:
   NEXT_HOP
   (
      IP_ADDRESS
      | IPV6_ADDRESS
      | DISCARD
      | SELF
   ) SEMICOLON
;

next_policy_then_t_ps_stanza
:
   NEXT POLICY SEMICOLON
;

next_term_then_t_ps_stanza
:
   NEXT TERM SEMICOLON
;

reject_then_t_ps_stanza
:
   REJECT SEMICOLON
;

null_then_t_ps_stanza
:
   load_balance_then_t_ps_stanza
;

rib_to_t_ps_stanza
:
   rib_common_stanza
;

instance_to_t_ps_stanza
:
   INSTANCE VARIABLE SEMICOLON
;

match_type_filter_from_t_ps_stanza
:
   (
      (
         ADDRESS_MASK
         (
            IP_ADDRESS
            | IPV6_ADDRESS
         )
      )
      | EXACT
      | LONGER
      | ORLONGER
      |
      (
         PREFIX_LENGTH_RANGE FORWARD_SLASH r1 = DEC DASH FORWARD_SLASH r2 = DEC
      )
      |
      (
         THROUGH
         (
            IP_ADDRESS_WITH_MASK
            | IPV6_ADDRESS_WITH_MASK
         )
      )
      |
      (
         UPTO FORWARD_SLASH r = DEC
      )
   )
;

action_filter_from_t_ps_stanza
:
   then_t_ps_stanza
;

load_balance_then_t_ps_stanza
:
   LOAD_BALANCE PER_PACKET SEMICOLON
;
