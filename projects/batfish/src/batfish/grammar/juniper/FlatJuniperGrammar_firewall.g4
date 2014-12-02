parser grammar FlatJuniperGrammar_firewall;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

fwfromt_next_header
:
   NEXT_HEADER protocol
;

fwfromt_port
:
   PORT port
;

fwfromt_source_prefix_list
:
   SOURCE_PREFIX_LIST variable
;

fwfromt_tcp_flags
:
   TCP_FLAGS DOUBLE_QUOTED_STRING
;

fwft_term
:
   fwft_term_header fwft_term_tail
;

fwft_term_header
:
   TERM variable
;

fwft_term_tail
:
   fwtt_from
   | fwtt_then
;

fwt_family
:
   fwt_family_header fwt_family_tail
;

fwt_family_header
:
   FAMILY
   (
      INET
      | INET6
   )
;

fwt_family_tail
:
   fwt_filter
;

fwt_filter
:
   fwt_filter_header fwt_filter_tail
;

fwt_filter_header
:
   FILTER variable
;

fwt_filter_tail
:
   fwft_term
;

fwthent_accept
:
   ACCEPT
;

fwthent_discard
:
   DISCARD
;

fwthent_null
:
   (
      POLICER
      | SAMPLE
   ) ~NEWLINE*
;

fwtt_from
:
   FROM fwtt_from_tail
;

fwtt_from_tail
:
   fwfromt_next_header
   | fwfromt_port
   | fwfromt_source_prefix_list
   | fwfromt_tcp_flags
;

fwtt_then
:
   THEN fwtt_then_tail
;

fwtt_then_tail
:
   fwthent_accept
   | fwthent_discard
   | fwthent_null
;

s_firewall
:
   FIREWALL s_firewall_tail
;

s_firewall_tail
:
   fwt_family
;
