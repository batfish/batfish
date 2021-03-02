parser grammar Fortios_common;

options {
  tokenVocab = FortiosLexer;
}

double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;

single_quoted_string: SINGLE_QUOTE text = quoted_text? SINGLE_QUOTE;

quoted_text: QUOTED_TEXT+;

ip_address: IP_ADDRESS | SUBNET_MASK;

subnet_mask: SUBNET_MASK;

ip_prefix: IP_PREFIX;

ip_address_or_prefix: ip_address | ip_prefix;

ipv6_address: IPV6_ADDRESS;

ipv6_prefix: IPV6_PREFIX;

mac_address_literal: MAC_ADDRESS_LITERAL;

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

// can include whitespace, newlines, html tags, etc.
str: (double_quoted_string | single_quoted_string | UNQUOTED_WORD_CHARS)*;

enabled_or_disabled: ENABLED | DISABLED;
