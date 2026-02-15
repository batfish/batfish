parser grammar Fortios_isis;

options {
  tokenVocab = FortiosLexer;
}

cr_isis: ISIS newline cri*;

cri: cri_set | cri_config;

cri_set: SET cri_set_singletons;

cri_set_singletons
:
    cri_set_is_type
    | cri_set_net
;

cri_set_is_type: IS_TYPE isis_level newline;

cri_set_net: NET isis_net newline;

cri_config
:
    CONFIG (
        crii_isis_interface
        | IGNORED_CONFIG_BLOCK
    ) END NEWLINE
;

crii_isis_interface: ISIS_INTERFACE newline criii_edit*;

criii_edit: EDIT isis_interface_name newline criiie* NEXT newline;

criiie
:
    SET (
        criiie_set_circuit_type
        | criiie_set_metric
        | criiie_set_metric_level1
        | criiie_set_metric_level2
        | criiie_set_bfd
        | criiie_set_status
    )
;

criiie_set_circuit_type: CIRCUIT_TYPE isis_level newline;

criiie_set_metric: METRIC isis_metric newline;

criiie_set_metric_level1: METRIC_LEVEL1 isis_metric newline;

criiie_set_metric_level2: METRIC_LEVEL2 isis_metric newline;

criiie_set_bfd: BFD bfd_enable = enable_or_disable newline;

criiie_set_status: STATUS status_enable = enable_or_disable newline;

isis_level: str;

isis_net: str;

isis_interface_name: str;

isis_metric: uint32;
