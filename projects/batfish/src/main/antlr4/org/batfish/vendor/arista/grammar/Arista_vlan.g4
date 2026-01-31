parser grammar Arista_vlan;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

no_vlan: VLAN eos_vlan_id NEWLINE;

s_vlan
:
   VLAN eos_vlan_id NEWLINE
   vlan_inner*
;

vlan_inner
:
  vlan_default
  | vlan_name
  | vlan_no
  | vlan_state
  | vlan_trunk
;

vlan_default
:
  DEFAULT (
    vlan_d_name
    | vlan_d_state
    | vlan_d_trunk
  )
;

vlan_d_name: NAME NEWLINE;
vlan_d_state: STATE NEWLINE;
vlan_d_trunk: TRUNK GROUP NEWLINE;

vlan_name
:
   NAME name = variable NEWLINE
;

vlan_no
:
  NO (
    vlan_no_name
    | vlan_no_state
    | vlan_no_trunk
  )
;

vlan_no_name
:
   NAME (name = variable)? NEWLINE
;

vlan_no_state
:
   STATE (ACTIVE | SUSPEND)? NEWLINE
;

vlan_no_trunk
:
   TRUNK GROUP (name = variable)? NEWLINE
;

vlan_state
:
   STATE (ACTIVE | SUSPEND) NEWLINE
;

vlan_trunk
:
   TRUNK GROUP name = variable NEWLINE
;

default_vlan_internal
:
   VLAN INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING)? (RANGE lo=dec hi=dec)? NEWLINE
;

no_vlan_internal
:
   VLAN INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING)? (RANGE lo=dec hi=dec)? NEWLINE
;

s_vlan_internal
:
   VLAN INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING) RANGE lo=dec hi=dec NEWLINE
;

