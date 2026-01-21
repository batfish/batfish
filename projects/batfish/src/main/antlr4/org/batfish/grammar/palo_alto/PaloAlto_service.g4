parser grammar PaloAlto_service;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_service
:
    'service' s_service_definition?
;

s_service_definition
:
    name = variable
    (
        sserv_description
        | sserv_port
        | sserv_protocol
        | sserv_source_port
        | sserv_tag
    )*
;

sserv_description
:
    DESCRIPTION description = value
;

sserv_port
:
    PORT variable_port_list
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
    SOURCE_PORT variable_port_list
;

sserv_tag
:
    TAG tags = variable_list
;
