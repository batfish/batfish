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
CiscoXr_vrf;


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
  | s_cdp
  | s_cef
  | s_clock
  | s_configuration
  | s_end
  | s_flow
  | s_ipv4
  | s_ipv6
  | s_isolation
  | s_lldp
  | s_mpls
  | s_multicast_routing
  | s_null
  | s_no
  | s_nsr
  | s_rd_set
  | s_router
  | s_sampler_map
  | s_taskgroup
  | s_tcp
  | s_telnet
  | s_tftp
  | s_usergroup
;

s_cdp
:
  CDP
  (
    NEWLINE
    | cdp_null
  )
;

cdp_null
:
  (
    ADVERTISE
    | HOLDTIME
    | LOG
    | TIMER
  ) null_rest_of_line
;

s_cef: CEF cef_null;

cef_null
:
  (
    ADJACENCY
    | LOAD_BALANCING
    | PBTS
    | PLATFORM
  ) null_rest_of_line
;

s_end: END NEWLINE;

s_clock: CLOCK TIMEZONE null_rest_of_line;

s_configuration: CONFIGURATION configuration_null;

configuration_null
:
  (
    COMMIT
    | DISPLAY
    | MODE
  ) null_rest_of_line
;

s_ipv4
:
  IPV4
  (
    ipv4_access_list
    | ipv4_conflict_policy
    | ipv4_null
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

ipv4_null
:
  (
    NETMASK_FORMAT
    | VIRTUAL
  ) null_rest_of_line
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

s_null
:
  (
    BUILDING_CONFIGURATION
    | CONFDCONFIG
    | FPD
    | FRI
    | MON
    | SAT
    | SUN
    | THU
    | TUE
    | VTY_POOL
    | WED
  ) null_rest_of_line
;

s_router
:
  ROUTER
  (
    router_igmp
    | router_mld
    | router_msdp
    | router_pim
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

s_tcp: TCP tcp_null;

tcp_null:
  (
    ACCEPT_RATE
    | DIRECTORY
    | MSS
    | NUM_THREAD
    | PATH_MTU_DISCOVERY
    | RECEIVE_QUEUE
    | SELECTIVE_ACK
    | SYNWAIT_TIME
    | THROTTLE
    | TIMESTAMP
    | WINDOW_SIZE
  ) null_rest_of_line
;

s_telnet: TELNET telnet_null;

telnet_null
:
  (
    IPV4
    | IPV6
    | VRF
  ) null_rest_of_line
;

s_usergroup: USERGROUP name = usergroup_name NEWLINE usergroup_inner*;

usergroup_inner: usergroup_null;

usergroup_null
:
  (
    DESCRIPTION
    | INHERIT
    | TASKGROUP
  ) null_rest_of_line
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
   BRIDGE GROUP name = variable NEWLINE
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
   BRIDGE_DOMAIN name = variable NEWLINE
   (
      lbgbd_mac
      | lbgbd_null
      | lbgbd_vfi
   )*
;

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
      INTERFACE
      | MTU
      | NEIGHBOR
      | ROUTED
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

s_router_vrrp
:
   NO? ROUTER VRRP NEWLINE
   (
      vrrp_interface
   )*
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
      spanning_tree_mst
      | spanning_tree_portfast
      | spanning_tree_pseudo_information
      | spanning_tree_null
      | NEWLINE
   )
;

s_ssh
:
   SSH
   (
      ssh_client
      | ssh_null
      | ssh_server
      | ssh_timeout
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
      t_null
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
      ua_null
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

spanning_tree_null
:
   (
      BACKBONEFAST
      | BPDUFILTER
      | BRIDGE
      | COST
      | DISPUTE
      | ETHERCHANNEL
      | EXTEND
      | FCOE
      | GUARD
      | LOGGING
      | LOOPGUARD
      | MODE
      | OPTIMIZE
      | PATHCOST
      | PORT
      | UPLINKFAST
      | VLAN
   ) null_rest_of_line
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

ssh_null
:
   (
      IP_ADDRESS
      | KEY
      | KEY_EXCHANGE
      | LOGIN_ATTEMPTS
      | MGMT_AUTH
      | STRICTHOSTKEYCHECK
      | VERSION
   ) null_rest_of_line
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
   | router_hsrp_stanza
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
   | s_router_vrrp
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

t_null
:
   (
      GROUP
      | HOST
   ) null_rest_of_line
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
  LIST null_rest_of_line track_list_null*
;

track_list_null
:
  (
    DELAY
    | OBJECT
  ) null_rest_of_line
;

ts_common
:
   ts_null
;

ts_host
:
   HOST hostname =
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) null_rest_of_line t_key?
;

ts_null
:
   (
      DEADTIME
      | DIRECTED_REQUEST
      | KEY
      | RETRANSMIT
      | TEST
      | TIMEOUT
   ) null_rest_of_line
;

vi_address_family
:
   NO? ADDRESS_FAMILY IPV4 NEWLINE
   (
      viaf_vrrp
   )*
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

ua_null
:
   (
      GROUP_LOCK
      | VPN_GROUP_POLICY
   ) null_rest_of_line
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

viaf_vrrp
:
   NO? VRRP groupnum = uint_legacy NEWLINE
   (
      viafv_address
      | viafv_null
      | viafv_preempt
      | viafv_priority
   )*
;

viafv_address
:
   ADDRESS address = IP_ADDRESS NEWLINE
;

viafv_null
:
   NO?
   (
      TIMERS
      | TRACK
   ) null_rest_of_line
;

viafv_preempt
:
   PREEMPT
   (
      DELAY delay = uint_legacy
   ) NEWLINE
;

viafv_priority
:
   PRIORITY priority = uint_legacy NEWLINE
;

vrrp_interface
:
   NO? INTERFACE iface = interface_name NEWLINE
   (
      vi_address_family
   )* NEWLINE?
;
