parser grammar CiscoNxos_mac;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_mac
:
  MAC mac_access_list
;

mac_access_list
:
  ACCESS_LIST name = mac_access_list_name NEWLINE
;