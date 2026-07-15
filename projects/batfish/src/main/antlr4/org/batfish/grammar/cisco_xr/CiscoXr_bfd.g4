parser grammar CiscoXr_bfd;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_bfd: BFD (bfd_inner | NEWLINE bfd_inner*);

bfd_inner
:
  bfd_bundle_null
  | bfd_dampening_null
  | bfd_echo_null
  | bfd_interface_null
  | bfd_ipv6_null
  | bfd_multihop_null
  | bfd_multipath_null
  | bfd_no
  | bfd_trap_null
;

bfd_no: NO ( bfd_no_bundle_null | bfd_no_dampening_null | bfd_no_echo_null | bfd_no_interface_null | bfd_no_ipv6_null | bfd_no_multihop_null | bfd_no_multipath_null | bfd_no_trap_null );

bfd_no_bundle_null
:
   BUNDLE null_rest_of_line
;
bfd_no_dampening_null
:
   DAMPENING null_rest_of_line
;
bfd_no_echo_null
:
   ECHO null_rest_of_line
;
bfd_no_interface_null
:
   INTERFACE null_rest_of_line
;
bfd_no_ipv6_null
:
   IPV6 null_rest_of_line
;
bfd_no_multihop_null
:
   MULTIHOP null_rest_of_line
;
bfd_no_multipath_null
:
   MULTIPATH null_rest_of_line
;
bfd_no_trap_null
:
   TRAP null_rest_of_line
;

bfd_bundle_null
:
   BUNDLE null_rest_of_line
;
bfd_dampening_null
:
   DAMPENING null_rest_of_line
;
bfd_echo_null
:
   ECHO null_rest_of_line
;
bfd_interface_null
:
   INTERFACE null_rest_of_line
;
bfd_ipv6_null
:
   IPV6 null_rest_of_line
;
bfd_multihop_null
:
   MULTIHOP null_rest_of_line
;
bfd_multipath_null
:
   MULTIPATH null_rest_of_line
;
bfd_trap_null
:
   TRAP null_rest_of_line
;
