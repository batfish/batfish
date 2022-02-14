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

track_interface
:
  INTERFACE interface_name
  (
     IP ROUTING
     | LINE_PROTOCOL
  )
  NEWLINE
;

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
 | tiprm_ip_vrf
 | tiprm_threshold
;

tipr_reachability
:
  REACHABILITY NEWLINE
  tiprr_inner*
;

tiprr_inner
:
  track_delay_null
  | tiprr_ip_vrf
;

tiprr_ip_vrf
:
  IP VRF name = variable NEWLINE
;

track_delay_null
:
  DELAY
  (
    UP up = track_delay_number (DOWN down = track_delay_number)?
    | DOWN down = track_delay_number (UP up = track_delay_number)?
  ) NEWLINE
;

track_delay_number
:
  // 0-180 seconds
  uint8
;

tiprm_ip_vrf: IP VRF name = variable NEWLINE;

tiprm_threshold
:
  THRESHOLD
  (
    UP up = uint8 (DOWN down = uint8)?
    | DOWN down = uint8 (UP up = uint8)?
  ) NEWLINE
;

tip_sla
:
  SLA sla_number (REACHABILITY | STATE)? NEWLINE
  tip_sla_inner*
;

tip_sla_inner
:
  track_delay_null
  | tips_default_state
;

tips_default_state: DEFAULT_STATE (UP | DOWN) NEWLINE;

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
  BOOLEAN
  (
    AND
    | OR
  )
  NEWLINE
  tlb_tail*
;

tl_threshold
:
   THRESHOLD
   (
      tlt_percentage
      | tlt_weight
   )
;

tlb_tail
:
  tl_null_tail
  | tl_object_tail
;

tlt_percentage
:
   PERCENTAGE NEWLINE
   (
       tl_null_tail
       | tl_object_tail
       | tlt_null_tail
   )*
;

tlt_weight
:
   WEIGHT NEWLINE
   (
       tl_null_tail
       | tltw_object_tail
       | tlt_null_tail
   )*
;

// common null tail for track list
tl_null_tail
:
  (
     DEFAULT
     | DELAY
  )
  null_rest_of_line
;

// common null tail for track list threshold
tlt_null_tail
:
  THRESHOLD null_rest_of_line
;

tl_object_tail
:
    tl_object NEWLINE
;

tl_object
:
    OBJECT name = variable
;

tltw_object_tail
:
  tl_object WEIGHT dec NEWLINE
;

