parser grammar AsaLegacy_ntp;

import Asa_common;

options {
   tokenVocab = AsaLexer;
}

ntp_access_group
:
   ACCESS_GROUP
   (
      IPV4
      | IPV6
      | KOD
      |
      (
         PEER
         (
            name = variable
         )
      )
      |
      (
         QUERY_ONLY
         (
            name = variable
         )
      )
      |
      (
         SERVE
         (
            name = variable
         )
      )
      |
      (
         SERVE_ONLY
         (
            name = variable
         )
      )
      |
      (
         VRF vrf = variable
      )
   )+ NEWLINE
;

ntp_authenticate
:
   AUTHENTICATE NEWLINE
;

ntp_authentication
:
   AUTHENTICATION NEWLINE
;

ntp_authentication_key
:
   AUTHENTICATION_KEY key_num = ntp_key hash_algorithm = ntp_hash_algorithm key = variable NEWLINE
;

ntp_hash_algorithm
:
   CMAC
   | MD5
   | SHA1
   | SHA256
   | SHA512
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
   | ntp_allow_null
   | ntp_authenticate
   | ntp_authentication
   | ntp_authentication_key
   | ntp_clock_period
   | ntp_commit
   | ntp_distribute
   | ntp_interface_null
   | ntp_log_internal_sync_null
   | ntp_logging
   | ntp_max_associations
   | ntp_master
   | ntp_passive_null
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
   MAX_ASSOCIATIONS dec NEWLINE
;

ntp_master
:
   MASTER NEWLINE
;

ntp_allow_null
:
   ALLOW null_rest_of_line
;
ntp_interface_null
:
   INTERFACE null_rest_of_line
;
ntp_log_internal_sync_null
:
   LOG_INTERNAL_SYNC null_rest_of_line
;
ntp_passive_null
:
   PASSIVE null_rest_of_line
;

ntp_peer
:
   PEER null_rest_of_line
;

ntp_server
:
   SERVER
   (VRF vrf = variable)?
   hostname = variable
   (
      IBURST
      | KEY key = ntp_key
      | MAXPOLL dec
      | MINPOLL dec
      | prefer = PREFER
      | SOURCE
        (
           src_interface = interface_name_unstructured
           | src_interface_alias = variable
        )
      | USE_VRF vrf = variable
      | VERSION ver = dec
   )*
   NEWLINE
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
   TRUSTED_KEY key = ntp_key NEWLINE
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

