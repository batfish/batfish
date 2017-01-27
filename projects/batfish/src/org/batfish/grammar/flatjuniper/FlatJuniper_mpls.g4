parser grammar FlatJuniper_mpls;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

c_interface_switch
:
   INTERFACE_SWITCH name = variable
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
   MPLS null_filler
;
