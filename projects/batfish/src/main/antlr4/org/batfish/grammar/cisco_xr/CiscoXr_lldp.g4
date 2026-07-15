parser grammar CiscoXr_lldp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_lldp: LLDP NEWLINE lldp_inner*;

lldp_inner
:
  lldp_extended_show_width_null
  | lldp_holdtime_null
  | lldp_reinit_null
  | lldp_subinterfaces_null
  | lldp_timer_null
  | lldp_tlv_select
;

lldp_extended_show_width_null
:
   EXTENDED_SHOW_WIDTH null_rest_of_line
;
lldp_holdtime_null
:
   HOLDTIME null_rest_of_line
;
lldp_reinit_null
:
   REINIT null_rest_of_line
;
lldp_subinterfaces_null
:
   SUBINTERFACES null_rest_of_line
;
lldp_timer_null
:
   TIMER null_rest_of_line
;

lldp_tlv_select: TLV_SELECT NEWLINE lldp_tlv_select_inner*;

lldp_tlv_select_inner:
 lldpts_management_address_null
 | lldpts_port_description_null
 | lldpts_system_capabilities_null
 | lldpts_system_description_null
 | lldpts_system_name_null
;

lldpts_management_address_null
:
   MANAGEMENT_ADDRESS null_rest_of_line
;
lldpts_port_description_null
:
   PORT_DESCRIPTION null_rest_of_line
;
lldpts_system_capabilities_null
:
   SYSTEM_CAPABILITIES null_rest_of_line
;
lldpts_system_description_null
:
   SYSTEM_DESCRIPTION null_rest_of_line
;
lldpts_system_name_null
:
   SYSTEM_NAME null_rest_of_line
;
