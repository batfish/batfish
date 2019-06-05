parser grammar PaloAlto_zone;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_zone
:
    ZONE s_zone_definition?
;

s_zone_definition
:
    name = variable
    (
        sz_network
    )?
;

sz_network
:
    NETWORK
    (
        szn_external
        | szn_layer2
        | szn_layer3
        | szn_tap
        | szn_virtual_wire
    )?
;

szn_external
:
    EXTERNAL variable_list?
;

szn_layer2
:
    LAYER2 variable_list?
;

szn_layer3
:
    LAYER3 variable_list?
;

szn_tap
:
    TAP variable_list?
;

szn_virtual_wire
:
    VIRTUAL_WIRE variable_list?
;
