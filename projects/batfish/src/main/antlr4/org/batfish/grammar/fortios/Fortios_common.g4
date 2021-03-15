parser grammar Fortios_common;

options {
  tokenVocab = FortiosLexer;
}

double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;

single_quoted_string: SINGLE_QUOTE text = quoted_text? SINGLE_QUOTE;

// 1-15 characters for interface, but 1-35 for zone
interface_or_zone_name: str;

interface_names: interface_name+;

quoted_text: QUOTED_TEXT+;

ip_address: IP_ADDRESS | SUBNET_MASK;

subnet_mask: SUBNET_MASK;

ip_prefix: IP_PREFIX;

ip_address_with_mask_or_prefix
:
    ip_address subnet_mask
    | ip_prefix
;

ip_wildcard
:
    ip = ip_address mask = ip_address
;

ipv6_address: IPV6_ADDRESS;

ipv6_prefix: IPV6_PREFIX;

mac_address_literal: MAC_ADDRESS_LITERAL;

newline: STR_SEPARATOR? NEWLINE;

null_rest_of_line: ~NEWLINE* NEWLINE;

uint8: UINT8;

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

// extractor should disallow whitespace, newlines
word: str;

str: STR_SEPARATOR str_content;

// can include whitespace, newlines, html tags, etc.
str_content: (double_quoted_string | single_quoted_string | UNQUOTED_WORD_CHARS)+;

enable_or_disable: ENABLE | DISABLE;

up_or_down: UP | DOWN;

port_range: port_low = uint16 (HYPHEN port_high = uint16)?;

allow_or_deny: ALLOW | DENY;
