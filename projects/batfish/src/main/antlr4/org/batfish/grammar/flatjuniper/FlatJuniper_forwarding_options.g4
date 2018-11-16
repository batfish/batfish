parser grammar FlatJuniper_forwarding_options;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

fo_dhcp_relay
:
   DHCP_RELAY
   (
      fod_common
      | fod_group
      | fod_null
      | fod_server_group
   )
;

fo_helpers
:
   HELPERS
   (
      foh_bootp
      | foh_null
   )
;

fo_null
:
   (
      ANALYZER
      | ENHANCED_HASH_KEY
      | FAMILY
      | HASH_KEY
      | LOAD_BALANCE
      | MULTICAST
      | PORT_MIRRORING
      | SAMPLING
      | STORM_CONTROL_PROFILES
   ) null_filler
;

fod_active_server_group
:
   ACTIVE_SERVER_GROUP name = variable
;

fod_common
:
   fod_active_server_group
;

fod_group
:
   GROUP name = variable
   (
      fod_common
      | fodg_interface
      | fodg_null
   )
;

fod_null
:
   (
      FORWARD_SNOOPED_CLIENTS
      | OVERRIDES
   ) null_filler
;

fod_server_group
:
   SERVER_GROUP name = variable address = IP_ADDRESS
;

fodg_interface
:
   INTERFACE
   (
      ALL
      | interface_id
   )
;

fodg_null
:
   (
      OVERRIDES
   ) null_filler
;

foh_bootp
:
   BOOTP
   (
      apply
      | fohb_common
      | fohb_interface
      | fohb_null
   )
;

foh_null
:
   (
      TRACEOPTIONS
   ) null_filler
;

fohb_common
:
   fohb_null
   | fohb_server
;

fohb_interface
:
   INTERFACE
   (
      ALL
      | interface_id
      | wildcard
   )
   (
      apply
      | fohb_common
   )
;

fohb_null
:
   (
      DESCRIPTION
      | RELAY_AGENT_OPTION
   ) null_filler
;

fohb_server
:
   SERVER address = IP_ADDRESS
   (
      ROUTING_INSTANCE ri = variable
   )?
;

s_forwarding_options
:
   FORWARDING_OPTIONS
   (
      apply
      | fo_dhcp_relay
      | fo_helpers
      | fo_null
   )
;
