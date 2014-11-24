parser grammar JuniperGrammar_system;

import JuniperGrammarCommonParser;

options {
   tokenVocab = JuniperGrammarLexer;
}


accounting_sys_stanza
:
   ACCOUNTING ignored_substanza
;

arp_sys_stanza
:
   ARP ignored_substanza
;

authentication_order_sys_stanza
:
   AUTHENTICATION_ORDER variable_list SEMICOLON
;

backup_router_sys_stanza
:
   BACKUP_ROUTER IP_ADDRESS SEMICOLON
;

domain_name_sys_stanza
:
   DOMAIN_NAME VARIABLE SEMICOLON
;

domain_search_sys_stanza
:
   DOMAIN_SEARCH VARIABLE SEMICOLON
;

dump_on_panic_sys_stanza
:
   DUMPONPANIC SEMICOLON
;

host_name_sys_stanza
:
   HOST_NAME name = VARIABLE SEMICOLON
;

license_sys_stanza
:
   LICENSE ignored_substanza
;

location_sys_stanza
:
   LOCATION VARIABLE+ SEMICOLON
;

login_sys_stanza
:
   LOGIN ignored_substanza
;

max_configuration_rollbacks_sys_stanza
:
   MAX_CONFIGURATION_ROLLBACKS VARIABLE+ SEMICOLON
;

max_configurations_on_flash_sys_stanza
:
   MAX_CONFIGURATIONS_ON_FLASH VARIABLE+ SEMICOLON
;

name_server_sys_stanza
:
   NAME_SERVER ignored_substanza
;

ntp_sys_stanza
:
   NTP ignored_substanza
;

null_sys_stanza
:
   accounting_sys_stanza
   | arp_sys_stanza
   | authentication_order_sys_stanza
   | backup_router_sys_stanza
   | domain_name_sys_stanza
   | domain_search_sys_stanza
   | dump_on_panic_sys_stanza
   | license_sys_stanza
   | login_sys_stanza
   | location_sys_stanza
   | max_configurations_on_flash_sys_stanza
   | max_configuration_rollbacks_sys_stanza
   | name_server_sys_stanza
   | ntp_sys_stanza
   | ports_sys_stanza
   | radius_options_sys_stanza
   | radius_server_sys_stanza
   | removed_stanza
   | root_authentication_sys_stanza
   | services_sys_stanza
   | syslog_sys_stanza
   | tacplus_server_sys_stanza
   | time_zone_sys_stanza
;

radius_options_sys_stanza
:
   RADIUS_OPTIONS ignored_substanza
;

radius_server_sys_stanza
:
   RADIUS_SERVER ignored_substanza
;

ports_sys_stanza
:
   PORTS ignored_substanza
;

root_authentication_sys_stanza
:
   ROOT_AUTHENTICATION ignored_substanza
;

services_sys_stanza
:
   SERVICES ignored_substanza
;

sys_stanza
:
   host_name_sys_stanza
   | null_sys_stanza
;

syslog_sys_stanza
:
   SYSLOG ignored_substanza
;

system_stanza
:
   SYSTEM OPEN_BRACE sys_stanza+ CLOSE_BRACE
;

tacplus_server_sys_stanza
:
   TACPLUS_SERVER ignored_substanza
;

time_zone_sys_stanza
:
   TIME_ZONE VARIABLE SEMICOLON
;
 