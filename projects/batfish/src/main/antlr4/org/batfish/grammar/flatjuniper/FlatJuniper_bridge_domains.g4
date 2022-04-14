parser grammar FlatJuniper_bridge_domains;

import FlatJuniper_common;

s_bridge_domains
:
  BRIDGE_DOMAINS name = bridge_domain_name
  (
    apply
    | bd_domain_type
    | bd_interface
    | bd_routing_interface
    | bd_vlan_id
    | bd_vlan_id_list
    | bd_vlan_tags
    | bd_vxlan
  )
;

bridge_domain_name
:
  // cannot include '/'
  junos_name
;

bd_domain_type: DOMAIN_TYPE BRIDGE;

bd_interface: INTERFACE interface_id;

bd_routing_interface: ROUTING_INTERFACE interface_id;

bd_vlan_id
:
  VLAN_ID
  (
    ALL
    | NONE
    | vlan_number
  )
;

bd_vlan_id_list: VLAN_ID_LIST vlan_range;

bd_vlan_tags: VLAN_TAGS OUTER outer = vlan_number INNER inner = vlan_number;

bd_vxlan
:
  // TODO: https://www.juniper.net/documentation/us/en/software/junos/ovsdb-vxlan/evpn-vxlan/topics/ref/statement/vxlan.html
  //       May want to use common grammar for:
  //       - vlans name vxlan
  //       - bridge-domains bridge-domain-name vxlan
  VXLAN null_filler
;