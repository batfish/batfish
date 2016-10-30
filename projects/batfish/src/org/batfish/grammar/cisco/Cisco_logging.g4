parser grammar Cisco_logging;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

logging_buffered
:
   BUFFERED size = DEC? logging_severity? NEWLINE
;

logging_console
:
   CONSOLE
   (
      DISABLE
      | logging_severity
   )?
   (
      EXCEPT ERRORS
   )? NEWLINE
;

logging_host
:
   HOST hostname = variable
   (
      VRF variable
   )? NEWLINE
;

logging_null
:
   (
      EVENT
      | FACILITY
      | HISTORY
      | IP_ADDRESS
      | IP
      | LEVEL
      | LINECARD
      | LOGFILE
      | QUEUE_LIMIT
      | RATE_LIMIT
      | SERVER
      | SEQUENCE_NUMS
      | SNMP_AUTHFAIL
      | SYNCHRONOUS
      | TIMESTAMP
   ) ~NEWLINE* NEWLINE
;

logging_on
:
   ON NEWLINE
;

logging_severity
:
   DEC
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
      VRF variable
   )? NEWLINE
;

logging_trap
:
   TRAP logging_severity? NEWLINE
;

s_logging
:
   LOGGING
   (
      logging_buffered
      | logging_console
      | logging_host
      | logging_null
      | logging_on
      | logging_source_interface
      | logging_trap
   )
;