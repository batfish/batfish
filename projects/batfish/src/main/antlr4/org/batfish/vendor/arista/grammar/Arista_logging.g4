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
    | logging_buffered_null
    | logging_console_null
    | logging_event_null
    | logging_format_null
    | logging_policy_null
    | logging_qos_null
    | logging_synchronous_null
    | logging_trap_null
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

logging_buffered_null
:
   BUFFERED null_rest_of_line
;
logging_console_null
:
   CONSOLE null_rest_of_line
;
logging_event_null
:
   EVENT null_rest_of_line
;
logging_format_null
:
   FORMAT null_rest_of_line
;
logging_policy_null
:
   POLICY null_rest_of_line
;
logging_qos_null
:
   QOS null_rest_of_line
;
logging_synchronous_null
:
   SYNCHRONOUS null_rest_of_line
;
logging_trap_null
:
   TRAP null_rest_of_line
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
  (
     no_logging_event_null
     | no_logging_format_null
     | no_logging_policy_null
     | no_logging_qos_null
     | no_logging_relogging_interval_null
     | no_logging_repeat_messages_null
     | no_logging_synchronous_null
  )
;

no_logging_event_null
:
   EVENT null_rest_of_line
;
no_logging_format_null
:
   FORMAT null_rest_of_line
;
no_logging_policy_null
:
   POLICY null_rest_of_line
;
no_logging_qos_null
:
   QOS null_rest_of_line
;
no_logging_relogging_interval_null
:
   RELOGGING_INTERVAL null_rest_of_line
;
no_logging_repeat_messages_null
:
   REPEAT_MESSAGES null_rest_of_line
;
no_logging_synchronous_null
:
   SYNCHRONOUS null_rest_of_line
;