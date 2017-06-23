lexer grammar VyosLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
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

CLOSE_PAREN
:
   ')'
;

LINE_COMMENT
:
   '#' F_NonNewlineChar* F_NewlineChar+ -> channel(HIDDEN)
;

MULTILINE_COMMENT
:
   '/*' .*? '*/' -> channel(HIDDEN)
;

NEWLINE
:
   F_NewlineChar+
;

OPEN_BRACE
:
   '{'
;

OPEN_PAREN
:
   '('
;

SEMICOLON
:
   ';'
;

WORD
:
   F_QuotedString
   | F_ParenString
   | F_WordChar+
;

WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
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
   '"' ~'"'* '"'
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
