parser grammar CiscoParser;

import
Cisco_common, Cisco_acl, Cisco_bgp, Cisco_ignored, Cisco_interface, Cisco_isis, Cisco_ospf, Cisco_rip, Cisco_routemap;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = CiscoLexer;
}

@header {
package org.batfish.grammar.cisco;
}

address_family_vrfd_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   ) NEWLINE afvrfd_stanza* EXIT_ADDRESS_FAMILY NEWLINE
;

afvrfd_stanza
:
   null_afvrfd_stanza
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
   ) NEWLINE
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
   )+ COLON? END? NEWLINE? EOF
;

hostname_stanza
:
   (
      HOSTNAME
      | SWITCHNAME
   ) name = ~NEWLINE* NEWLINE
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

macro_stanza
:
   MACRO ~NEWLINE* NEWLINE
;

no_ip_access_list_stanza
:
   NO IP ACCESS_LIST ~NEWLINE* NEWLINE
;

null_stanza
:
   banner_stanza
   | certificate_stanza
   | macro_stanza
   | no_ip_access_list_stanza
   | null_block_stanza
   | null_standalone_stanza_DEPRECATED_DO_NOT_ADD_ITEMS
;

null_afvrfd_stanza
:
   MAXIMUM ~NEWLINE* NEWLINE
;

null_vrfd_stanza
:
   (
      RD
      | ROUTE_TARGET
   ) ~NEWLINE* NEWLINE
;

stanza
:
   appletalk_access_list_stanza
   | extended_access_list_stanza
   | hostname_stanza
   | interface_stanza
   | ip_as_path_access_list_stanza
   | ip_community_list_expanded_stanza
   | ip_community_list_standard_stanza
   | ip_default_gateway_stanza
   | ip_prefix_list_stanza
   | ip_route_stanza
   | ipv6_router_ospf_stanza
   | ipx_sap_access_list_stanza
   | nexus_access_list_stanza
   | nexus_prefix_list_stanza
   | null_stanza
   | protocol_type_code_access_list_stanza
   | route_map_stanza
   | router_bgp_stanza
   | router_isis_stanza
   | router_ospf_stanza
   | router_rip_stanza
   | standard_access_list_stanza
   | switching_mode_stanza
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
   VRF DEFINITION name = variable NEWLINE vrfd_stanza*
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
