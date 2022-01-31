parser grammar A10_slb_template;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

ss_template: TEMPLATE
  (
    sst_port
  )
;

sst_port:
  PORT name = template_name NEWLINE
  sstp_definition*
;

sstp_definition:
  sstp_conn_limit
;

sstp_conn_limit: CONN_LIMIT limit = connection_limit NEWLINE;