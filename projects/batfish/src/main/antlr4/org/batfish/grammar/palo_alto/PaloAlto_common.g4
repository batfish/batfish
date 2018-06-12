parser grammar PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

null_rest_of_line
:
    ~NEWLINE* NEWLINE
;

variable
:
    ~NEWLINE
;
