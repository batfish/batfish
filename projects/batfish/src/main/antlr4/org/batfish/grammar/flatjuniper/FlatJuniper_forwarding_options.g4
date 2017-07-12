parser grammar FlatJuniper_forwarding_options;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_forwarding_options
:
   FORWARDING_OPTIONS
   (
      fo_dhcp_relay
      | fo_null
   )
;