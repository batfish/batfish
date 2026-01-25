parser grammar PaloAlto_application_override;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sr_application_override
:
    APPLICATION_OVERRIDE sr_ao_rules?
;

sr_ao_rules
:
    RULES srao_definition+
;
srao_definition : name = variable srao_uuid?
    (
        srao_application
        | srao_description
        | srao_disabled
        | srao_negate_destination
        | srao_negate_source
        | srao_port
        | srao_protocol
        | srao_destination
        | srao_from
        | srao_source
        | srao_tag
        | srao_to
    )?;


srao_application
:
    APPLICATION application = variable
;

srao_description
:
    DESCRIPTION description = value
;

srao_disabled
:
    DISABLED yn = yes_or_no
;

srao_negate_destination
:
    NEGATE_DESTINATION yn = yes_or_no
;

srao_negate_source
:
    NEGATE_SOURCE yn = yes_or_no
;

srao_port
:
    PORT variable_port_list
;

srao_protocol
:
    PROTOCOL protocol = tcp_or_udp
;

srao_destination
:
    DESTINATION src_or_dst_list
;

srao_from
:
    FROM variable_list
;

srao_source
:
    SOURCE src_or_dst_list
;

srao_tag
:
    TAG variable_list
;

srao_to
:
    TO variable_list
;

srao_uuid : UUID
 | ~(
    APPLICATION
    | DESCRIPTION
    | DISABLED
    | NEGATE_DESTINATION
    | NEGATE_SOURCE
    | PORT
    | PROTOCOL
    | DESTINATION
    | FROM
    | SOURCE
    | TAG
    | TO
    | NEWLINE
 );
