parser grammar FlatJuniper_isis;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

isisilt_enable
:
   ENABLE
;

isisilt_metric
:
   METRIC DEC
;

isisilt_te_metric
:
   TE_METRIC DEC
;

isisilt_null
:
   (
      HELLO_AUTHENTICATION_KEY
      | HELLO_AUTHENTICATION_TYPE
      | HELLO_INTERVAL
      | HOLD_TIME
   ) s_null_filler
;

isisit_apply_groups
:
   s_apply_groups
;

isisit_apply_groups_except
:
   s_apply_groups_except
;

isisit_level
:
   LEVEL DEC isisit_level_tail
;

isisit_level_tail
:
   isisilt_enable
   | isisilt_metric
   | isisilt_te_metric
   | isisilt_null
;

isisit_null
:
   (
      BFD_LIVENESS_DETECTION
      | HELLO_PADDING
      | LSP_INTERVAL
   ) s_null_filler
;

isisit_passive
:
   PASSIVE
;

isisit_point_to_point
:
   POINT_TO_POINT
;

isislt_disable
:
   DISABLE
;

isislt_enable
:
   ENABLE
;

isislt_null
:
   (
      AUTHENTICATION_KEY
      | AUTHENTICATION_TYPE
      | PREFIX_EXPORT_LIMIT
   ) s_null_filler
;

isislt_wide_metrics_only
:
   WIDE_METRICS_ONLY
;

isist_apply_groups
:
   s_apply_groups
;

isist_export
:
   EXPORT
   (
      policies += variable
   )+
;

isist_interface
:
   INTERFACE
   (
      id = interface_id
      | WILDCARD
   ) isist_interface_tail
;

isist_interface_tail
:
// intentional blank

   | isisit_apply_groups
   | isisit_apply_groups_except
   | isisit_level
   | isisit_null
   | isisit_passive
   | isisit_point_to_point
;

isist_level
:
   LEVEL
   (
      DEC
      | WILDCARD
   ) isist_level_tail
;

isist_level_tail
:
   isislt_disable
   | isislt_enable
   | isislt_null
   | isislt_wide_metrics_only
;

isist_null
:
   (
      LSP_LIFETIME
      | SPF_OPTIONS
      | OVERLOAD
      | TRACEOPTIONS
   ) s_null_filler
;

isist_no_ipv4_routing
:
   NO_IPV4_ROUTING
;

isist_rib_group
:
   RIB_GROUP INET name = variable
;

isist_traffic_engineering
:
   TRAFFIC_ENGINEERING isist_traffic_engineering_tail
;

isist_traffic_engineering_tail
:
   isistet_credibility_protocol_preference
   | isistet_family_shortcuts
   | isistet_multipath
;

isistet_credibility_protocol_preference
:
   CREDIBILITY_PROTOCOL_PREFERENCE
;

isistet_family_shortcuts
:
   FAMILY
   (
      INET
      | INET6
   ) SHORTCUTS
;

isistet_multipath
:
   MULTIPATH LSP_EQUAL_COST
;

s_protocols_isis
:
   ISIS s_protocols_isis_tail
;

s_protocols_isis_tail
:
   isist_apply_groups
   | isist_export
   | isist_interface
   | isist_level
   | isist_null
   | isist_no_ipv4_routing
   | isist_rib_group
   | isist_traffic_engineering
;