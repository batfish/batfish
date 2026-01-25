parser grammar Ftd_object;

options {
   tokenVocab = FtdLexer;
}

object_stanza
:
   OBJECT object_type name = object_name_text NEWLINE
   object_tail*
;

object_type
:
   NETWORK
   | SERVICE
;

object_tail
:
   (
      object_host
      | object_subnet
      | object_fqdn
      | description_line
      | object_unrecognized_line
   )
;

// Like unrecognized_line but doesn't match lines starting with stanza keywords
object_unrecognized_line
:
   ~(NEWLINE | INTERFACE | ACCESS_LIST | OBJECT | OBJECT_GROUP | HOSTNAME | ROUTE | NAT | FAILOVER | CRYPTO | ACCESS_GROUP)
   ~NEWLINE* NEWLINE
;

object_host
:
   HOST ip = IP_ADDRESS NEWLINE
;

object_subnet
:
   SUBNET network = IP_ADDRESS mask = IP_ADDRESS NEWLINE
;

object_fqdn
:
   FQDN fqdn_name (ID id_value = dec)? NEWLINE
;

fqdn_name
:
   ~NEWLINE+
;

object_group_stanza
:
   OBJECT_GROUP group_type name = object_group_name_text NEWLINE
   object_group_tail*
;

group_type
:
   NETWORK
   | SERVICE protocol?
;

object_group_tail
:
   (
      og_network_object
      | og_group_object
      | og_service_object
      | og_port_object
      | description_line
      // Use object_unrecognized_line to avoid consuming lines starting stanza keywords
      | object_unrecognized_line
   )
;

og_network_object
:
   NETWORK_OBJECT
   (
      HOST ip = IP_ADDRESS
      | OBJECT obj_name
      | network = IP_ADDRESS mask = IP_ADDRESS
   )
   NEWLINE
;

obj_name
:
   ~NEWLINE+
;

og_group_object
:
   GROUP_OBJECT name = object_group_name_text NEWLINE
;

object_name_text
:
   (~NEWLINE)+
;

object_group_name_text
:
   (~NEWLINE)+
;

og_service_object
:
   SERVICE_OBJECT protocol
   (
      SOURCE port_spec
   )?
   (
      DESTINATION port_spec
   )?
   NEWLINE
;

og_port_object
:
   PORT_OBJECT
   (
      EQ port = port_value
      | RANGE port_low = port_value port_high = port_value
   )
   NEWLINE
;

port_value
:
   dec | service_name
;

service_name
:
   ~NEWLINE+
;

object_group_search_stanza
:
   OBJECT_GROUP_SEARCH null_rest_of_line
;
