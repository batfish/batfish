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
    PaloAlto_profiles,
    PaloAlto_readonly,
    PaloAlto_response,
    PaloAlto_rip,
    PaloAlto_rulebase,
    PaloAlto_service,
    PaloAlto_service_group,
    PaloAlto_shared,
    PaloAlto_tag,
    PaloAlto_template,
    PaloAlto_template_stack,
    PaloAlto_virtual_router,
    PaloAlto_virtual_wire,
    PaloAlto_vlan,
    PaloAlto_vsys,
    PaloAlto_zone;

options {
    superClass = 'org.batfish.grammar.BatfishParser';
    tokenVocab = PaloAltoLexer;
}

palo_alto_configuration
:
    (
        delete_line
        | move_line
        | set_line
        | newline
    )+ EOF
;

delete_line
:
   DELETE delete_line_tail NEWLINE
;

delete_line_tail
:
   ~NEWLINE*
;

move_line: MOVE move_src move_action NEWLINE;

move_src: move_src_element+;

move_src_element: ~(NEWLINE | AFTER | BEFORE | BOTTOM | TOP);

move_action
:
    AFTER name = variable
    | BEFORE name = variable
    | BOTTOM
    | TOP
;

newline
:
   NEWLINE
;

s_null
:
    (
        LOG_COLLECTOR
        | LOG_COLLECTOR_GROUP
        | MGT_CONFIG
        | SCHEDULE
        | USER_ID_COLLECTOR
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
    | s_dynamic_user_group
    | s_deviceconfig
    | s_network
    | s_null
    | s_profiles
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

statement_template
:
    st_description
    | st_variable
    | statement_template_config
;

statement_template_config
:
    CONFIG (DEVICES name = variable)? statement_template_config_devices
;

// Templates support a small subset of device configuration (statement_config_devices)
statement_template_config_devices
:
    s_deviceconfig
    | s_network
    | s_shared
    | s_vsys
    // Ignore irrelevant syntax
    | s_null
;

statement_template_stack
:
    sts_description
    | sts_devices
    | sts_templates
;

set_line
:
    SET set_line_tail NEWLINE
;

set_line_template
:
    TEMPLATE name = variable statement_template?
;

set_line_template_stack
:
    TEMPLATE_STACK name = variable statement_template_stack
;

set_line_device_group
:
    DEVICE_GROUP name = variable statement_device_group?
;

set_line_readonly
:
    READONLY s_readonly
;

/*
 * Special-case for handling extra data from device queries (e.g. hostname information for firewalls
 * managed by a Panorama device)
 */
set_line_response
:
    RESPONSE sresp_result
;

/*
 * Device-group supports a subset of device configuration (statement_config_devices)
 * plus some device-group / panorama specific items
 */
statement_device_group
:
    // Shared with statement_config_devices
    s_address
    | s_address_group
    | s_application
    | s_application_group
    | s_profiles
    | s_service
    | s_service_group
    | s_tag
    // Device-group / panorama specific
    | s_log_settings
    | s_post_rulebase
    | s_pre_rulebase
    | sdg_description
    | sdg_devices
    | sdg_parent_dg
    | sdg_profile_group
;

set_line_tail
:
    set_line_config_devices
    | set_line_config_general
    | set_line_device_group
    | set_line_readonly
    | set_line_response
    | set_line_template
    | set_line_template_stack
    | s_import
    | s_policy
;

s_import
:
    IMPORT NETWORK INTERFACE variable_list
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
        | s_post_rulebase
        | s_pre_rulebase
    )
;

s_policy_shared
:
    SHARED /* TODO */
;
