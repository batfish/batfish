parser grammar Fortios_vpn;

options {
  tokenVocab = FortiosLexer;
}

c_vpn: VPN (
    cv_ipsec
    | IGNORED_CONFIG_BLOCK
    | null_rest_of_line  // empty config vpn block
);

cv_ipsec: IPSEC newline cvi*;

cvi: cvi_phase1 | cvi_phase2;

cvi_phase1: CONFIG PHASE1 newline cvip1_edit* END NEWLINE;

cvip1_edit: EDIT ipsec_phase1_name newline cvip1e* NEXT newline;

cvip1e
:
    SET (
        cvip1e_set_interface
        | cvip1e_set_remote_gw
        | cvip1e_set_proposal
        | cvip1e_set_psksecret
        | cvip1e_set_dhgrp
        | cvip1e_set_keylife
    )
;

cvip1e_set_interface: INTERFACE ipsec_interface newline;

cvip1e_set_remote_gw: REMOTE_GW ipsec_remote_gw newline;

cvip1e_set_proposal: PROPOSAL ipsec_proposal newline;

cvip1e_set_psksecret: PSKSECRET ipsec_psksecret newline;

cvip1e_set_dhgrp: DHGRP ipsec_dhgrp newline;

cvip1e_set_keylife: KEYLIFE ipsec_keylife newline;

cvi_phase2: CONFIG PHASE2 newline cvip2_edit* END NEWLINE;

cvip2_edit: EDIT ipsec_phase2_name newline cvip2e* NEXT newline;

cvip2e
:
    SET (
        cvip2e_set_phase1name
        | cvip2e_set_proposal
        | cvip2e_set_src_name
        | cvip2e_set_dst_name
    )
;

cvip2e_set_phase1name: PHASE1NAME ipsec_phase1name newline;

cvip2e_set_proposal: PROPOSAL ipsec_proposal newline;

cvip2e_set_src_name: SRC_NAME ipsec_src_name newline;

cvip2e_set_dst_name: DST_NAME ipsec_dst_name newline;

ipsec_phase1_name: str;

ipsec_phase2_name: str;

ipsec_interface: str;

ipsec_remote_gw: str;

ipsec_proposal: str;

ipsec_psksecret: str;

ipsec_dhgrp: str;

ipsec_keylife: uint32;

ipsec_phase1name: str;

ipsec_src_name: str;

ipsec_dst_name: str;
