parser grammar Huawei_vrf;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// VRF configuration (Phase 9)

// VPN-instance stanza (Huawei's term for VRF)
// ip vpn-instance <vrf-name>
s_vrf
:
   IP VPN_INSTANCE vrf_name = variable
   (
      vrf_substanza
   )*
;

// VRF sub-stanzas
vrf_substanza
:
   vrf_route_distinguisher
   | vrf_vpn_target
   | vrf_description
   | vrf_null
;

// Route distinguisher: route-distinguisher 100:1
vrf_route_distinguisher
:
   ROUTE_DISTINGUISHER rd = variable
;

// VPN target (route target): vpn-target 100:1 export
vrf_vpn_target
:
   VPN_TARGET rt_value = variable (IMPORT | EXPORT | BOTH)?
;

// VRF description: description Customer A VRF
vrf_description
:
   DESCRIPTION desc_text = variable
;

// Null VRF configuration (parse but ignore)
vrf_null
:
   NO?
   (
      null_rest_of_line
   )
;
