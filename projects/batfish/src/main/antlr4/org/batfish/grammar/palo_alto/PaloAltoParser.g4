parser grammar PaloAltoParser;

/* This is only needed if parser grammar is spread across files */
import
    PaloAlto_address,
    PaloAlto_address_group,
    PaloAlto_application,
    PaloAlto_application_filter,
    PaloAlto_bgp,
    PaloAlto_common,
    PaloAlto_deviceconfig,
    PaloAlto_device_group,
    PaloAlto_interface,
    PaloAlto_network,
    PaloAlto_ospf,
    PaloAlto_rip,
    PaloAlto_rulebase,
    PaloAlto_service,
    PaloAlto_service_group,
    PaloAlto_shared,
    PaloAlto_tag,
    PaloAlto_virtual_router,
    PaloAlto_vsys,
    PaloAlto_zone;

options {
    superClass = 'org.batfish.grammar.BatfishParser';
    tokenVocab = PaloAltoLexer;
}

palo_alto_configuration
:
    (
        set_line
        /* TODO: delete line, etc. */
        | newline
    )+ EOF
;

newline
:
   NEWLINE
;

s_null
:
    (
        MGT_CONFIG
    )
    null_rest_of_line
;

/*
 * The distinction between config device and general config statements is needed in order to handle
 * syntax differences in filesystem-style config dumps
 */
set_line_config_devices
:
    (CONFIG DEVICES name = variable)? statement_config_devices
;

set_line_config_general
:
    CONFIG? statement_config_general
;

/*
 * These are settings that show up on the device under /config/devices/<DEV>/...
 */
statement_config_devices
:
    s_address
    | s_address_group
    | s_application
    | s_application_group
    | s_deviceconfig
    | s_network
    | s_null
    | s_rulebase
    | s_service
    | s_service_group
    | s_tag
    | s_vsys
    | s_zone
;

/*
 * These are settings that show up on the device under /config/... (NOT under the devices/ dir)
 */
statement_config_general
:
    s_null
    | s_shared
;

set_line
:
    SET set_line_tail NEWLINE
;

set_line_template
:
// TODO: do we need this if we have the applied template from other show commands?
    TEMPLATE null_rest_of_line
;

set_line_device_group
:
    DEVICE_GROUP name = variable statement_device_group
;

/*
 * Device-group supports a subset of device configuration (statement_config_devices)
 * plus a couple device-group / panorama specific items
 */
statement_device_group
:
    // Shared with statement_config_devices
    s_address
    | s_address_group
    | s_application
    | s_application_group
    | s_service
    | s_service_group
    | s_tag
    // Device-group / panorama specific
    | panorama_post_rulebase
    | panorama_pre_rulebase
    | sdg_description
    | sdg_devices
;

set_line_tail
:
    set_line_config_devices
    | set_line_config_general
    | set_line_device_group
    | set_line_template
    | s_policy
;

s_policy
:
    POLICY
    (
        s_policy_panorama
        | s_policy_shared
    )
;

s_policy_panorama
:
    PANORAMA
    (
        ss_common
        | panorama_post_rulebase
        | panorama_pre_rulebase
    )
;

s_policy_shared
:
    SHARED /* TODO */
;

panorama_post_rulebase
:
    POST_RULEBASE rulebase_inner
;

panorama_pre_rulebase
:
    PRE_RULEBASE rulebase_inner
;
