parser grammar Cisco_track;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_track
:
  TRACK name = variable
  (
    track_block
    | track_interface
    | track_ip
    | track_list
  )
;

track_block
:
  NEWLINE track_block_null*
;

track_block_null
:
  TYPE null_rest_of_line track_block_type_null*
;

track_block_type_null
:
  OBJECT null_rest_of_line
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
  IP null_rest_of_line track_ip_null*
;

track_ip_null
:
  (
    DEFAULT
    | DELAY
  ) null_rest_of_line
;

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
