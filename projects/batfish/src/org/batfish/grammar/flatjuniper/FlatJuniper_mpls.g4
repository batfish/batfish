parser grammar FlatJuniper_mpls;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

connist_interface
:
   INTERFACE interface_id
;

connt_interface_switch
:
   INTERFACE_SWITCH name = variable connt_interface_switch_tail
;

connt_interface_switch_tail
:
   connist_interface
;

s_protocols_connections
:
   CONNECTIONS s_protocols_connections_tail
;

s_protocols_connections_tail
:
   connt_interface_switch
;

s_protocols_mpls
:
   MPLS s_null_filler
;
