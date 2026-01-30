parser grammar HuaweiParser;

import
Huawei_common,
Huawei_system,
Huawei_interface,
Huawei_vlan,
Huawei_bgp,
Huawei_ospf,
Huawei_static,
Huawei_acl,
Huawei_nat,
Huawei_vrf,
Huawei_ignored;

options {
   superClass = 'org.batfish.grammar.huawei.parsing.HuaweiBaseParser';
   tokenVocab = HuaweiLexer;
}

// Main entry point for Huawei VRP configuration
huawei_configuration
:
   ( stanza += s_stanza )*
   EOF
;

// Stanza at top level (includes return which ends parsing)
s_stanza
:
   s_sysname
   | s_interface
   | s_vlan
   | s_bgp
   | s_ospf
   | s_static_route
   | s_acl
   | s_nat
   | s_vrf
   | s_return
   | s_ignored
;

// Return statement (ends current configuration mode)
s_return
:
   RETURN
;
