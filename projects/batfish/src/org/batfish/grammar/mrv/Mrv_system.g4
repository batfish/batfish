parser grammar Mrv_system;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

system_systemname
:
   SYSTEM PERIOD SYSTEMNAME
;