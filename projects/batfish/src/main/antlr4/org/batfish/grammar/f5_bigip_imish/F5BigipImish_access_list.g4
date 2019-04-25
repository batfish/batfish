parser grammar F5BigipImish_access_list;

import F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

ip_spec
:
  ANY
  | prefix = ip_prefix
;

s_access_list
:
  ACCESS_LIST name = word action = line_action src = ip_spec NEWLINE
;
