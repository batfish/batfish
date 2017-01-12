parser grammar Cisco_mpls;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

mldp_address_family
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   ) NEWLINE
   (
      mldpaf_label
      | mldpaf_null
   )*
;

mldp_interface
:
   INTERFACE ~NEWLINE* NEWLINE
;

mldp_log
:
   NO? LOG ~NEWLINE* NEWLINE
   (
      mlpdl_null
   )*
;

mldp_neighbor
:
   NO? NEIGHBOR ~NEWLINE* NEWLINE
   (
      mldpn_null
   )*
;

mldp_null
:
   NO?
   (
      NSR
      | IGP
   ) ~NEWLINE* NEWLINE
;

mldp_router_id
:
   ROUTER_ID IP_ADDRESS NEWLINE
;

mldpaf_label
:
   LABEL NEWLINE
   (
      mldpafl_local
   )*
;

mldpaf_null
:
   NO?
   (
      DISCOVERY
   ) ~NEWLINE* NEWLINE
;

mldpafl_local
:
   LOCAL NEWLINE
   (
      mldpafll_advertise
      | mldpafll_null
   )*
;

mldpafll_advertise
:
   ADVERTISE NEWLINE
   (
      mldpaflla_null
   )*
;

mldpafll_null
:
   NO?
   (
      ALLOCATE
   ) ~NEWLINE* NEWLINE
;

mldpaflla_null
:
   NO?
   (
      | DISABLE
      | EXPLICIT_NULL
      | FOR
   ) ~NEWLINE* NEWLINE
;

mlpdl_null
:
   NO?
   (
      ADJACENCY
      | NEIGHBOR
      | NSR
      | SESSION_PROTECTION
   ) ~NEWLINE* NEWLINE
;

mldpn_null
:
   NO?
   (
      PASSWORD
   ) ~NEWLINE* NEWLINE
;

s_mpls_ldp
:
   MPLS
   (
      LABEL PROTOCOL
   )? LDP NEWLINE
   (
      mldp_router_id
      | mldp_address_family
      | mldp_interface
      | mldp_log
      | mldp_neighbor
      | mldp_null
   )*
;

s_mpls_label_range
:
   MPLS LABEL RANGE DEC+ NEWLINE
;

s_mpls_traffic_eng
:
   MPLS TRAFFIC_ENG NEWLINE
   (
      mte_attribute_set
      | mte_auto_tunnel
      | mte_interface
      | mte_null
      | mte_soft_preemption
   )*
;

mte_attribute_set
:
   NO? ATTRIBUTE_SET ~NEWLINE* NEWLINE
   (
      mteas_null
   )*
;

mte_auto_tunnel
:
   NO? AUTO_TUNNEL ~NEWLINE* NEWLINE
   (
      mteat_null
   )*
;

mte_interface
:
   INTERFACE name = interface_name NEWLINE
   (
      mtei_auto_tunnel
      | mtei_null
   )*
;

mte_null
:
   NO?
   (
      AFFINITY_MAP
      | LOGGING
      | REOPTIMIZE
   ) ~NEWLINE* NEWLINE
;

mte_soft_preemption
:
   SOFT_PREEMPTION ~NEWLINE* NEWLINE
   (
      mtes_null
   )*
;

mteas_null
:
   NO?
   (
      AFFINITY
   ) ~NEWLINE* NEWLINE
;

mteat_null
:
   NO?
   (
      TUNNEL_ID
   ) ~NEWLINE* NEWLINE
;

mtei_auto_tunnel
:
   NO? AUTO_TUNNEL ~NEWLINE* NEWLINE
   (
      mteiat_null
   )*
;

mtei_null
:
   NO?
   (
      ATTRIBUTE_NAMES
   ) ~NEWLINE* NEWLINE
;

mteiat_null
:
   NO?
   (
      ATTRIBUTE_SET
      | EXCLUDE
      | NHOP_ONLY
   ) ~NEWLINE* NEWLINE
;

mtes_null
:
   NO?
   (
      TIMEOUT
   ) ~NEWLINE* NEWLINE
;
