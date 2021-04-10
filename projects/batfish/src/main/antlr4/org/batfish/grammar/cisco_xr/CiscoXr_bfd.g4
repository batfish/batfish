parser grammar CiscoXr_bfd;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_bfd: BFD (bfd_inner | NEWLINE bfd_inner*);

bfd_inner
:
  bfd_no
  | bfd_null
;

bfd_no: NO bfd_no_null;

bfd_no_null
:
  (
    BUNDLE
    | DAMPENING
    | ECHO
    | INTERFACE
    | IPV6
    | MULTIHOP
    | MULTIPATH
    | TRAP
  ) null_rest_of_line
;

bfd_null
:
  (
    BUNDLE
    | DAMPENING
    | ECHO
    | INTERFACE
    | IPV6
    | MULTIHOP
    | MULTIPATH
    | TRAP
  ) null_rest_of_line
;
