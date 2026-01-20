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
        sz_enable_user_identification
        | sz_network
        | sz_user_acl
    )?
;

sz_network
:
    NETWORK
    (
        szn_enable_packet_buffer_protection
        | szn_external
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

szn_enable_packet_buffer_protection
:
    ENABLE_PACKET_BUFFER_PROTECTION yn = yes_or_no
;

sz_user_acl
:
    USER_ACL
    (
        szua_exclude_list
        | szua_include_list
    )?
;

szua_exclude_list
:
    EXCLUDE_LIST variable_list
;

szua_include_list
:
    INCLUDE_LIST variable_list
;

sz_enable_user_identification
:
    ENABLE_USER_IDENTIFICATION yn = yes_or_no
;
