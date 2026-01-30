parser grammar Huawei_static;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Static route configuration (stub for Phase 1)

// Static route stanza
s_static_route
:
   IP ROUTE_STATIC
   (
      // ip route-static X.X.X.X Y.Y.Y.Y Z.Z.Z.Z
      dest = IP_ADDRESS mask = IP_ADDRESS next_hop = IP_ADDRESS
      |
      // ip route-static X.X.X.X/M Z.Z.Z.Z
      dest_prefix = IP_PREFIX next_hop = IP_ADDRESS
   )
   (
      // Optional parameters
      preference = dec
      |
      // Optional: track
      TRACK track_num = uint16
   )*
;
