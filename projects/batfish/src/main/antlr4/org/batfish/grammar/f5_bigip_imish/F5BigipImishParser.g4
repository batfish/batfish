parser grammar F5BigipImishParser;

/* This is only needed if parser grammar is spread across files */
import F5BigipImish_common, F5BigipImish_access_list, F5BigipImish_route_map;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = F5BigipImishLexer;
}

// goal rule

f5_bigip_imish_configuration
:
  NEWLINE? statement+ NEWLINE? EOF
;

// other rules

s_end
:
  END NEWLINE
;

statement
:
  s_access_list
  | s_route_map
  | s_end
;
