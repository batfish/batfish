parser grammar PaloAlto_vsys;

import PaloAlto_address, PaloAlto_common, PaloAlto_group_mapping, PaloAlto_rulebase, PaloAlto_shared, PaloAlto_zone;

options {
    tokenVocab = PaloAltoLexer;
}

s_vsys
:
    VSYS s_vsys_definition?
;

s_vsys_definition
:
    name = variable
    (
        s_rulebase
        | s_zone
        | ss_common
        | sv_alg
        | sv_display_name
        | sv_dos_protection
        | sv_email_scheduler
        | sv_import
        | sv_local_user_database
        | sv_qos
        | sv_redistribution_agent
        | sv_region
        | sv_reports
        | sv_setting
        | sv_threats
        | sv_user_id_agent
        | svg_group_mapping
    )*
;

sv_region
:
    REGION variable
;

sv_user_id_agent
:
    USER_ID_AGENT null_rest_of_line
;

sv_alg
:
    ALG null_rest_of_line
;

sv_qos
:
    QOS null_rest_of_line
;

sv_threats
:
    THREATS null_rest_of_line
;

sv_dos_protection
:
    DOS_PROTECTION null_rest_of_line
;

sv_email_scheduler
:
    EMAIL_SCHEDULER null_rest_of_line
;

sv_reports
:
    REPORTS null_rest_of_line
;

sv_local_user_database
:
    LOCAL_USER_DATABASE null_rest_of_line
;

sv_redistribution_agent
:
    REDISTRIBUTION_AGENT null_rest_of_line
;

sv_display_name
:
    DISPLAY_NAME variable
;

sv_setting
:
    SETTING null_rest_of_line
;

sv_import
:
    IMPORT
    (
        svi_network
        | svi_visible_vsys
    )?
;

svi_network
:
    NETWORK
    (
        svin_interface
        | svin_virtual_router
    )?
;

svi_visible_vsys
:
    VISIBLE_VSYS variable_list?
;

svin_interface
:
    INTERFACE variable_list?
;

svin_virtual_router
:
    VIRTUAL_ROUTER variable_list?
;