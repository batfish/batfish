parser grammar A10_rba;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

s_rba: RBA sr_role;

sr_role: ROLE name = rba_role_name RBA_TAIL? NEWLINE rba_line*;

rba_line: RBA_LINE NEWLINE;

rba_role_name: word;
