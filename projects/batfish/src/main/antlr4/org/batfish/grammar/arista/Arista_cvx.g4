parser grammar Arista_cvx;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

s_cvx
:
  CVX NEWLINE
  (
    cvx_no
    | cvx_null
    | cvx_service
  )*
;

cvx_no
:
  NO cvx_no_null
;

cvx_no_null
:
  (
    PEER
    | SHUTDOWN
    | SOURCE_INTERFACE
    | SSL
  ) null_rest_of_line
;

cvx_null
:
  (
    HEARTBEAT_INTERVAL
    | HEARTBEAT_TIMEOUT
    | PEER
    | PORT
    | SHUTDOWN
  ) null_rest_of_line
;

cvx_service
:
  SERVICE
  (
    cvx_s_bug_alert
    | cvx_s_debug
    | cvx_s_hsc
    | cvx_s_mss
    | cvx_s_openstack
    | cvx_s_vxlan
  )
;

cvx_s_bug_alert
:
  BUG_ALERT NEWLINE
  (
    cvx_sba_no
    | cvx_sba_shutdown
  )
;

cvx_sba_no
:
  NO SHUTDOWN NEWLINE
;

cvx_sba_shutdown
:
  SHUTDOWN NEWLINE
;

cvx_s_debug
:
  DEBUG NEWLINE
  (
    cvx_sba_interval
    | cvx_sba_no
    | cvx_sba_shutdown
  )*
;

cvx_sba_interval
:
  INTERVAL ival = dec NEWLINE
;

cvx_sba_no
:
  NO SHUTDOWN NEWLINE
;

cvx_sba_shutdown
:
  SHUTDOWN NEWLINE
;

cvx_s_hsc
:
  HSC NEWLINE
  (
    cvx_shsc_no
    | cvx_shsc_null
    | cvx_shsc_shutdown
  )*
;

cvx_shsc_no
:
  NO
  (
    cvx_shsc_no_null
    | cvx_shsc_no_shutdown
  )
;

cvx_shsc_no_null
:
  (
    ERROR_REPORTING
    | OVSDB_SHUTDOWN
    | SSL
  ) null_rest_of_line
;

cvx_shsc_null
:
  (
    LOG_CONSOLE
    | LOG_FILE
    | LOG_SYSLOG
    | PERSIST_DATABASE
    | ROUTING
    | VTEP
  ) null_rest_of_line
;

cvx_shsc_no_shutdown
:
  SHUTDOWN NEWLINE
;

cvx_shsc_shutdown
:
  SHUTDOWN NEWLINE
;

cvx_s_mss
:
  MSS NEWLINE
  (
    cvx_smss_no
    | cvx_smss_shutdown
    | cvx_smss_vni
  )*
;

cvx_smss_no
:
  NO SHUTDOWN NEWLINE
;

cvx_smss_shutdown
:
  SHUTDOWN NEWLINE
;

vni_range
:
// 1-16777214
  lo = uint32 DASH hi = uint32
;

cvx_smss_vni
:
  VNI RANGE vni_range NEWLINE
;

cvx_s_openstack
:
  OPENSTACK NEWLINE
  (
    cvx_sos_no
    | cvx_sos_null
    | cvx_sos_shutdown
  )*
;

cvx_sos_no
:
  NO SHUTDOWN NEWLINE
;

cvx_sos_null
:
  (
    GRACE_PERIOD
    | NAME_RESOLUTION
    | NETWORK
  ) null_rest_of_line
;

cvx_sos_shutdown
:
  SHUTDOWN NEWLINE
;

cvx_s_vxlan
:
  VXLAN NEWLINE
  (
    cvx_svxlan_no
    | cvx_svxlan_null
    | cvx_svxlan_shutdown
  )*
;

cvx_svxlan_no
:
  NO
  (
    cvx_svxlan_no_null
    | cvx_svxlan_no_shutdown
  )
;

cvx_svxlan_no_null
:
  (
    ARP
    | FLOOD
    | VNI
  ) null_rest_of_line
;

cvx_svxlan_no_shutdown
:
  SHUTDOWN NEWLINE
;

cvx_svxlan_null
:
  (
    RESYNC_PERIOD
    | VTEP
  ) null_rest_of_line
;

cvx_svxlan_shutdown
:
  SHUTDOWN NEWLINE
;
