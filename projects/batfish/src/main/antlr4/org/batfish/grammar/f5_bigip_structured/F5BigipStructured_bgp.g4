parser grammar F5BigipStructured_bgp;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

nr_bgp
:
  BGP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      nrb_address_family
      | nrb_local_as
      | nrb_neighbor
      | nrb_null
      | nrb_profile
      | nrb_router_id
      | nrb_router_id6
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrb_address_family
:
  ADDRESS_FAMILY BRACE_LEFT
  (
    NEWLINE
    (
      nrbaf_ipv4
      | nrbaf_ipv6
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbaf_ipv4
:
  IPV4 BRACE_LEFT
  (
    NEWLINE
    (
      nrbaf_common
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbaf_ipv6
:
  IPV6 BRACE_LEFT
  (
    NEWLINE
    (
      nrbaf_common
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbaf_common
:
  nrbafc_redistribute
;

nrbafc_redistribute
:
  REDISTRIBUTE BRACE_LEFT
  (
    NEWLINE
    (
      nrbafcr_kernel
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbafcr_kernel
:
  KERNEL BRACE_LEFT
  (
    NEWLINE
    (
      nrbafcrk_route_map
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbafcrk_route_map
:
  ROUTE_MAP name = structure_name NEWLINE
;

nrb_local_as
:
  LOCAL_AS as = uint32 NEWLINE
;

nrb_neighbor
:
  NEIGHBOR BRACE_LEFT
  (
    NEWLINE nrbn_name*
  )? BRACE_RIGHT NEWLINE
;

nrbn_name
:
  (
    name = IP_ADDRESS
    | name = IPV6_ADDRESS
  ) BRACE_LEFT
  (
    NEWLINE
    (
      nrbnn_address_family
      | nrbnn_description
      | nrbnn_ebgp_multihop
      | nrbnn_null
      | nrbnn_remote_as
      | nrbnn_update_source
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbnn_address_family
:
  ADDRESS_FAMILY BRACE_LEFT
  (
    NEWLINE
    (
      nrbnnaf_ipv4
      | nrbnnaf_ipv6
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbnnaf_ipv4
:
  IPV4 BRACE_LEFT
  (
    NEWLINE
    (
      nrbnnaf_common
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbnnaf_ipv6
:
  IPV6 BRACE_LEFT
  (
    NEWLINE
    (
      nrbnnaf_common
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbnnaf_common
:
  nrbnnafc_activate
  | nrbnnafc_null
  | nrbnnafc_route_map
;

nrbnnafc_activate
:
  ACTIVATE DISABLED NEWLINE
;

nrbnnafc_null
:
  (
    CAPABILITY
    | MAXIMUM_PREFIX
  ) ignored
;

nrbnnafc_route_map
:
  ROUTE_MAP BRACE_LEFT
  (
    NEWLINE
    (
      nrbnnafcr_out
    )*
  )? BRACE_RIGHT NEWLINE
;

nrbnnafcr_out
:
  OUT name = structure_name NEWLINE
;

nrbnn_description
:
  DESCRIPTION description = word NEWLINE
;

nrbnn_ebgp_multihop
:
  EBGP_MULTIHOP count = uint16 NEWLINE
;

nrbnn_null
:
  (
    FALL_OVER
  ) ignored
;

nrbnn_remote_as
:
  REMOTE_AS as = uint32 NEWLINE
;

nrbnn_update_source
:
  UPDATE_SOURCE name = structure_name NEWLINE
;

nrb_null
:
  (
    ROUTE_DOMAIN
  ) ignored
;

nrb_profile
:
  PROFILE ignored
;

nrb_router_id
:
  ROUTER_ID id = ip_address NEWLINE
;

nrb_router_id6
:
  ROUTER_ID id6 = ipv6_address NEWLINE
;
