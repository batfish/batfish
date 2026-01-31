parser grammar Fortios_common;

options {
  tokenVocab = FortiosLexer;
}

double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;

single_quoted_string: SINGLE_QUOTE text = quoted_text? SINGLE_QUOTE;

// 1-35 characters
access_list_or_prefix_list_name: access_list_name;

// 1-35 characters
access_list_name: str;

// 1-79 characters
address_name: str;

// 1-35 characters
ippool_name: str;

// 1-35 characters
route_map_name: str;

address_names: address_name+;

// 1-15 characters for interface, but 1-35 for zone
interface_or_zone_name: str;

// Up to 15 characters
interface_name: str;

interface_names: interface_name+;

// Up to 79 characters
service_name: str;

service_names: service_name+;

// Up to 63 characters
tagging_name: str;

quoted_text: QUOTED_TEXT+;

ip_address: IP_ADDRESS | SUBNET_MASK;

subnet_mask: SUBNET_MASK;

ip_prefix: IP_PREFIX;

ip_address_with_mask_or_prefix
:
    ip_address subnet_mask
    | ip_prefix
;

ip_address_with_mask_or_prefix_or_any: ip_address_with_mask_or_prefix | ANY;

// For ip_address_with_mask_or_prefix contexts where the mask can be invalid
ip_address_with_maybe_invalid_mask_or_prefix
:
    ip = ip_address mask = ip_address
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

// General, catch-all rule for unimplemented edit blocks
unimplemented_edit_stanza
:
    (
        SET
        | UNSET
        | SELECT
        | UNSELECT
        | APPEND
        | CLEAR
    ) unimplemented
;

// This must be a separate rule from unimplemented_edit_stanza, since its parent context is lost on recovery
// This should only be used directly from unimplemented_edit_stanza
unimplemented: UNIMPLEMENTED_PLACEHOLDER newline;

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

permit_or_deny: PERMIT | DENY;

// 1-4094
vlanid: uint16;

after_or_before: AFTER | BEFORE;

// 1-63 characters
internet_service_name: str;

// 0-4294967295
internet_service_id: str;
