parser grammar Fortios_interface;

options {
  tokenVocab = FortiosLexer;
}

cs_interface: INTERFACE newline csi_edit*;

csi_edit: EDIT interface_name newline csie* NEXT newline;

csie: SET csi_set_singletons;

csi_set_singletons:
    csi_set_alias
    | csi_set_description
    | csi_set_ip
    | csi_set_mtu
    | csi_set_mtu_override
    | csi_set_status
    | csi_set_type
    | csi_set_vdom
    | csi_set_vrf
    | csi_set_null
;

csi_set_alias: ALIAS alias = interface_alias newline;

csi_set_description: DESCRIPTION description = str newline;

csi_set_ip: IP ip = ip_address_with_mask_or_prefix newline;

csi_set_mtu: MTU value = mtu newline;

csi_set_mtu_override: MTU_OVERRIDE value = enable_or_disable newline;

csi_set_status: STATUS status = up_or_down newline;

csi_set_type: TYPE type = interface_type newline;

csi_set_vdom: VDOM vdom = str newline;

csi_set_vrf: VRF value = vrf newline;

csi_set_null: SNMP_INDEX null_rest_of_line;

// 68-65535
mtu: uint16;

// 0-31
vrf: uint8;

interface_type:
    AGGREGATE
    | EMAC_VLAN
    | LOOPBACK
    | PHYSICAL
    | REDUNDANT
    | TUNNEL
    | VLAN
    | WL_MESH
;

// Up to 25 characters
interface_alias: str;

// Up to 15 characters
interface_name: str;
