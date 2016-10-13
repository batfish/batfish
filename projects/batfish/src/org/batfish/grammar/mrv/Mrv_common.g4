parser grammar Mrv_common;

options {
   tokenVocab = MrvLexer;
}

nbdecl
:
   PERIOD DEC TYPE BOOL VALUE quoted_string
;

nfdecl
:
   PERIOD DEC TYPE FACILITY VALUE quoted_string
;

nidecl
:
   PERIOD DEC TYPE INTEGER VALUE quoted_string
;

nipdecl
:
   PERIOD DEC TYPE IPADDR VALUE quoted_string
;

nodecl
:
   PERIOD DEC TYPE OCTET VALUE quoted_string
;

nosdecl
:
   PERIOD DEC TYPE OCTETSTRING VALUE quoted_string
;

npdecl
:
   PERIOD DEC TYPE PASSWORD VALUE quoted_string
;

nprdecl
:
   PERIOD DEC TYPE PRIORITY VALUE quoted_string
;

nsdecl
:
   PERIOD DEC TYPE STRING VALUE quoted_string
;

nshdecl
:
   PERIOD DEC TYPE SHORT VALUE quoted_string
;

nssdecl
:
   PERIOD DEC TYPE SHORTSTRING VALUE quoted_string
;

quoted_string
:
   DOUBLE_QUOTE text = QUOTED_TEXT? DOUBLE_QUOTE
;

type
:
   BOOL
;

type_declaration
:
   TYPE type
;