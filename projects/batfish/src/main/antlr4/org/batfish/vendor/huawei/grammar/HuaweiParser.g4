parser grammar HuaweiParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = HuaweiLexer;
}

// Entry point
huawei_configuration: NEWLINE* statement* EOF;

statement
  : s_acl
  | s_bgp
  | s_interface
  | s_ip
  | s_ospf
  | s_sysname
  | s_vlan
  | quit_line
  | return_line
  | s_null
  ;

// Null statement - consume unrecognized lines
s_null: null_rest_of_line;
null_rest_of_line: ~NEWLINE* NEWLINE;

// Return statement (exits configuration blocks)
return_line: RETURN NEWLINE;
quit_line: QUIT NEWLINE;
block_exit_line: return_line | quit_line;

// System name
s_sysname: SYSNAME host_name=hostname NEWLINE;

// Interface configuration
s_interface: INTERFACE name=interface_name NEWLINE interface_statement* block_exit_line?;

interface_statement
  : is_description
  | is_ip_address
  | is_shutdown
  | is_null
  ;

is_description: DESCRIPTION (~NEWLINE)+ NEWLINE;

is_ip_address: IP ADDRESS addr=ip_address mask=ip_address NEWLINE;

is_shutdown: UNDO? SHUTDOWN NEWLINE;

is_null:
  ~(NEWLINE | RETURN | QUIT | ACL | BGP | INTERFACE | IP | OSPF | SYSNAME | VLAN)
  ~NEWLINE* NEWLINE; // Don't match block exits or top-level statements

// IP commands
s_ip: IP si;

si
  : si_route_static
  | si_vpn_instance
  | si_null
  ;

si_vpn_instance: VPN_INSTANCE name=vrf_name NEWLINE vpn_statement* block_exit_line?;

si_route_static:
  ROUTE_STATIC dest=ip_address mask=ip_address nexthop=ip_next_hop NEWLINE;

si_null:
  ~(NEWLINE | RETURN | QUIT | ACL | BGP | INTERFACE | IP | OSPF | SYSNAME | VLAN)
  ~NEWLINE* NEWLINE; // Don't match block exits or top-level statements

// BGP configuration
s_bgp: BGP asn=bgp_asn NEWLINE bgp_statement* block_exit_line?;

bgp_statement
  : bs_router_id
  | bs_peer
  | bs_network
  | bs_null
  ;

bs_router_id: ROUTER_ID id=router_id NEWLINE;

bs_peer: PEER ip=ip_address (AS_NUMBER asn=bgp_peer_asn)? NEWLINE;

bs_network: NETWORK ip=ip_prefix_ip mask=ip_prefix_mask NEWLINE;

bs_null:
  ~(NEWLINE | RETURN | QUIT | ACL | BGP | INTERFACE | IP | OSPF | SYSNAME | VLAN)
  ~NEWLINE* NEWLINE; // Don't match block exits or top-level statements

// OSPF configuration
s_ospf: OSPF proc=ospf_process_id NEWLINE ospf_statement* block_exit_line?;

ospf_statement
  : os_router_id
  | os_area
  | os_network
  | os_null
  ;

os_router_id: ROUTER_ID id=router_id NEWLINE;

os_area: AREA area_id=ospf_area_id NEWLINE area_statement*;

area_statement
  : as_network
  | as_null
  ;

os_network: NETWORK ip=ip_prefix_ip mask=ip_prefix_mask NEWLINE;

as_network: NETWORK ip=ip_prefix_ip wildcard=ip_wildcard NEWLINE;

os_null:
  ~(NEWLINE | RETURN | QUIT | ACL | BGP | INTERFACE | IP | OSPF | SYSNAME | VLAN)
  ~NEWLINE* NEWLINE; // Don't match block exits or top-level statements
as_null:
  ~(NEWLINE | RETURN | QUIT | ACL | BGP | INTERFACE | IP | OSPF | SYSNAME | VLAN)
  ~NEWLINE* NEWLINE; // Don't match block exits or top-level statements

// VLAN configuration
s_vlan: VLAN (vlan_batch | vlan_id) NEWLINE;

vlan_batch: BATCH vlan_list;

vlan_list: vlan_item (COMMA vlan_item)*;

vlan_item: uint16 MINUS uint16 | uint16 | word;

vlan_id: uint16;

// ACL configuration
s_acl: ACL acl_name NEWLINE acl_statement* block_exit_line?;

acl_name: word | uint32;

acl_statement
  : acls_rule
  | acls_null
  ;

acls_rule: RULE num=acl_rule_number action=(PERMIT | DENY) word* NEWLINE;

acls_null:
  ~(NEWLINE | RETURN | QUIT | ACL | BGP | INTERFACE | IP | OSPF | SYSNAME | VLAN)
  ~NEWLINE* NEWLINE; // Don't match block exits or top-level statements

// VRF/VPN instance statements (shared between si_vpn_instance and s_vpn_instance)
vpn_statement
  : vs_route_distinguisher
  | vs_null
  ;

vs_route_distinguisher: (ROUTE_DISTINGUISHER | ROUTER_ID) route_distinguisher NEWLINE;

vs_null:
  ~(NEWLINE | RETURN | QUIT | ACL | BGP | INTERFACE | IP | OSPF | SYSNAME | VLAN)
  ~NEWLINE* NEWLINE; // Don't match block exits or top-level statements

// Common types
interface_name: word;
hostname: word;
vrf_name: word;
route_distinguisher: word;
router_id: ip_address;
ip_next_hop: ip_address;
ip_prefix_ip: ip_address;
ip_prefix_mask: ip_address;
ip_wildcard: ip_address;
bgp_asn: uint32;
bgp_peer_asn: uint32;
ospf_process_id: uint16;
ospf_area_id: uint32;
acl_rule_number: uint32;

word: WORD;
ip_address: IP_ADDRESS;

uint8: UINT8;
uint16: UINT8 | UINT16;
uint32: UINT8 | UINT16 | UINT32;
