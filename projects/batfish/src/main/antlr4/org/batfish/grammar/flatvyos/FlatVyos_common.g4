parser grammar FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

interface_type
:
   BONDING
   | BRIDGE
   | DUMMY
   | ETHERNET
   | INPUT
   | L2TPV3
   | LOOPBACK
   | OPENVPN
   | PSEUDO_ETHERNET
   | TUNNEL
   | VTI
   | VXLAN
   | WIRELESS
   | WIRELESSMODEM
;

line_action
:
   DENY
   | PERMIT
;

description
:
   DESCRIPTION text = DESCRIPTION_TEXT?
;

null_filler
:
   ~NEWLINE*
;

variable
:
   text = ~( NEWLINE | OPEN_PAREN | OPEN_BRACE )
;
