parser grammar F5BigipStructured_net;

import
F5BigipStructured_common, F5BigipStructured_bgp, F5BigipStructured_prefix_list, F5BigipStructured_route_map;

options {
  tokenVocab = F5BigipStructuredLexer;
}

bundle_speed
:
  FORTY_G
  | ONE_HUNDRED_G
;

net_interface
:
  INTERFACE name = word BRACE_LEFT
  (
    NEWLINE
    (
      ni_bundle
      | ni_bundle_speed
      | ni_description
      | ni_disabled
      | ni_enabled
      | ni_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

net_route
:
  ROUTE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      nroute_gw
      | nroute_gw6
      | nroute_network
      | nroute_network6
      | nroute_pool
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nroute_gw
:
  GW gw = ip_address NEWLINE
;

nroute_gw6
:
  GW gw6 = ipv6_address NEWLINE
;

nroute_network
:
  NETWORK
  (
    network = ip_prefix
    | DEFAULT
  ) NEWLINE
;

nroute_network6
:
  NETWORK network6 = ipv6_prefix NEWLINE
;

nroute_pool
:
  POOL pool = structure_name NEWLINE
;

net_routing
:
  ROUTING
  (
    nr_bgp
    | nr_prefix_list
    | nr_route_map
    | unrecognized
  )
;

net_self
:
  SELF name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      ns_address
      | ns_address6
      | ns_allow_service
      | ns_traffic_group
      | ns_vlan
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

ns_address
:
  ADDRESS interface_address = ip_prefix NEWLINE
;

ns_address6
:
  ADDRESS interface_address = ipv6_prefix NEWLINE
;

ns_allow_service
:
  ALLOW_SERVICE
  (
    nsas_all
    | nsas_specific
  )
;

nsas_all
:
  ALL NEWLINE
;

nsas_specific
:
// list
  ignored
;

ns_traffic_group
:
  TRAFFIC_GROUP name = structure_name NEWLINE
;

ns_vlan
:
  VLAN name = structure_name NEWLINE
;

net_trunk
:
  TRUNK name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      nt_interfaces
      | nt_lacp
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nt_interfaces
:
  INTERFACES BRACE_LEFT
  (
    NEWLINE nti_interface*
  )? BRACE_RIGHT NEWLINE
;

nti_interface
:
  name = word NEWLINE
;

nt_lacp
:
  LACP ENABLED NEWLINE
;

net_vlan
:
  VLAN name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      nv_interfaces
      | nv_tag
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

ni_bundle
:
  BUNDLE ENABLED NEWLINE
;

ni_bundle_speed
:
  BUNDLE_SPEED bundle_speed NEWLINE
;

ni_description
:
  DESCRIPTION text = word NEWLINE
;

ni_disabled
:
  DISABLED NEWLINE
;

ni_enabled
:
  ENABLED NEWLINE
;

ni_null
:
  (
    FORWARD_ERROR_CORRECTION
    | LLDP_ADMIN
    | LLDP_TLVMAP
  ) ignored
;

nv_interfaces
:
  INTERFACES BRACE_LEFT
  (
    NEWLINE
    (
      nvi_interface
    )*
  )? BRACE_RIGHT NEWLINE
;

nv_tag
:
  TAG tag = vlan_id NEWLINE
;

nvi_interface
:
  name = structure_name BRACE_LEFT NEWLINE? BRACE_RIGHT NEWLINE
;

s_net
:
  NET
  (
    net_dns_resolver
    | net_interface
    | net_ipsec
    | net_null
    | net_route
    | net_route_domain
    | net_routing
    | net_self
    | net_self_allow
    | net_trunk
    | net_tunnels
    | net_vlan
    | unrecognized
  )
;

net_dns_resolver
:
  DNS_RESOLVER ignored
;

net_ipsec
:
  (IPSEC | IPSECALG)
  (
    ipsec_ike_daemon
    | unrecognized
  )
;

ipsec_ike_daemon
:
  IKE_DAEMON ignored
;

net_null
:
  (
    FDB
    | LLDP_GLOBALS
    | STP
    | STP_GLOBALS
  ) ignored
;

net_route_domain
:
  ROUTE_DOMAIN ignored
;

net_self_allow
:
  SELF_ALLOW ignored
;

net_tunnels
:
  TUNNELS ignored
;
