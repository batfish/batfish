parser grammar CiscoNxos_ntp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ntp_poll_interval
:
// 4-16
  uint8
;

s_ntp
:
  NTP
  (
    ntp_access_group
    | ntp_authenticate
    | ntp_authentication_key
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

ntp_authenticate
:
  AUTHENTICATE NEWLINE
;

ntp_authentication_key
:
  AUTHENTICATION_KEY num = ntp_authentication_key_number MD5 md5 = md5_string md5_string_type? NEWLINE
;

md5_string_type
:
// 0 or 7
  uint8
;

ntp_authentication_key_number
:
// 1-65535
  uint16
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
  SERVER host = ntp_host
  (
    ntps_key
    | ntps_maxpoll
    | ntps_minpoll
    | ntps_prefer
    | ntps_use_vrf
  )* NEWLINE
;

ntps_key
:
  KEY ntp_authentication_key_number
;

ntps_maxpoll
:
  MAXPOLL ntp_poll_interval
;

ntps_minpoll
:
  MINPOLL ntp_poll_interval
;

ntps_prefer
:
  PREFER
;

ntps_use_vrf
:
  USE_VRF vrf = vrf_name
;

ntp_source_interface
:
  SOURCE_INTERFACE name = interface_name NEWLINE
;
