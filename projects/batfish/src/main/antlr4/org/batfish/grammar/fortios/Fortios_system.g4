parser grammar Fortios_system;

options {
  tokenVocab = FortiosLexer;
}

c_system: SYSTEM cs_global;

cs_global
:
  GLOBAL NEWLINE
  (
    SET
    (
      csg_hostname
    )
  )*
;

csg_hostname: HOSTNAME hostname=double_quoted_string NEWLINE;

