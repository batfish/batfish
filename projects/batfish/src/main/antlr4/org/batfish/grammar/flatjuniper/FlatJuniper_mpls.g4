parser grammar FlatJuniper_mpls;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

c_interface_switch
:
   INTERFACE_SWITCH name = junos_name
   (
      ci_interface
   )
;

ci_interface
:
   INTERFACE interface_id
;

p_connections
:
   CONNECTIONS
   (
      c_interface_switch
   )
;

p_mpls
:
   MPLS
   (
       mpls_admin_groups
       | mpls_interface
       | mpls_label_switched_path
   )
;

mpls_interface
:
   INTERFACE name = interface_id
   (
      apply
      | mplsi_admin_group
      | mplsi_srlg
   )
;

mplsi_admin_group
:
   ADMIN_GROUP name = junos_name
;

mplsi_srlg
:
   SRLG name = junos_name
;

mpls_admin_groups
:
   // Admin-group values should be in the range 0-31
   ADMIN_GROUPS name = junos_name number = uint8
;

mpls_label_switched_path
:
   LABEL_SWITCHED_PATH name = junos_name
   (
      apply
      | mplslsp_adaptive_null
      | mplslsp_admin_group
      | mplslsp_auto_bandwidth
      | mplslsp_conditional_metric_null
      | mplslsp_in_place_lsp_bandwidth_update_null
      | mplslsp_install_null
      | mplslsp_link_protection_null
      | mplslsp_no_decrement_ttl_null
      | mplslsp_no_self_ping_null
      | mplslsp_optimize_hold_dead_delay_null
      | mplslsp_priority_null
      | mplslsp_random_null
      | mplslsp_retry_timer_null
      | mplslsp_secondary
      | mplslsp_self_ping_duration_null
      | mplslsp_soft_preemption_null
      | mplslsp_to_null
   )
;

mplslsp_to_null
:
   TO (ip_address | ipv6_address)
;

mplslsp_install_null
:
   INSTALL (ip_prefix_default_32 | ipv6_prefix_default_128)
;

mplslsp_no_decrement_ttl_null
:
   NO_DECREMENT_TTL null_filler
;

mplslsp_link_protection_null
:
   LINK_PROTECTION null_filler
;

mplslsp_no_self_ping_null
:
   NO_SELF_PING null_filler
;

mplslsp_self_ping_duration_null
:
   SELF_PING_DURATION uint32
;

mplslsp_adaptive_null
:
   ADAPTIVE null_filler
;

mplslsp_auto_bandwidth
:
   AUTO_BANDWIDTH
   (
      mplslspab_adjust_interval_null
      | mplslspab_adjust_threshold_null
      | mplslspab_adjust_threshold_absolute_null
      | mplslspab_adjust_threshold_activate_bandwidth_null
      | mplslspab_adjust_threshold_overflow_limit_null
      | mplslspab_adjust_threshold_underflow_limit_null
      | mplslspab_maximum_bandwidth_null
      | mplslspab_minimum_bandwidth_null
   )
;

mplslspab_adjust_interval_null
:
   ADJUST_INTERVAL uint32
;

mplslspab_adjust_threshold_null
:
   ADJUST_THRESHOLD uint32
;

mplslspab_adjust_threshold_absolute_null
:
   ADJUST_THRESHOLD_ABSOLUTE null_filler
;

mplslspab_adjust_threshold_activate_bandwidth_null
:
   ADJUST_THRESHOLD_ACTIVATE_BANDWIDTH null_filler
;

mplslspab_adjust_threshold_overflow_limit_null
:
   ADJUST_THRESHOLD_OVERFLOW_LIMIT uint32
;

mplslspab_adjust_threshold_underflow_limit_null
:
   ADJUST_THRESHOLD_UNDERFLOW_LIMIT uint32
;

mplslspab_maximum_bandwidth_null
:
   MAXIMUM_BANDWIDTH null_filler
;

mplslspab_minimum_bandwidth_null
:
   MINIMUM_BANDWIDTH null_filler
;

mplslsp_conditional_metric_null
:
   CONDITIONAL_METRIC IGP_METRIC_THRESHOLD uint32 uint32
;

mplslsp_in_place_lsp_bandwidth_update_null
:
   IN_PLACE_LSP_BANDWIDTH_UPDATE null_filler
;

mplslsp_optimize_hold_dead_delay_null
:
   OPTIMIZE_HOLD_DEAD_DELAY uint32
;

mplslsp_priority_null
:
   PRIORITY uint32 uint32
;

mplslsp_random_null
:
   RANDOM null_filler
;

mplslsp_retry_timer_null
:
   RETRY_TIMER uint32
;

mplslsp_soft_preemption_null
:
   SOFT_PREEMPTION null_filler
;

mplslsp_admin_group
:
   ADMIN_GROUP
   (
      mplslspag_exclude
      | mplslspag_include_all
      | mplslspag_include_any
   )
;

mplslspag_exclude
:
   EXCLUDE name = junos_name
;

mplslspag_include_all
:
   INCLUDE_ALL name = junos_name
;

mplslspag_include_any
:
   INCLUDE_ANY name = junos_name
;

mplslsp_secondary
:
   SECONDARY name = junos_name
   (
      apply
      | mplslsps_admin_group
   )
;

mplslsps_admin_group
:
   ADMIN_GROUP
   (
      mplslspsag_exclude
      | mplslspsag_include_all
      | mplslspsag_include_any
   )
;

mplslspsag_exclude
:
   EXCLUDE name = junos_name
;

mplslspsag_include_all
:
   INCLUDE_ALL name = junos_name
;

mplslspsag_include_any
:
   INCLUDE_ANY name = junos_name
;