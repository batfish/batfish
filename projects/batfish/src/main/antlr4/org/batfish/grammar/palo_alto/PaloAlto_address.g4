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
    (name=variable | name_token=IP_NETMASK | name_token=IP_RANGE_LITERAL)
    (
        sa_description
        | sa_fqdn
        | sa_ip_netmask
        | sa_ip_range
        | sa_tag
    )?
;

sa_description
:
    DESCRIPTION description = value
;

sa_fqdn
:
    FQDN
    null_rest_of_line
;

sa_ip_netmask: IP_NETMASK ip_netmask;

sa_ip_range
:
    IP_RANGE_LITERAL ip_range
;

sa_tag
:
    TAG tags = variable_list
;
