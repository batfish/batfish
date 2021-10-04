parser grammar A10_ip_nat;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

si_nat: NAT sin;

sin: sin_pool;

sin_pool: POOL nat_pool_name start = ip_address end = ip_address NETMASK ip_netmask sinp_properties* NEWLINE;

sinp_properties
:
   (
      sinpp_gateway
      | sinpp_ip_rr
      | sinpp_port_overload
      | sinpp_scaleout_device_id
      | sinpp_vrid
   )
;

sinpp_gateway: GATEWAY gateway = ip_address;

sinpp_ip_rr: IP_RR;

sinpp_port_overload: PORT_OVERLOAD;

sinpp_scaleout_device_id: SCALEOUT_DEVICE_ID scaleout_device_id;

sinpp_vrid: VRID vrid;

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
