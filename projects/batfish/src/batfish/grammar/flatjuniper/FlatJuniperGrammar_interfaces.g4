parser grammar FlatJuniperGrammar_interfaces;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

direction
:
   INPUT
   | OUTPUT
;

family
:
   INET
   | MPLS
;

famt_inet
:
   INET famt_inet_tail
;

famt_inet_tail
:
   ifamt_address
   | ifamt_filter
   | ifamt_no_redirects
;

famt_inet6
:
   INET6 s_null_filler
;

famt_mpls
:
   MPLS famt_mpls_tail
;

famt_mpls_tail
:
// intentional blank alternative

   | mfamt_filter
   | mfamt_maximum_labels
   | mfamt_mtu
;

filter
:
   filter_header filter_tail
;

filter_header
:
   FILTER direction
;

filter_tail
:
   VARIABLE
;

ifamt_address
:
   ADDRESS IP_ADDRESS_WITH_MASK
   (
      PRIMARY
      | PREFERRED
   )?
;

ifamt_filter
:
   filter
;

ifamt_no_redirects
:
   NO_REDIRECTS
;

it_apply_groups
:
   s_apply_groups
;

it_description
:
   s_description
;

it_disable
:
   DISABLE
;

it_enable
:
   ENABLE
;

it_mtu
:
   MTU size = DEC
;

it_null
:
   (
      AGGREGATED_ETHER_OPTIONS
      | GIGETHER_OPTIONS
   ) s_null_filler
;

it_unit
:
   it_unit_header it_unit_tail
;

it_unit_header
:
   UNIT
   (
      WILDCARD
      | num = DEC
   )
;

it_unit_tail
:
   ut_description
   | ut_family
   | ut_null
   | ut_vlan_id
;

it_vlan_tagging
:
   VLAN_TAGGING
;

mfamt_filter
:
   filter
;

mfamt_maximum_labels
:
   MAXIMUM_LABELS num = DEC
;

mfamt_mtu
:
   MTU DEC
;

s_interfaces
:
   s_interfaces_header s_interfaces_tail
;

s_interfaces_header
:
   INTERFACES
   (
      WILDCARD
      | name = VARIABLE
      | // intentional blank

   )
;

s_interfaces_tail
:
   it_apply_groups
   | it_description
   | it_disable
   | it_enable
   | it_mtu
   | it_null
   | it_unit
   | it_vlan_tagging
;

ut_description
:
   s_description
;

ut_family
:
   FAMILY ut_family_tail
;

ut_family_tail
:
   famt_inet
   | famt_inet6
   | famt_mpls
;

ut_null
:
   BANDWIDTH s_null_filler
;

ut_vlan_id
:
   VLAN_ID id = DEC
;
