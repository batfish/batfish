parser grammar PaloAlto_vsys;

import PaloAlto_common, PaloAlto_rulebase, PaloAlto_shared, PaloAlto_zone;

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
        | sv_import
    )?
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