parser grammar Huawei_nat;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// NAT configuration

// NAT stanza
s_nat
:
   NAT nat_substanza
;

// NAT sub-stanza
nat_substanza
:
   nat_address_group
   | nat_outbound
   | nat_static
   | nat_server
   | nat_null
;

// NAT address-group configuration (for address pools)
nat_address_group
:
   NO?
   ADDRESS_GROUP uint16
   (
      ADDRESS ip_address
      | MASK ip_address
   )*
;

// NAT outbound configuration (dynamic NAT / Easy IP)
nat_outbound
:
   NO?
   OUTBOUND
   (
      // nat outbound <acl-number> [acl-name]
      uint16
      (
         VARIABLE
      )?
      // nat outbound <acl-number> interface
      | INTERFACE
      // nat outbound <acl-number> <pool-name>
      | VARIABLE
   )
   (
      // optional: vpn-instance name
      VPN_INSTANCE VARIABLE
   )?
;

// NAT static configuration (static one-to-one NAT)
nat_static
:
   NO?
   STATIC
   (
      // nat static global <global-ip> inside <inside-ip>
      GLOBAL ip_address INSIDE ip_address
      // nat static global <global-ip> <netmask> inside <inside-ip> <netmask>
      | GLOBAL ip_address ip_address INSIDE ip_address ip_address
   )
   (
      // optional: vpn-instance name
      VPN_INSTANCE VARIABLE
   )?
;

// NAT server configuration (port forwarding / NAT with ports)
nat_server
:
   NO?
   SERVER
   (
      // nat server global <global-ip> inside <inside-ip>
      GLOBAL ip_address INSIDE ip_address
      // nat server protocol <protocol> global <global-ip> <global-port> inside <inside-ip> <inside-port>
      | PROTOCOL (TCP | UDP) GLOBAL ip_address global_port_proto = UINT16 INSIDE ip_address inside_port_proto = UINT16
      // nat server global <global-ip> <global-port> inside <inside-ip> [inside-port]
      | GLOBAL ip_address global_port_simple = UINT16 INSIDE ip_address
   )
   (
      // optional: vpn-instance name
      VPN_INSTANCE VARIABLE
   )?
;

// Null NAT configuration (parse but ignore unknown commands)
nat_null
:
   NO?
   (
      // Add NAT-specific commands here as needed
      null_rest_of_line
   )
;
