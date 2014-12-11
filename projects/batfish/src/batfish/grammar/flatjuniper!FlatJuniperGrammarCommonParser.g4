parser grammar FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

s_null_filler
:
   ~( APPLY_GROUPS | NEWLINE )* s_apply_groups?
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
   | LDP
   | NTP
   | SNMP
   | SSH
   | TACACS
;

protocol
:
   AGGREGATE
   | AH
   | BGP
   | DEC
   | DIRECT
   | ESP
   | GRE
   | ICMP
   | ICMPV6
   | IGMP
   | ISIS
   | OSPF
   | PIM
   | RSVP
   | STATIC
   | TCP
   | UDP
   | VRRP
;

range
:
   range_list += subrange
   (
      COMMA range_list += subrange
   )*
;

subrange
:
   low = DEC
   (
      DASH high = DEC
   )?
;

variable
:
   text = ~( NEWLINE | WILDCARD )
;
