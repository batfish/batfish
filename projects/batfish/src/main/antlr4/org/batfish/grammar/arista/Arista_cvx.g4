parser grammar Arista_cvx;

import Cisco_common;

options {
   tokenVocab = AristaLexer;
}

s_cvx
:
  CVX NEWLINE
  (
    cvx_no
    | cvx_null
    // | cvx_service
  )*
;

cvx_no
:
  NO cvx_no_null
;

cvx_no_null
:
  (
    PEER
    | SHUTDOWN
    | SOURCE_INTERFACE
    | SSL
  ) null_rest_of_line
;

cvx_null
:
  (
    HEARTBEAT_INTERVAL
    | HEARTBEAT_TIMEOUT
    | PEER
    | PORT
    | SHUTDOWN
  ) null_rest_of_line
;

