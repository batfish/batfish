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
  email_no_null
;

email_no_null
:
  (
    AUTH
    | FROM_USER
    | SERVER
    | TLS
  ) null_rest_of_line
;