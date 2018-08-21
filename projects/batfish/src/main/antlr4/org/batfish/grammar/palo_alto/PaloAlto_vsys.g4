parser grammar PaloAlto_vsys;

import PaloAlto_common, PaloAlto_rulebase, PaloAlto_shared, PaloAlto_zone;

options {
    tokenVocab = PaloAltoLexer;
}

s_vsys
:
    VSYS name = variable
    (
        s_rulebase
        | s_zone
        | ss_common
    )
;
