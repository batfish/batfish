parser grammar CheckPointGateway_common;

options {
    tokenVocab = CheckPointGatewayLexer;
}

quoted_text: QUOTED_TEXT;
double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;
single_quoted_string: SINGLE_QUOTE text = quoted_text? SINGLE_QUOTE;
word_content: (double_quoted_string | single_quoted_string | WORD)+;
word: STR_SEPARATOR word_content;
hostname: word;
interface_name: word;
// Alphanum, comma, period, space
static_route_comment: word;

null_rest_of_line: ~NEWLINE* NEWLINE;

on_or_off: ON | OFF;

ip_address: IP_ADDRESS;
ip_prefix: IP_PREFIX;
subnet_mask: IP_ADDRESS;

uint8: UINT8;
ip_mask_length: uint8;
// 1-8
static_route_priority: uint8;

uint16: UINT8 | UINT16;
// 0-1024
bonding_group_number: uint16;
// 68-16000 for ipv4
mtu: uint16;

uint32: UINT8 | UINT16 | UINT32;
