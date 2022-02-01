parser grammar CiscoNxos_lacp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

// 1-65535
lacp_priority: uint16;

s_lacp: LACP (
  lacp_system_mac_null
  | lacp_system_priority_null
);

lacp_system_mac_null: SYSTEM_MAC addr=MAC_ADDRESS_LITERAL ROLE (PRIMARY | SECONDARY) NEWLINE;

lacp_system_priority_null: SYSTEM_PRIORITY priority = lacp_priority NEWLINE;
