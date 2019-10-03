parser grammar PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

bgp_asn
:
// 1-4294967295
    DEC
;

null_rest_of_line
:
    ~NEWLINE*
;

interface_address
:
    IP_ADDRESS
    | IP_PREFIX
;

ip_address
:
    IP_ADDRESS
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
        any = ANY
        | address = ip_address
        | prefix = IP_PREFIX
        | range = IP_RANGE
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

yes_or_no
:
    YES | NO
;