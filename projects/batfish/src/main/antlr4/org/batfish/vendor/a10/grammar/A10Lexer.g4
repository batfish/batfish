lexer grammar A10Lexer;

options {
   superClass = 'A10BaseLexer';
}

tokens {
  QUOTED_TEXT,
  WORD,
  WORD_SEPARATOR
}

// A10 keywords
ACCESS_LIST: 'access-list';
ACTIVE_PARTITION: 'active-partition';
ADDRESS: 'address';
ADMIN: 'admin';
ADMIN_DETAIL: 'admin-detail';
ADMIN_LOCKOUT: 'admin-lockout';
ADMIN_SESSION: 'admin-session';
AUTHENTICATION: 'authentication';
BACKUP_LOG: 'backup.log';
BACKUP_PERIODIC: 'backup-periodic';
BACKUP_SYSTEM: 'backup.system';
BANNER: 'banner';
BOOTIMAGE: 'bootimage';
CGNV6_RESOURCE_USAGE: 'cgnv6.resource-usage';
CLOCK: 'clock';
CLOCK_SHOW: 'clock.show';
CONFIGURE_SYNC: 'configure.sync';
DEFAULT_PRIVILEGE: 'default-privilege';
DELETE_GLM_LICENSE: 'delete.glm-license';
DELETE_PARTITION: 'delete.partition';
DELETE_STARTUP_CONFIG: 'delete.startup-config';
DEVICE_CONTEXT: 'device-context';
DISABLE: 'disable';
DISABLE_MANAGEMENT: 'disable-management';
ENABLE: 'enable';
ENABLE_MANAGEMENT: 'enable-management';
ETHERNET: 'ethernet';
EXPORT: 'export';
FILE_GLM_LICENSE: 'file.glm-license';
FILE_STARTUP_CONFIG: 'file.startup-config';
FILE_TECHSUPPORT: 'file.techsupport';
FILE_TEMPLATE: 'file.template';
FILE_WEB_SERVICE_CERT_KEY: 'file.web-service-cert-key';
GLM: 'glm';
HOSTNAME: 'hostname' -> pushMode(M_Hostname);
IMPORT: 'import';
INTERFACE: 'interface';
INTERFACE_ETHERNET: 'interface.ethernet';
INTERFACE_MANAGEMENT: 'interface.management';
INTERFACE_VE: 'interface.ve';
IP: 'ip';
IPV6_ACCESS_LIST: 'ipv6.access-list';
IP_DNS: 'ip.dns';
LDAP_SERVER_HOST: 'ldap-server.host';
LINK_STARTUP_CONFIG: 'link.startup-config';
LOGGING: 'logging';
LOOPBACK: 'loopback';
MONITOR: 'monitor';
MTU: 'mtu';
MULTI_CONFIG: 'multi-config';
NAME: 'name' -> pushMode(M_Word);
NETWORK_VLAN: 'network.vlan';
NO_ACCESS: 'no-access';
NTP_AUTH_KEY: 'ntp.auth-key';
NTP_SERVER: 'ntp.server';
NTP_STATUS: 'ntp-status';
NTP_TRUSTED_KEY: 'ntp.trusted-key';
OPER: 'oper';
PARTITION: 'partition';
PARTITION_ALL: 'partition-all';
PARTITION_GROUP: 'partition-group';
RADIUS_SERVER: 'radius-server';
RBA: 'rba';
READ: 'read';
REBOOT: 'reboot';
RELOAD: 'reload';
RESTORE: 'restore';
ROLE: 'role' -> pushMode(M_Word);
ROUTER_INTERFACE: 'router-interface';
RRD: 'rrd';
SCM_LICENSE_SRC_INFO: 'scm.license-src-info';
SHUTDOWN: 'shutdown';
SLB_RESOURCE_USAGE: 'slb.resource-usage';
SMTP: 'smtp';
SNMP_SERVER: 'snmp-server';
SSH_LOGIN_GRACE_TIME: 'ssh-login-grace-time';
SYSLOG: 'syslog';
SYSTEM_CPU_CTRL_CPU: 'system-cpu.ctrl-cpu';
SYSTEM_CPU_DATA_CPU: 'system-cpu.data-cpu';
SYSTEM_ENVIRONMENT: 'system.environment';
SYSTEM_GUI_IMAGE_LIST: 'system.gui-image-list';
SYSTEM_HARDWARE: 'system.hardware';
SYSTEM_MEMORY: 'system.memory';
SYSTEM_RESOURCE_ACCOUNTING: 'system.resource-accounting';
SYSTEM_RESOURCE_USAGE: 'system.resource-usage';
SYSTEM_UPGRADE_STATUS: 'system.upgrade-status';
SYS_AUDIT_LOG: 'sys-audit-log';
TACACS_SERVER: 'tacacs-server';
TAGGED: 'tagged';
TERMINAL: 'terminal';
TFTP: 'tftp';
TIMEZONE: 'timezone';
TO: 'to';
UNTAGGED: 'untagged';
UPGRADE_CF: 'upgrade.cf';
UPGRADE_HD: 'upgrade.hd';
VCS: 'vcs';
VCS_VBLADES: 'vcs-vblades';
VE: 've';
VERSION: 'version';
VLAN: 'vlan';
WEB_SERVICE: 'web-service';
WRITE: 'write';
WRITE_MEMORY: 'write.memory';

// Complex tokens
COMMENT_LINE
:
  F_Whitespace* '!'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewlineChar* (F_Newline | EOF) -> skip
;

DOUBLE_QUOTE
:
  '"' -> pushMode ( M_DoubleQuote )
;

SINGLE_QUOTE
:
  ['] -> pushMode ( M_SingleQuote )
;

SUBNET_MASK: F_SubnetMask;

IP_ADDRESS: F_IpAddress;

IP_SLASH_PREFIX: F_IpSlashPrefix;

NEWLINE: F_Newline+;

UINT8: F_Uint8;

UINT16: F_Uint16;

UINT32: F_Uint32;

WS: F_Whitespace+ -> skip;

// Fragments

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
F_Digit: [0-9];

fragment
F_IpAddress: F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte;

fragment
F_IpSlashPrefix: '/' F_IpPrefixLength;

fragment
F_IpPrefixLength
:
    F_Digit
    | [12] F_Digit
    | [3] [012]
;

fragment
F_Newline
:
    [\r\n] // carriage return or line feed
;

fragment
F_NonNewlineChar
:
    ~[\r\n] // carriage return or line feed
;

fragment
F_PositiveDigit: [1-9];

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
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_Word: F_WordChar+;

fragment
F_WordChar
:
  [0-9A-Za-z!@#$%^&*()_=+.;:{}/]
  | '-'
;

fragment
F_StrChar: ~( [ \t\u000C\u00A0\n\r(),!$'"*#] | '[' | ']' );
fragment
F_Str: F_StrChar+;

fragment
F_EscapedDoubleQuote: '\\"';

fragment
F_EscapedSingleQuote: '\\' ['];

// Modes
mode M_DoubleQuote;
M_DoubleQuote_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;
M_DoubleQuote_QUOTED_TEXT: (F_EscapedDoubleQuote | ~'"')+ -> type(QUOTED_TEXT);

mode M_SingleQuote;
M_SingleQuote_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), popMode;
M_SingleQuote_QUOTED_TEXT: (F_EscapedSingleQuote | ~['])+ -> type(QUOTED_TEXT);

mode M_Word;
M_Word_WS: F_Whitespace+ -> type(WORD_SEPARATOR), mode(M_WordValue);
M_Word_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_WordValue;
M_WordValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_WordValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_WordValue_WORD: F_Word -> type(WORD);
M_WordValue_WS: F_Whitespace+ -> skip, popMode;
M_WordValue_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Hostname;
M_Hostname_WS: F_Whitespace+ -> type(WORD_SEPARATOR), mode(M_HostnameValue);
M_Hostname_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_HostnameValue;
M_HostnameValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_HostnameValue_READ: READ -> type(READ), popMode;
M_HostnameValue_NO_ACCESS: NO_ACCESS -> type(NO_ACCESS), popMode;
M_HostnameValue_OPER: OPER -> type(OPER), popMode;
M_HostnameValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_HostnameValue_WORD: F_Word -> type(WORD);
M_HostnameValue_WRITE: WRITE -> type(WRITE), popMode;
M_HostnameValue_WS: F_Whitespace+ -> skip, popMode;
M_HostnameValue_NEWLINE: F_Newline -> type(NEWLINE), popMode;
