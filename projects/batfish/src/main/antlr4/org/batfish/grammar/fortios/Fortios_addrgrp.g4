parser grammar Fortios_addrgrp;

options {
  tokenVocab = FortiosLexer;
}

cf_addrgrp: ADDRGRP newline cfaddrgrp*;

cfaddrgrp: cfaddrgrp_edit | cfaddrgrp_rename;

cfaddrgrp_rename: RENAME current_name = address_name TO new_name = address_name newline;

cfaddrgrp_edit: EDIT address_name newline cfaddrgrpe* NEXT newline;

cfaddrgrpe
:
    SET (cfaddrgrp_set_singletons | cfaddrgrp_set_lists)
    | SELECT cfaddrgrp_set_lists
    | APPEND cfaddrgrp_append_lists
;

cfaddrgrp_set_singletons:
    cfaddrgrp_set_comment
    | cfaddrgrp_set_exclude
    | cfaddrgrp_set_fabric_object
    | cfaddrgrp_set_type
    | cfaddrgrp_set_null
;

cfaddrgrp_set_comment: COMMENT comment = str newline;

cfaddrgrp_set_exclude: EXCLUDE exclude = enable_or_disable newline;

cfaddrgrp_set_fabric_object: FABRIC_OBJECT value = enable_or_disable newline;

cfaddrgrp_set_type: TYPE type = addrgrp_type newline;

// Cosmetic/non-functional attributes
cfaddrgrp_set_null:
    (
        COLOR
        | UUID
    ) null_rest_of_line
;

cfaddrgrp_set_lists
:
    cfaddrgrp_set_exclude_member
    | cfaddrgrp_set_member
;

cfaddrgrp_set_exclude_member: EXCLUDE_MEMBER address_names newline;

cfaddrgrp_set_member: MEMBER address_names newline;

cfaddrgrp_append_lists
:
    cfaddrgrp_append_exclude_member
    | cfaddrgrp_append_member
;

cfaddrgrp_append_exclude_member: EXCLUDE_MEMBER address_names newline;

cfaddrgrp_append_member: MEMBER address_names newline;

addrgrp_type: DEFAULT | FOLDER;
