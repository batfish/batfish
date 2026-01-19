parser grammar PaloAlto_template;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

st_description
:
    DESCRIPTION description = value
;

st_variable: (VARIABLE | WORD) variable_name stv_type;

stv_type
:
    TYPE
    (
        stvt_ip_netmask
        | stvt_ip_range
    )
;

stvt_ip_netmask: IP_NETMASK ip_netmask;

stvt_ip_range: IP_RANGE_LITERAL ip_range;

variable_name: variable | TYPE;
