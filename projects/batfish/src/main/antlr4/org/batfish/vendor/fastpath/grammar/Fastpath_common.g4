parser grammar Fastpath_common;

options {
  tokenVocab = FastpathLexer;
}

quoted_text
:
  QUOTED_TEXT
;

double_quoted_string
:
  DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE
;

word_content
:
  (
    double_quoted_string
    | WORD
  )+
;

word
:
  WORD_SEPARATOR word_content
;

hostname
:
  word
;

host_value
:
  double_quoted_string
  | ip_address
;

ip_address
:
  IP_ADDRESS
;

uint16
:
  UINT8
  | UINT16
;
