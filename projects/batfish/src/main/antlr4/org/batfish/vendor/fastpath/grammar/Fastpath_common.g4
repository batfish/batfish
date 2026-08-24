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

word
:
  double_quoted_string
  | WORD
;

hostname
:
  word
;

host_value
:
  ip_address
  | word
;

ip_address
:
  IP_ADDRESS
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

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

interface_name
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
