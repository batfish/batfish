parser grammar A10_interface;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

s_rba: RBA sr_role;

sr_role: ROLE name = rba_role_name RBA_TAIL? NEWLINE RBA_LINE+;
