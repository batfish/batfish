parser grammar Legacy_aaa;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

aaa_accounting
:
   ACCOUNTING
   (
      aaa_accounting_commands_line
      | aaa_accounting_commands_stanza
      | aaa_accounting_connection_line
      | aaa_accounting_connection_stanza
      | aaa_accounting_default
      | aaa_accounting_exec_line
      | aaa_accounting_exec_stanza
      | aaa_accounting_identity
      | aaa_accounting_nested
      | aaa_accounting_network_line
      | aaa_accounting_network_stanza
      | aaa_accounting_system_line
      | aaa_accounting_system_stanza
      | aaa_accounting_update
   )
;

aaa_accounting_commands_line
:
   COMMANDS
   (
      ALL
      | levels = range
   )?
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_commands_stanza
:
   COMMANDS
   (
      ALL
      | levels = range
   )?
   (
      DEFAULT
      | list = variable
   ) NEWLINE
   (
      aaaac_action_type
      | aaaac_group
   )*
;

aaa_accounting_connection_line
:
   CONNECTION
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_connection_stanza
:
   CONNECTION DEFAULT NEWLINE
   (
      (
         ACTION_TYPE
         | GROUP
      ) null_rest_of_line
   )+
;

aaa_accounting_default
:
   DEFAULT
   (
      aaa_accounting_default_group
      | aaa_accounting_default_local
   )
;

aaa_accounting_default_group
:
   GROUP
   (
      groups += variable
   )+ NEWLINE
;

aaa_accounting_default_local
:
   LOCAL NEWLINE
;

aaa_accounting_exec_line
:
   EXEC
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_exec_stanza
:
   EXEC DEFAULT NEWLINE
   (
      (
         ACTION_TYPE
         | GROUP
      ) null_rest_of_line
   )+
;

aaa_accounting_identity
:
   IDENTITY
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_method
:
   aaa_accounting_method_none
   | aaa_accounting_method_start_stop
   | aaa_accounting_method_stop_only
   | aaa_accounting_method_wait_start
;

aaa_accounting_method_none
:
   NONE
;

aaa_accounting_method_start_stop
:
   START_STOP aaa_accounting_method_target
;

aaa_accounting_method_stop_only
:
   STOP_ONLY aaa_accounting_method_target
;

aaa_accounting_method_wait_start
:
   WAIT_START aaa_accounting_method_target
;

aaa_accounting_method_target
:
   BROADCAST?
   (
      GROUP?
      (
         RADIUS
         | TAC_PLUS
         (
            NONE
         )?
         | TACACS_PLUS
         | groups += variable
      )
   )+
;

aaa_accounting_nested
:
   NESTED NEWLINE
;

aaa_accounting_network_line
:
   NETWORK
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_network_stanza
:
   NETWORK
   (
      DEFAULT
      | list = variable
   ) NEWLINE
   (
      (
         ACTION_TYPE
         | GROUP
      ) null_rest_of_line
   )+
;

aaa_accounting_system_line
:
   SYSTEM
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_system_stanza
:
   SYSTEM
   (
      DEFAULT
      | list = variable
   ) NEWLINE
   (
      aaaas_action_type
      | aaaas_group
   )
;

aaa_accounting_update
:
   UPDATE NEWINFO? (PERIODIC? dec)? NEWLINE
;

aaa_authentication
:
   AUTHENTICATION
   (
      aaa_authentication_captive_portal
      | aaa_authentication_dot1x
      | aaa_authentication_enable
      | aaa_authentication_include
      | aaa_authentication_login
      | aaa_authentication_mac
      | aaa_authentication_mgmt
      | aaa_authentication_policy
      | aaa_authentication_ppp
      | aaa_authentication_stateful_dot1x
      | aaa_authentication_stateful_kerberos
      | aaa_authentication_stateful_ntlm
      | aaa_authentication_username_prompt
      | aaa_authentication_vpn
      | aaa_authentication_wired
      | aaa_authentication_wispr
   )
;

aaa_authentication_captive_portal
:
   CAPTIVE_PORTAL null_rest_of_line
   (
      aaa_authentication_captive_portal_null
   )*
;

aaa_authentication_captive_portal_null
:
   NO?
   (
      DEFAULT_GUEST_ROLE
      | DEFAULT_ROLE
      | ENABLE_WELCOME_PAGE
      | GUEST_LOGON
      | LOGIN_PAGE
      | MAX_AUTHENTICATION_FAILURES
      | PROTOCOL_HTTP
      | REDIRECT_PAUSE
      | SERVER_GROUP
      | WELCOME_PAGE
      | WHITE_LIST
   ) null_rest_of_line
;

aaa_authentication_dot1x
:
   DOT1X
   (
      (
         (
            DEFAULT
            | list = variable
         ) aaa_authentication_list_method+ NEWLINE
      )
      |
      (
         double_quoted_string NEWLINE
         (
            aaa_authentication_dot1x_null
         )*
      )
   )
;

aaa_authentication_dot1x_null
:
   NO?
   (
      MACHINE_AUTHENTICATION
      | MAX_AUTHENTICATION_FAILURES
      | REAUTHENTICATION
      | TERMINATION
      | TIMER
      | WPA_FAST_HANDOVER
   ) null_rest_of_line
;

aaa_authentication_enable
:
   ENABLE
   (
      (
         DEFAULT aaa_authentication_list_method+
         | IMPLICIT_USER
      ) NEWLINE
   )
;

aaa_authentication_include
:
   INCLUDE name = variable
   (
      FORWARD_SLASH dec
   )? iface = variable srcip = IP_ADDRESS srcmask = IP_ADDRESS
   (
      dstip = IP_ADDRESS dstmask = IP_ADDRESS
   )? group = variable NEWLINE
;

aaa_authentication_list_method
:
   (
      aaa_authentication_list_method_ios
      | aaa_authentication_list_method_fallback
      | aaa_authentication_list_method_group
      | aaa_authentication_list_method_if_needed
      | aaa_authentication_list_method_radius
      | aaa_authentication_list_method_tacacs_local
      | aaa_authentication_list_method_tacacs_plus
   )
;

aaa_authentication_list_method_fallback
:
   FALLBACK ERROR LOCAL
;

aaa_authentication_list_method_group
:
   GROUP aaa_authentication_list_method_group_ios
   (
      groups += aaa_authentication_list_method_group_additional
   )*
;

aaa_authentication_list_method_group_ios
:
   RADIUS
   | TACACS_PLUS
   | groupName = variable
;

aaa_authentication_list_method_group_additional
:
   ~(
      NEWLINE
      | CACHE
      | ENABLE
      | FALLBACK
      | GROUP
      | IF_NEEDED
      | KRB5
      | KRB5_TELNET
      | LINE
      | LOCAL
      | LOCAL_CASE
      | NONE
      | TACACS_PLUS
   )
;

aaa_authentication_list_method_if_needed
:
   IF_NEEDED
;

aaa_authentication_list_method_ios
:
   ENABLE
   | KRB5
   | KRB5_TELNET
   | LINE
   | LOCAL
   | LOCAL_CASE
   | NONE
;

aaa_authentication_list_method_radius
:
   RADIUS
;

aaa_authentication_list_method_tacacs_local
:
   TACACS LOCAL
;

aaa_authentication_list_method_tacacs_plus
:
   TACACS_PLUS
;

aaa_authentication_login
:
   LOGIN
   (
      aaa_authentication_login_ascii_authentication
      | aaa_authentication_login_chap
      | aaa_authentication_login_error_enable
      | aaa_authentication_login_invalid_username_log
      | aaa_authentication_login_list
      | aaa_authentication_login_mschap
      | aaa_authentication_login_mschapv2
      | aaa_authentication_login_privilege_mode
   )
;

aaa_authentication_login_ascii_authentication
:
   ASCII_AUTHENTICATION NEWLINE
;

aaa_authentication_login_chap
:
   CHAP ENABLE NEWLINE
;

aaa_authentication_login_error_enable
:
   ERROR_ENABLE NEWLINE
;

aaa_authentication_login_invalid_username_log
:
   INVALID_USERNAME_LOG NEWLINE
;

aaa_authentication_login_list
:
   (
      DEFAULT
      | name = variable
   ) aaa_authentication_list_method+ NEWLINE
;

aaa_authentication_login_mschap
:
   MSCHAP ENABLE NEWLINE
;

aaa_authentication_login_mschapv2
:
   MSCHAPV2 ENABLE NEWLINE
;

aaa_authentication_login_privilege_mode
:
   PRIVILEGE_MODE NEWLINE
;

aaa_authentication_mac
:
   MAC double_quoted_string NEWLINE
   (
      aaa_authentication_mac_null
   )*
;

aaa_authentication_mac_null
:
   NO?
   (
      CASE
      | DELIMITER
      | MAX_AUTHENTICATION_FAILURES
      | REAUTHENTICATION
      | TIMER
   ) null_rest_of_line
;

aaa_authentication_mgmt
:
   MGMT NEWLINE
   (
      aaa_authentication_mgmt_null
   )*
;

aaa_authentication_mgmt_null
:
   NO?
   (
      ENABLE
      | SERVER_GROUP
   ) null_rest_of_line
;

aaa_authentication_policy
:
   POLICY
   (
      LOCAL ALLOW_NOPASSWORD_REMOTE_LOGIN
      | ON_FAILURE LOG
      | ON_SUCCESS LOG
   ) NEWLINE
;

aaa_authentication_ppp
:
   PPP
   (
      DEFAULT
      | list = variable
   ) aaa_authentication_list_method+ NEWLINE
;

aaa_authentication_server
:
   AUTHENTICATION_SERVER
   (
      aaa_authentication_server_radius
      | aaa_authentication_server_tacacs
   )
;

aaa_authentication_server_radius
:
   RADIUS double_quoted_string NEWLINE
   (
      aaa_authentication_server_radius_null
   )*
;

aaa_authentication_server_radius_null
:
   NO?
   (
      HOST
   ) null_rest_of_line
;

aaa_authentication_server_tacacs
:
   TACACS double_quoted_string NEWLINE
   (
      aaa_authentication_server_tacacs_null
   )*
;

aaa_authentication_server_tacacs_null
:
   NO?
   (
      HOST
      | SESSION_AUTHORIZATION
   ) null_rest_of_line
;

aaa_authentication_stateful_dot1x
:
   STATEFUL_DOT1X NEWLINE
;

aaa_authentication_stateful_kerberos
:
   STATEFUL_KERBEROS double_quoted_string NEWLINE
;

aaa_authentication_stateful_ntlm
:
   STATEFUL_NTLM double_quoted_string NEWLINE
;

aaa_authentication_username_prompt
:
   USERNAME_PROMPT DOUBLE_QUOTE RAW_TEXT? DOUBLE_QUOTE NEWLINE
;

aaa_authentication_vpn
:
   VPN double_quoted_string NEWLINE
;

aaa_authentication_wired
:
   WIRED NEWLINE
;

aaa_authentication_wispr
:
   WISPR double_quoted_string NEWLINE
;

aaa_authorization
:
   AUTHORIZATION
   (
      aaa_authorization_auth_proxy
      | aaa_authorization_commands
      | aaa_authorization_config_commands
      | aaa_authorization_console
      | aaa_authorization_exec
      | aaa_authorization_include
      | aaa_authorization_network
      | aaa_authorization_reverse_access
      | aaa_authorization_ssh_certificate
      | aaa_authorization_ssh_publickey
   )
;

aaa_authorization_auth_proxy
:
   AUTH_PROXY
   (
      DEFAULT
      | list = variable
   ) aaa_authorization_method
;

aaa_authorization_commands
:
   COMMANDS level = dec?
   (
      CONSOLE
      | DEFAULT
      | list = variable
   ) aaa_authorization_method
;

aaa_authorization_config_commands
:
   CONFIG_COMMANDS
   (
      (
         (
            CONSOLE
            | DEFAULT
         ) aaa_authorization_method
      )
      | NEWLINE
   )
;

aaa_authorization_console
:
   CONSOLE NEWLINE
;

aaa_authorization_exec
:
   EXEC
   (
      DEFAULT
      | list = variable
   ) aaa_authorization_method
;

aaa_authorization_include
:
   INCLUDE name = variable
   (
      FORWARD_SLASH dec
   )? iface = variable srcip = IP_ADDRESS srcmask = IP_ADDRESS
   (
      dstip = IP_ADDRESS dstmask = IP_ADDRESS
   )? group = variable NEWLINE
;

aaa_authorization_method
:
   (
      (
         GROUP?
         (
            groups += ~( NEWLINE | LOCAL | NONE )
         )+
      )
      | LOCAL
      | NONE
   )* NEWLINE
;

aaa_authorization_network
:
   NETWORK
   (
      DEFAULT
      | list = variable
   ) aaa_authorization_method
;

aaa_authorization_reverse_access
:
   REVERSE_ACCESS
   (
      DEFAULT
      | list = variable
   ) aaa_authorization_method
;

aaa_authorization_ssh_certificate
:
   SSH_CERTIFICATE DEFAULT aaa_authorization_method
;

aaa_authorization_ssh_publickey
:
   SSH_PUBLICKEY DEFAULT aaa_authorization_method
;

aaa_bandwidth_contract
:
   BANDWIDTH_CONTRACT name = variable null_rest_of_line
;

aaa_default_taskgroup
:
   DEFAULT_TASKGROUP null_rest_of_line
;

aaa_derivation_rules
:
   DERIVATION_RULES null_rest_of_line
   (
      aaa_derivation_rules_null
   )*
;

aaa_derivation_rules_null
:
   NO?
   (
      SET
   ) null_rest_of_line
;

aaa_group
:
   GROUP SERVER
   (
      LDAP
      | RADIUS
      | TACACS_PLUS
   ) name = variable NEWLINE
   aaa_group_server
;

aaa_group_server
:
   SERVER
   (
      IP_ADDRESS
      | IPV6_ADDRESS
      | name = variable
   )
   (
      ACCT_PORT acct_port = uint16
      | AUTH_PORT auth_port = uint16
      | PORT prt = uint16
      | VRF vrf = vrf_name
   )* NEWLINE
;

aaa_new_model
:
   NEW_MODEL NEWLINE
;

aaa_password_policy
:
   PASSWORD_POLICY null_rest_of_line
;

aaa_profile
:
   PROFILE double_quoted_string NEWLINE
   (
      aaa_profile_null
   )*
;

aaa_profile_null
:
   NO?
   (
      AUTHENTICATION_DOT1X
      | AUTHENTICATION_MAC
      | DOT1X_DEFAULT_ROLE
      | DOT1X_SERVER_GROUP
      | ENFORCE_DHCP
      | INITIAL_ROLE
      | MAC_DEFAULT_ROLE
      | MAC_SERVER_GROUP
      | RADIUS_ACCOUNTING
      | RADIUS_INTERIM_ACCOUNTING
      | RFC_3576_SERVER
      | WIRED_TO_WIRELESS_ROAM
   ) null_rest_of_line
;

aaa_rfc_3576_server
:
   RFC_3576_SERVER double_quoted_string NEWLINE
;

aaa_server
:
   SERVER RADIUS DYNAMIC_AUTHOR NEWLINE
   (
      aaa_server_auth_type
      | aaa_server_client
      | aaa_server_ignore
      | aaa_server_port
   )*
;

aaa_server_auth_type
:
   AUTH_TYPE
   (
      ANY
      | ALL
      | SESSION_KEY
   ) NEWLINE
;

aaa_server_client
:
   CLIENT
   (
      IP_ADDRESS
      | name = variable
   )
   SERVER_KEY
   dec?
   variable
   NEWLINE
;

aaa_server_ignore
:
   IGNORE
   (
      SERVER_KEY
      | SESSION_KEY
   ) NEWLINE
;

aaa_server_port
:
   PORT port_num = dec NEWLINE
;

aaa_server_group
:
   SERVER_GROUP double_quoted_string NEWLINE
   (
      aaa_server_group_null
   )*
;

aaa_server_group_null
:
   NO?
   (
      ALLOW_FAIL_THROUGH
      | AUTH_SERVER
      | LOAD_BALANCE
      | SET
   ) null_rest_of_line
;

aaa_session_id
:
   SESSION_ID
   (
      COMMON
      | UNIQUE
   ) NEWLINE
;

aaa_user
:
   USER
   (
      DEFAULT_ROLE
      | FAST_AGE
   ) NEWLINE
;

aaaac_action_type
:
   ACTION_TYPE
   (
      START_STOP
      | STOP_ONLY
      | WAIT_START
   ) NEWLINE
;

aaaac_group
:
   GROUP
   (
      LDAP
      | RADIUS
      | TACACS_PLUS
   ) NEWLINE
;

aaaas_action_type
:
   ACTION_TYPE
   (
      START_STOP
      | STOP_ONLY
      | WAIT_START
   ) NEWLINE
;

aaaas_group
:
   GROUP
   (
      LDAP
      | RADIUS
      | TACACS_PLUS
   ) NEWLINE
;

null_aaa_substanza
:
   aaa_default_taskgroup
;

s_aaa
:
   AAA
   (
      aaa_accounting
      | aaa_authentication
      | aaa_authentication_server
      | aaa_authorization
      | aaa_bandwidth_contract
      | aaa_derivation_rules
      | aaa_group
      | aaa_new_model
      | aaa_password_policy
      | aaa_profile
      | aaa_rfc_3576_server
      | aaa_server
      | aaa_server_group
      | aaa_session_id
      | aaa_user
      | null_aaa_substanza
   )
;

no_aaa
:
  AAA
  no_aaa_root
;

no_aaa_root
:
  ROOT NEWLINE
;