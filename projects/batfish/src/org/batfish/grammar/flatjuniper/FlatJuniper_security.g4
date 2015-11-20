parser grammar FlatJuniper_security;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

abast_address
:
   ADDRESS name = variable
;

abast_address_set
:
   ADDRESS_SET name = variable
;

abt_address
:
   ADDRESS name = variable abt_address_tail
;

abt_address_tail
:
   apply
   | IP_PREFIX
;

abt_address_set
:
   ADDRESS_SET name = variable abt_address_set_tail
;

abt_address_set_tail
:
   apply
   | abast_address
   | abast_address_set
;

address_specifier
:
   ANY
   | ANY_IPV4
   | ANY_IPV6
   | name = variable
;

certt_local
:
   LOCAL name = variable cert = DOUBLE_QUOTED_STRING
;

dh_group
:
   GROUP1
   | GROUP14
   | GROUP2
   | GROUP5
;

encryption_algorithm
:
   THREEDES_CBC
   | AES_128_CBC
   | AES_192_CBC
   | AES_256_CBC
   | DES_CBC
;

gdt_connections_limit
:
   CONNECTIONS_LIMIT limit = DEC
;

gdt_hostname
:
   HOSTNAME name = variable
;

gdt_ike_user_type
:
   IKE_USER_TYPE
   (
      GROUP_IKE_ID
      | SHARED_IKE_ID
   )
;

hibt_protocols
:
   PROTOCOLS
   (
      ALL
      | BFD
      | BGP
      | DVMRP
      | IGMP
      | LDP
      | MSDP
      | NHRP
      | OSPF
      | OSPF3
      | PGM
      | PIM
      | RIP
      | RIPNG
      | ROUTER_DISCOVERY
      | RSVP
      | SAP
      | VRRP
   )
;

hibt_system_services
:
   SYSTEM_SERVICES
   (
      ALL
      | ANY_SERVICE
      | DNS
      | FINGER
      | FTP
      | HTTP
      | HTTPS
      | IDENT_RESET
      | IKE
      | LSPING
      | NETCONF
      | NTP
      | PING
      | R2CP
      | REVERSE_SSH
      | REVERSE_TELNET
      | RLOGIN
      | RPM
      | RSH
      | SIP
      | SNMP
      | SNMP_TRAP
      | SSH
      | TELNET
      | TFTP
      | TRACEROUTE
      | XNM_CLEAR_TEXT
      | XNM_SSL
   )
;

ike_authentication_algorithm
:
   MD5
   | SHA_256
   | SHA_384
   | SHA1
;

ike_authentication_method
:
   DSA_SIGNATURES
   | PRE_SHARED_KEYS
   | RSA_SIGNATURES
;

ikegt_address
:
   ADDRESS IP_ADDRESS
;

ikegt_dead_peer_detection
:
   DEAD_PEER_DETECTION ALWAYS_SEND?
;

ikegt_dynamic
:
   DYNAMIC ikegt_dynamic_tail
;

ikegt_dynamic_tail
:
   apply
   | gdt_connections_limit
   | gdt_hostname
   | gdt_ike_user_type
;

ikegt_external_interface
:
   EXTERNAL_INTERFACE interface_id
;

ikegt_ike_policy
:
   IKE_POLICY name = variable
;

ikegt_local_address
:
   LOCAL_ADDRESS IP_ADDRESS
;

ikegt_local_identity
:
   LOCAL_IDENTITY ikegt_local_identity_tail
;

ikegt_local_identity_tail
:
   lit_inet
;

ikegt_no_nat_traversal
:
   NO_NAT_TRAVERSAL
;

ikegt_version
:
   VERSION V1_ONLY
;

ikegt_xauth
:
   XAUTH ACCESS_PROFILE name = variable
;

ikeprt_authentication_algorithm
:
   AUTHENTICATION_ALGORITHM ike_authentication_algorithm
;

ikeprt_authentication_method
:
   AUTHENTICATION_METHOD ike_authentication_method
;

ikeprt_description
:
   DESCRIPTION s_null_filler
;

ikeprt_dh_group
:
   DH_GROUP dh_group
;

ikeprt_encryption_algorithm
:
   ENCRYPTION_ALGORITHM encryption_algorithm
;

ikeprt_lifetime_seconds
:
   LIFETIME_SECONDS seconds = DEC
;

ikept_description
:
   DESCRIPTION s_null_filler
;

ikept_mode
:
   MODE
   (
      AGGRESSIVE
      | MAIN
   )
;

ikept_pre_shared_key
:
   PRE_SHARED_KEY ASCII_TEXT key = DOUBLE_QUOTED_STRING
;

ikept_proposal_set
:
   PROPOSAL_SET proposal_set_type
;

ikept_proposals
:
   PROPOSALS name = variable
;

iket_gateway
:
   GATEWAY name = variable iket_gateway_tail
;

iket_gateway_tail
:
   ikegt_address
   | ikegt_dead_peer_detection
   | ikegt_dynamic
   | ikegt_external_interface
   | ikegt_ike_policy
   | ikegt_local_address
   | ikegt_local_identity
   | ikegt_no_nat_traversal
   | ikegt_version
   | ikegt_xauth
;

iket_policy
:
   POLICY name = variable iket_policy_tail
;

iket_policy_tail
:
   ikept_description
   | ikept_mode
   | ikept_pre_shared_key
   | ikept_proposal_set
   | ikept_proposals
;

iket_proposal
:
   PROPOSAL name = variable iket_proposal_tail
;

iket_proposal_tail
:
   ikeprt_authentication_algorithm
   | ikeprt_authentication_method
   | ikeprt_description
   | ikeprt_dh_group
   | ikeprt_encryption_algorithm
   | ikeprt_lifetime_seconds
;

ipsec_authentication_algorithm
:
   HMAC_MD5_96
   | HMAC_SHA1_96
;

ipsec_protocol
:
   AH
   | ESP
;

ipsecprt_authentication_algorithm
:
   AUTHENTICATION_ALGORITHM ipsec_authentication_algorithm
;

ipsecprt_description
:
   DESCRIPTION s_null_filler
;

ipsecprt_encryption_algorithm
:
   ENCRYPTION_ALGORITHM encryption_algorithm
;

ipsecprt_lifetime_kilobytes
:
   LIFETIME_KILOBYTES kilobytes = DEC
;

ipsecprt_lifetime_seconds
:
   LIFETIME_SECONDS seconds = DEC
;

ipsecprt_protocol
:
   PROTOCOL ipsec_protocol
;

ipsecpt_perfect_forward_secrecy
:
   PERFECT_FORWARD_SECRECY KEYS dh_group
;

ipsecpt_proposal_set
:
   PROPOSAL_SET proposal_set_type
;

ipsecpt_proposals
:
   PROPOSALS name = variable
;

ipsect_policy
:
   POLICY name = variable ipsect_policy_tail
;

ipsect_policy_tail
:
   ipsecpt_perfect_forward_secrecy
   | ipsecpt_proposal_set
   | ipsecpt_proposals
;

ipsect_proposal
:
   PROPOSAL name = variable ipsect_proposal_tail
;

ipsect_proposal_tail
:
   apply
   | ipsecprt_authentication_algorithm
   | ipsecprt_description
   | ipsecprt_encryption_algorithm
   | ipsecprt_lifetime_kilobytes
   | ipsecprt_lifetime_seconds
   | ipsecprt_protocol
;

ipsect_vpn
:
   VPN name = variable ipsect_vpn_tail
;

ipsect_vpn_tail
:
   ipsecvt_bind_interface
   | ipsecvt_df_bit
   | ipsecvt_establish_tunnels
   | ipsecvt_ike
   | ipsecvt_vpn_monitor
;

ipsecvit_gateway
:
   GATEWAY name = variable
;

ipsecvit_ipsec_policy
:
   IPSEC_POLICY name = variable
;

ipsecvit_null
:
   (
      NO_ANTI_REPLAY
   ) s_null_filler
;

ipsecvit_proxy_identity
:
   PROXY_IDENTITY ipsecvit_proxy_identity_tail
;

ipsecvit_proxy_identity_tail
:
   pit_local
   | pit_remote
   | pit_service
;

ipsecvmt_destination_ip
:
   DESTINATION_IP IP_ADDRESS
;

ipsecvmt_source_interface
:
   SOURCE_INTERFACE interface_id
;

ipsecvt_bind_interface
:
   BIND_INTERFACE interface_id
;

ipsecvt_df_bit
:
   DF_BIT CLEAR
;

ipsecvt_establish_tunnels
:
   ESTABLISH_TUNNELS IMMEDIATELY
;

ipsecvt_ike
:
   IKE ipsecvt_ike_tail
;

ipsecvt_ike_tail
:
   ipsecvit_gateway
   | ipsecvit_ipsec_policy
   | ipsecvit_null
   | ipsecvit_proxy_identity
;

ipsecvt_vpn_monitor
:
   VPN_MONITOR ipsecvt_vpn_monitor_tail
;

ipsecvt_vpn_monitor_tail
:
// intentional blank

   | ipsecvmt_destination_ip
   | ipsecvmt_source_interface
;

lit_inet
:
   INET name = variable
;

nat_rule_set
:
   RULE_SET nat_rule_set_named
;

nat_rule_set_named
:
   name = variable nat_rule_set_tail
;

nat_rule_set_tail
:
   natrst_from
   | natrst_rule
   | natrst_to
;

natrsfromt_zone
:
   ZONE name = variable
;

natrsrmt_destination_address
:
   DESTINATION_ADDRESS IP_PREFIX
;

natrsrmt_destination_port
:
   DESTINATION_PORT from = DEC
   (
      TO to = DEC
   )?
;

natrsrmt_source_address
:
   SOURCE_ADDRESS IP_PREFIX
;

natrsrt_description
:
   DESCRIPTION s_null_filler
;

natrsrt_match
:
   MATCH natrsrt_match_tail
;

natrsrt_match_tail
:
   natrsrmt_destination_address
   | natrsrmt_destination_port
   | natrsrmt_source_address
;

natrsrt_then
:
   THEN natrsrt_then_tail
;

natrsrt_then_tail
:
   natrsrtt_source_nat
   | natrsrtt_static_nat
;

natrsrtt_source_nat
:
   SOURCE_NAT natrsrtt_source_nat_tail
;

natrsrtt_source_nat_tail
:
   tsnt_interface
   | tsnt_pool
;

natrsrtt_static_nat
:
   STATIC_NAT natrsrtt_static_nat_tail
;

natrsrtt_static_nat_tail
:
   stnt_prefix
;

natrst_from
:
   FROM natrst_from_tail
;

natrst_from_tail
:
   natrsfromt_zone
;

natrst_rule
:
   RULE natrst_rule_named
;

natrst_rule_named
:
   name = variable natrst_rule_tail
;

natrst_rule_tail
:
   natrsrt_description
   | natrsrt_match
   | natrsrt_then
;

natrst_to
:
   TO natrst_from_tail
;

natrst_to_tail
:
   natrstot_zone
;

natrstot_zone
:
   ZONE name = variable
;

natsoit_port_overloading
:
   PORT_OVERLOADING OFF
;

natsoit_port_overloading_factor
:
   PORT_OVERLOADING_FACTOR factor = DEC
;

natsot_interface
:
   INTERFACE natsot_interface_tail
;

natsot_interface_tail
:
   natsoit_port_overloading
   | natsoit_port_overloading_factor
;

natsot_pool
:
   POOL name = variable natsot_pool_tail
;

natsot_pool_tail
:
   poolt_address
   | poolt_description
;

natsot_port_randomization
:
   PORT_RANDOMIZATION DISABLE
;

natsot_rule_set
:
   nat_rule_set
;

natstt_rule_set
:
   nat_rule_set
;

natt_proxy_arp
:
   PROXY_ARP natt_proxy_arp_tail
;

natt_proxy_arp_tail
:
   apply
   | pat_interface
;

natt_source
:
   SOURCE natt_source_tail
;

natt_source_tail
:
   natsot_interface
   | natsot_pool
   | natsot_port_randomization
   | natsot_rule_set
;

natt_static
:
   STATIC natt_static_tail
;

natt_static_tail
:
   natstt_rule_set
;

pait_address
:
   ADDRESS
   (
      from = IP_ADDRESS
      | from = IP_PREFIX
   )
   (
      TO
      (
         to = IP_ADDRESS
         | to = IP_PREFIX
      )
   )?
;

pat_interface
:
   INTERFACE interface_id pat_interface_tail
;

pat_interface_tail
:
   apply
   | pait_address
;

pit_local
:
   LOCAL IP_PREFIX
;

pit_remote
:
   REMOTE IP_PREFIX
;

pit_service
:
   SERVICE
   (
      ANY
      | name = variable
   )
;

pnt_inactivity_timeout
:
   INACTIVITY_TIMEOUT seconds = DEC
;

pnt_max_session_number
:
   MAX_SESSION_NUMBER max = DEC
;

pnt_permit
:
   PERMIT
   (
      ANY_REMOTE_HOST
      | TARGET_HOST
      | TARGET_HOST_PORT
   )
;

poolt_address
:
   ADDRESS IP_PREFIX
   (
      TO IP_PREFIX
   )?
;

poolt_description
:
   DESCRIPTION s_null_filler
;

proposal_set_type
:
   BASIC
   | COMPATIBLE
   | STANDARD
;

ptt_ipsec_vpn
:
   IPSEC_VPN name = variable
;

s_security
:
   SECURITY s_security_tail
;

s_security_tail
:
   sect_certificates
   | sect_ike
   | sect_ipsec
   | sect_nat
   | sect_null
   | sect_policies
   | sect_zones
;

sect_certificates
:
   CERTIFICATES sect_certificates_tail
;

sect_certificates_tail
:
   certt_local
;

sect_ike
:
   IKE sect_ike_tail
;

sect_ike_tail
:
   iket_gateway
   | iket_policy
   | iket_proposal
;

sect_ipsec
:
   IPSEC sect_ipsec_tail
;

sect_ipsec_tail
:
   ipsect_policy
   | ipsect_proposal
   | ipsect_vpn
;

sect_nat
:
   NAT sect_nat_tail
;

sect_nat_tail
:
   natt_proxy_arp
   | natt_source
   | natt_static
;

sect_null
:
   (
      ALG
      | APPLICATION_TRACKING
      | FLOW
      | LOG
      | SCREEN
   ) s_null_filler
;

sect_policies
:
   POLICIES sect_policies_tail
;

sect_policies_tail
:
   spt_default_policy
   | spt_from_zone
;

sect_zones
:
   ZONES sect_zones_tail
;

sect_zones_tail
:
   apply
   | szt_security_zone
;

sp_description
:
   DESCRIPTION s_null_filler
;

sp_match
:
   MATCH sp_match_tail
;

sp_match_tail
:
   spmt_application
   | spmt_destination_address
   | spmt_source_address
   | spmt_source_identity
;

sp_then
:
   THEN sp_then_tail
;

sp_then_tail
:
   sptt_deny
   | sptt_permit
;

spmt_application
:
   APPLICATION
   (
      ANY
      | name = variable
   )
;

spmt_destination_address
:
   DESTINATION_ADDRESS address_specifier
;

spmt_source_address
:
   SOURCE_ADDRESS address_specifier
;

spmt_source_identity
:
   SOURCE_IDENTITY
   (
      ANY
      | name = variable
   )
;

spt_default_policy
:
   DEFAULT_POLICY spt_default_policy_tail
;

spt_default_policy_tail
:
   apply
   | DENY_ALL
   | PERMIT_ALL
;

spt_from_zone
:
   FROM_ZONE from = zone TO_ZONE to = zone spt_from_zone_tail
;

spt_from_zone_tail
:
   apply
   | spt_policy
;

spt_policy
:
   POLICY name = variable spt_policy_tail
;

spt_policy_tail
:
   apply
   | sp_description
   | sp_match
   | sp_then
;

sptpt_tunnel
:
   TUNNEL sptpt_tunnel_tail
;

sptpt_tunnel_tail
:
   apply
   | ptt_ipsec_vpn
;

sptt_deny
:
   DENY
;

sptt_permit
:
   PERMIT sptt_permit_tail
;

sptt_permit_tail
:
   apply
   | sptpt_tunnel
;

stnpt_mapped_port
:
   MAPPED_PORT low = DEC
   (
      TO high = DEC
   )?
;

stnpt_prefix
:
   IP_PREFIX
;

stnt_prefix
:
   PREFIX stnt_prefix_tail
;

stnt_prefix_tail
:
   stnpt_mapped_port
   | stnpt_prefix
;

szszt_address_book
:
   ADDRESS_BOOK szszt_address_book_tail
;

szszt_address_book_tail
:
   apply
   | abt_address
   | abt_address_set
;

szszt_application_tracking
:
   APPLICATION_TRACKING
;

szszt_host_inbound_traffic
:
   HOST_INBOUND_TRAFFIC szszt_host_inbound_traffic_tail
;

szszt_host_inbound_traffic_tail
:
   apply
   | hibt_protocols
   | hibt_system_services
;

szszt_interfaces
:
   INTERFACES interface_id szszt_interfaces_tail
;

szszt_interfaces_tail
:
   apply
   | szszt_host_inbound_traffic
;

szszt_screen
:
   SCREEN
   (
      UNTRUST_SCREEN
      | name = variable
   )
;

szszt_tcp_rst
:
   TCP_RST
;

szt_security_zone
:
   SECURITY_ZONE zone szt_security_zone_tail
;

szt_security_zone_tail
:
   apply
   | szszt_address_book
   | szszt_application_tracking
   | szszt_host_inbound_traffic
   | szszt_interfaces
   | szszt_screen
   | szszt_tcp_rst
;

tsnpt_common
:
   apply
   | tsnpt_persistent_nat
;

tsnpt_named
:
   name = variable tsnpt_common
;

tsnpt_persistent_nat
:
   PERSISTENT_NAT tsnpt_persistent_nat_tail
;

tsnpt_persistent_nat_tail
:
   apply
   | pnt_inactivity_timeout
   | pnt_max_session_number
   | pnt_permit
;

tsnt_interface
:
   INTERFACE
;

tsnt_pool
:
   POOL tsnt_pool_tail
;

tsnt_pool_tail
:
   tsnpt_common
   | tsnpt_named
;

zone
:
   JUNOS_HOST
   | TRUST
   | UNTRUST
   | name = variable
;
