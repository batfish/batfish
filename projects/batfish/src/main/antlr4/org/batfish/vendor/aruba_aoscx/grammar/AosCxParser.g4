parser grammar AosCxParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = AosCxLexer;
}

aoscx_configuration
:
  (statement | NEWLINE)* EOF
;

statement
:
  s_hostname
  | s_interface
  | s_ip_address
  | s_ip_route
  | s_no_shutdown
  | s_shutdown
  | null_statement
;

s_hostname
:
  HOSTNAME WORD NEWLINE
;

s_interface
:
  INTERFACE interface_name NEWLINE
;

interface_name
:
  LOOPBACK WORD
  | VLAN WORD
  | WORD
;

s_ip_address
:
  IP ADDRESS WORD NEWLINE
;

s_ip_route
:
  IP ROUTE WORD static_route_next_hop NEWLINE
;

static_route_next_hop
:
  NULLROUTE
  | REJECT
  | WORD
;

s_no_shutdown
:
  NO SHUTDOWN NEWLINE
;

s_shutdown
:
  SHUTDOWN NEWLINE
;

null_statement
:
  (ADDRESS | HOSTNAME | INTERFACE | IP | LOOPBACK | NO | NULLROUTE | REJECT | ROUTE | SHUTDOWN | VLAN | WORD)+ NEWLINE
;
