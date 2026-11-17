parser grammar Cisco_line;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

l_access_class
:
   IPV6? ACCESS_CLASS
   (
      (
         (
            EGRESS
            | INGRESS
         ) name = variable
      )
      |
      (
         name = variable
         (
            IN
            | OUT
         )?
      )
   ) VRF_ALSO? NEWLINE
;

l_accounting
:
   (
      (
         NO ACCOUNTING
         (
            COMMANDS
            | EXEC
         )
      )
      |
      (
         ACCOUNTING
         (
            COMMANDS
            | EXEC
         )
         (
            DEFAULT
            | variable
         )
      )
   ) NEWLINE
;

l_exec_timeout
:
   EXEC_TIMEOUT minutes = dec seconds = dec? NEWLINE
;

l_length
:
   (
      LENGTH dec NEWLINE
   )
   |
   (
      NO LENGTH NEWLINE
   )
;

l_login
:
   LOGIN
   (
      l_login_authentication
      | l_login_local
   )
;

l_login_authentication
:
   AUTHENTICATION
   (
      DEFAULT
      | name = variable
   ) NEWLINE
;

l_login_local
:
   LOCAL NEWLINE
;

l_absolute_timeout_null: NO? ABSOLUTE_TIMEOUT null_rest_of_line;
l_activation_character_null: NO? ACTIVATION_CHARACTER null_rest_of_line;
l_authorization_null: NO? AUTHORIZATION null_rest_of_line;
l_autohangup_null: NO? AUTOHANGUP null_rest_of_line;
l_autoselect_null: NO? AUTOSELECT null_rest_of_line;
l_buffer_length_null: NO? BUFFER_LENGTH null_rest_of_line;
l_databits_null: NO? DATABITS null_rest_of_line;
l_escape_character_null: NO? ESCAPE_CHARACTER null_rest_of_line;
l_exec_null: NO? EXEC null_rest_of_line;
l_flowcontrol_null: NO? FLOWCONTROL null_rest_of_line;
l_flush_at_activation_null: NO? FLUSH_AT_ACTIVATION null_rest_of_line;
l_history_null: NO? HISTORY null_rest_of_line;
l_hold_character_null: NO? HOLD_CHARACTER null_rest_of_line;
l_ipv6_null: NO? IPV6 null_rest_of_line;
l_location_null: NO? LOCATION null_rest_of_line;
l_logging_null: NO? LOGGING null_rest_of_line;
l_logout_warning_null: NO? LOGOUT_WARNING null_rest_of_line;
l_modem_null: NO? MODEM null_rest_of_line;
l_notify_null: NO? NOTIFY null_rest_of_line;
l_password_null: NO? PASSWORD null_rest_of_line;
l_privilege_null: NO? PRIVILEGE null_rest_of_line;
l_rotary_null: NO? ROTARY null_rest_of_line;
l_rxspeed_null: NO? RXSPEED null_rest_of_line;
l_session_disconnect_warning_null: NO? SESSION_DISCONNECT_WARNING null_rest_of_line;
l_session_limit_null: NO? SESSION_LIMIT null_rest_of_line;
l_session_timeout_null: NO? SESSION_TIMEOUT null_rest_of_line;
l_speed_null: NO? SPEED null_rest_of_line;
l_stop_character_null: NO? STOP_CHARACTER null_rest_of_line;
l_stopbits_null: NO? STOPBITS null_rest_of_line;
l_terminal_type_null: NO? TERMINAL_TYPE null_rest_of_line;
l_timeout_null: NO? TIMEOUT null_rest_of_line;
l_timestamp_null: NO? TIMESTAMP null_rest_of_line;
l_txspeed_null: NO? TXSPEED null_rest_of_line;
l_no_null: NO VACANT_MESSAGE null_rest_of_line;

l_script
:
   SCRIPT DIALER variable NEWLINE
;

l_transport
:
   TRANSPORT
   (
      INPUT
      | OUTPUT
      | PREFERRED
   ) prot += variable+ NEWLINE
;

l_vacant_message
:
  VACANT_MESSAGE message = ios_delimited_banner NEWLINE
;

s_line
:
   LINE line_type
   (
      (
         slot1 = dec FORWARD_SLASH
         (
            port1 = dec FORWARD_SLASH
         )?
      )? first = dec
      (
         (
            slot2 = dec FORWARD_SLASH
            (
               port2 = dec FORWARD_SLASH
            )?
         )? last = dec
      )?
   )? NEWLINE
   (
      l_absolute_timeout_null
      | l_access_class
      | l_accounting
      | l_activation_character_null
      | l_authorization_null
      | l_autohangup_null
      | l_autoselect_null
      | l_buffer_length_null
      | l_databits_null
      | l_escape_character_null
      | l_exec_null
      | l_exec_timeout
      | l_flowcontrol_null
      | l_flush_at_activation_null
      | l_history_null
      | l_hold_character_null
      | l_ipv6_null
      | l_length
      | l_location_null
      | l_logging_null
      | l_login
      | l_logout_warning_null
      | l_modem_null
      | l_no_null
      | l_notify_null
      | l_password_null
      | l_privilege_null
      | l_rotary_null
      | l_rxspeed_null
      | l_script
      | l_session_disconnect_warning_null
      | l_session_limit_null
      | l_session_timeout_null
      | l_speed_null
      | l_stop_character_null
      | l_stopbits_null
      | l_terminal_type_null
      | l_timeout_null
      | l_timestamp_null
      | l_transport
      | l_txspeed_null
      | l_vacant_message
      | description_line
   )*
;
