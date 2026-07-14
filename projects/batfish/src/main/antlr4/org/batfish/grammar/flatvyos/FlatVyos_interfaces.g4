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

it_duplex_null
:
   DUPLEX null_filler
;
it_hw_id_null
:
   HW_ID null_filler
;
it_mtu_null
:
   MTU null_filler
;
it_smp_affinity_null
:
   SMP_AFFINITY null_filler
;
it_speed_null
:
   SPEED null_filler
;

s_interfaces
:
   INTERFACES interface_type name = variable s_interfaces_tail
;

s_interfaces_tail
:
   it_address
   | it_description
   | it_duplex_null
   | it_hw_id_null
   | it_mtu_null
   | it_smp_affinity_null
   | it_speed_null
;