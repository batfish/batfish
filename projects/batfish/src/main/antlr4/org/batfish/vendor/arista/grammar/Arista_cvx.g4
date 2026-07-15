parser grammar Arista_cvx;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

s_cvx
:
  CVX NEWLINE
  (
    cvx_heartbeat_interval_null
    | cvx_heartbeat_timeout_null
    | cvx_no
    | cvx_peer_null
    | cvx_port_null
    | cvx_service
    | cvx_shutdown_null
  )*
;

cvx_no
:
  NO ( cvx_no_peer_null | cvx_no_shutdown_null | cvx_no_source_interface_null | cvx_no_ssl_null )
;

cvx_no_peer_null
:
   PEER null_rest_of_line
;
cvx_no_shutdown_null
:
   SHUTDOWN null_rest_of_line
;
cvx_no_source_interface_null
:
   SOURCE_INTERFACE null_rest_of_line
;
cvx_no_ssl_null
:
   SSL null_rest_of_line
;

cvx_heartbeat_interval_null
:
   HEARTBEAT_INTERVAL null_rest_of_line
;
cvx_heartbeat_timeout_null
:
   HEARTBEAT_TIMEOUT null_rest_of_line
;
cvx_peer_null
:
   PEER null_rest_of_line
;
cvx_port_null
:
   PORT null_rest_of_line
;
cvx_shutdown_null
:
   SHUTDOWN null_rest_of_line
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
    cvx_shsc_log_console_null
    | cvx_shsc_log_file_null
    | cvx_shsc_log_syslog_null
    | cvx_shsc_no
    | cvx_shsc_persist_database_null
    | cvx_shsc_routing_null
    | cvx_shsc_shutdown
    | cvx_shsc_vtep_null
  )*
;

cvx_shsc_no
:
  NO
  (
    cvx_shsc_no_error_reporting_null
    | cvx_shsc_no_ovsdb_shutdown_null
    | cvx_shsc_no_shutdown
    | cvx_shsc_no_ssl_null
  )
;

cvx_shsc_no_error_reporting_null
:
   ERROR_REPORTING null_rest_of_line
;
cvx_shsc_no_ovsdb_shutdown_null
:
   OVSDB_SHUTDOWN null_rest_of_line
;
cvx_shsc_no_ssl_null
:
   SSL null_rest_of_line
;

cvx_shsc_log_console_null
:
   LOG_CONSOLE null_rest_of_line
;
cvx_shsc_log_file_null
:
   LOG_FILE null_rest_of_line
;
cvx_shsc_log_syslog_null
:
   LOG_SYSLOG null_rest_of_line
;
cvx_shsc_persist_database_null
:
   PERSIST_DATABASE null_rest_of_line
;
cvx_shsc_routing_null
:
   ROUTING null_rest_of_line
;
cvx_shsc_vtep_null
:
   VTEP null_rest_of_line
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
    cvx_sos_grace_period_null
    | cvx_sos_name_resolution_null
    | cvx_sos_network_null
    | cvx_sos_no
    | cvx_sos_shutdown
  )*
;

cvx_sos_no
:
  NO SHUTDOWN NEWLINE
;

cvx_sos_grace_period_null
:
   GRACE_PERIOD null_rest_of_line
;
cvx_sos_name_resolution_null
:
   NAME_RESOLUTION null_rest_of_line
;
cvx_sos_network_null
:
   NETWORK null_rest_of_line
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
    | cvx_svxlan_resync_period_null
    | cvx_svxlan_shutdown
    | cvx_svxlan_vtep_null
  )*
;

cvx_svxlan_no
:
  NO
  (
    cvx_svxlan_no_arp_null
    | cvx_svxlan_no_flood_null
    | cvx_svxlan_no_shutdown
    | cvx_svxlan_no_vni_null
  )
;

cvx_svxlan_no_arp_null
:
   ARP null_rest_of_line
;
cvx_svxlan_no_flood_null
:
   FLOOD null_rest_of_line
;
cvx_svxlan_no_vni_null
:
   VNI null_rest_of_line
;

cvx_svxlan_no_shutdown
:
  SHUTDOWN NEWLINE
;

cvx_svxlan_resync_period_null
:
   RESYNC_PERIOD null_rest_of_line
;
cvx_svxlan_vtep_null
:
   VTEP null_rest_of_line
;

cvx_svxlan_shutdown
:
  SHUTDOWN NEWLINE
;
