parser grammar PaloAlto_template;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

st_description
:
    DESCRIPTION description = value
;
