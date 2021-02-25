lexer grammar FortiosLexer;

options {
  superClass = 'org.batfish.grammar.fortios.parsing.FortiosBaseLexer';
}

tokens {
  QUOTED_TEXT,
  UNQUOTED_ESCAPED_CHAR,
  WORD
}

// Keyword Tokens

ADMIN:
  'admin'
  {
    if (lastTokenType() == REPLACEMSG) {
      pushMode(M_Word);
    }
  }
;

ALERTMAIL: 'alertmail';
AUTH: 'auth';
BUFFER: 'buffer' -> pushMode(M_Word);
CONFIG: 'config';
END: 'end';
FORTIGUARD_WF: 'fortiguard-wf';
FTP: 'ftp';
GLOBAL: 'global';
HOSTNAME: 'hostname' -> pushMode(M_Word);
HTTP: 'http';
ICAP: 'icap';
MAIL: 'mail';
NAC_QUAR: 'nac-quar';
REPLACEMSG: 'replacemsg';
SET: 'set';
SPAM: 'spam';
SSLVPN: 'sslvpn';
SYSTEM: 'system';
TRAFFIC_QUOTA: 'traffic-quota';
UNSET: 'unset';
UTM: 'utm';
WEBPROXY: 'webproxy';

// Other Tokens

BLANK_LINE
:
  F_Whitespace* F_Newline
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_Newline* -> channel(HIDDEN)
;

COMMENT_LINE
:
  F_Whitespace* '#'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewline* (F_Newline+ | EOF) -> channel(HIDDEN)
;

DOUBLE_QUOTE: '"' -> pushMode(M_DoubleQuote);

SUBNET_MASK
:
  F_SubnetMask
;

IP_ADDRESS
:
  F_IpAddress
;

IP_PREFIX
:
  F_IpPrefix
;

IPV6_ADDRESS
:
  F_Ipv6Address
;

IPV6_PREFIX
:
  F_Ipv6Prefix
;

MAC_ADDRESS_LITERAL
:
  F_MacAddress
;

NEWLINE
:
  F_Newline+
;

SINGLE_QUOTE: ['] -> pushMode(M_SingleQuote);

UINT8
:
  F_Uint8
;

UINT16
:
  F_Uint16
;

UINT32
:
  F_Uint32
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

// Fragments

fragment
F_Digit
:
  [0-9]
;

fragment
F_HexDigit
:
  [0-9A-Fa-f]
;

fragment
F_HexUint32
:
  '0x' F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_IpAddress
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
;

fragment
F_IpPrefix
:
  F_IpAddress '/' F_IpPrefixLength
;

fragment
F_IpPrefixLength
:
  F_Digit
  | [12] F_Digit
  | [3] [012]
;

fragment
F_Ipv6Address
:
  '::' F_Ipv6HexWordLE7
  | F_Ipv6HexWord '::' F_Ipv6HexWordLE6
  | F_Ipv6HexWord2 '::' F_Ipv6HexWordLE5
  | F_Ipv6HexWord3 '::' F_Ipv6HexWordLE4
  | F_Ipv6HexWord4 '::' F_Ipv6HexWordLE3
  | F_Ipv6HexWord5 '::' F_Ipv6HexWordLE2
  | F_Ipv6HexWord6 '::' F_Ipv6HexWordLE1
  | F_Ipv6HexWord7 '::'
  | F_Ipv6HexWord8
;

fragment
F_Ipv6HexWord
:
  F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_Ipv6HexWord2
:
  F_Ipv6HexWord ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord3
:
  F_Ipv6HexWord2 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord4
:
  F_Ipv6HexWord3 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord5
:
  F_Ipv6HexWord4 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord6
:
  F_Ipv6HexWord5 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord7
:
  F_Ipv6HexWord6 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord8
:
  F_Ipv6HexWord6 ':' F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordFinal2
:
  F_Ipv6HexWord2
  | F_IpAddress
;

fragment
F_Ipv6HexWordFinal3
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordFinal4
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal3
;

fragment
F_Ipv6HexWordFinal5
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal4
;

fragment
F_Ipv6HexWordFinal6
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal5
;

fragment
F_Ipv6HexWordFinal7
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal6
;

fragment
F_Ipv6HexWordLE1
:
  F_Ipv6HexWord?
;

fragment
F_Ipv6HexWordLE2
:
  F_Ipv6HexWordLE1
  | F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordLE3
:
  F_Ipv6HexWordLE2
  | F_Ipv6HexWordFinal3
;

fragment
F_Ipv6HexWordLE4
:
  F_Ipv6HexWordLE3
  | F_Ipv6HexWordFinal4
;

fragment
F_Ipv6HexWordLE5
:
  F_Ipv6HexWordLE4
  | F_Ipv6HexWordFinal5
;

fragment
F_Ipv6HexWordLE6
:
  F_Ipv6HexWordLE5
  | F_Ipv6HexWordFinal6
;

fragment
F_Ipv6HexWordLE7
:
  F_Ipv6HexWordLE6
  | F_Ipv6HexWordFinal7
;

fragment
F_Ipv6Prefix
:
  F_Ipv6Address '/' F_Ipv6PrefixLength
;

fragment
F_Ipv6PrefixLength
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' [01] F_Digit
  | '12' [0-8]
;

fragment
F_MacAddress
:
  F_HexDigit F_HexDigit F_HexDigit F_HexDigit '.'
  F_HexDigit F_HexDigit F_HexDigit F_HexDigit '.'
  F_HexDigit F_HexDigit F_HexDigit F_HexDigit
;

fragment
F_Newline
:
  '\r'? '\n'
;

fragment
F_NonNewline
:
  ~[\n\r]
;

fragment
F_NonWhitespace
:
  ~[ \t\u000C\u00A0\n\r]
;

fragment
F_PositiveDigit
:
  [1-9]
;

fragment
F_StandardCommunity
:
  F_Uint16 ':' F_Uint16
;

fragment
F_SubnetMask
:
  F_SubnetMaskOctet '.0.0.0'
  | '255.' F_SubnetMaskOctet '.0.0'
  | '255.255.' F_SubnetMaskOctet '.0'
  | '255.255.255.' F_SubnetMaskOctet
;

fragment
F_SubnetMaskOctet
:
  '0'
  | '128'
  | '192'
  | '224'
  | '240'
  | '248'
  | '252'
  | '254'
  | '255'
;

fragment
F_Uint8
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
;

fragment
F_Uint16
:
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit?
  | [1-5] F_Digit F_Digit F_Digit F_Digit
  | '6' [0-4] F_Digit F_Digit F_Digit
  | '65' [0-4] F_Digit F_Digit
  | '655' [0-2] F_Digit
  | '6553' [0-5]
;

fragment
F_Uint32
:
// 0-4294967295
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit? F_Digit? F_Digit? F_Digit?
  F_Digit? F_Digit?
  | [1-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  F_Digit
  | '4' [0-1] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '42' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '429' [0-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '4294' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit
  | '42949' [0-5] F_Digit F_Digit F_Digit F_Digit
  | '429496' [0-6] F_Digit F_Digit F_Digit
  | '4294967' [0-1] F_Digit F_Digit
  | '42949672' [0-8] F_Digit
  | '429496729' [0-5]
;

fragment
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

fragment
F_LineContinuation: '\\' F_Newline;

fragment
F_UnquotedEscapedChar: '\\' ~[\n];

fragment
F_QuotedEscapedChar: '\\' ["'\\];

fragment
F_WordChar: ~[ \t\u000C\u00A0\r\n#()<>?'"\\];

mode M_DoubleQuote;

M_DoubleQuote_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;

M_DoubleQuote_QUOTED_TEXT: (F_QuotedEscapedChar | ~'"')+ -> type(QUOTED_TEXT);

mode M_SingleQuote;

M_SingleQuote_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), popMode;

M_SingleQuote_QUOTED_TEXT: ~[']+ -> type(QUOTED_TEXT);

mode M_Word;

M_WordWs_NEWLINE: F_Newline+ -> type(NEWLINE), popMode;

M_WordWs_WS: F_Whitespace+ -> skip, mode(M_Word2);

mode M_Word2;

M_Word_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);

M_Word_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);

M_Word_LINE_CONTINUATION: F_LineContinuation -> skip;

M_Word_UNQUOTED_ESCAPED_CHAR: F_UnquotedEscapedChar -> type(UNQUOTED_ESCAPED_CHAR);

M_Word_WORD: F_WordChar+ -> type(WORD);

M_Word_WS: F_Whitespace+ -> skip, popMode;

M_Word_NEWLINE: F_Newline+ -> type(NEWLINE), popMode;
