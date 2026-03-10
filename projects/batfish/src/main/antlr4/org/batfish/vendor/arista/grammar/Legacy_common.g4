parser grammar Legacy_common;

options {
   tokenVocab = AristaLexer;
}

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

bgp_asn
:
    asn = uint32
    | asn4b = FLOAT // dec.dec , but this lexes as FLOAT
;

literal_standard_community
:
   GSHUT
   | INTERNET
   | LOCAL_AS
   | NO_ADVERTISE
   | NO_EXPORT
   | hi = uint16 COLON lo = uint16
   | u32 = uint32
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
   dec
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

ec_literal
:
   dec COLON dec
;

eos_vlan_id
:
   vlan_ids += subrange
   (
      COMMA vlan_ids += subrange
   )*
;

eos_vxlan_interface_name
:
   VXLAN dec
;

exit_line
:
   EXIT NEWLINE
;

extended_community
:
   ec_literal
;

int_expr
:
   (
      (
         PLUS
         | DASH
      )? dec
   )
   | IGP_COST
;

interface_name
:
  name_prefix_alpha = M_Interface_PREFIX
  (
    (
      (
        name_middle_parts += M_Interface_PREFIX
      )? name_middle_parts += (DEC | UINT8 | UINT16 | UINT32)
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
    | variable_interface_name dec?
  )
  (
    (
      COLON
      | FORWARD_SLASH
      | PERIOD
    ) dec
  )*
;

ip_hostname
:
   IP_ADDRESS
   | IPV6_ADDRESS
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

netservice_alg
:
   DHCP
   | DNS
   | FTP
   | NOE
   | RTSP
   | SCCP
   | SIPS
   | SVP
   | TFTP
   | VOCERA
;

null_rest_of_line
:
    ~NEWLINE* NEWLINE
;

ospf_route_type
:
   (
      EXTERNAL dec?
   )
   | INTERNAL
   |
   (
      NSSA_EXTERNAL dec?
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
   dec
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
   | CVX
   | CVX_CLUSTER
   | CVX_LICENSE
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

protocol
:
   AH
   | AHP
   | dec
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
   (IP_ADDRESS | bgp_asn) COLON uint16
;

route_target
:
   (IP_ADDRESS | bgp_asn) COLON uint16
;

subrange
:
   low = dec
   (
      DASH high = dec
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

variable_aclname
:
   (
      ~( IN | NEWLINE | OUT | REMARK | STANDARD | WS )
   )+
;

variable_community_name
:
   ~( NEWLINE | DOUBLE_QUOTE | GROUP | IPV4 | IPV6 | RO | RW | SDROWNER |
   SYSTEMOWNER | USE_ACL | USE_IPV4_ACL | USE_IPV6_ACL | VIEW )
;

variable_distribute_list
:
  ~( NEWLINE | IN | OUT )+
;

variable_hostname
:
   ~( NEWLINE | VRF )+
;

variable_interface_name
:
   ~( DEC | UINT8 | UINT16 | UINT32 | IP_ADDRESS | IP_PREFIX | ADMIN_DIST | ADMIN_DISTANCE | METRIC |
   NAME | NEWLINE | TAG | TRACK | VARIABLE )
;

variable_max_metric
:
   ~(NEWLINE | BGP | EXTERNAL_LSA | INCLUDE_STUB | ON_STARTUP | ROUTER_LSA | SUMMARY_LSA | WAIT_FOR)
;

variable_permissive
:
   (
      ~( NEWLINE | STANDARD | WS )
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

vlan_id
:
  v = (DEC | UINT8 | UINT16 | UINT32)
  {isVlanId($v)}?

;
