parser grammar PaloAlto_service;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_service
:
    SERVICE name = variable
    (
        sserv_description
        | sserv_port
        | sserv_protocol
        | sserv_source_port
    )+
;

sserv_description
:
    DESCRIPTION description = variable
;

sserv_port
:
    PORT variable_comma_separated_dec
;

sserv_protocol
:
    PROTOCOL
    (
        SCTP
        | TCP
        | UDP
    )
;

sserv_source_port
:
    SOURCE_PORT variable_comma_separated_dec
;
