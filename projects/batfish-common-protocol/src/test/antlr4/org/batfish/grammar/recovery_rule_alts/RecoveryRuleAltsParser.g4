parser grammar RecoveryRuleAltsParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = RecoveryRuleAltsLexer;
}

recovery_rule_alts_configuration
:
  (
    statement
    | NEWLINE
  )* EOF
;

statement
:
  s_interface
  | s_ip
  | s_permit
;

s_interface
:
  INTERFACE structure_name NEWLINE si*
;

si
:
  i_ip
  | i_mtu
  | i_permit
;

i_ip
:
  IP iip
;

iip
:
  iip_address
  | iip_ospf_cost
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
  PERMIT protocol+ NEWLINE
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
  PERMIT protocol+ NEWLINE
;

protocol
:
  DNS
  | SSH
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
