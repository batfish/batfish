parser grammar Cisco_qos;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

cm_end_class_map
:
   END_CLASS_MAP NEWLINE
;

cm_match
:
   MATCH
   (
      cmm_access_group
      | cmm_access_list
      | cmm_any
      | cmm_cos
      | cmm_default_inspection_traffic
      | cmm_dscp
      | cmm_exception
      | cmm_mpls
      | cmm_non_client_nrt
      | cmm_port
      | cmm_precedence
      | cmm_protocol
      | cmm_redirect
      | cmm_qos_group
   )
;

cmm_access_group
:
   (
      IP
      | IPV6
   )? ACCESS_GROUP
   (
      IP
      | IPV6
      | IPV4
   )?
   (
      num = DEC
      |
      (
         NAME name = variable
      )
      |
      (
         name = variable color_setter?
      )
   ) NEWLINE
;

cmm_access_list
:
   ACCESS_LIST name = variable NEWLINE
;

cmm_any
:
   ANY NEWLINE
;

cmm_cos
:
   COS range NEWLINE
;

cmm_default_inspection_traffic
:
   DEFAULT_INSPECTION_TRAFFIC NEWLINE
;

cmm_dscp
:
   IP? DSCP
   (
      (
         dscp_types += dscp_type
      )+
      |
      (
         dscp_range = range
      )
   ) NEWLINE
;

cmm_exception
:
   EXCEPTION ~NEWLINE+ NEWLINE
;

cmm_mpls
:
   MPLS ~NEWLINE* NEWLINE
;

cmm_non_client_nrt
:
   NON_CLIENT_NRT NEWLINE
;

cmm_port
:
   PORT
   (
      TCP
      | UDP
   ) port_specifier NEWLINE
;

cmm_precedence
:
   IP? PRECEDENCE IPV4?
   (
      DEC+
      | name = variable
   ) NEWLINE
;

cmm_protocol
:
   PROTOCOL ~NEWLINE* NEWLINE
;

cmm_qos_group
:
   QOS_GROUP DEC NEWLINE
;

cmm_redirect
:
   REDIRECT ~NEWLINE NEWLINE
;

pm_class
:
   CLASS ~NEWLINE* NEWLINE pmc_null*
;

pm_end_policy_map
:
   END_POLICY_MAP NEWLINE
;

pmc_null
:
   NO?
   (
      BANDWIDTH
      | CONGESTION_CONTROL
      | MTU
      | PAUSE
      | POLICE
      | PRIORITY
      | QUEUE_LIMIT
      | SET
   ) ~NEWLINE* NEWLINE
;

s_class_map
:
   CLASS_MAP
   (
      TYPE ~NEWLINE
   )?
   (
      MATCH_ALL
      | MATCH_ANY
   )? name = variable NEWLINE
   (
      DESCRIPTION ~NEWLINE+ NEWLINE
   )? s_class_map_tail*
;

s_class_map_tail
:
   cm_end_class_map
   | cm_match
;

s_object
:
   OBJECT s_object_tail
;

s_object_tail
:
   o_network
   | o_service
;

s_object_group
:
   OBJECT_GROUP s_object_group_tail
;

s_object_group_tail
:
   og_icmp_type
   | og_ip_address
   | og_network
   | og_protocol
   | og_service
   | og_user
;

s_policy_map
:
   POLICY_MAP ~NEWLINE* NEWLINE
   (
      pm_class
      | pm_end_policy_map
   )*
;