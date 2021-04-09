parser grammar CiscoXr_qos;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

cm_end_class_map
:
   END_CLASS_MAP NEWLINE
;

cm_match
:
   num = uint_legacy? MATCH NOT?
   (
      cmm_access_group
      | cmm_access_list
      | cmm_activated_service_template
      | cmm_any
      | cmm_authorization_status
      | cmm_class_map
      | cmm_cos
      | cmm_default_inspection_traffic
      | cmm_dscp
      | cmm_exception
      | cmm_method
      | cmm_mpls
      | cmm_non_client_nrt
      | cmm_port
      | cmm_precedence
      | cmm_protocol
      | cmm_qos_group
      | cmm_redirect
      | cmm_result_type
      | cmm_service_template
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
      num = uint_legacy
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

cmm_activated_service_template
:
   ACTIVATED_SERVICE_TEMPLATE name = variable NEWLINE
;

cmm_any
:
   ANY NEWLINE
;

cmm_authorization_status
:
   AUTHORIZATION_STATUS
   (
      AUTHORIZED
      | UNAUTHORIZED
   ) NEWLINE
;

cmm_class_map
:
   CLASS_MAP name = variable NEWLINE
;

cmm_cos
:
   COS
   (
      uint_legacy+
      | range
   ) NEWLINE
;

cmm_default_inspection_traffic
:
   DEFAULT_INSPECTION_TRAFFIC NEWLINE
;

cmm_dscp
:
   IP? DSCP IPV4?
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

cmm_method
:
   METHOD
   (
      DOT1X
      | MAB
      | WEBAUTH
   ) NEWLINE
;

cmm_mpls
:
   MPLS null_rest_of_line
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
      uint_legacy+
      | name = variable
   ) NEWLINE
;

cmm_protocol
:
   PROTOCOL null_rest_of_line
;

cmm_qos_group
:
   QOS_GROUP uint_legacy NEWLINE
;

cmm_redirect
:
   REDIRECT ~NEWLINE NEWLINE
;

cmm_result_type
:
   RESULT_TYPE
   (
      METHOD
      (
         DOT1X
         | MAB
         | WEBAUTH
      )
   )? variable NEWLINE
;

cmm_service_template
:
   SERVICE_TEMPLATE name = variable NEWLINE
;

color_setter
:
   SET_COLOR
   (
      RED
      | YELLOW
      | GREEN
   )
;

inspect_protocol
:
   HTTP
   | HTTPS
   | ICMP
   | TCP
   | TFTP
   | UDP
;

match_semantics
:
   MATCH_ALL
   | MATCH_ANY
;

og_network
:
   NETWORK name = variable_permissive NEWLINE
   (
      ogn_description
      | ogn_group_object
      | ogn_host_ip
      | ogn_ip_with_mask
      | ogn_network_object
   )*
;

ogn_description
:
   description_line
;

ogn_group_object
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

ogn_host_ip
:
   HOST ip = IP_ADDRESS NEWLINE
;

ogn_ip_with_mask
:
   ip = IP_ADDRESS mask = IP_ADDRESS NEWLINE
;

ogn_network_object
:
   NETWORK_OBJECT
   (
      HOST
      (
         address = IP_ADDRESS
         | address6 = IPV6_ADDRESS
         // Do not reorder: variable_permissive captures all tokens in line

         | host = variable_permissive
      )
      | wildcard_address = IP_ADDRESS wildcard_mask = IP_ADDRESS
      | prefix = IP_PREFIX
      | prefix6 = IPV6_PREFIX
      | OBJECT name = variable_permissive
      // Do not reorder: variable_permissive captures all tokens in line

      | host = variable_permissive
   ) NEWLINE
;

on_group
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

pm_end_policy_map
:
   END_POLICY_MAP NEWLINE
;

pm_type_accounting
:
   TYPE ACCOUNTING mapname = variable NEWLINE
   pm_type_null_tail*
;

pm_type_null_tail
:
     (
         CLASS
         | DESCRIPTION
     )  null_rest_of_line
;

pm_type_control_subscriber
:
   TYPE CONTROL SUBSCRIBER mapname = variable NEWLINE
   (
      pmtcs_event
      | pm_type_null_tail
   )*
;

pm_type_pbr
:
   TYPE PBR mapname = variable NEWLINE
   pm_type_null_tail*
;

pm_type_performance_traffic
:
   TYPE PERFORMANCE_TRAFFIC mapname = variable NEWLINE
   pm_type_null_tail*
;

pm_type_qos
:
   (TYPE QOS)? mapname = variable NEWLINE
   pm_type_null_tail*
;

pm_type_redirect
:
   TYPE REDIRECT mapname = variable NEWLINE
   pm_type_null_tail*
;

pm_type_traffic
:
   TYPE TRAFFIC mapname = variable NEWLINE
   pm_type_null_tail*
;

pmtcs_event
:
   EVENT null_rest_of_line
   (
      pmtcse_class
   )*
;

pmtcse_class
:
   CLASS
   (
      CLASS_DEFAULT
      | TYPE CONTROL SUBSCRIBER classname = variable
   )
   pmtcsec_do?
   NEWLINE
   pmtcsec_tail*
;

pmtcsec_do
:
  DO_ALL
  | DO_UNTIL_FAILURE
  | DO_UNTIL_SUCCESS
;

pmtcsec_tail
:
  uint_legacy
  (
     pmtcsec_activate
     | pmtcsec_null
  )
;

pmtcsec_activate
:
  ACTIVATE DYNAMIC_TEMPLATE dtname = variable NEWLINE
;

pmtcsec_null
:
  (
   AUTHENTICATE
   | AUTHORIZE
   | DEACTIVATE
   | DISCONNECT
   | MONITOR
   | SET_TIMER
   | STOP_TIMER
) null_rest_of_line
;

s_class_map
:
   CLASS_MAP
   (
      TYPE
      (
         CONTROL SUBSCRIBER
         | CONTROL_PLANE
         | NETWORK_QOS
         | PBR
         | QOS
         | QUEUING
      )
   )?
   (
      MATCH_ALL
      | MATCH_ANY
      | MATCH_NONE
   )? name = variable NEWLINE
   (
      DESCRIPTION ~NEWLINE+ NEWLINE
   )?
   (
      cm_end_class_map
      | cm_match
   )*
;

s_object_group
:
   OBJECT_GROUP
   (
      og_network
   )*
;

s_policy_map
:
   POLICY_MAP
   (
      pm_type_accounting
      | pm_type_control_subscriber
      | pm_type_pbr
      | pm_type_performance_traffic
      | pm_type_qos
      | pm_type_redirect
      | pm_type_traffic
   )
   pm_end_policy_map?
;

variable_policy_map_header
:
   ~( TYPE | NEWLINE )
;
