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
  Fortios_policy,
  Fortios_route_map,
  Fortios_router,
  Fortios_service,
  Fortios_static,
  Fortios_system,
  Fortios_vdom,
  Fortios_zone;

options {
  superClass = 'org.batfish.grammar.fortios.parsing.FortiosBaseParser';
  tokenVocab = FortiosLexer;
}

fortios_configuration: NEWLINE? statement+ EOF;

statement: s_config;

s_config: CONFIG (c_global | c_vdom | config_body);

c_global: GLOBAL NEWLINE cg_config* END NEWLINE;

cg_config: CONFIG config_body;

config_body
:
    (
        c_system
        | c_firewall
        | c_router
        | config_unsupported
        | IGNORED_CONFIG_BLOCK
    ) END NEWLINE
;

config_unsupported: UNSUPPORTED_CONFIG_BLOCK;
