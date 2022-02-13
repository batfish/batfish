parser grammar CiscoXr_interface;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

if_autostate
:
   NO? AUTOSTATE NEWLINE
;

if_bandwidth
:
   NO? BANDWIDTH uint_legacy KBPS? NEWLINE
;

if_bfd
:
  // Only valid on Bundle-Ether
  BFD
  (
    if_bfd_address_family
    | if_bfd_mode
  )
;

if_bfd_address_family
:
  ADDRESS_FAMILY
  (
    if_bfdaf_ipv4
    | if_bfdaf_ipv6
  )
;

if_bfdaf_ipv4
:
  IPV4
  (
    if_bfdaf4_destination
    | if_bfdaf4_echo
    | if_bfdaf_fast_detect
    | if_bfdaf_minimum_interval
    | if_bfdaf_multiplier
    | if_bfdaf_timers
  )
;

if_bfdaf4_destination: DESTINATION IP_ADDRESS NEWLINE;

if_bfdaf4_echo: ECHO MINIMUM_INTERVAL bfd_echo_minimum_interval_ms NEWLINE;

bfd_echo_minimum_interval_ms
:
  // 15-2000ms
  uint16
;

if_bfdaf_fast_detect: FAST_DETECT NEWLINE;

if_bfdaf_minimum_interval: MINIMUM_INTERVAL bfd_minimum_interval_ms NEWLINE;

bfd_minimum_interval_ms
:
  // 3-30000ms
  uint16
;

if_bfdaf_multiplier: MULTIPLIER bfd_multiplier NEWLINE;

bfd_multiplier
:
  // 2-50
  uint8
;

if_bfdaf_timers
:
  TIMERS
  (
    if_bfdaf_timers_nbr_unconfig
    | if_bfdaf_timers_start
  )
;

if_bfdaf_timers_nbr_unconfig: NBR_UNCONFIG bfd_nbr_unconfig_time_s NEWLINE;

bfd_nbr_unconfig_time_s
:
  // 60-3600
  uint16
;

if_bfdaf_timers_start: START bfd_start_time_s NEWLINE;

bfd_start_time_s
:
  // 60-3600
  uint16
;

if_bfdaf_ipv6
:
  IPV6
  (
    if_bfdaf6_destination
    | if_bfdaf_fast_detect
    | if_bfdaf_minimum_interval
    | if_bfdaf_multiplier
    | if_bfdaf_timers
  )
;

if_bfdaf6_destination: DESTINATION IPV6_ADDRESS NEWLINE;

if_bfd_mode: MODE (CISCO | IETF) NEWLINE;

if_bundle
:
  BUNDLE
  (
    if_bundle_id
    | if_bundle_null
  )
;

if_bundle_id
:
  ID id = uint_legacy MODE (ACTIVE | ON | PASSIVE) NEWLINE
;

if_bundle_null
:
  (
    MAXIMUM_ACTIVE
    | MINIMUM_ACTIVE
    | PORT_PRIORITY
  ) null_rest_of_line
;

if_cdp: CDP NEWLINE;

if_channel_group
:
   CHANNEL_GROUP num = uint_legacy
   (
      MODE
      (
         ACTIVE
         | DESIRABLE
         | AUTO
         | ON
         | PASSIVE
      )
      (
        NON_SILENT
        | SILENT
      )?
   )? NEWLINE
;

if_crypto_map
:
   CRYPTO MAP name = variable NEWLINE
;

if_default_gw
:
   DEFAULT_GW IP_ADDRESS NEWLINE
;

if_delay
:
   NO? DELAY uint_legacy NEWLINE
;

if_description
:
   description_line
;

if_encapsulation
:
  ENCAPSULATION DOT1Q vlan = vlan_id NEWLINE
;

if_flow
:
  FLOW (IPV4 | IPV6) MONITOR flow_monitor_map_name SAMPLER sampler_map_name (EGRESS | INGRESS) NEWLINE
;

if_flow_sampler
:
   NO? FLOW_SAMPLER variable EGRESS? NEWLINE
;

if_ip_authentication
:
   IP AUTHENTICATION
   (
     if_ip_auth_key_chain
     | if_ip_auth_mode
   )
;

if_ip_auth_key_chain
:
   KEY_CHAIN EIGRP asn = uint_legacy name = variable_permissive NEWLINE
;

if_ip_auth_mode
:
   MODE EIGRP asn = uint_legacy MD5 NEWLINE
;

if_ip_dhcp
:
   NO? IP DHCP
   (
      ifdhcp_null
      | ifdhcp_relay
   )
;

if_ip_forward
:
   NO? IP FORWARD NEWLINE
;

if_ip_hello_interval
:
   IP HELLO_INTERVAL EIGRP asn = uint_legacy interval = uint_legacy NEWLINE
;

if_ip_helper_address
:
   IP HELPER_ADDRESS address = IP_ADDRESS NEWLINE
;

if_ip_hold_time
:
   IP HOLD_TIME EIGRP asn = uint_legacy interval = uint_legacy NEWLINE
;

if_ip_igmp
:
   NO? IP IGMP
   (
      NEWLINE
      | ifigmp_access_group
      | ifigmp_host_proxy
      | ifigmp_null
      | ifigmp_static_group
   )
;

if_ip_nat_inside
:
   IP NAT INSIDE NEWLINE
;

if_ip_nat_outside
:
   IP NAT OUTSIDE NEWLINE
;

if_ip_nbar
:
   IP NBAR PROTOCOL_DISCOVERY (IPV4 | IPV6)? NEWLINE
;

if_ip_passive_interface_eigrp
:
   NO? IP PASSIVE_INTERFACE EIGRP tag = uint_legacy NEWLINE
;

if_ip_pim_neighbor_filter
:
   IP PIM NEIGHBOR_FILTER acl = variable NEWLINE
;

if_ip_proxy_arp
:
   (NO | DEFAULT)? IP PROXY_ARP NEWLINE
;

if_ip_router_isis
:
   IP ROUTER ISIS null_rest_of_line
;

if_ip_rtp
:
   IP RTP HEADER_COMPRESSION (PASSIVE | IPHC_FORMAT | IETF_FORMAT) PERIODIC_REFRESH? NEWLINE
;

if_ip_sticky_arp
:
   (NO? IP STICKY_ARP NEWLINE)
   |
   (IP STICKY_ARP IGNORE NEWLINE)
;

if_ip_summary_address
:
   IP SUMMARY_ADDRESS EIGRP asn = uint_legacy
   (
      addr = IP_ADDRESS netmask = IP_ADDRESS
      | prefix = IP_PREFIX
   )
   NEWLINE
;

if_ip_tcp
:
   IP TCP
   (
      if_ip_tcp_adjust_mss
      | if_ip_tcp_compression_connections
      | if_ip_tcp_header_compression
   )
;

if_ip_tcp_adjust_mss
:
   ADJUST_MSS value = uint_legacy NEWLINE
;

if_ip_tcp_compression_connections
:
   COMPRESSION_CONNECTIONS value = uint_legacy NEWLINE
;

if_ip_tcp_header_compression
:
   HEADER_COMPRESSION ( PASSIVE | IETF_FORMAT )? NEWLINE
;

if_ip_verify
:
   IP VERIFY UNICAST
   (
      (
         NOTIFICATION THRESHOLD uint_legacy
      )
      |
      (
         REVERSE_PATH ALLOW_SELF_PING? acl = uint_legacy?
      )
      |
      (
         SOURCE REACHABLE_VIA
         (
            ANY
            | RX
         )
         (
            ALLOW_DEFAULT
            | ALLOW_SELF_PING
            | L2_SRC
         )* acl = uint_legacy?
      )
   ) NEWLINE
;

if_ipv4: IPV4 if_ipv4_inner;

if_ipv4_inner
:
  if_ipv4_access_group
  | if_ipv4_address
  | if_ipv4_null
;

if_ipv4_access_group
:
  ACCESS_GROUP
  (
    COMMON common_acl = access_list_name
    (
      INGRESS HARDWARE_COUNT?
      | interface_acl = access_list_name INGRESS
        (HARDWARE_COUNT INTERFACE_STATISTICS? | INTERFACE_STATISTICS HARDWARE_COUNT?)?
    )
    | interface_acl = access_list_name (EGRESS | INGRESS)
      (HARDWARE_COUNT INTERFACE_STATISTICS? | INTERFACE_STATISTICS HARDWARE_COUNT?)?
  ) NEWLINE
;

if_ipv4_address: ADDRESS interface_ipv4_address SECONDARY? (ROUTE_TAG tag=route_tag)? NEWLINE;

interface_ipv4_address
:
  address = IP_ADDRESS mask = IP_ADDRESS
  | prefix = IP_PREFIX
;

if_ipv4_null
:
// TODO: some of these should be handled or at least warn
  (
    MTU
    | POINT_TO_POINT
    | UNNUMBERED
    | UNREACHABLES
    | VERIFY
  ) null_rest_of_line
;

if_ipv6
:
   IPV6 if_ipv6_inner
;

if_ipv6_inner
:
   if_ipv6_access_group
   | if_ipv6_address
   | if_ipv6_enable
   | if_ipv6_nd
;

if_ipv6_access_group
:
  ACCESS_GROUP
  (
    COMMON common_acl = access_list_name
    (
      INGRESS
      | interface_acl = access_list_name INGRESS INTERFACE_STATISTICS?
    )
    | interface_acl = access_list_name (EGRESS | INGRESS) INTERFACE_STATISTICS?
  ) NEWLINE
;

if_ipv6_address
:
  ADDRESS
  (
    address = IPV6_ADDRESS (LINK_LOCAL | len = ipv6_interface_address_length)
    | prefix = IPV6_PREFIX EUI_64?
  ) (ROUTE_TAG tag = route_tag)? NEWLINE
;

ipv6_interface_address_length
:
  // 1-128
  uint8
;

if_ipv6_enable
:
   ENABLE NEWLINE
;

if_ipv6_nd
:
  ND SUPPRESS_RA NEWLINE
;

if_isis_circuit_type
:
   ISIS CIRCUIT_TYPE
   (
      LEVEL_1
      | LEVEL_2_ONLY
      | LEVEL_2
   ) NEWLINE
;

if_isis_enable
:
   ISIS ENABLE num = uint_legacy NEWLINE
;

if_isis_hello_interval
:
   ISIS HELLO_INTERVAL uint_legacy
   (
      LEVEL_1
      | LEVEL_2
   )? NEWLINE
;

if_isis_metric
:
   ISIS IPV6? METRIC metric = uint_legacy
   (
      LEVEL_1
      | LEVEL_2
   )? NEWLINE
;

if_isis_network
:
   ISIS NETWORK POINT_TO_POINT NEWLINE
;

if_isis_passive
:
   ISIS PASSIVE NEWLINE
;

if_isis_tag
:
   ISIS TAG tag = uint_legacy NEWLINE
;

if_load_interval
:
   LOAD_INTERVAL li = uint_legacy NEWLINE
;

if_mtu
:
   MTU mtu_size = uint_legacy NEWLINE
;

if_no_bfd
:
   NO BFD (IPV4 | IPV6)?
   (
       AUTHENTICATION
       | ECHO
       | ECHO_RX_INTERVAL
       | INTERVAL
       | NEIGHBOR SRC_IP src_ip = IP_ADDRESS DEST_IP dst_ip = IP_ADDRESS
       | NEIGHBOR SRC_IP src_ip6 = IPV6_ADDRESS DEST_IP dst_ip6 = IPV6_ADDRESS
       | OPTIMIZE SUBINTERFACE
   ) NEWLINE
;

if_no_ip_address
:
   NO IP ADDRESS NEWLINE
;

if_no_routing_dynamic
:
   NO ROUTING DYNAMIC NEWLINE
;

if_null_block
:
   NO?
   (
      ACTIVE
      | AFFINITY
      | ANTENNA
      | ARP
      | ASYNC
      | ATM
      | AUTHENTICATION
      | AUTO
      | AUTOROUTE
      | BANDWIDTH GUARANTEED
      | BANDWIDTH INHERIT
      | BANDWIDTH PERCENT_LITERAL
      | BEACON
      | BGP_POLICY
      | BRIDGE_GROUP
      | CABLE
      | CABLELENGTH
      | CARRIER_DELAY
      | CHANNEL
      | CHANNEL_PROTOCOL
      | CLASS
      | CLNS
      | COUNTER
      | CRC
      | CRYPTO
      | DAMPENING
      | DCB
      | DCBX
      | DCB_POLICY
      | DELAY
      | DESTINATION
      | DIALER
      | DIALER_GROUP
      | DFS
      | DOWNSTREAM
      | DSL
      |
      (
         DSU BANDWIDTH
      )
      | DUPLEX
      | ENABLE
      | ENCRYPTION
      | ETHERNET
      | EXIT
      | FAIR_QUEUE
      | FAST_REROUTE
      | FLOW_CONTROL
      | FLOWCONTROL
      | FORWARDER
      | FRAME_RELAY
      | FRAMING
      | FULL_DUPLEX
      | GIG_DEFAULT
      | GLBP
      | GROUP_RANGE
      | H323_GATEWAY
      | HALF_DUPLEX
      | HARDWARE
      | HISTORY
      | HOLD_QUEUE
      | IGNORE
      | INGRESS
      |
      (
         IP
         (
            ACCOUNTING
            | ADDRESS
            (
               NEGOTIATED
            )
            | ARP
            | BGP
            | BROADCAST_ADDRESS
            | CGMP
            | CONTROL_APPS_USE_MGMT_PORT
            | DVMRP
            |
            (
               DIRECTED_BROADCAST
            )
            | IP_ADDRESS
            | IRDP
            | LOAD_SHARING
            | MASK_REPLY
            | MROUTE_CACHE
            | MTU
            | MULTICAST
            | MULTICAST_BOUNDARY
            | NHRP
            |
            (
               OSPF
               (
                  AUTHENTICATION
                  | AUTHENTICATION_KEY
                  | BFD
                  | DEMAND_CIRCUIT
                  | MESSAGE_DIGEST_KEY
                  | MTU_IGNORE
                  | PRIORITY
                  | RETRANSMIT_INTERVAL
                  | TRANSMIT_DELAY
               )
            )
            |
            (
               PIM
               (
                  BORDER
                  | BORDER_ROUTER
                  | BSR_BORDER
                  | DENSE_MODE
                  | DR_PRIORITY
                  | HELLO_INTERVAL
                  | PASSIVE
                  | QUERY_INTERVAL
                  | SNOOPING
                  | SPARSE_DENSE_MODE
                  | SPARSE_MODE
                  | SPARSE_MODE_SSM
               )
            )
            | PIM_SPARSE
            | PORT_UNREACHABLE
            | REDIRECT
            | REDIRECTS
            | RIP
            | ROUTE_CACHE
            | RSVP
            | SAP
            | SDR
            | UNNUMBERED
            | UNREACHABLES
            | VERIFY
            | VIRTUAL_REASSEMBLY
            | WCCP
         )
      )
      | ISDN
      |
      (
         ISIS
         (
            AUTHENTICATION
            | CSNP_INTERVAL
            | DS_HELLO_INTERVAL
            | HELLO
            | HELLO_INTERVAL
            | HELLO_MULTIPLIER
            | LSP_INTERVAL
            | POINT_TO_POINT
            | PROTOCOL
            | SMALL_HELLO
            | WIDE_METRIC
         )
      )
      | KEEPALIVE
      | L2_FILTER
      | L2PROTOCOL_TUNNEL
      | L2TRANSPORT
      | LANE
      | LAPB
      | LACP
      | LINK
      | LINK_FAULT_SIGNALING
      | LLDP
      | LOAD_BALANCING
      | LOAD_INTERVAL
      | LOGGING
      | LOOPBACK
      | LRE
      | MAC
      | MAC_ADDRESS
      | MACRO
      | MANAGEMENT
      | MANAGEMENT_ONLY
      | MAP_GROUP
      | MDIX
      | MEDIA_TYPE
      | MEDIUM
      | MEMBER
      | MINIMUM_LINKS
      | MLS
      | MOBILITY
      | MOP
      | MPLS
      | NAME
      | NEGOTIATE
      | NEGOTIATION
      | NMSP
      |
      (
         NO
         (
            DESCRIPTION
         )
      )
      |
      (
         NTP
         (
            BROADCAST
            | DISABLE
            | MULTICAST
         )
      )
      | NV
      | OPENFLOW
      | OPTICAL_MONITOR
      | OSPFV3
      | PACKET
      | PATH_OPTION
      | PEAKDETECT
      | PEER
      | PFC PRIORITY
      | PHYSICAL_LAYER
      | PLATFORM
      | PORT_CHANNEL
      | PORT_CHANNEL_PROTOCOL
      | PORT_NAME
      | PORT_TYPE
      | PORTMODE
      | POS
      | POWER
      | POWER_LEVEL
      | PPP
      | PREEMPT
      | PRIORITY
      | PRIORITY_FLOW_CONTROL
      | PRIORITY_QUEUE
      | PVC
      | QOS
      | QUEUE_MONITOR
      | QUEUE_SET
      | RANDOM_DETECT
      | RATE_LIMIT
      | RATE_MODE
      | RCV_QUEUE
      | REDIRECTS
      | REMOTE
      | ROUTE_CACHE
      | ROUTE_ONLY
      | SCRAMBLE
      | SERIAL
      | SERVICE_MODULE
      | SFLOW
      | SHAPE
      | SIGNALLED_BANDWIDTH
      | SIGNALLED_NAME
      | SONET
      | SOURCE
      | SPEED_DUPLEX
      | SNMP
      | SRR_QUEUE
      | SSID
      | STACK_MIB
      | STATION_ROLE
      | STBC
      | STORM_CONTROL
      |
      (
         SWITCHPORT
         (
            BACKUP
            | BLOCK
            | DOT1Q
            | EMPTY
            |
            (
               MODE PRIVATE_VLAN
            )
            | MONITOR
            | NONEGOTIATE
            | PORT_SECURITY
            | PRIORITY
            | TAP
            | TOOL
            |
            (
               TRUNK PRUNING
            )
            | VOICE
            | VLAN
         )
      )
      | TAG_SWITCHING
      | TAGGED
      | TAP
      | TCAM
      | TRANSCEIVER
      | TRANSPORT_MODE
      | TRUST
      | TUNABLE_OPTIC
      | TX_QUEUE
      | UC_TX_QUEUE
      | UDLD
      | UNTAGGED
      | VLT_PEER_LAG
      | VMTRACER
      | VPC
      | VTP
      | WEIGHTING
      | WRR_QUEUE
      | X25
      | XCONNECT
   ) ~NEWLINE* NEWLINE if_null_inner*
;

if_null_inner
:
   NO?
   (
      ADDRESS
      | BACKUP
      | BRIDGE_DOMAIN
      | DIALER
      | L2PROTOCOL
      | MODE
      | PRIORITY
      | PROPAGATE
      | PROTOCOL
      | RECEIVE
      | REMOTE_PORTS
      | REWRITE
      | SATELLITE_FABRIC_LINK
      | TRANSMIT
      | VIRTUAL_ADDRESS
   ) ~NEWLINE* NEWLINE  // do not change to null_rest_of_line
;

if_null_single
:
  NO?
  (
    BCMC_OPTIMIZATION
    | DOT1X
    | IP TRAFFIC_EXPORT
    | JUMBO
    | LINKDEBOUNCE
    | MAB
    | PHY
    | REDUNDANCY
    |
    (
      SPEED NONEGOTIATE
    )
    | SWITCHPORT CAPTURE
    | SUPPRESS_ARP
    | TRIMODE
    | TRUSTED
  ) ~NEWLINE* NEWLINE // do not change to null_rest_of_line

;

if_port_security
:
   PORT SECURITY NEWLINE
   (
      if_port_security_null
   )*
;

if_private_vlan
:
   PRIVATE_VLAN MAPPING (ADD | REMOVE)? null_rest_of_line
;

if_rewrite_ingress_tag: REWRITE INGRESS TAG ifrit_policy NEWLINE;

ifrit_policy: ifrit_pop;

ifrit_pop: POP ifrit_pop_count SYMMETRIC?;

// 1 or 2
ifrit_pop_count: uint8;

if_routing_dynamic
:
   ROUTING DYNAMIC NEWLINE
;

if_service_instance
:
   SERVICE INSTANCE id = uint_legacy ETHERNET NEWLINE
   if_si_inner*
;

if_si_inner
:
    if_si_bridge_domain
    | if_si_encapsulation
    | if_si_l2protocol
    | if_si_no_bridge_domain
    | if_si_rewrite
    | if_si_service_policy
;

if_si_bridge_domain
:
    BRIDGE_DOMAIN id = uint_legacy SPLIT_HORIZON? NEWLINE
;

if_si_encapsulation
:
    NO? ENCAPSULATION null_rest_of_line
;

if_si_l2protocol
:
    L2PROTOCOL TUNNEL? (DROP | FORWARD | PEER)? (CDP | DOT1X | DTP | LACP | PAGP | STP | VTP)? NEWLINE
;

if_si_no_bridge_domain
:
    NO BRIDGE_DOMAIN id = uint_legacy NEWLINE
;

if_si_rewrite
:
    REWRITE null_rest_of_line
;

if_si_service_policy
:
    SERVICE_POLICY (INPUT | OUTPUT) policy_map = variable NEWLINE
;

if_spanning_tree
:
   NO? SPANNING_TREE
   (
      if_st_null
      | if_st_portfast
      | NEWLINE
   )
;

if_speed_auto
:
   SPEED AUTO NEWLINE
;

if_speed_ios
:
   SPEED mbits = uint_legacy NEWLINE
;

if_speed_ios_dot11radio
:
// https://www.cisco.com/en/US/docs/routers/access/800/880/software/configuration/guide/880_radio_config.html
   SPEED
   (
      BASIC_1_0
      | BASIC_2_0
      | BASIC_5_5
      | BASIC_6_0
      | BASIC_9_0
      | BASIC_11_0
      | BASIC_12_0
      | BASIC_18_0
      | BASIC_24_0
      | BASIC_36_0
      | BASIC_48_0
      | BASIC_54_0
      | DEFAULT
      | FLOAT
      | M0_7
      | M0_DOT
      | M1_DOT
      | M2_DOT
      | M3_DOT
      | M4_DOT
      | M5_DOT
      | M6_DOT
      | M7_DOT
      | M8_15
      | M8_DOT
      | M9_DOT
      | M10_DOT
      | M11_DOT
      | M12_DOT
      | M13_DOT
      | M14_DOT
      | M15_DOT
      | OFDM
      | OFDM_THROUGHPUT
      | ONLY_OFDM
      | RANGE
      | THROUGHPUT
   )* NEWLINE
;

if_st_null
:
   (
      BPDUFILTER
      | BPDUGUARD
      | COST
      | GUARD
      | LINK_TYPE
      | MST
      | PORT
      | PORT_PRIORITY
      | PRIORITY
      | PROTECT
      | RSTP
      | VLAN
   ) null_rest_of_line
;

if_st_portfast
:
   PORTFAST
   (
      disable = DISABLE
      | edge = EDGE
      | network = NETWORK
      | trunk = TRUNK
   )* NEWLINE
;

if_port_security_null
:
   NO?
   (
      AGE
      | ENABLE
      | MAXIMUM
      | SECURE_MAC_ADDRESS
      | VIOLATION
   ) null_rest_of_line
;

if_service_policy
:
   SERVICE_POLICY
   (
      TYPE (
         CONTROL SUBSCRIBER
         | PBR
         | QOS
         | QUEUING
      )
   )?
   (INPUT | OUTPUT)?
   policy_map = variable NEWLINE
;

if_shutdown
:
   NO?
   (
      DISABLE
      | SHUTDOWN
   ) FORCE? LAN? NEWLINE
;

track_action
:
  track_action_decrement
;

track_action_decrement
:
  DECREMENT subtrahend = uint_legacy
;

if_switchport
:
   NO? SWITCHPORT NEWLINE
;

if_switchport_access
:
   SWITCHPORT ACCESS VLAN
   (
      vlan = uint_legacy
      | DYNAMIC
   ) NEWLINE
;

if_switchport_mode
:
   SWITCHPORT MODE
   (
      ACCESS
      | DOT1Q_TUNNEL
      |
      (
         DYNAMIC
         (
            AUTO
            | DESIRABLE
         )
      )
      | FEX_FABRIC
      | if_switchport_mode_monitor
      | TAP
      | TOOL
      | TRUNK
   ) NEWLINE
;

if_switchport_mode_monitor
:
   MONITOR BUFFER_LIMIT limit=uint_legacy (BYTES | KBYTES | MBYTES | PACKETS)
;

if_switchport_private_vlan_association
:
   SWITCHPORT PRIVATE_VLAN ASSOCIATION TRUNK primary_vlan_id = uint_legacy
   secondary_vlan_id = uint_legacy NEWLINE
;

if_switchport_private_vlan_host_association
:
   SWITCHPORT PRIVATE_VLAN HOST_ASSOCIATION primary_vlan_id = uint_legacy
   secondary_vlan_id = uint_legacy NEWLINE
;

if_switchport_private_vlan_mapping
:
   SWITCHPORT PRIVATE_VLAN MAPPING TRUNK? primary_vlan_id = uint_legacy
   secondary_vlan_list = range NEWLINE
;

if_switchport_trunk_allowed
:
   SWITCHPORT TRUNK ALLOWED VLAN
   (
      NONE
      |
      (
         ADD? r = range
      )
   ) NEWLINE
;

if_switchport_trunk_encapsulation
:
   SWITCHPORT TRUNK ENCAPSULATION e = switchport_trunk_encapsulation NEWLINE
;

if_switchport_trunk_native
:
   SWITCHPORT TRUNK NATIVE VLAN vlan = uint_legacy NEWLINE
;

if_tunnel
:
   TUNNEL
   (
       iftunnel_bandwidth
       | iftunnel_destination
       | iftunnel_key
       | iftunnel_mode
       | iftunnel_path_mtu_discovery
       | iftunnel_protection
       | iftunnel_source
   )
;

if_vlan
:
  VLAN vlan = vlan_id NEWLINE
;

if_vrf
:
   VRF name = vrf_name NEWLINE
;

ifdhcp_null
:
   (
      SMART_RELAY
      | SNOOPING
   ) null_rest_of_line
;

ifdhcp_relay
:
   RELAY
   (
      ifdhcpr_address
      | ifdhcpr_client
      | ifdhcpr_null
   )
;

ifdhcpr_address
:
   ADDRESS address = IP_ADDRESS NEWLINE
;

ifdhcpr_client
:
   CLIENT NEWLINE
;

ifdhcpr_null
:
   (
      INFORMATION
      | SUBNET_BROADCAST
   ) null_rest_of_line
;

ifigmp_access_group
:
   ACCESS_GROUP name = variable NEWLINE
;

ifigmp_host_proxy
:
   HOST_PROXY (
       ifigmphp_access_list
       | ifigmphp_null
   )
;

ifigmphp_access_list
:
   ACCESS_LIST name = access_list_name NEWLINE
;

ifigmphp_null
:
   (
      EXCLUDE
      | INCLUDE
      | IP_ADDRESS
      | REPORT_INTERVAL
      | VERSION
   ) null_rest_of_line

;

ifigmp_null
:
   (
      GROUP_TIMEOUT
      | JOIN_GROUP
      | LAST_MEMBER_QUERY_COUNT
      | LAST_MEMBER_QUERY_INTERVAL
      | LAST_MEMBER_QUERY_RESPONSE_TIME
      | MULTICAST_STATIC_ONLY
      | QUERY_INTERVAL
      | QUERY_MAX_RESPONSE_TIME
      | QUERY_TIMEOUT
      | ROBUSTNESS_VARIABLE
      | ROUTER_ALERT
      | SNOOPING
      | STARTUP_QUERY_COUNT
      | STARTUP_QUERY_INTERVAL
      | VERSION
   ) null_rest_of_line
;

ifigmp_static_group
:
   STATIC_GROUP
   (
      ifigmpsg_acl
      | ifigmpsg_null
   )
;

ifigmpsg_acl
:
   ACL name = variable NEWLINE
;

ifigmpsg_null
:
   (
      IP_ADDRESS
      | RANGE
   ) null_rest_of_line
;

iftunnel_bandwidth
:
   BANDWIDTH 
   (
      RECEIVE
      | TRANSMIT
   ) uint_legacy NEWLINE
;


iftunnel_destination
:
   DESTINATION IP_ADDRESS NEWLINE
;

iftunnel_key
:
   KEY keynum = uint_legacy NEWLINE
;

iftunnel_mode
:
   MODE
   (
     | gre_ipv4 = GRE IPV4 // ios-xr
     | gre_multipoint = GRE MULTIPOINT
     | ipsec_ipv4 = IPSEC IPV4
     | ipv6ip = IPV6IP
   )
   NEWLINE
;

iftunnel_path_mtu_discovery
:
   PATH_MTU_DISCOVERY NEWLINE
;

iftunnel_protection
:
   PROTECTION IPSEC PROFILE name = variable NEWLINE
;

iftunnel_source
:
   SOURCE 
   (
     DYNAMIC
     | IP_ADDRESS
     | iname = interface_name
   ) NEWLINE
;

s_interface
:
   INTERFACE PRECONFIGURE? iname = interface_name
   (
      L2TRANSPORT
      | MULTIPOINT
      | POINT_TO_POINT
   )?
   NEWLINE
   if_inner*
;

if_inner
:
   if_autostate
   | if_bandwidth
   | if_bfd
   | if_bundle
   | if_cdp
   | if_channel_group
   | if_crypto_map
   | if_default_gw
   | if_delay
   | if_description
   | if_encapsulation
   | if_flow
   | if_flow_sampler
   | if_ip_proxy_arp
   | if_ip_verify
   | if_ip_authentication
   | if_ip_dhcp
   | if_ip_forward
   | if_ip_hello_interval
   | if_ip_helper_address
   | if_ip_hold_time
   | if_ip_igmp
   | if_ip_nat_inside
   | if_ip_nat_outside
   | if_ip_nbar
   | if_ip_passive_interface_eigrp
   | if_ip_pim_neighbor_filter
   | if_ip_router_isis
   | if_ip_rtp
   | if_ip_sticky_arp
   | if_ip_summary_address
   | if_ip_tcp
   | if_ipv4
   | if_ipv6
   | if_isis_circuit_type
   | if_isis_enable
   | if_isis_hello_interval
   | if_isis_metric
   | if_isis_network
   | if_isis_passive
   | if_isis_tag
   | if_load_interval
   | if_mtu
   | if_no_bfd
   | if_no_ip_address
   | if_no_routing_dynamic
   | if_port_security
   | if_private_vlan
   | if_rewrite_ingress_tag
   | if_routing_dynamic
   | if_service_instance
   | if_service_policy
   | if_shutdown
   | if_spanning_tree
   | if_speed_auto
   | if_speed_ios
   | if_speed_ios_dot11radio
   | if_switchport
   | if_switchport_access
   | if_switchport_mode
   | if_switchport_private_vlan_association
   | if_switchport_private_vlan_host_association
   | if_switchport_private_vlan_mapping
   | if_switchport_trunk_allowed
   | if_switchport_trunk_encapsulation
   | if_switchport_trunk_native
   | if_tunnel
   | if_vlan
   | if_vrf
   // do not rearrange items below
 
   | if_null_single
   | if_null_block
;
