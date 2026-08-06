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
  word_content
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

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

source_interface
:
  LOOPBACK uint16
  | SERVICEPORT
  | TUNNEL uint16
  | VLAN uint16
  | interface_slot_port
;

interface_slot_port
:
  uint16
  (
    FORWARD_SLASH uint16
  )+
;
