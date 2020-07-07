parser grammar PaloAlto_device_group;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sdg_description
:
    DESCRIPTION description = value
;

sdg_devices
:
    DEVICES device = variable sdgd_vsys?
;

sdg_profiles
:
    PROFILES null_rest_of_line
;

sdgd_vsys
:
    VSYS name = variable
;
