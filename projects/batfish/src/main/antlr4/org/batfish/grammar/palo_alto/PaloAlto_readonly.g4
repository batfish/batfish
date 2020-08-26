parser grammar PaloAlto_readonly;

import
    PaloAlto_common,
    PaloAlto_device_group;

s_readonly
:
    DEVICES name = variable sro_statement
;

sro_statement
:
    sro_device_group
    // null rule must come last
    | null_rest_of_line
;

sro_device_group
:
    DEVICE_GROUP name = variable
    (
        sdg_parent_dg
        // null rule must come last
        | null_rest_of_line
    )
;
