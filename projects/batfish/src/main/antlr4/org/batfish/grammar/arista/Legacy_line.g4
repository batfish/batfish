parser grammar Legacy_line;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
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

lc_null
:
   (
      ACCOUNTING
      | AUTHENTICATION
      | AUTHORIZATION
      | ENABLE_AUTHENTICATION
      | IDLE_TIMEOUT
      | LENGTH
      | LOGIN_AUTHENTICATION
      | PASSWORD
      | SESSION_TIMEOUT
      | SPEED
   ) null_rest_of_line
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
      l_access_class
      | l_accounting
      | l_exec_timeout
      | l_length
      | l_login
      | l_null
      | l_script
      | l_transport
      | description_line
   )*
;