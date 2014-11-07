parser grammar JuniperGrammar_routing_options;

import JuniperGrammarCommonParser;

options {
   tokenVocab = JuniperGrammarLexer;
}

routing_options_stanza
:
   ROUTING_OPTIONS OPEN_BRACE ro_stanza+ CLOSE_BRACE
;

ro_stanza
:
   autonomous_system_ro_stanza
   | martians_ro_stanza
   | rib_groups_ro_stanza
   | rib_ro_stanza
   | router_id_ro_stanza
   | static_ro_stanza
   | null_ro_stanza
;

autonomous_system_ro_stanza
:
   AUTONOMOUS_SYSTEM num = DEC SEMICOLON
;

martians_ro_stanza
:
   MARTIANS OPEN_BRACE
   (
      (
         IP_ADDRESS_WITH_MASK
         | IPV6_ADDRESS_WITH_MASK
      )
      (
         ORLONGER
         | EXACT
      ) ALLOW? SEMICOLON
   )+ CLOSE_BRACE
;

rib_groups_ro_stanza
:
   RIB_GROUPS OPEN_BRACE
   (
      group_name = VARIABLE OPEN_BRACE
      (
         EXPORT_RIB export_list = variable_list SEMICOLON
      )? IMPORT_RIB import_list = variable_list SEMICOLON CLOSE_BRACE
   )+ CLOSE_BRACE
;

rib_ro_stanza
:
// TODO [Ask Ari]: probably am not supposed to be ignoring this stuff.
   RIB name = VARIABLE ignored_substanza
;

router_id_ro_stanza
:
   ROUTER_ID id = IP_ADDRESS SEMICOLON
;

static_ro_stanza
:
   STATIC OPEN_BRACE
   (
      sro_stanza
      | inactive_sro_stanza
   )+ CLOSE_BRACE
;

null_ro_stanza
:
   aggregate_ro_stanza
   | interface_routes_ro_stanza
   | forwarding_table_ro_stanza
   | multicast_ro_stanza
;

inactive_sro_stanza
:
   INACTIVE COLON sro_stanza
;

sro_stanza
:
   defaults_sro_stanza
   | rib_group_sro_stanza
   | route_sro_stanza
;

aggregate_ro_stanza // TODO [Ask Ari]: Should this really get ignored?

:
   AGGREGATE ignored_substanza
;

interface_routes_ro_stanza // TODO [Ask Ari]: Should this really get ignored?

:
   INTERFACE_ROUTES ignored_substanza
;

forwarding_table_ro_stanza // TODO [Ask Ari]: Should this really get ignored?

:
   FORWARDING_TABLE ignored_substanza
;

multicast_ro_stanza // TODO [Ask Ari]: Should this really get ignored?

:
   MULTICAST ignored_substanza
;

defaults_sro_stanza
// TODO [Ask Ari]: I'm sure we care about what's in here

:
   DEFAULTS OPEN_BRACE static_opts_sro_stanza+ CLOSE_BRACE
;

rib_group_sro_stanza
:
   RIB_GROUP group_name = VARIABLE SEMICOLON
;

route_sro_stanza
:
   ROUTE
   (
      IP_ADDRESS_WITH_MASK
      | IPV6_ADDRESS_WITH_MASK
   )
   (
      (
         OPEN_BRACE
         (
            static_opts_sro_stanza
         )+ CLOSE_BRACE
      )
      | static_opts_sro_stanza
   )
;

static_opts_sro_stanza
:
   (
      ACTIVE
      | PASSIVE
      | DISCARD
      |
      (
         AS_PATH x = VARIABLE
      )
      | c = community_static_opts_sro_stanza
      | INSTALL
      | NEXT_HOP i = IP_ADDRESS
      | NEXT_TABLE v = VARIABLE
      | NO_INSTALL
      | METRIC n = DEC
      | PREFERENCE n = DEC
      | READVERTISE
      | NO_READVERTISE
      | RESOLVE
      | NO_RESOLVE
      | RETAIN
      | NO_RETAIN
      | TAG v = VARIABLE
   ) SEMICOLON
;

community_static_opts_sro_stanza
:
   COMMUNITY
   (
      variable_list
      | as_id
   )
;
 