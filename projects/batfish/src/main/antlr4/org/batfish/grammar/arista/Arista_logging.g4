parser grammar Arista_logging;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

logging_severity
:
  dec // 0-7
  | ALERTS
  | CRITICAL
  | DEBUGGING
  | EMERGENCIES
  | ERRORS
  | INFORMATIONAL
  | NOTIFICATIONS
  | WARNINGS
;

s_logging
:
  LOGGING
  (
    logging_facility
    | logging_level
    | logging_null
    | logging_on
    | logging_vrf
    | logging_vrf_host // default vrf
    | logging_vrf_source_interface // default vrf
  )
;

logging_facility
:
  FACILITY name=word NEWLINE
;

logging_level
:
  LEVEL name=word sev=logging_severity NEWLINE
;

logging_null
:
  (
    BUFFERED
    | CONSOLE
    | EVENT
    | FORMAT
    | POLICY
    | QOS
    | SYNCHRONOUS
    | TRAP
  ) null_rest_of_line
;

logging_on
:
  ON NEWLINE
;

logging_vrf
:
  VRF name = vrf_name
  (
    logging_vrf_host
    | logging_vrf_source_interface
  )
;

logging_vrf_host
:
  HOST host=word (portnum=dec (PROTOCOL (TCP | UDP))?)? NEWLINE
;

logging_vrf_source_interface
:
  SOURCE_INTERFACE name = interface_name NEWLINE
;

no_logging
:
  LOGGING
  no_logging_null
;

no_logging_null
:
  (
    EVENT
    | FORMAT
    | POLICY
    | QOS
    | RELOGGING_INTERVAL
    | REPEAT_MESSAGES
    | SYNCHRONOUS
  ) null_rest_of_line
;