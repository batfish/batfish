parser grammar Legacy_mpls;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
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
   INTERFACE null_rest_of_line
;

mldp_log
:
   NO? LOG null_rest_of_line
   (
      mlpdl_null
   )*
;

mldp_neighbor
:
   NO? NEIGHBOR null_rest_of_line
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
   ) null_rest_of_line
;

mldp_session_protection
:
   SESSION PROTECTION (FOR peer = variable)? (DURATION dec)? NEWLINE
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
   ) null_rest_of_line
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
   ) null_rest_of_line
;

mldpaflla_null
:
   NO?
   (
      | DISABLE
      | EXPLICIT_NULL
      | FOR
   ) null_rest_of_line
;

mlpdl_null
:
   NO?
   (
      ADJACENCY
      | NEIGHBOR
      | NSR
      | SESSION_PROTECTION
   ) null_rest_of_line
;

mldpn_null
:
   NO?
   (
      PASSWORD
   ) null_rest_of_line
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
      | mldp_session_protection
      | mldp_null
   )*
;

s_mpls_label_range
:
   MPLS LABEL RANGE dec+ NEWLINE
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
   NO? ATTRIBUTE_SET null_rest_of_line
   (
      mteas_null
   )*
;

mte_auto_tunnel
:
   NO? AUTO_TUNNEL null_rest_of_line
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
   ) null_rest_of_line
;

mte_soft_preemption
:
   SOFT_PREEMPTION null_rest_of_line
   (
      mtes_null
   )*
;

mteas_null
:
   NO?
   (
      AFFINITY
   ) null_rest_of_line
;

mteat_null
:
   NO?
   (
      TUNNEL_ID
   ) null_rest_of_line
;

mtei_auto_tunnel
:
   NO? AUTO_TUNNEL null_rest_of_line
   (
      mteiat_null
   )*
;

mtei_null
:
   NO?
   (
      ATTRIBUTE_NAMES
   ) null_rest_of_line
;

mteiat_null
:
   NO?
   (
      ATTRIBUTE_SET
      | EXCLUDE
      | NHOP_ONLY
   ) null_rest_of_line
;

mtes_null
:
   NO?
   (
      TIMEOUT
   ) null_rest_of_line
;
