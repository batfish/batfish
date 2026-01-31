parser grammar FortiosParser;

import
  Fortios_access_list,
  Fortios_address,
  Fortios_addrgrp,
  Fortios_bgp,
  Fortios_common,
  Fortios_firewall,
  Fortios_interface,
  Fortios_internet_service_name,
  Fortios_ippool,
  Fortios_policy,
  Fortios_route_map,
  Fortios_router,
  Fortios_service,
  Fortios_static,
  Fortios_system,
  Fortios_zone;

options {
  superClass = 'org.batfish.grammar.fortios.parsing.FortiosBaseParser';
  tokenVocab = FortiosLexer;
}

fortios_configuration: NEWLINE? statement+ EOF;

statement: s_config;

s_config
:
    CONFIG (
        c_system
        | c_firewall
        | c_router
        | IGNORED_CONFIG_BLOCK
    ) END NEWLINE?
;
