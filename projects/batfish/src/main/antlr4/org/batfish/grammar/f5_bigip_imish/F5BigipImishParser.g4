parser grammar F5BigipImishParser;

/* This is only needed if parser grammar is spread across files */
//import F5BigipImish_;

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
  s_end
;
