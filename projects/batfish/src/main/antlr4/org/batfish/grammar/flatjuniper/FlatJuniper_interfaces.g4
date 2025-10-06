parser grammar FlatJuniper_interfaces;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

direction
:
   INPUT
   | INPUT_LIST
   | OUTPUT
   | OUTPUT_LIST
;

eo_802_3ad
:
   EIGHT02_3AD
   (
      eo8023ad_interface
      | eo8023ad_lacp
   )
;

eo_auto_negotiation
:
   AUTO_NEGOTIATION
;

eo_no_auto_negotiation
:
   NO_AUTO_NEGOTIATION
;

eo_null
:
   (
      AUTO_NEGOTIATION
      | FLOW_CONTROL
      | IGNORE_L3_INCOMPLETES
      | NO_AUTO_NEGOTIATION
      | NO_FLOW_CONTROL
      | LINK_MODE
      | LOOPBACK
   ) null_filler
;

eo_redundant_parent
:
   REDUNDANT_PARENT name = interface_id
;

eo_speed
:
   SPEED dec speed_abbreviation
;

eo8023ad_interface
:
   interface_id
;

eo8023ad_lacp
:
   LACP FORCE_UP
;

ether_options
:
   eo_802_3ad
   | eo_null
   | eo_redundant_parent
   | eo_speed
;

filter
:
   FILTER
   (
      direction name = filter_name
   )?
;

i_arp_resp
:
   ARP_RESP
;

i_bandwidth
:
    BANDWIDTH bandwidth
;

i_common
:
   apply
   | i_arp_resp
   | i_description
   | i_common_physical
   | i_disable
   | i_enable
   | i_family
   | i_null
   | i_vlan_id
   | i_vlan_id_list
   | i_vlan_tagging
;

// configuration relevant for physical interfaces; there can be overlap with non-physical ones
i_common_physical
:
    apply
    | i_damping
    | i_description
    | i_disable
    | i_ether_options
    | i_fastether_options
    | i_gigether_options
    | i_mac
    | i_mtu
    | i_null
    | i_redundant_ether_options
    | i_speed
;

i_damping
:
   DAMPING
   (
      id_enable_null
      | id_half_life_null
      | id_max_suppress_null
      | id_reuse_null
      | id_suppress_null
   )
;

id_enable_null
:
   ENABLE
;

id_half_life_null
:
  // range 1 through 30
   HALF_LIFE uint8
;

id_max_suppress_null
:
  // range 1 through 20,000
   MAX_SUPPRESS uint16
;

id_reuse_null
:
  // range 1 through 20,000
   REUSE uint16
;

id_suppress_null
:
  // range 1 through 20,000
   SUPPRESS uint16
;

i_description
:
   description
;

i_disable
:
   DISABLE
;

i_enable
:
   ENABLE
;

i_ether_options
:
   ETHER_OPTIONS ether_options
;

i_fastether_options
:
   FASTETHER_OPTIONS ether_options
;

i_family
:
   FAMILY
   (
      if_bridge
      | if_ccc
      | if_ethernet_switching
      | if_inet
      | if_inet6
      | if_iso
      | if_primary
      | if_mpls
   )
;

i_flexible_vlan_tagging
:
   FLEXIBLE_VLAN_TAGGING
;

i_gigether_options
:
   GIGETHER_OPTIONS ether_options
;

i_input_vlan_map
:
   INPUT_VLAN_MAP i_vlan_action
;

i_link_mode
:
   LINK_MODE FULL_DUPLEX
;

i_mac
:
   MAC mac = MAC_ADDRESS
;

i_mtu
:
// maximum is chassis-dependent, but it ranges from 1 to about 16KB it looks like:
// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/mtu-edit-interfaces-ni.html
   MTU bytes = uint16
;

i_native_vlan_id
:
   NATIVE_VLAN_ID id = dec
;

i_null
:
   (
      AGGREGATED_ETHER_OPTIONS
      | ENCAPSULATION
      | FABRIC_OPTIONS
      | FORWARDING_CLASS_ACCOUNTING
      | FRAMING
      | HOLD_TIME
      | INTERFACE_TRANSMIT_STATISTICS
      | MULTISERVICE_OPTIONS
      | NO_TRAPS
      | PROXY_MACIP_ADVERTISEMENT
      | REDUNDANT_ETHER_OPTIONS
      | SONET_OPTIONS
      | TRACEOPTIONS
      | TRAPS
      | TUNNEL
   ) null_filler
;

i_output_vlan_map
:
   OUTPUT_VLAN_MAP i_vlan_action
;

i_peer_unit
:
   PEER_UNIT unit = dec
;

i_per_unit_scheduler
:
   PER_UNIT_SCHEDULER
;

i_redundant_ether_options
:
  // TODO: should this just accept a number?
   REDUNDANCY_GROUP name = junos_name
;

i_speed
:
   SPEED dec speed_abbreviation
;

i_unit
:
   UNIT
   (
      wildcard
      | num = dec
   )
   (
      i_common
      | i_bandwidth
      | i_input_vlan_map
      | i_output_vlan_map
      | i_peer_unit
   )
;

i_vlan_action
:
   POP | POP_POP | POP_SWAP | PUSH | PUSH_PUSH | SWAP | SWAP_PUSH | SWAP_SWAP
;

i_vlan_id
:
   VLAN_ID id = dec
;

i_vlan_id_list
:
   VLAN_ID_LIST vlan_range
;

i_vlan_tagging
:
   VLAN_TAGGING
;

if_bridge
:
   BRIDGE
   (
      apply
      | if_storm_control
      | ifbr_filter
      | ifbr_interface_mode
      | ifbr_vlan_id_list
   )
;

if_ccc
:
   CCC null_filler
;

if_ethernet_switching
:
   ETHERNET_SWITCHING
   (
      apply
      | if_storm_control
      | ife_filter
      | ife_interface_mode
      | ife_native_vlan_id
      | ife_port_mode
      | ife_recovery_timeout_null
      | ife_vlan
   )
;

ife_recovery_timeout_null
:
   RECOVERY_TIMEOUT uint16 null_filler
;

if_inet
:
   INET
   (
      apply
      | ifi_address
      | ifi_destination_udp_port
      | ifi_filter
      | ifi_mtu
      | ifi_no_redirects
      | ifi_null
      | ifi_rpf_check
      | ifi_tcp_mss
   )
;

if_inet6
:
   INET6
   (
      apply
      | ifi6_address
      | ifi6_destination_udp_port
      | ifi6_filter
      | ifi6_mtu
   )
;

ifi6_address
:
   ADDRESS
   (
      ipv6_address
      | ipv6_prefix
      | wildcard
   )
   (
      ifi6a_preferred
      | ifi6a_primary
   )?
;

ifi6a_preferred: PREFERRED;
ifi6a_primary: PRIMARY;

ifi6_destination_udp_port: DESTINATION_UDP_PORT port_number;

ifi6_filter: FILTER ifi6f_input;

ifi6f_input: INPUT name = junos_name;

ifi6_mtu: i_mtu;

if_iso
:
   ISO
   (
      apply
      | ifiso_address
      | ifiso_destination_udp_port
      | ifiso_mtu
   )
;

if_mpls
:
   MPLS
   (
      apply
      | ifm_destination_udp_port
      | ifm_filter
      | ifm_maximum_labels
      | ifm_mtu
   )
;

if_primary
:
   PRIMARY
;

if_storm_control
:
    STORM_CONTROL null_filler
;

ifbr_filter
:
   filter
;

ifbr_interface_mode
:
   INTERFACE_MODE interface_mode
;

ifbr_vlan_id_list
:
   VLAN_ID_LIST vlan_range
;

ife_filter
:
   filter
;

ife_interface_mode
:
   INTERFACE_MODE
   (
      ACCESS
      | TRUNK
   )
;

ife_native_vlan_id
:
   NATIVE_VLAN_ID id = dec
;

ife_port_mode
:
   PORT_MODE
   (
      ACCESS
      | TRUNK
   )
;

ife_vlan
:
   VLAN MEMBERS
   (
      ALL
      | DEFAULT
      | range
      | name = junos_name
   )
;

ifi_address
:
   ADDRESS
   (
      ip_address
      | ip_prefix
      | wildcard
   )
   (
      ifia_arp
      | ifia_master_only
      | ifia_preferred
      | ifia_primary
      | ifia_vrrp_group
      | ifia_virtual_gateway_address
   )?
;

ifi_filter
:
   filter
;

ifi_destination_udp_port: DESTINATION_UDP_PORT port_number;

ifi_mtu
:
   i_mtu
;

ifi_no_redirects
:
   NO_REDIRECTS
;

ifi_null
:
   (
      DHCP
      | POLICER
      | SAMPLING
      | SERVICE
      | TARGETED_BROADCAST
   ) null_filler
;

ifi_rpf_check
:
   RPF_CHECK FAIL_FILTER name = junos_name
;

ifi_tcp_mss
:
  TCP_MSS size = dec
;

ifia_arp
:
   ARP ip = ip_address
   (
      L2_INTERFACE interface_id
   )?
   (
      MAC
      | MULTICAST_MAC
   ) MAC_ADDRESS
;

ifia_master_only
:
   MASTER_ONLY
;

ifia_preferred
:
   PREFERRED
;

ifia_primary
:
   PRIMARY
;

ifia_virtual_gateway_address
:
   VIRTUAL_GATEWAY_ADDRESS ip = ip_address
;

ifia_vrrp_group
:
   VRRP_GROUP (wildcard | number = dec)
   (
      apply
      | ifiav_accept_data_null
      | ifiav_advertise_interval_null
      | ifiav_authentication_key_null
      | ifiav_authentication_type_null
      | ifiav_fast_interval_null
      | ifiav_no_preempt
      | ifiav_preempt
      | ifiav_priority
      | ifiav_track
      | ifiav_virtual_address
   )
;

ifiav_accept_data_null
:
   ACCEPT_DATA
;

ifiav_advertise_interval_null
:
   ADVERTISE_INTERVAL dec
;

ifiav_authentication_key_null
:
   AUTHENTICATION_KEY secret_string
;

ifiav_authentication_type_null
:
   AUTHENTICATION_TYPE
   (
      MD5
      | SIMPLE
   )
;

ifiav_fast_interval_null
:
   FAST_INTERVAL dec
;

ifiav_no_preempt
:
   NO_PREEMPT
;

ifiav_preempt
:
   PREEMPT (HOLD_TIME dec)?
;

ifiav_priority
:
   PRIORITY priority = dec
;

ifiav_track
:
   TRACK
   (
      ifiavt_interface
      | ifiavt_route
   )
;

ifiav_virtual_address
:
   VIRTUAL_ADDRESS ip_address
;

ifiavt_interface
:
   INTERFACE interface_id
   (
      ifiavti_priority_cost
   )
;

ifiavt_route
:
   ROUTE ip_prefix ROUTING_INSTANCE ri = junos_name PRIORITY_COST dec
;

ifiavti_priority_cost
:
   PRIORITY_COST cost = dec
;

ifiso_address
:
   ADDRESS iso_address
;

ifiso_destination_udp_port: DESTINATION_UDP_PORT port_number;

ifiso_mtu
:
   MTU dec
;

ifm_destination_udp_port: DESTINATION_UDP_PORT port_number;

ifm_filter
:
   filter
;

ifm_maximum_labels
:
   MAXIMUM_LABELS num = dec
;

ifm_mtu
:
   MTU dec
;

int_interface_range
:
   INTERFACE_RANGE irange = junos_name
   (
       i_common_physical
       | intir_member
       | intir_member_range
   )
;

int_named
:
   (
      wildcard
      | interface_id
   )
   (
      i_common
      | i_flexible_vlan_tagging
      | i_link_mode
      | i_native_vlan_id
      | i_per_unit_scheduler
      | i_unit
   )
;

int_null
:
   (
      TRACEOPTIONS
   ) null_filler
;

interface_mode
:
   TRUNK
;

intir_member
:
   MEMBER
   (
       DOUBLE_QUOTED_STRING
       | interface_id
   )
;

intir_member_range
:
   MEMBER_RANGE from_i = interface_id TO to_i = interface_id
;

s_interfaces
:
   INTERFACES
   (
      apply
      | int_interface_range
      | int_named
      | int_null
   )
;

speed_abbreviation
:
   G
   | M
;
