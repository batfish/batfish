parser grammar Ftd_bgp;

import Ftd_common;

options {
   tokenVocab = FtdLexer;
}

router_bgp_stanza
:
   ROUTER BGP asn=dec NEWLINE
   bgp_stanza_tail*
;

bgp_stanza_tail
:
   (
      bgp_log_neighbor_changes_null
      | bgp_router_id
      | bgp_neighbor
      | bgp_address_family
      | stanza_unrecognized_line
   )
;

bgp_log_neighbor_changes_null
:
    BGP LOG_NEIGHBOR_CHANGES NEWLINE
;

bgp_router_id
:
   BGP ROUTER_ID id=IP_ADDRESS NEWLINE
;

bgp_neighbor
:
    NEIGHBOR ip=IP_ADDRESS
    (
        REMOTE_AS remote_as=dec
        | DESCRIPTION description=~NEWLINE+
        | ACTIVATE
        | bgp_neighbor_timers
        | bgp_neighbor_route_map
    ) NEWLINE
;

bgp_neighbor_timers
:
    TIMERS KEEPALIVE keepalive=dec HOLDTIME holdtime=dec
;

bgp_neighbor_route_map
:
    ROUTE_MAP
    (
        map_name=route_map_name_null (IN | OUT)
        | (IN | OUT) map_name=route_map_name_null
    )
;

route_map_name_null
:
    ~(
        IN
        | OUT
        | NEWLINE
    )+
;

bgp_address_family
:
   ADDRESS_FAMILY IPV4 (UNICAST)? NEWLINE
   bgp_address_family_tail*
   EXIT_ADDRESS_FAMILY NEWLINE
;

bgp_address_family_tail
:
    (
        bgp_neighbor
        | bgp_network
        | stanza_unrecognized_line
    )
;

bgp_network
:
    NETWORK ip=IP_ADDRESS (MASK mask=IP_ADDRESS)? NEWLINE
;
