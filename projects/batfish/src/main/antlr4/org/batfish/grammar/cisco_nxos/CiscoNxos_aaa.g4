parser grammar CiscoNxos_aaa;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_aaa
:
  AAA
  (
    aaa_group
    | aaa_accounting
    | aaa_authentication
    | aaa_authorization
  )
;

aaa_group
:
  GROUP SERVER aaag_tacacsp
;

aaag_tacacsp
:
  TACACSP name = aaa_group_name NEWLINE
  (
    aaagt_server
    | aaagt_use_vrf
  )*
;

aaa_group_name
:
// 1-127 characters
  WORD
;

aaagt_server
:
  SERVER
  (
    aaagts_dns
    | aaagts_ip4
    | aaagts_ip6
  )
;

aaagts_dns
:
  dns = WORD NEWLINE
;

aaagts_ip4
:
  ip = ip_address NEWLINE
;

aaagts_ip6
:
  ip6 = ipv6_address NEWLINE
;

aaagt_use_vrf
:
  USE_VRF name = vrf_name NEWLINE
;

aaa_accounting
:
  ACCOUNTING aaa_accounting_default
;

aaa_accounting_default
:
  DEFAULT
  (
    GROUP groups += aaa_group_name+
  )? LOCAL? NEWLINE
;

aaa_authentication
:
  AUTHENTICATION aaa_authentication_login
;

aaa_authentication_login
:
  LOGIN
  (
    aaa_authentication_login_default
    | aaa_authentication_login_error_enable
  )
;

aaa_authentication_login_default
:
  DEFAULT
  (
    GROUP groups += aaa_group_name+
  )? LOCAL? NEWLINE
;

aaa_authentication_login_error_enable
:
  ERROR_ENABLE NEWLINE
;

aaa_authorization
:
  AUTHORIZATION
  (
    aaa_authorization_commands
    | aaa_authorization_config_commands
  )
;

aaa_authorization_commands
:
  COMMANDS aaa_authorization_commands_default
;

aaa_authorization_commands_default
:
  DEFAULT
  (
    GROUP groups += aaa_group_name+
  )? LOCAL? NEWLINE
;

aaa_authorization_config_commands
:
  CONFIG_COMMANDS aaa_authorization_config_commands_default
;

aaa_authorization_config_commands_default
:
  DEFAULT
  (
    GROUP groups += aaa_group_name+
  )? LOCAL? NEWLINE
;
