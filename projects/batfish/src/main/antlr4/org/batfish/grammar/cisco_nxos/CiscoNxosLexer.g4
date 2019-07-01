lexer grammar CiscoNxosLexer;

options {
  superClass = 'org.batfish.grammar.cisco_nxos.parsing.CiscoNxosBaseLexer';
}

tokens {
  SUBDOMAIN_NAME
}

ADDRESS
:
  'address'
;

ETHERNET
:
  [Ee] [Tt] [Hh] [Ee] [Rr] [Nn] [Ee] [Tt]
;

FEATURE
:
  'feature'
;

HOSTNAME
:
  'hostname'
  // Mode is needed so as not to interfere with interface names.
  // E.g. 'Ethernet1' should be ETHERNET UINT8 rather than SUBDOMAIN_NAME
  // May be revisited as grammar is fleshed out.
  {
    if (lastTokenType() == NEWLINE || lastTokenType() == -1) {
      pushMode(M_Hostname);
    }
  }

;

INTERFACE
:
// most common abbreviation
  'int'
  (
    'erface'
  )?
;

IP
:
  'ip'
;

IP_ADDRESS
:
  F_IpAddress
;

IP_PREFIX
:
  F_IpPrefix
;

LOOPBACK
:
// most common abbreviation
  [Ll] [Oo]
  (
    [Oo] [Pp] [Bb] [Aa] [Cc] [Kk]
  )?
;

MGMT
:
  [Mm] [Gg] [Mm] [Tt]
;

NO
:
  'no'
;

PORT_CHANNEL
:
  [Pp] [Oo] [Rr] [Tt] '-' [Cc] [Hh] [Aa] [Nn] [Nn] [Ee] [Ll]
;

REDIRECTS
:
  'redirects'
;

SECONDARY
:
  'secondary'
;

SHUTDOWN
:
  'shutdown'
;

SWITCHPORT
:
  'switchport'
;

// Other Tokens

BLANK_LINE
:
  (
    F_Whitespace
  )* F_Newline
  {lastTokenType() == NEWLINE|| lastTokenType() == -1}?

  F_Newline* -> channel ( HIDDEN )
;

COMMENT_LINE
:
  (
    F_Whitespace
  )* [!#]
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewline* F_Newline+ -> channel ( HIDDEN )
;

DASH
:
  '-'
;

FORWARD_SLASH
:
  '/'
;

NEWLINE
:
  F_Newline+
;

PERIOD
:
  '.'
;

UINT8
:
  F_Uint8
;

UINT16
:
  F_Uint16
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
F_HexWord
:
  F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_HexWord2
:
  F_HexWord ':' F_HexWord
;

fragment
F_HexWord3
:
  F_HexWord2 ':' F_HexWord
;

fragment
F_HexWord4
:
  F_HexWord3 ':' F_HexWord
;

fragment
F_HexWord5
:
  F_HexWord4 ':' F_HexWord
;

fragment
F_HexWord6
:
  F_HexWord5 ':' F_HexWord
;

fragment
F_HexWord7
:
  F_HexWord6 ':' F_HexWord
;

fragment
F_HexWord8
:
  F_HexWord6 ':' F_HexWordFinal2
;

fragment
F_HexWordFinal2
:
  F_HexWord2
  | F_IpAddress
;

fragment
F_HexWordFinal3
:
  F_HexWord ':' F_HexWordFinal2
;

fragment
F_HexWordFinal4
:
  F_HexWord ':' F_HexWordFinal3
;

fragment
F_HexWordFinal5
:
  F_HexWord ':' F_HexWordFinal4
;

fragment
F_HexWordFinal6
:
  F_HexWord ':' F_HexWordFinal5
;

fragment
F_HexWordFinal7
:
  F_HexWord ':' F_HexWordFinal6
;

fragment
F_HexWordLE1
:
  F_HexWord?
;

fragment
F_HexWordLE2
:
  F_HexWordLE1
  | F_HexWordFinal2
;

fragment
F_HexWordLE3
:
  F_HexWordLE2
  | F_HexWordFinal3
;

fragment
F_HexWordLE4
:
  F_HexWordLE3
  | F_HexWordFinal4
;

fragment
F_HexWordLE5
:
  F_HexWordLE4
  | F_HexWordFinal5
;

fragment
F_HexWordLE6
:
  F_HexWordLE5
  | F_HexWordFinal6
;

fragment
F_HexWordLE7
:
  F_HexWordLE6
  | F_HexWordFinal7
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
  '::' F_HexWordLE7
  | F_HexWord '::' F_HexWordLE6
  | F_HexWord2 '::' F_HexWordLE5
  | F_HexWord3 '::' F_HexWordLE4
  | F_HexWord4 '::' F_HexWordLE3
  | F_HexWord5 '::' F_HexWordLE2
  | F_HexWord6 '::' F_HexWordLE1
  | F_HexWord7 '::'
  | F_HexWord8
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
F_Newline
:
  [\n\r]
;

fragment
F_NonNewline
:
  ~[\n\r]
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
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

fragment
F_WordChar
:
  ~[ \t\n\r{}[\]]
;

mode M_Hostname;

M_Hostname_SUBDOMAIN_NAME
:
  (
    (
      [A-Za-z0-9_]
      | '-'
    )+ '.'
  )*
  (
    [A-Za-z0-9_]
    | '-'
  )+ -> type ( SUBDOMAIN_NAME ) , popMode
;

M_Hostname_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;
