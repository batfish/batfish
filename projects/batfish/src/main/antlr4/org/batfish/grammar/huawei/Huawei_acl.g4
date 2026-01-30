parser grammar Huawei_acl;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// ACL configuration (stub for Phase 1)

// ACL stanza
s_acl
:
   (
      // Basic ACL
      ACL (ACL_BASIC | ACL_ADVANCED)? acl_num = uint16
      |
      // Named ACL
      ACL name = variable (ACL_BASIC | ACL_ADVANCED)?
   )
   (
      acl_substanza
   )*
;

// ACL sub-stanza (stub)
acl_substanza
:
   acl_null
;

// Null ACL configuration (parse but ignore)
acl_null
:
   NO?
   (
      null_rest_of_line
   )
;
