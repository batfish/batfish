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
      | ni_disabled
      | ni_enabled
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

net_route
:
  ROUTE name = word BRACE_LEFT
  (
    NEWLINE
    (
      nroute_gw
      | nroute_network
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nroute_gw
:
  GW gw = word NEWLINE
;

nroute_network
:
  NETWORK network = word NEWLINE
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
  SELF name = word BRACE_LEFT
  (
    NEWLINE
    (
      ns_address
      | ns_allow_service
      | ns_traffic_group
      | ns_vlan
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

net_trunk
:
  TRUNK name = word BRACE_LEFT
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
  VLAN name = word BRACE_LEFT
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

ni_disabled
:
  DISABLED NEWLINE
;

ni_enabled
:
  ENABLED NEWLINE
;

ns_address
:
  ADDRESS interface_address = word NEWLINE
;

ns_allow_service
:
  ALLOW_SERVICE ALL NEWLINE
;

ns_traffic_group
:
  TRAFFIC_GROUP name = word NEWLINE
;

ns_vlan
:
  VLAN name = word NEWLINE
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
  TAG tag = word NEWLINE
;

nvi_interface
:
  name = word BRACE_LEFT NEWLINE? BRACE_RIGHT NEWLINE
;

s_net
:
  NET
  (
    net_interface
    | net_route
    | net_routing
    | net_self
    | net_trunk
    | net_vlan
    | unrecognized
  )
;
