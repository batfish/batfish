parser grammar PaloAlto_ospf;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

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
        | ospf_reject_default_route
        | ospf_router_id
    )
;

ospf_area
:
    AREA addr = ip_address_or_slash32
    (
        ospfa_type
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

ospf_reject_default_route
:
    REJECT_DEFAULT_ROUTE yn = yes_or_no
;

ospf_router_id
:
    ROUTER_ID addr = ip_address_or_slash32
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



