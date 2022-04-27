parser grammar Cisco_mpls;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_mpls
:
  MPLS
  (
    mpls_ldp
  )
;

mpls_ldp
:
  LDP
  (
    mldp_password
  )
;

mldp_password: PASSWORD mldp_pw_required;

mldp_pw_required: REQUIRED FOR acl = variable NEWLINE;