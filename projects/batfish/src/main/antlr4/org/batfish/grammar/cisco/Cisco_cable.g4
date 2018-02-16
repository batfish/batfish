parser grammar Cisco_cable;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

c_fiber_node
:
   FIBER_NODE
   (
      node = DEC
      | quoted_name = double_quoted_string
   ) NEWLINE
   (
      cfn_null
   )*
;

c_filter
:
   FILTER ~NEWLINE* NEWLINE
   (
      cf_null
   )*
;

c_load_balance
:
   LOAD_BALANCE
   (
      clb_docsis_group
      | clb_docsis_policy
      | clb_null
      | clb_rule
   )
;

c_modulation_profile_block
:
   MODULATION_PROFILE DEC NEWLINE
   (
      cmp_null
   )* EXIT NEWLINE
;

c_modulation_profile_single
:
// intentional + after ~NEWLINE
   MODULATION_PROFILE DEC ~NEWLINE+ NEWLINE
;

c_null
:
   (
      ACFE
      | ADMISSION_CONTROL
      | CLOCK
      | DEFAULT_TOS_QOS10
      | DS_MAX_BURST
      | DSG
      | FLAP_LIST
      | GLOBAL
      | INTERCEPT
      | IPV6
      | LOGGING
      | METERING
      | MODEM
      | MULTICAST
      | PRE_EQUALIZATION
      | SHARED_SECONDARY_SECRET
      | SHARED_SECRET
      | SNMP
      | SUBMGMT
      | UTIL_INTERVAL
      | WIDEBAND
   ) ~NEWLINE* NEWLINE
;

c_qos
:
   QOS
   (
      cq_enforce_rule
      | cq_null
   )
;

c_service
:
   SERVICE
   (
      cs_class
      | cs_null
   )
;

c_tag
:
   TAG num = DEC NEWLINE
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
   ) ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

clb_docsis_group
:
   DOCSIS_GROUP group = DEC NEWLINE
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
   ) policy = DEC RULE rulenum = DEC NEWLINE
;

clb_null
:
   (
      D20_GGRP_DEFAULT
      | D30_GGRP_DEFAULT
      | DOCSIS_ENABLE
      | DOCSIS30_ENABLE
      | DOWNSTREAM_START_THRESHOLD
      | EXCLUDE
      | FAILED_LIST
      | GENERAL_GROUP_DEFAULTS
      | METHOD_UTILIZATION
      | MODEM
      | TCS_LOAD_BALANCE
      | UPSTREAM_START_THRESHOLD
   ) ~NEWLINE* NEWLINE
;

clb_rule
:
   RULE rulenum = DEC ~NEWLINE* NEWLINE
;

clbdg_docsis_policy
:
   DOCSIS_POLICY policy = DEC NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

cmp_null
:
   NO?
   (
      IUC
   ) ~NEWLINE* NEWLINE
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
      | LINECODE
      | PM
      | PRI_GROUP
      | PROACTIVE
      | SHUTDOWN
      | STS_1
      | WAVELENGTH
   ) ~NEWLINE* NEWLINE
;

cntlr_rf_channel
:
   NO? RF_CHANNEL channel = DEC
   (
      cntlrrfc_depi_tunnel
      | cntrlrrfc_null
   )*
;

cntlrrfc_depi_tunnel
:
   DEPI_TUNNEL name = variable TSID tsid = DEC NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

cq_enforce_rule
:
   ENFORCE_RULE name = variable NEWLINE
   (
      cqer_null
      | cqer_service_class
   )*
;

cq_null
:
   (
      PERMISSION
      | PROFILE
   ) ~NEWLINE* NEWLINE
;

cqer_null
:
   NO?
   (
      DURATION
      | ENABLED
      | MONITORING_BASICS
      | PENALTY_PERIOD
   ) ~NEWLINE* NEWLINE
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
   CLASS num = DEC
   (
      csc_name
      | csc_null
   )
;

cs_null
:
   (
      ATTRIBUTE
      | FLOW
   ) ~NEWLINE* NEWLINE
;

csc_name
:
   NAME name = variable NEWLINE
;

csc_null
:
   (
      DOWNSTREAM
      | MAX_BURST
      | MAX_CONCAT_BURST
      | MAX_RATE
      | MIN_PACKET_SIZE
      | MIN_RATE
      | PRIORITY
      | REQ_TRANS_POLICY
      | SCHED_TYPE
      | TOS_OVERWRITE
      | UPSTREAM
   ) ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

dc_null
:
   NO?
   (
      MODE
   ) ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

dt_protect_tunnel
:
   PROTECT_TUNNEL name = variable NEWLINE
;

s_cable
:
   NO? CABLE
   (
      c_fiber_node
      | c_filter
      | c_load_balance
      | c_modulation_profile_block
      | c_modulation_profile_single
      | c_null
      | c_qos
      | c_service
      | c_tag
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
