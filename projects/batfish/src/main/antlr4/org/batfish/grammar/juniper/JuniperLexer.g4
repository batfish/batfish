lexer grammar JuniperLexer;

options {
   superClass = 'org.batfish.grammar.juniper.parsing.JuniperBaseLexer';
}

ACTIVE: 'active:';
DELETE: 'delete:';
INACTIVE: 'inactive:';
REPLACE: 'replace:';

CLOSE_BRACE: '}';
CLOSE_BRACKET: ']';
CLOSE_PAREN: ')';

OPEN_BRACE: '{';
OPEN_BRACKET: '[';
OPEN_PAREN: '(';

// The start of a flat line
START_FLAT_LINE
:
  F_WhitespaceChar* ('activate'|'deactivate'|'delete'|'insert'|'set') F_WhitespaceChar+
  {lastTokenType() == -1 || lastTokenType() == NEWLINE}? -> pushMode(M_FLAT_LINE)
;

// Handle Juniper-style and RANCID-header-style line comments, as well as end-of-line comments
COMMENT_LINE: F_WhitespaceChar* [!#] F_NonNewlineChar* -> skip;

MULTILINE_COMMENT
:
  '/*' .*? '*/'
  {
    switch(lastTokenType()) {
      case -1:
      case NEWLINE:
        break;
      default:
        skip();
    }
  }
;

PROMPT: '{master:0}' -> channel(HIDDEN);

SEMICOLON
:
   F_OMITTED? ';' F_SECRET_DATA?
;

WORD
:
   F_QuotedString
   | F_ParenString
   | F_WordChar+
;

NEWLINE: F_NewlineChar+ -> channel(HIDDEN);

WS
:
   F_WhitespaceChar+ -> skip // so not counted as last token
;

mode M_FLAT_LINE;

M_FLAT_LINE_WORD
:
   (
      F_QuotedString
      | F_ParenString
      | F_WordChar+
   ) -> type(WORD)
;

M_FLAT_LINE_NEWLINE
:
   F_NewlineChar -> type(NEWLINE), popMode
;

M_FLAT_LINE_WS
:
   F_WhitespaceChar+ -> skip
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
F_ParenString
:
   '(' ~')'* ')'
;

fragment
F_QuotedString
:
   '"' ~["\r\n]* '"'
;

// This may appear before a semicolon if settings are present to hide certain secrets.
// For example: set system login apply-flags omit
F_OMITTED: '{ /* OMITTED */ }';

// This may appear after a semicolon when there is a secret key in the file
// Search for examples online.
fragment
F_SECRET_DATA
:
   ' '* '## SECRET-DATA'
;

fragment
F_WhitespaceChar
:
   [ \t\u000C]
;

fragment
F_WordChar
:
   ~[ \t\u000C\r\n;{}[\]"#()]
;
