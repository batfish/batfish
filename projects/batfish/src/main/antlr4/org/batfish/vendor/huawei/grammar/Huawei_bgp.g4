parser grammar Huawei_bgp;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// BGP configuration

// BGP stanza
s_bgp
:
   BGP as_num = uint32
   (
      bgp_substanza
   )*
;

// BGP sub-stanzas
bgp_substanza
:
   bgp_router_id
   | bgp_peer
   | bgp_peer_group
   | bgp_network
   | bgp_import
   | bgp_export
   | bgp_null
;

// Router ID: router-id 1.1.1.1
bgp_router_id
:
   ROUTER_ID router_ip = ip_address
;

// BGP peer: peer 192.168.1.2 as-number 65002
// Note: as-number is optional for edge case handling (malformed configs)
bgp_peer
:
   PEER peer_ip = ip_address (AS_NUMBER peer_as = uint16)?
   (
      // Optional peer parameters
      bgp_peer_param
   )*
;

// BGP peer parameters (simplified)
bgp_peer_param
:
   // peer X.X.X.X connect-interface GigabitEthernet0/0/0
   CONNECT_INTERFACE iface = variable
   |
   // peer X.X.X.X password <password>
   PASSWORD password = variable
   |
   // Other parameters (ignore for now)
   null_rest_of_line
;

// BGP peer group: group GROUP_NAME external
bgp_peer_group
:
   GROUP group_name = variable
   (
      // Optional group parameters
      bgp_group_param
   )*
;

// BGP peer group parameters
bgp_group_param
:
   // Type: internal or external
   INTERNAL
   |
   EXTERNAL
   |
   // Other parameters (ignore)
   null_rest_of_line
;

// Network announcement: network 10.0.0.0 255.255.255.0
bgp_network
:
   NETWORK network_addr = ip_address network_mask = ip_address
   (
      // Optional route-policy
      null_rest_of_line
   )?
;

// Import policy: import-route <protocol>
bgp_import
:
   IMPORT_ROUTE protocol = variable
   (
      null_rest_of_line
   )?
;

// Export policy: export-route something (ignore details)
bgp_export
:
   // Placeholder for export configurations
   null_rest_of_line
;

// Null BGP configuration (parse but ignore)
bgp_null
:
   NO?
   (
      null_rest_of_line
   )
;
