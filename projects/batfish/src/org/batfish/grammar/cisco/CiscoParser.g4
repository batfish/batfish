parser grammar CiscoParser;

import
Cisco_common, Cisco_aaa, Cisco_acl, Cisco_bgp, Cisco_crypto, Cisco_callhome, Cisco_eigrp, Cisco_hsrp, Cisco_ignored, Cisco_interface, Cisco_isis, Cisco_line, Cisco_logging, Cisco_mpls, Cisco_ntp, Cisco_ospf, Cisco_pim, Cisco_qos, Cisco_rip, Cisco_routemap, Cisco_snmp, Cisco_static;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = CiscoLexer;
}

@header {
package org.batfish.grammar.cisco;
}

@members {
   private boolean _multilineBgpNeighbors;
   
   public void setMultilineBgpNeighbors(boolean multilineBgpNeighbors) {
      _multilineBgpNeighbors = multilineBgpNeighbors;
   }
   
   @Override
   public String getStateInfo() {
      return "_multilineBgpNeighbors: " + _multilineBgpNeighbors + "\n";
   }
   
}

address_aiimgp_stanza
:
   ADDRESS ~NEWLINE* NEWLINE
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
   ALLOW ~NEWLINE* NEWLINE aiimgp_stanza*
;

asa_comment_stanza
:
   COLON ~NEWLINE* NEWLINE
;

banner_stanza
:
   BANNER banner_type banner
;

cisco_configuration
:
   NEWLINE?
   (
      sl += stanza
   )+ COLON? NEWLINE? EOF
;

controller_null
:
   NO?
   (
      ADMIN_STATE
      | AIS_SHUT
      | ALARM_REPORT
      | CABLELENGTH
      | CHANNEL_GROUP
      | CLOCK
      | DESCRIPTION
      | FRAMING
      | G709
      | LINECODE
      | PM
      | PRI_GROUP
      | PROACTIVE
      | SHUTDOWN
      | STS_1
      | WAVELENGTH
   ) ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

cp_service_policy
:
   SERVICE_POLICY
   (
      INPUT
      | OUTPUT
   ) name = variable NEWLINE
;

color_setter
:
   SET_COLOR
   (
      RED
      | YELLOW
      | GREEN
   )
;

cqg_null
:
   NO?
   (
      PRECEDENCE
      | QUEUE
      | RANDOM_DETECT_LABEL
   ) ~NEWLINE* NEWLINE
;

ctlf_null
:
   NO?
   (
      RECORD_ENTRY
      | SHUTDOWN
   ) ~NEWLINE* NEWLINE
;

d11_null
:
   NO?
   (
      ACCOUNTING
      | AUTHENTICATION
      | GUEST_MODE
      | MAX_ASSOCIATIONS
      | MBSSID
      | VLAN
   ) ~NEWLINE* NEWLINE
;

dapr_null
:
   NO?
   (
      ACTION
      | USER_MESSAGE
   ) ~NEWLINE* NEWLINE
;

dapr_webvpn
:
   WEBVPN NEWLINE
   (
      daprw_null
   )*
;

daprw_null
:
   NO?
   (
      ALWAYS_ON_VPN
      | SVC
      | URL_LIST
   ) ~NEWLINE* NEWLINE
;

del_stanza
:
   DEL ~NEWLINE* NEWLINE
;

dhcp_null
:
   NO?
   (
      INTERFACE
   ) ~NEWLINE* NEWLINE
;

dhcp_profile
:
   NO? PROFILE ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

domain_name
:
   NAME hostname = variable_hostname NEWLINE
;

domain_name_server
:
   NAME_SERVER hostname = variable_hostname NEWLINE
;

dspf_null
:
   NO?
   (
      ASSOCIATE
      | DESCRIPTION
      | CODEC
      | MAXIMUM
   ) ~NEWLINE* NEWLINE
;

eh_null
:
   NO?
   (
      ACTION
      | ASYNCHRONOUS
      | DELAY
      | TRIGGER
   ) ~NEWLINE* NEWLINE
;

event_null
:
   NO?
   (
      ACTION
      | EVENT
      | SET
   ) ~NEWLINE* NEWLINE
;

failover_lan
:
   LAN failover_lan_tail
;

failover_lan_tail
:
   flan_interface
   | flan_unit
;

failover_link
:
   LINK name = variable iface = interface_name NEWLINE
;

failover_interface
:
   INTERFACE IP name = variable pip = IP_ADDRESS pmask = IP_ADDRESS STANDBY sip
   = IP_ADDRESS NEWLINE
;

flan_interface
:
   INTERFACE name = variable iface = interface_name NEWLINE
;

flan_unit
:
   UNIT
   (
      PRIMARY
      | SECONDARY
   ) NEWLINE
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
      | RECORD
      | SOURCE
      | STATISTICS
      | TRANSPORT
   ) ~NEWLINE* NEWLINE
;

flow_version
:
   NO? VERSION ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

gk_null
:
   NO?
   (
      GW_TYPE_PREFIX
      | LRQ
      | SHUTDOWN
      | ZONE
   ) ~NEWLINE* NEWLINE
;

gpsec_null
:
   NO?
   (
      AGE
      | DELETE_DYNAMIC_LEARN
   ) ~NEWLINE* NEWLINE
;

hardware_null
:
   NO?
   (
      IFACL
      | QOS
      | RBACL
      | SPAN
      | VACL
   ) ~NEWLINE* NEWLINE
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
   INTERFACE ~NEWLINE* NEWLINE iimgp_stanza*
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
   ) ~NEWLINE* NEWLINE
;

ip_as_path_regex_mode_stanza
:
   IP AS_PATH REGEX_MODE
   (
      ASN
      | STRING
   ) NEWLINE
;

ip_default_gateway_stanza
:
   IP DEFAULT_GATEWAY gateway = IP_ADDRESS NEWLINE
;

ip_dhcp_null
:
   NO?
   (
      BOOTFILE
      | CLIENT_IDENTIFIER
      | CLIENT_NAME
      | DEFAULT_ROUTER
      | DNS_SERVER
      | DOMAIN_NAME
      | HARDWARE_ADDRESS
      | HOST
      | LEASE
      | NETWORK
      | NEXT_SERVER
      | OPTION
   ) ~NEWLINE* NEWLINE
;

ip_domain_name
:
   NAME
   (
      VRF vrf = variable
   )? hostname = variable_hostname NEWLINE
;

ip_domain_null
:
   (
      LIST
      | LOOKUP
   ) ~NEWLINE* NEWLINE
;

ip_nat_null
:
   NO?
   (
      RANGE
   ) ~NEWLINE* NEWLINE
;

ip_route_stanza
:
   (
      IP
      | MANAGEMENT
   ) ROUTE
   (
      VRF vrf = ~NEWLINE
   )? ip_route_tail
;

ip_route_tail
:
   (
      (
         address = IP_ADDRESS mask = IP_ADDRESS
      )
      | prefix = IP_PREFIX
   )
   (
      nexthopip = IP_ADDRESS
      | nexthopprefix = IP_PREFIX
      | distance = DEC
      |
      (
         TAG tag = DEC
      )
      | perm = PERMANENT
      |
      (
         TRACK track = DEC
      )
      |
      (
         NAME variable
      )
      // do not move interface_name up

      | nexthopint = interface_name
   )* NEWLINE
;

ip_sla_null
:
   NO?
   (
      FREQUENCY
      | HISTORY
      | HOPS_OF_STATISTICS_KEPT
      | ICMP_ECHO
      | PATH_ECHO
      | PATHS_OF_STATISTICS_KEPT
      | REQUEST_DATA_SIZE
      | SAMPLES_OF_HISTORY_KEPT
      | TAG
      | TOS
      | UDP_JITTER
   ) ~NEWLINE* NEWLINE
;

ip_ssh_null
:
   (
      AUTHENTICATION_RETRIES
      | CLIENT
      | PORT
      | SOURCE_INTERFACE
      | TIME_OUT
   ) ~NEWLINE* NEWLINE
;

ip_ssh_pubkey_chain
:
   PUBKEY_CHAIN NEWLINE
   (
      (
         KEY_HASH
         | QUIT
         | USERNAME
      ) ~NEWLINE* NEWLINE
   )+
;

ip_ssh_version
:
   VERSION version = DEC NEWLINE
;

ipc_association
:
   ASSOCIATION ~NEWLINE* NEWLINE
   (
      ipca_null
   )*
;

ipca_null
:
   NO?
   (
      ASSOC_RETRANSMIT
      | LOCAL_IP
      | LOCAL_PORT
      | PATH_RETRANSMIT
      | PROTOCOL
      | REMOTE_IP
      | REMOTE_PORT
      | RETRANSMIT_TIMEOUT
      | SHUTDOWN
   ) ~NEWLINE* NEWLINE
;

ispla_operation
:
   NO? OPERATION ~NEWLINE* NEWLINE
   (
      ipslao_type
   )*
;

ipsla_reaction
:
   NO? REACTION ~NEWLINE* NEWLINE
   (
      ipslar_react
   )*
;

ipsla_responder
:
   NO? RESPONDER ~NEWLINE* NEWLINE
   (
      ipslarp_null
   )*
;

ipsla_schedule
:
   NO? SCHEDULE ~NEWLINE* NEWLINE
   (
      ipslas_null
   )*
;

ipslao_type
:
   NO? TYPE ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

ipslaot_statistics
:
   NO? STATISTICS ~NEWLINE* NEWLINE
   (
      ipslaots_null
   )*
;

ipslaots_null
:
   NO?
   (
      BUCKETS
   ) ~NEWLINE* NEWLINE
;

ipslar_react
:
   NO? REACT ~NEWLINE* NEWLINE
   (
      ispalrr_null
   )*
;

ipslarp_null
:
   NO?
   (
      TYPE
   ) ~NEWLINE* NEWLINE
;

ispalrr_null
:
   NO?
   (
      ACTION
      | THRESHOLD
   ) ~NEWLINE* NEWLINE
;

ipslas_null
:
   NO?
   (
      LIFE
      | START_TIME
   ) ~NEWLINE* NEWLINE
;

l2_null
:
   NO?
   (
      BRIDGE_DOMAIN
      | MTU
      | NEIGHBOR
      | VPN
   ) ~NEWLINE* NEWLINE
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
   NO? P2P ~NEWLINE* NEWLINE
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
   NO? MAC ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

lbgbd_vfi
:
   NO? VFI ~NEWLINE* NEWLINE
   (
      lbgbdv_null
   )*
;

lbgbdm_limit
:
   NO? LIMIT ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

lbgbdv_null
:
   NO?
   (
      NEIGHBOR
   ) ~NEWLINE* NEWLINE
;

lpts_null
:
   NO?
   (
      FLOW
   ) ~NEWLINE* NEWLINE
;

lxp_neighbor
:
   NO? NEIGHBOR ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

lxpn_null
:
   NO?
   (
      SOURCE
   ) ~NEWLINE* NEWLINE
;

lxpn_l2tp
:
   NO? L2TP ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

map_class_null
:
   NO?
   (
      DIALER
   ) ~NEWLINE* NEWLINE
;

mgmt_api_stanza
:
   MANAGEMENT API HTTP_COMMANDS NEWLINE
   (
      (
         (
            PROTOCOL HTTPS NEWLINE
         )
         | mgmt_null
      )+
   )
;

mgmt_egress_iface_stanza
:
   MANAGEMENT EGRESS_INTERFACE_SELECTION NEWLINE
   (
      (
         APPLICATION HTTP
         | APPLICATION SNMP
         | APPLICATION RADIUS
         | APPLICATION TACACS
         | APPLICATION SYSLOG
         | APPLICATION SSH
         | APPLICATION
      ) NEWLINE
   )+
   (
      EXIT NEWLINE
   )?
;

mgmt_ip_access_group
:
   IP ACCESS_GROUP name = variable
   (
      IN
      | OUT
   ) NEWLINE
;

mgmt_null
:
   NO?
   (
      AUTHENTICATION
      | EXIT
      | IDLE_TIMEOUT
      | SHUTDOWN
   ) ~NEWLINE* NEWLINE
;

mgp_stanza
:
   inband_mgp_stanza
;

monitor_destination
:
   NO? DESTINATION ~NEWLINE* NEWLINE
   (
      monitor_destination_null
   )*
;

monitor_destination_null
:
   NO?
   (
      ERSPAN_ID
      | IP
      | MTU
      | ORIGIN
   ) ~NEWLINE* NEWLINE
;

monitor_null
:
   NO?
   (
      DESCRIPTION
      | DESTINATION
      | SHUTDOWN
      | SOURCE
   ) ~NEWLINE* NEWLINE
;

monitor_session_null
:
   NO?
   (
      DESTINATION
   ) ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

mt_null
:
   NO?
   (
      ADDRESS
   ) ~NEWLINE* NEWLINE
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
   NO AAA GROUP SERVER ~NEWLINE* NEWLINE
;

no_failover
:
   NO FAILOVER NEWLINE
;

no_ip_access_list_stanza
:
   NO IP ACCESS_LIST ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

null_imgp_stanza
:
   NO?
   (
      VRF
   ) ~NEWLINE* NEWLINE
;

nv_satellite
:
   NO?
   (
      SATELLITE
   ) ~NEWLINE* NEWLINE
   (
      nvs_null
   )*
;

nvs_null
:
   NO?
   (
      DESCRIPTION
      | IP
      | SERIAL_NUMBER
      | TYPE
   ) ~NEWLINE* NEWLINE
;

of_null
:
   NO?
   (
      BIND
      | CONTROLLER
      | DEFAULT_ACTION
      | DESCRIPTION
      | ENABLE
   ) ~NEWLINE* NEWLINE
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

phone_proxy_null
:
   NO?
   (
      CIPC
      | CTL_FILE
      | DISABLE
      | MEDIA_TERMINATION
      | PROXY_SERVER
      | TFTP_SERVER
      | TLS_PROXY
   ) ~NEWLINE* NEWLINE
;

redundancy_main_cpu
:
   MAIN_CPU ~NEWLINE* NEWLINE
   (
      redundancy_main_cpu_null
   )*
;

redundancy_main_cpu_null
:
   NO?
   (
      AUTO_SYNC
   ) ~NEWLINE* NEWLINE
;

redundancy_null
:
   NO?
   (
      KEEPALIVE_ENABLE
      | MODE
      | NOTIFICATION_TIMER
      | PROTOCOL
      | SCHEME
   ) ~NEWLINE* NEWLINE
;

rmc_null
:
   NO?
   (
      MAXIMUM
   ) ~NEWLINE* NEWLINE
;

role_null
:
   NO?
   (
      DESCRIPTION
      | RULE
   ) ~NEWLINE* NEWLINE
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
         INTERFACE ALL ~NEWLINE* NEWLINE
      )
      | interface_multicast_stanza
      | null_inner
      | peer_stanza
      | rmc_null
   )*
;

s_archive
:
   ARCHIVE ~NEWLINE* NEWLINE
   (
      (
         HIDEKEYS
         | LOG
         | LOGGING
         | NOTIFY
      ) ~NEWLINE* NEWLINE
   )*
;

s_authentication
:
   AUTHENTICATION
   (
      COMMAND
      | MAC_MOVE
   ) ~NEWLINE* NEWLINE
;

s_cluster
:
   NO? CLUSTER
   (
      ENABLE
      | RUN
   ) ~NEWLINE* NEWLINE
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

s_controller
:
   NO? CONTROLLER ~NEWLINE* NEWLINE
   (
      controller_null
   )*
;

s_cos_queue_group
:
   COS_QUEUE_GROUP ~NEWLINE* NEWLINE
   (
      cqg_null
   )*
;

s_ctl_file
:
   NO? CTL_FILE ~NEWLINE* NEWLINE
   (
      ctlf_null
   )*
;

s_dhcp
:
   NO? DHCP ~NEWLINE* NEWLINE
   (
      dhcp_null
      | dhcp_profile
   )*
;

s_dial_peer
:
   DIAL_PEER ~NEWLINE* NEWLINE
   (
      NO?
      (
         CODEC
         | DESCRIPTION
         | DESTINATION_PATTERN
         | DTMF_RELAY
         | FORWARD_DIGITS
         | INCOMING
         | MEDIA
         | PORT
         | SERVICE
         | SESSION
         | TRANSLATION_PROFILE
         | VAD
      ) ~NEWLINE* NEWLINE
   )*
;

s_domain
:
   DOMAIN
   (
      VRF vrf = variable
   )?
   (
      domain_name
      | domain_name_server
   )
;

s_domain_name
:
   DOMAIN_NAME hostname = variable_hostname NEWLINE
;

s_dot11
:
   DOT11 ~NEWLINE* NEWLINE
   (
      d11_null
   )*
;

s_dspfarm
:
   NO? DSPFARM ~NEWLINE* NEWLINE
   (
      dspf_null
   )*
;

s_dynamic_access_policy_record
:
   NO? DYNAMIC_ACCESS_POLICY_RECORD ~NEWLINE* NEWLINE
   (
      dapr_null
      | dapr_webvpn
   )*
;

s_event
:
   NO? EVENT ~NEWLINE* NEWLINE
   (
      event_null
   )*
;

s_event_handler
:
   NO? EVENT_HANDLER ~NEWLINE* NEWLINE
   (
      eh_null
   )*
;

s_failover
:
   FAILOVER s_failover_tail
;

s_failover_tail
:
   NEWLINE
   | failover_lan
   | failover_link
   | failover_interface
;

s_feature
:
   NO? FEATURE
   (
      words += variable
   )+ NEWLINE
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
   ) ~NEWLINE* NEWLINE
   (
      flow_null
      | flow_version
   )*
;

s_gatekeeper
:
   GATEKEEPER NEWLINE
   (
      gk_null
   )*
;

s_global_port_security
:
   GLOBAL_PORT_SECURITY NEWLINE
   (
      gpsec_null
   )*
;

s_hardware
:
   NO? HARDWARE ~NEWLINE* NEWLINE
   (
      hardware_null
   )*
;

s_hostname
:
   (
      HOSTNAME
      | SWITCHNAME
   )
   (
      name_parts += ~NEWLINE
   )+ NEWLINE
;

s_ip_dhcp
:
   NO?
   (
      IP
      | IPV6
   ) DHCP ~NEWLINE* NEWLINE
   (
      ip_dhcp_null
   )*
;

s_ip_domain
:
   NO? IP DOMAIN
   (
      ip_domain_name
      | ip_domain_null
   )
;

s_ip_domain_name
:
   IP DOMAIN_NAME hostname = variable_hostname
   (
      USE_VRF variable
   )? NEWLINE
;

s_ip_name_server
:
   IP NAME_SERVER
   (
      VRF vrf = variable
   )?
   (
      hostnames += ip_hostname
   )+
   (
      USE_VRF vrf = variable
   )? NEWLINE
;

s_ip_nat
:
   NO? IP NAT
   (
      INSIDE
      | LOG
      | OUTSIDE
      | POOL
      | TRANSLATION
   ) ~NEWLINE* NEWLINE
   (
      ip_nat_null
   )*
;

s_ip_sla
:
   NO? IP SLA ~NEWLINE* NEWLINE
   (
      ip_sla_null
   )*
;

s_ip_source_route
:
   NO? IP SOURCE_ROUTE NEWLINE
;

s_ip_ssh
:
   IP SSH
   (
      ip_ssh_pubkey_chain
      | ip_ssh_version
      | ip_ssh_null
   )
;

s_ip_wccp
:
   NO? IP WCCP
   (
      VRF vrf = variable
   )?
   (
      wccp_id
      | wccp_null
   )
;

s_ipc
:
   IPC ~NEWLINE* NEWLINE
   (
      ipc_association
   )*
;

s_ipsla
:
   NO? IPSLA ~NEWLINE* NEWLINE
   (
      ispla_operation
      | ipsla_reaction
      | ipsla_responder
      | ipsla_schedule
   )*
;

s_l2
:
   NO? L2 ~NEWLINE* NEWLINE
   (
      l2_null
   )*
;

s_l2vpn
:
   NO? L2VPN ~NEWLINE* NEWLINE
   (
      l2vpn_bridge_group
      | l2vpn_logging
      | l2vpn_xconnect
   )*
;

s_lpts
:
   NO? LPTS ~NEWLINE* NEWLINE
   (
      lpts_null
   )*
;

s_management
:
   MANAGEMENT
   (
      CONSOLE
      | SSH
      | TELNET
   ) NEWLINE s_management_tail*
;

s_management_tail
:
   mgmt_ip_access_group
   | mgmt_null
;

s_map_class
:
   NO? MAP_CLASS ~NEWLINE* NEWLINE
   (
      map_class_null
   )*
;

s_media_termination
:
   NO? MEDIA_TERMINATION ~NEWLINE* NEWLINE
   (
      mt_null
   )*
;

s_monitor
:
   NO? MONITOR ~NEWLINE* NEWLINE
   (
      monitor_destination
      | monitor_null
   )*
;

s_monitor_session
:
   NO? MONITOR_SESSION ~NEWLINE* NEWLINE
   (
      monitor_session_null
   )*
;

s_mtu
:
   MTU variable DEC NEWLINE
;

s_name
:
   NAME variable variable ~NEWLINE* NEWLINE
;

s_no_access_list_extended
:
   NO ACCESS_LIST ACL_NUM_EXTENDED NEWLINE
;

s_no_access_list_standard
:
   NO ACCESS_LIST ACL_NUM_STANDARD NEWLINE
;

s_nv
:
   NO? NV NEWLINE
   (
      nv_satellite
   )*
;

s_openflow
:
   NO? OPENFLOW ~NEWLINE* NEWLINE
   (
      of_null
   )*
;

s_phone_proxy
:
   NO? PHONE_PROXY ~NEWLINE* NEWLINE
   (
      phone_proxy_null
   )*
;

s_privilege
:
   NO? PRIVILEGE
   (
      CLEAR
      | CMD
      | CONFIGURE
      | EXEC
      | INTERFACE
      | IPENACL
      | ROUTER
      | SHOW
   ) ~NEWLINE* NEWLINE
;

s_redundancy
:
   NO? REDUNDANCY ~NEWLINE* NEWLINE
   (
      redundancy_main_cpu
      | redundancy_null
   )*
;

s_role
:
   NO? ROLE ~NEWLINE* NEWLINE
   (
      role_null
   )*
;

s_router_vrrp
:
   NO? ROUTER VRRP NEWLINE
   (
      vrrp_interface
   )*
;

s_sccp
:
   NO? SCCP ~NEWLINE* NEWLINE
   (
      sccp_null
   )*
;

s_service
:
   NO? SERVICE
   (
      words += variable
   )+ NEWLINE
;

s_sntp
:
   SNTP sntp_server
;

s_spanning_tree
:
   NO? SPANNING_TREE
   (
      spanning_tree_mst
      | spanning_tree_pseudo_information
      | spanning_tree_null
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
   NO? STATISTICS ~NEWLINE* NEWLINE
   (
      statistics_null
   )*
;

s_stcapp
:
   STCAPP ~NEWLINE* NEWLINE
   (
      (
         CALL
         | CPTONE
         | FALLBACK_DN
         | PICKUP
         | PORT
         | PREFIX
      ) ~NEWLINE* NEWLINE
   )*
;

s_switchport
:
   SWITCHPORT DEFAULT MODE
   (
      ACCESS
      | ROUTED
   ) NEWLINE
;

s_tacacs
:
   TACACS
   (
      t_null
      | t_server
   )
;

s_tacacs_server
:
   NO? TACACS_SERVER
   (
      ts_host
      | ts_null
   )
;

s_tap
:
   NO? TAP ~NEWLINE* NEWLINE
   (
      tap_null
   )*
;

s_track
:
   TRACK ~NEWLINE* NEWLINE
   (
      track_null
   )*
;

s_tunnel_group
:
   NO? TUNNEL_GROUP ~NEWLINE* NEWLINE
   (
      tg_null
   )*
;

s_vlan
:
   NO? VLAN
   (
      ACCESS_MAP
      | DEC
   ) ~NEWLINE* NEWLINE
   (
      vlan_null
   )*
;

s_voice
:
   NO? VOICE ~NEWLINE* NEWLINE
   (
      voice_null
   )*
;

s_voice_port
:
   NO? VOICE_PORT ~NEWLINE* NEWLINE
   (
      vp_null
   )*
;

s_vpc
:
   NO? VPC ~NEWLINE* NEWLINE
   (
      vpc_null
   )*
;

s_vpdn_group
:
   NO? VPDN_GROUP ~NEWLINE* NEWLINE
   (
      vpdng_accept_dialin
      | vpdng_null
   )*
;

s_vpn
:
   NO? VPN ~NEWLINE* NEWLINE
   (
      vpn_null
   )*
;

s_vrf_context
:
   VRF CONTEXT name = variable NEWLINE
   (
      vrfc_ip_route
   )*
;

s_vrf_definition
:
   VRF DEFINITION? name = variable NEWLINE
   (
      vrfd_address_family
      | vrfd_null
   )*
   (
      EXIT_VRF NEWLINE
   )?
;

s_webvpn
:
   NO? WEBVPN ~NEWLINE* NEWLINE
   (
      webvpn_null
   )*
;

s_wsma
:
   WSMA ~NEWLINE* NEWLINE
   (
      wsma_null
   )*
;

s_xconnect_logging
:
   NO? XCONNECT LOGGING ~NEWLINE* NEWLINE
;

sccp_null
:
   NO?
   (
      ASSOCIATE
      | BIND
      | DESCRIPTION
      | SWITCHBACK
   ) ~NEWLINE* NEWLINE
;

sntp_server
:
   SERVER hostname = variable
   (
      VERSION version = DEC
   )? NEWLINE
;

spanning_tree_mst
:
   MST ~NEWLINE* NEWLINE spanning_tree_mst_null*
;

spanning_tree_mst_null
:
   NO?
   (
      INSTANCE
      | NAME
      | REVISION
   ) ~NEWLINE* NEWLINE
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
   // intentional blank

      | BACKBONEFAST
      | BRIDGE
      | DISPUTE
      | ETHERCHANNEL
      | EXTEND
      | FCOE
      | LOGGING
      | LOOPGUARD
      | MODE
      | OPTIMIZE
      | PATHCOST
      | PORT
      | PORTFAST
      | UPLINKFAST
      | VLAN
   ) ~NEWLINE* NEWLINE
;

spti_null
:
   NO?
   (
      MST
   ) ~NEWLINE* NEWLINE
;

srlg_interface_numeric_stanza
:
   DEC ~NEWLINE* NEWLINE
;

srlg_interface_stanza
:
   INTERFACE ~NEWLINE* NEWLINE srlg_interface_numeric_stanza*
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
   CLIENT ~NEWLINE* NEWLINE
;

ssh_null
:
   (
      IP_ADDRESS
      | KEY
      | KEY_EXCHANGE
      | LOGIN_ATTEMPTS
      | STRICTHOSTKEYCHECK
      | VERSION
   ) ~NEWLINE* NEWLINE
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

stanza
:
   appletalk_access_list_stanza
   | asa_comment_stanza
   | as_path_set_stanza
   | banner_stanza
   | community_set_stanza
   | del_stanza
   | extended_access_list_stanza
   | extended_ipv6_access_list_stanza
   | ip_as_path_access_list_stanza
   | ip_as_path_regex_mode_stanza
   | ip_community_list_expanded_stanza
   | ip_community_list_standard_stanza
   | ip_default_gateway_stanza
   | ip_prefix_list_stanza
   | ip_route_stanza
   | ipv6_prefix_list_stanza
   | ipx_sap_access_list_stanza
   | mgmt_api_stanza
   | mgmt_egress_iface_stanza
   | multicast_routing_stanza
   | no_aaa_group_server_stanza
   | no_failover
   | no_ip_access_list_stanza
   | no_ip_prefix_list_stanza
   | no_route_map_stanza
   | prefix_set_stanza
   | protocol_type_code_access_list_stanza
   | route_map_stanza
   | route_policy_stanza
   | router_bgp_stanza
   | router_hsrp_stanza
   | router_isis_stanza
   | router_multicast_stanza
   | router_rip_stanza
   | rsvp_stanza
   | s_aaa
   | s_archive
   | s_authentication
   | s_call_home
   | s_callhome
   | s_class_map
   | s_cluster
   | s_control_plane
   | s_controller
   | s_cos_queue_group
   | s_crypto
   | s_ctl_file
   | s_dhcp
   | s_dial_peer
   | s_domain
   | s_domain_name
   | s_dot11
   | s_dspfarm
   | s_dynamic_access_policy_record
   | s_ethernet_services
   | s_event
   | s_event_handler
   | s_failover
   | s_flow
   | s_foundry_mac_access_list
   | s_feature
   | s_gatekeeper
   | s_global_port_security
   | s_hardware
   | s_hostname
   | s_interface
   | s_ip_dhcp
   | s_ip_domain
   | s_ip_domain_name
   | s_ip_name_server
   | s_ip_nat
   | s_ip_pim
   | s_ip_sla
   | s_ip_source_route
   | s_ip_ssh
   | s_ip_wccp
   | s_ipc
   | s_ipv6_router_ospf
   | s_ipsla
   | s_key
   | s_l2
   | s_l2vpn
   | s_line
   | s_logging
   | s_lpts
   | s_management
   | s_mac_access_list
   | s_mac_access_list_extended
   | s_map_class
   | s_media_termination
   | s_monitor
   | s_monitor_session
   | s_mpls_label_range
   | s_mpls_ldp
   | s_mpls_traffic_eng
   | s_mtu
   | s_name
   | s_no_access_list_extended
   | s_no_access_list_standard
   | s_ntp
   | s_null
   | s_nv
   | s_object
   | s_object_group
   | s_openflow
   | s_phone_proxy
   | s_policy_map
   | s_privilege
   | s_qos_mapping
   | s_redundancy
   | s_role
   | s_router_eigrp
   | s_router_ospf
   | s_router_ospfv3
   | s_router_static
   | s_router_vrrp
   | s_sccp
   | s_service
   | s_snmp_server
   | s_sntp
   | s_spanning_tree
   | s_ssh
   | s_statistics
   | s_stcapp
   | s_switchport
   | s_table_map
   | s_tacacs
   | s_tacacs_server
   | s_tap
   | s_track
   | s_tunnel_group
   | s_vlan
   | s_voice
   | s_voice_port
   | s_vpc
   | s_vpdn_group
   | s_vpn
   | s_vrf_context
   | s_vrf_definition
   | s_webvpn
   | s_wsma
   | s_xconnect_logging
   | srlg_stanza
   | standard_access_list_stanza
   | standard_ipv6_access_list_stanza
   | switching_mode_stanza
   | unrecognized_block_stanza
;

statistics_null
:
   NO?
   (
      EXTENDED_COUNTERS
      | TM_VOQ_COLLECTION
   ) ~NEWLINE* NEWLINE
;

switching_mode_stanza
:
   SWITCHING_MODE ~NEWLINE* NEWLINE
;

t_null
:
   (
      SOURCE_INTERFACE
   ) ~NEWLINE* NEWLINE
;

t_server
:
   SERVER hostname = variable_hostname NEWLINE
   (
      t_server_address
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
   ) ~NEWLINE* NEWLINE
;

tap_null
:
   NO?
   (
      MODE
   ) ~NEWLINE* NEWLINE
;

tg_null
:
   NO?
   (
      ACCOUNTING_SERVER_GROUP
      | ADDRESS_POOL
      | AUTHENTICATION
      | AUTHENTICATION_SERVER_GROUP
      | DEFAULT_GROUP_POLICY
      | GROUP_URL
      | IPV6_ADDRESS_POOL
      | ISAKMP
   ) ~NEWLINE* NEWLINE
;

track_null
:
   NO?
   (
      DELAY
      | OBJECT
      | TYPE
   ) ~NEWLINE* NEWLINE
;

ts_host
:
   HOST hostname =
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) ~NEWLINE* NEWLINE
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
   ) ~NEWLINE* NEWLINE
;

vi_address_family
:
   NO? ADDRESS_FAMILY ~NEWLINE* NEWLINE
   (
      viaf_vrrp
   )*
;

viaf_vrrp
:
   NO? VRRP ~NEWLINE* NEWLINE
   (
      viafv_null
   )*
;

viafv_null
:
   NO?
   (
      ADDRESS
      | PREEMPT
      | PRIORITY
      | TIMERS
      | TRACK
   ) ~NEWLINE* NEWLINE
;

vlan_null
:
   NO?
   (
      ACTION
      | BACKUPCRF
      | BRIDGE
      | MATCH
      | MEDIA
      | MTU
      | MULTICAST
      | NAME
      | PARENT
      | PRIORITY
      | PRIVATE_VLAN
      | REMOTE_SPAN
      | ROUTER_INTERFACE
      | SPANNING_TREE
      | STATE
      | STATISTICS
      | STP
      | TAGGED
      | TRUNK
      | TB_VLAN1
      | TB_VLAN2
      | UNTAGGED
   ) ~NEWLINE* NEWLINE
;

voice_null
:
   NO?
   (
      ALLOW_CONNECTIONS
      | FAX
      | H225
      | H323
      | RULE
      | SHUTDOWN
   ) ~NEWLINE* NEWLINE
;

vp_null
:
   NO?
   (
      CALLER_ID
      | DESCRIPTION
      | ECHO_CANCEL
      | SHUTDOWN
      | SIGNAL
      | TIMEOUTS
      | TIMING
   ) ~NEWLINE* NEWLINE
;

vpc_null
:
   NO?
   (
      DELAY
      | IP
      | PEER_GATEWAY
      | PEER_KEEPALIVE
      | PEER_SWITCH
      | ROLE
      | SYSTEM_PRIORITY
   ) ~NEWLINE* NEWLINE
;

vpdng_accept_dialin
:
   NO? ACCEPT_DIALIN ~NEWLINE* NEWLINE
   (
      vpdnga_null
   )*
;

vpdng_null
:
   NO?
   (
      L2TP
   ) ~NEWLINE* NEWLINE
;

vpdnga_null
:
   NO?
   (
      PROTOCOL
      | VIRTUAL_TEMPLATE
   ) ~NEWLINE* NEWLINE
;

vpn_null
:
   NO?
   (
      CLUSTER
      | PARTICIPATE
      | PRIORITY
      | REDIRECT_FQDN
   ) ~NEWLINE* NEWLINE
;

vrfc_ip_route
:
   IP ROUTE ip_route_tail
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

vrfd_null
:
   NO?
   (
      RD
      | ROUTE_TARGET
      |
      (
         NO SHUTDOWN
      )
   ) ~NEWLINE* NEWLINE
;

vrrp_interface
:
   NO? INTERFACE interface_name NEWLINE
   (
      vi_address_family
   )* NEWLINE?
;

wccp_id
:
   id = DEC
   (
      (
         GROUP_LIST group_list = variable
      )
      |
      (
         MODE
         (
            CLOSED
            | OPEN
         )
      )
      |
      (
         PASSWORD DEC? password = variable
      )
      |
      (
         REDIRECT_LIST redirect_list = variable
      )
      |
      (
         SERVICE_LIST service_list = variable
      )
   )* NEWLINE
;

wccp_null
:
   (
      CHECK
      | OUTBOUND_ACL_CHECK
      | SOURCE_INTERFACE
      | VERSION
      | WEB_CACHE
   ) ~NEWLINE* NEWLINE
;

webvpn_null
:
   NO?
   (
      ANYCONNECT
      | ANYCONNECT_ESSENTIALS
      | CACHE
      | CSD
      | DISABLE
      | ENABLE
      | ERROR_RECOVERY
      | KEEPOUT
      | TUNNEL_GROUP_LIST
   ) ~NEWLINE* NEWLINE
;

wsma_null
:
   NO?
   (
      PROFILE
      | TRANSPORT
   ) ~NEWLINE* NEWLINE
;
