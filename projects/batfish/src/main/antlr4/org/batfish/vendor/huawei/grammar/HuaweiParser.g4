parser grammar HuaweiParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = HuaweiLexer;
}

// Entry point
huawei_configuration: NEWLINE* statement* EOF;

statement
  : s_sysname
  | s_interface
  | s_bgp
  | s_ospf
  | s_ip
  | s_vlan
  | s_acl
  | s_return
  | s_null
  ;

// Null statement - consume unrecognized lines
s_null: null_rest_of_line;
null_rest_of_line: ~NEWLINE* NEWLINE;

// Return statement (exits configuration blocks)
s_return: RETURN NEWLINE;

// System name
s_sysname: SYSNAME hostname=word NEWLINE;

// Interface configuration
s_interface: INTERFACE name=word NEWLINE interface_statement* s_return;

interface_statement
  : is_description
  | is_ip_address
  | is_shutdown
  | is_undo_shutdown
  | is_null
  ;

is_description: DESCRIPTION (~NEWLINE)+ NEWLINE;

is_ip_address: IP ADDRESS addr=IP_ADDRESS mask=IP_ADDRESS NEWLINE;

is_shutdown: SHUTDOWN NEWLINE;

is_undo_shutdown: UNDO SHUTDOWN NEWLINE;

is_null: ~(NEWLINE | RETURN) ~NEWLINE* NEWLINE;  // Don't match return statements

// IP commands
s_ip: IP si;

si
  : si_route_static
  | si_vpn_instance
  | si_null
  ;

si_vpn_instance: VPN_INSTANCE name=word NEWLINE vpn_statement* s_return;

si_route_static: ROUTE_STATIC dest=IP_ADDRESS mask=IP_ADDRESS nexthop=IP_ADDRESS NEWLINE;

si_null: null_rest_of_line;

// BGP configuration
s_bgp: BGP asn=dec NEWLINE bgp_statement* s_return;

bgp_statement
  : bs_router_id
  | bs_peer
  | bs_network
  | bs_null
  ;

bs_router_id: ROUTER_ID id=IP_ADDRESS NEWLINE;

bs_peer: PEER ip=IP_ADDRESS (AS_NUMBER asn=dec)? NEWLINE;

bs_network: NETWORK ip=IP_ADDRESS mask=IP_ADDRESS NEWLINE;

bs_null: ~(NEWLINE | RETURN) ~NEWLINE* NEWLINE;  // Don't match return statements

// OSPF configuration
s_ospf: OSPF proc=dec NEWLINE ospf_statement* s_return;

ospf_statement
  : os_router_id
  | os_area
  | os_network
  | os_null
  ;

os_router_id: ROUTER_ID id=IP_ADDRESS NEWLINE;

os_area: AREA area_id=dec NEWLINE area_statement*;

area_statement
  : as_network
  | as_null
  ;

os_network: NETWORK ip=IP_ADDRESS mask=IP_ADDRESS NEWLINE;

as_network: NETWORK ip=IP_ADDRESS wildcard=IP_ADDRESS NEWLINE;

os_null: ~(NEWLINE | RETURN) ~NEWLINE* NEWLINE;  // Don't match return statements
as_null: null_rest_of_line;

// VLAN configuration
s_vlan: VLAN (vlan_batch | vlan_id) NEWLINE;

vlan_batch: BATCH vlan_list;

vlan_list: vlan_item (COMMA vlan_item)*;

vlan_item: dec MINUS dec | dec | word;

vlan_id: dec;

// ACL configuration
s_acl: ACL acl_name NEWLINE acl_statement* s_return;

acl_name: word | dec;

acl_statement
  : acls_rule
  | acls_null
  ;

acls_rule: RULE num=dec action=(PERMIT | DENY) word* NEWLINE;

acls_null: ~(NEWLINE | RETURN) ~NEWLINE* NEWLINE;  // Don't match return statements

// VRF/VPN instance statements (shared between si_vpn_instance and s_vpn_instance)
vpn_statement
  : vs_route_distinguisher
  | vs_null
  ;

vs_route_distinguisher: (ROUTE_DISTINGUISHER | ROUTER_ID) word NEWLINE;

vs_null: ~(NEWLINE | RETURN) ~NEWLINE* NEWLINE;  // Don't match return statements

// Common types
word: WORD;

dec: DECIMAL;
