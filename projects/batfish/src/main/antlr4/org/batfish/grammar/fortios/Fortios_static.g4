parser grammar Fortios_static;

options {
  tokenVocab = FortiosLexer;
}

cr_static: STATIC newline crs_edit*;

crs_edit: EDIT route_num newline crse* NEXT newline;

crse: crs_set;

crs_set: SET crs_set_singletons;

crs_set_singletons:
    crs_set_device
    | crs_set_distance
    | crs_set_dst
    | crs_set_gateway
    | crs_set_sdwan
    | crs_set_status
    | crs_set_bfd
;

crs_set_device: DEVICE iface = interface_name newline;

crs_set_distance: DISTANCE route_distance newline;

crs_set_dst: DST dst = ip_address_with_mask_or_prefix newline;

crs_set_gateway: GATEWAY gateway = ip_address newline;

crs_set_sdwan: SDWAN enabled = enable_or_disable newline;

crs_set_status: STATUS enabled = enable_or_disable newline;

crs_set_bfd: BFD bfd_enable = enable_or_disable newline;

// 0-4294967295 inclusive
route_num: str;

// 1-255
route_distance: uint8;
