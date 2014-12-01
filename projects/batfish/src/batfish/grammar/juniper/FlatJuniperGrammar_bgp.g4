parser grammar FlatJuniperGrammar_bgp;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

bt_path_selection
:
   PATH_SELECTION bt_path_selection_tail
;

bt_path_selection_tail
:
   pst_always_compare_med
;

pst_always_compare_med
:
   ALWAYS_COMPARE_MED
;

s_protocols_bgp
:
   BGP s_protocols_bgp_tail
;

s_protocols_bgp_tail
:
   bt_path_selection
;