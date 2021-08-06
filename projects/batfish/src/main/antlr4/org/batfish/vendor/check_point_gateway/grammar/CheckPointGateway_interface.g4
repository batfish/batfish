parser grammar CheckPointGateway_interface;

options {
    tokenVocab = CheckPointGatewayLexer;
}

a_interface: INTERFACE interface_name ai;

ai: ai_vlan;

ai_vlan: VLAN vlan_id;

// 2-4094
vlan_id: uint16;

s_interface: INTERFACE interface_name si;

si
:
    si_auto_negotiation
    | si_comments
    | si_ipv4_address
    | si_link_speed
    | si_mtu
    | si_state
;

si_auto_negotiation: AUTO_NEGOTIATION on_or_off;

si_comments: COMMENTS word;

si_ipv4_address: IPV4_ADDRESS ip_address siia_mask;

siia_mask: siia_mask_length | siia_subnet_mask;

siia_mask_length: MASK_LENGTH ip_mask_length;

siia_subnet_mask: SUBNET_MASK ip_address;

si_link_speed: LINK_SPEED link_speed FORCE?;

link_speed
:
    TEN_M_FULL
    | TEN_M_HALF
    | HUNDRED_M_FULL
    | HUNDRED_M_HALF
    | THOUSAND_M_FULL
;

si_mtu: MTU mtu;

si_state: STATE on_or_off;
