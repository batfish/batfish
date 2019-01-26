parser grammar PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

null_rest_of_line
:
    ~NEWLINE*
;

ip_range
:
    from = IP_ADDRESS DASH to = IP_ADDRESS
;

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
        | IP_ADDRESS
        | IP_PREFIX
        | range = ip_range
        | name = variable
    )
;

variable
:
    ~NEWLINE
;

variable_comma_separated_dec
:
    DEC
    (
        COMMA DEC
    )*
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
