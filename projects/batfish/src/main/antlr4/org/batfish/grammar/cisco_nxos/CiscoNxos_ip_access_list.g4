parser grammar CiscoNxos_ip_access_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ip_access_list
:
  ACCESS_LIST name = ip_access_list_name NEWLINE
  ip_access_list_inner*
;

ip_access_list_inner
:
  acl_fragments
  | acl_line
  | acl_null
;

acl_fragments
:
  FRAGMENTS
  (
    deny = DENY_ALL
    | permit = PERMIT_ALL
  ) NEWLINE
;

acl_line
:
  num = ip_access_list_line_number?
  (
    acll_action
    | acll_remark
  )
;

ip_access_list_line_number
:
// 1-4294967295
  UINT8
  | UINT16
  | UINT32
;

acll_action
:
  action = line_action
  (
    aclla_icmp
    | aclla_igmp
    | aclla_tcp
    | aclla_udp
    | aclla_l3
  )
;

aclla_l3
:
  acllal3_protocol_spec acllal3_src_address acllal3_dst_address
  (
    acllal3_fragments
    | acllal3_option
  )* NEWLINE
;

acllal3_protocol_spec
:
  IP
  | prot = acl_l3_protocol
;

acl_l3_protocol
:
  num = uint8
  | AHP
  | EIGRP
  | ESP
  | GRE
  | NOS
  | OSPF
  | PCP
  | PIM
;

acllal3_src_address
:
  addr = acllal3_address_spec
;

acllal3_dst_address
:
  addr = acllal3_address_spec
;

acllal3_address_spec
:
  address = ip_address wildcard = ip_address
  | prefix = ip_prefix
  | ADDRGROUP group = WORD
  | ANY
  | HOST host = ip_address
;

acllal3_fragments
:
  FRAGMENTS
;

acllal3_option
:
  acllal3o_dscp
  | acllal3o_log
  | acllal3o_packet_length
  | acllal3o_precedence
  | acllal3o_ttl
;

acllal3o_dscp
:
  DSCP dscp = dscp_spec
;

dscp_spec
:
  num = dscp_number
  | AF11
  | AF12
  | AF13
  | AF21
  | AF22
  | AF23
  | AF31
  | AF32
  | AF33
  | AF41
  | AF42
  | AF43
  | CS1
  | CS2
  | CS3
  | CS4
  | CS5
  | CS6
  | CS7
  | DEFAULT
  | EF
;

dscp_number
:
// 0-63
  UINT8
;

acllal3o_log
:
  LOG
;

acllal3o_packet_length
:
  PACKET_LENGTH spec = acllal3o_packet_length_spec
;

acllal3o_packet_length_spec
:
  eq = EQ arg1 = packet_length
  | lt = LT arg1 = packet_length
  | gt = GT arg1 = packet_length
  | neq = NEQ arg1 = packet_length
  // NX-OS will flip first and second args if arg1 > arg2

  | range = RANGE arg1 = packet_length arg2 = packet_length
;

acllal3o_precedence
:
  PRECEDENCE
  (
    num = precedence_number
    | CRITICAL
    | FLASH
    | FLASH_OVERRIDE
    | IMMEDIATE
    | INTERNET
    | NETWORK
    | PRIORITY
    | ROUTINE
  )
;

precedence_number
:
// 0-7
  UINT8
;

acllal3o_ttl
:
  TTL num = uint8
;

packet_length
:
// 20-9210
  UINT8
  | UINT16
;

aclla_icmp
:
  ICMP acllal3_src_address acllal3_dst_address
  // NX-OS allows multiple l3 options but at most l4 option, interleaved in any order.
  acllal3_option* acllal4icmp_option? acllal3_option*
  NEWLINE
;

acllal4icmp_option
:
  (
    type = uint8 code = uint8?
  )
  | ADMINISTRATIVELY_PROHIBITED
  | ALTERNATE_ADDRESS
  | CONVERSION_ERROR
  | DOD_HOST_PROHIBITED
  | DOD_NET_PROHIBITED
  | ECHO
  | ECHO_REPLY
  | GENERAL_PARAMETER_PROBLEM
  | HOST_ISOLATED
  | HOST_PRECEDENCE_UNREACHABLE
  | HOST_REDIRECT
  | HOST_TOS_REDIRECT
  | HOST_TOS_UNREACHABLE
  | HOST_UNKNOWN
  | HOST_UNREACHABLE
  | INFORMATION_REPLY
  | INFORMATION_REQUEST
  | MASK_REPLY
  | MASK_REQUEST
  | MOBILE_REDIRECT
  | NET_REDIRECT
  | NET_TOS_REDIRECT
  | NET_TOS_UNREACHABLE
  | NET_UNREACHABLE
  | NETWORK_UNKNOWN
  | NO_ROOM_FOR_OPTION
  | OPTION_MISSING
  | PACKET_TOO_BIG
  | PARAMETER_PROBLEM
  | PORT_UNREACHABLE
  | PRECEDENCE_UNREACHABLE
  | PROTOCOL_UNREACHABLE
  | REASSEMBLY_TIMEOUT
  | REDIRECT
  | ROUTER_ADVERTISEMENT
  | ROUTER_SOLICITATION
  | SOURCE_QUENCH
  | SOURCE_ROUTE_FAILED
  | TIME_EXCEEDED
  | TIMESTAMP_REPLY
  | TIMESTAMP_REQUEST
  | TRACEROUTE
  | TTL_EXCEEDED
  | UNREACHABLE
;

aclla_igmp
:
  IGMP acllal3_src_address acllal3_dst_address
  (
    acllal3_option
    | acllal4igmp_option
  )* NEWLINE
;

acllal4igmp_option
:
  num = igmp_message_type_number
  | DVMRP
  | HOST_QUERY
  | HOST_REPORT
  | PIM
;

igmp_message_type_number
:
// 0-16
  UINT8
;

aclla_tcp
:
  TCP acllal3_src_address acllal4tcp_source_port? acllal3_dst_address
  acllal4tcp_destination_port?
  (
    acllal3_option
    | acllal4tcp_option
  )* NEWLINE
;

acllal4tcp_source_port
:
  port = acllal4tcp_port_spec
;

acllal4tcp_destination_port
:
  port = acllal4tcp_port_spec
;

acllal4tcp_port_spec
:
  literal = acllal4tcp_port_spec_literal
  | group = acllal4tcp_port_spec_port_group
;

acllal4tcp_port_spec_literal
:
  eq = EQ arg1 = tcp_port
  | lt = LT arg1 = tcp_port
  | gt = GT arg1 = tcp_port
  | neq = NEQ arg1 = tcp_port
  // NX-OS will flip first and second args if arg1 > arg2

  | range = RANGE arg1 = tcp_port arg2 = tcp_port
;

acllal4tcp_port_spec_port_group
:
  PORTGROUP name = WORD
;

tcp_port
:
  num = tcp_port_number
  | BGP
  | CHARGEN
  | CMD
  | DAYTIME
  | DISCARD
  | DOMAIN
  | DRIP
  | ECHO
  | EXEC
  | FINGER
  | FTP
  | FTP_DATA
  | GOPHER
  | HOSTNAME
  | IDENT
  | IRC
  | KLOGIN
  | KSHELL
  | LOGIN
  | LPD
  | NNTP
  | PIM_AUTO_RP
  | POP2
  | POP3
  | SMTP
  | SUNRPC
  | TACACS
  | TALK
  | TELNET
  | TIME
  | UUCP
  | WHOIS
  | WWW
;

acllal4tcp_option
:
  acllal4tcpo_established
  | acllal4tcpo_flags
  | acllal4tcpo_http_method
  | acllal4tcpo_tcp_flags_mask
  | acllal4tcpo_tcp_option_length
;

acllal4tcpo_established
:
  ESTABLISHED
;

acllal4tcpo_flags
:
  ACK
  | FIN
  | PSH
  | RST
  | SYN
  | URG
;

acllal4tcpo_http_method
:
  HTTP_METHOD
  (
    num = http_method_number
    | CONNECT
    | DELETE
    | GET
    | HEAD
    | POST
    | PUT
    | TRACE
  )
;

http_method_number
:
// 1-7
  UINT8
;

acllal4tcpo_tcp_flags_mask
:
  TCP_FLAGS_MASK mask = tcp_flags_mask
;

tcp_flags_mask
:
// 0-63
  UINT8
;

acllal4tcpo_tcp_option_length
:
  TCP_OPTION_LENGTH length = tcp_option_length
;

tcp_option_length
:
// 0-40, must be multiple of 4
  UINT8
;

aclla_udp
:
  UDP acllal3_src_address acllal4udp_source_port? acllal3_dst_address
  acllal4udp_destination_port?
  (
    acllal3_option
    | acllal4udp_option
  )* NEWLINE
;

acllal4udp_source_port
:
  port = acllal4udp_port_spec
;

acllal4udp_destination_port
:
  port = acllal4udp_port_spec
;

acllal4udp_port_spec
:
  literal = acllal4udp_port_spec_literal
  | group = acllal4udp_port_spec_port_group
;

acllal4udp_port_spec_literal
:
  eq = EQ arg1 = udp_port
  | lt = LT arg1 = udp_port
  | gt = GT arg1 = udp_port
  | neq = NEQ arg1 = udp_port
  // NX-OS will flip first and second args if arg1 > arg2

  | range = RANGE arg1 = udp_port arg2 = udp_port
;

acllal4udp_port_spec_port_group
:
  PORTGROUP name = WORD
;

udp_port
:
  num = udp_port_number
  | BIFF
  | BOOTPC
  | BOOTPS
  | DISCARD
  | DNSIX
  | DOMAIN
  | ECHO
  | ISAKMP
  | MOBILE_IP
  | NAMESERVER
  | NETBIOS_DGM
  | NETBIOS_NS
  | NETBIOS_SS
  | NON500_ISAKMP
  | NTP
  | PIM_AUTO_RP
  | RIP
  | SNMP
  | SNMPTRAP
  | SUNRPC
  | SYSLOG
  | TACACS
  | TALK
  | TFTP
  | TIME
  | WHO
  | XDMCP
;

udp_port_number
:
// 0-65535
  uint16
;

acllal4udp_option
:
  acllal4udpo_nve
;

acllal4udpo_nve
:
  NVE VNI vni_number
;

acll_remark
:
  REMARK text = REMARK_TEXT NEWLINE
;

acl_null
:
  NO?
  (
    IGNORE
    | STATISTICS
  ) null_rest_of_line
;