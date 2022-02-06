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
   INTERCONNECT_DEVICE name1 = junos_name name2 = junos_name
;

faba_node_device
:
   NODE_DEVICE name1 = junos_name name2 = junos_name
;

fabr_node_group
:
   NODE_GROUP group = junos_name
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
   NODE_DEVICE node = junos_name
;

s_fabric
:
   FABRIC
   (
      fab_aliases
      | fab_resources
   )
;
