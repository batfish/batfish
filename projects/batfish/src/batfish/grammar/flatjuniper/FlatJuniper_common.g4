parser grammar FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

as_path_expr
:
   (
      items += as_unit
   )+
;

as_set
:
   OPEN_BRACKET
   (
      items += DEC
   )+ CLOSE_BRACKET
;

as_unit
:
   as_set
   | DEC
;

icmp_code
:
   DESTINATION_HOST_UNKNOWN
   | DESTINATION_NETWORK_UNKNOWN
   | FRAGMENTATION_NEEDED
   | HOST_UNREACHABLE
   | NETWORK_UNREACHABLE
   | PORT_UNREACHABLE
;

icmp_type
:
   DESTINATION_UNREACHABLE
   | ECHO_REPLY
   | ECHO_REQUEST
   | NEIGHBOR_ADVERTISEMENT
   | NEIGHBOR_SOLICIT
   | PACKET_TOO_BIG
   | PARAMETER_PROBLEM
   | SOURCE_QUENCH
   | TIME_EXCEEDED
   | UNREACHABLE
;

interface_id
:
   name = VARIABLE
   (
      PERIOD unit = DEC
   )?
;

ip_protocol
:
   AH
   | DEC
   | ESP
   | GRE
   | ICMP
   | ICMPV6
   | IGMP
   | OSPF
   | PIM
   | RSVP
   | STATIC
   | TCP
   | UDP
   | VRRP
;

s_apply_groups
:
   APPLY_GROUPS name = variable
;

s_apply_groups_except
:
   APPLY_GROUPS_EXCEPT name = variable
;

s_description
:
   DESCRIPTION description = M_Description_DESCRIPTION?
;

port
:
   BGP
   | BOOTPC
   | BOOTPS
   | DEC
   | DHCP
   | DOMAIN
   | FTP
   | LDP
   | NTP
   | SNMP
   | SSH
   | TACACS
   | TELNET
;

range
:
   range_list += subrange
   (
      COMMA range_list += subrange
   )*
;

routing_protocol
:
   AGGREGATE
   | BGP
   | DIRECT
   | ISIS
   | OSPF
   | STATIC
;

subrange
:
   low = DEC
   (
      DASH high = DEC
   )?
;

s_null_filler
:
   ~( APPLY_GROUPS | NEWLINE )* s_apply_groups?
;

variable
:
   text = ~( NEWLINE | OPEN_PAREN | OPEN_BRACKET | OPEN_BRACE | WILDCARD )
;
