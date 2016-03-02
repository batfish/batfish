parser grammar FlatJuniper_fabric;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

fabat_interconnect_device
:
   INTERCONNECT_DEVICE name1 = variable name2 = variable
;

fabat_node_device
:
   NODE_DEVICE name1 = variable name2 = variable
;

fabrngt_network_domain
:
   NETWORK_DOMAIN
;

fabrngt_node_device
:
   NODE_DEVICE node = variable
;

fabrt_node_group
:
   NODE_GROUP group = variable fabrt_node_group_tail
;

fabrt_node_group_tail
:
   fabrngt_network_domain
   | fabrngt_node_device
;

fabt_aliases
:
   ALIASES fabt_aliases_tail
;

fabt_aliases_tail
:
   fabat_interconnect_device
   | fabat_node_device
;

fabt_resources
:
   RESOURCES fabt_resources_tail
;

fabt_resources_tail
:
   fabrt_node_group
;

s_fabric
:
   FABRIC s_fabric_tail
;

s_fabric_tail
:
   fabt_aliases
   | fabt_resources
;