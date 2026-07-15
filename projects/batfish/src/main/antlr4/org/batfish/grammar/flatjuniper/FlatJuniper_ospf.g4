parser grammar FlatJuniper_ospf;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

o_area
:
   AREA
   (
      area_int = uint32
      | area_ip = ip_address
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
   | o_disable
   | o_enable
   | o_export
   | o_external_preference
   | o_graceful_restart_null
   | o_import
   | o_no_active_backbone
   | o_no_rfc_1583_null
   | o_overload_null
   | o_prefix_export_limit
   | o_reference_bandwidth
   | o_rib_group
   | o_spf_options_null
   | o_traceoptions_null
   | o_traffic_engineering
;

o_disable
:
   DISABLE
;

o_enable
:
   ENABLE
;

o_export
:
   EXPORT expr = policy_expression
;

o_external_preference
:
   EXTERNAL_PREFERENCE dec
;

o_import
:
   IMPORT expr = policy_expression
;

o_no_active_backbone
:
   NO_ACTIVE_BACKBONE
;

o_graceful_restart_null
:
   GRACEFUL_RESTART null_filler
;
o_no_rfc_1583_null
:
   NO_RFC_1583 null_filler
;
o_overload_null
:
   OVERLOAD null_filler
;
o_spf_options_null
:
   SPF_OPTIONS null_filler
;
o_traceoptions_null
:
   TRACEOPTIONS null_filler
;

o_prefix_export_limit
:
    PREFIX_EXPORT_LIMIT limit = uint32
;

o_reference_bandwidth
:
   REFERENCE_BANDWIDTH bandwidth
;

o_rib_group
:
   RIB_GROUP name = junos_name
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
      prefix = ip_prefix_default_32
      | prefix6 = ipv6_prefix_default_128
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
      | ip = ip_address
      | wildcard
   )
   (
      apply
      | oai_authentication_null
      | oai_bfd_liveness_detection_null
      | oai_dead_interval
      | oai_disable
      | oai_enable
      | oai_hello_interval
      | oai_interface_type
      | oai_ldp_synchronization
      | oai_link_protection
      | oai_metric
      | oai_neighbor
      | oai_no_neighbor_down_notification_null
      | oai_passive
      | oai_poll_interval_null
      | oai_priority
      | oai_retransmit_interval_null
      | oai_te_metric
   )
;

oa_label_switched_path
:
   LABEL_SWITCHED_PATH name = junos_name
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
   OVERRIDE_METRIC dec
;

oaa_restrict
:
   RESTRICT
;

oai_dead_interval
:
// https://www.juniper.net/documentation/us/en/software/junos/ospf/topics/topic-map/configuring-ospf-timers.html
// By default, the routing device waits 40 seconds (four times the hello interval).
// The range is 1 through 65,535 seconds.
   DEAD_INTERVAL uint16
;

oai_disable
:
   DISABLE
;

oai_enable
:
   ENABLE
;

oai_hello_interval
:
// https://www.juniper.net/documentation/us/en/software/junos/ospf/topics/topic-map/configuring-ospf-timers.html
// By default, the routing device sends hello packets every 10 seconds.
// The range is from 1 through 255 seconds.
   HELLO_INTERVAL uint8
;

oai_interface_type
:
   INTERFACE_TYPE type = ospf_interface_type
;

oai_ldp_synchronization
:
   LDP_SYNCHRONIZATION
   (
       apply
       | oai_ls_disable
       | oai_ls_hold_time
   )
;

oai_link_protection
:
   LINK_PROTECTION
;

oai_ls_disable
:
   DISABLE
;

oai_ls_hold_time
:
   HOLD_TIME time = dec
;

oai_metric
:
   METRIC dec
;

oai_neighbor
:
   NEIGHBOR ip_address ELIGIBLE?
;

oai_authentication_null
:
   AUTHENTICATION null_filler
;
oai_bfd_liveness_detection_null
:
   BFD_LIVENESS_DETECTION null_filler
;
oai_no_neighbor_down_notification_null
:
   NO_NEIGHBOR_DOWN_NOTIFICATION null_filler
;
oai_poll_interval_null
:
   POLL_INTERVAL null_filler
;

oai_passive
:
   PASSIVE
;

oai_priority
:
   PRIORITY dec
;

oai_retransmit_interval_null
:
// https://www.juniper.net/documentation/us/en/software/junos/ospf/topics/topic-map/configuring-ospf-timers.html
// By default, the routing device retransmits LSAs to its neighbors every 5 seconds.
// The range is from 1 through 65,535 seconds.
   RETRANSMIT_INTERVAL uint16
;

oai_te_metric
:
   TE_METRIC dec
;

oal_metric
:
   METRIC dec
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
   DEFAULT_METRIC dec
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
   DEFAULT_METRIC dec
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
