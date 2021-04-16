parser grammar CiscoXr_msdp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

router_msdp: MSDP NEWLINE router_msdp_inner*;

router_msdp_inner
:
  rmsdp_null
  | rmsdp_vrf
  | rmsdp_vrf_inner
;

rmsdp_vrf_inner
:
  rmsdp_cache_sa_state
  | rmsdp_peer
  | rmsdp_sa_filter
  | rmsdpv_null
;

rmsdp_peer_inner
:
  rmsdpp_null
  | rmsdpp_sa_filter
;

rmsdp_null
:
  (
    GLOBAL
    | NSR_DELAY
  ) null_rest_of_line
;

rmsdpv_null
:
  (
    CACHE_SA_HOLDTIME
    | CONNECT_SOURCE
    | DEFAULT_PEER
    | KEEPALIVE
    | MAXIMUM
    | ORIGINATOR_ID
    | TTL_THRESHOLD
  ) null_rest_of_line
;

rmsdpp_null
:
  (
    CONNECT_SOURCE
    | DESCRIPTION
    | KEEPALIVE
    | MAXIMUM
    | MESH_GROUP
    | NSR_DOWN
    | PASSWORD
    | REMOTE_AS
    | SHUTDOWN
    | TTL_THRESHOLD
  ) null_rest_of_line
;

rmsdp_cache_sa_state: CACHE_SA_STATE (LIST | RP_LIST) name = access_list_name NEWLINE;

rmsdp_peer: PEER IP_ADDRESS NEWLINE rmsdp_peer_inner*;

rmsdpp_sa_filter: SA_FILTER (IN | OUT) (LIST | RP_LIST) name = access_list_name NEWLINE;

rmsdp_sa_filter: SA_FILTER (IN | OUT) (LIST | RP_LIST) name = access_list_name NEWLINE;

rmsdp_vrf: VRF name = vrf_name NEWLINE rmsdp_vrf_inner*;