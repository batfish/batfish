parser grammar Huawei_vlan;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// VLAN configuration (stub for Phase 1)

// VLAN batch command (create multiple VLANs)
s_vlan
:
   VLAN vlan_range
   |
   VLAN BATCH vlan_batch_range
;

// VLAN range (e.g., vlan 10, vlan 10-20)
vlan_range
:
   uint8
   |
   uint8 DASH uint8
;

// VLAN batch range (e.g., vlan batch 10 20 30)
vlan_batch_range
:
   (
      vlan = uint8
   )+
;
