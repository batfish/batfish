parser grammar A10_common;

options {
    tokenVocab = A10Lexer;
}

quoted_text: QUOTED_TEXT;
double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;
single_quoted_string: SINGLE_QUOTE text = quoted_text? SINGLE_QUOTE;
word_content: (double_quoted_string | single_quoted_string | WORD)+;
word: WORD_SEPARATOR word_content;

hostname: word;

interface_name_str: word;

ip_prefix: ip_address (subnet_mask | ip_slash_prefix);

ip_address: IP_ADDRESS;

ip_slash_prefix: IP_SLASH_PREFIX;

subnet_mask: SUBNET_MASK;

null_rest_of_line: ~NEWLINE* NEWLINE;

uint8: UINT8;

uint16: UINT16;

uint32: UINT32;
