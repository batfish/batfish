parser grammar CiscoXr_logging;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

logging_address
:
   hostname =
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   )
   (
      DISCRIMINATOR descr = variable
      | FACILITY facility = variable
      |
      (
         PORT
         (
            DEFAULT
            | uint_legacy
         )
      )
      | SEVERITY severity = variable
      | TYPE ltype = variable
      | VRF vrf = vrf_name
   )* NEWLINE
;

logging_archive: ARCHIVE null_rest_of_line logging_archive_inner*;

logging_archive_inner
:
  logging_archive_no
  | logging_archive_null
;

logging_archive_no
:
  NO
  (
    logging_archive_null
  )
;

logging_archive_null
:
   (
      ARCHIVE_LENGTH
      | ARCHIVE_SIZE
      | DEVICE
      | FILE_SIZE
      | FREQUENCY
      | SEVERITY
      | THRESHOLD
   ) null_rest_of_line
;

logging_buffered
:
   BUFFERED
   (
     logging_buffered_buffer_size
     | logging_buffered_discriminator
     | logging_buffered_set_severity
   )
;

logging_buffered_buffer_size: logging_buffer_size NEWLINE;

logging_buffer_size
:
  // 2097152-125000000
  uint32
;

logging_buffered_discriminator: DISCRIMINATOR NEWLINE discriminator_inner*;

discriminator_inner: discriminator_null;

discriminator_null
:
  (
    MATCH1
    | MATCH2
    | MATCH3
    | NOMATCH1
    | NOMATCH2
    | NOMATCH3
  ) null_rest_of_line
;

logging_buffered_set_severity: logging_buffered_severity NEWLINE;

logging_buffered_severity
:
  ALERTS
  | CRITICAL
  | DEBUGGING
  | EMERGENCIES
  | ERRORS
  | INFORMATIONAL
  | NOTIFICATIONS
  | WARNINGS
;

logging_console
:
  CONSOLE
  (
    logging_console_disable
    | logging_console_discriminator
    | logging_console_set_severity
  )
;

logging_console_disable: DISABLE NEWLINE;

logging_console_discriminator: DISCRIMINATOR NEWLINE discriminator_inner*;

logging_console_set_severity: logging_console_severity NEWLINE;

logging_console_severity
:
  ALERTS
  | CRITICAL
  | DEBUGGING
  | EMERGENCIES
  | ERRORS
  | INFORMATIONAL
  | NOTIFICATIONS
  | WARNING
;

logging_null
:
   (
      FACILITY
      | HISTORY
      | HOSTNAMEPREFIX
      | MONITOR
   ) null_rest_of_line
;

logging_severity
:
   uint_legacy
   | ALERTS
   | CRITICAL
   | DEBUGGING
   | EMERGENCIES
   | ERRORS
   | INFORMATIONAL
   | NOTIFICATIONS
   | WARNINGS
;

logging_source_interface
:
   SOURCE_INTERFACE interface_name
   (
      VRF vrf = variable
   )? NEWLINE
;

logging_suppress
:
   SUPPRESS null_rest_of_line
   (
      logging_suppress_null
   )*
;

logging_suppress_null
:
   NO?
   (
      ALARM
      | ALL_ALARMS
      | ALL_OF_ROUTER
   ) null_rest_of_line
;

logging_trap
:
  TRAP
  (
    logging_trap_disable
    | logging_trap_set_severity
  )
;

logging_trap_disable: DISABLE NEWLINE;

logging_trap_set_severity: logging_trap_severity NEWLINE;

logging_trap_severity
:
  ALERTS
  | CRITICAL
  | DEBUGGING
  | EMERGENCIES
  | ERRORS
  | INFORMATIONAL
  | NOTIFICATIONS
  | WARNING
;

s_logging
:
  LOGGING
  (
    logging_address
    | logging_archive
    | logging_buffered
    | logging_console
    | logging_events
    | logging_null
    | logging_source_interface
    | logging_suppress
    | logging_trap
  )
;

logging_events
:
  EVENTS
  (
    logging_events_filter
    | logging_events_null
  )
;

logging_events_filter: FILTER NEWLINE logging_events_filter_inner*;

logging_events_filter_inner: logging_events_filter_null;

logging_events_filter_null: MATCH null_rest_of_line;

logging_events_null
:
  (
    BUFFER_SIZE
    | DISPLAY_LOCATION
    | LEVEL
    | LINK_STATUS
    | THRESHOLD
  ) null_rest_of_line
;
