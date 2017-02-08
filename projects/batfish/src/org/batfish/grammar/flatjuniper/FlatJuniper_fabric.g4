parser grammar FlatJuniper_fabric;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

fab_aliases
:
   ALIASES
   (
      faba_interconnect_device
      | faba_node_device
   )
;

fab_resources
:
   RESOURCES
   (
      fabr_node_group
   )
;

faba_interconnect_device
:
   INTERCONNECT_DEVICE name1 = variable name2 = variable
;

faba_node_device
:
   NODE_DEVICE name1 = variable name2 = variable
;

fabr_node_group
:
   NODE_GROUP group = variable
   (
      fabrn_network_domain
      | fabrn_node_device
   )
;

fabrn_network_domain
:
   NETWORK_DOMAIN
;

fabrn_node_device
:
   NODE_DEVICE node = variable
;

s_fabric
:
   FABRIC
   (
      fab_aliases
      | fab_resources
   )
;
