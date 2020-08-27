parser grammar PaloAlto_readonly;

import PaloAlto_common;

sresp_result
:
    RESULT srespr_devices
;

srespr_devices
:
    DEVICES name = variable
    (
        sresprd_hostname
        // null rule must come last
        | null_rest_of_line
    )
;

sresprd_hostname
:
    HOSTNAME hostname = variable
;
