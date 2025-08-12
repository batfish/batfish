parser grammar FlatJuniper_common;

options {
  tokenVocab = FlatJuniperLexer;
}

administrator_as
:
  dec L
;

administrator_dec
:
  dec
;

administrator_dotted_as
:
  dec PERIOD dec
;

administrator_ip
:
  dec PERIOD dec PERIOD dec PERIOD dec
;

apply
:
// intentional blank

  | apply_groups
  | apply_groups_except
;

apply_groups
:
  APPLY_GROUPS name = junos_name
;

apply_groups_except
:
  APPLY_GROUPS_EXCEPT name = junos_name
;

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
    items += bgp_asn
  )+ CLOSE_BRACKET
;

as_unit
:
  as_set
  | bgp_asn
;

bgp_asn
:
    asn = uint32
    | asn4hi = uint16 PERIOD asn4lo = uint16
;

bgp_priority_queue_number
:
  // 1-16
  uint8
;

bgp_priority_queue_id
:
   EXPEDITED
   | PRIORITY bgp_priority_queue_number
;

bgp_rib_name
:
    BGP_RIB_NAME
;

dec
:
  UINT8
  | UINT16
  | UINT32
  | DEC
;

description
:
  DESCRIPTION text = M_Description_DESCRIPTION
;

ec_administrator
:
  administrator_as
  | administrator_dec
  | administrator_dotted_as
  | administrator_ip
;

ec_literal
:
  dec COLON dec COLON dec
;

ec_named
:
  ec_type COLON ec_administrator COLON assigned_number = dec
;

ec_type
:
  ORIGIN
  | TARGET
;

extended_community
:
  ec_literal
  | ec_named
;

icmp_code
:
  uint8
  | named_icmp_code
;

named_icmp_code
:
  COMMUNICATION_PROHIBITED_BY_FILTERING
  | DESTINATION_HOST_PROHIBITED
  | DESTINATION_HOST_UNKNOWN
  | DESTINATION_NETWORK_PROHIBITED
  | DESTINATION_NETWORK_UNKNOWN
  | FRAGMENTATION_NEEDED
  | HOST_PRECEDENCE_VIOLATION
  | HOST_UNREACHABLE
  | HOST_UNREACHABLE_FOR_TOS
  | IP_HEADER_BAD
  | NETWORK_UNREACHABLE
  | NETWORK_UNREACHABLE_FOR_TOS
  | PORT_UNREACHABLE
  | PRECEDENCE_CUTOFF_IN_EFFECT
  | PROTOCOL_UNREACHABLE
  | REDIRECT_FOR_HOST
  | REDIRECT_FOR_NETWORK
  | REDIRECT_FOR_TOS_AND_HOST
  | REDIRECT_FOR_TOS_AND_NET
  | REQUIRED_OPTION_MISSING
  | SOURCE_HOST_ISOLATED
  | SOURCE_ROUTE_FAILED
  | TTL_EQ_ZERO_DURING_REASSEMBLY
  | TTL_EQ_ZERO_DURING_TRANSIT
;

icmp_type
:
  uint8
  | named_icmp_type
;

named_icmp_type
:
  DESTINATION_UNREACHABLE
  | ECHO_REPLY
  | ECHO_REQUEST
  | INFO_REPLY
  | INFO_REQUEST
  | MASK_REPLY
  | MASK_REQUEST
  | MEMBERSHIP_REPORT
  | MEMBERSHIP_QUERY
  | PARAMETER_PROBLEM
  | REDIRECT
  | ROUTER_ADVERTISEMENT
  | ROUTER_SOLICIT
  | SOURCE_QUENCH
  | TIME_EXCEEDED
  | TIMESTAMP
  | TIMESTAMP_REPLY
  | UNREACHABLE
;

icmp6_only_type
:
    NEIGHBOR_ADVERTISEMENT
    | NEIGHBOR_SOLICIT
    | PACKET_TOO_BIG
;

inet_rib_name
:
INET_RIB_NAME
;

inet6_rib_name
:
INET6_RIB_NAME
;

iso_rib_name
:
ISO_RIB_NAME
;

mpls_rib_name
:
MPLS_RIB_NAME
;

rib_name
:
(bgp_rib_name | inet_rib_name | inet6_rib_name | iso_rib_name | mpls_rib_name | vxlan_rib_name )
;

interface_id: INTERFACE_ID;

interface_wildcard: INTERFACE_WILDCARD;

ip_option
:
  ANY
  | LOOSE_SOURCE_ROUTE
  | ROUTE_RECORD
  | ROUTER_ALERT
  | SECURITY
  | STREAM_ID
  | STRICT_SOURCE_ROUTE
  | TIMESTAMP
;

ip_address: IP_ADDRESS;

ip_address_and_mask: IP_ADDRESS_AND_MASK;

ip_prefix: IP_PREFIX;

ip_prefix_default_32: IP_PREFIX | IP_ADDRESS;

ipv6_address: IPV6_ADDRESS;

ipv6_prefix: IPV6_PREFIX;

ipv6_prefix_default_128: IPV6_PREFIX | IPV6_ADDRESS;

iso_address: ISO_ADDRESS;

ip_protocol
:
  AH
  | dec
  | EGP
  | ESP
  | GRE
  | HOP_BY_HOP
  | ICMP
  | ICMP6
  | IGMP
  | IPIP
  | IPV6
  | OSPF
  | PIM
  | RSVP
  | SCTP
  | TCP
  | UDP
  | VRRP
;

junos_application
:
  ANY
  | JUNOS_AOL
  | JUNOS_BGP
  | JUNOS_BIFF
  | JUNOS_BOOTPC
  | JUNOS_BOOTPS
  | JUNOS_CHARGEN
  | JUNOS_CVSPSERVER
  | JUNOS_DHCP_CLIENT
  | JUNOS_DHCP_RELAY
  | JUNOS_DHCP_SERVER
  | JUNOS_DISCARD
  | JUNOS_DNS_TCP
  | JUNOS_DNS_UDP
  | JUNOS_ECHO
  | JUNOS_FINGER
  | JUNOS_FTP
  | JUNOS_FTP_DATA
  | JUNOS_GNUTELLA
  | JUNOS_GOPHER
  | JUNOS_GPRS_GTP_C
  | JUNOS_GPRS_GTP_U
  | JUNOS_GPRS_GTP_V0
  | JUNOS_GPRS_SCTP
  | JUNOS_GRE
  | JUNOS_GTP
  | JUNOS_H323
  | JUNOS_HTTP
  | JUNOS_HTTP_EXT
  | JUNOS_HTTPS
  | JUNOS_ICMP_ALL
  | JUNOS_ICMP_PING
  | JUNOS_ICMP6_ALL
  | JUNOS_ICMP6_DST_UNREACH_ADDR
  | JUNOS_ICMP6_DST_UNREACH_ADMIN
  | JUNOS_ICMP6_DST_UNREACH_BEYOND
  | JUNOS_ICMP6_DST_UNREACH_PORT
  | JUNOS_ICMP6_DST_UNREACH_ROUTE
  | JUNOS_ICMP6_ECHO_REPLY
  | JUNOS_ICMP6_ECHO_REQUEST
  | JUNOS_ICMP6_PACKET_TOO_BIG
  | JUNOS_ICMP6_PARAM_PROB_HEADER
  | JUNOS_ICMP6_PARAM_PROB_NEXTHDR
  | JUNOS_ICMP6_PARAM_PROB_OPTION
  | JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY
  | JUNOS_ICMP6_TIME_EXCEED_TRANSIT
  | JUNOS_IDENT
  | JUNOS_IKE
  | JUNOS_IKE_NAT
  | JUNOS_IMAP
  | JUNOS_IMAPS
  | JUNOS_INTERNET_LOCATOR_SERVICE
  | JUNOS_IRC
  | JUNOS_L2TP
  | JUNOS_LDAP
  | JUNOS_LDP_TCP
  | JUNOS_LDP_UDP
  | JUNOS_LPR
  | JUNOS_MAIL
  | JUNOS_MGCP_CA
  | JUNOS_MGCP_UA
  | JUNOS_MS_RPC_EPM
  | JUNOS_MS_RPC_IIS_COM_1
  | JUNOS_MS_RPC_IIS_COM_ADMINBASE
  | JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP
  | JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR
  | JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE
  | JUNOS_MS_RPC_TCP
  | JUNOS_MS_RPC_UDP
  | JUNOS_MS_RPC_UUID_ANY_TCP
  | JUNOS_MS_RPC_UUID_ANY_UDP
  | JUNOS_MS_RPC_WMIC_ADMIN
  | JUNOS_MS_RPC_WMIC_ADMIN2
  | JUNOS_MS_RPC_WMIC_MGMT
  | JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT
  | JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT
  | JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN
  | JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID
  | JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER
  | JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK
  | JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES
  | JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER
  | JUNOS_MS_RPC_WMIC_WEBM_SERVICES
  | JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN
  | JUNOS_MS_SQL
  | JUNOS_MSN
  | JUNOS_NBDS
  | JUNOS_NBNAME
  | JUNOS_NETBIOS_SESSION
  | JUNOS_NFS
  | JUNOS_NFSD_TCP
  | JUNOS_NFSD_UDP
  | JUNOS_NNTP
  | JUNOS_NS_GLOBAL
  | JUNOS_NS_GLOBAL_PRO
  | JUNOS_NSM
  | JUNOS_NTALK
  | JUNOS_NTP
  | JUNOS_OSPF
  | JUNOS_PC_ANYWHERE
  | JUNOS_PERSISTENT_NAT
  | JUNOS_PING
  | JUNOS_PINGV6
  | JUNOS_POP3
  | JUNOS_PPTP
  | JUNOS_PRINTER
  | JUNOS_R2CP
  | JUNOS_RADACCT
  | JUNOS_RADIUS
  | JUNOS_REALAUDIO
  | JUNOS_RDP
  | JUNOS_RIP
  | JUNOS_RSH
  | JUNOS_RTSP
  | JUNOS_SCCP
  | JUNOS_SCTP_ANY
  | JUNOS_SIP
  | JUNOS_SMB
  | JUNOS_SMB_SESSION
  | JUNOS_SMTP
  | JUNOS_SMTPS
  | JUNOS_SNMP_AGENTX
  | JUNOS_SNPP
  | JUNOS_SQL_MONITOR
  | JUNOS_SQLNET_V1
  | JUNOS_SQLNET_V2
  | JUNOS_SSH
  | JUNOS_STUN_TCP
  | JUNOS_STUN_UDP
  | JUNOS_SUN_RPC_ANY_TCP
  | JUNOS_SUN_RPC_ANY_UDP
  | JUNOS_SUN_RPC_MOUNTD_TCP
  | JUNOS_SUN_RPC_MOUNTD_UDP
  | JUNOS_SUN_RPC_NFS_TCP
  | JUNOS_SUN_RPC_NFS_UDP
  | JUNOS_SUN_RPC_NLOCKMGR_TCP
  | JUNOS_SUN_RPC_NLOCKMGR_UDP
  | JUNOS_SUN_RPC_PORTMAP_TCP
  | JUNOS_SUN_RPC_PORTMAP_UDP
  | JUNOS_SUN_RPC_RQUOTAD_TCP
  | JUNOS_SUN_RPC_RQUOTAD_UDP
  | JUNOS_SUN_RPC_RUSERD_TCP
  | JUNOS_SUN_RPC_RUSERD_UDP
  | JUNOS_SUN_RPC_SADMIND_TCP
  | JUNOS_SUN_RPC_SADMIND_UDP
  | JUNOS_SUN_RPC_SPRAYD_TCP
  | JUNOS_SUN_RPC_SPRAYD_UDP
  | JUNOS_SUN_RPC_STATUS_TCP
  | JUNOS_SUN_RPC_STATUS_UDP
  | JUNOS_SUN_RPC_TCP
  | JUNOS_SUN_RPC_UDP
  | JUNOS_SUN_RPC_WALLD_TCP
  | JUNOS_SUN_RPC_WALLD_UDP
  | JUNOS_SUN_RPC_YPBIND_TCP
  | JUNOS_SUN_RPC_YPBIND_UDP
  | JUNOS_SUN_RPC_YPSERV_TCP
  | JUNOS_SUN_RPC_YPSERV_UDP
  | JUNOS_SYSLOG
  | JUNOS_TACACS
  | JUNOS_TACACS_DS
  | JUNOS_TALK
  | JUNOS_TCP_ANY
  | JUNOS_TELNET
  | JUNOS_TFTP
  | JUNOS_UDP_ANY
  | JUNOS_UUCP
  | JUNOS_VDO_LIVE
  | JUNOS_VNC
  | JUNOS_WAIS
  | JUNOS_WHO
  | JUNOS_WHOIS
  | JUNOS_WINFRAME
  | JUNOS_WXCONTROL
  | JUNOS_X_WINDOWS
  | JUNOS_XNM_CLEAR_TEXT
  | JUNOS_XNM_SSL
  | JUNOS_YMSG
;

junos_application_set
:
  JUNOS_CIFS
  | JUNOS_MGCP
  | JUNOS_MS_RPC
  | JUNOS_MS_RPC_ANY
  | JUNOS_MS_RPC_IIS_COM
  | JUNOS_MS_RPC_MSEXCHANGE
  | JUNOS_MS_RPC_WMIC
  | JUNOS_ROUTING_INBOUND
  | JUNOS_STUN
  | JUNOS_SUN_RPC
  | JUNOS_SUN_RPC_ANY
  | JUNOS_SUN_RPC_MOUNTD
  | JUNOS_SUN_RPC_NFS
  | JUNOS_SUN_RPC_NFS_ACCESS
  | JUNOS_SUN_RPC_NLOCKMGR
  | JUNOS_SUN_RPC_PORTMAP
  | JUNOS_SUN_RPC_RQUOTAD
  | JUNOS_SUN_RPC_RUSERD
  | JUNOS_SUN_RPC_SADMIND
  | JUNOS_SUN_RPC_SPRAYD
  | JUNOS_SUN_RPC_STATUS
  | JUNOS_SUN_RPC_WALLD
  | JUNOS_SUN_RPC_YPBIND
  | JUNOS_SUN_RPC_YPSERV
;

// The name of a JunOS structure. These are: letters, numbers, and hyphens, with spaces allowed
// inside quotes.
//
// NOTE: we also added underscore, since this appears to be supported in practice.
//
// The length of names varies by structure term. E.g., term-name (Firewall Filter) is 64,
// term-name (Simpler Filter) is 255.
junos_name
:
  NAME
  | wildcard
;

junos_name_list
:
  junos_name
  | OPEN_BRACKET junos_name* CLOSE_BRACKET
;

filter_name
:
  junos_name
  // Edge-cases due to language ambiguity
  | INPUT
  | OUTPUT
;

name_or_ip
:
  junos_name
  | IP_ADDRESS
  | IPV6_ADDRESS
;

route_distinguisher
:
  (
    rd_ip_address_colon_id
    | rd_asn_colon_id
  )
;

rd_asn_colon_id
:
  // Type-0 (Administrator subfield: 2 bytes):(Assigned Number subfield: 4 bytes)
  // Type-2 (Administrator subfield: 4 bytes):(Assigned Number subfield: 2 bytes)
  // Junos docs: An RD that includes a 4-byte AS number, append the letter “L” to the end of the AS number.

  high16 = uint16 COLON low32 = uint32
  | high32 = uint32l COLON low16 = uint16
;

rd_ip_address_colon_id
:
  // ip-address:id — ip-address is a 4-byte value and id is a 2-byte value.
  // Type-1
  IP_ADDRESS COLON uint16
;

secret_string
:
  SECRET_STRING
  | SCRUBBED
;

certificate_string
:
  CERTIFICATE_STRING | SCRUBBED
;

null_filler
:
  ~( APPLY_GROUPS | NEWLINE )* apply_groups?
;

origin_type
:
  EGP
  | IGP
  | INCOMPLETE
;

pe_conjunction
:
  OPEN_PAREN policy_expression
  (
    DOUBLE_AMPERSAND policy_expression
  )+ CLOSE_PAREN
;

pe_disjunction
:
  OPEN_PAREN policy_expression
  (
    DOUBLE_PIPE policy_expression
  )+ CLOSE_PAREN
;

pe_nested
:
  OPEN_PAREN policy_expression CLOSE_PAREN
;

policy_expression
:
  pe_conjunction
  | pe_disjunction
  | pe_nested
  | junos_name
;

port
:
  port_number
  | named_port
;

port_range
:
  named = named_port
  | start = port_number (DASH end = port_number)?
;

named_port
:
  AFS
  | BGP
  | BIFF
  | BOOTPC
  | BOOTPS
  | CMD
  | CVSPSERVER
  | DHCP
  | DOMAIN
  | EKLOGIN
  | EKSHELL
  | EXEC
  | FINGER
  | FTP
  | FTP_DATA
  | HTTP
  | HTTPS
  | IDENT
  | IMAP
  | KERBEROS_SEC
  | KLOGIN
  | KPASSWD
  | KRB_PROP
  | KRBUPDATE
  | KSHELL
  | LDAP
  | LDP
  | LOGIN
  | MOBILEIP_AGENT
  | MOBILIP_MN
  | MSDP
  | NETBIOS_DGM
  | NETBIOS_NS
  | NETBIOS_SSN
  | NFSD
  | NNTP
  | NTALK
  | NTP
  | POP3
  | PPTP
  | PRINTER
  | RADACCT
  | RADIUS
  | RIP
  | RKINIT
  | SMTP
  | SNMP
  | SNMPTRAP
  | SNPP
  | SOCKS
  | SSH
  | SUNRPC
  | SYSLOG
  | TACACS
  | TACACS_DS
  | TALK
  | TELNET
  | TFTP
  | TIMED
  | WHO
  | XDMCP
;

port_number
:
  // 1-65535
  uint16
;

range
:
  range_list += subrange
  (
    COMMA range_list += subrange
  )*
;

bandwidth
:
  base = dec
  (
    C
    | K
    | M
    | G
  )?
;

sc_literal
:
  STANDARD_COMMUNITY
;

sc_named
:
  NO_ADVERTISE
  | NO_EXPORT
  | NO_EXPORT_SUBCONFED
;

standard_community
:
  sc_literal
  | sc_named
;

subrange
:
  low = dec
  (
    DASH high = dec
  )?
;

threshold
:
  THRESHOLD value = dec
;

uint8
:
  UINT8
;

uint8_range: start = uint8 (DASH end = uint8)?;

uint16
:
  UINT8
  | UINT16
;

uint16_range: start = uint16 (DASH end = uint16)?;

uint32
:
  UINT8
  | UINT16
  | UINT32
;

uint32l
:
  UINT32L
;

vxlan_rib_name
:
    VXLAN_RIB_NAME
;

wildcard
:
  WILDCARD
  | WILDCARD_ARTIFACT
;

vlan_number
:
  // 1-4094
  uint16
;

vlan_range
:
  start = vlan_number (DASH end = vlan_number)?
;

vni_number
:
  // 0 through 16,777,215
  // https://www.juniper.net/documentation/us/en/software/junos/evpn-vxlan/topics/ref/statement/vni-vxlan.html#id-vni__d49551e60
  uint32
;


vni_range
:
  start = vni_number (DASH end = vni_number)?
;
