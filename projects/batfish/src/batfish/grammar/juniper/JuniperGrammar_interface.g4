parser grammar JuniperGrammar_interface;

import JuniperGrammarCommonParser;

options {
   tokenVocab = JuniperGrammarLexer;
}

address_fam_u_if_stanza
:
   ADDRESS
   (
      IP_ADDRESS_WITH_MASK
      | IPV6_ADDRESS_WITH_MASK
      | ISO_ADDRESS
   )
   (
      SEMICOLON
      | ignored_substanza
   )
;

aggregated_ether_options_if_stanza
:
   AGGREGATED_ETHER_OPTIONS ignored_substanza
;

apply_groups_if_stanza // TODO: FIX!

:
   apply_groups_stanza
;

apply_groups_u_if_stanza // TODO: FIX!

:
   apply_groups_stanza
;

description_if_stanza
:
   description_common_stanza
;

description_u_if_stanza
:
   description_common_stanza
;

disable_if_stanza
:
   DISABLE SEMICOLON
;

disable_u_if_stanza
:
   DISABLE SEMICOLON
;

enable_if_stanza
:
   ENABLE SEMICOLON
;

enable_u_if_stanza
:
   ENABLE SEMICOLON
;

encapsulation_if_stanza
:
   encapsulation_common_stanza
;

encapsulation_u_if_stanza
:
   encapsulation_common_stanza
;

fam_u_if_stanza
:
   address_fam_u_if_stanza
   | filter_fam_u_if_stanza
   | native_vlan_id_fam_u_if_stanza
   | vlan_members_fam_u_if_stanza
   | null_fam_u_if_stanza
;

fam_u_if_stanza_list
:
   (
      fam_u_if_stanza
      | inactive_fam_u_if_stanza
   )+
;

family_u_if_stanza
:
   FAMILY
   (
      ft = BRIDGE
      | ft = CCC
      | ft = INET
      | ft = INET_VPN
      | ft = INET6
      | ft = INET6_VPN
      | ft = ISO
      | ft = L2_VPN
      | ft = ETHERNET_SWITCHING
      | ft = MPLS
      | ft = VPLS
   )
   (
      SEMICOLON
      |
      (
         OPEN_BRACE fam_u_if_stanza_list CLOSE_BRACE
      )
   )
;

filter_fam_u_if_stanza
:
   FILTER OPEN_BRACE
   (
      (
         INACTIVE COLON
      )? INPUT infltr = VARIABLE SEMICOLON
   )?
   (
      (
         INACTIVE COLON
      )? OUTPUT outfltr = VARIABLE SEMICOLON
   )? CLOSE_BRACE
;

framing_if_stanza
:
   FRAMING ignored_substanza
;

flexible_vlan_tagging_if_stanza
:
   FLEXIBLE_VLAN_TAGGING SEMICOLON
;

gigether_options_if_stanza
:
   GIGETHER_OPTIONS ignored_substanza
;

if_stanza
:
   disable_if_stanza
   | enable_if_stanza
   | unit_if_stanza
   | null_if_stanza
   | apply_groups_if_stanza
;

if_stanza_list
:
   (
      if_stanza
      | inactive_if_stanza
   )+
;

inactive_fam_u_if_stanza
:
   INACTIVE COLON fam_u_if_stanza
;

inactive_if_stanza
:
   INACTIVE COLON if_stanza
;

inactive_interface_stanza
:
   INACTIVE COLON interface_stanza
;

inactive_u_if_stanza
:
   INACTIVE COLON u_if_stanza
;

input_vlan_map_u_if_stanza // TODO [Ask Ari]: probably not supposed to ignore this

:
   INPUT_VLAN_MAP ignored_substanza
;

interface_mode_fam_u_if_stanza // TODO [P0]: should not be ignored

:
   INTERFACE_MODE ACCESS SEMICOLON
;

interface_stanza
:
   name = VARIABLE? OPEN_BRACE if_stanza_list CLOSE_BRACE
;

interfaces_stanza
:
   INTERFACES OPEN_BRACE
   (
      interface_stanza
      | inactive_interface_stanza
   )+ CLOSE_BRACE
;

link_mode_if_stanza
:
   LINK_MODE VARIABLE SEMICOLON
;

mac_if_stanza
:
// TODO [p1]: should be MAC_ADDRESS, solve with lexer modes in antlr 4
   x = MAC y = MAC_ADDRESS SEMICOLON
;

mtu_fam_u_if_stanza
:
   mtu_common_stanza
;

mtu_if_stanza
:
   mstr = mtu_common_stanza
;

native_vlan_id_fam_u_if_stanza
:
   NATIVE_VLAN_ID DEC SEMICOLON
;

no_neighbor_learn_fam_u_if_stanza
:
   NO_NEIGHBOR_LEARN SEMICOLON
;

no_redirects_fam_u_if_stanza
:
   NO_REDIRECTS SEMICOLON
;

null_fam_u_if_stanza
:
   interface_mode_fam_u_if_stanza
   | mtu_fam_u_if_stanza
   | port_mode_fam_u_if_stanza
   | no_redirects_fam_u_if_stanza
   | no_neighbor_learn_fam_u_if_stanza
   | policer_fam_u_if_stanza
   | primary_fam_u_if_stanza
   | rpf_check_fam_u_if_stanza
   | targeted_broadcast_fam_u_if_stanza
   | vlan_id_fam_u_if_stanza
;

null_if_stanza
:
   aggregated_ether_options_if_stanza
   | description_if_stanza
   | encapsulation_if_stanza
   | flexible_vlan_tagging_if_stanza
   | framing_if_stanza
   | gigether_options_if_stanza
   | link_mode_if_stanza
   | mac_if_stanza
   | mtu_if_stanza
   | traps_if_stanza
   | vlan_tagging_if_stanza
;

null_u_if_stanza
:
   description_u_if_stanza
   | encapsulation_u_if_stanza
   | input_vlan_map_u_if_stanza
   | output_vlan_map_u_if_stanza
   | tunnel_u_if_stanza
   | vlan_tags_u_if_stanza
;

output_vlan_map_u_if_stanza // TODO [Ask Ari]: probably not supposed to ignore this

:
   OUTPUT_VLAN_MAP VARIABLE SEMICOLON
;

policer_fam_u_if_stanza
:
   POLICER ignored_substanza
;

port_mode_fam_u_if_stanza // TODO [P0]: should not be ignored

:
   PORT_MODE VARIABLE
;

primary_fam_u_if_stanza
:
   PRIMARY SEMICOLON
;

rpf_check_fam_u_if_stanza
:
   RPF_CHECK FAIL_FILTER? URPF_LOGGING? SEMICOLON
;

targeted_broadcast_fam_u_if_stanza
:
   TARGETED_BROADCAST SEMICOLON
;

traps_if_stanza
:
   TRAPS SEMICOLON
;

tunnel_u_if_stanza // TODO [Ask Ari]: probably not supposed to ignore this

:
   TUNNEL ignored_substanza
;

u_if_stanza
:
   apply_groups_u_if_stanza
   | disable_u_if_stanza
   | enable_u_if_stanza
   | family_u_if_stanza
   | vlanid_u_if_stanza
   | null_u_if_stanza
;

unit_if_stanza
:
   UNIT
   (
      num = DEC
      | UNIT_WILDCARD
   )
   (
      SEMICOLON
      |
      (
         OPEN_BRACE
         (
            u_if_stanza
            | inactive_u_if_stanza
         )+ CLOSE_BRACE
      )
   )
;

vlan_id_fam_u_if_stanza // TODO [P0]: should not be ignored

:
   VLAN_ID DEC SEMICOLON
;

vlan_members_fam_u_if_stanza
:
   VLAN OPEN_BRACE MEMBERS integer_list CLOSE_BRACE
;

vlan_tagging_if_stanza
:
   VLAN_TAGGING SEMICOLON
;

vlan_tags_u_if_stanza // TODO [Ask Ari]: probably not supposed to ignore this

:
   VLAN_TAGS
   (
      OUTER
      (
         DEC
         |
         (
            HEX PERIOD DEC
         )
      )
   )?
   (
      INNER
      (
         DEC
         |
         (
            HEX PERIOD DEC
         )
      )
   )? SEMICOLON
;

vlanid_u_if_stanza
:
   VLAN_ID DEC SEMICOLON
;
