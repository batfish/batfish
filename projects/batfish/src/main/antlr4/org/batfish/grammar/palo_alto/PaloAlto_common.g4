parser grammar PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

null_rest_of_line
:
    ~NEWLINE*
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
        | IP_RANGE
        | name = variable
    )
;

port_or_range
:
    range = RANGE
    | port = DEC
;

variable_port_list
:
    port_or_range
    (
        COMMA port_or_range
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

variable
:
    ~NEWLINE
;
