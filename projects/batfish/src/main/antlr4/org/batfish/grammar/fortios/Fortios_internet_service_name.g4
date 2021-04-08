parser grammar Fortios_internet_service_name;

options {
  tokenVocab = FortiosLexer;
}

cf_internet_service_name: INTERNET_SERVICE_NAME newline cfisn_edit*;

cfisn_edit: EDIT internet_service_name newline cfisne* NEXT newline;

cfisne: cfisne_set | UNSET unimplemented;

cfisne_set
:
    SET (
        cfisne_set_internet_service_id
        | cfisne_set_type
    )
;

cfisne_set_internet_service_id: INTERNET_SERVICE_ID internet_service_id newline;

cfisne_set_type: TYPE internet_service_name_type newline;

internet_service_name_type: DEFAULT | LOCATION;
