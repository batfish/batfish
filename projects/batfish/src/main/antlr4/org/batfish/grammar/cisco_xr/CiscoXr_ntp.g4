parser grammar CiscoXr_ntp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

ntp_access_group
:
   ACCESS_GROUP
   (VRF vrf = vrf_name)?
   (IPV4 | IPV6)?
   (PEER | QUERY_ONLY | SERVE | SERVE_ONLY)
   name = access_list_name NEWLINE
;

ntp_authenticate
:
   AUTHENTICATE NEWLINE
;

ntp_authentication
:
   AUTHENTICATION NEWLINE
;

ntp_clock_period
:
   CLOCK_PERIOD null_rest_of_line
;

ntp_commit
:
   COMMIT NEWLINE
;

ntp_common
:
   ntp_access_group
   | ntp_authenticate
   | ntp_authentication
   | ntp_clock_period
   | ntp_commit
   | ntp_distribute
   | ntp_logging
   | ntp_max_associations
   | ntp_master
   | ntp_null
   | ntp_peer
   | ntp_server
   | ntp_source
   | ntp_source_interface
   | ntp_trusted_key
   | ntp_update_calendar
;

ntp_distribute
:
   DISTRIBUTE NEWLINE
;

ntp_logging
:
   LOGGING NEWLINE
;

ntp_max_associations
:
   MAX_ASSOCIATIONS uint_legacy NEWLINE
;

ntp_master
:
   MASTER NEWLINE
;

ntp_null
:
   (
      ALLOW
      | AUTHENTICATION_KEY
      | INTERFACE
      | LOG_INTERNAL_SYNC
      | PASSIVE
   ) null_rest_of_line
;

ntp_peer
:
   PEER null_rest_of_line
;

ntp_server
:
   SERVER
   (
      VRF vrf = vrf_name
   )? hostname = variable
   (
      (
         KEY key = uint_legacy
      )
      |
      (
         MAXPOLL uint_legacy
      )
      |
      (
         MINPOLL uint_legacy
      )
      | prefer = PREFER
      | SOURCE src_interface = interface_name
      |
      (
         VERSION ver = uint_legacy
      )
   )* NEWLINE
;

ntp_source
:
   SOURCE null_rest_of_line
;

ntp_source_interface
:
   SOURCE_INTERFACE iname = interface_name NEWLINE
;

ntp_trusted_key
:
   TRUSTED_KEY uint_legacy NEWLINE
;

ntp_update_calendar
:
   UPDATE_CALENDAR null_rest_of_line
;

s_ntp
:
   NO? NTP
   (
      ntp_common
      |
      (
         NEWLINE ntp_common*
      )
   )
;

