parser grammar FlatJuniper_bmp;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

ro_bmp
:
   BMP
   (
      rob_station_address_null
      | rob_station_null
      | rob_station_port_null
   )
;

rob_station_address_null
:
   STATION_ADDRESS IP_ADDRESS
;

rob_station_null
:
   STATION name = junos_name
   (
     apply_groups
     | robs_connection_mode_null
     | robs_hold_down_null
     | robs_local_address_null
     | robs_local_port_null
     | robs_priority_null
     | robs_route_monitoring_null
     | robs_station_address_null
     | robs_statistics_timeout_null
   )
;

rob_station_port_null
:
   STATION_PORT dec
;

robs_connection_mode_null
:
   CONNECTION_MODE
   (
     ACTIVE
     | PASSIVE
   )
;

robs_hold_down_null
:
   HOLD_DOWN
   (
     timer = uint16
     | FLAPS flaps = uint8
     | FLAP_PERIOD period = uint16
   )*
;

robs_local_address_null
:
   LOCAL_ADDRESS address = ip_address
;

robs_local_port_null
:
   LOCAL_PORT number = uint16
;

robs_priority_null
:
   PRIORITY (HIGH | LOW | MEDIUM)
;

robs_route_monitoring_null
:
   ROUTE_MONITORING
   (
     LOC_RIB
     | POST_POLICY EXCLUDE_NON_ELIGIBLE?
     | PRE_POLICY EXCLUDE_NON_FEASIBLE?
     | RIB_OUT (POST_POLICY | PRE_POLICY)
   )
;

robs_station_address_null
:
   STATION_ADDRESS address = IP_ADDRESS
;

robs_statistics_timeout_null
:
   STATISTICS_TIMEOUT seconds = uint16
;
