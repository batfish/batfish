parser grammar Cisco_cable;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

c_fiber_node
:
   FIBER_NODE node = DEC NEWLINE
   (
      cfn_null
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
      | IPV6
      | LOGGING
      | METERING
      | MODEM
      | MODULATION_PROFILE
      | MULTICAST
      | PRE_EQUALIZATION
      | SNMP
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

cfn_null
:
   NO?
   (
      DOWNSTREAM
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
   DOCSIS_POLICY policy = DEC RULE rulenum = DEC NEWLINE
;

clb_null
:
   (
      D20_GGRP_DEFAULT
      | D30_GGRP_DEFAULT
      | DOCSIS_ENABLE
      | DOCSIS30_ENABLE
      | EXCLUDE
      | METHOD_UTILIZATION
      | MODEM
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

s_cable
:
   NO? CABLE
   (
      c_fiber_node
      | c_load_balance
      | c_null
      | c_qos
      | c_service
      | c_tag
   )
;
