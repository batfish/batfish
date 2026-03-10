parser grammar Legacy_qos;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

class_name: WORD;

cm_match
:
   num = dec? MATCH NOT?
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
      num = dec
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
      dec+
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
      dec+
      | name = variable
   ) NEWLINE
;

cmm_protocol
:
   PROTOCOL null_rest_of_line
;

cmm_qos_group
:
   QOS_GROUP dec NEWLINE
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

pm_class
:
   CLASS (CLASS_DEFAULT | BUILT_IN? name = class_name) NEWLINE
   (
      pmc_null
      | pmc_police
      | pmc_service_policy
   )*
;

pm_event
:
   EVENT null_rest_of_line
   (
      pm_event_class
   )*
;

pm_event_class
:
   dec CLASS
   (
      ALWAYS
      | classname = variable
   ) DO_UNTIL_FAILURE NEWLINE
   (
      dec
      (
         ACTIVATE SERVICE_TEMPLATE stname = variable
         | AUTHENTICATE
         | AUTHENTICATION_RESTART
         | AUTHORIZE
         | CLEAR_SESSION
         | PAUSE
         | RESTRICT
         | RESUME
         | TERMINATE
      ) null_rest_of_line
   )*
;

pm_null
:
   NO?
   (
      CIR
      | COUNT
      | DESCRIPTION
   ) null_rest_of_line
;

pm_parameters
:
   PARAMETERS NEWLINE
   (
      pmp_null
   )*
;

pmc_null
:
   NO?
   (
      BANDWIDTH
      | CONGESTION_CONTROL
      | DBL
      | DROP
      | FAIR_QUEUE
      | INSPECT
      | MTU
      | PASS
      | PAUSE
      | PRIORITY
      | QUEUE_BUFFERS
      | QUEUE_LIMIT
      | RANDOM_DETECT
      | SET
      | SHAPE
      | TRUST
      | USER_STATISTICS
   ) null_rest_of_line
;

pmc_police
:
   POLICE null_rest_of_line
   (
      pmcp_null
   )*
;

pmc_service_policy
:
   SERVICE_POLICY name = variable NEWLINE
;

pmcp_null
:
   NO?
   (
      CONFORM_ACTION
      | EXCEED_ACTION
      | VIOLATE_ACTION
   ) null_rest_of_line
;

pmp_null
:
   NO?
   (
      ID_MISMATCH
      | ID_RANDOMIZATION
      | MESSAGE_LENGTH
      | PROTOCOL_VIOLATION
      | TCP_INSPECTION
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
      cm_match
   )*
;

s_policy_map
:
   POLICY_MAP
   (
     (
       TYPE
       (
         CONTROL_PLANE           // < (in 4.18, not in 4.22)
         | COPP                  // > (in 4.22, not in 4.18)
         | PBR                   // in both 4.18 and 4.25
         | PDP SHARED?           // > (not in 4.18, in 4.25)
         | QOS                   // < (in 4.18, not in 4.25)
         | QUALITY_OF_SERVICE    // > (not in 4.18, in 4.25)
       )
     )? // implicit type is QOS
     mapname = variable
   ) NEWLINE
   (
      pm_class
      | pm_event
      | pm_null
      | pm_parameters
   )*
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
   INACTIVITY_TIMER dec NEWLINE
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
   VLAN dec NEWLINE
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
