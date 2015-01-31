parser grammar Cisco_interface;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

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
   description_if_stanza
   | ip_access_group_if_stanza
   | ip_address_if_stanza
   | ip_address_secondary_if_stanza
   | ip_ospf_cost_if_stanza
   | ip_ospf_dead_interval_if_stanza
   | ip_ospf_dead_interval_minimal_if_stanza
   | ip_ospf_passive_interface_if_stanza
   | ip_policy_if_stanza
   | isis_circuit_type_if_stanza
   | isis_metric_if_stanza
   | isis_network_if_stanza
   | isis_tag_if_stanza
   | no_ip_address_if_stanza
   | null_if_stanza
   | shutdown_if_stanza
   | switchport_access_if_stanza
   | switchport_trunk_native_if_stanza
   | switchport_trunk_encapsulation_if_stanza
   | switchport_trunk_allowed_if_stanza
   | switchport_mode_access_stanza
   | switchport_mode_dynamic_auto_stanza
   | switchport_mode_dynamic_desirable_stanza
   | switchport_mode_trunk_stanza
   | vrf_forwarding_if_stanza
   | vrf_member_if_stanza
;

interface_stanza
:
   INTERFACE iname = interface_name
   (
      MULTIPOINT
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
   IP PORT? ACCESS_GROUP name = .
   (
      IN
      | OUT
   ) NEWLINE
;

ip_address_hsrpc_stanza
:
   IP ip = IP_ADDRESS NEWLINE
;

ip_address_if_stanza
:
   IP ADDRESS
   (
      (
         ip = IP_ADDRESS subnet = IP_ADDRESS
      )
      | prefix = IP_PREFIX
   )
   (
      STANDBY IP_ADDRESS
   )? NEWLINE
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

ip_ospf_passive_interface_if_stanza
:
   NO? IP OSPF PASSIVE_INTERFACE NEWLINE
;

ip_policy_if_stanza
:
   IP POLICY ROUTE_MAP name = ~NEWLINE NEWLINE
;

isis_circuit_type_if_stanza
:
   ISIS CIRCUIT_TYPE LEVEL_2_ONLY NEWLINE
;

isis_metric_if_stanza
:
   ISIS METRIC metric = DEC NEWLINE
;

isis_network_if_stanza
:
   ISIS NETWORK POINT_TO_POINT NEWLINE
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
   | null_standalone_if_stanza
;

null_standalone_if_stanza
:
   NO?
   (
      ARP
      | ASYNC
      | ATM
      | AUTO
      | AUTOSTATE
      | BANDWIDTH
      | BEACON
      | CABLELENGTH
      | CDP
      | CHANNEL
      | CHANNEL_GROUP
      | CHANNEL_PROTOCOL
      | CLASS
      | CLNS
      | CLOCK
      | COUNTER
      | CRYPTO
      | DCBX
      |
      (
         DSU BANDWIDTH
      )
      | DUPLEX
      | ENCAPSULATION
      | FAIR_QUEUE
      | FLOWCONTROL
      | FORWARDER
      | FRAME_RELAY
      | FRAMING
      | FULL_DUPLEX
      | GLBP
      | GROUP_RANGE
      | HALF_DUPLEX
      | HOLD_QUEUE
      |
      (
         HSRP VERSION
      )
      | IGNORE
      |
      (
         IP
         (
            ACCOUNTING
            | ARP
            | BROADCAST_ADDRESS
            | CGMP
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
      | MLAG
      | MLS
      | MOBILITY
      | MOP
      | MPLS
      | MTU
      | NAMEIF
      | NEGOTIATE
      | NEGOTIATION
      |
      (
         NTP BROADCAST
      )
      | PEER
      | PHYSICAL_LAYER
      | PORT_CHANNEL
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
            |
            (
               TRUNK GROUP
            )
            | VOICE
            | VLAN
         )
      )
      | TAG_SWITCHING
      | TCAM
      | TRUST
      | TUNNEL
      | TX_QUEUE
      | UC_TX_QUEUE
      | UDLD
      | VPC
      | VRRP
      | WEIGHTING
      | WRR_QUEUE
      | X25
      | XCONNECT
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
   SWITCHPORT ACCESS VLAN vlan = DEC NEWLINE
;

switchport_mode_access_stanza
:
   SWITCHPORT MODE ACCESS NEWLINE
;

switchport_mode_dynamic_auto_stanza
:
   SWITCHPORT MODE DYNAMIC AUTO NEWLINE
;

switchport_mode_dynamic_desirable_stanza
:
   SWITCHPORT MODE DYNAMIC DESIRABLE NEWLINE
;

switchport_mode_trunk_stanza
:
   SWITCHPORT MODE TRUNK NEWLINE
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
   VRF FORWARDING name = ~NEWLINE NEWLINE
;

vrf_member_if_stanza
:
   VRF MEMBER name = ~NEWLINE NEWLINE
;
