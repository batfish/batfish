parser grammar Fortios_address;

options {
  tokenVocab = FortiosLexer;
}

cf_address: ADDRESS newline cfa*;

cfa: cfa_edit | cfa_rename;

cfa_rename: RENAME current_name = address_name TO new_name = address_name newline;

cfa_edit: EDIT address_name newline cfae* NEXT newline;

cfae: SET cfa_set_singletons;

cfa_set_singletons:
    cfa_set_allow_routing
    | cfa_set_associated_interface
    | cfa_set_comment
    | cfa_set_end_ip
    | cfa_set_interface
    | cfa_set_fabric_object
    | cfa_set_start_ip
    | cfa_set_subnet
    | cfa_set_type
    | cfa_set_wildcard
    | cfa_set_null
;

cfa_set_allow_routing: ALLOW_ROUTING value = enable_or_disable newline;

cfa_set_associated_interface: ASSOCIATED_INTERFACE name = interface_or_zone_name newline;

cfa_set_comment: COMMENT comment = str newline;

cfa_set_end_ip: END_IP ip = ip_address newline;

cfa_set_interface: INTERFACE name = interface_name newline;

cfa_set_fabric_object: FABRIC_OBJECT value = enable_or_disable newline;

cfa_set_start_ip: START_IP ip = ip_address newline;

// Shown in config as IP and mask, but accepts input formatted as prefix
cfa_set_subnet: SUBNET subnet = ip_address_with_mask_or_prefix newline;

cfa_set_type: TYPE type = address_type newline;

cfa_set_wildcard: WILDCARD wildcard = ip_wildcard newline;

cfa_set_null:
    (
        // color just controls the GUI icon color
        COLOR
        // Options for unsupported address types
        | COUNTRY
        | FQDN
        | SDN
        | SUB_TYPE
    ) null_rest_of_line;

address_type:
    IPMASK
    | IPRANGE
    | FQDN
    | GEOGRAPHY
    | WILDCARD
    | DYNAMIC
    | INTERFACE_SUBNET
    | MAC
;

// 1-79 characters
address_name: str;
