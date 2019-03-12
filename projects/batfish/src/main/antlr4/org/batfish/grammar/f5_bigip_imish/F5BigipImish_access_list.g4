parser grammar F5BigipImish_access_list;

import F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

s_access_list
:
  action = line_action src = ip_spec NEWLINE
;

word
:
  ~NEWLINE
;