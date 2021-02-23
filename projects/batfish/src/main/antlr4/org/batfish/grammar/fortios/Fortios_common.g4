parser grammar Fortios_common;

options {
  tokenVocab = FortiosLexer;
}

double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;

quoted_text: QUOTED_TEXT+;

ip_address: IP_ADDRESS | SUBNET_MASK;

subnet_mask: SUBNET_MASK;

ip_prefix: IP_PREFIX;

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

