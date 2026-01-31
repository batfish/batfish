parser grammar PaloAlto_rip;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

vrp_rip
:
    RIP
    (
        rip_enable
        | rip_null
    )
;

rip_enable
:
    ENABLE yn = yes_or_no
;

rip_null
:
    null_rest_of_line
;
