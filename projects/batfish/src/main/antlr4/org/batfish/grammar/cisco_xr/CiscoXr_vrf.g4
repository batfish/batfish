parser grammar CiscoXr_vrf;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_vrf: VRF name = vrf_name NEWLINE vrf_inner*;

vrf_inner
:
  vrf_address_family
  | vrf_description
  | vrf_null
;

vrf_address_family: ADDRESS_FAMILY af = vrf_address_family_type NEWLINE vrf_af_inner*;

vrf_address_family_type
:
  (
    IPV4
    | IPV6
  )
  (
    FLOWSPEC
    | MULTICAST
    | UNICAST
  )
;

vrf_af_inner
:
  vrf_af_export
  | vrf_af_import
  | vrf_af_null
;

vrf_af_export
:
  EXPORT vrf_af_export_inner
;

vrf_af_export_inner
:
  vrf_afe_route_policy
  | vrf_afe_route_target
  | vrf_afe_to
;

vrf_afe_route_policy: ROUTE_POLICY policy = route_policy_name NEWLINE;

vrf_afe_route_target
:
  ROUTE_TARGET
  (
    vrf_afe_route_target_value
    | NEWLINE vrf_afe_route_target_value*
  )
;

vrf_afe_route_target_value: route_target NEWLINE;

vrf_afe_to
:
  TO
  (
    vrf_afet_default_vrf
    | vrf_afet_vrf
  )
;

vrf_afet_default_vrf: DEFAULT_VRF ROUTE_POLICY policy = route_policy_name ALLOW_IMPORTED_VPN? NEWLINE;

vrf_afet_vrf: VRF ALLOW_IMPORTED_VPN NEWLINE;

vrf_af_import
:
  IMPORT vrf_af_import_inner
;

vrf_af_import_inner
:
  vrf_afi_from
  | vrf_afi_route_policy
  | vrf_afi_route_target
;

vrf_afi_from
:
  FROM
  (
    vrf_afif_default_vrf
    | vrf_afif_vrf
  )
;

vrf_afif_default_vrf: DEFAULT_VRF ROUTE_POLICY policy = route_policy_name ADVERTISE_AS_VPN? NEWLINE;

vrf_afif_vrf: VRF ADVERTISE_AS_VPN NEWLINE;

vrf_afi_route_policy: ROUTE_POLICY policy = route_policy_name NEWLINE;

vrf_afi_route_target
:
  ROUTE_TARGET
  (
    vrf_afi_route_target_value
    | NEWLINE vrf_afi_route_target_value*
  )
;

vrf_afi_route_target_value: route_target NEWLINE;

vrf_af_null
:
   NO?
   (
      MAXIMUM
   ) null_rest_of_line
;

vrf_description:
   description_line
;

vrf_null
:
  NO?
  (
    MODE
  ) null_rest_of_line
;
