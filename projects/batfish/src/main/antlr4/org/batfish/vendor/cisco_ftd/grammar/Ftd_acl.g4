parser grammar Ftd_acl;

options {
   tokenVocab = FtdLexer;
}

access_list_stanza
:
   ACCESS_LIST acl_name
   (
      acl_remark
      | acl_advanced
      | acl_extended
      | acl_implicit_extended
   )
;

acl_name
:
   (
      ~(
         REMARK
         | ADVANCED
         | EXTENDED
         | STANDARD
         | PERMIT
         | DENY
         | TRUST
         | NEWLINE
      )
   )+
;

acl_remark
:
   REMARK remark_text = RAW_TEXT? NEWLINE
;

acl_advanced
:
   ADVANCED action = access_list_action protocol ifc_clause_null? src_spec_null dst_spec? acl_options* NEWLINE
;

acl_extended
:
   EXTENDED action = access_list_action protocol src_spec_null dst_spec? acl_options* NEWLINE
;

acl_implicit_extended
:
   action = access_list_action protocol src_spec_null dst_spec? acl_options* NEWLINE
;

ifc_clause_null
:
   IFC acl_ifc_name_null
;

acl_ifc_name_null
:
   (
      ~(
         HOST
         | IP_ADDRESS
         | OBJECT
         | OBJECT_GROUP
         | ANY
         | ANY4
         | ANY6
         | NEWLINE
      )
   )+
;

src_spec_null
:
   acl_address_spec
;

dst_spec
:
   acl_address_spec port_spec?
;

acl_address_spec
:
   HOST ip = IP_ADDRESS
   | OBJECT object_name_null
   | OBJECT_GROUP object_group_name_null
   | ANY
   | ANY4
   | ANY6
   | ip = IP_ADDRESS mask = IP_ADDRESS
;

object_name_null
:
   (
      ~(
         HOST
         | IP_ADDRESS
         | OBJECT
         | OBJECT_GROUP
         | ANY
         | ANY4
         | ANY6
         | EQ
         | GT
         | LT
         | NEQ
         | RANGE
         | RULE_ID
         | EVENT_LOG
         | INACTIVE
         | LOG
         | TIME_RANGE
         | NEWLINE
      )
   )+
;

object_group_name_null
:
   (
      ~(
         HOST
         | IP_ADDRESS
         | OBJECT
         | OBJECT_GROUP
         | ANY
         | ANY4
         | ANY6
         | EQ
         | GT
         | LT
         | NEQ
         | RANGE
         | RULE_ID
         | EVENT_LOG
         | INACTIVE
         | LOG
         | TIME_RANGE
         | NEWLINE
      )
   )+
;

port_spec
:
   port_specifier
   | OBJECT_GROUP port_object_group_name_null
;

port_object_group_name_null
:
   (
      ~(
         RULE_ID
         | EVENT_LOG
         | INACTIVE
         | LOG
         | TIME_RANGE
         | NEWLINE
      )
   )+
;

acl_options
:
   RULE_ID id = dec
   | EVENT_LOG log_option = (FLOW_END | FLOW_START | FLOW_NSEL)
   | INACTIVE
   | LOG log_level_null?
   | TIME_RANGE time_range_name
;

time_range_name
:
   (
      ~(
         RULE_ID
         | EVENT_LOG
         | INACTIVE
         | LOG
         | NEWLINE
      )
   )+
;

log_level_null
:
   DEFAULT
   | EMERGENCIES
   | ALERTS
   | CRITICAL
   | ERRORS
   | WARNINGS
   | NOTIFICATIONS
   | INFORMATIONAL
   | DEBUGGING
   | dec
;
