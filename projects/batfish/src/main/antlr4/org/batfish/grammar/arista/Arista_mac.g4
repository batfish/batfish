parser grammar Arista_mac;

default_mac
:
    MAC default_mac_access_list
;

default_mac_access_list
:
    ACCESS_LIST variable NEWLINE
;

mac_access_list
:
    ACCESS_LIST name = variable NEWLINE
    (
       macacl_line
    )*
;

macacl_line
:
    macacl_entry
    | macacl_no
    | macacl_remark
;

macacl_entry
:
    macacl_seq_number? (PERMIT|DENY) null_rest_of_line
;

macacl_no
:
    NO macacl_seq_number NEWLINE
;

macacl_remark
:
    REMARK RAW_TEXT NEWLINE
;

macacl_seq_number
:
// 1-4294967295
  UINT8
  | UINT16
  | UINT32
;

no_mac
:
   MAC no_mac_access_list
;

no_mac_access_list
:
   ACCESS_LIST variable NEWLINE
;

s_mac
:
    MAC mac_access_list
;
