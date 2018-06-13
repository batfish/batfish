parser grammar PaloAltoParser;

/* This is only needed if parser grammar is spread across files */
import
PaloAlto_common, PaloAlto_deviceconfig, PaloAlto_network, PaloAlto_shared;

options {
    superClass = 'org.batfish.grammar.BatfishParser';
    tokenVocab = PaloAltoLexer;
}

palo_alto_configuration
:
    NEWLINE?
    (
        set_line
    )+ NEWLINE? EOF
;

set_line
:
    SET statement NEWLINE
;

statement
:
    s_deviceconfig
    | s_network
    | s_shared
;
