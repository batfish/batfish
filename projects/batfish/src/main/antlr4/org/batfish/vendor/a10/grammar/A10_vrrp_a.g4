parser grammar A10_vrrp_a;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_vrrp_a
:
  VRRP_A
  (
    vrrpa_arp_retry_null
    | vrrpa_common
    | vrrpa_dead_timer_null
    | vrrpa_fail_over_policy_template
    | vrrpa_get_ready_time_acos2_null
    | vrrpa_hello_interval_null
    | vrrpa_interface
    | vrrpa_peer_group
    | vrrpa_preemption_delay_null
    | vrrpa_session_sync_null
    | vrrpa_track_event_delay_null
    | vrrpa_vrid
    | vrrpa_vrid_lead
  )
;

vrrpa_common: COMMON NEWLINE vrrpac*;

vrrpac
:
  vrrpac_arp_retry_null
  | vrrpac_dead_timer_null
  | vrrpac_device_id
  | vrrpac_disable_default_vrid
  | vrrpac_enable
  | vrrpac_get_ready_time_null
  | vrrpac_hello_interval_null
  | vrrpac_preemption_delay_null
  | vrrpac_restart_time_null
  | vrrpac_set_id
  | vrrpac_track_event_delay_null
;

vrrpac_device_id: DEVICE_ID vrrpa_device_id_number NEWLINE;

vrrpa_device_id_number
:
  // 1-4
  uint8
;

vrrpac_disable_default_vrid: DISABLE_DEFAULT_VRID NEWLINE;

vrrpac_enable: ENABLE NEWLINE;

vrrpac_arp_retry_null
:
   ARP_RETRY null_rest_of_line
;
vrrpac_dead_timer_null
:
   DEAD_TIMER null_rest_of_line
;
vrrpac_get_ready_time_null
:
   GET_READY_TIME null_rest_of_line
;
vrrpac_hello_interval_null
:
   HELLO_INTERVAL null_rest_of_line
;
vrrpac_preemption_delay_null
:
   PREEMPTION_DELAY null_rest_of_line
;
vrrpac_restart_time_null
:
   RESTART_TIME null_rest_of_line
;
vrrpac_track_event_delay_null
:
   TRACK_EVENT_DELAY null_rest_of_line
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

vrrpa_arp_retry_null
:
   ARP_RETRY null_rest_of_line
;
vrrpa_dead_timer_null
:
   DEAD_TIMER null_rest_of_line
;
vrrpa_get_ready_time_acos2_null
:
   GET_READY_TIME_ACOS2 null_rest_of_line
;
vrrpa_hello_interval_null
:
   HELLO_INTERVAL null_rest_of_line
;
vrrpa_preemption_delay_null
:
   PREEMPTION_DELAY null_rest_of_line
;
vrrpa_session_sync_null
:
   SESSION_SYNC null_rest_of_line
;
vrrpa_track_event_delay_null
:
   TRACK_EVENT_DELAY null_rest_of_line
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
  vrrpavi_floating_ip
  | vrrpavi_preempt_mode
  | vrrpavi_blade_parameters
;

vrrpavi_floating_ip: FLOATING_IP ip = ip_address NEWLINE;

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

vrrpa_vrid_lead: VRID_LEAD name = vrid_lead_name NEWLINE vrrpavl*;

vrid_lead_name
:
  // 1-63 chars
  word
;

vrrpavl: vrrpavl_partition;

vrrpavl_partition: PARTITION name = partition_name VRID (DEFAULT | vrid) NEWLINE;

