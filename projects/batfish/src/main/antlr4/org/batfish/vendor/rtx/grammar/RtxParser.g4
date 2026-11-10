parser grammar RtxParser;

import
  Rtx_bgp,
  Rtx_filter,
  Rtx_interface,
  Rtx_nat,
  Rtx_route,
  Rtx_tunnel;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = RtxLexer;
}

rtx_configuration
:
  NEWLINE? statement+ EOF
;

statement
:
  s_bgp
  | s_console
  | s_ip
  | s_nat
  | s_null
  | s_tunnel
  | s_vlan
;

s_ip
:
  IP
  (
    ip_filter
    | ip_interface
    | ip_route
    | ip_tunnel_null
  )
;

s_console
:
  CONSOLE
  (
    sc_prompt
    | sc_null
  )
;

// The hostname. RTX has no `host-name` command; `console prompt` is the only place a device's own
// name appears in every J57 config.
sc_prompt
:
  PROMPT name = WORD NEWLINE
;

sc_null
:
  ( CHARACTER | COLUMNS ) IGNORED_TEXT* NEWLINE
;

s_vlan
:
  VLAN iface = INTERFACE_NAME DOT1Q vid = VLAN_VID IGNORED_TEXT* NEWLINE
;

// Commands that do not affect what Batfish models. The lexer has already swallowed everything
// after the leading keyword. IPv6 is included wholesale: Batfish's vendor-independent model has no
// IPv6 addresses or filters.
s_null
:
  (
    ADMINISTRATOR
    | DNS
    | HTTPD
    | IPV6
    | LAN
    | LOGIN
    | NGN
    | SCHEDULE
    | SFTPD
    | SNMPV2C
    | SNTPD
    | SSHD
    | STATISTICS
    | SYSLOG
    | SYSTEM
    | TELNETD
    | USER
  )
  IGNORED_TEXT* NEWLINE
;
