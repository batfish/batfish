parser grammar Rtx_route;

// `ip route <network> gateway <gw> [<param>...] [gateway <gw> [<param>...]]...`
ip_route
:
  ROUTE network = route_network gateways += ipr_gateway+ NEWLINE
;

route_network
:
  DEFAULT
  | IPV4_PREFIX
  | IPV4_ADDRESS
;

ipr_gateway
:
  GATEWAY route_gateway route_gateway_param*
;

route_gateway
:
  ip = IPV4_ADDRESS
  | TUNNEL num = UINT
;

// Parsed but not modeled. `filter` selects a gateway per-flow and `metric`/`weight`/`keepalive`
// affect route preference; none is used by the J57 IPv4 routes.
route_gateway_param
:
  FILTER UINT+
  | HIDE
  | KEEPALIVE UINT
  | METRIC UINT
  | WEIGHT UINT
;

// `ip tunnel mtu|tcp|secure filter ...`. The lexer swallowed the rest of the line.
ip_tunnel_null
:
  TUNNEL IGNORED_TEXT* NEWLINE
;
