parser grammar CumulusInterfaces_common;

options {
  tokenVocab = CumulusInterfacesLexer;
}

interface_name
:
  WORD
;