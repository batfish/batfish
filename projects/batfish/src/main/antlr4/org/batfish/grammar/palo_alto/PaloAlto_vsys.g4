parser grammar PaloAlto_vsys;

import PaloAlto_common, PaloAlto_shared;

options {
    tokenVocab = PaloAltoLexer;
}

s_vsys
:
    VSYS name = variable
    (
        ss_common
    )
;
