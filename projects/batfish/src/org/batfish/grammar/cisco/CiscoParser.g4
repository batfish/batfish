parser grammar CiscoParser;

import
Cisco_common, Cisco_acl, Cisco_bgp, Cisco_ignored, Cisco_interface, Cisco_isis, Cisco_mpls, Cisco_ospf, Cisco_rip, Cisco_routemap, Cisco_static;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = CiscoLexer;
}

@header {
package org.batfish.grammar.cisco;
}

address_aiimgp_stanza
:
   ADDRESS ~NEWLINE* NEWLINE
;

address_family_multicast_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   ) NEWLINE address_family_multicast_tail
;

address_family_multicast_tail
:
   (
      MULTIPATH NEWLINE
      | interface_multicast_stanza
   )*
;

address_family_vrfd_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      MULTICAST
      | UNICAST
   )? NEWLINE afvrfd_stanza*
   (
      EXIT_ADDRESS_FAMILY NEWLINE
   )?
;

afvrfd_stanza
:
   null_afvrfd_stanza
;

aiimgp_stanza
:
   address_aiimgp_stanza
;

allow_iimgp_stanza
:
   ALLOW ~NEWLINE* NEWLINE aiimgp_stanza*
;

asa_comment_stanza
:
   COLON ~NEWLINE* NEWLINE
;

banner_stanza
:
   BANNER
   (
      (
         (
            ESCAPE_C ~ESCAPE_C* ESCAPE_C
         )
         |
         (
            POUND ~POUND* POUND
         )
         |
         (
            NEWLINE ~EOF_LITERAL* EOF_LITERAL
         )
      )
   ) NEWLINE?
;

certificate_stanza
:
   CERTIFICATE ~QUIT* QUIT NEWLINE
;

cisco_configuration
:
   NEWLINE?
   (
      sl += stanza
   )+ COLON? NEWLINE? EOF
;

del_stanza
:
   DEL ~NEWLINE* NEWLINE
;

hostname_stanza
:
   (
      HOSTNAME
      | SWITCHNAME
   ) name = ~NEWLINE* NEWLINE
;

iimgp_stanza
:
   allow_iimgp_stanza
;

imgp_stanza
:
   interface_imgp_stanza
   | null_imgp_stanza
;

inband_mgp_stanza
:
   (
      INBAND
      | OUT_OF_BAND
   ) NEWLINE imgp_stanza*
;

interface_imgp_stanza
:
   INTERFACE ~NEWLINE* NEWLINE iimgp_stanza*
;

interface_multicast_stanza
:
   INTERFACE ~NEWLINE* NEWLINE
   (
      BSR_BORDER NEWLINE
   )?
   (
      ENABLE NEWLINE
   )?
;

ip_as_path_regex_mode_stanza
:
   IP AS_PATH REGEX_MODE
   (
      ASN
      | STRING
   ) NEWLINE
;

ip_default_gateway_stanza
:
   IP DEFAULT_GATEWAY gateway = IP_ADDRESS NEWLINE
;

ip_route_stanza
:
   IP ROUTE
   (
      VRF vrf = ~NEWLINE
   )? ip_route_tail
;

ip_route_tail
:
   (
      (
         address = IP_ADDRESS mask = IP_ADDRESS
      )
      | prefix = IP_PREFIX
   )
   (
      nexthopip = IP_ADDRESS
      | nexthopint = interface_name
      | nexthopprefix = IP_PREFIX
      | distance = DEC
      |
      (
         TAG tag = DEC
      )
      | perm = PERMANENT
      |
      (
         TRACK track = DEC
      )
      |
      (
         NAME variable
      )
   )* NEWLINE
;

ip_route_vrfc_stanza
:
   IP ROUTE ip_route_tail
;

l2vpn_stanza
:
   L2VPN NEWLINE xconnect_stanza*
;

management_plane_stanza
:
   MANAGEMENT_PLANE NEWLINE mgp_stanza*
;

mgp_stanza
:
   inband_mgp_stanza
;

multicast_routing_stanza
:
   MULTICAST_ROUTING NEWLINE
   (
      address_family_multicast_stanza
   )*
;

no_ip_access_list_stanza
:
   NO IP ACCESS_LIST ~NEWLINE* NEWLINE
;

null_stanza
:
   asa_comment_stanza
   | banner_stanza
   | certificate_stanza
   | del_stanza
   | management_plane_stanza
   | no_ip_access_list_stanza
   | null_block_stanza
   | null_standalone_stanza_DEPRECATED_DO_NOT_ADD_ITEMS
   | srlg_stanza
;

null_afvrfd_stanza
:
   MAXIMUM ~NEWLINE* NEWLINE
;

null_imgp_stanza
:
   NO?
   (
      VRF
   ) ~NEWLINE* NEWLINE
;

null_vrfd_stanza
:
   (
      RD
      | ROUTE_TARGET
   ) ~NEWLINE* NEWLINE
;

o_network
:
   NETWORK name = variable NEWLINE o_network_tail*
;

o_network_tail
:
   on_description
   | on_fqdn
   | on_host
   | on_nat
   | on_range
   | on_subnet
;

o_service
:
   SERVICE name = variable NEWLINE o_service_tail*
;

o_service_tail
:
   os_description
   | os_service
;

og_icmp_type
:
   ICMP_TYPE name = variable NEWLINE og_icmp_type_tail*
;

og_icmp_type_tail
:
   ogit_group_object
   | ogit_icmp_object
;

og_ip_address
:
   IP ADDRESS name = variable NEWLINE og_ip_address_tail*
;

og_ip_address_tail
:
   ogipa_host_info
   | ogipa_ip_addresses
;

og_network
:
   NETWORK name = variable NEWLINE og_network_tail*
;

og_network_tail
:
   ogn_group_object
   | ogn_network_object
;

og_protocol
:
   PROTOCOL name = variable NEWLINE og_protocol_tail*
;

og_protocol_tail
:
   ogp_description
   | ogp_group_object
   | ogp_protocol_object
;

og_service
:
   SERVICE name = variable NEWLINE og_service_tail*
;

og_service_tail
:
   ogs_description
   | ogs_group_object
   | ogs_service_object
;

og_user
:
   USER name = variable NEWLINE og_user_tail*
;

og_user_tail
:
   ogu_description
   | ogu_group_object
   | ogu_user
   | ogu_user_group
;

ogipa_host_info
:
   HOST_INFO IP_ADDRESS NEWLINE
;

ogipa_ip_addresses
:
   IP_ADDRESS+ NEWLINE
;

ogit_group_object
:
   GROUP_OBJECT name = variable NEWLINE
;

ogit_icmp_object
:
   ICMP_OBJECT icmp_object_type NEWLINE
;

ogn_group_object
:
   GROUP_OBJECT name = variable NEWLINE
;

ogn_network_object
:
   NETWORK_OBJECT ogn_network_object_tail
;

ogn_network_object_tail
:
   (
      prefix = IP_PREFIX
      | prefix6 = IPV6_PREFIX
      |
      (
         HOST
         (
            address = IP_ADDRESS
            | address6 = IPV6_ADDRESS
            | host = variable
         )
      )
      |
      (
         OBJECT name = variable
      )
      | host = variable
   ) NEWLINE
;

ogp_description
:
   description_line
;

ogp_group_object
:
   GROUP_OBJECT name = variable NEWLINE
;

ogp_protocol_object
:
   PROTOCOL_OBJECT protocol NEWLINE
;

ogs_description
:
   description_line
;

ogs_group_object
:
   GROUP_OBJECT name = variable NEWLINE
;

ogs_service_object
:
   (
      protocol
      |
      (
         OBJECT name = variable
      )
   ) NEWLINE
;

ogu_description
:
   description_line
;

ogu_group_object
:
   GROUP_OBJECT name = variable NEWLINE
;

ogu_user
:
   USER name = variable NEWLINE
;

ogu_user_group
:
   name = variable NEWLINE
;

on_description
:
   description_line
;

on_fqdn
:
   FQDN
   (
      V4
      | V6
   )? fqdn = variable NEWLINE
;

on_host
:
   HOST
   (
      address = IP_ADDRESS
      | address6 = IPV6_ADDRESS
   ) NEWLINE
;

on_nat
:
   NAT ~NEWLINE* NEWLINE // todo

;

on_range
:
   RANGE start = IP_ADDRESS end = IP_ADDRESS NEWLINE
;

on_subnet
:
   SUBNET
   (
      (
         address = IP_ADDRESS mask = IP_ADDRESS
      )
      | prefix6 = IPV6_PREFIX
   ) NEWLINE
;

os_description
:
   description_line
;

os_service
:
   SERVICE protocol NEWLINE //todo: change to os_service_typoe allowing tcp and udp port ranges

;

s_object
:
   OBJECT s_object_tail
;

s_object_tail
:
   o_network
   | o_service
;

s_object_group
:
   OBJECT_GROUP s_object_group_tail
;

s_object_group_tail
:
   og_icmp_type
   | og_ip_address
   | og_network
   | og_protocol
   | og_service
   | og_user
;

peer_stanza
:
   PEER IP_ADDRESS NEWLINE
   (
      null_block_substanza
   )*
;

p2p_stanza
:
   P2P VARIABLE NEWLINE INTERFACE ~NEWLINE* NEWLINE INTERFACE ~NEWLINE* NEWLINE
;

router_multicast_stanza
:
   ROUTER
   (
      IGMP
      | MLD
      | MSDP
      | PIM
   ) NEWLINE router_multicast_tail
;

router_multicast_tail
:
   (
      address_family_multicast_stanza
      | null_block_substanza
      | peer_stanza
   )*
;

srlg_interface_numeric_stanza
:
   DEC ~NEWLINE* NEWLINE
;

srlg_interface_stanza
:
   INTERFACE ~NEWLINE* NEWLINE srlg_interface_numeric_stanza*
;

srlg_stanza
:
   SRLG NEWLINE srlg_interface_stanza*
;

stanza
:
   appletalk_access_list_stanza
   | community_set_stanza
   | extended_access_list_stanza
   | hostname_stanza
   | interface_stanza
   | ip_as_path_access_list_stanza
   | ip_as_path_regex_mode_stanza
   | ip_community_list_expanded_stanza
   | ip_community_list_standard_stanza
   | ip_default_gateway_stanza
   | ip_prefix_list_stanza
   | ip_route_stanza
   | ipv6_router_ospf_stanza
   | ipx_sap_access_list_stanza
   | l2vpn_stanza
   | multicast_routing_stanza
   | mpls_ldp_stanza
   | mpls_traffic_eng_stanza
   | null_stanza
   | s_object
   | s_object_group
   | prefix_set_stanza
   | protocol_type_code_access_list_stanza
   | route_map_stanza
   | route_policy_stanza
   | router_bgp_stanza
   | router_isis_stanza
   | router_multicast_stanza
   | router_ospf_stanza
   | router_ospfv3_stanza
   | router_rip_stanza
   | router_static_stanza
   | rsvp_stanza
   | standard_access_list_stanza
   | switching_mode_stanza
   | unrecognized_block_stanza
   | vrf_context_stanza
   | vrf_definition_stanza
;

switching_mode_stanza
:
   SWITCHING_MODE ~NEWLINE* NEWLINE
;

vrf_context_stanza
:
   VRF CONTEXT name = variable NEWLINE vrfc_stanza*
;

vrf_definition_stanza
:
   VRF DEFINITION? name = variable NEWLINE vrfd_stanza*
   (
      EXIT_VRF NEWLINE
   )?
;

vrfc_stanza
:
   ip_route_vrfc_stanza
;

vrfd_stanza
:
   address_family_vrfd_stanza
   | null_vrfd_stanza
;

xconnect_stanza
:
   XCONNECT GROUP VARIABLE NEWLINE p2p_stanza*
;
