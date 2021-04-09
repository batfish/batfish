parser grammar CiscoXr_line;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
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
   EXEC_TIMEOUT minutes = uint_legacy seconds = uint_legacy? NEWLINE
;

l_length
:
   (
      LENGTH uint_legacy NEWLINE
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

l_null
:
   NO?
   (
      ABSOLUTE_TIMEOUT
      | ACTIVATION_CHARACTER
      | AUTHORIZATION
      | AUTOHANGUP
      | AUTOSELECT
      | DATABITS
      | ESCAPE_CHARACTER
      | EXEC
      | FLOWCONTROL
      | FLUSH_AT_ACTIVATION
      | HISTORY
      | IPV6
      | LOCATION
      | LOGGING
      | LOGOUT_WARNING
      | MODEM
      | NOTIFY
      | PASSWORD
      | PRIVILEGE
      | ROTARY
      | RXSPEED
      | SESSION_DISCONNECT_WARNING
      | SESSION_LIMIT
      | SESSION_TIMEOUT
      | SPEED
      | STOPBITS
      | TERMINAL_TYPE
      | TIMEOUT
      | TIMESTAMP
      | TXSPEED
      |
      (
        NO VACANT_MESSAGE
      )
   ) null_rest_of_line
;

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
         slot1 = uint_legacy FORWARD_SLASH
         (
            port1 = uint_legacy FORWARD_SLASH
         )?
      )? first = uint_legacy
      (
         (
            slot2 = uint_legacy FORWARD_SLASH
            (
               port2 = uint_legacy FORWARD_SLASH
            )?
         )? last = uint_legacy
      )?
   )? NEWLINE
   (
      l_access_class
      | l_accounting
      | l_exec_timeout
      | l_length
      | l_login
      | l_null
      | l_script
      | l_transport
      | l_vacant_message
      | description_line
   )*
;