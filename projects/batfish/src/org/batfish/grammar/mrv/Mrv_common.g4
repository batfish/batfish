parser grammar Mrv_common;

options {
   tokenVocab = MrvLexer;
}

type
:
   BOOL
;

type_declaration
:
   TYPE type
;