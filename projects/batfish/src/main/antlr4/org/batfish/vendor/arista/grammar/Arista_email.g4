parser grammar Arista_email;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

s_email
:
  EMAIL NEWLINE
  (
    email_no
  )*
;

email_no
:
  NO
  ( email_no_auth_null | email_no_from_user_null | email_no_server_null | email_no_tls_null )
;

email_no_auth_null
:
   AUTH null_rest_of_line
;
email_no_from_user_null
:
   FROM_USER null_rest_of_line
;
email_no_server_null
:
   SERVER null_rest_of_line
;
email_no_tls_null
:
   TLS null_rest_of_line
;