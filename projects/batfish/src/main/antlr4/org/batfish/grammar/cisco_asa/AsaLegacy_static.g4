parser grammar AsaLegacy_static;

import Asa_common;

options {
   tokenVocab = AsaLexer;
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
   ) NEWLINE rs_route*
;

rs_route
:
   (
      prefix = IP_PREFIX
      | prefix6 = IPV6_PREFIX
   )
   (
      nhip = IP_ADDRESS
      | nhip6 = IPV6_ADDRESS
      | nhint = interface_name_unstructured
      (
         nhip = IP_ADDRESS
         | nhip6 = IPV6_ADDRESS
      )?
   )
   (
      (
         BFD FAST_DETECT
         (
            (
               MINIMUM_INTERVAL minimum_interval = dec
            )
            |
            (
               MULTIPLIER multiplier = dec
            )
         )*
      )
      | distance = dec
      |
      (
         DESCRIPTION description = RAW_TEXT
      )
      | PERMANENT
      |
      (
         TAG tag = dec
      )
      |
      (
         TRACK track = variable
      )
   )* NEWLINE
;

rs_vrf
:
   VRF name = variable NEWLINE
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
