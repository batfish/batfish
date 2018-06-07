parser grammar FlatJuniper_isis;

import FlatJuniper_common;

options {
  tokenVocab = FlatJuniperLexer;
}

hello_authentication_type
:
  MD5
  | SIMPLE
;

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
    | isi_bfd_liveness_detection
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
    | TRACEOPTIONS
  ) null_filler
;

is_overload
:
  OVERLOAD iso_timeout
;

is_reference_bandwidth
:
  REFERENCE_BANDWIDTH reference_bandwidth
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

isi_bfd_liveness_detection
:
  BFD_LIVENESS_DETECTION
  (
    isib_minimum_interval
    | isib_multiplier
  )
;

isi_level
:
  LEVEL DEC
  (
    isil_enable
    | isil_hello_authentication_key
    | isil_hello_authentication_type
    | isil_hello_interval
    | isil_hold_time
    | isil_metric
    | isil_te_metric
  )
;

isi_null
:
  (
    HELLO_PADDING
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

isib_minimum_interval
:
  MINIMUM_INTERVAL DEC
;

isib_multiplier
:
  MULTIPLIER DEC
;

isil_enable
:
  ENABLE
;

isil_hello_authentication_key
:
  HELLO_AUTHENTICATION_KEY key = string
;

isil_hello_authentication_type
:
  HELLO_AUTHENTICATION_TYPE hello_authentication_type
;

isil_hello_interval
:
  HELLO_INTERVAL DEC
;

isil_hold_time
:
  HOLD_TIME DEC
;

isil_metric
:
  METRIC DEC
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

iso_timeout
:
  TIMEOUT DEC
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
    | is_overload
    | is_reference_bandwidth
    | is_rib_group
    | is_traffic_engineering
  )
;
