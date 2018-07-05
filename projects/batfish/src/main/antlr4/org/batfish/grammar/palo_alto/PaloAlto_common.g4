parser grammar PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

null_rest_of_line
:
    ~NEWLINE*
;

// TODO flesh out more predefined applications
pan_application
:
    ANY
    | DNS
    | ICMP
;


// TODO handle country codes
/*
pan_country_code
:
    US
;
*/

src_or_dst_list
:
    (
        src_or_dst_list_item
        |
        (
            OPEN_BRACKET
            (
                src_or_dst_list_item
            )*
            CLOSE_BRACKET
        )
    )
;

src_or_dst_list_item
:
    (
        ANY
        | IP_PREFIX
        // TODO handle this
        // | IP_RANGE
        // TODO handle this
        // | pan_country_code
        | name = variable
    )
;

variable
:
    ~NEWLINE
;

variable_list
:
    (
        variable_list_item
        |
        (
            OPEN_BRACKET
            (
                variable_list_item
            )*
            CLOSE_BRACKET
        )
    )
;

variable_list_item
:
    ~(
        CLOSE_BRACKET
        | NEWLINE
    )
;
