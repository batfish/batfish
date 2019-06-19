parser grammar Mrv_subtemplate;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

a_subtemplate
:
   SUBTEMPLATE PERIOD
   (
      a_subtemplate_idletimeout
      | a_subtemplate_sercurityv3
      | a_subtemplate_prompt
   )
;

a_subtemplate_idletimeout
:
   IDLETIMEOUT nidecl
;

a_subtemplate_sercurityv3
:
   SECURITYV3 nidecl
;

a_subtemplate_prompt
:
   PROMPT nsdecl
;
