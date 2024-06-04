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

fo_vxlan_routing
:
   VXLAN_ROUTING
   (
      fov_overlay_ecmp
   )
;

fod_active_server_group
:
   ACTIVE_SERVER_GROUP name = junos_name
;

fod_common
:
   fod_active_server_group
;

fod_group
:
   GROUP name = junos_name
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
   SERVER_GROUP name = junos_name fods_address?
;

fods_address
:
   address = ip_address
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
   | fohb_server_null
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

fohb_server_null
:
   SERVER
   (
      wildcard
      | IP_ADDRESS
   )
   (
      ROUTING_INSTANCE ri = junos_name
   )?
;

fov_overlay_ecmp
:
   OVERLAY_ECMP
;

s_forwarding_options
:
   FORWARDING_OPTIONS
   (
      apply
      | fo_dhcp_relay
      | fo_helpers
      | fo_null
      | fo_vxlan_routing
   )
;
