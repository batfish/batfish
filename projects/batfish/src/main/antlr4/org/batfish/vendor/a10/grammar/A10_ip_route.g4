parser grammar A10_ip_route;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

si_route: ROUTE ip_prefix static_route_definition;

// Same syntax is used for `no ip route` as for `ip route`
sni_route: ROUTE ip_prefix static_route_definition;

static_route_definition
:
   ip_address
   (
      static_route_description static_route_distance?
      | static_route_distance static_route_description?
   )? NEWLINE
;

static_route_description: DESCRIPTION route_description;

// 1-255
static_route_distance: uint8;
