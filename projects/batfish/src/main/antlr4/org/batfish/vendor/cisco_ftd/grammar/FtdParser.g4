parser grammar FtdParser;

import
Ftd_common,
Ftd_interface,
Ftd_acl,
Ftd_object,
Ftd_route,
Ftd_nat,
Ftd_failover,
Ftd_crypto,
Ftd_tunnel_group,
Ftd_ospf,
Ftd_bgp;

options {
   superClass = 'org.batfish.vendor.cisco_ftd.grammar.FtdBaseParser';
   tokenVocab = FtdLexer;
}

ftd_configuration
:
   NEWLINE?
   (
      stanza
      | NEWLINE
   )* EOF
;

// ============================================================================
// MINIMAL STANZA DISPATCH
// Only parse in detail what we need to extract, everything else is unrecognized
// ============================================================================

stanza
:
   // Essential stanzas with data extraction (from imported grammars)
   interface_stanza
   | access_list_stanza
   | access_group_stanza
   | object_stanza
   | object_group_stanza
   | route_stanza
   | nat_stanza
   | router_bgp_stanza
   | router_ospf_stanza
   | crypto_stanza
   | tunnel_group_stanza
   | failover_stanza

   // Minimal extraction stanzas (defined here)
   | hostname_stanza
   | names_stanza
   | mtu_stanza
   | arp_stanza
   | class_map_stanza
   | policy_map_stanza
   | service_policy_stanza
   | cryptochecksum_stanza

   // Everything else - just consume
   | unrecognized_stanza
;

// ============================================================================
// ESSENTIAL STANZAS - Minimal parsing for data extraction
// ============================================================================

hostname_stanza
:
   HOSTNAME raw_text = RAW_TEXT NEWLINE
;

names_stanza
:
   NO? NAMES NEWLINE
;

mtu_stanza
:
   MTU iface_name = ~NEWLINE+ mtu_value = dec NEWLINE
;

arp_stanza
:
   ARP null_rest_of_line
;

cryptochecksum_stanza
:
   CRYPTOCHECKSUM_LINE
;

// Class/Policy maps - needed for service policy bindings
class_map_stanza
:
   CLASS_MAP class_map_type? name = class_map_name NEWLINE
   class_map_tail*
;

class_map_type
:
   TYPE type = class_map_type_value
;

class_map_type_value
:
   NAME
   | WORD
;

class_map_name
:
   name_parts += ~NEWLINE+
;

class_map_tail
:
   (
      MATCH null_rest_of_line
      | description_line
      | stanza_unrecognized_line
   )
;

policy_map_stanza
:
   POLICY_MAP policy_map_type? name = policy_map_name NEWLINE
   policy_map_tail*
;

policy_map_type
:
   TYPE type = policy_map_type_value
;

policy_map_type_value
:
   NAME
   | WORD
;

policy_map_name
:
   name_parts += ~NEWLINE+
;

policy_map_tail
:
   (
      CLASS null_rest_of_line
      | PARAMETERS null_rest_of_line
      | description_line
      | stanza_unrecognized_line
   )
;

service_policy_stanza
:
   SERVICE_POLICY policy_name = service_policy_name service_policy_scope? NEWLINE
;

service_policy_name
:
   name_parts += ~(GLOBAL | INTERFACE | NEWLINE)+
;

service_policy_scope
:
   GLOBAL
   | INTERFACE interface_name_value = service_policy_interface_name
;

service_policy_interface_name
:
   name_parts += ~NEWLINE+
;

access_group_stanza
:
   ACCESS_GROUP name = access_group_name access_group_tail? NEWLINE
;

access_group_name
:
   name_parts += ~(GLOBAL | IN | OUT | INTERFACE | NEWLINE)+
;

access_group_tail
:
   GLOBAL
   | access_group_direction INTERFACE interface_name_value = access_group_interface_name
   | access_group_direction
;

access_group_direction
:
   IN
   | OUT
;

access_group_interface_name
:
   name_parts += ~NEWLINE+
;

// ============================================================================
// UNRECOGNIZED - Catch-all for anything we don't need to parse
// ============================================================================

// Restricted unrecognized line - doesn't consume lines starting with known stanza keywords
// Used in imported grammar tail rules
stanza_unrecognized_line
:
   ~(NEWLINE | INTERFACE | ACCESS_LIST | OBJECT | OBJECT_GROUP | HOSTNAME | ROUTE | NAT | FAILOVER | CRYPTO | ACCESS_GROUP | ROUTER | TUNNEL_GROUP | CLASS_MAP | POLICY_MAP | SERVICE_POLICY)
   ~NEWLINE* NEWLINE
;

unrecognized_stanza
:
   ~NEWLINE* NEWLINE
;
