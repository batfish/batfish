parser grammar CumulusFrr_ip_community_list;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ip_community_list
:
  COMMUNITY_LIST icl_standard
;

icl_standard
:
  EXPANDED name = ip_community_list_name
  action = line_action communities += literal_standard_community+ NEWLINE
;
