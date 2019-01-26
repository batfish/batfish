parser grammar PaloAlto_address;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_address
:
    ADDRESS name = variable
    (
        sa_description
        | sa_fqdn
        | sa_ip_netmask
        | sa_ip_range
        | sa_null
    )? // a line without the tail, which just defined the address object, is legal
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
    IP_RANGE ip_range
;

sa_null
:
    TAG
    null_rest_of_line
;
