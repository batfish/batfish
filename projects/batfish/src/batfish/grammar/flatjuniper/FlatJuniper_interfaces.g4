parser grammar FlatJuniper_interfaces;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

brt_interface_mode
:
   INTERFACE_MODE interface_mode
;

brt_vlan_id_list
:
   VLAN_ID_LIST DEC
;

direction
:
   INPUT
   | OUTPUT
;

famt_bridge
:
   BRIDGE famt_bridge_tail
;

famt_bridge_tail
:
   brt_interface_mode
   | brt_vlan_id_list
;

famt_inet
:
   INET famt_inet_tail
;

famt_inet_tail
:
// intentional blank

   | ifamt_address
   | ifamt_filter
   | ifamt_mtu
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
   FILTER direction name = variable
;

ifamt_address
:
   ADDRESS IP_PREFIX
   (
      PRIMARY
      | PREFERRED
   )?
;

ifamt_filter
:
   filter
;

ifamt_mtu
:
   it_mtu
;

ifamt_no_redirects
:
   NO_REDIRECTS
;

interface_mode
:
   TRUNK
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
      | TRAPS
   ) s_null_filler
;

it_unit
:
   UNIT
   (
      WILDCARD
      | num = DEC
   ) it_unit_tail
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
   INTERFACES
   (
      WILDCARD
      | name = VARIABLE
      | // intentional blank

   ) s_interfaces_tail
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
   famt_bridge
   | famt_inet
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
