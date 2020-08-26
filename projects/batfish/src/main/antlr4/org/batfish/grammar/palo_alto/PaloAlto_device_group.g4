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

sdg_parent_dg
:
    PARENT_DG name = variable
;

sdg_profile_group
:
    PROFILE_GROUP null_rest_of_line
;

sdg_profiles
:
    PROFILES null_rest_of_line
;

sdgd_vsys
:
    VSYS name = variable
;
