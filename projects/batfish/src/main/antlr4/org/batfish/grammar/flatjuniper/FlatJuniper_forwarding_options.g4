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
      | fod_forward_only_replies_null
      | fod_forward_snooped_clients_null
      | fod_group
      | fod_no_snoop_null
      | fod_overrides_null
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

fo_analyzer_null
:
   ANALYZER null_filler
;
fo_enhanced_hash_key_null
:
   ENHANCED_HASH_KEY null_filler
;
fo_evpn_vxlan
:
   EVPN_VXLAN foev_shared_tunnels_null
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/evpn-vxlan.html
foev_shared_tunnels_null
:
   SHARED_TUNNELS
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/explicit-null-cos-edit-forwarding-options.html
fo_explicit_null_cos
:
   EXPLICIT_NULL_COS
   (
      INET
      | INET6
   )?
;

fo_family_null
:
   FAMILY null_filler
;
fo_hash_key_null
:
   HASH_KEY null_filler
;
fo_load_balance_null
:
   LOAD_BALANCE null_filler
;
fo_multicast_null
:
   MULTICAST null_filler
;
fo_port_mirroring_null
:
   PORT_MIRRORING null_filler
;
fo_sampling_null
:
   SAMPLING null_filler
;
fo_storm_control_profiles_null
:
   STORM_CONTROL_PROFILES null_filler
;

fo_vxlan_routing
:
   VXLAN_ROUTING
   (
      fov_interface_num_null
      | fov_next_hop_null
      | fov_overlay_ecmp
   )
;

fod_active_server_group
:
   ACTIVE_SERVER_GROUP name = junos_name
;

fod_common
:
   fod_active_server_group
   | fod_forward_only_null
   | fod_route_suppression_null
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/forward-only-edit-forwarding-options.html
fod_forward_only_null
:
   FORWARD_ONLY
   (
      fodfo_logical_system_null
      | fodfo_routing_instance_null
   )
;

fodfo_logical_system_null
:
   LOGICAL_SYSTEM junos_name
;

fodfo_routing_instance_null
:
   ROUTING_INSTANCE junos_name
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/forward-only-replies-edit-forwarding-options.html
fod_forward_only_replies_null
:
   FORWARD_ONLY_REPLIES
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/no-snoop-edit-dhcp.html
fod_no_snoop_null
:
   NO_SNOOP
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/route-suppression-edit-dhcp.html
fod_route_suppression_null
:
   ROUTE_SUPPRESSION
   (
      ACCESS
      | ACCESS_INTERNAL
      | DESTINATION
   )
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

fod_forward_snooped_clients_null
:
   FORWARD_SNOOPED_CLIENTS null_filler
;
fod_overrides_null
:
   OVERRIDES null_filler
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
      | fohb_description_null
      | fohb_interface
      | fohb_relay_agent_option_null
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
   fohb_description_null
   | fohb_relay_agent_option_null
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

fohb_description_null
:
   DESCRIPTION null_filler
;
fohb_relay_agent_option_null
:
   RELAY_AGENT_OPTION null_filler
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

// https://www.juniper.net/documentation/us/en/software/junos/evpn-vxlan/topics/ref/statement/interface-num-edit-forwarding-options.html
fov_interface_num_null
:
   INTERFACE_NUM uint32
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/next-hop-edit-forwarding-options-vxlan-routing.html
fov_next_hop_null
:
   NEXT_HOP uint32
;

s_forwarding_options
:
   FORWARDING_OPTIONS
   (
      apply
      | fo_analyzer_null
      | fo_dhcp_relay
      | fo_enhanced_hash_key_null
      | fo_evpn_vxlan
      | fo_explicit_null_cos
      | fo_family_null
      | fo_hash_key_null
      | fo_helpers
      | fo_load_balance_null
      | fo_multicast_null
      | fo_port_mirroring_null
      | fo_sampling_null
      | fo_storm_control_profiles_null
      | fo_vxlan_routing
   )
;
