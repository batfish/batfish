parser grammar FlatJuniper_security;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

address_specifier
:
   ANY
   | ANY_IPV4
   | ANY_IPV6
   | name = address_specifier_name
;

address_specifier_name
:
  // All of these alternatives should be treated as names. We use tokens instead of rules here
  // because the only converter should be for address_specifier_name. That is, we don't assign
  // semantic content to the text of the tokens.
  NAME
  | IP_PREFIX
  | IPV6_PREFIX
;

dh_group
:
   GROUP1
   | GROUP14
   | GROUP15
   | GROUP16
   | GROUP19
   | GROUP2
   | GROUP20
   | GROUP24
   | GROUP5
;

encryption_algorithm
:
   AES_128_CBC
   | AES_128_GCM
   | AES_192_CBC
   | AES_192_GCM
   | AES_256_CBC
   | AES_256_GCM
   | DES_CBC
   | THREEDES_CBC
;

hib_protocol
:
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
;

hib_system_service
:
   ALL
   | ANY_SERVICE
   | DHCP
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

ipsec_authentication_algorithm
:
   HMAC_MD5_96
   | HMAC_SHA1_96
   | HMAC_SHA_256_128
;

ipsec_protocol
:
   AH
   | BUNDLE
   | ESP
;

nat_interface
:
   INTERFACE
   (
      nati_port_overloading
      | nati_port_overloading_factor
   )
;

nat_pool
:
   POOL name = junos_name
   (
      natp_address
      | natp_description
      | natp_port
      | natp_routing_instance
   )
;

nat_pool_utilization_alarm
:
    POOL_UTILIZATION_ALARM null_filler
;

nat_pool_default_port_range
:
   POOL_DEFAULT_PORT_RANGE low = port_number (TO high = port_number)?
;

nat_port_randomization
:
   PORT_RANDOMIZATION DISABLE
;

nat_rule_set
:
   RULE_SET name = junos_name
   (
      rs_packet_location
      | rs_rule
   )
;

nati_port_overloading
:
   PORT_OVERLOADING OFF
;

nati_port_overloading_factor
:
   PORT_OVERLOADING_FACTOR factor = dec
;

natp_address
:
   ADDRESS
   (
      prefix = ip_prefix (PORT port_num = port_number)?
      | from_address = ip_address TO to_address = ip_address
      | from_prefix = ip_prefix TO to_prefix = ip_prefix
      | ip = ip_address (PORT port_num = port_number)?
   )
;

natp_port
:
   PORT
   (
      NO_TRANSLATION
      | RANGE from = port_number (TO to = port_number)?
   )
;

natp_description
:
   description
;

natp_routing_instance
:
   ROUTING_INSTANCE name = junos_name
;

proposal_set_type
:
   BASIC
   | COMPATIBLE
   | STANDARD
;

rs_interface
:
    INTERFACE name = interface_id
;

rs_packet_location
:
   (
     FROM
     | TO
   )
   (
     rs_interface
     | rs_routing_instance
     | rs_zone
   )
;

rs_routing_instance
:
    ROUTING_INSTANCE name = junos_name
;

rs_rule
:
   RULE name = junos_name
   (
      rsr_description
      | rsr_match
      | rsr_then
   )?
;

rs_zone
:
   ZONE name = junos_name
;

rsr_description
:
   description
;

rsr_match
:
   MATCH
   (
      rsrm_destination_address
      | rsrm_destination_address_name
      | rsrm_destination_port
      | rsrm_protocol
      | rsrm_source_address
      | rsrm_source_address_name
      | rsrm_source_port
   )
;

rsr_then
:
   THEN
   (
      rsrt_destination_nat
      | rsrt_source_nat
      | rsrt_static_nat
   )
;

rsrm_destination_address
:
   DESTINATION_ADDRESS ip_prefix
;

rsrm_destination_address_name
:
   DESTINATION_ADDRESS_NAME name = junos_name
;

rsrm_destination_port
:
   DESTINATION_PORT from = port_number
   (
      TO to = port_number
   )?
;

rsrm_protocol: PROTOCOL p = ip_protocol;

rsrm_source_address
:
   SOURCE_ADDRESS ip_prefix
;

rsrm_source_address_name
:
   SOURCE_ADDRESS_NAME name = junos_name
;

rsrm_source_port
:
   SOURCE_PORT from = port_number
    (
        TO to = port_number
    )?
;

rsrt_destination_nat
:
   DESTINATION_NAT
   (
      rsrt_nat_off
      | rsrt_nat_pool
   )
;

rsrt_nat_interface
:
   INTERFACE
;

rsrt_nat_off
:
   OFF
;

rsrt_nat_pool
:
   POOL name = junos_name
   (
      rsrtnp_persistent_nat
   )?
;

rsrt_source_nat
:
   SOURCE_NAT
   (
      rsrt_nat_interface
      | rsrt_nat_off
      | rsrt_nat_pool
   )
;

rsrt_static_nat
:
   STATIC_NAT
   (
      rsrtst_prefix
      | rsrtst_prefix_name
   )
;

rsrtnp_persistent_nat
:
   PERSISTENT_NAT
   (
      apply
      | rsrtnpp_inactivity_timeout
      | rsrtnpp_max_session_number
      | rsrtnpp_permit
   )
;

rsrtnpp_inactivity_timeout
:
   INACTIVITY_TIMEOUT seconds = dec
;

rsrtnpp_max_session_number
:
   MAX_SESSION_NUMBER max = dec
;

rsrtnpp_permit
:
   PERMIT
   (
      ANY_REMOTE_HOST
      | TARGET_HOST
      | TARGET_HOST_PORT
   )
;

rsrtst_prefix
:
   PREFIX
   (
      rsrtstp_mapped_port
      | rsrtstp_prefix
   )
;

rsrtst_prefix_name
:
   PREFIX_NAME
   (
      rsrtstp_prefix_name
      | rsrtstp_routing_instance
   )
;


rsrtstp_mapped_port
:
   MAPPED_PORT low = port_number
   (
      TO high = port_number
   )?
;

rsrtstp_prefix
:
   ip_prefix
;

rsrtstp_prefix_name
:
   name = junos_name
;

rsrtstp_routing_instance
:
   ROUTING_INSTANCE name = junos_name
;

s_security
:
   SECURITY
   (
      apply
      | se_address_book
      | se_authentication_key_chain
      | se_certificates
      | se_ike
      | se_ipsec
      | se_nat
      | se_null
      | se_policies
      | se_screen
      | se_zones
   )
;

se_address_book
:
   ADDRESS_BOOK name = junos_name
   (
       apply
       | sead_address
       | sead_address_set
       | sead_attach
       | sead_description
   )
;

se_authentication_key_chain
:
   AUTHENTICATION_KEY_CHAINS KEY_CHAIN name = junos_name
   (
      sea_key
      | sea_description
      | sea_tolerance
   )
;

se_certificates
:
   CERTIFICATES
   (
      sec_local
   )
;

se_ike
:
   IKE
   (
      seik_gateway
      | seik_policy
      | seik_proposal
   )
;

se_ipsec
:
   IPSEC
   (
      seip_policy
      | seip_proposal
      | seip_vpn
   )
;

se_nat
:
   NAT
   (
      sen_destination
      | sen_proxy_arp
      | sen_source
      | sen_static
   )
;

se_null
:
   (
      ALG
      | APPLICATION_TRACKING
      | FLOW
      | LOG
   ) null_filler
;

se_policies
:
   POLICIES
   (
      sep_default_policy
      | sep_from_zone
      | sep_global
   )
;

se_screen
:
    SCREEN
    (
        ses_ids_option
        | ses_null
    )
;

se_zones
:
   ZONES
   (
      apply
      | sez_security_zone
   )
;

sea_description
:
   description
;

sea_key
:
   KEY name = junos_name
   (
      seak_algorithm
      | seak_options
      | seak_secret
      | seak_start_time
   )
;

sea_tolerance
:
   TOLERANCE dec
;

sead_address
:
   ADDRESS name = junos_name
   (
      description
      | address = ip_address
      | prefix = ip_prefix
      | RANGE_ADDRESS lower_limit = ip_address TO upper_limit = ip_address
      | WILDCARD_ADDRESS ip_and_mask = ip_address_and_mask
   )
;

sead_address_set
:
   ADDRESS_SET name = junos_name
   (
      apply
      | seada_address
      | seada_address_set
      | seada_description
   )
;

sead_attach
:
   ATTACH ZONE name = junos_name
;

sead_description: description;

seada_address
:
   ADDRESS name = junos_name
;

seada_address_set
:
   ADDRESS_SET name = junos_name
;

seada_description
:
   description
;

sec_local
:
   LOCAL name = junos_name cert = certificate_string
;

seak_algorithm
:
   ALGORITHM
   (
      HMAC_SHA1
      | MD5
   )
;

seak_options
:
   OPTIONS
   (
      BASIC
      | ISIS_ENHANCED
   )
;

seak_secret
:
   SECRET key = secret_string
;

seak_start_time
:
   START_TIME time = null_filler
;

seik_gateway
:
   GATEWAY name = junos_name
   (
      seikg_address
      | seikg_dead_peer_detection
      | seikg_dynamic
      | seikg_external_interface
      | seikg_ike_policy
      | seikg_local_address
      | seikg_local_identity
      | seikg_no_nat_traversal
      | seikg_version
      | seikg_xauth
   )
;

seik_policy
:
   POLICY name = junos_name
   (
      seikp_description
      | seikp_mode
      | seikp_pre_shared_key
      | seikp_proposal_set
      | seikp_proposals
   )
;

seik_proposal
:
   PROPOSAL name = junos_name
   (
      seikpr_authentication_algorithm
      | seikpr_authentication_method
      | seikpr_description
      | seikpr_dh_group
      | seikpr_encryption_algorithm
      | seikpr_lifetime_seconds
   )
;

seikg_address
:
   ADDRESS ip_address
;

seikg_dead_peer_detection
:
   DEAD_PEER_DETECTION
   (
      seikgdpd_always_send
      | seikgdpd_interval
      | seikgdpd_optimized
      | seikgdpd_probe_idle_tunnel
      | seikgdpd_threshold
   )?
;

seikgdpd_always_send: ALWAYS_SEND;
seikgdpd_interval
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/security-edit-dead-peer-detection.html
   // 2-60, default 10
   INTERVAL secs = uint8
;
seikgdpd_optimized: OPTIMIZED;
seikgdpd_probe_idle_tunnel: PROBE_IDLE_TUNNEL;
seikgdpd_threshold
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/security-edit-dead-peer-detection.html
   // 1-5, default 5
   THRESHOLD max_failures = uint8
;

seikg_dynamic
:
   DYNAMIC
   (
      apply
      | seikgd_connections_limit
      | seikgd_hostname
      | seikgd_ike_user_type
   )
;

seikg_external_interface
:
   EXTERNAL_INTERFACE interface_id
;

seikg_ike_policy
:
   IKE_POLICY name = junos_name
;

seikg_local_address
:
   LOCAL_ADDRESS ip_address
;

seikg_local_identity
:
   LOCAL_IDENTITY
   (
      seikgl_inet
   )
;

seikg_no_nat_traversal
:
   NO_NAT_TRAVERSAL
;

seikg_version
:
   VERSION (V1_ONLY | V2_ONLY)
;

seikg_xauth
:
   XAUTH ACCESS_PROFILE name = junos_name
;

seikgd_connections_limit
:
   CONNECTIONS_LIMIT limit = dec
;

seikgd_hostname
:
   HOSTNAME name = junos_name
;

seikgd_ike_user_type
:
   IKE_USER_TYPE
   (
      GROUP_IKE_ID
      | SHARED_IKE_ID
   )
;

seikgl_inet
:
   INET name = junos_name
;

seikp_description
:
   description
;

seikp_mode
:
   MODE
   (
      AGGRESSIVE
      | MAIN
   )
;

seikp_pre_shared_key
:
   PRE_SHARED_KEY
   (
      ASCII_TEXT key = DOUBLE_QUOTED_STRING
      | HEXADECIMAL key = DOUBLE_QUOTED_STRING
      | SCRUBBED
   )
;

seikp_proposal_set
:
   PROPOSAL_SET proposal_set_type
;

seikp_proposals
:
   PROPOSALS junos_name_list
;

seikpr_authentication_algorithm
:
   AUTHENTICATION_ALGORITHM ike_authentication_algorithm
;

seikpr_authentication_method
:
   AUTHENTICATION_METHOD ike_authentication_method
;

seikpr_description
:
   description
;

seikpr_dh_group
:
   DH_GROUP dh_group
;

seikpr_encryption_algorithm
:
   ENCRYPTION_ALGORITHM encryption_algorithm
;

seikpr_lifetime_seconds
:
   LIFETIME_SECONDS seconds = dec
;

seip_policy
:
   POLICY name = junos_name
   (
      seipp_perfect_forward_secrecy
      | seipp_proposal_set
      | seipp_proposals
   )
;

seip_proposal
:
   PROPOSAL name = junos_name
   (
      apply
      | seippr_authentication_algorithm
      | seippr_description
      | seippr_encryption_algorithm
      | seippr_lifetime_kilobytes
      | seippr_lifetime_seconds
      | seippr_protocol
   )
;

seip_vpn
:
   VPN name = junos_name
   (
      seipv_bind_interface
      | seipv_df_bit
      | seipv_establish_tunnels
      | seipv_ike
      | seipv_vpn_monitor
   )
;

seipp_perfect_forward_secrecy
:
   PERFECT_FORWARD_SECRECY KEYS dh_group
;

seipp_proposal_set
:
   PROPOSAL_SET proposal_set_type
;

seipp_proposals
:
   PROPOSALS junos_name_list
;

seippr_authentication_algorithm
:
   AUTHENTICATION_ALGORITHM ipsec_authentication_algorithm
;

seippr_description
:
   description
;

seippr_encryption_algorithm
:
   ENCRYPTION_ALGORITHM encryption_algorithm
;

seippr_lifetime_kilobytes
:
   LIFETIME_KILOBYTES kilobytes = dec
;

seippr_lifetime_seconds
:
   LIFETIME_SECONDS seconds = dec
;

seippr_protocol
:
   PROTOCOL ipsec_protocol
;

seipv_bind_interface
:
   BIND_INTERFACE interface_id
;

seipv_df_bit
:
   DF_BIT (CLEAR| COPY)
;

seipv_establish_tunnels
:
   ESTABLISH_TUNNELS IMMEDIATELY
;

seipv_ike
:
   IKE
   (
      seipvi_gateway
      | seipvi_ipsec_policy
      | seipvi_null
      | seipvi_proxy_identity
   )
;

seipv_vpn_monitor
:
   VPN_MONITOR
   (
      apply
      | seipvv_destination_ip
      | seipvv_source_interface
   )
;

seipvi_gateway
:
   GATEWAY name = junos_name
;

seipvi_ipsec_policy
:
   IPSEC_POLICY name = junos_name
;

seipvi_null
:
   (
      NO_ANTI_REPLAY
   ) null_filler
;

seipvi_proxy_identity
:
   PROXY_IDENTITY
   (
      seipvip_local
      | seipvip_remote
      | seipvip_service
   )
;

seipvip_local
:
   LOCAL IP_PREFIX
;

seipvip_remote
:
   REMOTE IP_PREFIX
;

seipvip_service
:
   SERVICE
   (
      ANY
      | name = junos_name
   )
;

seipvv_destination_ip
:
   DESTINATION_IP IP_ADDRESS
;

seipvv_source_interface
:
   SOURCE_INTERFACE interface_id
;

sen_destination
:
   DESTINATION
   (
      nat_rule_set
      | nat_interface
      | nat_pool
      | nat_pool_utilization_alarm
      | nat_port_randomization
   )
;

sen_proxy_arp
:
   PROXY_ARP
   (
      apply
      | senp_interface
   )
;

sen_source
:
   SOURCE
   (
      nat_rule_set
      | nat_interface
      | nat_pool
      | nat_pool_utilization_alarm
      | nat_port_randomization
      | nat_pool_default_port_range
   )
;

sen_static
:
   STATIC nat_rule_set
;

senp_interface
:
   INTERFACE interface_id
   (
      apply
      | senpi_address
   )
;

senpi_address
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

sep_default_policy
:
   DEFAULT_POLICY
   (
      apply
      | DENY_ALL
      | PERMIT_ALL
   )
;

sep_from_zone
:
   FROM_ZONE from = zone TO_ZONE to = zone
   (
      apply
      | sepctx_policy
   )
;

sep_global
:
   GLOBAL
   (
      apply
      | sepctx_policy
   )
;

sepctx_policy
:
   POLICY name = junos_name
   (
      apply
      | sepctxp_description
      | sepctxp_match
      | sepctxp_then
   )
;

sepctxp_description
:
   description
;

sepctxp_match
:
   MATCH
   (
      sepctxpm_application
      | sepctxpm_destination_address
      | sepctxpm_destination_address_excluded
      | sepctxpm_source_address
      | sepctxpm_source_address_excluded
      | sepctxpm_source_identity
   )
;

sepctxp_then
:
   THEN
   (
      sepctxpt_count
      | sepctxpt_deny
      | sepctxpt_log
      | sepctxpt_permit
      | sepctxpt_trace
   )
;

sepctxpm_application
:
   APPLICATION
   (
      junos_application
      | junos_application_set
      | name = junos_name
   )
;

sepctxpm_destination_address
:
   DESTINATION_ADDRESS address_specifier
;

sepctxpm_destination_address_excluded
:
   DESTINATION_ADDRESS_EXCLUDED
;

sepctxpm_source_address
:
   SOURCE_ADDRESS address_specifier
;

sepctxpm_source_address_excluded
:
   SOURCE_ADDRESS_EXCLUDED
;

sepctxpm_source_identity
:
   SOURCE_IDENTITY
   (
      ANY
      | name = junos_name
   )
;

sepctxpt_count
:
   COUNT
;

sepctxpt_deny
:
   DENY
   | REJECT
;

sepctxpt_log
:
   LOG null_filler
;

sepctxpt_permit
:
   PERMIT
   (
      apply
      | sepctxptp_application_services
      | sepctxptp_services_offload
      | sepctxptp_tunnel
   )
;

sepctxptp_application_services
:
  APPLICATION_SERVICES null_filler
;

sepctxpt_trace
:
   TRACE
;

sepctxptp_services_offload
:
   SERVICES_OFFLOAD apply
;

sepctxptp_tunnel
:
   TUNNEL
   (
      apply
      | sepctxptpt_ipsec_vpn
   )
;

sepctxptpt_ipsec_vpn
:
   IPSEC_VPN name = junos_name
;

ses_ids_option
:
   IDS_OPTION name = junos_name
   (
      seso_alarm
      | seso_description
      | seso_icmp
      | seso_ip
      | seso_tcp
      | seso_udp
      | seso_limit_session
   )+
;

ses_null
:
   ( TRACEOPTIONS | TRAP ) null_filler
;

seso_alarm
:
   ALARM_WITHOUT_DROP
;

seso_description
:
   description
;

seso_icmp
:
   ICMP
   (
      sesoi_flood
      | sesoi_fragment
      | sesoi_icmpv6_malformed
      | sesoi_ip_sweep
      | sesoi_large
      | sesoi_ping_death
   )
;

seso_ip
:
   IP
   (
      sesop_bad_option
      | sesop_block_frag
      | sesop_ipv6_extension_header
      | sesop_ipv6_extension_header_limit
      | sesop_ipv6_malformed_header
      | sesop_loose_source_route_option
      | sesop_record_route_option
      | sesop_security_option
      | sesop_source_route_option
      | sesop_spoofing
      | sesop_stream_option
      | sesop_strict_source_route_option
      | sesop_tear_drop
      | sesop_timestamp_option
      | sesop_unknown_protocol
      | sesop_tunnel
   )
;

seso_limit_session
:
   LIMIT_SESSION
   (
      DESTINATION_IP_BASED dec
      | SOURCE_IP_BASED dec
   )
;

seso_tcp
:
   TCP
   (
      sesot_fin_no_ack
      | sesot_land
      | sesot_port_scan
      | sesot_syn_ack_ack_proxy
      | sesot_syn_fin
      | sesot_syn_flood
      | sesot_syn_frag
      | sesot_tcp_no_flag
      | sesot_tcp_sweep
      | sesot_winnuke
   )
;

seso_udp
:
   UDP
   (
      sesou_flood
      | sesou_port_scan
      | sesou_udp_sweep
   )
;

sesoi_flood
:
   FLOOD threshold
;

sesoi_fragment
:
   FRAGMENT
;

sesoi_icmpv6_malformed
:
   ICMPV6_MALFORMED
;

sesoi_ip_sweep
:
   IP_SWEEP threshold
;

sesoi_large
:
   LARGE
;

sesoi_ping_death
:
   PING_DEATH
;

sesop_bad_option
:
   BAD_OPTION
;

sesop_block_frag
:
   BLOCK_FRAG
;

sesop_ipv6_extension_header
:
   IPV6_EXTENSION_HEADER
   (
       AH_HEADER
       | ESP_HEADER
       | HIP_HEADER
       | sesop6_dst_header
       | FRAGMENT_HEADER
       | sesop6_hop_header
       | MOBILITY_HEADER
       | NO_NEXT_HEADER
       | ROUTING_HEADER
       | SHIM6_HEADER
       | sesop6_user_option
   )
;

sesop_ipv6_extension_header_limit
:
   IPV6_EXTENSION_HEADER_LIMIT limit=dec
;

sesop_ipv6_malformed_header
:
   IPV6_MALFORMED_HEADER
;

sesop_loose_source_route_option
:
   LOOSE_SOURCE_ROUTE_OPTION
;

sesop_record_route_option
:
   RECORD_ROUTE_OPTION
;

sesop_security_option
:
   SECURITY_OPTION
;

sesop_source_route_option
:
   SOURCE_ROUTE_OPTION
;

sesop_spoofing
:
   SPOOFING
;

sesop_stream_option
:
   STREAM_OPTION
;

sesop_strict_source_route_option
:
   STRICT_SOURCE_ROUTE_OPTION
;

sesop_tear_drop
:
   TEAR_DROP
;

sesop_timestamp_option
:
   TIMESTAMP_OPTION
;

sesop_tunnel
:
   TUNNEL
   (
      sesopt_gre
      | sesopt_ip_in_udp
      | sesopt_ipip
      | BAD_INNER_HEADER
   )
;

sesop_unknown_protocol
:
   UNKNOWN_PROTOCOL
;

sesop6_dst_header
:
   DESTINATION_HEADER
   (
      ILNP_NONCE_OPTION
      | HOME_ADDRESS_OPTION
      | LINE_IDENTIFICATION_OPTION
      | TUNNEL_ENCAPSULATION_LIMIT_OPTION
      | sesop6_user_option
   )
;

sesop6_hop_header
:
   HOP_BY_HOP_HEADER
   (
      CALIPSO_OPTION
      | RPL_OPTION
      | SFM_DPD_OPTION
      | JUMBO_PAYLOAD_OPTION
      | QUICK_START_OPTION
      | ROUTER_ALERT_OPTION
      | sesop6_user_option
   )
;

sesop6_user_option
:
   USER_DEFINED_OPTION_TYPE type_low=dec (TO type_high=dec)?
;

sesot_fin_no_ack
:
   FIN_NO_ACK
;

sesot_land
:
   LAND
;

sesot_port_scan
:
   PORT_SCAN threshold
;

sesot_syn_ack_ack_proxy
:
   SYN_ACK_ACK_PROXY threshold
;

sesot_syn_fin
:
   SYN_FIN
;

sesot_syn_flood
:
   SYN_FLOOD
   (
      sesots_alarm_thred
      | sesots_attack_thred
      | sesots_dst_thred
      | sesots_src_thred
      | sesots_timeout
      | sesots_whitelist
  )
;

sesot_syn_frag
:
   SYN_FRAG
;

sesot_tcp_no_flag
:
   TCP_NO_FLAG
;

sesot_tcp_sweep
:
   TCP_SWEEP threshold
;

sesot_winnuke
:
   WINNUKE
;

sesots_alarm_thred
:
   ALARM_THRESHOLD number=dec
;

sesots_attack_thred
:
   ATTACK_THRESHOLD number=dec
;

sesots_dst_thred
:
   DESTINATION_THRESHOLD number=dec
;

sesots_src_thred
:
   SOURCE_THRESHOLD number=dec
;

sesots_timeout
:
   TIMEOUT seconds=dec
;

sesots_whitelist
:
   WHITE_LIST name = junos_name
   (
      sesotsw_dst
      | sesotsw_src
   )*
;

sesotsw_dst
:
   DESTINATION_ADDRESS address=IP_ADDRESS
;

sesotsw_src
:
   SOURCE_ADDRESS address=IP_ADDRESS
;

sesou_flood
:
   FLOOD threshold
;

sesou_port_scan
:
   PORT_SCAN threshold
;

sesou_udp_sweep
:
   UDP_SWEEP threshold
;


sesopt_gre
:
   GRE
   (
      GRE_4IN4
      | GRE_4IN6
      | GRE_6IN4
      | GRE_6IN6
   )
;

sesopt_ip_in_udp
:
   IP_IN_UDP TEREDO
;

sesopt_ipip
:
   IPIP
   (
      IPIP_4IN4
      | IPIP_4IN6
      | IPIP_6IN4
      | IPIP_6IN6
      | IPIP_6OVER4
      | IPIP_6TO4RELAY
      | ISATAP
      | DSLITE
   )
;

sez_security_zone
:
   SECURITY_ZONE (zone | wildcard)
   (
      apply
      | sezs_address_book
      | sezs_application_tracking
      | sezs_host_inbound_traffic
      | sezs_interfaces
      | sezs_screen
      | sezs_tcp_rst
   )
;

sezs_address_book
:
   ADDRESS_BOOK
   (
      apply
      | sezsa_address
      | sezsa_address_set
   )
;

sezs_application_tracking
:
   APPLICATION_TRACKING
;

sezs_host_inbound_traffic
:
   HOST_INBOUND_TRAFFIC
   (
      apply
      | sezsh_protocols
      | sezsh_system_services
   )
;

sezs_interfaces
:
   INTERFACES (interface_id | wildcard)
   (
      apply
      | sezs_host_inbound_traffic
   )
;

sezs_screen
:
   SCREEN
   (
      UNTRUST_SCREEN
      | name = junos_name
   )
;

sezs_tcp_rst
:
   TCP_RST
;

sezsa_address
:
   ADDRESS name = junos_name
   (
      address = ip_address
      | prefix = ip_prefix
      | WILDCARD_ADDRESS ip_and_mask = ip_address_and_mask
   )
;

sezsa_address_set
:
   ADDRESS_SET name = junos_name
   (
      apply
      | sezsaad_address
      | sezsaad_address_set
   )
;

sezsaad_address
:
   ADDRESS name = junos_name
;

sezsaad_address_set
:
   ADDRESS_SET name = junos_name
;

sezsh_protocols
:
   PROTOCOLS hib_protocol
;

sezsh_system_services
:
   SYSTEM_SERVICES hib_system_service
;

zone
:
   JUNOS_HOST
   | TRUST
   | UNTRUST
   | name = junos_name
;
