parser grammar A10_vrrp_a;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_vrrp_a
:
  VRRP_A
  (
    vrrpa_common
    | vrrpa_fail_over_policy_template
    | vrrpa_interface
    | vrrpa_null
    | vrrpa_peer_group
    | vrrpa_vrid
    | vrrpa_vrid_lead
  )
;

vrrpa_common: COMMON NEWLINE vrrpac*;

vrrpac
:
  vrrpac_device_id
  | vrrpac_disable_default_vrid
  | vrrpac_enable
  | vrrpac_null
  | vrrpac_set_id
;

vrrpac_device_id: DEVICE_ID vrrpa_device_id_number NEWLINE;

vrrpa_device_id_number
:
  // 1-4
  uint8
;

vrrpac_disable_default_vrid: DISABLE_DEFAULT_VRID NEWLINE;

vrrpac_enable: ENABLE NEWLINE;

vrrpac_null
:
  (
    ARP_RETRY
    | DEAD_TIMER
    | GET_READY_TIME
    | HELLO_INTERVAL
    | PREEMPTION_DELAY
    | RESTART_TIME
    | TRACK_EVENT_DELAY
  ) null_rest_of_line
;

vrrpac_set_id: SET_ID vrrpa_set_id_number NEWLINE;

vrrpa_set_id_number
:
  // 1-15
  uint8
;

vrrpa_fail_over_policy_template
:
  FAIL_OVER_POLICY_TEMPLATE name = fail_over_policy_template_name NEWLINE vrrpaf*
;

fail_over_policy_template_name
:
  // 1-63 chars
  word
;

vrrpaf: vrrpaf_gateway;

vrrpaf_gateway: GATEWAY gwip = ip_address WEIGHT weight = vrrpaf_gateway_weight NEWLINE;

vrrpaf_gateway_weight
:
  // 1-255
  uint8
;

vrrpa_interface: INTERFACE ref = ethernet_or_trunk_reference NEWLINE;

vrrpa_null
:
  (
    ARP_RETRY
    | DEAD_TIMER
    | GET_READY_TIME_ACOS2
    | HELLO_INTERVAL
    | PREEMPTION_DELAY
    | SESSION_SYNC
    | TRACK_EVENT_DELAY
  ) null_rest_of_line
;

vrrpa_peer_group: PEER_GROUP NEWLINE vrrpapg_peer*;

vrrpapg_peer: PEER ip = ip_address NEWLINE;

vrrpa_vrid
:
  VRID
  (
    vrrpa_vrid_default
    | vrrpa_vrid_id
  )
;

vrrpa_vrid_default: DEFAULT NEWLINE;

vrrpa_vrid_id: vrid NEWLINE vrrpavi*;

vrrpavi
:
  vrrpavi_preempt_mode
  | vrrpavi_blade_parameters
;

vrrpavi_preempt_mode
:
  PREEMPT_MODE
  (
    vrrpavi_preempt_mode_disable
    | vrrpavi_preempt_mode_threshold
  )
;

vrrpavi_preempt_mode_disable: DISABLE NEWLINE;

vrrpavi_preempt_mode_threshold: THRESHOLD threshold = uint8 NEWLINE;

vrrpavi_blade_parameters: BLADE_PARAMETERS NEWLINE vrrpavib*;

vrrpavib
:
  vrrpavib_priority
  | vrrpavib_fail_over_policy_template
;

vrrpavib_priority: PRIORITY vrrpa_priority_number NEWLINE;

vrrpa_priority_number
:
  // 1-255
  uint8
;

vrrpavib_fail_over_policy_template
:
  FAIL_OVER_POLICY_TEMPLATE name = fail_over_policy_template_name NEWLINE
;

vrrpa_vrid_lead: VRID_LEAD name = vrid_lead_name NEWLINE;

vrid_lead_name
:
  // 1-63 chars
  word
;
