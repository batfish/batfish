parser grammar FlatJuniper_isis;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

is_export
:
   EXPORT
   (
      policies += variable
   )+
;

is_interface
:
   INTERFACE
   (
      id = interface_id
      | wildcard
   )
   (
      apply
      | isi_level
      | isi_null
      | isi_passive
      | isi_point_to_point
   )
;

is_level
:
   LEVEL
   (
      DEC
      | wildcard
   )
   (
      isl_disable
      | isl_enable
      | isl_null
      | isl_wide_metrics_only
   )
;

is_no_ipv4_routing
:
   NO_IPV4_ROUTING
;

is_null
:
   (
      LSP_LIFETIME
      | SPF_OPTIONS
      | OVERLOAD
      | TRACEOPTIONS
   ) null_filler
;

is_rib_group
:
   RIB_GROUP INET name = variable
;

is_traffic_engineering
:
   TRAFFIC_ENGINEERING
   (
      ist_credibility_protocol_preference
      | ist_family_shortcuts
      | ist_multipath
   )
;

isi_level
:
   LEVEL DEC
   (
      isil_enable
      | isil_metric
      | isil_te_metric
      | isil_null
   )
;

isi_null
:
   (
      BFD_LIVENESS_DETECTION
      | HELLO_PADDING
      | LDP_SYNCHRONIZATION
      | LSP_INTERVAL
      | NO_ADJACENCY_DOWN_NOTIFICATION
      | NODE_LINK_PROTECTION
   ) null_filler
;

isi_passive
:
   PASSIVE
;

isi_point_to_point
:
   POINT_TO_POINT
;

isil_enable
:
   ENABLE
;

isil_metric
:
   METRIC DEC
;

isil_null
:
   (
      HELLO_AUTHENTICATION_KEY
      | HELLO_AUTHENTICATION_TYPE
      | HELLO_INTERVAL
      | HOLD_TIME
   ) null_filler
;

isil_te_metric
:
   TE_METRIC DEC
;

isl_disable
:
   DISABLE
;

isl_enable
:
   ENABLE
;

isl_null
:
   (
      AUTHENTICATION_KEY
      | AUTHENTICATION_TYPE
      | PREFIX_EXPORT_LIMIT
   ) null_filler
;

isl_wide_metrics_only
:
   WIDE_METRICS_ONLY
;

ist_credibility_protocol_preference
:
   CREDIBILITY_PROTOCOL_PREFERENCE
;

ist_family_shortcuts
:
   FAMILY
   (
      INET
      | INET6
   ) SHORTCUTS
;

ist_multipath
:
   MULTIPATH LSP_EQUAL_COST
;

p_isis
:
   ISIS
   (
      apply
      | is_export
      | is_interface
      | is_level
      | is_null
      | is_no_ipv4_routing
      | is_rib_group
      | is_traffic_engineering
   )
;
