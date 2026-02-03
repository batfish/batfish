lexer grammar FtdLexerOptimized;

options {
   superClass = 'org.batfish.grammar.cisco_ftd.parsing.FtdBaseLexerOptimized';
}

tokens {
   QUOTED_TEXT,
   RAW_TEXT
}

// ============================================================================
// MULTI-WORD TOKENS - Kept in grammar (cannot be handled by HashMap)
// These tokens have spaces or special patterns that require lexer rules
// ============================================================================

// Two-word keywords that must be in the grammar
ACCESS_LIST: 'access-list';

ADDRESS_FAMILY: 'address-family';

CRYPTO_CHECKSUM: 'Cryptochecksum';

EVENT_LOG: 'event-log';

EXIT_ADDRESS_FAMILY: 'exit-address-family';

HOLDTIME: 'holdtime';

IPSEC_ATTRIBUTES: 'ipsec-attributes';

IPSEC_L2L: 'ipsec-l2l';

KEEPALIVE_COUNTER: 'keepalive-counter';

KEEPALIVE_TIMEOUT: 'keepalive-timeout';

LOCAL_AUTHENTICATION: 'local-authentication';

LOG_ADJACENCY_CHANGES: 'log-adj-changes';

LOG_NEIGHBOR_CHANGES: 'log-neighbor-changes';

MAC_ADDRESS: 'mac-address';

MANAGEMENT_ONLY: 'management-only';

MANAGEMENT: 'Management';

MONITOR_INTERFACE: 'monitor-interface';

METRIC_TYPE: 'metric-type';

NAME_SERVER: 'name-server';

NAMES: 'names';

NETWORK_OBJECT: 'network-object';

REMOTE_ACCESS: 'remote-access';

REMOTE_AUTHENTICATION: 'remote-authentication';

OBJECT_GROUP_SEARCH: 'object-group-search';

PRE_SHARED_KEY: 'pre-shared-key';

PMTU_AGING: 'pmtu-aging';

PORT_OBJECT: 'port-object';

SECURITY_ASSOCIATION: 'security-association';

SECURITY_LEVEL: 'security-level';

SERVER_GROUP: 'server-group';

SERVICE_MODULE: 'service-module';

SERVICE_OBJECT: 'service-object';

SERVICE_POLICY: 'service-policy';

PRESERVE_UNTAG: 'preserve-untag';

REMOTE_AS: 'remote-as';

ROUTER_ID: 'router-id';

SUBNETS: 'subnets';

TIME_RANGE: 'time-range';

THREAT_DETECTION: 'threat-detection';

TRANSFORM_SET: 'transform-set';

DOMAIN_LOOKUP: 'domain-lookup';

COMMUNITY_LIST: 'community-list';

PASSIVE_INTERFACE: 'passive-interface';

DEFAULT_INFORMATION: 'default-information';

FILTER_LIST: 'filter-list';

CLASS_MAP: 'class-map';

POLICY_MAP: 'policy-map';

OBJECT_GROUP: 'object-group';

COMMUNITY_LIST: 'community-list';

// ============================================================================
// TOKENS WITH SPECIAL ACTIONS - Must remain in grammar
// ============================================================================

DESCRIPTION: 'description' -> pushMode(M_Description);

REMARK: 'remark' -> pushMode(M_REMARK);

// ============================================================================
// FIXED STRINGS - Could be moved to HashMap but kept for now
// ============================================================================

ETHERNET: 'Ethernet';

GIGABIT_ETHERNET: 'GigabitEthernet';

PORT_CHANNEL: 'Port-channel';

// ESP variants - kept for clarity
ESP_3DES: 'esp-3des';
ESP_AES: 'esp-aes';
ESP_AES_192: 'esp-aes-192';
ESP_AES_256: 'esp-aes-256';
ESP_DES: 'esp-des';
ESP_MD5_HMAC: 'esp-md5-hmac';
ESP_SHA_HMAC: 'esp-sha-hmac';
ESP_NONE: 'esp-none';

// ============================================================================
// PUNCTUATION AND OPERATORS
// ============================================================================

AMPERSAND: '&';
ASTERISK: '*';
CARET: '^';
COLON: ':';
COMMA: ',';
DASH: '-';
DOLLAR: '$';
DOUBLE_QUOTE: '"';
FORWARD_SLASH: '/';
LEFT_BRACE: '{';
LEFT_BRACKET: '[';
LEFT_PAREN: '(';
PERIOD: '.';
PIPE: '|';
PLUS: '+';
RIGHT_BRACE: '}';
RIGHT_BRACKET: ']';
RIGHT_PAREN: ')';
SEMICOLON: ';';
UNDERSCORE: '_';

// ============================================================================
// NUMERIC TOKENS
// ============================================================================

UINT8
:
   '0'
   | F_Digit_1_9
;

UINT16
:
   F_Digit_1_9 F_Digit
   | F_Digit_1_9 F_Digit F_Digit
   | F_Digit_1_9 F_Digit F_Digit F_Digit
   | [1-5] F_Digit F_Digit F_Digit F_Digit
   | '6' [0-4] F_Digit F_Digit F_Digit
   | '65' [0-4] F_Digit F_Digit
   | '655' [0-2] F_Digit
   | '6553' [0-5]
;

UINT32
:
   F_Uint32
;

DEC
:
   F_Digit+
;

IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

IP_PREFIX
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit F_Digit?
;

// ============================================================================
// OTHER TOKENS
// ============================================================================

COMMENT_LINE
:
   (
      '!'
      | '#'
      | ':'
   )
   {lastTokenType() == NEWLINE || lastTokenType() == -1}?

   F_NonNewlineChar* F_Newline+ -> channel(HIDDEN)
;

VARIABLE_NAME
:
   '$' F_Variable_Name_Char+
;

// ============================================================================
// CATCH-ALL RULES - Must be last
// ============================================================================

// NAME matches identifiers and names that aren't recognized keywords
// It requires at least one non-alphanumeric character (underscore, hyphen, etc.)
NAME
:
   F_Variable_Char* F_Variable_Special_Char F_Variable_Char*
;

// WORD matches alphabetic sequences that aren't recognized keywords
// These will be converted to keyword tokens by FtdBaseLexerOptimized.nextToken()
WORD
:
   [a-zA-Z]+
;

NEWLINE: ('\r'? '\n')+;

WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

// ============================================================================
// FRAGMENTS
// ============================================================================

fragment
F_DecByte
:
   F_Digit
   | F_PositiveDigit F_Digit
   | '1' F_Digit F_Digit
   | '2' [0-4] F_Digit
   | '25' [0-5]
;

fragment
F_Digit
:
   [0-9]
;

fragment
F_Digit_1_9
:
   [1-9]
;

fragment
F_HexDigit
:
   [0-9A-Fa-f]
;

fragment
F_Newline
:
   [\r\n]
;

fragment
F_NonNewlineChar
:
   ~[\r\n]
;

fragment
F_PositiveDigit
:
   [1-9]
;

fragment
F_Uint32
:
   '0'
   | F_Digit_1_9 F_Digit F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit?
;

fragment
F_Variable_Name_Char
:
   [0-9A-Za-z_]
;

fragment
F_WhitespaceChar
:
   [ \t\u000C]
;

// F_Variable_Char matches all characters allowed in identifiers/names
// Note: period (.) is NOT included so interface subinterfaces like Port-channel1.320
// can be tokenized as NAME PERIOD DEC
fragment
F_Variable_Char
:
   [a-zA-Z0-9_\-:@#]
;

// F_Variable_Special_Char - characters that distinguish names from keywords
// These are characters that can appear in names but NOT in keywords
fragment
F_Variable_Special_Char
:
   [_\-@#]
;

// ============================================================================
// LEXER MODES
// ============================================================================

mode M_REMARK;

M_REMARK_RAW_TEXT
:
   F_NonNewlineChar+ -> type(RAW_TEXT)
;

M_REMARK_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_REMARK_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Description;

M_Description_RAW_TEXT
:
   F_NonNewlineChar+ -> type(RAW_TEXT)
;

M_Description_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_Description_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;
