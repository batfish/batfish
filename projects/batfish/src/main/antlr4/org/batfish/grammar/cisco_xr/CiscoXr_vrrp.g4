parser grammar CiscoXr_vrrp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

router_vrrp
:
   VRRP NEWLINE
   (
      vrrp_interface
   )*
;

vrrp_interface
:
   NO? INTERFACE iface = interface_name NEWLINE
   (
      vi_address_family
   )* NEWLINE?
;

vi_address_family
:
   NO? ADDRESS_FAMILY IPV4 NEWLINE
   (
      viaf_vrrp
   )*
;

viaf_vrrp
:
   NO? VRRP groupnum = uint_legacy NEWLINE
   (
      viafv_address
      | viafv_null
      | viafv_preempt
      | viafv_priority
   )*
;

viafv_address
:
   ADDRESS address = IP_ADDRESS NEWLINE
;

viafv_null
:
   NO?
   (
      TIMERS
      | TRACK
   ) null_rest_of_line
;

viafv_preempt
:
   PREEMPT
   (
      DELAY delay = uint_legacy
   ) NEWLINE
;

viafv_priority
:
   PRIORITY priority = uint_legacy NEWLINE
;