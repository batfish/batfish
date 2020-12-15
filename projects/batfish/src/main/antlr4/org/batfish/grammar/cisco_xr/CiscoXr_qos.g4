parser grammar CiscoXr_qos;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

cm_end_class_map
:
   END_CLASS_MAP NEWLINE
;

cm_ios_inspect
:
   INSPECT HTTP? match_semantics? name = variable_permissive NEWLINE
   cm_iosi_match*
;

cm_iosi_match
:
   MATCH
   (
      cm_iosim_access_group
      | cm_iosim_protocol
      | cm_iosim_req_resp
      | cm_iosim_request
      | cm_iosim_response
   )
;

cm_iosim_access_group
:
   ACCESS_GROUP NAME name = variable_permissive NEWLINE
;

cm_iosim_protocol
:
   PROTOCOL inspect_protocol NEWLINE
;

cm_iosim_req_resp
:
   NOT? REQ_RESP CONTENT_TYPE MISMATCH
;

cm_iosim_request
:
   NOT? REQUEST null_rest_of_line
;

cm_iosim_response
:
   NOT? RESPONSE null_rest_of_line
;

cm_match
:
   num = DEC? MATCH NOT?
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
      DEC+
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
      DEC+
      | name = variable
   ) NEWLINE
;

cmm_protocol
:
   PROTOCOL null_rest_of_line
;

cmm_qos_group
:
   QOS_GROUP DEC NEWLINE
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

o_network
:
   NETWORK name = variable_permissive NEWLINE
   (
      on_description
      | on_fqdn
      | on_host
      | on_range
      | on_subnet
   )*
;

o_service
:
   SERVICE name = variable_permissive NEWLINE
   (
      os_description
      | os_service
   )*
;

ogg_icmp_type
:
   ICMP_TYPE name = variable_permissive NEWLINE
   (
      og_description
      | oggit_group_object
   )*
;

oggit_group_object
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

ogg_network
:
   NETWORK name = variable_permissive NEWLINE
   (
      og_description
      | oggn_group_object
   )*
;

oggn_group_object
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

ogg_protocol
:
   PROTOCOL name = variable_permissive NEWLINE
   (
      og_description
      | oggp_group_object
   )*
;

oggp_group_object
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

ogg_service
:
   SERVICE name = variable_group_id
   (
      protocol_type = service_group_protocol
   )?
   NEWLINE
   (
      og_description
      | oggs_group_object
   )*
;

oggs_group_object
:
   GROUP_OBJECT name = variable_group_id NEWLINE
;

og_description
:
   description_line
;

og_group
:
   GROUP
   (
      ogg_icmp_type
      | ogg_network
      | ogg_protocol
      | ogg_service
   )
;

og_icmp_type
:
   ICMP_TYPE name = variable_permissive NEWLINE
   (
      ogit_description
      | ogit_group_object
      | ogit_icmp_object
   )*
;

og_ip_address
:
   IP ADDRESS name = variable_permissive NEWLINE
   (
      ogipa_host_info
      | ogipa_ip_addresses
   )*
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

og_protocol
:
   PROTOCOL name = variable_permissive NEWLINE
   (
      ogp_description
      | ogp_group_object
      | ogp_protocol_object
   )*
;

og_service
:
   SERVICE name = variable_group_id
   (
      protocol_type = service_group_protocol
   )?
   NEWLINE
   (
      ogs_description
      | ogs_group_object
      | ogs_icmp
      | ogs_service_object
      | ogs_tcp
      | ogs_udp
      | ogs_port_object
   )*
;

og_user
:
   USER name = variable_permissive NEWLINE
   (
      ogu_description
      | ogu_group_object
      | ogu_user
      | ogu_user_group
   )*
;

ogipa_host_info
:
   HOST_INFO IP_ADDRESS NEWLINE
;

ogipa_ip_addresses
:
   (
      IP_ADDRESS+
      |
      (
         num = DEC
         (
            HOST IP_ADDRESS
            | IP_PREFIX
         )
      )
   ) NEWLINE
;

ogit_description
:
   description_line
;

ogit_group_object
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

ogit_icmp_object
:
   ICMP_OBJECT icmp_object_type NEWLINE
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

ogp_description
:
   description_line
;

ogp_group_object
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

ogp_protocol_object
:
   PROTOCOL_OBJECT protocol NEWLINE
;

ogs_description
:
   description_line
;

ogs_group_object
:
   GROUP_OBJECT name = variable_group_id NEWLINE
;

ogs_icmp
:
   ICMP NEWLINE
;

ogs_service_object
:
   SERVICE_OBJECT
   (
      (
         service_specifier
      )
      |
      (
         OBJECT name = variable
      )
   ) NEWLINE
;

ogs_tcp
:
   TCP ps = port_specifier NEWLINE
;

ogs_udp
:
   UDP ps = port_specifier NEWLINE
;

ogs_port_object
:
   PORT_OBJECT ps = port_specifier NEWLINE
;

ogu_description
:
   description_line
;

ogu_group_object
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

ogu_user
:
   USER name = variable_permissive NEWLINE
;

ogu_user_group
:
   name = variable_permissive NEWLINE
;

on_description
:
   description_line
;

on_fqdn
:
   FQDN
   (
      V4
      | V6
   )? fqdn = variable_permissive NEWLINE
;

on_host
:
   HOST
   (
      address = IP_ADDRESS
      | address6 = IPV6_ADDRESS
   ) NEWLINE
;

on_range
:
   RANGE start = IP_ADDRESS end = IP_ADDRESS NEWLINE
;

on_subnet
:
   SUBNET
   (
      (
         address = IP_ADDRESS mask = IP_ADDRESS
      )
      | prefix6 = IPV6_PREFIX
   ) NEWLINE
;

on_group
:
   GROUP_OBJECT name = variable_permissive NEWLINE
;

os_description
:
   description_line
;

os_service
:
   SERVICE service_specifier NEWLINE
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
  DEC
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

qm_null
:
   NO?
   (
      DSCP
      | DSCP_VALUE
      | PCP
      | PCP_VALUE
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

s_class_map_ios
:
   CLASS_MAP TYPE cm_ios_inspect
;

s_object
:
   OBJECT
   (
      o_network
      | o_service
   )*
;

s_object_group
:
   OBJECT_GROUP
   (
      og_group
      | og_icmp_type
      | og_ip_address
      | og_network
      | og_protocol
      | og_service
      | og_user
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

s_qos_mapping
:
   QOS_MAPPING NEWLINE
   (
      qm_null
   )*
;

s_service_template
:
   SERVICE_TEMPLATE name = variable NEWLINE
   (
      st_access_group
      | st_description
      | st_inactivity_timer
      | st_linksec
      | st_tag
      | st_vlan
      | st_voice_vlan
   )*
;

s_table_map
:
   TABLE_MAP name = variable NEWLINE
   (
      table_map_null
   )*
;

st_access_group
:
   ACCESS_GROUP name = variable NEWLINE
;

st_description
:
   DESCRIPTION null_rest_of_line
;

st_inactivity_timer
:
   INACTIVITY_TIMER DEC NEWLINE
;

st_linksec
:
   LINKSEC POLICY
   (
      MUST_SECURE
      | SHOULD_SECURE
   ) NEWLINE
;

st_tag
:
   TAG name = variable NEWLINE
;

st_vlan
:
   VLAN DEC NEWLINE
;

st_voice_vlan
:
   VOICE VLAN NEWLINE
;

table_map_null
:
   NO?
   (
      DEFAULT
      | FROM
      | MAP
   ) null_rest_of_line
;

variable_policy_map_header
:
   ~( TYPE | NEWLINE )
;
