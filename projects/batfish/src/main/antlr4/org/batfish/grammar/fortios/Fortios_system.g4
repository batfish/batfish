parser grammar Fortios_system;

options {
  tokenVocab = FortiosLexer;
}

c_system: SYSTEM (
  cs_global
  | cs_interface
  | cs_replacemsg
  | cs_zone
);

cs_global: GLOBAL newline csg*;

csg:
  SET (
    csg_hostname
  )
;

csg_hostname: HOSTNAME host=device_hostname newline;

// [A-Za-z0-9_-]
device_hostname: str;

cs_replacemsg: REPLACEMSG major_type=replacemsg_major_type minor_type=replacemsg_minor_type newline csr*;

replacemsg_major_type:
  ADMIN
  | ALERTMAIL
  | AUTH
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

csr:
  SET (
    csr_set_buffer
  )
  | UNSET (
    csr_unset_buffer
  )
;

csr_set_buffer: BUFFER buffer=str newline;

csr_unset_buffer: BUFFER newline;
