parser grammar Arista_mlag;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

eos_vlan_name
:
   NAME name = variable NEWLINE
;

eos_vlan_no_name
:
   (NO | DEFAULT) NAME (name = variable)? NEWLINE
;

eos_vlan_no_state
:
   (NO | DEFAULT) STATE (ACTIVE | SUSPEND)? NEWLINE
;

eos_vlan_no_trunk
:
   (NO | DEFAULT) TRUNK GROUP (name = variable)? NEWLINE
;

eos_vlan_state
:
   STATE (ACTIVE | SUSPEND) NEWLINE
;

eos_vlan_trunk
:
   TRUNK GROUP name = variable NEWLINE
;

s_no_vlan_internal_eos
:
   (NO | DEFAULT)? VLAN INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING)? (RANGE lo=DEC hi=DEC)? NEWLINE
;

s_vlan_internal_eos
:
   VLAN INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING) RANGE lo=DEC hi=DEC NEWLINE
;

