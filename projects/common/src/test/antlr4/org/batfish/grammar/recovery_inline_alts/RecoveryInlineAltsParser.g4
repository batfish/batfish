parser grammar RecoveryInlineAltsParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = RecoveryInlineAltsLexer;
}

recovery_inline_alts_configuration
:
  (
    s_interface
    | s_ip
    | s_permit
    | NEWLINE
  )* EOF
;

s_interface
:
  INTERFACE structure_name NEWLINE
  (
    i_ip
    | i_mtu
    | i_permit
  )*
;

i_ip
:
  IP
  (
    iip_address
    | iip_ospf_cost
  )
;

iip_address
:
  ADDRESS ip = ip_address NEWLINE
;

iip_ospf_cost
:
  OSPF COST cost = uint32 NEWLINE
;

i_mtu
:
  MTU mtu = uint32 NEWLINE
;

i_permit
:
  PERMIT
  (
    DNS
    | SSH
  )+ NEWLINE
;

s_ip
:
  IP ip_routing
;

ip_routing
:
  ROUTING NEWLINE
;

s_permit
:
  PERMIT
  (
    DNS
    | SSH
  )+ NEWLINE
;

ip_address
:
  IP_ADDRESS
;

structure_name
:
  WORD
;

uint32
:
  UINT32
;
