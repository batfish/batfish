parser grammar Arista_aspath;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

s_ip_as_path
:
  AS_PATH
  ipap_access_list
;

ipap_access_list
:
  ACCESS_LIST
    name = WORD
    action = access_list_action
    regex = WORD
    (origin = ipap_origin)?
    NEWLINE
;

ipap_origin
:
  ANY
  | EGP
  | IGP
  | INCOMPLETE
;