parser grammar CiscoXr_static;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

rs_address_family
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      UNICAST
      | MULTICAST
   ) NEWLINE (rs_route | rs_no_route)*
;

route_prefix: (prefix = IP_PREFIX | prefix6 = IPV6_PREFIX);

route_nexthop
:
   nhip = IP_ADDRESS
   | nhip6 = IPV6_ADDRESS
   | nhint = interface_name_unstructured
   (
      nhip = IP_ADDRESS
      | nhip6 = IPV6_ADDRESS
   )?
;

route_properties
:
   (
      (
         BFD FAST_DETECT
         (
            (
               MINIMUM_INTERVAL minimum_interval = uint_legacy
            )
            |
            (
               MULTIPLIER multiplier = uint_legacy
            )
         )*
      )
      | distance = uint_legacy
      |
      (
         DESCRIPTION description = RAW_TEXT
      )
      | PERMANENT
      |
      (
         TAG tag = uint_legacy
      )
      |
      (
         TRACK track = variable
      )
   )*
;

rs_no_route: NO route_prefix (route_nexthop route_properties)? NEWLINE;

rs_route: route_prefix route_nexthop route_properties NEWLINE;

rs_vrf
:
   VRF name = vrf_name NEWLINE
   (
      rs_address_family
      | rs_route
   )*
;

s_router_static
:
   ROUTER STATIC NEWLINE
   (
      rs_address_family
      | rs_route
      | rs_vrf
   )*
;
