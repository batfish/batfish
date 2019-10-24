parser grammar F5BigipStructured_cm;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

s_cm
:
  CM
  (
    cm_cert
    | cm_device
    | cm_device_group
    | cm_key
    | cm_traffic_group
    | cm_trust_domain
    | unrecognized
  )
;

cm_cert
:
  CERT name = structure_name ignored
;

cm_device
:
  DEVICE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      cmd_base_mac
      | cmd_cert
      | cmd_configsync_ip
      | cmd_hostname
      | cmd_key
      | cmd_management_ip
      | cmd_null
      | cmd_self_device
      | cmd_unicast_address
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

cmd_base_mac
:
  BASE_MAC mac = mac_address NEWLINE
;

cmd_cert
:
  CERT name = structure_name NEWLINE
;

cmd_configsync_ip
:
  CONFIGSYNC_IP ip = ip_address NEWLINE
;

cmd_hostname
:
  HOSTNAME hostname = word NEWLINE
;

cmd_key
:
  KEY name = structure_name NEWLINE
;

cmd_management_ip
:
  MANAGEMENT_IP ip = ip_address NEWLINE
;

cmd_null
:
  (
    ACTIVE_MODULES
    | BUILD
    | CHASSIS_ID
    | EDITION
    | MARKETING_NAME
    | OPTIONAL_MODULES
    | PLATFORM_ID
    | PRODUCT
    | TIME_ZONE
    | VERSION
  ) ignored
;

cmd_self_device
:
  SELF_DEVICE
  (
    TRUE
    | FALSE
  ) NEWLINE
;

cmd_unicast_address
:
  UNICAST_ADDRESS BRACE_LEFT
  (
    NEWLINE cmdua_address*
  )? BRACE_RIGHT NEWLINE
;

cmdua_address
:
  BRACE_LEFT NEWLINE
  (
    cmduaa_effective_ip
    | cmduaa_effective_port
    | cmduaa_ip
    | cmduaa_port
    | unrecognized
  )* BRACE_RIGHT NEWLINE
;

cmduaa_effective_ip
:
  EFFECTIVE_IP ip = unicast_address_ip NEWLINE
;

unicast_address_ip
:
  ip = ip_address
  | MANAGEMENT_IP
;

cmduaa_effective_port
:
  EFFECTIVE_PORT port = uint16 NEWLINE
;

cmduaa_ip
:
  IP ip = unicast_address_ip NEWLINE
;

cmduaa_port
:
  PORT port = uint16 NEWLINE
;

cm_device_group
:
  DEVICE_GROUP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      cmdg_auto_sync
      | cmdg_devices
      | cmdg_hidden
      | cmdg_network_failover
      | cmdg_type
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

cmdg_auto_sync
:
  AUTO_SYNC
  (
    DISABLED
    | ENABLED
  ) NEWLINE
;

cmdg_devices
:
  DEVICES BRACE_LEFT
  (
    NEWLINE cmdgd_device*
  )? BRACE_RIGHT NEWLINE
;

cmdgd_device
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      cmdgdd_set_sync_leader
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

cmdgdd_set_sync_leader
:
  SET_SYNC_LEADER NEWLINE
;

cmdg_hidden
:
  HIDDEN_LITERAL
  (
    FALSE
    | TRUE
  ) NEWLINE
;

cmdg_network_failover
:
  NETWORK_FAILOVER
  (
    DISABLED
    | ENABLED
  ) NEWLINE
;

cmdg_type
:
  TYPE type = device_group_type NEWLINE
;

device_group_type
:
  SYNC_FAILOVER
  | SYNC_ONLY
;

cm_key
:
  KEY name = structure_name ignored
;

cm_traffic_group
:
  TRAFFIC_GROUP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      cmtg_ha_group
      | cmtg_mac
      | cmtg_unit_id
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

cmtg_ha_group
:
  HA_GROUP name = structure_name NEWLINE
;

cmtg_mac
:
  MAC mac = mac_address NEWLINE
;

cmtg_unit_id
:
  UNIT_ID id = uint16 NEWLINE
;

cm_trust_domain
:
  TRUST_DOMAIN name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      cmtd_ca_cert
      | cmtd_ca_cert_bundle
      | cmtd_ca_devices
      | cmtd_ca_key
      | cmtd_null
      | cmtd_trust_group
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

cmtd_ca_cert
:
  CA_CERT name = structure_name NEWLINE
;

cmtd_ca_cert_bundle
:
  CA_CERT_BUNDLE name = structure_name NEWLINE
;

cmtd_ca_devices
:
  CA_DEVICES BRACE_LEFT names += structure_name* BRACE_RIGHT NEWLINE
;

cmtd_ca_key
:
  CA_KEY name = structure_name NEWLINE
;

cmtd_null
:
  (
    GUID
    | STATUS
  ) ignored
;

cmtd_trust_group
:
  TRUST_GROUP name = structure_name NEWLINE
;
