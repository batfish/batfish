parser grammar A10_common;

options {
    tokenVocab = A10Lexer;
}

quoted_text: QUOTED_TEXT;
double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;
single_quoted_string: SINGLE_QUOTE text = quoted_text? SINGLE_QUOTE;
word_content: (double_quoted_string | single_quoted_string | WORD)+;
word: WORD_SEPARATOR word_content;

hostname: word | WORD_SEPARATOR (NO_ACCESS | OPER | PARTITION_ONLY | READ | WRITE);

interface_name_str: word;

rba_role_name: word;

vlan_name: word;

ip_prefix: ip_address (subnet_mask | ip_slash_prefix);

ip_address: IP_ADDRESS;

ip_slash_prefix: IP_SLASH_PREFIX;

subnet_mask: SUBNET_MASK;

null_rest_of_line: ~NEWLINE* NEWLINE;

// 2-4094
vlan_number: uint16;

// 1-40? assuming 40 is max for now
ethernet_number: uint8;

// 0-10
loopback_number: uint8;

uint8: UINT8;

uint16: UINT8 | UINT16;

uint32: UINT8 | UINT16 | UINT32;
