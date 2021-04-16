parser grammar CiscoXr_lldp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_lldp: LLDP NEWLINE lldp_inner*;

lldp_inner
:
  lldp_null
  | lldp_tlv_select
;

lldp_null
:
  (
    EXTENDED_SHOW_WIDTH
    | HOLDTIME
    | REINIT
    | SUBINTERFACES
    | TIMER
  ) null_rest_of_line
;

lldp_tlv_select: TLV_SELECT NEWLINE lldp_tlv_select_inner*;

lldp_tlv_select_inner: lldpts_null;

lldpts_null
:
  (
    MANAGEMENT_ADDRESS
    | PORT_DESCRIPTION
    | SYSTEM_CAPABILITIES
    | SYSTEM_DESCRIPTION
    | SYSTEM_NAME
  ) null_rest_of_line
;
