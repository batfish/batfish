parser grammar Arista_mlag;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

eos_vlan_id
:
   vlan_ids += subrange
   (
      COMMA vlan_ids += subrange
   )*
;

eos_vlan_name
:
   (NO | DEFAULT)? NAME name = variable NEWLINE
;

eos_vlan_no_name
:
   (NO | DEFAULT) NAME NEWLINE
;

eos_vlan_no_state
:
   (NO | DEFAULT) STATE NEWLINE
;

eos_vlan_no_trunk
:
   (NO | DEFAULT) TRUNK GROUP NEWLINE
;

eos_vlan_state
:
   (NO | DEFAULT)? STATE (ACTIVE | SUSPEND) NEWLINE
;

eos_vlan_trunk
:
   (NO | DEFAULT)? TRUNK GROUP name = variable NEWLINE
;

s_no_vlan_internal_eos
:
   (NO | DEFAULT)? VLAN INTERNAL ALLOCATION POLICY NEWLINE
;

s_vlan_internal_eos
:
   (NO | DEFAULT)? VLAN INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING) RANGE lo=DEC hi=DEC NEWLINE
;

