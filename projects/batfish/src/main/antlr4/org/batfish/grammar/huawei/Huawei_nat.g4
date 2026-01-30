parser grammar Huawei_nat;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// NAT configuration (stub for Phase 1)

// NAT stanza
s_nat
:
   NAT
;

// NAT sub-stanza (stub)
nat_substanza
:
   nat_null
;

// Null NAT configuration (parse but ignore)
nat_null
:
   NO?
   (
      // Add NAT-specific commands here as needed
      null_rest_of_line
   )
;
