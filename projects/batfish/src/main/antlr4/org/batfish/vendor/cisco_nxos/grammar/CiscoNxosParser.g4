parser grammar CiscoNxosParser;

import
  CiscoNxos_common,
  CiscoNxos_aaa,
  CiscoNxos_bgp,
  CiscoNxos_class_map,
  CiscoNxos_crypto,
  CiscoNxos_dhcp,
  CiscoNxos_eigrp,
  CiscoNxos_evpn,
  CiscoNxos_fex,
  CiscoNxos_flow,
  CiscoNxos_interface,
  CiscoNxos_ip_access_list,
  CiscoNxos_ip_as_path_access_list,
  CiscoNxos_ip_community_list,
  CiscoNxos_ip_prefix_list,
  CiscoNxos_ipv6_access_list,
  CiscoNxos_ipv6_prefix_list,
  CiscoNxos_lacp,
  CiscoNxos_line,
  CiscoNxos_logging,
  CiscoNxos_mac,
  CiscoNxos_monitor,
  CiscoNxos_ntp,
  CiscoNxos_object_group,
  CiscoNxos_ospf,
  CiscoNxos_ospfv3,
  CiscoNxos_policy_map,
  CiscoNxos_rip,
  CiscoNxos_rmon,
  CiscoNxos_route_map,
  CiscoNxos_snmp,
  CiscoNxos_static,
  CiscoNxos_tacacs_server,
  CiscoNxos_vdc,
  CiscoNxos_vlan,
  CiscoNxos_vrf;

options {
  superClass = 'org.batfish.vendor.cisco_nxos.grammar.CiscoNxosBaseParser';
  tokenVocab = CiscoNxosLexer;
}

cisco_nxos_configuration
:
  NEWLINE?
  statement+ EOF
;

statement
:
  s_aaa
  | s_banner
  | s_boot
  | s_class_map
  | s_control_plane
  | s_crypto
  | s_evpn
  | s_fabric
  | s_fex
  | s_flow
  | s_hostname
  | s_interface
  | s_ip
  | s_ipv6
  | s_key
  | s_lacp
  | s_line
  | s_logging
  | s_mac
  | s_monitor
  | s_no
  | s_ntp
  | s_null
  | s_nv
  | s_object_group
  | s_policy_map
  | s_rmon
  | s_role
  | s_route_map
  | s_router
  | s_snmp_server
  | s_system
  | s_tacacs_server
  | s_track
  | s_version
  | s_vdc
  | s_vlan
  | s_vrf_context
  | s_xml
;

s_banner
:
  BANNER
  (
    banner_exec
    | banner_motd
  )
;

banner_exec
:
  EXEC BANNER_DELIMITER body=BANNER_BODY? BANNER_DELIMITER NEWLINE
;

banner_motd
:
  MOTD BANNER_DELIMITER body=BANNER_BODY? BANNER_DELIMITER NEWLINE
;

s_boot
:
  BOOT
  (
    boot_kickstart
    | boot_null
    | boot_nxos
    | boot_system
  )
;

boot_kickstart
:
  KICKSTART image = WORD (SUP_1 | SUP_2)? NEWLINE
;

boot_null
:
  (
    POAP
  ) null_rest_of_line
;

boot_nxos
:
  NXOS image = WORD (SUP_1 | SUP_2)? NEWLINE
;

boot_system
:
  SYSTEM image = WORD (SUP_1 | SUP_2)? NEWLINE
;

s_control_plane
:
  CONTROL_PLANE NEWLINE cp_service_policy*
;

cp_service_policy
:
  SERVICE_POLICY INPUT name = policy_map_cp_name NEWLINE
;

s_fabric: FABRIC (s_fabric_forwarding | s_fabric_null);

s_fabric_forwarding
:
  FORWARDING
  (
    ff_admin_distance
    | ff_anycast_gateway_mac
    | ff_null
  )
;

ff_admin_distance: ADMIN_DISTANCE dist = protocol_distance NEWLINE;

ff_anycast_gateway_mac: ANYCAST_GATEWAY_MAC mac = mac_address_literal NEWLINE;

ff_null
:
  (
    DUP_HOST_IP_ADDR_DETECTION
    | DUP_HOST_RECOVERY_TIMER
    | DUP_HOST_UNFREEZE_TIMER
    | LIMIT_VLAN_MAC
  ) null_rest_of_line
;

s_fabric_null: DATABASE null_rest_of_line;

s_hostname
:
  (HOSTNAME | SWITCHNAME) hostname = subdomain_name NEWLINE
;

s_ip
:
  IP
  (
    ip_access_list
    | ip_as_path_access_list
    | ip_community_list
    | ip_dhcp
    | ip_domain_name
    | ip_name_server
    | ip_null
    | ip_pim
    | ip_prefix_list
    | ip_route
    | ip_tacacs
    | ip_sla
  )
;

ip_domain_name
:
  DOMAIN_NAME domain = domain_name NEWLINE
;

domain_name
:
// 1-64 characters
  WORD
;

ip_name_server
:
  NAME_SERVER servers += name_server+ (USE_VRF vrf = vrf_name)? NEWLINE
;

name_server
:
  ip_address
  | ipv6_address
;

ip_null
:
  (
    ARP
    | DOMAIN_LIST
    | DOMAIN_LOOKUP
  ) null_rest_of_line
;

ip_pim
:
  PIM
  (
    ipp_rp_address
    | ipp_rp_candidate
  )
;

ipp_rp_address
:
  RP_ADDRESS ip = ip_address
  (
    GROUP_LIST ip_prefix
    | PREFIX_LIST pl = ip_prefix_list_name
    | ROUTE_MAP map = route_map_name
  )? BIDIR? OVERRIDE? NEWLINE
;

ipp_rp_candidate
:
  RP_CANDIDATE interface_name
  (
    GROUP_LIST ip_prefix
    | PREFIX_LIST pl = ip_prefix_list_name
    | ROUTE_MAP rm = route_map_name
  ) NEWLINE
;

ip_sla
:
  SLA
  (
    ip_sla_block
    | ip_sla_null
  )
;

ip_sla_block
:
  entry = uint32 NEWLINE
  ip_sla_entry+
;

ip_sla_entry
:
  (
    DNS
    | FREQUENCY
    | HTTP
    | ICMP_ECHO
    | TCP_CONNECT
    | THRESHOLD
    | TIMEOUT
    | UDP_ECHO
    | UDP_JITTER
    | VRF
  ) null_rest_of_line
;

ip_sla_null
:
  (
    GROUP
    | LOGGING
    | REACTION_CONFIGURATION
    | REACTION_TRIGGER
    | RESET
    | RESPONDER
    | SCHEDULE
  ) null_rest_of_line
;

s_ipv6
:
  IPV6
  (
    ipv6_access_list
    | ipv6_dhcp
    | ipv6_prefix_list
    | ipv6_route
  )
;

s_key
:
  KEY key_chain
;

key_chain
:
  CHAIN name = key_chain_name NEWLINE kc_key*
;

kc_key
:
  KEY num = uint16 NEWLINE kck_key_string*
;

kck_key_string
:
  KEY_STRING key_text = key_string_text NEWLINE
;

key_string_text
:
// 1-63 characters
  REMARK_TEXT
;

s_null
:
  (
    CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | LICENSE
    | SERVICE
    | SPANNING_TREE
    | SSH
    | USERNAME
    | USERPASSPHRASE
  ) null_rest_of_line
  | s_null_text
;

// Null statements that use M_NullLine lexer mode (accepts arbitrary text)
s_null_text
:
  (
    HA_POLICY
    | HARDWARE
    | KEYSTORE
    | POWER
    | TELNET
  ) NULL_LINE_TEXT? NEWLINE
;

s_no
:
  NO
  (
    no_ip
    | no_system
    // no_null should come last
    | no_null
  )
;

no_ip: IP (no_ip_route | no_ip_null);

no_ip_null
:
  (
    ADJACENCY
    | ADJMGR
    | AMT
    | ARP
    | AUTO_DISCARD
    | DNS
    | DOMAIN_LIST
    | DOMAIN_LOOKUP
    | DSCP_LOP
    | EXTCOMMUNITY_LIST
    | HOST
    | IGMP
    | INTERNAL
    | LOAD_SHARING
    | MFWD
    | MROUTE
    | MULTICAST
    | RADIUS
    | ROUTING
    | SOURCE_ROUTE
    | TCP
    | TELNET
    | TFTP
  ) null_rest_of_line
;

no_null
:
  HARDWARE NULL_LINE_TEXT? NEWLINE
  | (
    FEATURE
    | IP
    | NTP
  ) null_rest_of_line
;

no_system
:
  SYSTEM
  (
    no_sys_default
    | no_sys_null
  )
;

no_sys_default
:
  DEFAULT no_sysd_switchport
;

no_sys_null
:
  (
    INTERFACE
    | MODE
  ) null_rest_of_line
;

no_sysd_switchport
:
  SWITCHPORT
  (
    no_sysds_shutdown
    | no_sysds_switchport
  )
;

no_sysds_shutdown
:
  SHUTDOWN NEWLINE
;

no_sysds_switchport
:
  NEWLINE
;

s_nv
:
  NV OVERLAY EVPN NEWLINE
;

s_role
:
  ROLE NAME name = role_name NEWLINE role_null*
;

role_name
:
// 1-16 characters
  WORD
;

role_null
:
  (
    DESCRIPTION
    | RULE
  ) null_rest_of_line
;

s_router
:
  ROUTER
  (
    router_bgp
    | router_eigrp
    | router_ospf
    | router_ospfv3
    | router_rip
  )
;

s_system
:
  SYSTEM
  (
    sys_default
    | sys_qos
    | sys_vlan
  )
;

sys_default
:
  DEFAULT sysd_switchport
;

sysd_switchport
:
  SWITCHPORT
  (
    sysds_shutdown
    | sysds_switchport
  )
;

sysds_shutdown
:
  SHUTDOWN NEWLINE
;

sysds_switchport
:
  NEWLINE
;

sys_qos
:
  QOS NEWLINE
  (
    sysqos_null
    | sysqos_service_policy
  )*
;

sysqos_null
:
  (
    FEX
  ) null_rest_of_line
;

sysqos_service_policy
:
  SERVICE_POLICY TYPE 
  (
    sysqosspt_network_qos
    | sysqosspt_qos
    | sysqosspt_queueing
  )
;

sysqosspt_network_qos
:
  NETWORK_QOS name = policy_map_network_qos_name NEWLINE
;

sysqosspt_qos
:
  QOS INPUT name = policy_map_qos_name NEWLINE
;

sysqosspt_queueing
:
  QUEUING (INPUT | OUTPUT) name = policy_map_queuing_name NEWLINE
;

sys_vlan
:
  VLAN sysvlan_reserve
;

sysvlan_reserve
:
  // 2-3968
  first = vlan_id RESERVE NEWLINE
;

s_track: TRACK num = track_object_id track_definition;

track_definition
:
  track_interface
  | track_ip
;

track_interface: INTERFACE interface_name track_interface_mode NEWLINE;

track_interface_mode
:
  LINE_PROTOCOL
  | IP ROUTING
  | IPV6 ROUTING
;

// Currently unsupported tracking structures
track_ip
:
  IP
  (
    track_ip_route
    | track_ip_sla
  )
;

track_ip_route
:
  ROUTE prefix = ip_prefix REACHABILITY HMM? NEWLINE
  track_ip_route_inner*
;

track_ip_route_inner
:
  tir_vrf
  | tir_null
;

tir_vrf: VRF MEMBER name = vrf_name NEWLINE;

tir_null: DELAY null_rest_of_line;

track_ip_sla
:
  SLA null_rest_of_line
;

s_version
:
// arbitray string, not actual command
  VERSION version = REMARK_TEXT NEWLINE
;

s_xml: XML SERVER VALIDATE ALL NEWLINE;