parser grammar CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

//// validated grammar

as_path_regex: SINGLE_QUOTE AS_PATH_REGEX SINGLE_QUOTE;

comparator
:
  EQ
  | GE
  | IS
  | LE
;

// structure names
access_list_name: WORD;
as_path_set_name: WORD;
community_set_name: WORD;
flow_exporter_map_name: WORD;
flow_monitor_map_name: WORD;
parameter: PARAMETER;
policy_map_name: WORD;
rd_set_name: WORD;
route_policy_name: WORD;
sampler_map_name: WORD;
usergroup_name: WORD;
vrf_name: WORD;

// numbers
as_number
:
  // 1-4294967295
  uint32
  | hi = uint16 PERIOD lo = uint16
;

as_path_length
:
  // 0-2047
  uint16
;

uint8
:
  UINT8
;

uint16
:
  UINT8
  | UINT16
;

uint32
:
  UINT8
  | UINT16
  | UINT32
;

uint64
:
  UINT8
  | UINT16
  | UINT32
  | UINT64
;

uint_big
:
  UINT8
  | UINT16
  | UINT32
  | UINT64
  | UINT_BIG
;

// TODO: replace all references with one of above rules and remove this rule
uint_legacy
:
  UINT8
  | UINT16
  | UINT32
  | UINT64
  | UINT_BIG
;

//// old/non-validated grammar

access_list_action
:
   PERMIT
   | DENY
;

address_family_footer
:
   (
      (
         EXIT_ADDRESS_FAMILY
         | EXIT
      ) NEWLINE
   )?
;

asn_dotted
:
  uint_legacy PERIOD uint_legacy
;

bgp_asn
:
    asn = uint_legacy
    | asn4b = asn_dotted
;

literal_community
:
   ACCEPT_OWN
   | GRACEFUL_SHUTDOWN
   | INTERNET
   | LOCAL_AS
   | NO_ADVERTISE
   | NO_EXPORT
   | hi = uint16 COLON lo = uint16
;

description_line
:
   DESCRIPTION text = RAW_TEXT? NEWLINE
;

double_quoted_string
:
   DOUBLE_QUOTE
   (
      inner_text += ~DOUBLE_QUOTE
   )* DOUBLE_QUOTE
;

dscp_type
:
   dscp_num
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

dscp_num
:
  // 0-63
  uint8
;

ec_literal
:
   uint_legacy COLON uint_legacy
;

eigrp_metric
:
   bw_kbps = uint_legacy delay_10us = uint_legacy reliability = uint_legacy eff_bw = uint_legacy mtu = uint_legacy
;

exit_line
:
   EXIT NEWLINE
;

extended_community
:
   ec_literal
;

hash_comment
:
   POUND RAW_TEXT
;

int_expr
:
   (
      (
         PLUS
         | DASH
      )? uint_legacy
   )
   | IGP_COST
   | RP_VARIABLE
;

interface_name
:
  name_prefix_alpha = M_Interface_PREFIX
  (
    (
      (
        name_middle_parts += M_Interface_PREFIX
      )? name_middle_parts += UINT_BIG
      (
        name_middle_parts += FORWARD_SLASH
        | name_middle_parts += PERIOD
        | name_middle_parts += COLON
      )
    )*
    | name_middle_parts += MODULE
  ) range?
;

interface_name_unstructured
:
  (
    VARIABLE
    | variable_interface_name uint_legacy?
  )
  (
    (
      COLON
      | FORWARD_SLASH
      | PERIOD
    ) uint_legacy
  )*
;

ios_delimited_banner
:
  BANNER_DELIMITER_IOS body = BANNER_BODY? BANNER_DELIMITER_IOS
;

isis_level
:
   LEVEL_1
   | LEVEL_1_2
   | LEVEL_2
;

line_type
:
// intentional blank

   | AUX
   | CON
   | CONSOLE
   | DEFAULT
   |
   (
      TEMPLATE name = variable
   )
   | TTY
   | VTY
;

null_rest_of_line
:
    ~NEWLINE* NEWLINE
;

ospf_network_type
:
   BROADCAST
   | NON_BROADCAST
   | POINT_TO_MULTIPOINT NON_BROADCAST?
   | POINT_TO_POINT
;

ospf_route_type
:
   (
      EXTERNAL uint_legacy?
   )
   | INTERNAL
   |
   (
      NSSA_EXTERNAL uint_legacy?
   )
;

port_specifier
:
   (
      EQ
      (
         args += port
      )+
   )
   |
   (
      GT arg = port
   )
   |
   (
      NEQ
      (
         args += port
      )+
   )
   |
   (
      LT arg = port
   )
   |
   (
      RANGE arg1 = port arg2 = port
   )
;

port
:
   uint_legacy
   | ACAP
   | ACR_NEMA
   | AFPOVERTCP
   | AOL
   | ARNS
   | ASF_RMCP
   | ASIP_WEBADMIN
   | AT_RTMP
   | AURP
   | AUTH
   | BFD
   | BFD_ECHO
   | BFTP
   | BGMP
   | BGP
   | BIFF
   | BOOTPC
   | BOOTPS
   | CHARGEN
   | CIFS
   | CISCO_TDP
   | CITADEL
   | CITRIX_ICA
   | CLEARCASE
   | CMD
   | COMMERCE
   | COURIER
   | CSNET_NS
   | CTIQBE
   | DAYTIME
   | DHCP_FAILOVER2
   | DHCPV6_CLIENT
   | DHCPV6_SERVER
   | DISCARD
   | DNSIX
   | DOMAIN
   | DSP
   | ECHO
   | EFS
   | EPP
   | ESRO_GEN
   | EXEC
   | FINGER
   | FTP
   | FTP_DATA
   | FTPS
   | FTPS_DATA
   | GODI
   | GOPHER
   | GRE
   | GTP_C
   | GTP_PRIME
   | GTP_U
   | H323
   | HA_CLUSTER
   | HOSTNAME
   | HP_ALARM_MGR
   | HTTP
   | HTTP_ALT
   | HTTP_MGMT
   | HTTP_RPC_EPMAP
   | HTTPS
   | IDENT
   | IEEE_MMS_SSL
   | IMAP
   | IMAP3
   | IMAP4
   | IMAPS
   | IPP
   | IPX
   | IRC
   | IRIS_BEEP
   | ISAKMP
   | ISCSI
   | ISI_GL
   | ISO_TSAP
   | KERBEROS
   | KERBEROS_ADM
   | KLOGIN
   | KPASSWD
   | KSHELL
   | L2TP
   | LA_MAINT
   | LANZ
   | LDAP
   | LDAPS
   | LDP
   | LMP
   | LOGIN
   | LOTUSNOTES
   | LPD
   | MAC_SRVR_ADMIN
   | MATIP_TYPE_A
   | MATIP_TYPE_B
   | MICRO_BFD
   | MICROSOFT_DS
   | MLAG
   | MOBILE_IP
   | MONITOR
   | MPP
   | MS_SQL_M
   | MS_SQL_S
   | MSDP
   | MSEXCH_ROUTING
   | MSG_ICP
   | MSP
   | MSRPC
   | NAMESERVER
   | NAS
   | NAT
   | NCP
   | NETBIOS_DGM
   | NETBIOS_NS
   | NETBIOS_SS
   | NETBIOS_SSN
   | NETRJS_1
   | NETRJS_2
   | NETRJS_3
   | NETRJS_4
   | NETWALL
   | NETWNEWS
   | NEW_RWHO
   | NFS
   | NNTP
   | NNTPS
   | NON500_ISAKMP
   | NSW_FE
   | NTP
   | ODMR
   | OLSR
   | OPENVPN
   | PCANYWHERE_DATA
   | PCANYWHERE_STATUS
   | PIM_AUTO_RP
   | PKIX_TIMESTAMP
   | PKT_KRB_IPSEC
   | POP2
   | POP3
   | POP3S
   | PPTP
   | PRINT_SRV
   | PTP_EVENT
   | PTP_GENERAL
   | QMTP
   | QOTD
   | RADIUS
   | RADIUS_ACCT
   | RE_MAIL_CK
   | REMOTEFS
   | REPCMD
   | RIP
   | RJE
   | RLP
   | RLZDBASE
   | RMC
   | RMONITOR
   | RPC2PORTMAP
   | RSH
   | RSYNC
   | RTELNET
   | RTSP
   | SECUREID_UDP
   | SGMP
   | SILC
   | SIP
   | SMTP
   | SMUX
   | SNAGAS
   | SNMP
   | SNMP_TRAP
   | SNMPTRAP
   | SNPP
   | SQLNET
   | SQLSERV
   | SQLSRV
   | SSH
   | SUBMISSION
   | SUNRPC
   | SVRLOC
   | SYSLOG
   | SYSTAT
   | TACACS
   | TACACS_DS
   | TALK
   | TBRPF
   | TCPMUX
   | TCPNETHASPSRV
   | TELNET
   | TFTP
   | TIME
   | TIMED
   | TUNNEL
   | UPS
   | UUCP
   | UUCP_PATH
   | VMNET
   | VXLAN
   | WHO
   | WHOIS
   | WWW
   | XDMCP
   | XNS_CH
   | XNS_MAIL
   | XNS_TIME
   | Z39_50
;

prefix_set_elem
:
   (
      ipa = IP_ADDRESS
      | prefix = IP_PREFIX
      | ipv6a = IPV6_ADDRESS
      | ipv6_prefix = IPV6_PREFIX
   )
   (
      (
         GE minpl = uint_legacy
      )
      |
      (
         LE maxpl = uint_legacy
      )
      |
      (
         EQ eqpl = uint_legacy
      )
   )*
;

protocol
:
   AH
   | AHP
   | uint_legacy
   | EIGRP
   | ESP
   | GRE
   | ICMP
   | ICMP6
   | ICMPV6
   | IGMP
   | IGRP
   | IP
   | IPINIP
   | IPSEC
   | IPV4
   | IPV6
   | ND
   | NOS
   | OSPF
   | PIM
   | PPTP
   | SCTP
   | SNP
   | TCP
   | TCP_UDP
   | UDP
   | VRRP
;

range
:
   (
      range_list += subrange
      (
         COMMA range_list += subrange
      )*
   )
   | NONE
;

route_distinguisher
:
   (IP_ADDRESS | bgp_asn) COLON uint_legacy
;

route_tag
:
  // 1-4294967295
  uint32
;

route_target
:
   (IP_ADDRESS | bgp_asn) COLON uint_legacy
;

route_policy_params_list
:
   params_list += variable
   (
      COMMA params_list += variable
   )*
;

subrange
:
   low = uint_legacy
   (
      DASH high = uint_legacy
   )?
;

switchport_trunk_encapsulation
:
   DOT1Q
   | ISL
   | NEGOTIATE
;

variable
:
   ~NEWLINE
;

variable_community_name
:
   ~( NEWLINE | DOUBLE_QUOTE | GROUP | IPV4 | IPV6 | RO | RW | SDROWNER |
   SYSTEMOWNER | USE_ACL | USE_IPV4_ACL | USE_IPV6_ACL | VIEW )
;

variable_hostname
:
   ~( USE_VRF | NEWLINE | VRF )+
;

variable_interface_name
:
   ~( UINT8 | UINT16 | IP_ADDRESS | IP_PREFIX | ADMIN_DIST | ADMIN_DISTANCE | METRIC |
   NAME | NEWLINE | TAG | TRACK | VARIABLE )
;

variable_max_metric
:
   ~(NEWLINE | BGP | EXTERNAL_LSA | INCLUDE_STUB | ON_STARTUP | ROUTER_LSA | SUMMARY_LSA | WAIT_FOR)
;

variable_permissive
:
   (
      ~( NEWLINE | WS )
   )+
;

variable_secret
:
   ~( NEWLINE | ATTRIBUTES | ENCRYPTED | LEVEL |  MSCHAP | NT_ENCRYPTED | PBKDF2 | PRIVILEGE | ROLE )+
;

variable_group_id
:
    ~( NEWLINE | TCP | TCP_UDP | UDP )+
;

vlan_id: v = uint16;
