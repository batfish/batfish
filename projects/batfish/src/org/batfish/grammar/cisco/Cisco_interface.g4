parser grammar Cisco_interface;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

default_gw_if_stanza
:
   DEFAULT_GW IP_ADDRESS NEWLINE
;

description_if_stanza
:
   description_line
;

hsrp_stanza
:
   HSRP group = DEC NEWLINE hsrp_stanza_tail
;

hsrp_stanza_tail
:
   (
      hsrpcl += hsrpc_stanza
   )*
;

hsrpc_stanza
:
   ip_address_hsrpc_stanza
   | preempt_hsrpc_stanza
   | priority_hsprc_stanza
   | track_hsrpc_stanza
;

if_stanza
:
   default_gw_if_stanza
   | description_if_stanza
   | ip_access_group_if_stanza
   | ip_address_if_stanza
   | ip_address_dhcp_if_stanza
   | ip_address_secondary_if_stanza
   | ip_ospf_cost_if_stanza
   | ip_ospf_dead_interval_if_stanza
   | ip_ospf_dead_interval_minimal_if_stanza
   | ip_ospf_hello_interval_if_stanza
   | ip_ospf_passive_interface_if_stanza
   | ip_policy_if_stanza
   | ip_router_isis_if_stanza
   | isis_circuit_type_if_stanza
   | isis_enable_if_stanza
   | isis_metric_if_stanza
   | isis_network_if_stanza
   | isis_passive_if_stanza
   | isis_tag_if_stanza
   | no_ip_address_if_stanza
   | null_if_stanza
   | shutdown_if_stanza
   | switchport_access_if_stanza
   | switchport_private_vlan_association_if_stanza
   | switchport_private_vlan_mapping_if_stanza
   | switchport_trunk_native_if_stanza
   | switchport_trunk_encapsulation_if_stanza
   | switchport_trunk_allowed_if_stanza
   | switchport_mode_stanza
   | unrecognized_line
   | vrf_forwarding_if_stanza
   | vrf_if_stanza
   | vrf_member_if_stanza
;

interface_stanza
:
   INTERFACE PRECONFIGURE? iname = interface_name
   (
      L2TRANSPORT
      | MULTIPOINT
      | POINT_TO_POINT
   )? NEWLINE interface_stanza_tail
;

interface_stanza_tail
:
   (
      ifsl += if_stanza
   )*
;

ip_access_group_if_stanza
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
   ) OPTIMIZED? NEWLINE
;

ip_address_hsrpc_stanza
:
   IP ip = IP_ADDRESS NEWLINE
;

ip_address_if_stanza
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
   )
   (
      STANDBY standby_address = IP_ADDRESS
   )? NEWLINE
;

ip_address_dhcp_if_stanza
:
   IP ADDRESS DHCP NEWLINE
;

ip_address_secondary_if_stanza
:
   IP ADDRESS
   (
      (
         ip = IP_ADDRESS subnet = IP_ADDRESS
      )
      | prefix = IP_PREFIX
   ) SECONDARY NEWLINE
;

ip_ospf_cost_if_stanza
:
   IP OSPF COST cost = DEC NEWLINE
;

ip_ospf_dead_interval_if_stanza
:
   IP OSPF DEAD_INTERVAL seconds = DEC NEWLINE
;

ip_ospf_dead_interval_minimal_if_stanza
:
   IP OSPF DEAD_INTERVAL MINIMAL HELLO_MULTIPLIER mult = DEC NEWLINE
;

ip_ospf_hello_interval_if_stanza
:
   IP OSPF HELLO_INTERVAL seconds = DEC NEWLINE
;

ip_ospf_passive_interface_if_stanza
:
   NO? IP OSPF PASSIVE_INTERFACE NEWLINE
;

ip_policy_if_stanza
:
   IP POLICY ROUTE_MAP name = ~NEWLINE NEWLINE
;

ip_router_isis_if_stanza
:
   IP ROUTER ISIS NEWLINE
;

isis_circuit_type_if_stanza
:
   ISIS CIRCUIT_TYPE LEVEL_2_ONLY NEWLINE
;

isis_enable_if_stanza
:
   ISIS ENABLE num = DEC NEWLINE
;

isis_metric_if_stanza
:
   ISIS METRIC metric = DEC NEWLINE
;

isis_network_if_stanza
:
   ISIS NETWORK POINT_TO_POINT NEWLINE
;

isis_passive_if_stanza
:
   ISIS PASSIVE NEWLINE
;

isis_tag_if_stanza
:
   ISIS TAG tag = DEC NEWLINE
;

no_ip_address_if_stanza
:
   NO IP ADDRESS NEWLINE
;

null_if_stanza
:
   hsrp_stanza
   |
   (
      NO? SWITCHPORT NEWLINE
   )
   | null_block_if_stanza
;

null_block_if_stanza
:
   NO?
   (
      AFFINITY
      | ARP
      | ASYNC
      | ATM
      | AUTO
      | AUTOROUTE
      | AUTOSTATE
      | BANDWIDTH
      | BEACON
      | BFD
      | BUNDLE
      | CABLELENGTH
      | CARRIER_DELAY
      | CDP
      | CHANNEL
      | CHANNEL_GROUP
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
      | DESTINATION
      |
      (
         DSU BANDWIDTH
      )
      | DUPLEX
      | ENABLE
      | ENCAPSULATION
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
      | HOLD_QUEUE
      |
      (
         HSRP VERSION
      )
      | IGNORE
      |
      (
         INTERFACE BREAKOUT
      )
      |
      (
         IP
         (
            ACCOUNTING
            | ARP
            | BROADCAST_ADDRESS
            | CGMP
            | CONTROL_APPS_USE_MGMT_PORT
            | DHCP
            | DVMRP
            |
            (
               DIRECTED_BROADCAST
            )
            | FLOW
            | HELPER_ADDRESS
            | IGMP
            | IP_ADDRESS
            | IRDP
            | LOAD_SHARING
            | MROUTE_CACHE
            | MTU
            | MULTICAST
            |
            (
               OSPF
               (
                  AUTHENTICATION
                  | AUTHENTICATION_KEY
                  | BFD
                  | MESSAGE_DIGEST_KEY
                  | MTU_IGNORE
                  | NETWORK
                  | PRIORITY
               )
            )
            | NAT
            | PIM
            | PORT_UNREACHABLE
            | PROXY_ARP
            | REDIRECTS
            | RIP
            | ROUTE_CACHE
            | ROUTER
            | TCP
            | UNNUMBERED
            | UNREACHABLES
            | VERIFY
            | VIRTUAL_REASSEMBLY
            | VIRTUAL_ROUTER
            | VRF
         )
      )
      |
      (
         IPV4
         (
            MTU
            | UNNUMBERED
            | UNREACHABLES
         )
      )
      | IPV6
      | ISDN
      |
      (
         ISIS
         (
            AUTHENTICATION
            | LSP_INTERVAL
         )
      )
      | KEEPALIVE
      | LANE
      | LAPB
      | LACP
      | LINK_FAULT_SIGNALING
      | LLDP
      | LOAD_BALANCING
      | LOAD_INTERVAL
      | LOGGING
      | LRE
      | MAC
      | MAC_ADDRESS
      | MACRO
      | MANAGEMENT_ONLY
      | MAP_GROUP
      | MDIX
      | MEDIA_TYPE
      | MEMBER
      | MINIMUM_LINKS
      | MLAG
      | MLS
      | MOBILITY
      | MOP
      | MPLS
      | MTU
      | NAME
      | NAMEIF
      | NEGOTIATE
      | NEGOTIATION
      | NMSP
      |
      (
         NTP
         (
            BROADCAST
            | DISABLE
         )
      )
      | OPENFLOW
      | PATH_OPTION
      | PEER
      | PFC PRIORITY
      | PHYSICAL_LAYER
      | PORT_CHANNEL
      | PORT_CHANNEL_PROTOCOL
      | PORT_NAME
      | PORTMODE
      | POS
      | POWER
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
      | ROUTE_CACHE
      | SECURITY_LEVEL
      | SERIAL
      | SERVICE_MODULE
      | SERVICE_POLICY
      | SIGNALLED_BANDWIDTH
      | SIGNALLED_NAME
      | SONET
      | SPANNING_TREE
      | SPEED
      | SNMP
      | SRR_QUEUE
      | STACK_MIB
      | STANDBY
      | STORM_CONTROL
      |
      (
         SWITCHPORT
         (
            EMPTY
            |
            (
               MODE PRIVATE_VLAN
            )
            | MONITOR
            | NONEGOTIATE
            | PORT_SECURITY
            | TAP
            | TOOL
            |
            (
               TRUNK GROUP
            )
            | VOICE
            | VLAN
         )
      )
      | TAG_SWITCHING
      | TAGGED
      | TAP
      | TCAM
      | TRUST
      | TUNNEL
      | TX_QUEUE
      | UC_TX_QUEUE
      | UDLD
      | UNTAGGED
      | VLT_PEER_LAG
      | VPC
      | VRRP
      | VRRP_GROUP
      | WEIGHTING
      | WRR_QUEUE
      | X25
      | XCONNECT
   ) ~NEWLINE* NEWLINE null_block_if_substanza*
;

null_block_if_substanza
:
   NO?
   (
      ADDRESS
      | PRIORITY
      | RECEIVE
      | TRANSMIT
      | VIRTUAL_ADDRESS
   ) ~NEWLINE* NEWLINE
;

preempt_hsrpc_stanza
:
   PREEMPT NEWLINE
;

priority_hsprc_stanza
:
   PRIORITY value = DEC NEWLINE
;

shutdown_if_stanza
:
   NO? SHUTDOWN FORCE? LAN? NEWLINE
;

switchport_access_if_stanza
:
   SWITCHPORT ACCESS VLAN
   (
      vlan = DEC
      | DYNAMIC
   ) NEWLINE
;

switchport_mode_stanza
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
      | TAP
      | TOOL
      | TRUNK
   ) NEWLINE
;

switchport_private_vlan_association_if_stanza
:
   SWITCHPORT PRIVATE_VLAN ASSOCIATION TRUNK primary_vlan_id = DEC
   secondary_vlan_id = DEC
;

switchport_private_vlan_mapping_if_stanza
:
   SWITCHPORT PRIVATE_VLAN MAPPING TRUNK? primary_vlan_id = DEC
   secondary_vlan_list = range
;

switchport_trunk_allowed_if_stanza
:
   SWITCHPORT TRUNK ALLOWED VLAN ADD? r = range NEWLINE
;

switchport_trunk_encapsulation_if_stanza
:
   SWITCHPORT TRUNK ENCAPSULATION e = switchport_trunk_encapsulation NEWLINE
;

switchport_trunk_native_if_stanza
:
   SWITCHPORT TRUNK NATIVE VLAN vlan = DEC NEWLINE
;

track_hsrpc_stanza
:
   TRACK ~NEWLINE* NEWLINE
;

vrf_forwarding_if_stanza
:
   VRF FORWARDING name = variable NEWLINE
;

vrf_if_stanza
:
   VRF name = variable NEWLINE
;

vrf_member_if_stanza
:
   VRF MEMBER name = variable NEWLINE
;
