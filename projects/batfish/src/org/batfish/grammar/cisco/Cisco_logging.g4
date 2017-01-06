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

logging_enable
:
   ENABLE NEWLINE
;

logging_format
:
   FORMAT
   (
      (
         HOSTNAME FQDN
      )
      |
      (
         TIMESTAMP
         (
            HIGH_RESOLUTION
            |
            (
               TRADITIONAL ~NEWLINE*
            )
         )
      )
   ) NEWLINE
;

logging_host
:
   HOST iname = variable? hostname = variable
   (
      VRF variable
   )? NEWLINE
;

logging_null
:
   (
      ASDM
      | ASDM_BUFFER_SIZE
      | BUFFER_SIZE
      | DEBUG_TRACE
      | ESM
      | EVENT
      | EVENTS
      | FACILITY
      | HISTORY
      | IP_ADDRESS
      | IP
      | IPV6_ADDRESS
      | LEVEL
      | LINECARD
      | LOGFILE
      | MONITOR
      | PERMIT_HOSTDOWN
      | QUEUE_LIMIT
      | RATE_LIMIT
      | SEQUENCE_NUMS
      | SERVER
      | SERVER_ARP
      | SNMP_AUTHFAIL
      | SUPPRESS
      | SYNCHRONOUS
      | TIMESTAMP
      | VRF
   ) ~NEWLINE* NEWLINE logging_null_inner*
;

logging_null_inner
:
   (
	  ALARM
	  | ALL_OF_ROUTER
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
      | logging_enable
      | logging_format
      | logging_host
      | logging_null
      | logging_on
      | logging_source_interface
      | logging_trap
   )
;