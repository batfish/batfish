parser grammar Huawei_vlan;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// VLAN configuration

// VLAN stanza: vlan batch or vlan <id>
s_vlan
:
   VLAN_BATCH vlan_batch_range
   |
   VLAN vlan_id = uint8 vlan_body
;

// VLAN batch range (e.g., vlan batch 10 20 30 or vlan batch 2 to 10)
vlan_batch_range
:
   (
      vlan = uint8
   )+
   (
      // Optional: "to <vlan>" for range specification
      TO vlan = uint8
   )?
;

// VLAN body (configuration for individual VLAN)
vlan_body
:
   (
      vlan_substanza
   )*
;

// VLAN sub-stanzas
vlan_substanza
:
   v_name
   |
   v_description
   |
   v_null
;

// VLAN name
v_name
:
   NAME name = variable
;

// VLAN description
v_description
:
   description_line
;

// Null VLAN configuration (parse but ignore)
v_null
:
   NO?
   (
      null_rest_of_line
   )
;
