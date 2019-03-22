parser grammar CumulusNcluParser;

/* This is only needed if parser grammar is spread across files */
import CumulusNclu_common;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = CumulusNcluLexer;
}

// goal rule

cumulus_nclu_configuration
:
  NEWLINE? statement+ NEWLINE? EOF
;

// other rules

s_null
:
  (
    (
      NET
      (
        ADD
        | COMMIT
        | DEL
      )
    )
    | SUDO
  ) null_rest_of_line
;

statement
:
  s_null
;
