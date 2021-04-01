parser grammar Asa_community_list;

import Asa_common;

options {
   tokenVocab = AsaLexer;
}

community_list_expanded
:
   COMMUNITY_LIST EXPANDED name = community_list_name ala = access_list_action regex=community_regex NEWLINE
;

community_list_standard
:
   COMMUNITY_LIST STANDARD name = community_list_name ala = access_list_action communities += standard_community+ NEWLINE
;

community_list_name: WORD;

community_regex: COMMUNITY_REGEX;