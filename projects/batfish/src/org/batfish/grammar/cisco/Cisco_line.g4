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

l_exec_timeout
:
   EXEC_TIMEOUT minutes = DEC seconds = DEC? NEWLINE
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
      | SESSION_DISCONNECT_WARNING
      | SESSION_LIMIT
      | SESSION_TIMEOUT
      | STOPBITS
      | TERMINAL_TYPE
      | TIMESTAMP
      | VACANT_MESSAGE
   ) ~NEWLINE* NEWLINE
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

s_line
:
   LINE line_type
   (
      (
         slot1 = DEC FORWARD_SLASH
         (
            port1 = DEC FORWARD_SLASH
         )?
      )? first = DEC
      (
         (
            slot2 = DEC FORWARD_SLASH
            (
               port2 = DEC FORWARD_SLASH
            )?
         )? last = DEC
      )?
   )? NEWLINE
   (
      l_access_class
      | l_exec_timeout
      | l_login
      | l_null
      | l_transport
      | description_line
   )*
;

