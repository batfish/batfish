parser grammar FlatVyosParser;

import
FlatVyos_bgp, FlatVyos_common, FlatVyos_interfaces, FlatVyos_policy, FlatVyos_protocols, FlatVyos_vpn;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = FlatVyosLexer;
}

flat_vyos_configuration
:
   NEWLINE* set_line+ NEWLINE* EOF
;

s_null
:
   (
      SERVICE
   ) null_filler
;

s_system
:
   SYSTEM s_system_tail
;

s_system_tail
:
   st_host_name
   | st_null
;

set_line
:
   SET set_line_tail NEWLINE
;

set_line_tail
:
   statement
;

st_default_address_selection
:
   DEFAULT_ADDRESS_SELECTION
;

st_host_name
:
   HOST_NAME name = variable
;

st_null
:
   (
      CONFIG_MANAGEMENT
      | CONSOLE
      | FLOW_ACCOUNTING
      | LOGIN
      | NTP
      | PACKAGE
      | SYSLOG
      | TASK_SCHEDULER
      | TIME_ZONE
   ) null_filler
;

statement
:
   s_interfaces
   | s_null
   | s_policy
   | s_protocols
   | s_system
   | s_vpn
;
