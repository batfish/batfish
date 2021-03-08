parser grammar Cisco_hsrp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

router_hsrp_stanza
:
   ROUTER HSRP NEWLINE router_hsrp_if+
;

router_hsrp_if
:
   INTERFACE interface_name NEWLINE router_hsrp_if_af+
;

router_hsrp_if_af
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   ) NEWLINE HSRP dec? NEWLINE router_hsrp_if_af_tail+
;

router_hsrp_if_af_tail
:
   (
      AUTHENTICATION
      | ADDRESS
      | PREEMPT
      | PRIORITY
      | TIMERS
      | TRACK OBJECT
      | VERSION dec
   ) null_rest_of_line
;

