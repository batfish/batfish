parser grammar CiscoXrParser;

import
CiscoXr_common,
CiscoXr_aaa,
CiscoXr_acl,
CiscoXr_as_path_set,
CiscoXr_bfd,
CiscoXr_bgp,
CiscoXr_community_set,
CiscoXr_crypto,
CiscoXr_callhome,
CiscoXr_eigrp,
CiscoXr_extcommunity_set,
CiscoXr_flow,
CiscoXr_hsrp,
CiscoXr_igmp,
CiscoXr_interface,
CiscoXr_isis,
CiscoXr_line,
CiscoXr_lldp,
CiscoXr_logging,
CiscoXr_mld,
CiscoXr_mpls,
CiscoXr_msdp,
CiscoXr_multicast_routing,
CiscoXr_ntp,
CiscoXr_ospf,
CiscoXr_pim,
CiscoXr_qos,
CiscoXr_rd_set,
CiscoXr_rip,
CiscoXr_rpl,
CiscoXr_snmp,
CiscoXr_static,
CiscoXr_tftp,
CiscoXr_vrf,
CiscoXr_vrrp
;


options {
   superClass = 'org.batfish.grammar.cisco_xr.parsing.CiscoXrBaseParser';
   tokenVocab = CiscoXrLexer;
}

////////////////////////////////////////////////////////////////////////////////////

cisco_xr_configuration
:
  (
    statement
    | stanza
    | NEWLINE
  )+
  EOF
;

// statement is for rewritten top-level rules. stanza is for old ones.
statement
:
  s_as_path_set
  | s_building_configuration_null
  | s_cdp
  | s_cef
  | s_clock
  | s_confdconfig_null
  | s_configuration
  | s_end
  | s_flow
  | s_fpd_null
  | s_fri_null
  | s_ipv4
  | s_ipv6
  | s_isolation
  | s_lldp
  | s_mon_null
  | s_mpls
  | s_multicast_routing
  | s_no
  | s_nsr
  | s_rd_set
  | s_router
  | s_sampler_map
  | s_sat_null
  | s_sun_null
  | s_taskgroup
  | s_tcp
  | s_telnet
  | s_tftp
  | s_thu_null
  | s_tue_null
  | s_usergroup
  | s_vty_pool_null
  | s_wed_null
;

s_cdp
:
  CDP
  (
    cdp_advertise_null
    | cdp_holdtime_null
    | cdp_log_null
    | cdp_timer_null
    | NEWLINE
  )
;

cdp_advertise_null
:
   ADVERTISE null_rest_of_line
;
cdp_holdtime_null
:
   HOLDTIME null_rest_of_line
;
cdp_log_null
:
   LOG null_rest_of_line
;
cdp_timer_null
:
   TIMER null_rest_of_line
;

s_cef: CEF (
   cef_adjacency_null
   | cef_load_balancing_null
   | cef_pbts_null
   | cef_platform_null
);

cef_adjacency_null
:
   ADJACENCY null_rest_of_line
;
cef_load_balancing_null
:
   LOAD_BALANCING null_rest_of_line
;
cef_pbts_null
:
   PBTS null_rest_of_line
;
cef_platform_null
:
   PLATFORM null_rest_of_line
;

s_end: END NEWLINE;

s_clock: CLOCK TIMEZONE null_rest_of_line;

s_configuration: CONFIGURATION (
   configuration_commit_null
   | configuration_display_null
   | configuration_mode_null
);

configuration_commit_null
:
   COMMIT null_rest_of_line
;
configuration_display_null
:
   DISPLAY null_rest_of_line
;
configuration_mode_null
:
   MODE null_rest_of_line
;

s_ipv4
:
  IPV4
  (
    ipv4_access_list
    | ipv4_conflict_policy
    | ipv4_netmask_format_null
    | ipv4_virtual_null
  )
;

ipv4_conflict_policy
:
  CONFLICT_POLICY
  (
    HIGHEST_IP
    | LONGEST_PREFIX
    | STATIC
  ) NEWLINE
;

ipv4_netmask_format_null
:
   NETMASK_FORMAT null_rest_of_line
;
ipv4_virtual_null
:
   VIRTUAL null_rest_of_line
;

s_ipv6
:
  IPV6 ipv6_access_list
;

s_isolation: ISOLATION ENABLE NEWLINE;

s_no
:
  NO
  (
    no_ipv4
    | no_ipv6
  )
;

no_ipv4
:
  IPV4 no_ipv4_access_list
;

no_ipv6
:
  IPV6 no_ipv6_access_list
;

s_nsr: NSR PROCESS_FAILURES SWITCHOVER NEWLINE;

s_building_configuration_null
:
   BUILDING_CONFIGURATION null_rest_of_line
;
s_confdconfig_null
:
   CONFDCONFIG null_rest_of_line
;
s_fpd_null
:
   FPD null_rest_of_line
;
s_fri_null
:
   FRI null_rest_of_line
;
s_mon_null
:
   MON null_rest_of_line
;
s_sat_null
:
   SAT null_rest_of_line
;
s_sun_null
:
   SUN null_rest_of_line
;
s_thu_null
:
   THU null_rest_of_line
;
s_tue_null
:
   TUE null_rest_of_line
;
s_vty_pool_null
:
   VTY_POOL null_rest_of_line
;
s_wed_null
:
   WED null_rest_of_line
;

s_router
:
  ROUTER
  (
    router_hsrp
    | router_igmp
    | router_mld
    | router_msdp
    | router_pim
    | router_vrrp
  )
;

s_taskgroup: TASKGROUP null_rest_of_line taskgroup_inner*;

taskgroup_inner
:
  taskgroup_inherit
  | taskgroup_task
;

taskgroup_inherit: INHERIT null_rest_of_line;

taskgroup_task
:
  (
    TASK
    | TASK_SPACE_EXECUTE
  )
  null_rest_of_line;

s_tcp: TCP (
   tcp_accept_rate_null
   | tcp_directory_null
   | tcp_mss_null
   | tcp_num_thread_null
   | tcp_path_mtu_discovery_null
   | tcp_receive_queue_null
   | tcp_selective_ack_null
   | tcp_synwait_time_null
   | tcp_throttle_null
   | tcp_timestamp_null
   | tcp_window_size_null
);

tcp_accept_rate_null
:
   ACCEPT_RATE null_rest_of_line
;
tcp_directory_null
:
   DIRECTORY null_rest_of_line
;
tcp_mss_null
:
   MSS null_rest_of_line
;
tcp_num_thread_null
:
   NUM_THREAD null_rest_of_line
;
tcp_path_mtu_discovery_null
:
   PATH_MTU_DISCOVERY null_rest_of_line
;
tcp_receive_queue_null
:
   RECEIVE_QUEUE null_rest_of_line
;
tcp_selective_ack_null
:
   SELECTIVE_ACK null_rest_of_line
;
tcp_synwait_time_null
:
   SYNWAIT_TIME null_rest_of_line
;
tcp_throttle_null
:
   THROTTLE null_rest_of_line
;
tcp_timestamp_null
:
   TIMESTAMP null_rest_of_line
;
tcp_window_size_null
:
   WINDOW_SIZE null_rest_of_line
;

s_telnet: TELNET (
   telnet_ipv4_null
   | telnet_ipv6_null
   | telnet_vrf_null
);

telnet_ipv4_null
:
   IPV4 null_rest_of_line
;
telnet_ipv6_null
:
   IPV6 null_rest_of_line
;
telnet_vrf_null
:
   VRF null_rest_of_line
;

s_usergroup: USERGROUP name = usergroup_name NEWLINE usergroup_inner*;

usergroup_inner:
 usergroup_description_null
 | usergroup_inherit_null
 | usergroup_taskgroup_null
;

usergroup_description_null
:
   DESCRIPTION null_rest_of_line
;
usergroup_inherit_null
:
   INHERIT null_rest_of_line
;
usergroup_taskgroup_null
:
   TASKGROUP null_rest_of_line
;

////////////////////////////////////////////////////////////////////////////////////

address_aiimgp_stanza
:
   ADDRESS null_rest_of_line
;

aiimgp_stanza
:
   address_aiimgp_stanza
;

allow_iimgp_stanza
:
   ALLOW null_rest_of_line aiimgp_stanza*
;

cp_management_plane
:
   MANAGEMENT_PLANE NEWLINE mgp_stanza*
;

cp_null
:
   NO?
   (
      EXIT
      | SCALE_FACTOR
   ) null_rest_of_line
;

cp_service_policy
:
   SERVICE_POLICY
   (
      INPUT
      | OUTPUT
   ) name = variable NEWLINE
;

dhcp_null
:
   NO?
   (
      INTERFACE
   ) null_rest_of_line
;

dhcp_profile
:
   NO? PROFILE null_rest_of_line
   (
      dhcp_profile_null
   )*
;

dhcp_profile_null
:
   NO?
   (
      DEFAULT_ROUTER
      | DOMAIN_NAME
      | DNS_SERVER
      | HELPER_ADDRESS
      | LEASE
      | POOL
      | SUBNET_MASK
   ) null_rest_of_line
;

domain_lookup
:
   LOOKUP
   (
      SOURCE_INTERFACE iname = interface_name
      | DISABLE
   ) NEWLINE
;

domain_name
:
   NAME hostname = variable_hostname NEWLINE
;

domain_name_server
:
   NAME_SERVER hostname = variable_hostname NEWLINE
;

event_null
:
   NO?
   (
      ACTION
      | EVENT
      | SET
   ) null_rest_of_line
;

iimgp_stanza
:
   allow_iimgp_stanza
;

imgp_stanza
:
   interface_imgp_stanza
   | null_imgp_stanza
;

inband_mgp_stanza
:
   (
      INBAND
      | OUT_OF_BAND
   ) NEWLINE imgp_stanza*
;

interface_imgp_stanza
:
   INTERFACE null_rest_of_line iimgp_stanza*
;

ispla_operation
:
   NO? OPERATION null_rest_of_line
   (
      ipslao_type
   )*
;

ipsla_reaction
:
   NO? REACTION null_rest_of_line
   (
      ipslar_react
   )*
;

ipsla_responder
:
   NO? RESPONDER null_rest_of_line
   (
      ipslarp_null
   )*
;

ipsla_schedule
:
   NO? SCHEDULE null_rest_of_line
   (
      ipslas_null
   )*
;

ipslao_type
:
   NO? TYPE null_rest_of_line
   (
      ipslaot_null
      | ipslaot_statistics
   )*
;

ipslaot_null
:
   NO?
   (
      DESTINATION
      | FREQUENCY
      | SOURCE
      | TIMEOUT
      | TOS
      | VERIFY_DATA
   ) null_rest_of_line
;

ipslaot_statistics
:
   NO? STATISTICS null_rest_of_line
   (
      ipslaots_null
   )*
;

ipslaots_null
:
   NO?
   (
      BUCKETS
   ) null_rest_of_line
;

ipslar_react
:
   NO? REACT null_rest_of_line
   (
      ispalrr_null
   )*
;

ipslarp_null
:
   NO?
   (
      TYPE
   ) null_rest_of_line
;

ispalrr_null
:
   NO?
   (
      ACTION
      | THRESHOLD
   ) null_rest_of_line
;

ipslas_null
:
   NO?
   (
      LIFE
      | START_TIME
   ) null_rest_of_line
;

l2tpc_null
:
   NO? DEFAULT?
   (
      AUTHENTICATION
      | COOKIE
      | HELLO
      | HIDDEN_LITERAL
      | HOSTNAME
      | PASSWORD
      | RECEIVE_WINDOW
      | RETRANSMIT
      | TIMEOUT
   ) null_rest_of_line
;

l2vpn_bridge_group
:
   BRIDGE GROUP bridge_group_name NEWLINE
   (
      lbg_bridge_domain
   )*
;

l2vpn_logging
:
   LOGGING NEWLINE
   (
      (
         BRIDGE_DOMAIN
         | PSEUDOWIRE
         | VFI
      ) NEWLINE
   )+
;

l2vpn_xconnect
:
   XCONNECT GROUP variable NEWLINE
   (
      l2vpn_xconnect_p2p
   )*
;

l2vpn_xconnect_p2p
:
   NO? P2P null_rest_of_line
   (
      lxp_neighbor
      | lxp_null
   )*
;

lbg_bridge_domain
:
   BRIDGE_DOMAIN bridge_domain_name NEWLINE
   (
      lbgbd_mac
      | lbgbd_interface
      | lbgbd_routed_interface
      | lbgbd_null
      | lbgbd_vfi
   )*
;

lbgbd_interface: INTERFACE interface_name NEWLINE lbgbdi_inner*;

lbgbdi_inner: lbgbdi_null;

lbgbdi_null: STORM_CONTROL null_rest_of_line;

lbgbd_routed_interface: ROUTED INTERFACE interface_name NEWLINE;

lbgbd_mac
:
   NO? MAC null_rest_of_line
   (
      lbgbdm_limit
   )*
;

lbgbd_null
:
   NO?
   (
      MTU
      | NEIGHBOR
   ) null_rest_of_line
;

lbgbd_vfi
:
   NO? VFI null_rest_of_line
   (
      lbgbdv_null
   )*
;

lbgbdm_limit
:
   NO? LIMIT null_rest_of_line
   (
      lbgbdml_null
   )*
;

lbgbdml_null
:
   NO?
   (
      ACTION
      | MAXIMUM
   ) null_rest_of_line
;

lbgbdv_null
:
   NO?
   (
      NEIGHBOR
   ) null_rest_of_line
;

lpts_null
:
   NO?
   (
      FLOW
   ) null_rest_of_line
;

lxp_neighbor
:
   NO? NEIGHBOR null_rest_of_line
   (
      lxpn_l2tp
      | lxpn_null
   )*
;

lxp_null
:
   NO?
   (
      INTERFACE
      | MONITOR_SESSION
   ) null_rest_of_line
;

lxpn_null
:
   NO?
   (
      SOURCE
   ) null_rest_of_line
;

lxpn_l2tp
:
   NO? L2TP null_rest_of_line
   (
      lxpnl_null
   )*
;

lxpnl_null
:
   NO?
   (
      LOCAL
      | REMOTE
   ) null_rest_of_line
;

mgp_stanza
:
   inband_mgp_stanza
;

no_aaa_group_server_stanza
:
   NO AAA GROUP SERVER null_rest_of_line
;

null_imgp_stanza
:
   NO?
   (
      VRF
   ) null_rest_of_line
;

s_banner_ios
:
  banner_header = ios_banner_header banner = ios_delimited_banner NEWLINE
;

ios_banner_header
:
  BANNER_IOS
  | BANNER_CONFIG_SAVE_IOS
  | BANNER_EXEC_IOS
  | BANNER_INCOMING_IOS
  | BANNER_LOGIN_IOS
  | BANNER_MOTD_IOS
  | BANNER_PROMPT_TIMEOUT_IOS
  | BANNER_SLIP_PPP_IOS
;

s_control_plane
:
   CONTROL_PLANE
   (
      SLOT uint_legacy
   )? NEWLINE s_control_plane_tail*
;

s_control_plane_tail
:
   cp_management_plane
   | cp_null
   | cp_service_policy
;

s_dhcp
:
   NO? DHCP null_rest_of_line
   (
      dhcp_null
      | dhcp_profile
   )*
;

s_domain
:
   DOMAIN
   (
      VRF vrf = vrf_name
   )?
   (
      domain_lookup
      | domain_name
      | domain_name_server
   )
;

s_event
:
   NO? EVENT null_rest_of_line
   (
      event_null
   )*
;

s_hostname: HOSTNAME hostname = host_name NEWLINE;

host_name: WORD;

s_ipsla
:
   NO? IPSLA null_rest_of_line
   (
      ispla_operation
      | ipsla_reaction
      | ipsla_responder
      | ipsla_schedule
   )*
;

s_l2tp_class
:
   NO? L2TP_CLASS name = variable NEWLINE
   (
      l2tpc_null
   )*
;

s_l2vpn
:
   NO? L2VPN null_rest_of_line
   (
      l2vpn_bridge_group
      | l2vpn_logging
      | l2vpn_xconnect
   )*
;

s_lpts
:
   NO? LPTS null_rest_of_line
   (
      lpts_null
   )*
;

s_no_bfd
:
   NO BFD null_rest_of_line
;

s_radius_server
:
   RADIUS SERVER name = variable NEWLINE
   (
      (
         ADDRESS
         | KEY
         | RETRANSMIT
         | TIMEOUT
      ) null_rest_of_line
   )+
;

s_service
:
   NO? SERVICE
   (
      words += variable
   )+ NEWLINE
;

s_service_policy_global
:
   SERVICE_POLICY name = variable GLOBAL NEWLINE
;

s_spanning_tree
:
   NO? SPANNING_TREE
   (
      spanning_tree_backbonefast_null
      | spanning_tree_bpdufilter_null
      | spanning_tree_bridge_null
      | spanning_tree_cost_null
      | spanning_tree_dispute_null
      | spanning_tree_etherchannel_null
      | spanning_tree_extend_null
      | spanning_tree_fcoe_null
      | spanning_tree_guard_null
      | spanning_tree_logging_null
      | spanning_tree_loopguard_null
      | spanning_tree_mode_null
      | spanning_tree_mst
      | spanning_tree_optimize_null
      | spanning_tree_pathcost_null
      | spanning_tree_port_null
      | spanning_tree_portfast
      | spanning_tree_pseudo_information
      | spanning_tree_uplinkfast_null
      | spanning_tree_vlan_null
      | NEWLINE
   )
;

s_ssh
:
   SSH
   (
      ssh_client
      | ssh_ip_address_null
      | ssh_key_exchange_null
      | ssh_key_null
      | ssh_login_attempts_null
      | ssh_mgmt_auth_null
      | ssh_server
      | ssh_stricthostkeycheck_null
      | ssh_timeout
      | ssh_version_null
   )
;

s_statistics
:
   NO? STATISTICS null_rest_of_line
   (
      statistics_null
   )*
;

s_tacacs
:
   TACACS
   (
      t_group_null
      | t_host_null
      | t_server
      | t_source_interface
   )
;

s_tacacs_server
:
   NO? TACACS_SERVER
   (
      ts_common
      | ts_host
      |
      (
         ts_host ts_common*
      )
   )
;

s_template
:
  TEMPLATE null_rest_of_line
  (
    template_null
  )*
;

s_track
:
  TRACK name = variable
  (
    track_block
    | track_interface
    | track_list
  )
;

s_username
:
   USERNAME
   (
      quoted_user = double_quoted_string
      | user = variable
   )
   (
      (
         u+ NEWLINE
      )
      |
      (
         NEWLINE
         (
            u NEWLINE
         )*
      )
   )
;

s_username_attributes
:
   USERNAME user = variable ATTRIBUTES NEWLINE
   (
      (
         ua_group_lock_null
         | ua_vpn_group_policy_null
      )
   )*
;

spanning_tree_mst
:
   MST null_rest_of_line spanning_tree_mst_null*
;

spanning_tree_mst_null
:
   NO?
   (
      INSTANCE
      | NAME
      | REVISION
   ) null_rest_of_line
;

spanning_tree_portfast
:
   PORTFAST
   (
      bpdufilter = BPDUFILTER
      | bpduguard = BPDUGUARD
      | defaultLiteral = DEFAULT
      | edge = EDGE
   )* NEWLINE
;

spanning_tree_pseudo_information
:
   PSEUDO_INFORMATION NEWLINE
   (
      spti_null
   )*
;

spanning_tree_backbonefast_null
:
   BACKBONEFAST null_rest_of_line
;
spanning_tree_bpdufilter_null
:
   BPDUFILTER null_rest_of_line
;
spanning_tree_bridge_null
:
   BRIDGE null_rest_of_line
;
spanning_tree_cost_null
:
   COST null_rest_of_line
;
spanning_tree_dispute_null
:
   DISPUTE null_rest_of_line
;
spanning_tree_etherchannel_null
:
   ETHERCHANNEL null_rest_of_line
;
spanning_tree_extend_null
:
   EXTEND null_rest_of_line
;
spanning_tree_fcoe_null
:
   FCOE null_rest_of_line
;
spanning_tree_guard_null
:
   GUARD null_rest_of_line
;
spanning_tree_logging_null
:
   LOGGING null_rest_of_line
;
spanning_tree_loopguard_null
:
   LOOPGUARD null_rest_of_line
;
spanning_tree_mode_null
:
   MODE null_rest_of_line
;
spanning_tree_optimize_null
:
   OPTIMIZE null_rest_of_line
;
spanning_tree_pathcost_null
:
   PATHCOST null_rest_of_line
;
spanning_tree_port_null
:
   PORT null_rest_of_line
;
spanning_tree_uplinkfast_null
:
   UPLINKFAST null_rest_of_line
;
spanning_tree_vlan_null
:
   VLAN null_rest_of_line
;

spti_null
:
   NO?
   (
      MST
   ) null_rest_of_line
;

srlg_interface_numeric_stanza
:
   uint_legacy null_rest_of_line
;

srlg_interface_stanza
:
   INTERFACE null_rest_of_line srlg_interface_numeric_stanza*
;

srlg_stanza
:
   SRLG NEWLINE srlg_interface_stanza*
;

ssh_client
:
   CLIENT null_rest_of_line
;

ssh_ip_address_null
:
   IP_ADDRESS null_rest_of_line
;
ssh_key_null
:
   KEY null_rest_of_line
;
ssh_key_exchange_null
:
   KEY_EXCHANGE null_rest_of_line
;
ssh_login_attempts_null
:
   LOGIN_ATTEMPTS null_rest_of_line
;
ssh_mgmt_auth_null
:
   MGMT_AUTH null_rest_of_line
;
ssh_stricthostkeycheck_null
:
   STRICTHOSTKEYCHECK null_rest_of_line
;
ssh_version_null
:
   VERSION null_rest_of_line
;

ssh_server
:
   SERVER
   (
      (
         IPV4 ACCESS_LIST acl = access_list_name
      )
      |
      (
         IPV6 ACCESS_LIST acl6 = access_list_name
      )
      | LOGGING
      |
      (
         SESSION_LIMIT limit = uint_legacy
      )
      | V2
      |
      (
         VRF vrf = vrf_name
      )
   )* NEWLINE
;

ssh_timeout
:
   TIMEOUT uint_legacy NEWLINE
;

// old top-level rules (from hybrid cisco parser)
stanza
:
   community_set_stanza
   | ip_prefix_list_stanza
   | ipv6_prefix_list_stanza
   | no_aaa_group_server_stanza
   | no_ip_prefix_list_stanza
   | prefix_set_stanza
   | route_policy_stanza
   | router_bgp_stanza
   | router_isis_stanza
   | rsvp_stanza
   | s_aaa
   | s_banner_ios
   | s_bfd
   | s_call_home
   | s_class_map
   | s_control_plane
   | s_crypto
   | s_dhcp
   | s_domain
   | s_ethernet_services
   | s_event
   | s_extcommunity_set
   | s_hostname
   | s_interface
   | s_ipsla
   | s_key
   | s_l2tp_class
   | s_l2vpn
   | s_line
   | s_logging
   | s_lpts
   | s_no_bfd
   | s_ntp
   | s_object_group
   | s_policy_map
   | s_radius_server
   | s_router_eigrp
   | s_router_ospf
   | s_router_ospfv3
   | s_router_rip
   | s_router_static
   | s_service
   | s_service_policy_global
   | s_snmp_server
   | s_spanning_tree
   | s_ssh
   | s_statistics
   | s_tacacs
   | s_tacacs_server
   | s_template
   | s_track
   | s_username
   | s_username_attributes
   | s_vrf
   | srlg_stanza
;

statistics_null
:
   NO?
   (
      EXTENDED_COUNTERS
      | TM_VOQ_COLLECTION
   ) null_rest_of_line
;

t_group_null
:
   GROUP null_rest_of_line
;
t_host_null
:
   HOST null_rest_of_line
;

t_server
:
   SERVER hostname = variable_hostname NEWLINE
   (
      t_server_address
      | t_key
      | t_server_null
   )*
;

t_server_address
:
   ADDRESS
   (
      (
         IPV4 IP_ADDRESS
      )
      |
      (
         IPV6 IPV6_ADDRESS
      )
   ) NEWLINE
;

t_server_null
:
   NO?
   (
      SINGLE_CONNECTION
      | TIMEOUT
   ) null_rest_of_line
;

t_key
:
   KEY uint_legacy? variable_permissive NEWLINE
;

t_source_interface
:
   SOURCE_INTERFACE iname = interface_name
   (
      VRF name = vrf_name
   )? NEWLINE
;

template_null
:
  NO?
  (
    ACCESS_SESSION
    | AUTHENTICATION
    | DOT1X
    | MAB
    | RADIUS_SERVER
  ) null_rest_of_line
;

track_block
:
  NEWLINE track_block_null*
;

track_block_null
:
  TYPE null_rest_of_line track_block_type_null*
;

track_block_type_null
:
  OBJECT null_rest_of_line
;

track_interface
:
  INTERFACE interface_name LINE_PROTOCOL NEWLINE
;

track_list
:
  LIST null_rest_of_line (
     track_list_delay_null
     | track_list_object_null
  )*
;

track_list_delay_null
:
   DELAY null_rest_of_line
;
track_list_object_null
:
   OBJECT null_rest_of_line
;

ts_common
:
   ts_deadtime_null
   | ts_directed_request_null
   | ts_key_null
   | ts_retransmit_null
   | ts_test_null
   | ts_timeout_null
;

ts_host
:
   HOST hostname =
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) null_rest_of_line t_key?
;

ts_deadtime_null
:
   DEADTIME null_rest_of_line
;
ts_directed_request_null
:
   DIRECTED_REQUEST null_rest_of_line
;
ts_key_null
:
   KEY null_rest_of_line
;
ts_retransmit_null
:
   RETRANSMIT null_rest_of_line
;
ts_test_null
:
   TEST null_rest_of_line
;
ts_timeout_null
:
   TIMEOUT null_rest_of_line
;

u
:
   u_encrypted_password
   | u_nohangup
   | u_passphrase
   | u_password
   | u_privilege
   | u_role
;

u_encrypted_password
:
   ENCRYPTED_PASSWORD pass = variable_permissive
;

u_nohangup
:
   NOHANGUP
;

u_passphrase
:
   PASSPHRASE
   (
      GRACETIME gracetime = uint_legacy
      | LIFETIME lifetime = uint_legacy
      | WARNTIME warntime = uint_legacy
   )*
;

u_password
:
   (PASSWORD | SECRET) up_cisco_xr
   | NOPASSWORD
;

u_privilege
:
   PRIVILEGE privilege = variable
;

u_role
:
   (
      GROUP
      | ROLE
   ) role = variable
;

ua_group_lock_null
:
   GROUP_LOCK null_rest_of_line
;
ua_vpn_group_policy_null
:
   VPN_GROUP_POLICY null_rest_of_line
;

up_cisco_xr
:
   uint_legacy? up_cisco_xr_tail
;

up_cisco_xr_tail
:
   (pass = variable_secret)
   (
      ENCRYPTED
      | MSCHAP
      | NT_ENCRYPTED
      | PBKDF2
   )?
;