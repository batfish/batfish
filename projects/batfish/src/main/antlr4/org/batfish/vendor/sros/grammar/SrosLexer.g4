lexer grammar SrosLexer;

options {
  superClass = 'SrosBaseLexer';
}

tokens {
  RIGHT_BRACKET,
  STRING
}

// BEGIN other tokens

// Whole-line comments. SR-OS `admin show configuration` output begins each metadata
// line with `#` (e.g. the TiMOS banner, "Configuration format version ...", and the
// trailing "# Finished ..." line). Only treat `#` as a comment when it begins a line.
COMMENT_LINE
:
  F_Whitespace* '#' F_NonNewline*
  (
    F_Newline
    | EOF
  )
  {lastTokenType() == NEWLINE || lastTokenType() == -1}? -> channel(HIDDEN)
;

OPEN_BRACE: '{';
CLOSE_BRACE: '}';

DOUBLE_QUOTE: '"' -> pushMode(M_DoubleQuotedString);
OPEN_BRACKET: '[' -> pushMode(M_StringList);

NEWLINE: F_Newline;

// A bare word: any run of characters that are not structural punctuation, quotes, or
// whitespace. This deliberately includes `/`, `.`, `:`, `-`, and digits so that IP
// addresses/prefixes (1.1.1.1, 1.1.1.1/32), port paths (1/1/c1/1), and the leading
// `/configure` of the flat form all lex as a single WORD. Type-level validation of
// these (parsing an IP, an integer range, etc.) happens in extraction (P4).
WORD: F_WordChar+;

WS: F_Whitespace -> channel(HIDDEN);

// END other tokens

fragment
F_Newline: [\r\n]+;

fragment
F_NonNewline: ~[\r\n];

fragment
F_Whitespace: [ \t]+;

fragment
F_WordChar: ~[ \t\r\n{}[\]"];

mode M_DoubleQuotedString;

M_DoubleQuotedString_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_DoubleQuotedString_STRING: ~["\r\n]+ -> type(STRING);
M_DoubleQuotedString_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;

mode M_StringList;

M_StringList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_StringList_WS: F_Whitespace -> channel(HIDDEN);
M_StringList_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_StringListDoubleQuotedString);
M_StringList_UNQUOTED_STRING: F_WordChar+ -> type(STRING);
M_StringList_RIGHT_BRACKET: ']' -> type(RIGHT_BRACKET), popMode;

mode M_StringListDoubleQuotedString;

M_StringListDoubleQuotedString_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_StringListDoubleQuotedString_STRING: ~["\r\n]+ -> type(STRING);
M_StringListDoubleQuotedString_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_StringList);
