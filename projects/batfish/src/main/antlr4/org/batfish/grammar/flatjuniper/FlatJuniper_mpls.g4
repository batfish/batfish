parser grammar FlatJuniper_mpls;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

c_interface_switch
:
   INTERFACE_SWITCH name = junos_name
   (
      ci_interface
   )
;

ci_interface
:
   INTERFACE interface_id
;

p_connections
:
   CONNECTIONS
   (
      c_interface_switch
   )
;

p_mpls
:
   MPLS
   (
       mpls_interface
   )
;

mpls_interface
:
   INTERFACE name = interface_id
   (
      apply
      | mplsi_srlg
   )
;

mplsi_srlg
:
   SRLG name = junos_name
;