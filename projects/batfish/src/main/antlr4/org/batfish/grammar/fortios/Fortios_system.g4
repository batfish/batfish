parser grammar Fortios_system;

options {
  tokenVocab = FortiosLexer;
}

c_system: SYSTEM (
  cs_global
  | cs_replacemsg
);

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

csg_hostname: HOSTNAME host=device_hostname NEWLINE;

// [A-Za-z0-9_-]
device_hostname: word;

cs_replacemsg: REPLACEMSG major_type=replacemsg_major_type minor_type=word NEWLINE csr*;

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

csr:
  SET (
    csr_set_buffer
  )
  | UNSET (
    csr_unset_buffer
  )
;

csr_set_buffer: BUFFER buffer=str NEWLINE;

csr_unset_buffer: BUFFER NEWLINE;
