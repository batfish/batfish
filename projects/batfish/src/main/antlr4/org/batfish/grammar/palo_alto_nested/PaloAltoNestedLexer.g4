lexer grammar PaloAltoNestedLexer;

options {
   superClass = 'org.batfish.grammar.palo_alto_nested.parsing.PaloAltoNestedBaseLexer';
}

CLOSE_BRACE
:
   '}'
;

CLOSE_BRACKET
:
   ']'
;

CLOSE_PAREN
:
   ')'
;

// Handle developer and RANCID-header-style line comments
COMMENT_LINE
:
  F_WhitespaceChar* [!#]
  {lastTokenType() == -1 || lastTokenType() == NEWLINE || lastTokenType() == SHOW_CONFIG_LINE}?
  F_NonNewlineChar* (F_NewlineChar+ | EOF)
    -> skip // so not counted as last token
;

OPEN_BRACE
:
   '{'
;

OPEN_BRACKET
:
   '['
;

OPEN_PAREN
:
   '('
;

SEMICOLON
:
   ';'
;

// Allow initial garbage for prompt, etc.
SHOW_CONFIG_LINE
:
  F_NonNewlineChar* 'show' F_WhitespaceChar+ 'config' F_NonNewlineChar* F_NewlineChar+ -> channel(HIDDEN)
;

// An HTTP log-forwarding profile stores its webhook body as a quoted string, and PAN-OS
// emits that string with its inner double quotes unescaped. F_QuotedString stops at the
// first of those, after which the body's own braces lex as OPEN_BRACE/CLOSE_BRACE and
// desynchronise config nesting for the remainder of the device -- typically leaving it with
// no interfaces at all, and no warning saying why.
//
// The body is not modeled: s_log_settings offers no http alternative, and no HTTP token
// exists in the PAN-OS grammar. So consume the whole statement, terminator included, and
// drop it. This must happen here rather than in PaloAltoNestedFlattener or the main
// grammar, both of which run after the tree has already been mis-nested.
LOG_PROFILE_PAYLOAD
:
   'payload' F_WhitespaceChar+ '"' .*? '";' -> skip
;

WORD
:
   F_QuotedString
   | F_Word
;

NEWLINE: F_NewlineChar+ -> channel(HIDDEN);

WS
:
   F_WhitespaceChar+ -> skip // so not counted as last token
;

fragment
F_NewlineChar
:
   [\r\n]
;

fragment
F_NonNewlineChar
:
   ~[\r\n]
;

fragment
F_QuotedString
:
   '"' ~'"'* '"'
;

fragment
F_WhitespaceChar
:
   [ \t\u000C]
;

fragment
F_Word
:
   F_WordStart (F_WordInteriorChar* F_WordFinalChar)?
;

F_WordFinalChar
:
// Not whitespace, not ; or } or ] as those are nested syntax.
   ~[ \t\u000C\r\n;}\]]
;

F_WordInteriorChar
:
   ~[ \t\u000C\r\n]
;

fragment
F_WordStart
:
   ~[ \t\u000C\r\n[\](){};]
;
