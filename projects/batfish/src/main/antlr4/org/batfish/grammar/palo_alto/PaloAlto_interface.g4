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
        | sni_tunnel
    )
;

/* A set of commands you can run on ANY interface, top-level or unit. */
if_common
:
    if_comment
;

if_comment
:
    COMMENT text = variable
;

sni_ethernet
:
    ETHERNET name = variable
    (
        if_common
        | snie_layer3
        | snie_link_state
    )
;

sni_tunnel
:
    TUNNEL snit_units?
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

sniel3_unit
:
    name = variable
    (
        if_common
        | sniel3_common
        | sniel3u_tag
    )
;

sniel3_units
:
    UNITS sniel3_unit?
;

sniel3u_tag
:
    TAG tag = DEC
;

snit_unit
:
    name = variable
    (
        if_common
    )?
;

snit_units
:
    UNITS snit_unit?
;