parser grammar Cisco_cable;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

c_fiber_node
:
   FIBER_NODE
   (
      node = dec
      | quoted_name = double_quoted_string
   ) NEWLINE
   (
      cfn_null
   )*
;

c_filter
:
   FILTER null_rest_of_line
   (
      cf_null
   )*
;

c_load_balance
:
   LOAD_BALANCE
   (
      clb_d20_ggrp_default_null
      | clb_d30_ggrp_default_null
      | clb_docsis30_enable_null
      | clb_docsis_enable_null
      | clb_docsis_group
      | clb_docsis_policy
      | clb_downstream_start_threshold_null
      | clb_exclude_null
      | clb_failed_list_null
      | clb_general_group_defaults_null
      | clb_method_utilization_null
      | clb_modem_null
      | clb_rule
      | clb_tcs_load_balance_null
      | clb_upstream_start_threshold_null
   )
;

c_modulation_profile_block
:
   MODULATION_PROFILE dec NEWLINE
   (
      cmp_null
   )* EXIT NEWLINE
;

c_modulation_profile_single
:
// intentional + after ~NEWLINE
   MODULATION_PROFILE dec ~NEWLINE+ NEWLINE
;

c_acfe_null
:
   ACFE null_rest_of_line
;
c_admission_control_null
:
   ADMISSION_CONTROL null_rest_of_line
;
c_clock_null
:
   CLOCK null_rest_of_line
;
c_default_tos_qos10_null
:
   DEFAULT_TOS_QOS10 null_rest_of_line
;
c_ds_max_burst_null
:
   DS_MAX_BURST null_rest_of_line
;
c_dsg_null
:
   DSG null_rest_of_line
;
c_flap_list_null
:
   FLAP_LIST null_rest_of_line
;
c_global_null
:
   GLOBAL null_rest_of_line
;
c_intercept_null
:
   INTERCEPT null_rest_of_line
;
c_ipv6_null
:
   IPV6 null_rest_of_line
;
c_logging_null
:
   LOGGING null_rest_of_line
;
c_metering_null
:
   METERING null_rest_of_line
;
c_modem_null
:
   MODEM null_rest_of_line
;
c_multicast_null
:
   MULTICAST null_rest_of_line
;
c_pre_equalization_null
:
   PRE_EQUALIZATION null_rest_of_line
;
c_shared_secondary_secret_null
:
   SHARED_SECONDARY_SECRET null_rest_of_line
;
c_shared_secret_null
:
   SHARED_SECRET null_rest_of_line
;
c_snmp_null
:
   SNMP null_rest_of_line
;
c_submgmt_null
:
   SUBMGMT null_rest_of_line
;
c_util_interval_null
:
   UTIL_INTERVAL null_rest_of_line
;
c_wideband_null
:
   WIDEBAND null_rest_of_line
;

c_qos
:
   QOS
   (
      cq_enforce_rule
      | cq_permission_null
      | cq_profile_null
   )
;

c_service
:
   SERVICE
   (
      cs_attribute_null
      | cs_class
      | cs_flow_null
   )
;

c_tag
:
   TAG num = dec NEWLINE
   (
      ct_name
      | ct_null
   )*
;

cf_null
:
   NO?
   (
      INDEX
   ) null_rest_of_line
;

cfn_null
:
   NO?
   (
      CABLE_DOWNSTREAM
      | CABLE_UPSTREAM
      | DOWNSTREAM
      | INIT
      | UPSTREAM
   ) null_rest_of_line
;

clb_docsis_group
:
   DOCSIS_GROUP group = dec NEWLINE
   (
      clbdg_docsis_policy
      | clbdg_null
   )*
;

clb_docsis_policy
:
   (
      DOCSIS_POLICY
      | POLICY
   ) policy = dec RULE rulenum = dec NEWLINE
;

clb_d20_ggrp_default_null
:
   D20_GGRP_DEFAULT null_rest_of_line
;
clb_d30_ggrp_default_null
:
   D30_GGRP_DEFAULT null_rest_of_line
;
clb_docsis_enable_null
:
   DOCSIS_ENABLE null_rest_of_line
;
clb_docsis30_enable_null
:
   DOCSIS30_ENABLE null_rest_of_line
;
clb_downstream_start_threshold_null
:
   DOWNSTREAM_START_THRESHOLD null_rest_of_line
;
clb_exclude_null
:
   EXCLUDE null_rest_of_line
;
clb_failed_list_null
:
   FAILED_LIST null_rest_of_line
;
clb_general_group_defaults_null
:
   GENERAL_GROUP_DEFAULTS null_rest_of_line
;
clb_method_utilization_null
:
   METHOD_UTILIZATION null_rest_of_line
;
clb_modem_null
:
   MODEM null_rest_of_line
;
clb_tcs_load_balance_null
:
   TCS_LOAD_BALANCE null_rest_of_line
;
clb_upstream_start_threshold_null
:
   UPSTREAM_START_THRESHOLD null_rest_of_line
;

clb_rule
:
   RULE rulenum = dec null_rest_of_line
;

clbdg_docsis_policy
:
   DOCSIS_POLICY policy = dec NEWLINE
;

clbdg_null
:
   NO?
   (
      DISABLE
      | DOWNSTREAM
      | INIT_TECH_LIST
      | INTERVAL
      | METHOD
      | POLICY
      | RESTRICTED
      | TAG
      | THRESHOLD
      | UPSTREAM
   ) null_rest_of_line
;

cmp_null
:
   NO?
   (
      IUC
   ) null_rest_of_line
;

cntlr_null
:
   NO?
   (
      ADMIN_STATE
      | AIS_SHUT
      | ALARM_REPORT
      | CABLELENGTH
      | CHANNEL_GROUP
      | CLOCK
      | DESCRIPTION
      | FDL
      | FRAMING
      | G709
      | LINE_TERMINATION
      | LINECODE
      | PM
      | PRI_GROUP
      | PROACTIVE
      | SHUTDOWN
      | STS_1
      | WAVELENGTH
   ) null_rest_of_line
;

cntlr_rf_channel
:
   NO? RF_CHANNEL channel = dec
   (
      cntlrrfc_depi_tunnel
      | cntrlrrfc_null
   )*
;

cntlrrfc_depi_tunnel
:
   DEPI_TUNNEL name = variable TSID tsid = dec NEWLINE
;

cntrlrrfc_null
:
   NO?
   (
      CABLE
      | FREQUENCY
      | NETWORK_DELAY
      | RF_POWER
      | RF_SHUTDOWN
   ) null_rest_of_line
;

cq_enforce_rule
:
   ENFORCE_RULE name = variable NEWLINE
   (
      cqer_null
      | cqer_service_class
   )*
;

cq_permission_null
:
   PERMISSION null_rest_of_line
;
cq_profile_null
:
   PROFILE null_rest_of_line
;

cqer_null
:
   NO?
   (
      DURATION
      | ENABLED
      | MONITORING_BASICS
      | PENALTY_PERIOD
   ) null_rest_of_line
;

cqer_service_class
:
   SERVICE_CLASS
   (
      ENFORCED
      | REGISTERED
   ) name = variable NEWLINE
;

cs_class
:
   CLASS num = dec
   (
      csc_downstream_null
      | csc_max_burst_null
      | csc_max_concat_burst_null
      | csc_max_rate_null
      | csc_min_packet_size_null
      | csc_min_rate_null
      | csc_name
      | csc_priority_null
      | csc_req_trans_policy_null
      | csc_sched_type_null
      | csc_tos_overwrite_null
      | csc_upstream_null
   )
;

cs_attribute_null
:
   ATTRIBUTE null_rest_of_line
;
cs_flow_null
:
   FLOW null_rest_of_line
;

csc_name
:
   NAME name = variable NEWLINE
;

csc_downstream_null
:
   DOWNSTREAM null_rest_of_line
;
csc_max_burst_null
:
   MAX_BURST null_rest_of_line
;
csc_max_concat_burst_null
:
   MAX_CONCAT_BURST null_rest_of_line
;
csc_max_rate_null
:
   MAX_RATE null_rest_of_line
;
csc_min_packet_size_null
:
   MIN_PACKET_SIZE null_rest_of_line
;
csc_min_rate_null
:
   MIN_RATE null_rest_of_line
;
csc_priority_null
:
   PRIORITY null_rest_of_line
;
csc_req_trans_policy_null
:
   REQ_TRANS_POLICY null_rest_of_line
;
csc_sched_type_null
:
   SCHED_TYPE null_rest_of_line
;
csc_tos_overwrite_null
:
   TOS_OVERWRITE null_rest_of_line
;
csc_upstream_null
:
   UPSTREAM null_rest_of_line
;

ct_name
:
   NAME name = variable NEWLINE
;

ct_null
:
   NO?
   (
      DOCSIS_VERSION
      | EXCLUDE
   ) null_rest_of_line
;

dc_null
:
   NO?
   (
      MODE
   ) null_rest_of_line
;

dt_depi_class
:
   DEPI_CLASS name = variable NEWLINE
;

dt_l2tp_class
:
   L2TP_CLASS name = variable NEWLINE
;

dt_null
:
   NO?
   (
      DEST_IP
   ) null_rest_of_line
;

dt_protect_tunnel
:
   PROTECT_TUNNEL name = variable NEWLINE
;

s_cable
:
   NO? CABLE
   (
      c_acfe_null
      | c_admission_control_null
      | c_clock_null
      | c_default_tos_qos10_null
      | c_ds_max_burst_null
      | c_dsg_null
      | c_fiber_node
      | c_filter
      | c_flap_list_null
      | c_global_null
      | c_intercept_null
      | c_ipv6_null
      | c_load_balance
      | c_logging_null
      | c_metering_null
      | c_modem_null
      | c_modulation_profile_block
      | c_modulation_profile_single
      | c_multicast_null
      | c_pre_equalization_null
      | c_qos
      | c_service
      | c_shared_secondary_secret_null
      | c_shared_secret_null
      | c_snmp_null
      | c_submgmt_null
      | c_tag
      | c_util_interval_null
      | c_wideband_null
   )
;

s_controller
:
   CONTROLLER iname = interface_name NEWLINE
   (
      cntlr_null
      | cntlr_rf_channel
   )*
;

s_depi_class
:
   DEPI_CLASS name = variable NEWLINE
   (
      dc_null
   )*
;

s_depi_tunnel
:
   DEPI_TUNNEL name = variable NEWLINE
   (
      dt_depi_class
      | dt_l2tp_class
      | dt_null
      | dt_protect_tunnel
   )*
;
