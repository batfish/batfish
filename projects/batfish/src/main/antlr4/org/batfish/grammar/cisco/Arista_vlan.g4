parser grammar Arista_mlag;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

eos_vlan_id
:
   (
      vlan_ids += subrange
      (
         COMMA vlan_ids += subrange
      )*
   ) NEWLINE
;

eos_vlan_internal
:
   (NO | DEFAULT)? VLAN INTERNAL ALLOCATION POLICY NEWLINE
   | INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING) RANGE lo=DEC hi=DEC NEWLINE
;

eos_vlan_inner
:
   eos_vlan_name
   | eos_vlan_state
   | eos_vlan_trunk
;

eos_vlan_name
:
   (NO | DEFAULT)? NAME NEWLINE
   | NAME name = variable NEWLINE
;

eos_vlan_state
:
   (NO | DEFAULT)? STATE NEWLINE
   | STATE (ACTIVE | SUSPEND) NEWLINE
;

eos_vlan_trunk
:
   (NO | DEFAULT)? TRUNK GROUP NEWLINE
   | TRUNK GROUP name = variable NEWLINE
;
