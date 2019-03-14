parser grammar F5BigipImish_bgp;

import F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

rb_bgp_router_id
:
  BGP ROUTER_ID id = word NEWLINE
;

rb_neighbor
:
  NO? NEIGHBOR name = word
  (
    rbn_description
    | rbn_peer_group
    | rbn_peer_group_assign
    | rbn_null
    | rbn_remote_as
    | rbn_route_map_out
  )
;

rbn_description
:
  DESCRIPTION text = LINE NEWLINE
;

rbn_peer_group
:
  PEER_GROUP NEWLINE
;

rbn_peer_group_assign
:
  PEER_GROUP name = word NEWLINE
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
  REMOTE_AS remoteas = word NEWLINE
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
  REDISTRIBUTE KERNEL ROUTE_MAP rm = word NEWLINE
;

s_router_bgp
:
  ROUTER BGP localas = word NEWLINE
  (
    rb_bgp_router_id
    | rb_neighbor
    | rb_redistribute_kernel
    | rb_null
  )*
;
