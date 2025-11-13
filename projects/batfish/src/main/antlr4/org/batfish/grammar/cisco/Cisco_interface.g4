parser grammar Cisco_interface;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

if_autostate
:
   NO? AUTOSTATE NEWLINE
;

if_bandwidth
:
   NO? BANDWIDTH dec KBPS? NEWLINE
;

if_bfd
:
  BFD (IPV4 | IPV6)? (
     if_bfd_authentication
     | if_bfd_echo
     | if_bfd_echo_rx_interval
     | if_bfd_interval
     | if_bfd_neighbor
     | if_bfd_optimize
     | if_bfd_template
  )
;

if_bfd_authentication
:
  AUTHENTICATION KEYED_SHA1 KEYID id = dec (
     HEX_KEY hex_key = variable
     | KEY ascii_key = variable
  )NEWLINE
;

if_bfd_echo
:
  ECHO NEWLINE
;

if_bfd_echo_rx_interval
:
  ECHO_RX_INTERVAL ms = dec NEWLINE
;

if_bfd_interval
:
  INTERVAL tx_ms = dec (MIN_RX | MIN_RX_VAR) tx_ms = dec MULTIPLIER mult = dec NEWLINE
;

if_bfd_neighbor
:
  NEIGHBOR SRC_IP (
     src_ip = IP_ADDRESS DEST_IP dst_ip = IP_ADDRESS
     | src_ip = IPV6_ADDRESS DEST_IP dst_ip = IPV6_ADDRESS
  ) NEWLINE
;

if_bfd_optimize
:
  OPTIMIZE SUBINTERFACE NEWLINE
;

if_bfd_template
:
  TEMPLATE name = variable_permissive NEWLINE
;

if_channel_group
:
   CHANNEL_GROUP num = dec
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

if_cts
:
   CTS
   (
      ifcts_dot1x
      | ifcts_layer3_null
      | ifcts_manual
   )
;

if_default_gw
:
   DEFAULT_GW IP_ADDRESS NEWLINE
;

if_description
:
   description_line
;

if_delay
:
   NO? DELAY dec NEWLINE
;

if_device_tracking
:
  DEVICE_TRACKING ifdt_attach_policy
;

ifdt_attach_policy
:
  ATTACH_POLICY name = device_tracking_policy_name NEWLINE
;

if_encapsulation
:
  ENCAPSULATION
  (
    if_encapsulation_dot1q
    | if_encapsulation_null
  )
;

if_encapsulation_dot1q
:
  DOT1Q vlan = vlan_id NEWLINE
;

if_encapsulation_null
:
  (
    DEFAULT
  ) null_rest_of_line
;

if_flow_sampler
:
   NO? FLOW_SAMPLER variable EGRESS? NEWLINE
;

if_ip
:
  IP
  (
    if_ip_address_dhcp
    | if_ip_authentication
    | if_ip_cef
    | if_ip_dhcp
    | if_ip_flow_monitor
    | if_ip_hello_interval
    | if_ip_helper_address
    | if_ip_hold_time
    | if_ip_inband_access_group
    | if_ip_mfib
    | if_ip_nat_inside
    | if_ip_nat_outside
    | if_ip_nbar
    | if_ip_ospf_area
    | if_ip_ospf_dead_interval
    | if_ip_ospf_dead_interval_minimal
    | if_ip_ospf_hello_interval
    | if_ip_ospf_network
    | if_ip_pim_neighbor_filter
    | if_ip_policy
    | if_ip_router_isis
    | if_ip_router_ospf_area
    | if_ip_rtp
    | if_ip_split_horizon
    | if_ip_summary_address
    | if_ip_tcp
    | if_ip_verify
    | if_ip_vrf_autoclassify
    | if_ip_vrf_receive
    | if_ip_vrf_select
    | if_ip_vrf_sitemap
  )
;

if_ip_access_group
:
   (
      (
         (
            IP
            | IPV4
         ) PORT? ACCESS_GROUP
      )
      |
      (
         ACCESS_LIST NAME
      )
   ) name = variable
   (
      EGRESS
      | IN
      | INGRESS
      | OUT
   )
   (
      HARDWARE_COUNT
      | OPTIMIZED
   )* NEWLINE
;

if_ip_address
:
   (IP | IPV4) ADDRESS
   VIRTUAL?
   (
      ip = IP_ADDRESS subnet = IP_ADDRESS
      | prefix = IP_PREFIX
   )
   (STANDBY standby_address = IP_ADDRESS)?
   (ROUTE_PREFERENCE pref=dec)?
   (TAG tag=dec)?
   NEWLINE
;

if_ip_address_dhcp
:
   ADDRESS DHCP NEWLINE
;

if_ip_address_secondary
:
   (
      IP
      | IPV4
   ) ADDRESS
   (
      (
         ip = IP_ADDRESS subnet = IP_ADDRESS
      )
      | prefix = IP_PREFIX
   ) SECONDARY DHCP_GIADDR? NEWLINE
;

if_ip_authentication
:
   AUTHENTICATION
   (
     if_ip_auth_key_chain
     | if_ip_auth_mode
   )
;

if_ip_auth_key_chain
:
   KEY_CHAIN EIGRP asn = dec name = variable_permissive NEWLINE
;

if_ip_auth_mode
:
   MODE EIGRP asn = dec MD5 NEWLINE
;

if_ip_cef
:
  CEF null_rest_of_line
;

if_ip_dhcp
:
   DHCP
   (
      ifipdhcp_null
      | ifipdhcp_relay
   )
;

if_ip_flow_monitor
:
   FLOW MONITOR name = variable
   (
      INPUT
      | OUTPUT
   ) NEWLINE
;

if_ip_forward
:
   NO? IP FORWARD NEWLINE
;

if_ip_hello_interval
:
   HELLO_INTERVAL EIGRP asn = dec interval = dec NEWLINE
;

if_ip_helper_address
:
   HELPER_ADDRESS address = IP_ADDRESS NEWLINE
;

if_ip_hold_time
:
   HOLD_TIME EIGRP asn = dec interval = dec NEWLINE
;

if_ip_inband_access_group
:
   INBAND ACCESS_GROUP name = variable_permissive NEWLINE
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

if_ip_mfib
:
  MFIB null_rest_of_line
;

if_ip_nat_inside
:
   NAT INSIDE NEWLINE
;

if_ip_nat_outside
:
   NAT OUTSIDE NEWLINE
;

if_ip_nbar
:
   NBAR PROTOCOL_DISCOVERY (IPV4 | IPV6)? NEWLINE
;

if_ip_ospf_area
:
   OSPF procname = variable AREA (area_ip = IP_ADDRESS | area_dec = dec) NEWLINE
;

if_ip_ospf_cost
:
   IP? OSPF COST cost = dec NEWLINE
;

if_ip_ospf_dead_interval
:
   OSPF DEAD_INTERVAL seconds = dec NEWLINE
;

if_ip_ospf_dead_interval_minimal
:
   OSPF DEAD_INTERVAL MINIMAL HELLO_MULTIPLIER mult = dec NEWLINE
;

if_ip_ospf_hello_interval
:
   OSPF HELLO_INTERVAL seconds = dec NEWLINE
;

if_ip_ospf_network
:
   OSPF NETWORK
   (
      BROADCAST
      | NON_BROADCAST
      |
      (
         POINT_TO_MULTIPOINT NON_BROADCAST?
      )
      | POINT_TO_POINT
   ) NEWLINE
;

if_ip_ospf_passive_interface
:
   NO? IP OSPF PASSIVE_INTERFACE NEWLINE
;

if_ip_ospf_shutdown
:
   NO? IP OSPF SHUTDOWN NEWLINE
;

if_ip_passive_interface_eigrp
:
   NO? IP PASSIVE_INTERFACE EIGRP tag = dec NEWLINE
;

if_ip_pim_neighbor_filter
:
   PIM NEIGHBOR_FILTER acl = variable NEWLINE
;

if_ip_policy
:
   POLICY ROUTE_MAP name = ~NEWLINE NEWLINE
;

if_ip_proxy_arp
:
   (NO | DEFAULT)? IP PROXY_ARP NEWLINE
;

if_ip_router_isis
:
   ROUTER ISIS null_rest_of_line
;

if_ip_router_ospf_area
:
   ROUTER OSPF procname = variable AREA (area_ip = IP_ADDRESS | area_dec = dec) NEWLINE
;

if_ip_rtp
:
   RTP HEADER_COMPRESSION (PASSIVE | IPHC_FORMAT | IETF_FORMAT) PERIODIC_REFRESH? NEWLINE
;

if_ip_split_horizon
:
  SPLIT_HORIZON NEWLINE
;

if_ip_sticky_arp
:
   (NO? IP STICKY_ARP NEWLINE)
   |
   (IP STICKY_ARP IGNORE NEWLINE)
;

if_ip_summary_address
:
   SUMMARY_ADDRESS EIGRP asn = dec
   (
      addr = IP_ADDRESS netmask = IP_ADDRESS
      | prefix = IP_PREFIX
   )
   (LEAK_MAP mapname = variable)? NEWLINE
;

if_ip_tcp
:
   TCP
   (
      if_ip_tcp_adjust_mss
      | if_ip_tcp_compression_connections
      | if_ip_tcp_header_compression
   )
;

if_ip_tcp_adjust_mss
:
   ADJUST_MSS value = dec NEWLINE
;

if_ip_tcp_compression_connections
:
   COMPRESSION_CONNECTIONS value = dec NEWLINE
;

if_ip_tcp_header_compression
:
   HEADER_COMPRESSION ( PASSIVE | IETF_FORMAT )? NEWLINE
;

if_ip_verify
:
   VERIFY UNICAST
   (
      (
         NOTIFICATION THRESHOLD dec
      )
      |
      (
         REVERSE_PATH ALLOW_SELF_PING? acl = dec?
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
         )* acl = dec?
      )
   ) NEWLINE
;

if_ip_vrf_autoclassify
:
   VRF AUTOCLASSIFY SOURCE NEWLINE
;

if_ip_vrf_forwarding
:
   IP? VRF FORWARDING vrf = variable (DOWNSTREAM vrf_down = variable)? NEWLINE
;

if_ip_vrf_receive
:
   VRF RECEIVE vrf = variable NEWLINE
;

if_ip_vrf_select
:
   VRF SELECT SOURCE NEWLINE
;

if_ip_vrf_sitemap
:
   VRF SITEMAP map = variable NEWLINE
;

if_ipv6
:
   IPV6 if_ipv6_inner
;

if_ipv6_inner
:
   if_ipv6_enable
   | if_ipv6_traffic_filter
;

if_ipv6_enable
:
   ENABLE NEWLINE
;

if_ipv6_traffic_filter
:
   TRAFFIC_FILTER acl = variable_aclname (IN | OUT) NEWLINE
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
   ISIS ENABLE num = dec NEWLINE
;

if_isis_hello_interval
:
   ISIS HELLO_INTERVAL dec
   (
      LEVEL_1
      | LEVEL_2
   )? NEWLINE
;

if_isis_metric
:
   ISIS IPV6? METRIC metric = dec
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
   ISIS TAG tag = dec NEWLINE
;

if_load_interval
:
   LOAD_INTERVAL li = dec NEWLINE
;

if_member_interface
:
  MEMBER_INTERFACE name = interface_name NEWLINE
;

if_mka
:
  MKA null_rest_of_line
;

if_mtu
:
   MTU mtu_size = dec NEWLINE
;

if_network_clock
:
  NETWORK_CLOCK null_rest_of_line
;

if_no
:
  NO
  (
    if_no_bfd
    | if_no_ip
    | if_no_mka
    | if_no_routing_dynamic
    | if_no_synchronous
  )
;

if_no_bfd
:
   BFD (IPV4 | IPV6)?
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

if_no_ip
:
  IP
  (
    if_no_ip_address
    | if_no_ip_dhcp
    | if_no_ip_flowspec
  )
;

if_no_ip_address
:
   ADDRESS NEWLINE
;

if_no_ip_dhcp
:
   DHCP
   (
      ifnoipdhcp_null
      | ifnoipdhcp_relay
   )
;

ifnoipdhcp_null
:
   (
      SMART_RELAY
      | SNOOPING
   ) null_rest_of_line
;

ifnoipdhcp_relay
:
   RELAY
   (
      ifnoipdhcpr_address
      | ifnoipdhcpr_client
      | ifnoipdhcpr_null
   )
;

ifnoipdhcpr_address
:
   ADDRESS NEWLINE
;

ifnoipdhcpr_client
:
   CLIENT NEWLINE
;

ifnoipdhcpr_null
:
   (
      INFORMATION
      | SUBNET_BROADCAST
   ) null_rest_of_line
;

if_no_ip_flowspec
:
  FLOWSPEC NEWLINE
;

if_no_mka
:
  MKA null_rest_of_line
;

if_no_routing_dynamic
:
   ROUTING DYNAMIC NEWLINE
;

if_no_synchronous
:
  SYNCHRONOUS null_rest_of_line
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
      | CDP
      | CHANNEL
      | CHANNEL_PROTOCOL
      | CLASS
      | CLNS
      | CLOCK
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
      | FLOW
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
            | FLOW
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
                  | JOIN_PRUNE_INTERVAL
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
      |
      (
         IPV4
         (
            ICMP
            | MTU
            | POINT_TO_POINT
            | UNNUMBERED
            | UNREACHABLES
            | VERIFY
         )
      )
      | IPV6
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
      | QOS
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
      | ENCAPSULATION
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

if_pvc
:
  PVC null_rest_of_line pvc_null*
;

pvc_null
:
  NO?
  (
    DIALER
    | ENCAPSULATION
    | PROTOCOL
  ) null_rest_of_line
;

if_routing_dynamic
:
   ROUTING DYNAMIC NEWLINE
;

if_service_instance
:
   SERVICE INSTANCE id = dec ETHERNET NEWLINE
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
    BRIDGE_DOMAIN id = dec SPLIT_HORIZON? NEWLINE
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
    NO BRIDGE_DOMAIN id = dec NEWLINE
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
   SPEED mbits = dec NEWLINE
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

if_standby
:
  NO? STANDBY
  (
    standby_group
    | standby_version
  ) NEWLINE
;

standby_group
:
  group = standby_group_number
  (
    standby_group_authentication
    | standby_group_ip
    | standby_group_preempt
    | standby_group_priority
    | standby_group_timers
    | standby_group_track
  )
;

standby_group_number
:
  // 0-255 for standby version 1
  // 0-4095 for standby version 2
  uint16
;

standby_group_authentication
:
  AUTHENTICATION auth = variable
;

standby_group_ip
:
  IP ip = IP_ADDRESS
;

standby_group_preempt
:
  PREEMPT standby_group_preempt_delay?
;

standby_group_preempt_delay
:
  DELAY
  (
     MINIMUM min_secs = dec
     | RELOAD reload_secs = dec
     | SYNC sync_secs = dec
  )+
;

standby_group_priority
:
  PRIORITY priority = dec
;

standby_group_timers
:
  TIMERS
  (
     MSEC hello_ms = dec
     | hello_sec = dec
  )
  (
     MSEC hold_ms = dec
     | hold_sec = dec
  )
;

standby_group_track
:
  TRACK num = track_number
   (
     // intentional blank, means decrement 10
     | DECREMENT decrement = pint8
     | SHUTDOWN
   )
;

standby_version
:
  VERSION version = standby_version_number
;

standby_version_number
:
  STANDBY_VERSION_1
  | STANDBY_VERSION_2
;

if_switchport
:
   NO? SWITCHPORT NEWLINE
;

if_switchport_access
:
   SWITCHPORT ACCESS VLAN
   (
      vlan = dec
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
   MONITOR BUFFER_LIMIT limit=dec (BYTES | KBYTES | MBYTES | PACKETS)
;

if_switchport_private_vlan_association
:
   SWITCHPORT PRIVATE_VLAN ASSOCIATION TRUNK primary_vlan_id = dec
   secondary_vlan_id = dec NEWLINE
;

if_switchport_private_vlan_host_association
:
   SWITCHPORT PRIVATE_VLAN HOST_ASSOCIATION primary_vlan_id = dec
   secondary_vlan_id = dec NEWLINE
;

if_switchport_private_vlan_mapping
:
   SWITCHPORT PRIVATE_VLAN MAPPING TRUNK? primary_vlan_id = dec
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
   SWITCHPORT TRUNK NATIVE VLAN vlan = dec NEWLINE
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
   VRF name = variable NEWLINE
;

if_vrf_member
:
   VRF MEMBER name = variable NEWLINE
;

if_vrrp
:
   VRRP groupnum = dec
   (
      ifvrrp_advertisement
      | ifvrrp_authentication
      | ifvrrp_description
      | ifvrrp_ip
      | ifvrrp_ipv4
      | ifvrrp_ipv6
      | ifvrrp_preempt
      | ifvrrp_priority
      | ifvrrp_priority_level
   )
;

if_vrrpno
:
   NO VRRP groupnum = dec
   (
      ifvrrpno_preempt
   )
;

ifvrrpno_preempt
:
   PREEMPT NEWLINE
;

if_zone_member
:
   ZONE_MEMBER SECURITY? name = variable_permissive NEWLINE
;

ifcts_dot1x
:
   DOT1X NEWLINE
   ifctsdot1x_null*
;

ifctsdot1x_null
:
    (DEFAULT | TIMER) null_rest_of_line
;

ifcts_manual
:
   MANUAL NEWLINE
   ifctsmanual_null*
;

ifctsmanual_null
:
   (POLICY | SAP) null_rest_of_line
;

ifcts_layer3_null
:
   LAYER3 (IPV4 | IPV6) null_rest_of_line
;

ifipdhcp_null
:
   (
      SMART_RELAY
      | SNOOPING
   ) null_rest_of_line
;

ifipdhcp_relay
:
   RELAY
   (
      ifipdhcpr_address
      | ifipdhcpr_client
      | ifipdhcpr_null
      | ifipdhcpr_source_interface
   )
;

ifipdhcpr_address
:
   ADDRESS address = IP_ADDRESS NEWLINE
;

ifipdhcpr_client
:
   CLIENT NEWLINE
;

ifipdhcpr_null
:
   (
      INFORMATION
      | SUBNET_BROADCAST
   ) null_rest_of_line
;

ifipdhcpr_source_interface
:
  SOURCE_INTERFACE iname = interface_name NEWLINE
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
   ACCESS_LIST name = variable NEWLINE
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
      | TCN
      | V3_QUERY_MAX_RESPONSE_TIME
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
   ) dec NEWLINE
;


iftunnel_destination
:
   DESTINATION IP_ADDRESS NEWLINE
;

iftunnel_key
:
   KEY keynum = dec NEWLINE
;

iftunnel_mode
:
   MODE
   (
     gre_multipoint = GRE MULTIPOINT
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
     | iname = interface_name_unstructured
   ) NEWLINE
;

ifvrrp_advertisement
:
   ADVERTISEMENT INTERVAL secs = dec NEWLINE
;

ifvrrp_authentication
:
   AUTHENTICATION TEXT text = variable_permissive NEWLINE
;

ifvrrp_description
:
   description_line
;

ifvrrp_ip
:
   IP ip = IP_ADDRESS SECONDARY? NEWLINE
;

ifvrrp_ipv4
:
   IPV4 ip = IP_ADDRESS NEWLINE
;

ifvrrp_ipv6
:
   IPV6 ip = IPV6_ADDRESS NEWLINE
;

ifvrrp_preempt
:
   PREEMPT DELAY
   (
      MINIMUM
      | RELOAD
   ) dec NEWLINE
;

ifvrrp_priority
:
   PRIORITY priority = dec NEWLINE
;

ifvrrp_priority_level
:
   PRIORITY_LEVEL priority = dec NEWLINE
;

s_interface
:
  INTERFACE
  (
    s_interface_definition
    | s_interface_nve1_null
  )
;

s_interface_definition
:
   PRECONFIGURE? iname = interface_name
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
   | if_channel_group
   | if_crypto_map
   | if_cts
   | if_default_gw
   | if_delay
   | if_description
   | if_device_tracking
   | if_encapsulation
   | if_flow_sampler
   | if_ip
   | if_ip_proxy_arp
   | if_ip_access_group
   | if_ip_address
   | if_ip_address_secondary
   | if_ip_forward
   | if_ip_igmp
   | if_ip_ospf_cost
   | if_ip_ospf_passive_interface
   | if_ip_ospf_shutdown
   | if_ip_passive_interface_eigrp
   | if_ip_sticky_arp
   | if_ip_vrf_forwarding
   | if_ipv6
   | if_isis_circuit_type
   | if_isis_enable
   | if_isis_hello_interval
   | if_isis_metric
   | if_isis_network
   | if_isis_passive
   | if_isis_tag
   | if_load_interval
   | if_member_interface
   | if_mka
   | if_mtu
   | if_network_clock
   | if_no
   | if_port_security
   | if_private_vlan
   | if_pvc
   | if_routing_dynamic
   | if_service_instance
   | if_service_policy
   | if_shutdown
   | if_spanning_tree
   | if_speed_auto
   | if_speed_ios
   | if_speed_ios_dot11radio
   | if_standby
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
   | if_vrf_member
   | if_vrrp
   | if_vrrpno
   | if_zone_member
   // do not rearrange items below
 
   | if_null_single
   | if_null_block
;