parser grammar CiscoXrParser;

import
CiscoXr_common,
CiscoXr_aaa,
CiscoXr_acl,
CiscoXr_bgp,
CiscoXr_community_set,
CiscoXr_crypto,
CiscoXr_callhome,
CiscoXr_eigrp,
CiscoXr_extcommunity_set,
CiscoXr_hsrp,
CiscoXr_ignored,
CiscoXr_interface,
CiscoXr_isis,
CiscoXr_line,
CiscoXr_logging,
CiscoXr_mpls,
CiscoXr_ntp,
CiscoXr_ospf,
CiscoXr_pim,
CiscoXr_qos,
CiscoXr_rip,
CiscoXr_rpl,
CiscoXr_snmp,
CiscoXr_static;


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
  s_ipv4
  | s_ipv6
  | s_no
;

s_ipv4
:
  IPV4 ipv4_access_list
;

s_ipv6
:
  IPV6 ipv6_access_list
;

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

////////////////////////////////////////////////////////////////////////////////////

address_aiimgp_stanza
:
   ADDRESS null_rest_of_line
;

address_family_multicast_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   ) NEWLINE address_family_multicast_tail
;

address_family_multicast_tail
:
   (
      (
         MULTIPATH NEWLINE
      )
      |
      (
         INTERFACE ALL ENABLE NEWLINE
      )
      | null_af_multicast_tail
      | interface_multicast_stanza
      | ip_pim_tail
   )*
;

aiimgp_stanza
:
   address_aiimgp_stanza
;

allow_iimgp_stanza
:
   ALLOW null_rest_of_line aiimgp_stanza*
;

bfd_null
:
   NO?
   (
      TRAP
   ) null_rest_of_line
;

cp_ip_access_group
:
   (
      IP
      | IPV6
   ) ACCESS_GROUP name = variable
   (
      VRF vrf = variable
   )?
   (
      IN
      | OUT
   ) NEWLINE
;

cp_ip_flow
:
   IP FLOW MONITOR name = variable
   (
      INPUT
      | OUTPUT
   ) NEWLINE
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

flow_null
:
   NO?
   (
      CACHE
      | COLLECT
      | DESCRIPTION
      | DESTINATION
      | EXPORT_PROTOCOL
      | EXPORTER
      | MATCH
      | OPTION
      | RECORD
      | SOURCE
      | STATISTICS
      | TRANSPORT
   ) null_rest_of_line
;

flow_version
:
   NO? VERSION null_rest_of_line
   (
      flowv_null
   )*
;

flowv_null
:
   NO?
   (
      OPTIONS
      | TEMPLATE
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

interface_multicast_stanza
:
   INTERFACE interface_name NEWLINE interface_multicast_tail*
;

interface_multicast_tail
:
   (
      BOUNDARY
      | BSR_BORDER
      | DISABLE
      | DR_PRIORITY
      | ENABLE
      | ROUTER
   ) null_rest_of_line
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

mp_null
:
   NO?
   (
      CONNECT_SOURCE
      | DESCRIPTION
      | MESH_GROUP
      | REMOTE_AS
      | SHUTDOWN
   ) null_rest_of_line
;

multicast_routing_stanza
:
   MULTICAST_ROUTING NEWLINE
   (
      address_family_multicast_stanza
   )*
;

no_aaa_group_server_stanza
:
   NO AAA GROUP SERVER null_rest_of_line
;

null_af_multicast_tail
:
   NSF NEWLINE
;

vrfd_af_null
:
   NO?
   (
      MAXIMUM
   ) null_rest_of_line
;

null_imgp_stanza
:
   NO?
   (
      VRF
   ) null_rest_of_line
;

peer_sa_filter
:
   SA_FILTER
   (
      IN
      | OUT
   )
   (
      LIST
      | RP_LIST
   ) name = variable NEWLINE
;

peer_stanza
:
   PEER IP_ADDRESS NEWLINE
   (
      mp_null
      | peer_sa_filter
   )*
;

rmc_null
:
   NO?
   (
      MAXIMUM
   ) null_rest_of_line
;

router_multicast_stanza
:
   IPV6? ROUTER
   (
      IGMP
      | MLD
      | MSDP
      | PIM
   ) NEWLINE router_multicast_tail
;

router_multicast_tail
:
   (
      address_family_multicast_stanza
      |
      (
         INTERFACE ALL null_rest_of_line
      )
      | interface_multicast_stanza
      | null_inner
      | peer_stanza
      | rmc_null
   )*
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

s_bfd
:
   BFD null_rest_of_line
   (
      bfd_null
   )*
;

s_control_plane
:
   CONTROL_PLANE
   (
      SLOT DEC
   )? NEWLINE s_control_plane_tail*
;

s_control_plane_tail
:
   cp_ip_access_group
   | cp_ip_flow
   | cp_management_plane
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
      VRF vrf = variable
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

s_flow
:
   FLOW
   (
      EXPORTER
      | EXPORTER_MAP
      | HARDWARE
      | MONITOR
      | MONITOR_MAP
      | PLATFORM
      | RECORD
   ) null_rest_of_line
   (
      flow_null
      | flow_version
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
      ssh_access_group
      | ssh_client
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


// a way to define a VRF on IOS
s_vrf_definition
:
   // DEFINITION is for IOS
   VRF DEFINITION? name = variable NEWLINE
   (
      vrfd_address_family
      | vrfd_description
      | vrfd_rd
      | vrfd_null
   )*
   (
      EXIT_VRF NEWLINE
   )?
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
   DEC null_rest_of_line
;

srlg_interface_stanza
:
   INTERFACE null_rest_of_line srlg_interface_numeric_stanza*
;

srlg_stanza
:
   SRLG NEWLINE srlg_interface_stanza*
;

ssh_access_group
:
   ACCESS_GROUP IPV6? name = variable NEWLINE
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
         IPV4 ACCESS_LIST acl = variable
      )
      |
      (
         IPV6 ACCESS_LIST acl6 = variable
      )
      | LOGGING
      |
      (
         SESSION_LIMIT limit = DEC
      )
      | V2
      |
      (
         VRF vrf = variable
      )
   )* NEWLINE
;

ssh_timeout
:
   TIMEOUT DEC NEWLINE
;

// old top-level rules (from hybrid cisco parser)
stanza
:
   as_path_set_stanza
   | community_set_stanza
   | ip_prefix_list_stanza
   | ipv6_prefix_list_stanza
   | multicast_routing_stanza
   | no_aaa_group_server_stanza
   | no_ip_prefix_list_stanza
   | prefix_set_stanza
   | route_policy_stanza
   | router_bgp_stanza
   | router_hsrp_stanza
   | router_isis_stanza
   | router_multicast_stanza
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
   | s_flow
   | s_hostname
   | s_interface
   | s_ipv6_router_ospf
   | s_ipsla
   | s_key
   | s_l2tp_class
   | s_l2vpn
   | s_line
   | s_logging
   | s_lpts
   | s_mpls_label_range
   | s_mpls_ldp
   | s_mpls_traffic_eng
   | s_no_bfd
   | s_ntp
   | s_null
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
   | s_vrf_definition
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
   KEY DEC? variable_permissive NEWLINE
;

t_source_interface
:
   SOURCE_INTERFACE iname = interface_name
   (
      VRF name = variable
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
      GRACETIME gracetime = DEC
      | LIFETIME lifetime = DEC
      | WARNTIME warntime = DEC
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
   DEC? up_cisco_xr_tail
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
   NO? VRRP groupnum = DEC NEWLINE
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
      DELAY delay = DEC
   ) NEWLINE
;

viafv_priority
:
   PRIORITY priority = DEC NEWLINE
;

vrfd_address_family
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      MULTICAST
      | UNICAST
   )?
   (
      MAX_ROUTE DEC
   )? NEWLINE
   (
      vrfd_af_null
   )*
   (
      EXIT_ADDRESS_FAMILY NEWLINE
   )?
;

vrfd_description
:
   description_line
;

vrfd_rd
:
   RD (AUTO | rd = route_distinguisher) NEWLINE
;

vrfd_null
:
   NO?
   (
      AUTO_IMPORT
      | ROUTE_TARGET
   ) null_rest_of_line
;

vrrp_interface
:
   NO? INTERFACE iface = interface_name NEWLINE
   (
      vi_address_family
   )* NEWLINE?
;
