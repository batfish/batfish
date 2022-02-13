parser grammar CiscoXr_hsrp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

//0-255 (v1) or 0-4095 (v2) depending on version
hsrp_group_num: uint16;

router_hsrp
:
   HSRP NEWLINE
   hsrp_if*
;

hsrp_if
:
   INTERFACE name = interface_name NEWLINE
   hsrp_if_af*
;

hsrp_if_af
:
   ADDRESS_FAMILY hsrp_if_af4
;

hsrp_if_af4
:
   IPV4 NEWLINE
   hsrp4_hsrp*
;

hsrp4_hsrp
:
   HSRP group_num=hsrp_group_num
   (
     hsrp4_hsrp_block
     // Single line hsrp group config below this.
     | hsrp4_hsrp_authentication_null
     | hsrp4_hsrp_address
     | hsrp4_hsrp_bfd_null
     | hsrp4_hsrp_preempt
     | hsrp4_hsrp_priority
     | hsrp4_hsrp_timers_null
     | hsrp4_hsrp_track
     | hsrp4_hsrp_version_null
   )
;

hsrp4_hsrp_block: NEWLINE hsrp4_hsrp_inner*;

hsrp4_hsrp_inner
:
   hsrp4_hsrp_authentication_null
   | hsrp4_hsrp_address
   | hsrp4_hsrp_bfd_null
   | hsrp4_hsrp_preempt
   | hsrp4_hsrp_priority
   | hsrp4_hsrp_timers_null
   | hsrp4_hsrp_track
   | hsrp4_hsrp_version_null
;

hsrp4_hsrp_authentication_null: AUTHENTICATION null_rest_of_line;
hsrp4_hsrp_address: ADDRESS addr = IP_ADDRESS NEWLINE;
hsrp4_hsrp_bfd_null: BFD null_rest_of_line;
hsrp4_hsrp_preempt: PREEMPT NEWLINE;
hsrp4_hsrp_priority: PRIORITY priority = uint8 NEWLINE;
hsrp4_hsrp_timers_null: TIMERS null_rest_of_line;
hsrp4_hsrp_track: TRACK name = interface_name (decrement_priority = uint8)? NEWLINE;
hsrp4_hsrp_version_null: VERSION null_rest_of_line;
