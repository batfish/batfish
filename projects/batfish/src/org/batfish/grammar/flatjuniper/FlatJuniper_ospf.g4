parser grammar FlatJuniper_ospf;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

ait_apply_groups_except
:
   s_apply_groups_except
;

ait_dead_interval
:
   DEAD_INTERVAL DEC
;

ait_hello_interval
:
   HELLO_INTERVAL DEC
;

ait_interface_type
:
   INTERFACE_TYPE P2P
;

ait_ldp_synchronization
:
   LDP_SYNCHRONIZATION
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
   ) s_null_filler
;

ait_passive
:
   PASSIVE
;

ait_priority
:
   PRIORITY DEC
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
   AREA_RANGE IP_PREFIX
;

at_interface
:
   INTERFACE
   (
      id = interface_id
      | WILDCARD
   ) at_interface_tail
;

at_interface_tail
:
// intentional blank

   | ait_apply_groups_except
   | ait_dead_interval
   | ait_hello_interval
   | ait_interface_type
   | ait_ldp_synchronization
   | ait_metric
   | ait_null
   | ait_passive
   | ait_priority
   | ait_te_metric
;

at_label_switched_path
:
   LABEL_SWITCHED_PATH
   (
      name = variable
      | WILDCARD
   ) at_label_switched_path_tail
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
// intentional blank

   | nssat_default_lsa
;

at_null
:
   (
      AUTHENTICATION_TYPE
   ) s_null_filler
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
   AREA
   (
      area = IP_ADDRESS
      | WILDCARD
   ) ot_area_tail
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
   EXPORT name = variable
;

ot_external_preference
:
   EXTERNAL_PREFERENCE DEC
;

ot_import
:
   IMPORT name = variable
;

ot_no_active_backbone
:
   NO_ACTIVE_BACKBONE
;

ot_null
:
   (
      OVERLOAD
      | REFERENCE_BANDWIDTH
      | TRACEOPTIONS
      | TRAFFIC_ENGINEERING
   ) s_null_filler
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
   | ot_external_preference
   | ot_import
   | ot_no_active_backbone
   | ot_null
;

s_protocols_ospf3
:
   OSPF3 s_protocols_ospf_tail
;
