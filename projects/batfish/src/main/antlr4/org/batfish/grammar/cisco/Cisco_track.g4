parser grammar Cisco_track;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_track
:
  TRACK num = track_number
  (
    track_interface
    | track_ip
    | track_list
  )
;

no_track: NO TRACK num = track_number NEWLINE;

track_interface
:
  INTERFACE interface_name
  (
     IP ROUTING
     | LINE_PROTOCOL
  )
  NEWLINE
  track_interface_inner*
;

track_interface_inner
:
  tii_carrier_delay_null
  | no_tii_carrier_delay_null
  | track_delay_null
  | no_track_delay_null
;

tii_carrier_delay_null: CARRIER_DELAY null_rest_of_line;

no_tii_carrier_delay_null: NO tii_carrier_delay_null;

track_ip
:
  IP (tip_route | tip_sla)
;

tip_route
:
  ROUTE
  (
    address = ip_address mask = ip_address
    | prefix = ip_prefix
  )
  (
    tipr_metric
    | tipr_reachability
  )
;

tipr_metric
:
  METRIC THRESHOLD NEWLINE
  tipr_metric_inner*
;

tipr_metric_inner
:
  track_delay_null
  | no_track_delay_null
  | tiprm_ip_vrf
  | no_tiprm_ip_vrf
  | tiprm_threshold_null
  | no_tiprm_threshold_null
;

tipr_reachability
:
  REACHABILITY NEWLINE
  tiprr_inner*
;

tiprr_inner
:
  track_delay_null
  | no_track_delay_null
  | tiprr_ip_vrf
  | no_tiprr_ip_vrf
;

tiprr_ip_vrf: IP VRF name = variable NEWLINE;

no_tiprr_ip_vrf: NO IP VRF name = variable NEWLINE;

track_delay_null
:
  DELAY
  (
    UP up = track_delay_number (DOWN down = track_delay_number)?
    | DOWN down = track_delay_number (UP up = track_delay_number)?
  ) NEWLINE
;

no_track_delay_null: NO track_delay_null;

track_delay_number
:
  // 0-180 seconds
  uint8
;

tiprm_ip_vrf: IP VRF name = variable NEWLINE;

no_tiprm_ip_vrf: NO IP VRF name = variable NEWLINE;

tiprm_threshold_null
:
  THRESHOLD
  (
    UP up = uint8 (DOWN down = uint8)?
    | DOWN down = uint8 (UP up = uint8)?
  ) NEWLINE
;

no_tiprm_threshold_null: NO tiprm_threshold_null;

tip_sla
:
  // STATE is default
  SLA num = sla_number (REACHABILITY | STATE)? NEWLINE
  tip_sla_inner*
;

tip_sla_inner
:
  track_delay_null
  | no_track_delay_null
  | tips_default_state
  | no_tips_default_state
;

tips_default_state: DEFAULT_STATE (UP | DOWN) NEWLINE;

no_tips_default_state: NO DEFAULT_STATE (UP | DOWN) NEWLINE;

track_list
:
  LIST
  (
     tl_boolean
     | tl_threshold
  )
;

tl_boolean
:
  BOOLEAN (AND | OR) NEWLINE
  tlb_inner*
;

tlb_inner
:
  track_delay_null
  | no_track_delay_null
  | tlb_object
  | no_tlb_object
;

tlb_object: OBJECT num = track_number NOT? NEWLINE;

no_tlb_object: NO OBJECT num = track_number NOT? NEWLINE;

tl_threshold
:
   THRESHOLD
   (
      tlt_percentage
      | tlt_weight
   )
;

tlt_percentage
:
   PERCENTAGE NEWLINE
   tltp_inner*
;

tltp_inner
:
  (
    track_delay_null
    | no_track_delay_null
    | tltp_object
    | no_tltp_object
    | tltp_threshold_null
    | no_tltp_threshold_null
  )
;

tltp_object: OBJECT num = track_number NEWLINE;

no_tltp_object: NO OBJECT num = track_number NEWLINE;

tltp_threshold_null
:
  THRESHOLD PERCENTAGE
  (
    // up percentage must be greater than down percentage
    DOWN down = percent (UP up = percent)?
    | UP up = percent (DOWN down = percent)?
  ) NEWLINE
;

no_tltp_threshold_null: NO tltp_threshold_null;

tlt_weight
:
  WEIGHT NEWLINE
  tltw_inner*
;

tltw_inner
:
    track_delay_null
    | no_track_delay_null
    | tltw_object
    | no_tltw_object
    | tltw_threshold_null
    | no_tltw_threshold_null
;

tltw_object: OBJECT num = track_number (WEIGHT weight = pint8)? NEWLINE;

no_tltw_object: NO OBJECT num = track_number (WEIGHT weight = pint8)? NEWLINE;

tltw_threshold_null
:
  THRESHOLD WEIGHT
  (
    // up weight must be greater than down weight
    DOWN down = uint8 (UP up = pint8)?
    | UP up = pint8 (DOWN down = uint8)?
  ) NEWLINE
;

no_tltw_threshold_null: NO tltw_threshold_null;

percent
:
  // 0-100
  uint8
;
