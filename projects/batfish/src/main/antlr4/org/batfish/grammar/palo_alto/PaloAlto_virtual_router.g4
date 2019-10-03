parser grammar PaloAlto_virtual_router;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_virtual_router
:
    VIRTUAL_ROUTER vr_definition?
;

vr_definition
:
    name = variable
    (
        vr_interface
        | vr_protocol
        | vr_routing_table
    )?
;

vr_interface
:
    INTERFACE variable_list
;

vr_protocol
:
    PROTOCOL
    (
        vrp_bgp
        | vrp_ospf
        | vrp_rip
    )?
;

vr_routing_table
:
    ROUTING_TABLE IP STATIC_ROUTE name = variable
    (
        vrrt_admin_dist
        | vrrt_destination
        | vrrt_interface
        | vrrt_metric
        | vrrt_nexthop
    )
;

vrrt_admin_dist
:
    ADMIN_DIST distance = uint8
;

vrrt_destination
:
    DESTINATION destination = IP_PREFIX
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
    vrrtn_ip
    | vrrtn_next_vr
  )
;

vrrtn_ip
:
  IP_ADDRESS_LITERAL address = ip_address
;

vrrtn_next_vr
:
  NEXT_VR name = variable
;
