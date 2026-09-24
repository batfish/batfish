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
      | fod_forward_snooped_clients_null
      | fod_group
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
   | fod_relay_option
   | fod_relay_option_82_null
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/relay-option-edit-forwarding-options.html
fod_relay_option
:
   RELAY_OPTION
   (
      fodro_default_action
      | fodro_equals
      | fodro_option_number_null
      | fodro_starts_with
   )
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/relay-option-82-edit-forwarding-options.html
fod_relay_option_82_null
:
   RELAY_OPTION_82
   (
      fodro82_circuit_id_null
      | fodro82_link_selection_null
      | fodro82_remote_id_null
      | fodro82_server_id_override_null
      | fodro82_vendor_specific_null
   )
;

fodro_action
:
   fodro_drop_null
   | fodro_forward_only_null
   | fodro_local_server_group_null
   | fodro_relay_server_group
;

fodro_default_action
:
   DEFAULT_ACTION fodro_action
;

fodro_drop_null
:
   DROP
;

fodro_equals
:
   EQUALS fodro_value fodro_action
;

fodro_forward_only_null
:
   FORWARD_ONLY
;

fodro_local_server_group_null
:
   LOCAL_SERVER_GROUP junos_name
;

fodro_option_number_null
:
   OPTION_NUMBER uint8
;

fodro_relay_server_group
:
   RELAY_SERVER_GROUP name = junos_name
;

fodro_starts_with
:
   STARTS_WITH fodro_value fodro_action
;

fodro_value
:
   ASCII junos_name
   | HEXADECIMAL secret_string
;

fodro82_circuit_id_null
:
   CIRCUIT_ID
   (
      INCLUDE_IRB_AND_L2
      | KEEP_INCOMING_CIRCUIT_ID
      | NO_VLAN_INTERFACE_NAME
      | PREFIX junos_name
      | USE_INTERFACE_DESCRIPTION
        (
           DEVICE
           | LOGICAL
        )
      | USE_VLAN_ID
   )
;

fodro82_link_selection_null
:
   LINK_SELECTION
;

fodro82_remote_id_null
:
   REMOTE_ID
   (
      INCLUDE_IRB_AND_L2
      | KEEP_INCOMING_REMOTE_ID
      | NO_VLAN_INTERFACE_NAME
      | PREFIX junos_name
      | USE_INTERFACE_DESCRIPTION
        (
           DEVICE
           | LOGICAL
        )
      | USE_VLAN_ID
   )
;

fodro82_server_id_override_null
:
   SERVER_ID_OVERRIDE
;

fodro82_vendor_specific_null
:
   VENDOR_SPECIFIC
   (
      HOST_NAME
      | LOCATION
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
   | fohb_maximum_hop_count_null
   | fohb_relay_agent_option_null
   | fohb_server_null
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/maximum-hop-count-edit-forwarding-options.html
fohb_maximum_hop_count_null
:
   MAXIMUM_HOP_COUNT uint8
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
