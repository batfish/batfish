parser grammar CiscoXr_pim;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

router_pim: PIM NEWLINE router_pim_inner*;

router_pim_inner
:
  rpim_address_family
  | rpim_ipv4
  | rpim_vrf
  | rpimaf4_inner
;

rpim_vrf_inner
:
  rpim_address_family
  | rpim_ipv4
;

rpimaf_inner
:
  rpim_accept_register
  | rpim_allow_rp
  | rpim_bsr
  | rpim_mdt
  | rpim_mofrr
  | rpim_neighbor_filter
  | rpim_null
  | rpim_rp_address
  | rpim_rp_static_deny
  | rpim_rpf
  | rpim_sg_expiry_timer
  | rpim_spt_threshold
  | rpim_ssm_threshold
;

rpimaf4_inner
:
  rpimaf_inner
  | rpimaf4_auto_rp
  | rpimaf4_null
  | rpimaf4_interface
  | rpimaf4_rpf_redirect
;

rpimaf6_inner
:
  rpimaf_inner
  | rpimaf6_embedded_rp
  | rpimaf6_interface
;

rpimaf4_interface_inner
:
  rpimafi_inner
  | rpimafi_null
  | rpimaf4i_null
;

rpimafi_inner
:
  rpimafi_neighbor_filter
;

rpimaf6_interface_inner
:
  rpimafi_inner
  | rpimafi_null
;

rpim_null
:
  (
    CONVERGENCE
    | CONVERGENCE_TIMEOUT
    | DR_PRIORITY
    | GLOBAL
    | HELLO_INTERVAL
    | JOIN_PRUNE_INTERVAL
    | JOIN_PRUNE_MTU
    | LOG
    | MAXIMUM
    | MDT_HELLO_INTERVAL
    | MULTIPATH
    | NEIGHBOR_CHECK_ON_RECV
    | NEIGHBOR_CHECK_ON_SEND
    | NSF
    | OLD_REGISTER_CHECKSUM
    | OVERRIDE_INTERVAL
    | PROPAGATION_DELAY
    | REGISTER_SOURCE
    | SUPPRESS_DATA_REGISTERS
    | SUPPRESS_RPF_CHANGE_PRUNES
  ) null_rest_of_line
;

rpimaf4_null
:
  (
    EXPLICIT_RPF_VECTOR
    | RPF_VECTOR
  ) null_rest_of_line
;

rpimafi_null
:
  (
    BFD
    | BSR_BORDER
    | DISABLE
    | DR_PRIORITY
    | ENABLE
    | HELLO_INTERVAL
    | JOIN_PRUNE_INTERVAL
    | JOIN_PRUNE_MTU
    | MAXIMUM
    | OVERRIDE_INTERVAL
    | PROPAGATION_DELAY
  ) null_rest_of_line
;

rpimaf4i_null
:
  (
    RPF_REDIRECT
  ) null_rest_of_line
;

rpim_accept_register: ACCEPT_REGISTER name = access_list_name NEWLINE;

rpim_address_family: ADDRESS_FAMILY (rpim_ipv4 | rpimaf_ipv6);

rpim_ipv4: IPV4 NEWLINE rpimaf4_inner*;

rpimaf_ipv6: IPV6 NEWLINE rpimaf6_inner*;

rpim_allow_rp
:
  ALLOW_RP
  (
    NEWLINE
    | rpim_allow_rp_group_list
    | rpim_allow_rp_rp_list
  )
;

rpim_allow_rp_group_list: GROUP_LIST name = access_list_name NEWLINE;

rpim_allow_rp_rp_list: RP_LIST name = access_list_name NEWLINE;

rpim_bsr
:
  BSR
  (
    rpim_bsr_candidate_rp
    | rpim_bsr_null
  )
;

rpim_bsr_candidate_rp
:
  CANDIDATE_RP
  (IP_ADDRESS | IPV6_ADDRESS) // enforce in extractor based on AF
  (GROUP_LIST name = access_list_name)?
  (INTERVAL bsr_interval)?
  (PRIORITY bsr_priority)?
  BIDIR?
  NEWLINE
;

bsr_interval
:
  // 30-600
  uint16
;

bsr_priority
:
  // 1-255
  uint8
;

rpim_bsr_null
:
  (
    CANDIDATE_BSR
    | RELAY
  ) null_rest_of_line
;

rpim_mdt
:
  MDT
  (
    rpim_mdt_neighbor_filter
    | rpim_mdt_null
  )
;

rpim_mdt_neighbor_filter: NEIGHBOR_FILTER name = access_list_name NEWLINE;

rpim_mdt_null
:
  (
    C_MULTICAST_ROUTING // TODO: expand, has route-policy references
    | DATA
  ) null_rest_of_line
;

rpim_mofrr
:
 MOFRR NEWLINE rpim_mofrr_inner*
;

rpim_mofrr_inner
:
  rpim_mofrr_flow
  | rpim_mofrr_null
  | rpim_mofrr_rib
;

rpim_mofrr_flow: FLOW name = access_list_name NEWLINE;

rpim_mofrr_null
:
  (
    CLONE
    | NON_REVERTIVE
  ) null_rest_of_line
;

rpim_mofrr_rib: RIB name = access_list_name NEWLINE;

rpim_neighbor_filter: NEIGHBOR_FILTER name = access_list_name NEWLINE;

rpim_rp_address
:
  RP_ADDRESS
  (IP_ADDRESS | IPV6_ADDRESS) // enforce in extractor based on AF
  (name = access_list_name)? OVERRIDE?  BIDIR? NEWLINE
;

rpim_rp_static_deny: RP_STATIC_DENY name = access_list_name NEWLINE;

rpim_rpf: RPF TOPOLOGY name = route_policy_name NEWLINE;

rpim_sg_expiry_timer: SG_EXPIRY_TIMER sg_expiry_timer_duration (SG_LIST name = access_list_name)? NEWLINE;

sg_expiry_timer_duration
:
  // 40-57600s
  uint16
;

rpim_spt_threshold: SPT_THRESHOLD INFINITY (GROUP_LIST name = access_list_name)? NEWLINE;

rpim_ssm_threshold
:
  SSM THRESHOLD
  (
    rpim_ssm_threshold_null
    | rpim_ssm_threshold_range
  )
;

rpim_ssm_threshold_null
:
  (
    ALLOW_OVERRIDE
    | DISABLE
  ) null_rest_of_line
;

rpim_ssm_threshold_range: RANGE name = access_list_name NEWLINE;

rpim_vrf: VRF name = vrf_name NEWLINE rpim_vrf_inner*;

rpimaf4_auto_rp
:
  AUTO_RP
  (
    rpimaf4_auto_rp_candidate_rp
    | rpimaf4_auto_rp_null
  )
;

rpimaf4_auto_rp_candidate_rp
:
  CANDIDATE_RP iname = interface_name SCOPE auto_rp_candidate_rp_scope_ttl
  (GROUP_LIST aclname = access_list_name)?
  (INTERVAL auto_rp_candidate_rp_interval)?
  BIDIR?
  NEWLINE
;

auto_rp_candidate_rp_scope_ttl
:
  // 1-255 hops
  uint8
;

auto_rp_candidate_rp_interval
:
  // 1-600s
  uint16
;

rpimaf4_auto_rp_null
:
  (
    LISTEN
    | MAPPING_AGENT
    | RELAY
  ) null_rest_of_line
;

rpimaf4_rpf_redirect: RPF_REDIRECT ROUTE_POLICY name = route_policy_name NEWLINE;

rpimaf6_embedded_rp
:
  EMBEDDED_RP
  (
    rpimaf6_embedded_rp_null
    | rpimaf6_embedded_rp_rendezvous_point
  )
;

rpimaf6_embedded_rp_null
:
  (
    DISABLE
  ) null_rest_of_line
;

rpimaf6_embedded_rp_rendezvous_point: IPV6_ADDRESS name = access_list_name NEWLINE;

rpimaf4_interface: INTERFACE name = interface_name NEWLINE rpimaf4_interface_inner*;

rpimaf6_interface: INTERFACE name = interface_name NEWLINE rpimaf6_interface_inner*;

rpimafi_neighbor_filter: NEIGHBOR_FILTER name = access_list_name NEWLINE;

