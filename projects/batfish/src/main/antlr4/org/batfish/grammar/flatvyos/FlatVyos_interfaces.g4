parser grammar FlatVyos_interfaces;

import FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

it_address
:
   ADDRESS
   (
      DHCP
      | IP_PREFIX
   )
;

it_description
:
   description
;

it_null
:
   (
      DUPLEX
      | HW_ID
      | MTU
      | SMP_AFFINITY
      | SPEED
   ) null_filler
;

s_interfaces
:
   INTERFACES interface_type name = variable s_interfaces_tail
;

s_interfaces_tail
:
   it_address
   | it_description
   | it_null
;