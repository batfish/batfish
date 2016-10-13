parser grammar Mrv_common;

options {
   tokenVocab = MrvLexer;
}

nbdecl
:
   PERIOD DEC TYPE T_BOOL VALUE quoted_string
;

nfdecl
:
   PERIOD DEC TYPE T_FACILITY VALUE quoted_string
;

nidecl
:
   PERIOD DEC TYPE T_INTEGER VALUE quoted_string
;

nipdecl
:
   PERIOD DEC TYPE T_IPADDR VALUE quoted_string
;

nodecl
:
   PERIOD DEC TYPE T_OCTET VALUE quoted_string
;

nosdecl
:
   PERIOD DEC TYPE T_OCTETSTRING VALUE quoted_string
;

npdecl
:
   PERIOD DEC TYPE T_PASSWORD VALUE quoted_string
;

nprdecl
:
   PERIOD DEC TYPE T_PRIORITY VALUE quoted_string
;

nsdecl
:
   PERIOD DEC TYPE T_STRING VALUE quoted_string
;

nshdecl
:
   PERIOD DEC TYPE T_SHORT VALUE quoted_string
;

nspdecl
:
   PERIOD DEC TYPE T_SPEED VALUE quoted_string
;

nssdecl
:
   PERIOD DEC TYPE T_SHORTSTRING VALUE quoted_string
;

quoted_string
:
   DOUBLE_QUOTE text = QUOTED_TEXT? DOUBLE_QUOTE
;

type
:
   T_BOOL
;

type_declaration
:
   TYPE type
;