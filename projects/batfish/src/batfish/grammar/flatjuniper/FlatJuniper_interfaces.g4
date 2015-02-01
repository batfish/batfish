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

famt_ccc
:
   CCC s_null_filler
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
   | ifamt_null
;

famt_inet6
:
   INET6 s_null_filler
;

famt_iso
:
   ISO s_null_filler
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

ifamat_master_only
:
   MASTER_ONLY
;

ifamat_preferred
:
   PREFERRED
;

ifamat_primary
:
   PRIMARY
;

ifamat_vrrp_group
:
   VRRP_GROUP name = variable ifamat_vrrp_group_tail
;

ifamat_vrrp_group_tail
:
   ivrrpt_accept_data
   | ivrrpt_preempt
   | ivrrpt_priority
   | ivrrpt_track
   | ivrrpt_virtual_address
;

ifamt_address
:
   ADDRESS IP_PREFIX ifamt_address_tail?
;

ifamt_address_tail
:
   ifamat_master_only
   | ifamat_preferred
   | ifamat_primary
   | ifamat_vrrp_group
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

ifamt_null
:
   SAMPLING s_null_filler
;

interface_mode
:
   TRUNK
;

it_apply_groups
:
   s_apply_groups
;

it_apply_groups_except
:
   s_apply_groups_except
;

it_common
:
   it_apply_groups
   | it_apply_groups_except
   | it_description
   | it_disable
   | it_enable
   | it_family
   | it_mtu
   | it_null
   | it_vlan_id
   | it_vlan_tagging
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

it_family
:
   FAMILY it_family_tail
;

it_family_tail
:
   famt_bridge
   | famt_ccc
   | famt_inet
   | famt_inet6
   | famt_iso
   | famt_mpls
;

it_mtu
:
   MTU size = DEC
;

it_null
:
   (
      AGGREGATED_ETHER_OPTIONS
      | BANDWIDTH
      | ENCAPSULATION
      | GIGETHER_OPTIONS
      | INTERFACE_TRANSMIT_STATISTICS
      | TRACEOPTIONS
      | TRAPS
      | TUNNEL
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
// intentional blank

   | it_common
;

it_vlan_id
:
   VLAN_ID id = DEC
;

it_vlan_tagging
:
   VLAN_TAGGING
;

ivrrpt_accept_data
:
   ACCEPT_DATA
;

ivrrpt_preempt
:
   PREEMPT
;

ivrrpt_priority
:
   PRIORITY DEC
;

ivrrpt_track
:
   TRACK ivrrpt_track_tail
;

ivrrpt_track_tail
:
   ivrrptt_interface
;

ivrrpt_virtual_address
:
   VIRTUAL_ADDRESS IP_ADDRESS
;

ivrrptt_interface
:
   INTERFACE interface_id ivrrptt_interface_tail
;

ivrrptt_interface_tail
:
   ivrrptti_priority_cost
;

ivrrptti_priority_cost
:
   PRIORITY_COST cost = DEC
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
   it_common
   | it_unit
;
