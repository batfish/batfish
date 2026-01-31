parser grammar Fortios_interface;

options {
  tokenVocab = FortiosLexer;
}

cs_interface: INTERFACE newline csi_edit*;

csi_edit: EDIT interface_name newline csie* NEXT newline;

csie: csie_config | csie_set;

csie_config: CONFIG csiec_secondaryip;

csiec_secondaryip: SECONDARYIP newline csiecsip_edit* END newline;

csiecsip_edit: EDIT sip_number newline csiecsipe* NEXT newline;

csiecsipe
:
    csiecsipe_set
    | (UNSET | SELECT | UNSELECT | APPEND | CLEAR) unimplemented
;

csiecsipe_set: SET csiecsipe_set_ip;

csiecsipe_set_ip: IP ip = ip_address_with_mask_or_prefix newline;

csie_set: SET csi_set_singletons;

csi_set_singletons:
    csi_set_alias
    | csi_set_description
    | csi_set_interface
    | csi_set_ip
    | csi_set_member
    | csi_set_mtu
    | csi_set_mtu_override
    | csi_set_secondary_ip
    | csi_set_speed
    | csi_set_status
    | csi_set_type
    | csi_set_vdom
    | csi_set_allowaccess
    | csi_set_vlanid
    | csi_set_vrf
    | csi_set_dedicated_to
    | csi_set_device_identification
    | csi_set_forward_error_correction
    | csi_set_ip_managed_by_fortiipam
    | csi_set_lldp_transmission
    | csi_set_mediatype
    | csi_set_mode
    | csi_set_monitor_bandwidth
    | csi_set_role
    | csi_set_src_check
    | csi_set_null
;

csi_set_alias: ALIAS alias = interface_alias newline;

csi_set_description: DESCRIPTION description = str newline;

csi_set_interface: INTERFACE interface_name newline;

csi_set_ip: IP ip = ip_address_with_mask_or_prefix newline;

csi_set_member: MEMBER members = interface_names newline;

csi_set_mtu: MTU value = mtu newline;

csi_set_mtu_override: MTU_OVERRIDE value = enable_or_disable newline;

csi_set_secondary_ip: SECONDARY_IP value = enable_or_disable newline;

csi_set_speed: SPEED interface_speed newline;

csi_set_status: STATUS status = up_or_down newline;

csi_set_type: TYPE type = interface_type newline;

csi_set_vdom: VDOM vdom = str newline;

csi_set_vlanid: VLANID vlanid newline;

csi_set_vrf: VRF value = vrf newline;

csi_set_null: SNMP_INDEX null_rest_of_line;

csi_set_allowaccess: ALLOWACCESS null_rest_of_line;
csi_set_dedicated_to: DEDICATED_TO null_rest_of_line;
csi_set_device_identification: DEVICE_IDENTIFICATION null_rest_of_line;
csi_set_forward_error_correction: FORWARD_ERROR_CORRECTION null_rest_of_line;
csi_set_ip_managed_by_fortiipam: IP_MANAGED_BY_FORTIIPAM null_rest_of_line;
csi_set_lldp_transmission: LLDP_TRANSMISSION null_rest_of_line;
csi_set_mediatype: MEDIATYPE null_rest_of_line;
csi_set_mode: MODE null_rest_of_line;
csi_set_monitor_bandwidth: MONITOR_BANDWIDTH null_rest_of_line;
csi_set_role: ROLE null_rest_of_line;
csi_set_src_check: SRC_CHECK null_rest_of_line;

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

// 0-4294967295
sip_number: str;

interface_speed
:
    AUTO
    | TEN_FULL
    | TEN_HALF
    | HUNDRED_FULL
    | HUNDRED_HALF
    | HUNDRED_AUTO
    | THOUSAND_FULL
    | THOUSAND_HALF
    | THOUSAND_AUTO
    | TEN_THOUSAND_FULL
    | TEN_THOUSAND_HALF
    | TEN_THOUSAND_AUTO
    | TWENTY_FIVE_THOUSAND_FULL
    | TWENTY_FIVE_THOUSAND_AUTO
    | TWO_THOUSAND_FIVE_HUNDRED_AUTO
    | FORTY_THOUSAND_FULL
    | FORTY_THOUSAND_AUTO
    | FIVE_THOUSAND_AUTO
    | FIFTY_THOUSAND_FULL
    | FIFTY_THOUSAND_AUTO
    | HUNDRED_GFULL
    | HUNDRED_GAUTO
    | HUNDRED_GHALF
    | TWO_HUNDRED_GFULL
    | TWO_HUNDRED_GAUTO
    | FOUR_HUNDRED_G_FULL
    | FOUR_HUNDRED_G_AUTO
;
