parser grammar CiscoNxos_ntp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_ntp
:
  NTP
  (
    ntp_access_group
    | ntp_commit
    | ntp_distribute
    | ntp_peer
    | ntp_server
    | ntp_source_interface
  )
;

ntp_access_group
:
  ACCESS_GROUP
  (
    ntpag_match_all
    | ntpag_peer
    | ntpag_query_only
    | ntpag_serve
    | ntpag_serve_only
  )
;

ntpag_match_all
:
  MATCH_ALL NEWLINE
;

ntpag_peer
:
  PEER name = ip_access_list_name NEWLINE
;

ntpag_query_only
:
  QUERY_ONLY name = ip_access_list_name NEWLINE
;

ntpag_serve
:
  SERVE name = ip_access_list_name NEWLINE
;

ntpag_serve_only
:
  SERVE_ONLY name = ip_access_list_name NEWLINE
;

ntp_commit
:
  COMMIT NEWLINE
;

ntp_distribute
:
  DISTRIBUTE NEWLINE
;

ntp_peer
:
  PEER host = ntp_host NEWLINE
;

ntp_host
:
  ip_address
  | ipv6_address
  | WORD
;

ntp_server
:
  SERVER host = ntp_host (USE_VRF vrf = vrf_name)? NEWLINE
;

ntp_source_interface
:
  SOURCE_INTERFACE name = interface_name NEWLINE
;
