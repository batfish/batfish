parser grammar PaloAlto_interface;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_interface
:
    INTERFACE
    (
        sni_ethernet
    )
;

sni_ethernet
:
    ETHERNET name = variable
    (
        snie_comment
        | snie_layer3
        | snie_link_state
    )
;

snie_comment
:
    COMMENT text = variable
;

snie_layer3
:
    LAYER3
    (
        sniel3_common
        | sniel3_units
    )
;

snie_link_state
:
    LINK_STATE
    (
        AUTO
        | DOWN
        | UP
    )
;

// Common syntax between layer3 interfaces and subinterfaces (units)
sniel3_common
:
    (
        sniel3_ip
        | sniel3_mtu
        | sniel3_null
    )
;

sniel3_ip
:
    IP address =
    (
        IP_PREFIX
        | IP_ADDRESS
    )
;

sniel3_mtu
:
    MTU mtu = DEC
;

sniel3_null
:
    (
        LLDP
        | IPV6
        | NDP_PROXY
    )
    null_rest_of_line
;

sniel3_units
:
    UNITS name = variable
    (
        sniel3_common
        | sniel3u_tag
    )
;

sniel3u_tag
:
    TAG tag = DEC
;