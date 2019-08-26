parser grammar CiscoNxos_monitor;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_monitor
:
  MONITOR monitor_session
;

monitor_session
:
  SESSION (ALL | monitor_session_id) NEWLINE
  (
    monitor_session_description
    | monitor_session_destination
    | monitor_session_source
  )*
;

monitor_session_description
:
  DESCRIPTION REMARK_TEXT NEWLINE
;

monitor_session_destination
:
  DESTINATION INTERFACE range = interface_range (BOTH | RX | TX)? NEWLINE
;

monitor_session_source
:
  SOURCE
  (
    monitor_session_source_interface
    | monitor_session_source_vlan
  )
;

monitor_session_source_interface
:
  INTERFACE range = interface_range (BOTH | RX | TX)? NEWLINE
;

monitor_session_source_vlan
:
  VLAN vlans = vlan_id_range NEWLINE
;