parser grammar A10_vlan;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_vlan: VLAN vlan_number NEWLINE sv_definition*;

sv_definition: svd_name | svd_router_interface | svd_tagged | svd_untagged;

svd_name: NAME vlan_name NEWLINE;

svd_router_interface: ROUTER_INTERFACE VE vlan_number NEWLINE;

svd_tagged: TAGGED vlan_iface_references NEWLINE;

svd_untagged: UNTAGGED vlan_iface_references NEWLINE;

// Only ACOS 2.X allows more than one interface specifier here
vlan_iface_references: (vlan_ifaces_range | vlan_ifaces_list)+;

vlan_ifaces_range:
    // All(?) versions of ACOS
    vlan_iface_ethernet_range | vlan_iface_trunk_range
;

vlan_ifaces_list:
    // Only ACOS 2.X allows more than one interface here
    (vlan_iface_ethernet | vlan_iface_trunk)+
;

vlan_iface_ethernet: ETHERNET num = ethernet_number;

vlan_iface_ethernet_range: ETHERNET num = ethernet_number TO to = ethernet_number;

vlan_iface_trunk: TRUNK num = trunk_number;

vlan_iface_trunk_range: TRUNK num = trunk_number TO to = trunk_number;
