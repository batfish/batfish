parser grammar Cisco_logging;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
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
            | dec
         )
      )
      | SEVERITY severity = variable
      | TYPE ltype = variable
      | VRF vrf = variable
   )* NEWLINE
;

logging_archive
:
   ARCHIVE null_rest_of_line
   (
      logging_archive_null
   )*
;

logging_archive_null
:
   NO?
   (
      ARCHIVE_LENGTH
      | DEVICE
      | FREQUENCY
   ) null_rest_of_line
;

logging_buffered
:
   BUFFERED
   (
      (
         DISCRIMINATOR descr = variable
      )? size = dec? logging_severity?
   ) NEWLINE
;

logging_common
:
   logging_address
   | logging_alarm_null
   | logging_archive
   | logging_asdm_buffer_size_null
   | logging_asdm_null
   | logging_buffer_size_null
   | logging_buffered
   | logging_cmts_null
   | logging_console
   | logging_count_null
   | logging_debug_trace_null
   | logging_device_id
   | logging_discriminator_null
   | logging_enable
   | logging_esm_null
   | logging_event_null
   | logging_events_null
   | logging_facility_null
   | logging_format
   | logging_history_null
   | logging_host
   | logging_hostnameprefix_null
   | logging_ip_null
   | logging_level_null
   | logging_linecard_null
   | logging_local_volatile_null
   | logging_logfile_null
   | logging_message
   | logging_message_counter_null
   | logging_monitor_null
   | logging_on
   | logging_origin_id_null
   | logging_override_null
   | logging_permit_hostdown_null
   | logging_proprietary_null
   | logging_queue
   | logging_queue_limit_null
   | logging_rate_limit_null
   | logging_sequence_nums_null
   | logging_server
   | logging_server_arp_null
   | logging_snmp_authfail_null
   | logging_source_interface
   | logging_suppress
   | logging_synchronous_null
   | logging_syslog_null
   | logging_timestamp_null
   | logging_trap
   | logging_userinfo_null
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

logging_device_id
:
   DEVICE_ID
   (
      CLUSTER_ID
      | CONTEXT_NAME
      | HOSTNAME
      | (IPADDRESS variable)
      | (STRING variable)
   ) NEWLINE
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
      VRF vrf = variable
   )?
   (
      DISCRIMINATOR descr = variable
   )?
   (
      TRANSPORT
      (
         TCP
         | UDP
      )
   )?
   (
      PORT
      (
         DEFAULT
         | dec
      )
   )?
   (
      FACILITY name = variable
   )? NEWLINE
;

logging_message
:
   MESSAGE (syslog_id = dec) (LEVEL level = variable)? STANDBY? NEWLINE
;

logging_alarm_null
:
   ALARM null_rest_of_line
;
logging_asdm_null
:
   ASDM null_rest_of_line
;
logging_asdm_buffer_size_null
:
   ASDM_BUFFER_SIZE null_rest_of_line
;
logging_buffer_size_null
:
   BUFFER_SIZE null_rest_of_line
;
logging_count_null
:
   COUNT null_rest_of_line
;
logging_cmts_null
:
   CMTS null_rest_of_line
;
logging_debug_trace_null
:
   DEBUG_TRACE null_rest_of_line
;
logging_discriminator_null
:
   DISCRIMINATOR null_rest_of_line
;
logging_esm_null
:
   ESM null_rest_of_line
;
logging_event_null
:
   EVENT null_rest_of_line
;
logging_events_null
:
   EVENTS null_rest_of_line
;
logging_facility_null
:
   FACILITY null_rest_of_line
;
logging_history_null
:
   HISTORY null_rest_of_line
;
logging_hostnameprefix_null
:
   HOSTNAMEPREFIX null_rest_of_line
;
logging_ip_null
:
   IP null_rest_of_line
;
logging_level_null
:
   LEVEL null_rest_of_line
;
logging_linecard_null
:
   LINECARD null_rest_of_line
;
logging_local_volatile_null
:
   LOCAL_VOLATILE null_rest_of_line
;
logging_logfile_null
:
   LOGFILE null_rest_of_line
;
logging_message_counter_null
:
   MESSAGE_COUNTER null_rest_of_line
;
logging_monitor_null
:
   MONITOR null_rest_of_line
;
logging_origin_id_null
:
   ORIGIN_ID null_rest_of_line
;
logging_permit_hostdown_null
:
   PERMIT_HOSTDOWN null_rest_of_line
;
logging_proprietary_null
:
   PROPRIETARY null_rest_of_line
;
logging_override_null
:
   OVERRIDE null_rest_of_line
;
logging_queue_limit_null
:
   QUEUE_LIMIT null_rest_of_line
;
logging_rate_limit_null
:
   RATE_LIMIT null_rest_of_line
;
logging_sequence_nums_null
:
   SEQUENCE_NUMS null_rest_of_line
;
logging_server_arp_null
:
   SERVER_ARP null_rest_of_line
;
logging_snmp_authfail_null
:
   SNMP_AUTHFAIL null_rest_of_line
;
logging_synchronous_null
:
   SYNCHRONOUS null_rest_of_line
;
logging_syslog_null
:
   SYSLOG null_rest_of_line
;
logging_timestamp_null
:
   TIMESTAMP null_rest_of_line
;
logging_userinfo_null
:
   USERINFO null_rest_of_line
;

logging_on
:
   ON NEWLINE
;

logging_queue
:
   QUEUE (size = dec)? NEWLINE
;

logging_severity
:
   dec
   | ALERTS
   | CRITICAL
   | DEBUGGING
   | EMERGENCIES
   | ERRORS
   | INFORMATIONAL
   | NOTIFICATIONS
   | WARNINGS
;

logging_server
:
   SERVER hostname =
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) null_rest_of_line
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
   TRAP logging_severity? NEWLINE
;

logging_vrf
:
   VRF vrf = variable logging_common
;

s_logging
:
   NO? LOGGING
   (
      logging_common
      | logging_vrf
   )
;
