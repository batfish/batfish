parser grammar Arista_ptp;

import Arista_common;

options {
   tokenVocab = AristaLexer;
}

ptp_domain_number: uint8; // 0-255

s_ptp: PTP (
  ptp_domain
  | ptp_forward_v1
  | ptp_mode
  | ptp_source
  | ptp_ttl
);

ptp_domain: DOMAIN num = ptp_domain_number NEWLINE;
ptp_forward_v1: FORWARD_V1 NEWLINE;
ptp_mode: MODE (BOUNDARY | DISABLED | E2ETRANSPARENT | P2PTRANSPARENT | GPTP) NEWLINE;
ptp_source: SOURCE IP ip = IP_ADDRESS NEWLINE;
ptp_ttl: TTL ttl = uint8 NEWLINE; // ttl: 1-255