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
        | sni_loopback
        | sni_tunnel
        | sni_vlan
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

/* tag command can be run on almost any unit's l2 or l3 config. */
if_tag
:
    TAG tag = DEC
;

sni_ethernet
:
    ETHERNET sni_ethernet_definition?
;

sni_ethernet_definition
:
    name = variable
    (
        if_common
        | snie_layer2
        | snie_layer3
        | snie_link_state
        | snie_tap
        | snie_virtual_wire
    )?
;

sni_loopback
:
    LOOPBACK
    (
        if_common
        | snil_units
    )?
;

sni_tunnel
:
    TUNNEL (
        if_common
        | snit_units
    )?
;

sni_vlan
:
    VLAN
    (
        if_common
        | sniv_units
    )?
;

snie_layer2
:
    LAYER2
    (
        sniel2_units
    )?
;

snie_layer3
:
    LAYER3
    (
        sniel3_common
        | sniel3_units
    )?
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

snie_tap
:
    TAP
;

snie_virtual_wire
:
    VIRTUAL_WIRE
;

sniel2_unit
:
    name = variable
    (
        if_common
        | if_tag
    )
;

sniel2_units
:
    UNITS sniel2_unit?
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
        | if_tag
    )
;

sniel3_units
:
    UNITS sniel3_unit?
;

snil_unit
:
    name = variable
    (
        if_common
    )?
;

snil_units
:
    UNITS snil_unit?
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

sniv_unit
:
    name = variable
    (
        if_common
    )?
;

sniv_units
:
    UNITS sniv_unit?
;
