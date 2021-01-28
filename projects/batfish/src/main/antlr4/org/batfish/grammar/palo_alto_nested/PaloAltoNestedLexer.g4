lexer grammar PaloAltoNestedLexer;

options {
   superClass = 'org.batfish.grammar.palo_alto_nested.parsing.PaloAltoNestedBaseLexer';
}

@members {
boolean enableIPV6_ADDRESS = true;
boolean enableIP_ADDRESS = true;
boolean enableDEC = true;

@Override
public String printStateVariables() {
   StringBuilder sb = new StringBuilder();
   sb.append("enableIPV6_ADDRESS: " + enableIPV6_ADDRESS + "\n");
   sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
   sb.append("enableDEC: " + enableDEC + "\n");
   return sb.toString();
}

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
