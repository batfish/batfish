parser grammar Huawei_vrf;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// VRF configuration (stub for Phase 1)

// VPN-instance stanza (Huawei's term for VRF)
s_vrf
:
   IP VPN_INSTANCE vrf_name = variable
;

// VRF sub-stanza (stub)
vrf_substanza
:
   vrf_null
;

// Null VRF configuration (parse but ignore)
vrf_null
:
   NO?
   (
      // Add VRF-specific commands here as needed
      null_rest_of_line
   )
;
