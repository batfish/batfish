parser grammar PaloAlto_bgp;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

bgp_local_pref
:
// 0-4294967295
   uint32
;

vrp_bgp
:
    BGP
    (
        bgp_enable
        | bgp_install_route
        | bgp_local_as
        | bgp_null
        | bgp_policy
        | bgp_reject_default_route
        | bgp_router_id
        | bgp_routing_options
    )
;

bgp_enable
:
    ENABLE yn = yes_or_no
;

bgp_install_route
:
    INSTALL_ROUTE yn = yes_or_no
;

bgp_local_as
:
    LOCAL_AS asn = bgp_asn
;

bgp_null
:
    DAMPENING_PROFILE
    null_rest_of_line
;

bgp_policy
:
    POLICY
    (
        bgpp_export
        | bgpp_import
    )
;

bgpp_export
:
    EXPORT
;

bgpp_import
:
    IMPORT
;

bgp_reject_default_route
:
    REJECT_DEFAULT_ROUTE yn = yes_or_no
;

bgp_router_id
:
    ROUTER_ID address = interface_address
;

bgp_routing_options
:
    ROUTING_OPTIONS
    (
        bgpro_aggregate
        | bgpro_as_format
        | bgpro_default_local_preference
        | bgpro_graceful_restart
        | bgpro_med
        | bgpro_reflector_cluster_id
    )
;

bgpro_aggregate
:
    AGGREGATE
    (
        bgproa_aggregate_med
    )
;

bgproa_aggregate_med
:
    AGGREGATE_MED yn = yes_or_no
;

bgpro_as_format
:
    AS_FORMAT (TWO_BYTE | FOUR_BYTE)
;

bgpro_default_local_preference
:
    DEFAULT_LOCAL_PREFERENCE pref = bgp_local_pref
;

bgpro_graceful_restart
:
    GRACEFUL_RESTART
    (
        bgprog_enable
    )
;

bgprog_enable
:
    ENABLE yn = yes_or_no
;

bgpro_med
:
    MED
    (
        bgprom_always_compare_med
        | bgprom_deterministic_med_comparison
    )
;

bgprom_always_compare_med
:
    ALWAYS_COMPARE_MED yn = yes_or_no
;

bgprom_deterministic_med_comparison
:
    DETERMINISTIC_MED_COMPARISON yn = yes_or_no
;

bgpro_reflector_cluster_id
:
    REFLECTOR_CLUSTER_ID id = ip_address
;