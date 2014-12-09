parser grammar FlatJuniperGrammar_ospf;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

ait_apply_groups_except
:
   s_apply_groups_except
;

ait_interface_type
:
   INTERFACE_TYPE P2P
;

ait_metric
:
   METRIC DEC
;

ait_null
:
   (
      AUTHENTICATION
      | BFD_LIVENESS_DETECTION
   ) ~NEWLINE*
;

ait_passive
:
   PASSIVE
;

ait_te_metric
:
   TE_METRIC DEC
;

alt_metric
:
   METRIC DEC
;

at_apply_groups
:
   s_apply_groups
;

at_area_range
:
   AREA_RANGE IP_ADDRESS_WITH_MASK
;

at_interface
:
   at_interface_header at_interface_tail
;

at_interface_header
:
   INTERFACE
   (
      variable
      | WILDCARD
   )
;

at_interface_tail
:
   ait_apply_groups_except
   | ait_interface_type
   | ait_metric
   | ait_null
   | ait_passive
   | ait_te_metric
;

at_label_switched_path
:
   at_label_switched_path_header at_label_switched_path_tail
;

at_label_switched_path_header
:
   LABEL_SWITCHED_PATH
   (
      variable
      | WILDCARD
   )
;

at_label_switched_path_tail
:
   alt_metric
;

at_nssa
:
   NSSA at_nssa_tail
;

at_nssa_tail
:
   nssat_default_lsa
;

at_null
:
   (
      AUTHENTICATION_TYPE
   ) ~NEWLINE*
;

dlsat_default_metric
:
   DEFAULT_METRIC DEC
;

dlsat_metric_type
:
   METRIC_TYPE
   (
      METRIC_TYPE_1
      | METRIC_TYPE_2
   )
;

dlsat_type_7
:
   TYPE_7
;

nssat_default_lsa
:
   DEFAULT_LSA nssat_default_lsa_tail
;

nssat_default_lsa_tail
:
   dlsat_default_metric
   | dlsat_metric_type
   | dlsat_type_7
;

ot_apply_groups
:
   s_apply_groups
;

ot_area
:
   ot_area_header ot_area_tail
;

ot_area_header
:
   AREA
   (
      IP_ADDRESS
      | WILDCARD
   )
;

ot_area_tail
:
   at_apply_groups
   | at_area_range
   | at_interface
   | at_label_switched_path
   | at_nssa
   | at_null
;

ot_export
:
   EXPORT variable
;

ot_null
:
   (
      OVERLOAD
      | REFERENCE_BANDWIDTH
      | TRACEOPTIONS
      | TRAFFIC_ENGINEERING
   ) ~NEWLINE*
;

s_protocols_ospf
:
   OSPF s_protocols_ospf_tail
;

s_protocols_ospf_tail
:
   ot_apply_groups
   | ot_area
   | ot_export
   | ot_null
;
