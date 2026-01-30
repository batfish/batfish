parser grammar Huawei_bgp;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// BGP configuration (stub for Phase 1)

// BGP stanza
s_bgp
:
   BGP as_num = uint32
   (
      bgp_substanza
   )*
;

// BGP sub-stanza (stub)
bgp_substanza
:
   bgp_null
;

// Null BGP configuration (parse but ignore)
bgp_null
:
   NO?
   (
      null_rest_of_line
   )
;
