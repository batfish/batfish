parser grammar Legacy_ntp;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

ntp_common
:
   ntp_authenticate
   | ntp_authentication_key
   | ntp_server
   | ntp_source
   | ntp_trusted_key
;

ntp_authenticate
:
   AUTHENTICATE NEWLINE
;

ntp_authentication_key
:
   AUTHENTICATION_KEY null_rest_of_line
;

ntp_server
:
   SERVER
   (VRF vrf = vrf_name)?
   hostname = variable
   (
      BURST
      | IBURST
      | KEY key = dec
      | MAXPOLL dec
      | MINPOLL dec
      | prefer = PREFER
      | SOURCE
        (
           src_interface = interface_name_unstructured
           | src_interface_alias = variable
        )
      | VERSION ver = dec
   )*
   NEWLINE
;

ntp_source
:
   SOURCE null_rest_of_line
;

ntp_trusted_key
:
   TRUSTED_KEY dec NEWLINE
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

