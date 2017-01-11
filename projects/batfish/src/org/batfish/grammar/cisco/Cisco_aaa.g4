parser grammar Cisco_aaa;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

aaa_accounting
:
   ACCOUNTING
   (
      aaa_accounting_commands
      | aaa_accounting_connection
      | aaa_accounting_default
      | aaa_accounting_delay_start
      | aaa_accounting_exec_line
      | aaa_accounting_exec_stanza
      | aaa_accounting_nested
      | aaa_accounting_network
      | aaa_accounting_send
      | aaa_accounting_system
      | aaa_accounting_update
   )
;

aaa_accounting_commands
:
   COMMANDS
   (
      ALL
      | level = DEC
   )?
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_connection
:
   CONNECTION
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
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

aaa_accounting_delay_start
:
   DELAY_START
   (
      ALL
      | VRF name = variable
   )?
   (
      EXTENDED_DELAY DEC
   )? NEWLINE
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
      ) ~NEWLINE* NEWLINE
   )+
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
      GROUP
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

aaa_accounting_network
:
   NETWORK
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_send
:
   SEND
   (
      aaa_accounting_send_counters
      | aaa_accounting_send_stop_record
   )
;

aaa_accounting_send_counters
:
   COUNTERS IPV6 NEWLINE
;

aaa_accounting_send_stop_record
:
   STOP_RECORD
   (
      ALWAYS
      |
      (
         AUTHENTICATION
         (
            (
               FAILURE
               |
               (
                  SUCCESS REMOTE_SERVER
               )
            )
            (
               VRF name = variable
            )?
         )
      )
   ) NEWLINE
;

aaa_accounting_system
:
   SYSTEM
   (
      DEFAULT
      | list = variable
   ) aaa_accounting_method NEWLINE
;

aaa_accounting_update
:
   UPDATE NEWINFO NEWLINE
;

aaa_authentication
:
   AUTHENTICATION
   (
      aaa_authentication_banner
      | aaa_authentication_dot1x
      | aaa_authentication_enable
      | aaa_authentication_http
      | aaa_authentication_include
      | aaa_authentication_login
      | aaa_authentication_ppp
      | aaa_authentication_serial
      | aaa_authentication_ssh
      | aaa_authentication_telnet
   )
;

aaa_authentication_asa_console
:
   CONSOLE group = variable? TACACS_PLUS_ASA? LOCAL_ASA? NEWLINE
;

aaa_authentication_banner
:
   BANNER banner
;

aaa_authentication_dot1x
:
   DOT1X
   (
      DEFAULT
      | list = variable
   ) aaa_authentication_list_method+ NEWLINE
;

aaa_authentication_enable
:
   ENABLE
   (
      (
         DEFAULT aaa_authentication_list_method+
         | IMPLICIT_USER
      ) NEWLINE
      | aaa_authentication_asa_console
   )
;

aaa_authentication_http
:
   HTTP aaa_authentication_asa_console
;

aaa_authentication_include
:
   INCLUDE name = variable
   (
      FORWARD_SLASH DEC
   )? iface = variable srcip = IP_ADDRESS srcmask = IP_ADDRESS
   (
      dstip = IP_ADDRESS dstmask = IP_ADDRESS
   )? group = variable NEWLINE
;

aaa_authentication_list_method
:
   (
      aaa_authentication_list_method_enable
      | aaa_authentication_list_method_fallback
      | aaa_authentication_list_method_group
      | aaa_authentication_list_method_if_needed
      | aaa_authentication_list_method_line
      | aaa_authentication_list_method_local
      | aaa_authentication_list_method_none
      | aaa_authentication_list_method_tacacs_local
      | aaa_authentication_list_method_tacacs_plus
   )
;

aaa_authentication_list_method_enable
:
   ENABLE
;

aaa_authentication_list_method_fallback
:
   FALLBACK ERROR LOCAL
;

aaa_authentication_list_method_group
:
   GROUP groups += ~NEWLINE
   (
      groups += ~( NEWLINE | ENABLE | FALLBACK | GROUP | IF_NEEDED | LINE |
      LOCAL | NONE | TACACS_PLUS )
   )*
;

aaa_authentication_list_method_if_needed
:
   IF_NEEDED
;

aaa_authentication_list_method_line
:
   LINE
;

aaa_authentication_list_method_local
:
   LOCAL
;

aaa_authentication_list_method_none
:
   NONE
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

aaa_authentication_ppp
:
   PPP
   (
      DEFAULT
      | list = variable
   ) aaa_authentication_list_method+ NEWLINE
;

aaa_authentication_serial
:
   SERIAL aaa_authentication_asa_console
;

aaa_authentication_ssh
:
   SSH aaa_authentication_asa_console
;

aaa_authentication_telnet
:
   TELNET aaa_authentication_asa_console
;

aaa_authorization
:
   AUTHORIZATION
   (
      aaa_authorization_commands
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

aaa_authorization_commands
:
   COMMANDS level = DEC?
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
      FORWARD_SLASH DEC
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

aaa_default_taskgroup
:
   DEFAULT_TASKGROUP ~NEWLINE* NEWLINE
;

aaa_group
:
   GROUP SERVER
   (
      LDAP
      | RADIUS
      | TACACS_PLUS
   ) name = variable NEWLINE
   (
      aaa_group_deadtime
      | aaa_group_ip_tacacs
      | aaa_group_no_source_interface
      | aaa_group_server
      | aaa_group_server_private
      | aaa_group_source_interface
      | aaa_group_use_vrf
      | aaa_group_vrf
   )*
;

aaa_group_deadtime
:
   DEADTIME minutes = DEC NEWLINE
;

aaa_group_ip_tacacs
:
   IP TACACS SOURCE_INTERFACE interface_name NEWLINE
;

aaa_group_no_source_interface
:
   NO SOURCE_INTERFACE NEWLINE
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
      (
         ACCT_PORT acct_port = DEC
      )
      |
      (
         AUTH_PORT auth_port = DEC
      )
      |
      (
         PORT prt = DEC
      )
   )* NEWLINE
;

aaa_group_server_private
:
   SERVER_PRIVATE
   (
      IP_ADDRESS
      | IPV6_ADDRESS
      | name = variable
   )
   (
      PORT DEC
   )? NEWLINE
;

aaa_group_source_interface
:
   SOURCE_INTERFACE interface_name DEC? NEWLINE
;

aaa_group_use_vrf
:
   USE_VRF
   (
      DEFAULT
      | MANAGEMENT
      | name = variable
   ) NEWLINE
;

aaa_group_vrf
:
   VRF
   (
      DEFAULT
      | MANAGEMENT
      | name = variable
   ) NEWLINE
;

aaa_new_model
:
   NEW_MODEL NEWLINE
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
   USER DEFAULT_ROLE NEWLINE
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
      | aaa_authorization
      | aaa_group
      | aaa_new_model
      | aaa_session_id
      | aaa_user
      | null_aaa_substanza
   )
;

