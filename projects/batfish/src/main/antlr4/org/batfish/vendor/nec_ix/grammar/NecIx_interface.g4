parser grammar NecIx_interface;

options {
  tokenVocab = NecIxLexer;
}

s_interface
:
  INTERFACE name = variable NEWLINE if_inner*
;

if_inner
:
  INDENT
  (
    if_bridge_group
    | if_description
    | if_encapsulation
    | if_ip
    | if_no
    | if_null
    | if_shutdown
  )
;

if_bridge_group
:
  BRIDGE_GROUP group = uint
  (
    NEWLINE
    // permanent-address, port-protected, port-table-size: forwarding details of the bridge.
    | ~NEWLINE null_rest_of_line
  )
;

if_description
:
  DESCRIPTION description = text? NEWLINE
;

if_encapsulation
:
  ENCAPSULATION
  (
    ife_dot1q
    | ife_null
  )
;

ife_dot1q
:
  DOT1Q vlan = uint
  (
    TPID tpid = variable
  )? NEWLINE
;

ife_null
:
  // ppp, pppoe, ...
  ~( DOT1Q | NEWLINE ) null_rest_of_line
;

if_ip
:
  IP
  (
    ifip_address
    | ifip_filter
    | ifip_mtu
    | ifip_napt
    | ifip_nat
    | ifip_null
    | ifip_vrf
  )
;

ifip_address
:
  ADDRESS
  (
    address = IP_PREFIX SECONDARY? NEWLINE
    // dhcp, ipcp, map-e, v6pv: addresses learned at run time.
    | ifipa_dynamic
  )
;

ifipa_dynamic
:
  ~( IP_PREFIX | NEWLINE ) null_rest_of_line
;

ifip_filter
:
  FILTER name = variable seq = uint direction = ( IN | OUT ) SUPPRESS_LOGGING? NEWLINE
;

ifip_mtu
:
  MTU mtu = uint NEWLINE
;

ifip_napt
:
  NAPT
  (
    ifipnapt_address
    | ifipnapt_enable
    | ifipnapt_inside
    | ifipnapt_port_range
    | ifipnapt_translation_null
    | ifipnapt_unimplemented
  )
;

ifipnapt_address
:
  ADDRESS
  (
    ip = IP_ADDRESS
    | iface = WORD
  ) NEWLINE
;

ifipnapt_enable
:
  ENABLE NEWLINE
;

ifipnapt_inside
:
  INSIDE LIST acl = variable
  (
    OUTSIDE outside = IP_ADDRESS
  )? NEWLINE
;

ifipnapt_port_range
:
  TRANSLATION PORT_RANGE ( ranges += ~NEWLINE )+ NEWLINE
;

// Session table sizes and timeouts.
ifipnapt_translation_null
:
  TRANSLATION ~( PORT_RANGE | NEWLINE ) null_rest_of_line
;

// Port forwarding (`service`, `static`), hairpinning, ALGs, ...
ifipnapt_unimplemented
:
  ~( ADDRESS | ENABLE | INSIDE | TRANSLATION | NEWLINE ) null_rest_of_line
;

ifip_nat
:
  NAT
  (
    ifipnat_enable
    | ifipnat_static
    | ifipnat_null
  )
;

ifipnat_enable
:
  ENABLE NEWLINE
;

ifipnat_static
:
  STATIC
  (
    inside = IP_ADDRESS outside = IP_ADDRESS
    | NETWORK inside_prefix = IP_PREFIX outside_prefix = IP_PREFIX
  ) NEWLINE
;

ifipnat_null
:
  ~( ENABLE | STATIC | NEWLINE ) null_rest_of_line
;

ifip_null
:
  (
    DHCP // dhcp binding: DHCP server
    | TCP // tcp adjust-mss
  ) null_rest_of_line
;

ifip_vrf
:
  VRF FORWARDING name = variable NEWLINE
;

if_no
:
  NO
  (
    ifno_ip_address
    | ifno_null
    | ifno_shutdown
  )
;

ifno_ip_address
:
  IP ADDRESS NEWLINE
;

ifno_null
:
  (
    AUTO_CONNECT
    | IPV6
    | KEEPALIVE
  ) null_rest_of_line
;

ifno_shutdown
:
  SHUTDOWN NEWLINE
;

if_null
:
  (
    AUTO_CONNECT
    | BRIDGE // bridge ip tcp adjust-mss
    | IKEV2
    | IPV6
    | KEEPALIVE
    | PPP
    | SERVICE_POLICY
    | TUNNEL // tunnel mode/source/destination: the tunnel endpoints are IPv6
  ) null_rest_of_line
;

if_shutdown
:
  SHUTDOWN NEWLINE
;
