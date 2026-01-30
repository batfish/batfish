parser grammar Huawei_ospf;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// OSPF configuration (stub for Phase 1)

// OSPF stanza
s_ospf
:
   OSPF process_id = uint32
;

// OSPF sub-stanza (stub)
ospf_substanza
:
   ospf_null
;

// Null OSPF configuration (parse but ignore)
ospf_null
:
   NO?
   (
      // Add OSPF-specific commands here as needed
      null_rest_of_line
   )
;
