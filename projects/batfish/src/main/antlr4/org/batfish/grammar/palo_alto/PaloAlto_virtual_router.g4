parser grammar PaloAlto_virtual_router;

import PaloAlto_common, PaloAlto_redist_profile;

options {
    tokenVocab = PaloAltoLexer;
}

protocol_ad
:
// 10-240
    uint8
;

sn_virtual_router
:
    VIRTUAL_ROUTER vr_definition?
;

vr_definition
:
    name = variable
    (
        vr_admin_dists
        | vr_ecmp
        | vr_interface
        | vr_multicast
        | vr_protocol
        | vr_routing_table
        | vr_vlan
    )*
;

vr_admin_dists
:
    ADMIN_DISTS
    (
        vrad_ebgp
        | vrad_ibgp
        | vrad_ospf_int
        | vrad_ospf_ext
        | vrad_ospfv3_int
        | vrad_ospfv3_ext
        | vrad_rip
        | vrad_static
        | vrad_static_ipv6
    )*
;

vrad_ebgp
:
    EBGP ad = protocol_ad
;

vrad_ibgp
:
    IBGP ad = protocol_ad
;

vrad_ospf_int
:
    OSPF_INT ad = protocol_ad
;

vrad_ospf_ext
:
    OSPF_EXT ad = protocol_ad
;

vrad_ospfv3_int
:
    OSPFV3_INT ad = protocol_ad
;

vrad_ospfv3_ext
:
    OSPFV3_EXT ad = protocol_ad
;
vrad_rip
:
    RIP ad = protocol_ad
;

vrad_static
:
    STATIC ad = protocol_ad
;

vrad_static_ipv6
:
    STATIC_IPV6 ad = protocol_ad
;

vr_ecmp
:
    ECMP
    (
       ALGORITHM null_rest_of_line
       | MAX_PATH uint8
       | vr_ecmp_enable
    )
;

vr_ecmp_enable
:
    ENABLE yes_or_no
;

vr_interface
:
    INTERFACE variable_list?
;

vr_protocol
:
    PROTOCOL
    (
        vrp_bgp
        | vrp_ospf
        | vrp_ospfv3
        | vrp_redist_profile
        | vrp_rip
    )*
;

vr_routing_table
:
    ROUTING_TABLE IP STATIC_ROUTE name = variable
    (
        vrrt_admin_dist
        | vrrt_bfd
        | vrrt_destination
        | vrrt_interface
        | vrrt_metric
        | vrrt_nexthop
        | vrrt_path_monitor
        | vrrt_route_table
    )
;

vrrt_admin_dist
:
    ADMIN_DIST distance = protocol_ad
;

vrrt_bfd
:
    BFD PROFILE name = variable
;

vrrt_destination
:
    DESTINATION destination = ip_prefix
;

vrrt_interface
:
    INTERFACE iface = variable
;

vrrt_metric
:
    METRIC metric = uint16
;

vrrt_nexthop
:
  NEXTHOP
  (
    vrrtn_discard
    | vrrtn_ip
    | vrrtn_next_vr
  )
;

vrrtn_discard: DISCARD;

vrrtn_ip
:
  IP_ADDRESS_LITERAL addr = interface_address_or_reference
;

vrrtn_next_vr
:
  NEXT_VR name = variable
;

vrrt_path_monitor
:
  PATH_MONITOR
  (
    vrrtpm_enable
    | vrrtpm_failure_condition
    | vrrtpm_hold_time
    | vrrtpm_monitor_destinations
  )
;

path_monitor_hold_time_min:
 // 0-1440, default 2
 uint16
;

vrrtpm_enable: ENABLE yn = yes_or_no;
vrrtpm_failure_condition: FAILURE_CONDITION (ANY | ALL);
vrrtpm_hold_time: HOLD_TIME min = path_monitor_hold_time_min;

vrrtpm_monitor_destinations
:
    MONITOR_DESTINATIONS name = variable vrrtpm_monitor_dest_settings*
;

vrrtpm_monitor_dest_settings
:
    vrrtpmds_enable
    | vrrtpmds_source
    | vrrtpmds_destination
    | vrrtpmds_interval
    | vrrtpmds_count
;

vrrtpmds_count: COUNT count = uint16;
vrrtpmds_destination: DESTINATION dest = variable;
vrrtpmds_enable: ENABLE yn = yes_or_no;
vrrtpmds_interval: INTERVAL interval = uint16;
vrrtpmds_source: SOURCE source = ip_prefix;

// TODO: more route tables
vrrt_route_table: ROUTE_TABLE (MULTICAST | UNICAST);

vr_multicast
:
    MULTICAST
    (
        vrm_enable
        | vrm_interface_group
        | vrm_rp
    )*
;

vrm_enable
:
    ENABLE yn = yes_or_no
;

vrm_interface_group
:
    INTERFACE_GROUP name = variable
    (
        vrmig_igmp
        | vrmig_interface
        | vrmig_pim
    )*
;

vrmig_interface
:
    INTERFACE iface = variable
;

vrmig_igmp
:
    IGMP
    (
        vrmigi_enable
        | vrmigi_immediate_leave
        | vrmigi_last_member_query_interval
        | vrmigi_max_groups
        | vrmigi_max_query_response_time
        | vrmigi_max_sources
        | vrmigi_query_interval
        | vrmigi_robustness
        | vrmigi_router_alert_policing
        | vrmigi_version
    )*
;

vrmigi_enable
:
    ENABLE yn = yes_or_no
;

vrmigi_immediate_leave
:
    IMMEDIATE_LEAVE yn = yes_or_no
;

vrmigi_last_member_query_interval
:
    LAST_MEMBER_QUERY_INTERVAL val = variable
;

vrmigi_max_groups
:
    MAX_GROUPS (UNLIMITED | val = variable)
;

vrmigi_max_query_response_time
:
    MAX_QUERY_RESPONSE_TIME val = variable
;

vrmigi_max_sources
:
    MAX_SOURCES (UNLIMITED | val = variable)
;

vrmigi_query_interval
:
    QUERY_INTERVAL val = variable
;

vrmigi_robustness
:
    ROBUSTNESS val = variable
;

vrmigi_router_alert_policing
:
    ROUTER_ALERT_POLICING yn = yes_or_no
;

vrmigi_version
:
    VERSION val = variable
;

vrmig_pim
:
    PIM
    (
        vrmigp_assert_interval
        | vrmigp_bsr_border
        | vrmigp_dr_priority
        | vrmigp_enable
        | vrmigp_hello_interval
        | vrmigp_join_prune_interval
    )*
;

vrmigp_assert_interval
:
    ASSERT_INTERVAL val = variable
;

vrmigp_bsr_border
:
    BSR_BORDER yn = yes_or_no
;

vrmigp_dr_priority
:
    DR_PRIORITY val = variable
;

vrmigp_enable
:
    ENABLE yn = yes_or_no
;

vrmigp_hello_interval
:
    HELLO_INTERVAL val = variable
;

vrmigp_join_prune_interval
:
    JOIN_PRUNE_INTERVAL val = variable
;

vrm_rp
:
    RP
    (
        vrmrp_external_rp
    )*
;

vrmrp_external_rp
:
    EXTERNAL_RP address = ip_address
    (
        vrmrpe_group_addresses
        | vrmrpe_override
    )*
;

vrmrpe_group_addresses
:
    GROUP_ADDRESSES prefix = ip_prefix
;

vrmrpe_override
:
    OVERRIDE yn = yes_or_no
;

vr_vlan
:
    VLAN vlan_id = variable?
    (
        vr_vlan_interface
    )*
;

vr_vlan_interface
:
    INTERFACE iface = variable
;
