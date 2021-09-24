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
ACCESS: 'access';
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
CLASS_LIST: 'class-list';
CLOCK: 'clock';
CLOCK_SHOW: 'clock.show';
CONFIGURE_SYNC: 'configure.sync';
DEFAULT_PRIVILEGE: 'default-privilege';
DELETE_BW_LIST: 'delete.bw-list';
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
FILE_AFLEX: 'file.aflex';
FILE_BW_LIST: 'file.bw-list';
FILE_CA_CERT: 'file.ca-cert';
FILE_CLASS_LIST: 'file.class-list';
FILE_CSR: 'file.csr';
FILE_GLM_LICENSE: 'file.glm-license';
FILE_HEALTH_EXTERNAL: 'file.health-external';
FILE_HEALTH_POSTFILE: 'file.health-postfile';
FILE_INSPECTION_TEMPLATE: 'file-inspection.template';
FILE_SSL_CERT: 'file.ssl-cert';
FILE_SSL_CERT_KEY: 'file.ssl-cert-key';
FILE_SSL_CRL: 'file.ssl-crl';
FILE_SSL_KEY: 'file.ssl-key';
FILE_STARTUP_CONFIG: 'file.startup-config';
FILE_TECHSUPPORT: 'file.techsupport';
FILE_TEMPLATE: 'file.template';
FILE_WEB_SERVICE_CERT_KEY: 'file.web-service-cert-key';
GLID: 'glid';
GLM: 'glm';
HEALTH: 'health';
HOSTNAME: 'hostname' -> pushMode(M_Hostname);
IMPORT: 'import';
IMPORT_PERIODIC: 'import-periodic';
IMPORT_PERIODIC_AFLEX: 'import-periodic.aflex';
IMPORT_PERIODIC_BW_LIST: 'import-periodic.bw-list';
INTERFACE: 'interface';
INTERFACE_ETHERNET: 'interface.ethernet';
INTERFACE_MANAGEMENT: 'interface.management';
INTERFACE_TRUNK: 'interface.trunk';
INTERFACE_VE: 'interface.ve';
IP: 'ip';
IPV6_ACCESS_LIST: 'ipv6.access-list';
IPV6_ADDRESS: 'ipv6.address';
IPV6_DEFAULT_GATEWAY: 'ipv6.default-gateway';
IPV6_NAT_INSIDE_SOURCE_LIST: 'ipv6.nat.inside.source.list';
IPV6_NAT_POOL: 'ipv6.nat.pool';
IPV6_NAT_POOL_GROUP: 'ipv6.nat.pool-group';
IPV6_NEIGHBOR_DYNAMIC: 'ipv6.neighbor.dynamic';
IPV6_NEIGHBOR_STATIC: 'ipv6.neighbor.static';
IPV6_ROUTE: 'ipv6.route';
IP_ACCESS_LIST: 'ip.access-list';
IP_DEFAULT_GATEWAY: 'ip.default-gateway';
IP_DNS: 'ip.dns';
IP_DOT_ADDRESS: 'ip.address';
IP_NAT_INSIDE_SOURCE_LIST_ACL_ID_LIST: 'ip.nat.inside.source.list.acl-id-list';
IP_NAT_INSIDE_SOURCE_LIST_ACL_NAME_LIST: 'ip.nat.inside.source.list.acl-name-list';
IP_NAT_INSIDE_SOURCE_STATIC: 'ip.nat.inside.source.static';
IP_NAT_POOL: 'ip.nat.pool';
IP_NAT_POOL_GROUP: 'ip.nat.pool-group';
IP_NAT_RANGE_LIST: 'ip.nat.range-list';
IP_NAT_TEMPLATE_LOGGING: 'ip.nat.template.logging';
IP_ROUTE: 'ip.route';
IP_TCP: 'ip.tcp';
LDAP_SERVER_HOST: 'ldap-server.host';
LINK_STARTUP_CONFIG: 'link.startup-config';
LOGGING: 'logging';
LOOPBACK: 'loopback';
MONITOR: 'monitor';
MTU: 'mtu';
MULTI_CONFIG: 'multi-config';
NAME: 'name' -> pushMode(M_Word);
NETFLOW_COMMON: 'netflow.common';
NETFLOW_MONITOR: 'netflow.monitor';
NETWORK: 'network';
NETWORK_AVAILABLE_TRUNK_LIST: 'network.available-trunk-list';
NETWORK_ICMPV6_RATE_LIMIT: 'network.icmpv6-rate-limit';
NETWORK_ICMP_RATE_LIMIT: 'network.icmp-rate-limit';
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
PARTITION_ONLY: 'partition-only';
PKI_CREATE_OPER: 'pki.create-oper';
PKI_DELETE: 'pki.delete';
RADIUS_SERVER: 'radius-server';
RBA: 'rba';
READ: 'read';
REBOOT: 'reboot';
RELOAD: 'reload';
RENAME: 'rename';
RESTORE: 'restore';
ROLE: 'role' -> pushMode(M_Word);
ROUTER_INTERFACE: 'router-interface';
RRD: 'rrd';
SCALEOUT: 'scaleout';
SCM_LICENSE_SRC_INFO: 'scm.license-src-info';
SESSIONS: 'sessions';
SESSION_FILTER: 'session-filter';
SFLOW: 'sflow';
SFLOW_GLOBAL: 'sflow.global';
SHUTDOWN: 'shutdown';
SLB: 'slb';
SLB_RESOURCE_USAGE: 'slb.resource-usage';
SLB_SERVER: 'slb.server';
SLB_TEMPLATE: 'slb.template';
SLB_VIRTUAL_SERVER: 'slb.virtual-server';
SMTP: 'smtp';
SNMP_SERVER: 'snmp-server';
SSH_LOGIN_GRACE_TIME: 'ssh-login-grace-time';
SYSLOG: 'syslog';
SYSTEM: 'system';
SYSTEM_CPU_CTRL_CPU: 'system-cpu.ctrl-cpu';
SYSTEM_CPU_DATA_CPU: 'system-cpu.data-cpu';
SYSTEM_ENVIRONMENT: 'system.environment';
SYSTEM_GUI_IMAGE_LIST: 'system.gui-image-list';
SYSTEM_HARDWARE: 'system.hardware';
SYSTEM_JUMBO_GLOBAL: 'system-jumbo-global';
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
M_HostnameValue_READ: READ -> type(READ), popMode;
M_HostnameValue_NO_ACCESS: NO_ACCESS -> type(NO_ACCESS), popMode;
M_HostnameValue_OPER: OPER -> type(OPER), popMode;
M_HostnameValue_PARTITION_ONLY: PARTITION_ONLY -> type(PARTITION_ONLY), popMode;
M_HostnameValue_WRITE: WRITE -> type(WRITE), popMode;
M_HostnameValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_HostnameValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_HostnameValue_WORD: F_Word -> type(WORD);
M_HostnameValue_WS: F_Whitespace+ -> skip, popMode;
M_HostnameValue_NEWLINE: F_Newline -> type(NEWLINE), popMode;
