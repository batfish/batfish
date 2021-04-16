parser grammar CiscoXr_mpls;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_mpls
:
  MPLS
  (
    mpls_label
    | mpls_ldp
    | mpls_oam
    | mpls_traffic_eng
  )
;

mpls_oam: OAM NEWLINE mpls_oam_inner*;

mpls_oam_inner: mpls_oam_null;

mpls_oam_null
:
  (
    ECHO
  ) null_rest_of_line
;

mldp_address_family
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   ) NEWLINE
   (
      mldpaf_discovery
      | mldpaf_label
      | mldpaf_null
      | mldpaf_redistribute
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
    | GRACEFUL_RESTART
    | IGP
    | NSR
  ) null_rest_of_line
;

mldp_session_protection
:
   SESSION PROTECTION (FOR peer = variable)? (DURATION uint_legacy)? NEWLINE
;

mldp_router_id
:
   ROUTER_ID IP_ADDRESS NEWLINE
;

mldpaf_discovery
:
  DISCOVERY
  (
    mldpafd_targeted_hello
    | mldpafd_transport_address
  )
;

mldpafd_targeted_hello: TARGETED_HELLO ACCEPT FROM name = access_list_name NEWLINE;

mldpafd_transport_address: TRANSPORT_ADDRESS IP_ADDRESS NEWLINE;

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
      NEIGHBOR
   ) null_rest_of_line
;

mldpaf_redistribute
:
  REDISTRIBUTE
  (
    mldpafr_bgp
    | NEWLINE mldpafr_bgp*
  )
;

mldpafr_bgp
:
  BGP
  (
    mldpafrb_inner
    | NEWLINE mldpafrb_inner*
  )
;

mldpafrb_inner
:
  mldpafrb_advertise_to
  | mldpafrb_null
;

mldpafrb_advertise_to: ADVERTISE_TO name = access_list_name NEWLINE;

mldpafrb_null: AS null_rest_of_line;

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
    GRACEFUL_RESTART
    | HELLO_ADJACENCY
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

mpls_ldp
:
   LDP NEWLINE
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

mpls_label: LABEL RANGE uint_legacy+ NEWLINE;

mpls_traffic_eng
:
   TRAFFIC_ENG NEWLINE
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
