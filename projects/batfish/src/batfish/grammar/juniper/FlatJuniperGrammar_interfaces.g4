parser grammar FlatJuniperGrammar_interfaces;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

address_ifamt
:
   ADDRESS IP_ADDRESS_WITH_MASK
;

apply_groups_it
:
   apply_groups_statement
;

description_it
:
   description_statement
;

description_ut
:
   description_statement
;

direction
:
   INPUT
   | OUTPUT
;

disable_it
:
   DISABLE
;

family
:
   INET
   | MPLS
;

family_ut_header
:
   FAMILY
;

family_ut
:
   family_ut_header family_ut_tail
;

family_ut_tail
:
   inet_famt
   | inet6_famt
   | mpls_famt
;

filter_ifamt
:
   filter_statement
;

filter_header
:
   FILTER direction
;

filter_mfamt
:
   filter_statement
;

filter_statement
:
   filter_header filter_tail
;

filter_tail
:
   VARIABLE
;

inet_famt
:
   INET inet_famt_tail
;

inet_famt_tail
:
   address_ifamt
   | filter_ifamt
   | no_redirects_ifamt
;

inet6_famt
:
   INET6 ~NEWLINE*
;

interfaces_header
:
   INTERFACES
   (
      wildcard
      | name = VARIABLE
      | // intentional blank

   )
;

interfaces_statement
:
   interfaces_header interfaces_tail
;

interfaces_tail
:
   apply_groups_it
   | description_it
   | disable_it
   | mtu_it
   | null_it
   | unit_it
   | vlan_tagging_it
;

maximum_labels_mfamt
:
   MAXIMUM_LABELS num = DEC
;

mpls_famt
:
   MPLS mpls_famt_tail
;

mpls_famt_tail
:
// intentional blank alternative

   | filter_mfamt
   | maximum_labels_mfamt
;

mtu_it
:
   MTU size = DEC
;

no_redirects_ifamt
:
   NO_REDIRECTS
;

null_it
:
   AGGREGATED_ETHER_OPTIONS ~NEWLINE*
;

null_ut
:
   BANDWIDTH ~NEWLINE*
;

unit_it_header
:
   UNIT
   (
      wildcard
      | num = DEC
   )
;

unit_it
:
   unit_it_header unit_it_tail
;

unit_it_tail
:
   description_ut
   | family_ut
   | null_ut
;

vlan_tagging_it
:
   VLAN_TAGGING
;
