parser grammar PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

bgp_asn
:
// 1-4294967295
    uint32
;

null_rest_of_line
:
    ~NEWLINE*
;

interface_address
:
    addr = ip_address
    | addr_with_mask = IP_PREFIX
;

ip_address
:
    IP_ADDRESS
;

ip_address_or_slash32
:
    addr = interface_address
;

ip_prefix
:
    IP_PREFIX
;

ip_prefix_list
:
    (
        ip_prefix
        |
        (
            OPEN_BRACKET
            (
                ip_prefix
            )*
            CLOSE_BRACKET
        )
    )
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

port_number
:
   uint16
;

port_or_range
:
    range = RANGE
    | port = port_number
;

uint8
:
    UINT8
;

uint16
:
    UINT8
    | UINT16
;

uint32
:
    UINT8
    | UINT16
    | UINT32
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

vlan_tag
:
// 1-4094
    uint16
;

yes_or_no
:
    YES | NO
;