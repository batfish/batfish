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
        | sserv_protocol
    )+
;

sserv_description
:
    DESCRIPTION description = variable
;

sserv_protocol
:
    PROTOCOL
    (
        SCTP
        | TCP
        | UDP
    )
    (
        sservp_port
        | sservp_source_port
    )+
;

sservp_port
:
    PORT variable_comma_separated_dec
;

sservp_source_port
:
    SOURCE_PORT variable_comma_separated_dec
;
