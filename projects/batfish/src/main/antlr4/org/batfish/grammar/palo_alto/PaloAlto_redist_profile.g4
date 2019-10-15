parser grammar PaloAlto_redist_profile;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

vrp_redist_profile
:
    REDIST_PROFILE name = variable
    (

    )
;




