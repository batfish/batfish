parser grammar PaloAlto_application;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_application
:
    APPLICATION name = variable
    (
        sapp_description
    )? // a line without the tail, which just defined the application object, is legal
;

sapp_description
:
    DESCRIPTION description = variable
;
