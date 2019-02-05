parser grammar F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

bracket_list
:
  BRACKET_LEFT
  (
    u_word
    | u_word_list
  )+ BRACKET_RIGHT
;

empty_list
:
  BRACE_LEFT BRACE_RIGHT
;

list
:
  empty_list
  | word_list
  | u_list
;

/* An unrecognized fragment of syntax. When used, MUST be LAST alternative */
u
:
  (
    (
      IF u_word_list
    )
    | u_word+
  ) list? NEWLINE
;

u_list
:
  BRACE_LEFT NEWLINE u+ BRACE_RIGHT
;

u_word
:
  bracket_list
  | word
;

u_word_list
:
  BRACE_LEFT u_word+ BRACE_RIGHT
;

word
:
  ~( BRACE_LEFT | BRACE_RIGHT | BRACKET_LEFT | BRACKET_RIGHT | NEWLINE )
;

word_list
:
  BRACE_LEFT word+ BRACE_RIGHT
;
