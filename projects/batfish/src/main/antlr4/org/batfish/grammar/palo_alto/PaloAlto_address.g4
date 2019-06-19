parser grammar PaloAlto_address;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_address
:
    ADDRESS s_address_definition?
;

s_address_definition
:
    name = variable
    (
        sa_description
        | sa_fqdn
        | sa_ip_netmask
        | sa_ip_range
        | sa_null
    )?
;

sa_description
:
    DESCRIPTION description = variable
;

sa_fqdn
:
    FQDN
    null_rest_of_line
;

sa_ip_netmask
:
    IP_NETMASK
    (
        IP_ADDRESS
        | IP_PREFIX
    )

;

sa_ip_range
:
    IP_RANGE_LITERAL IP_RANGE
;

sa_null
:
    TAG
    null_rest_of_line
;
