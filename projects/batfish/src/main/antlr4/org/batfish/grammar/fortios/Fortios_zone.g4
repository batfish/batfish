parser grammar Fortios_zone;

options {
  tokenVocab = FortiosLexer;
}

cs_zone: ZONE newline csz_edit*;

csz
:
    csz_edit
    | csz_rename
;

csz_edit: EDIT zone_name newline csze* NEXT newline;

csz_rename: RENAME current_name = zone_name TO new_name = zone_name newline;

csze
:
    (
        SET (csz_set_singletons | csz_set_interface)
        | APPEND csz_append_interface
        | SELECT csz_set_interface
    )
;

csz_set_singletons:
    csz_set_description
    | csz_set_intrazone
;

csz_set_description: DESCRIPTION description = str newline;

csz_set_intrazone: INTRAZONE value = allow_or_deny newline;

csz_set_interface: INTERFACE interfaces = interface_names newline;

csz_append_interface: INTERFACE interfaces = interface_names newline;

// Up to 35 characters
zone_name: str;
