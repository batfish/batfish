parser grammar NecIxParser;

import
  NecIx_bgp,
  NecIx_common,
  NecIx_interface,
  NecIx_ip,
  NecIx_route_map;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = NecIxLexer;
}

nec_ix_configuration
:
  NEWLINE? statement* EOF
;

statement
:
  s_device
  | s_hostname
  | s_interface
  | s_ip
  | s_null
  | s_route_map
  | s_router_bgp
;

s_hostname
:
  HOSTNAME name = variable NEWLINE
;

// `device <name>` configures the physical port behind the `<name>.<n>` interfaces. Only
// `shutdown` matters to Batfish; the rest (speed, duplex, port-based VLANs, ...) is ignored.
s_device
:
  DEVICE name = variable NEWLINE device_inner*
;

device_inner
:
  INDENT
  (
    dev_shutdown
    | dev_null
  )
;

dev_shutdown
:
  SHUTDOWN NEWLINE
;

dev_null
:
  ~( SHUTDOWN | NEWLINE ) null_rest_of_line
;

// Top-level commands that do not affect what Batfish models, together with any commands in the
// mode they open (e.g. `policy-map`, `ikev2 default-profile`, `ipv6 dhcp client-profile`).
// IPv6 is not modeled by Batfish, so the whole IPv6 configuration is ignored.
s_null
:
  (
    BRIDGE
    | IKEV2
    | IPV6
    | LOGGING
    | NTP
    | POLICY_MAP
    | PPP
    | PROXY_DNS
    | SERVICE
    | SNMP_AGENT
    | SSH_SERVER
    | SYSLOG
    | TERMINAL
    | TIMEZONE
    | URL_LIST
  ) null_rest_of_line null_block_line*
;
