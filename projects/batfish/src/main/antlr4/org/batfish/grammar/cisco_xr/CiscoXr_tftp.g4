parser grammar CiscoXr_tftp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_tftp
:
  TFTP
  (
    tftp_null
    | tftp_vrf
  )
;

tftp_null
:
  (
    CLIENT
  ) null_rest_of_line
;

tftp_vrf
:
  (VRF vrf = vrf_name)?
  (IPV4 | IPV6)
  SERVER HOMEDIR WORD
  (
    ACCESS_LIST name = access_list_name
    | DSCP dscp = dscp_type
    | MAX_SERVERS (max_servers = tftp_max_servers | no_limit = NO_LIMIT)
  )*
;

tftp_max_servers
:
  // 1-2147483647
  uint32
;