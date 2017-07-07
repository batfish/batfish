parser grammar Mrv_subscriber;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

a_subscriber
:
   SUBSCRIBER PERIOD
   (
      a_subscriber_despassword
      | a_subscriber_guimenuname
      | a_subscriber_idletimeout
      | a_subscriber_maxsubs
      | a_subscriber_menuname
      | a_subscriber_name
      | a_subscriber_prompt
      | a_subscriber_remoteaccesslist
      | a_subscriber_securityv3
      | a_subscriber_shapassword
      | a_subscriber_substat
      | a_subscriber_superpassword
   )
;

a_subscriber_despassword
:
   DESPASSWORD npdecl
;

a_subscriber_guimenuname
:
   GUIMENUNAME nsdecl
;

a_subscriber_idletimeout
:
   IDLETIMEOUT nidecl
;

a_subscriber_maxsubs
:
   MAXSUBS nodecl
;

a_subscriber_menuname
:
   MENUNAME nsdecl
;

a_subscriber_name
:
   NAME nsdecl
;

a_subscriber_prompt
:
   PROMPT nsdecl
;

a_subscriber_remoteaccesslist
:
   REMOTEACCESSLIST nodecl
;

a_subscriber_securityv3
:
   SECURITYV3 nidecl
;

a_subscriber_shapassword
:
   SHAPASSWORD npdecl
;

a_subscriber_substat
:
   SUBSTAT nbdecl
;

a_subscriber_superpassword
:
   SUPERPASSWORD npdecl
;
