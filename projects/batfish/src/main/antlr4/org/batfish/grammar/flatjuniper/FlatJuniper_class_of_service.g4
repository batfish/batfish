parser grammar FlatJuniper_class_of_service;

import FlatJuniper_common;

s_class_of_service
:
   CLASS_OF_SERVICE
   (
       scos_code_point_aliases
       | scos_null
   )
;

scos_code_point_aliases
:
    CODE_POINT_ALIASES DSCP name = junos_name dec
;

scos_null
:
   (
      ADAPTIVE_SHAPERS
      | APPLICATION_TRAFFIC_CONTROL
      | CLASSIFIERS
      | DROP_PROFILES
      | FORWARDING_CLASSES
      | FORWARDING_POLICY
      | FRAGMENTATION_MAPS
      | HOST_OUTBOUND_TRAFFIC
      | INTERFACES
      | LOSS_PRIORITY_MAPS
      | NON_STRICT_PRIORITY_SCHEDULING
      | RESTRICTED_QUEUES
      | REWRITE_RULES
      | ROUTING_INSTANCES
      | SCHEDULER_MAPS
      | SCHEDULERS
      | SHARED_BUFFER
      | TRACE_OPTIONS
      | TRAFFIC_CONTROL_PROFILES
      | TRANSLATION_TABLE
      | TRI_COLOR
      | VIRTUAL_CHANNEL
   )
   null_filler
;

