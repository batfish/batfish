parser grammar Fortios_system;

options {
  tokenVocab = FortiosLexer;
}

c_system: SYSTEM (
  cs_bfd
  | cs_global
  | cs_interface
  | cs_npu
  | cs_replacemsg
  | cs_zone
  | IGNORED_CONFIG_BLOCK
);

cs_global: GLOBAL newline csg*;

csg: csg_set | csg_split_port_mode | csg_ignored_config_block;

csg_set: SET (csg_hostname | csg_set_unimplemented);

csg_hostname: HOSTNAME host=device_hostname newline;

csg_set_unimplemented: null_rest_of_line;

csg_ignored_config_block: CONFIG IGNORED_CONFIG_BLOCK END newline;

csg_split_port_mode: CONFIG SPLIT_PORT_MODE newline csgspm_edit* END newline;

csgspm_edit: EDIT interface_name newline csgspm_line* NEXT newline;

csgspm_line: (SET | UNSET) null_rest_of_line;

cs_bfd: BFD newline csb*;

csb: csb_set;

csb_set: SET (
    csb_set_interval
    | csb_set_min_rx
    | csb_set_min_tx
    | csb_set_multiplier
);

csb_set_interval: INTERVAL interval = str newline;
csb_set_min_rx: MIN_RX min_rx = str newline;
csb_set_min_tx: MIN_TX min_tx = str newline;
csb_set_multiplier: MULTIPLIER multiplier = str newline;

// [A-Za-z0-9_-]
device_hostname: str;

cs_replacemsg: REPLACEMSG major_type=replacemsg_major_type minor_type=replacemsg_minor_type newline csr*;

replacemsg_major_type:
  ADMIN
  | ALERTMAIL
  | AUTH
  | AUTOMATION
  | FORTIGUARD_WF
  | FTP
  | HTTP
  | ICAP
  | MAIL
  | NAC_QUAR
  | SPAM
  | SSLVPN
  | TRAFFIC_QUOTA
  | UTM
  | WEBPROXY
;

replacemsg_minor_type:
/* TODO: something smarter. Enumerating the minor types is complicated by the fact that in show
         output they appear in quotes, but need not be entered that way. */
  word;

csr: csr_set | csr_unset;

csr_set: SET csr_set_buffer;

csr_set_buffer: BUFFER buffer=str newline;

csr_unset: UNSET csr_unset_buffer;

csr_unset_buffer: BUFFER newline;

cs_npu: NPU newline csnpu_line* END newline;

csnpu_line
:
  csnpu_set
  | csnpu_config
;

csnpu_set: SET null_rest_of_line;

csnpu_config: CONFIG IGNORED_CONFIG_BLOCK END newline;
