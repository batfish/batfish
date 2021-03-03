parser grammar Fortios_interface;

options {
  tokenVocab = FortiosLexer;
}

cs_interface: INTERFACE NEWLINE csi_edit*;

csi_edit
:
    EDIT interface_name NEWLINE (
        SET csi_set_singletons
    )* NEXT NEWLINE
;

csi_set_singletons:
    csi_set_vdom
    | csi_set_ip
    | csi_set_type
    | csi_set_alias
    | csi_set_status
    | csi_set_mtu_override
    | csi_set_mtu
    | csi_set_description
    | csi_set_snmp_index
    | csi_set_vrf
;

csi_set_vdom: VDOM vdom = str NEWLINE;

csi_set_ip: IP ip = ip_address_with_mask_or_prefix NEWLINE;

csi_set_type: TYPE type = interface_type NEWLINE;

csi_set_alias: ALIAS alias = interface_alias NEWLINE;

csi_set_status: STATUS status = enabled_or_disabled NEWLINE;

csi_set_mtu_override: MTU_OVERRIDE value = enabled_or_disabled NEWLINE;

csi_set_mtu: MTU value = mtu NEWLINE;

csi_set_description: DESCRIPTION description = str NEWLINE;

csi_set_snmp_index: SNMP_INDEX null_rest_of_line;

csi_set_vrf: VRF value = vrf NEWLINE;

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
