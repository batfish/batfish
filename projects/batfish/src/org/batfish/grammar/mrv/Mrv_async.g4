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
      | a_async_outauthtype
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

a_async_outauthtype
:
   OUTAUTHTYPE nbdecl
;
