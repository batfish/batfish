parser grammar PaloAlto_interface;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_interface
:
    INTERFACE
    (
        sni_aggregate_ethernet
        | sni_ethernet
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
    TAG tag = vlan_tag
;

sni_aggregate_ethernet
:
    AGGREGATE_ETHERNET sni_aggregate_ethernet_definition?
;

sni_aggregate_ethernet_definition
:
    name = variable
    (
        if_common
        | snie_lacp
        | snie_layer2
        | snie_layer3
        | snie_virtual_wire
    )?
;

snie_lacp
: LACP
    (
        sniel_enable
        | sniel_fast_null
        | sniel_high_availability
        | sniel_mode
        | sniel_passive_pre_negotiation_null
        | sniel_port_priority
        | sniel_transmission_rate_null
    )?
;

sniel_high_availability
:
    HIGH_AVAILABILITY
    (
        sniel_ha_passive_pre_negotiation_null
        | sniel_use_same_system_mac_null
    )?
;

sniel_ha_passive_pre_negotiation_null
:
    PASSIVE_PRE_NEGOTIATION yn = yes_or_no
;

sniel_use_same_system_mac_null
:
    USE_SAME_SYSTEM_MAC ENABLE yn = yes_or_no
;

sniel_mode
:
    MODE (ACTIVE | PASSIVE)
;

sniel_port_priority
:
    PORT_PRIORITY priority = uint16
;

sniel_enable
:
    ENABLE yn = yes_or_no
;

sniel_fast_null
:
    FAST
;

sniel_passive_pre_negotiation_null
:
    PASSIVE_PRE_NEGOTIATION yn = yes_or_no
;

sniel_transmission_rate_null
:
    TRANSMISSION_RATE rate = variable
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
        | snie_aggregate_group
        | snie_ha
        | snie_lacp
        | snie_layer2
        | snie_layer3
        | snie_link_duplex
        | snie_link_speed
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
        | snil_ip
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

snie_aggregate_group
:
    AGGREGATE_GROUP group = variable
;

snie_ha
:
    HA
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
        | snie_lacp
        | sniel3_units
    )?
;

snie_link_duplex
:
    LINK_DUPLEX
    (
        AUTO
        | FULL
        | HALF
    )
;

snie_link_speed
:
    LINK_SPEED
    (
        AUTO
        | fixed = uint16
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
    IP address = interface_address_or_reference
;

sniel3_mtu
:
    MTU mtu = uint32
;

sniel3_null
:
    (
        ADJUST_TCP_MSS
        | LLDP
        | IPV6
        | NDP_PROXY
        | NETFLOW_PROFILE
        | INTERFACE_MANAGEMENT_PROFILE
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

snil_ip
:
    IP address = interface_address_or_reference
;

snil_unit
:
    name = variable
    (
        if_common
        | snil_ip
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
        | sniel3_common
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
