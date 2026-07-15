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
   st_config_management_null
   | st_console_null
   | st_flow_accounting_null
   | st_host_name
   | st_login_null
   | st_ntp_null
   | st_package_null
   | st_syslog_null
   | st_task_scheduler_null
   | st_time_zone_null
;

set_line
:
   SET set_line_tail NEWLINE
;

set_line_tail
:
   statement
;

st_host_name
:
   HOST_NAME name = variable
;

st_config_management_null
:
   CONFIG_MANAGEMENT null_filler
;
st_console_null
:
   CONSOLE null_filler
;
st_flow_accounting_null
:
   FLOW_ACCOUNTING null_filler
;
st_login_null
:
   LOGIN null_filler
;
st_ntp_null
:
   NTP null_filler
;
st_package_null
:
   PACKAGE null_filler
;
st_syslog_null
:
   SYSLOG null_filler
;
st_task_scheduler_null
:
   TASK_SCHEDULER null_filler
;
st_time_zone_null
:
   TIME_ZONE null_filler
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
