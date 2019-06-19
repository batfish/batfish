parser grammar Mrv_async;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

a_async
:
   ASYNC PERIOD
   (
      a_async_access
      | a_async_autohang
      | a_async_dsrwait
      | a_async_flowcont
      | a_async_maxconnections
      | a_async_name
      | a_async_outauthtype
      | a_async_speed
   )
;

a_async_access
:
   ACCESS nbdecl
;

a_async_autohang
:
   AUTOHANG nbdecl
;

a_async_dsrwait
:
   DSRWAIT nbdecl
;

a_async_flowcont
:
   FLOWCONT nbdecl
;

a_async_maxconnections
:
   MAXCONNECTIONS nodecl
;

a_async_name
:
   NAME nsdecl
;

a_async_speed
:
   SPEED nspdecl
;

a_async_outauthtype
:
   OUTAUTHTYPE nbdecl
;
