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
  | s_ip_ospf_area
  | s_ip_ospf_network
  | s_ip_route
  | s_router_ospf
  | s_router_id
  | s_no_shutdown
  | s_shutdown
  | s_speed
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


s_ip_ospf_area
:
  IP OSPF WORD AREA WORD NEWLINE
;

s_ip_ospf_network
:
  IP OSPF NETWORK POINT_TO_POINT NEWLINE
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


s_router_ospf
:
  ROUTER OSPF WORD NEWLINE
;

s_router_id
:
  ROUTER_ID WORD NEWLINE
;

s_no_shutdown
:
  NO SHUTDOWN NEWLINE
;

s_shutdown
:
  SHUTDOWN NEWLINE
;

s_speed
:
  SPEED WORD+ NEWLINE
;

null_statement
:
  (ADDRESS | AREA | HOSTNAME | INTERFACE | IP | LOOPBACK | NETWORK | NO | NULLROUTE | OSPF | POINT_TO_POINT | REJECT | ROUTE | ROUTER | ROUTER_ID | SHUTDOWN | SPEED | VLAN | WORD)+ NEWLINE
;
