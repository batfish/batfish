parser grammar NecIx_bgp;

options {
  tokenVocab = NecIxLexer;
}

// `router bgp` opens BGP mode. Inside it, `address-family ipv4 unicast` opens address-family mode
// and `vrf <name>` opens BGP-VRF mode (which has its own address-family mode). The extractor
// tracks which of these the current line belongs to.
s_router_bgp
:
  ROUTER BGP asn = uint NEWLINE rb_inner*
;

rb_inner
:
  INDENT
  (
    rb_address_family
    | rb_default_local_preference
    | rb_neighbor
    | rb_network
    | rb_no
    | rb_null
    | rb_originate_default
    | rb_redistribute
    | rb_router_id
    | rb_vrf
  )
;

rb_address_family
:
  ADDRESS_FAMILY family = ( IPV4 | IPV6 ) UNICAST? NEWLINE
;

rb_default_local_preference
:
  DEFAULT_LOCAL_PREFERENCE value = uint NEWLINE
;

rb_neighbor
:
  NEIGHBOR ip = IP_ADDRESS
  (
    rbn_ebgp_multihop
    | rbn_next_hop_self
    | rbn_null
    | rbn_remote_as
    | rbn_route_map
    | rbn_send_default
    | rbn_shutdown
    | rbn_update_source
  )
;

rbn_ebgp_multihop
:
  EBGP_MULTIHOP ttl = uint? NEWLINE
;

rbn_next_hop_self
:
  NEXT_HOP_SELF NEWLINE
;

// Session timers and the like.
rbn_null
:
  (
    ADVERTISEMENT_INTERVAL
    | CONNECT_INTERVAL
    | DESCRIPTION
    | PASSIVE
    | PASSWORD
    | SEND_CAPABILITY
    | TIMERS
  ) null_rest_of_line
;

rbn_remote_as
:
  REMOTE_AS asn = uint NEWLINE
;

rbn_route_map
:
  ROUTE_MAP name = variable direction = ( IN | OUT ) NEWLINE
;

rbn_send_default
:
  SEND_DEFAULT NEWLINE
;

rbn_shutdown
:
  SHUTDOWN NEWLINE
;

rbn_update_source
:
  UPDATE_SOURCE iface = variable NEWLINE
;

rb_network
:
  NETWORK prefix = IP_PREFIX NEWLINE
;

rb_no
:
  NO
  (
    rbno_send_default
  )
;

rbno_send_default
:
  NEIGHBOR ip = IP_ADDRESS SEND_DEFAULT NEWLINE
;

rb_null
:
  (
    DEFAULT_METRIC // MED of redistributed routes
    | DISTANCE
    | MULTIPATH
    | TIMERS
  ) null_rest_of_line
;

rb_originate_default
:
  ORIGINATE_DEFAULT ALWAYS? NEWLINE
;

rb_redistribute
:
  REDISTRIBUTE protocol = ( CONNECTED | STATIC )
  (
    METRIC metric = uint
    | ROUTE_MAP route_map = variable
  )* NEWLINE
;

rb_router_id
:
  ROUTER_ID id = IP_ADDRESS NEWLINE
;

rb_vrf
:
  VRF name = variable NEWLINE
;
