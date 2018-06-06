lexer grammar PaloAltoLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
// Java code to end up in PaloAltoLexer.java goes here
}

// Keywords

AUTHENTICATION_TYPE
:
   'authentication-type'
;

DEVICECONFIG
:
    'deviceconfig'
;

DNS_SETTING
:
    'dns-setting'
;

HOSTNAME
:
    'hostname'
;

NTP_SERVER_ADDRESS
:
    'ntp-server-address'
;

NTP_SERVERS
:
    'ntp-servers'
;

PRIMARY
:
    'primary'
;

PRIMARY_NTP_SERVER
:
    'primary-ntp-server'
;

SECONDARY
:
    'secondary'
;

SECONDARY_NTP_SERVER
:
    'secondary-ntp-server'
;

SERVERS
:
    'servers'
;

SET
:
    'set'
;

SYSTEM
:
    'system'
;

// Complex tokens

IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

IP_PREFIX
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit
;

NEWLINE
:
    F_Newline+
;

VARIABLE
:
   F_Variable_VarChar+
;

WS
:
    F_Whitespace+ -> channel(HIDDEN) // parser never sees tokens on hidden channel
;

// Fragments

fragment
F_DecByte
:
   (
   F_DecByteThreeDigit
   | F_DecByteTwoDigit
   | F_DecByteOneDigit
   )
;

fragment
F_DecByteOneDigit
:
   [0-9]
;

fragment
F_DecByteThreeDigit
:
   [12][0-9][0-9]
;

fragment
F_DecByteTwoDigit
:
   [1-9][0-9]
;

fragment
F_Digit
:
   [0-9]
;

fragment
F_IpAddress
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

fragment
F_Newline
:
    [\r\n] // carriage return or line feed
;

fragment
F_Whitespace
:
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_Variable_VarChar
:
   ~[ \t\n\r;{}[\]&|()"']
;

// Modes
// Blank for now, not all lexers will require modes
