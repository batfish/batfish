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
   REDUNDANT_PARENT name = variable
;

eo_speed
:
   SPEED DEC speed_abbreviation
;

eo8023ad_interface
:
   (
      node = variable COLON
   )? name = variable
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
      direction name = variable
   )?
;

i_apply_groups
:
   apply_groups
;

i_apply_groups_except
:
   apply_groups_except
;

i_arp_resp
:
   ARP_RESP
;

i_common
:
   apply
   | i_arp_resp
   | i_description
   | i_disable
   | i_enable
   | i_ether_options
   | i_fastether_options
   | i_gigether_options
   | i_family
   | i_mtu
   | i_null
   | i_redundant_ether_options
   | i_speed
   | i_vlan_id
   | i_vlan_id_list
   | i_vlan_tagging
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

i_link_mode
:
   LINK_MODE FULL_DUPLEX
;

i_mtu
:
   MTU size = DEC
;

i_native_vlan_id
:
   NATIVE_VLAN_ID id = DEC
;

i_null
:
   (
      AGGREGATED_ETHER_OPTIONS
      | BANDWIDTH
      | ENCAPSULATION
      | FABRIC_OPTIONS
      | FORWARDING_CLASS_ACCOUNTING
      | FRAMING
      | HOLD_TIME
      | INTERFACE_TRANSMIT_STATISTICS
      | MULTISERVICE_OPTIONS
      | NO_TRAPS
      | REDUNDANT_ETHER_OPTIONS
      | SONET_OPTIONS
      | TRACEOPTIONS
      | TRAPS
      | TUNNEL
   ) null_filler
;

i_peer_unit
:
   PEER_UNIT unit = DEC
;

i_per_unit_scheduler
:
   PER_UNIT_SCHEDULER
;

i_redundant_ether_options
:
   REDUNDANCY_GROUP name = variable
;

i_speed
:
   SPEED DEC speed_abbreviation
;

i_unit
:
   UNIT
   (
      wildcard
      | num = DEC
   )
   (
      i_common
      | i_peer_unit
   )
;

i_vlan_id
:
   VLAN_ID id = DEC
;

i_vlan_id_list
:
   VLAN_ID_LIST subrange
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
      | ife_vlan
   )
;

if_inet
:
   INET
   (
      apply
      | ifi_address
      | ifi_filter
      | ifi_mtu
      | ifi_no_redirects
      | ifi_null
      | ifi_rpf_check
   )
;

if_inet6
:
   INET6 null_filler
;

if_iso
:
   ISO
   (
      apply
      | ifiso_address
      | ifiso_mtu
   )
;

if_mpls
:
   MPLS
   (
      apply
      | ifm_filter
      | ifm_maximum_labels
      | ifm_mtu
   )
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
   VLAN_ID_LIST DEC
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
   NATIVE_VLAN_ID name = variable
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
      | range
      | name = variable
   )
;

ifi_address
:
   ADDRESS
   (
      IP_ADDRESS
      | IP_PREFIX
      | wildcard
   )
   (
      ifia_arp
      | ifia_master_only
      | ifia_preferred
      | ifia_primary
      | ifia_vrrp_group
   )?
;

ifi_filter
:
   filter
;

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
   RPF_CHECK FAIL_FILTER name = variable
;

ifia_arp
:
   ARP ip = IP_ADDRESS
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

ifia_vrrp_group
:
   VRRP_GROUP
   (
      number = DEC
      | name = variable
   )
   (
      ifiav_accept_data
      | ifiav_advertise_interval
      | ifiav_authentication_key
      | ifiav_authentication_type
      | ifiav_preempt
      | ifiav_priority
      | ifiav_track
      | ifiav_virtual_address
   )
;

ifiav_accept_data
:
   ACCEPT_DATA
;

ifiav_advertise_interval
:
   ADVERTISE_INTERVAL DEC
;

ifiav_authentication_key
:
   AUTHENTICATION_KEY string
;

ifiav_authentication_type
:
   AUTHENTICATION_TYPE
   (
      MD5
      | SIMPLE
   )
;

ifiav_preempt
:
   PREEMPT
;

ifiav_priority
:
   PRIORITY priority = DEC
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
   VIRTUAL_ADDRESS IP_ADDRESS
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
   ROUTE IP_PREFIX ROUTING_INSTANCE variable PRIORITY_COST DEC
;

ifiavti_priority_cost
:
   PRIORITY_COST cost = DEC
;

ifiso_address
:
   ADDRESS ISO_ADDRESS
;

ifiso_mtu
:
   MTU DEC
;

ifm_filter
:
   filter
;

ifm_maximum_labels
:
   MAXIMUM_LABELS num = DEC
;

ifm_mtu
:
   MTU DEC
;

int_interface_range
:
   INTERFACE_RANGE irange = variable MEMBER member = DOUBLE_QUOTED_STRING
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
