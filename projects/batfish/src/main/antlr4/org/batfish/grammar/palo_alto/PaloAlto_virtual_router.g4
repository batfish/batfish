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
        | vr_protocol
        | vr_routing_table
    )?
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
    )?
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
       | MAX_PATH UINT8 // only 2,3,4 are allowed
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
    )?
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
  IP_ADDRESS_LITERAL addr = ip_address_or_slash32
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
  )
;

path_monitor_hold_time_min:
 // 0-1440, default 2
 uint16
;

vrrtpm_enable: ENABLE yn = yes_or_no;
vrrtpm_failure_condition: FAILURE_CONDITION (ANY | ALL);
vrrtpm_hold_time: HOLD_TIME min = path_monitor_hold_time_min;

// TODO: more route tables
vrrt_route_table: ROUTE_TABLE UNICAST;
