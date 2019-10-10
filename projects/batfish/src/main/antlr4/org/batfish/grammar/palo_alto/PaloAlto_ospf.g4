parser grammar PaloAlto_ospf;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

ospf_interface_dead_counts
:
// 3-20
    uint8
;

ospf_interface_hello_interval
:
// 0-3600
    uint16
;

ospf_interface_priority
:
// 0-255
    uint8
;

ospf_interface_retransmit_interval
:
// 1-3600
    uint16
;

ospf_interface_transit_delay
:
// 0-3600
    uint16
;

ospf_metric
:
// 0-255
    uint8
;



vrp_ospf
:
    OSPF
    (
        ospf_area
        | ospf_enable
        | ospf_graceful_restart
        | ospf_null
        | ospf_reject_default_route
        | ospf_router_id
    )
;

ospf_area
:
    AREA addr = ip_address_or_slash32
    (
        ospfa_interface
        | ospfa_type
    )?
;

ospfa_type
:
    TYPE
    (
        ospfat_normal
        | ospfat_nssa
        | ospfat_stub
    )
;

ospf_enable
:
    ENABLE yn = yes_or_no
;

ospf_graceful_restart
:
    GRACEFUL_RESTART
    (
        ospfgr_enable
        | ospfgr_helper_enable
        | ospfgr_strict_lsa_checking
    )
;

ospf_null
:
    (
        AUTH_PROFILE
        | GLOBAL_BFD
    )
    null_rest_of_line
;


ospf_reject_default_route
:
    REJECT_DEFAULT_ROUTE yn = yes_or_no
;

ospf_router_id
:
    ROUTER_ID addr = ip_address_or_slash32
;

ospfa_interface
:
    INTERFACE name = variable
    (
        ospfai_dead_counts
        | ospfai_enable
        | ospfai_hello_interval
        | ospfai_link_type
        | ospfai_metric
        | ospfai_null
        | ospfai_passive
        | ospfai_priority
        | ospfai_retransmit_interval
        | ospfai_transit_delay
    )
;

ospfai_dead_counts
:
    DEAD_COUNTS dead_counts = ospf_interface_dead_counts
;

ospfai_enable
:
    ENABLE yn = yes_or_no
;

ospfai_hello_interval
:
    HELLO_INTERVAL hello_interval = ospf_interface_hello_interval
;

ospfai_link_type
:
    LINK_TYPE
    (
        BROADCAST
        | P2P
        | P2MP
    )
;

ospfai_metric
:
    METRIC metric = ospf_metric
;

ospfai_null
:
    (
        AUTHENTICATION
        | BFD
        | GR_DELAY
    )
    null_rest_of_line
;

ospfai_passive
:
    PASSIVE yn = yes_or_no
;

ospfai_priority
:
    PRIORITY priority = ospf_interface_priority
;

ospfai_retransmit_interval
:
    RETRANSMIT_INTERVAL retransmit_interval = ospf_interface_retransmit_interval
;

ospfai_transit_delay
:
    TRANSIT_DELAY transit_delay = ospf_interface_transit_delay
;

ospfat_normal
:
    NORMAL
;

ospfat_nssa
:
    NSSA
    (
        ospfatn_accept_summary
        | ospfatn_default_route
    )
;

ospfat_stub
:
    STUB
    (
        ospfats_accept_summary
        | ospfats_default_route
    )
;

ospfatn_accept_summary
:
    ACCEPT_SUMMARY yn = yes_or_no
;

ospfatn_default_route
:
    DEFAULT_ROUTE
    (
        ospfatndr_advertise
        | ospfatndr_disable
    )
;

ospfatndr_advertise
:
    ADVERTISE
    (
        ospfatndra_metric
        | ospfatndra_type
    )
;

ospfatndr_disable
:
    DISABLE
;

ospfatndra_metric
:
    METRIC metric = ospf_metric
;

ospfatndra_type
:
    TYPE (EXT_1 | EXT_2)
;

ospfats_accept_summary
:
    ACCEPT_SUMMARY yn = yes_or_no
;

ospfats_default_route
:
    DEFAULT_ROUTE
    (
        ospfatsdr_advertise_metric
        | ospfatsdr_disable
    )
;

ospfatsdr_advertise_metric
:
    ADVERTISE METRIC metric = ospf_metric
;

ospfatsdr_disable
:
    DISABLE
;

ospfgr_enable
:
    ENABLE yn = yes_or_no
;

ospfgr_helper_enable
:
    HELPER_ENABLE yn = yes_or_no
;

ospfgr_strict_lsa_checking
:
    STRICT_LSA_CHECKING yn = yes_or_no
;



