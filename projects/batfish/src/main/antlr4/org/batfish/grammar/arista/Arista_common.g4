parser grammar Arista_common;

options {
   tokenVocab = AristaLexer;
}

interface_address
:
  ip = IP_ADDRESS subnet = IP_ADDRESS
  | prefix = IP_PREFIX
;