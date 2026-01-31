parser grammar CiscoNxos_crypto;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ck_param
:
  PARAM RSA LABEL label = WORD MODULUS uint32 NEWLINE
;

crypto_key
:
  KEY
  (
    ck_param
  )
;

s_crypto
:
  CRYPTO
  (
    crypto_key
  )
;