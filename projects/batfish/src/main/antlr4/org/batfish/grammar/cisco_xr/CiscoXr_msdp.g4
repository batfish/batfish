parser grammar CiscoXr_msdp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

router_msdp: MSDP NEWLINE router_msdp_inner*;

router_msdp_inner
:
  rmsdp_global_null
  | rmsdp_nsr_delay_null
  | rmsdp_vrf
  | rmsdp_vrf_inner
;

rmsdp_vrf_inner
:
  rmsdp_cache_sa_holdtime_null
  | rmsdp_cache_sa_state
  | rmsdp_connect_source_null
  | rmsdp_default_peer_null
  | rmsdp_keepalive_null
  | rmsdp_maximum_null
  | rmsdp_originator_id_null
  | rmsdp_peer
  | rmsdp_sa_filter
  | rmsdp_ttl_threshold_null
;

rmsdp_peer_inner
:
  rmsdpp_connect_source_null
  | rmsdpp_description_null
  | rmsdpp_keepalive_null
  | rmsdpp_maximum_null
  | rmsdpp_mesh_group_null
  | rmsdpp_nsr_down_null
  | rmsdpp_password_null
  | rmsdpp_remote_as_null
  | rmsdpp_sa_filter
  | rmsdpp_shutdown_null
  | rmsdpp_ttl_threshold_null
;

rmsdp_global_null
:
   GLOBAL null_rest_of_line
;
rmsdp_nsr_delay_null
:
   NSR_DELAY null_rest_of_line
;

rmsdp_cache_sa_holdtime_null
:
   CACHE_SA_HOLDTIME null_rest_of_line
;
rmsdp_connect_source_null
:
   CONNECT_SOURCE null_rest_of_line
;
rmsdp_default_peer_null
:
   DEFAULT_PEER null_rest_of_line
;
rmsdp_keepalive_null
:
   KEEPALIVE null_rest_of_line
;
rmsdp_maximum_null
:
   MAXIMUM null_rest_of_line
;
rmsdp_originator_id_null
:
   ORIGINATOR_ID null_rest_of_line
;
rmsdp_ttl_threshold_null
:
   TTL_THRESHOLD null_rest_of_line
;

rmsdpp_connect_source_null
:
   CONNECT_SOURCE null_rest_of_line
;
rmsdpp_description_null
:
   DESCRIPTION null_rest_of_line
;
rmsdpp_keepalive_null
:
   KEEPALIVE null_rest_of_line
;
rmsdpp_maximum_null
:
   MAXIMUM null_rest_of_line
;
rmsdpp_mesh_group_null
:
   MESH_GROUP null_rest_of_line
;
rmsdpp_nsr_down_null
:
   NSR_DOWN null_rest_of_line
;
rmsdpp_password_null
:
   PASSWORD null_rest_of_line
;
rmsdpp_remote_as_null
:
   REMOTE_AS null_rest_of_line
;
rmsdpp_shutdown_null
:
   SHUTDOWN null_rest_of_line
;
rmsdpp_ttl_threshold_null
:
   TTL_THRESHOLD null_rest_of_line
;

rmsdp_cache_sa_state: CACHE_SA_STATE (LIST | RP_LIST) name = access_list_name NEWLINE;

rmsdp_peer: PEER IP_ADDRESS NEWLINE rmsdp_peer_inner*;

rmsdpp_sa_filter: SA_FILTER (IN | OUT) (LIST | RP_LIST) name = access_list_name NEWLINE;

rmsdp_sa_filter: SA_FILTER (IN | OUT) (LIST | RP_LIST) name = access_list_name NEWLINE;

rmsdp_vrf: VRF name = vrf_name NEWLINE rmsdp_vrf_inner*;