parser grammar Arista_pim;

import Arista_common;

options {
   tokenVocab = AristaLexer;
}

// router pim is newer syntax (4.24 ; ip pim in EOS 4.20)
s_router_pim: PIM (
  pim_sparse_mode
);

pim_sparse_mode:
  SPARSE_MODE NEWLINE
  pim_sm_inner*
;

pim_sm_inner:
  pim_sm_ipv4
;

pim_sm_ipv4:
  IPV4 NEWLINE
  pim_sm4_inner*
;

pim_sm4_inner:
  pim_sm4_rp
;

pim_sm4_rp: RP (
  pim_sm4_rp_address
);

pim_sm4_rp_address: ADDRESS IP_ADDRESS ACCESS_LIST name = variable NEWLINE;