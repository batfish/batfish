parser grammar F5BigipImish_bgp;

import F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

rb_bgp_always_compare_med
:
  BGP ALWAYS_COMPARE_MED NEWLINE
;

rb_bgp_deterministic_med
:
  BGP DETERMINISTIC_MED NEWLINE
;

rb_bgp_router_id
:
  BGP ROUTER_ID id = IP_ADDRESS NEWLINE
;

rb_neighbor_ipv4
:
  NO? NEIGHBOR ip = IP_ADDRESS
  (
    rbn_common
    | rbn_peer_group_assign
  )
;

rb_neighbor_ipv6
:
  NO? NEIGHBOR ip6 = IPV6_ADDRESS
  (
    rbn_common
    | rbn_peer_group_assign
  )
;

rb_neighbor_peer_group
:
  NO? NEIGHBOR name = peer_group_name
  (
    rbn_common
    | rbn_peer_group
  )
;

rbn_common
:
  (
    rbn_description
    | rbn_next_hop_self
    | rbn_null
    | rbn_remote_as
    | rbn_route_map_out
  )
;

rbn_description
:
  DESCRIPTION text = DESCRIPTION_LINE NEWLINE
;

rbn_next_hop_self
:
  NEXT_HOP_SELF NEWLINE
;

rbn_peer_group
:
  PEER_GROUP NEWLINE
;

rbn_peer_group_assign
:
  PEER_GROUP name = peer_group_name NEWLINE
;

rbn_null
:
  (
    CAPABILITY
    | FALL_OVER
    | MAXIMUM_PREFIX
    | UPDATE_SOURCE
  ) null_rest_of_line
;

rbn_remote_as
:
  REMOTE_AS remoteas = uint32 NEWLINE
;

rbn_route_map_out
:
  ROUTE_MAP name = word OUT NEWLINE
;

rb_null
:
  NO?
  (
    (
      BGP GRACEFUL_RESTART
    )
    | MAX_PATHS
  ) null_rest_of_line
;

rb_redistribute_kernel
:
  REDISTRIBUTE KERNEL
  (
    ROUTE_MAP rm = word
  )? NEWLINE
;

s_router_bgp
:
  ROUTER BGP localas = uint32 NEWLINE
  (
    rb_bgp_always_compare_med
    | rb_bgp_deterministic_med
    | rb_bgp_router_id
    | rb_neighbor_ipv4
    | rb_neighbor_ipv6
    | rb_neighbor_peer_group
    | rb_redistribute_kernel
    | rb_null
  )*
;

peer_group_name
:
  ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
;
