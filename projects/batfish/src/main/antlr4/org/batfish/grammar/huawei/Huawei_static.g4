parser grammar Huawei_static;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Static route configuration

// Static route stanza
s_static_route
:
   IP ROUTE_STATIC static_route_body
;

// Static route body
static_route_body
:
   (
      // With VRF: ip route-static vpn-instance <vrf> <dest> <mask> <next-hop>
      VPN_INSTANCE vrf = variable
   )?
   (
      // CIDR notation: ip route-static 10.0.0.0/24 192.168.1.1
      dest_prefix = ip_prefix next_hop = ip_address
      |
      // Traditional notation: ip route-static 10.0.0.0 255.255.255.0 192.168.1.1
      dest_addr = ip_address dest_mask = ip_address next_hop = ip_address
      |
      // With interface: ip route-static 10.0.0.0 255.255.255.0 GigabitEthernet0/0/0 192.168.1.1
      dest_addr2 = ip_address dest_mask2 = ip_address
      out_if = interface_name
      next_hop2 = ip_address
   )
   (
      // Optional preference: preference <value>
      PREFERENCE pref = uint16
   )?
   (
      // Optional track: track <number>
      TRACK track_num = uint16
   )?
   (
      // Other optional parameters (ignore)
      null_rest_of_line
   )?
;
