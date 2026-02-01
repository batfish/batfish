parser grammar Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Common types used across multiple grammars

// Access list action
access_list_action
:
   PERMIT
   | DENY
;

// Description line
description_line
:
   DESCRIPTION text = null_rest_of_line
;

// IP address
ip_address: IPV4_ADDRESS_PATTERN;

// IPv6 address
ipv6_address: IPV6_ADDRESS;

// IP prefix
ip_prefix: IPV4_PREFIX_PATTERN;

// IPv6 prefix
ipv6_prefix: IPV6_PREFIX;

// Unsigned 8-bit integer (0-255)
uint8
:
   UINT8
;

// Unsigned 16-bit integer (0-65535)
uint16
:
   UINT8
   | UINT16
;

// Unsigned 32-bit integer (0-4294967295)
uint32
:
   UINT8
   | UINT16
   | UINT32
;

// Decimal number (can be negative)
dec
:
   DEC
;

// Variable (any non-whitespace text)
variable
:
   VARIABLE
;

// Interface name patterns
interface_name
:
   // GigabitEthernet interfaces
   name = GIGABITETHERNET (sub = uint16)? FORWARD_SLASH (card = uint16)? FORWARD_SLASH port = uint16
   |
   // Ethernet interfaces
   name = ETHERNET (sub = uint16)? FORWARD_SLASH (card = uint16)? FORWARD_SLASH port = uint16
   |
   // VLAN interfaces (Vlanif + number, no separator)
   name = VLANIF vlan = uint16
   |
   // Loopback interfaces
   name = LOOPBACK num = uint16
   |
   // Eth-Trunk interfaces
   name = ETH_TRUNK num = uint16
   |
   // Other interface types: match multi-part names (contain /, -, or .)
   // This avoids matching single-word config keywords like "nat", "bgp", "acl", etc.
   name = VARIABLE (FORWARD_SLASH | DASH | PERIOD) VARIABLE
   |
   // Fallback: Any other interface name (including Vlanif + number without space)
   // Use negative lookahead to prevent matching major stanza-starting keywords
   name = VARIABLE
   ;

// Variable for interface names
variable_interface_name
:
   ~(IP_ADDRESS | IP_PREFIX | UINT8 | UINT16 | UINT32 | VARIABLE)+
;

// Null rest of line (consume all tokens until next command)
// Matches tokens that can appear in descriptions or parameter values
// IMPORTANT: Must NOT match tokens that start new stanzas (INTERFACE, IP, ACL, BGP, etc.)
null_rest_of_line
:
   null_token+
;

// Tokens that can appear in values (descriptions, names, etc.)
// This allows SOME keywords to appear within string values
// EXCLUDED: Tokens that start new top-level stanzas (IP, ACL, BGP, OSPF, NAT, VLAN, SYSNAME, RETURN)
// Note: INTERFACE is allowed for descriptions like "Test interface"
null_token
:
   VARIABLE
   | UINT8
   | UINT16
   | UINT32
   | DEC
   | IPV4_ADDRESS_PATTERN
   | IPV6_ADDRESS
   | INTERFACE
   | DESCRIPTION
   | NAME
   | PERMIT
   | DENY
   | TCP
   | UDP
   | ICMP
   | SOURCE
   | DESTINATION
   | PROTOCOL
   | TO
   | AREA
   | ROUTER_ID
   | PEER
   | GROUP
   | NETWORK
   | ROUTE_DISTINGUISHER
   | VPN_TARGET
   | SHUTDOWN
   | ADDRESS
   | MASK
   | DOT1Q
   | TERMINATION
   | VID
   | PORT
   | GLOBAL
   | INSIDE
   | OUTBOUND
   | SERVER
   | ACL_NUMBER
   | ACL_NAME
   | RULE
   | SOURCE_PORT
   | DESTINATION_PORT
   | EQ
   | GT
   | LT
   | RANGE
   | ANY
   | COMMAND
;

// Exit from current configuration mode
exit_line
:
   (RETURN | QUIT | EXIT)
;

// Double-quoted string
double_quoted_string
:
   DOUBLE_QUOTE
   (
      inner_text += ~DOUBLE_QUOTE
   )* DOUBLE_QUOTE
;
