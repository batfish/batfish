parser grammar CiscoNxos_ip_access_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ip_access_list
:
  ACCESS_LIST name = ip_access_list_name NEWLINE
  (
    acl_fragments
    | acl_line
    | acl_null
  )*
;

ip_access_list_name
:
// 1-64 characters
  WORD
;

acl_fragments
:
  FRAGMENTS
  (
    DENY_ALL
    | PERMIT_ALL
  ) NEWLINE
;

acl_line
:
  num = uint32?
  (
    acll_action
    | acll_remark
  )
;

acll_action
:
  action = line_action
  (
  // DO NOT REORDER
  // If not using l4 options for l4 protocol, should be considered l3 line.
    aclla_l3
    | aclla_l4
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
  | prot = ip_protocol
;

ip_protocol
:
  num = uint8
  | AHP
  | EIGRP
  | ESP
  | GRE
  | ICMP
  | IGMP
  | NOS
  | OSPF
  | PCP
  | PIM
  | TCP
  | UDP
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
  DSCP
  (
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
  )
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
  PACKET_LENGTH
  (
    EQ eq = packet_length
    | LT lt = packet_length
    | GT gt = packet_length
    | NEQ neq = packet_length
    // NX-OS will flip first and second args if endpoint1 > endpoint2

    | RANGE endpoint1 = packet_length endpoint2 = packet_length
  )
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

aclla_l4
:
  acllal4_icmp
  | acllal4_igmp
  | acllal4_tcp
  | acllal4_udp
;

acllal4_icmp
:
  ICMP acllal3_src_address acllal3_dst_address
  (
    acllal3_option
    // extractor should enforce at most 1 icmp option

    | acllal4icmp_option
  )* NEWLINE
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

acllal4_igmp
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

acllal4_tcp
:
  TCP acllal3_src_address srcport = acllal4tcp_port_spec? acllal3_dst_address
  dstport = acllal4tcp_port_spec?
  (
    acllal3_option
    | acllal4tcp_option
  )* NEWLINE
;

acllal4tcp_port_spec
:
  EQ eq = tcp_port
  | LT lt = tcp_port
  | GT gt = tcp_port
  | NEQ neq = tcp_port
  | PORTGROUP name = WORD
  // NX-OS will flip first and second args if endpoint1 > endpoint2

  | RANGE endpoint1 = tcp_port endpoint2 = tcp_port
;

tcp_port
:
  tcp_port_number
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

tcp_port_number
:
// 0-65535
  UINT8
  | UINT16
  | UINT32
;

acllal4tcp_option
:
  acllal4tcpo_established
  | acllal4tcpo_flags
  | acllal4tcpo_http_method
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
  | TCP_FLAGS_MASK mask = tcp_flags_mask
;

tcp_flags_mask
:
// 0-63
  UINT8
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

acllal4tcpo_tcp_option_length
:
  TCP_OPTION_LENGTH length = tcp_option_length
;

tcp_option_length
:
// 0-40, must be multiple of 4
  UINT8
;

acllal4_udp
:
  UDP acllal3_src_address srcport = acllal4udp_port_spec? acllal3_dst_address
  dstport = acllal4udp_port_spec?
  (
    acllal3_option
    | acllal4udp_option
  )* NEWLINE
;

acllal4udp_port_spec
:
  EQ eq = udp_port
  | LT lt = udp_port
  | GT gt = udp_port
  | NEQ neq = udp_port
  | PORTGROUP name = WORD
  // NX-OS will flip first and second args if endpoint1 > endpoint2

  | RANGE endpoint1 = udp_port endpoint2 = udp_port
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
  UINT8
  | UINT16
  | UINT32
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