parser grammar FlatJuniper_ospf;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

o_area
:
   AREA
   (
      area = IP_ADDRESS
      | wildcard
   )
   (
      apply
      | oa_area_range
      | oa_interface
      | oa_label_switched_path
      | oa_nssa
      | oa_null
      | oa_stub
   )
;

o_common
:
   apply
   | o_area
   | o_export
   | o_external_preference
   | o_import
   | o_no_active_backbone
   | o_null
   | o_reference_bandwidth
   | o_rib_group
   | o_traffic_engineering
;

o_export
:
   EXPORT name = variable
;

o_external_preference
:
   EXTERNAL_PREFERENCE DEC
;

o_import
:
   IMPORT name = variable
;

o_no_active_backbone
:
   NO_ACTIVE_BACKBONE
;

o_null
:
   (
      GRACEFUL_RESTART
      | NO_RFC_1583
      | OVERLOAD
      | SPF_OPTIONS
      | TRACEOPTIONS
   ) null_filler
;

o_reference_bandwidth
:
   REFERENCE_BANDWIDTH bandwidth
;

o_rib_group
:
   RIB_GROUP name = variable
;

o_traffic_engineering
:
   TRAFFIC_ENGINEERING
   (
      apply
      | ot_credibility_protocol_preference
      | ot_shortcuts
   )
;

oa_area_range
:
   AREA_RANGE
   (
      IP_PREFIX
      | IPV6_PREFIX
   )
   (
      apply
      |
      (
         oaa_override_metric
         | oaa_restrict
      )+
   )
;

oa_interface
:
   INTERFACE
   (
      ALL
      | id = interface_id
      | ip = IP_ADDRESS
      | wildcard
   )
   (
      apply
      | oai_dead_interval
      | oai_disable
      | oai_hello_interval
      | oai_interface_type
      | oai_ldp_synchronization
      | oai_link_protection
      | oai_metric
      | oai_neighbor
      | oai_null
      | oai_passive
      | oai_priority
      | oai_te_metric
   )
;

oa_label_switched_path
:
   LABEL_SWITCHED_PATH name = variable
   (
      apply
      | oal_metric
   )
;

oa_nssa
:
   NSSA
   (
      apply
      | oan_area_range
      | oan_default_lsa
      | oan_no_summaries
   )
;

oa_null
:
   (
      AUTHENTICATION_TYPE
   ) null_filler
;

oa_stub
:
   STUB
   (
      oas_no_summaries
      | oas_default_metric
   )*
;

oaa_override_metric
:
   OVERRIDE_METRIC DEC
;

oaa_restrict
:
   RESTRICT
;

oai_dead_interval
:
   DEAD_INTERVAL DEC
;

oai_disable
:
   DISABLE
;

oai_hello_interval
:
   HELLO_INTERVAL DEC
;

oai_interface_type
:
   INTERFACE_TYPE type = ospf_interface_type
;

oai_ldp_synchronization
:
   LDP_SYNCHRONIZATION
;

oai_link_protection
:
   LINK_PROTECTION
;

oai_metric
:
   METRIC DEC
;

oai_neighbor
:
   NEIGHBOR IP_ADDRESS ELIGIBLE?
;

oai_null
:
   (
      AUTHENTICATION
      | BFD_LIVENESS_DETECTION
      | NO_NEIGHBOR_DOWN_NOTIFICATION
      | POLL_INTERVAL
   ) null_filler
;

oai_passive
:
   PASSIVE
;

oai_priority
:
   PRIORITY DEC
;

oai_te_metric
:
   TE_METRIC DEC
;

oal_metric
:
   METRIC DEC
;

oan_area_range
:
   oa_area_range
;

oan_default_lsa
:
   DEFAULT_LSA
   (
      apply
      | oand_default_metric
      | oand_metric_type
      | oand_type_7
   )
;

oan_no_summaries
:
   NO_SUMMARIES
;

oand_default_metric
:
   DEFAULT_METRIC DEC
;

oand_metric_type
:
   METRIC_TYPE
   (
      METRIC_TYPE_1
      | METRIC_TYPE_2
   )
;

oand_type_7
:
   TYPE_7
;

oas_no_summaries
:
   NO_SUMMARIES
;

oas_default_metric
:
   DEFAULT_METRIC DEC
;

ospf_interface_type
:
   (
      NBMA
      | P2MP
      | P2MP_OVER_LAN
      | P2P
   )
;

ot_credibility_protocol_preference
:
   CREDIBILITY_PROTOCOL_PREFERENCE
;

ot_shortcuts
:
   SHORTCUTS
;

p_ospf
:
   OSPF o_common
;

p_ospf3
:
   OSPF3 o_common
;
