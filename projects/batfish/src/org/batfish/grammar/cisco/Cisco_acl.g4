parser grammar Cisco_acl;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

access_list_ip_range
:
   (
      ip = IP_ADDRESS wildcard = IP_ADDRESS
   )
   | ANY
   | ANY4
   |
   (
      HOST? ip = IP_ADDRESS
   )
   | prefix = IP_PREFIX
   |
   (
      ADDRGROUP address_group = variable
   )
   |
   (
      INTERFACE iface = variable
   )
   |
   (
      OBJECT obj = variable
   )
   |
   (
      OBJECT_GROUP og = variable
   )
;

access_list_ip6_range
:
   (
      ip = IPV6_ADDRESS wildcard = IPV6_ADDRESS
   )
   | ANY
   | ANY6
   |
   (
      HOST? ipv6 = IPV6_ADDRESS
   )
   | ipv6_prefix = IPV6_PREFIX
   |
   (
      ADDRGROUP address_group = variable
   )
;

access_list_mac_range
:
   ANY
   |
   (
      address = MAC_ADDRESS_LITERAL wildcard = MAC_ADDRESS_LITERAL
   )
   |
   (
      HOST address = MAC_ADDRESS_LITERAL
   )
;

appletalk_access_list_null_tail
:
   action = access_list_action
   (
      (
         CABLE_RANGE ~NEWLINE*
      )
      | OTHER_ACCESS
   )? NEWLINE
;

appletalk_access_list_stanza
:
   ACCESS_LIST name = ACL_NUM_APPLETALK appletalk_access_list_null_tail
;

as_path_set_elem
:
   IOS_REGEX AS_PATH_SET_REGEX
;

as_path_set_stanza
:
   AS_PATH_SET name = variable NEWLINE
   (
      elems += as_path_set_elem NEWLINE?
      (
         COMMA NEWLINE? elems += as_path_set_elem NEWLINE?
      )*
   )? END_SET NEWLINE
;

bandwidth_irs_stanza
:
   BANDWIDTH ~NEWLINE* NEWLINE
;

community_set_stanza
:
   COMMUNITY_SET name = variable NEWLINE community_set_elem_list END_SET
   NEWLINE
;

community_set_elem_list
:
// no elements

   |
   (
      (
         (
            community_set_elem COMMA
         )
         | hash_comment
      ) NEWLINE
   )*
   (
      community_set_elem
      | hash_comment
   ) NEWLINE
;

community_set_elem
:
   rp_community_set_elem
   | ACCEPT_OWN
   | DFA_REGEX COMMUNITY_SET_REGEX
   | INTERNET
   | IOS_REGEX COMMUNITY_SET_REGEX
   | LOCAL_AS
   | NO_ADVERTISE
   | NO_EXPORT
   | PRIVATE_AS
;

etype
:
   ANY
   | ARP
   | IPV4_L5
;

extended_access_list_additional_feature
:
   ACK
   | BEYOND_SCOPE
   | COUNT
   | CWR
   | DESTINATION_UNREACHABLE
   |
   (
      DSCP dscp_type
   )
   |
   (
      icmpv6_message_type = DEC icmpv6_message_code = DEC?
   )
   | ECE
   | ECHO
   | ECHO_REPLY
   | ECHO_REQUEST
   |
   (
      ECN ecn = DEC
   )
   | ESTABLISHED
   | FIN
   | FRAGMENTS
   | HOP_LIMIT
   | HOPLIMIT
   | HOST_UNKNOWN
   | HOST_UNREACHABLE
   | LOG
   | LOG_INPUT
   | MLD_QUERY
   | MLD_REDUCTION
   | MLD_REPORT
   | MLDV2
   | ND
   | ND_NA
   | ND_NS
   | NEIGHBOR
   | NETWORK_UNKNOWN
   | NET_UNREACHABLE
   | PACKET_TOO_BIG
   | PARAMETER_PROBLEM
   | PORT_UNREACHABLE
   | PSH
   | REDIRECT
   | ROUTER
   | ROUTER_ADVERTISEMENT
   | ROUTER_SOLICITATION
   | RST
   | SOURCE_QUENCH
   | SYN
   | TIME_EXCEEDED
   | TRACEROUTE
   | TRACKED
   | TTL_EXCEEDED
   | TTL EQ DEC
   | UNREACHABLE
   | URG
;

extended_access_list_null_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = DEC
   )?
   (
      (
         access_list_action protocol access_list_ip_range port_specifier?
         access_list_ip_range port_specifier? REFLECT
      )
      | DYNAMIC
      | EVALUATE
      | MENU
      | REMARK
      | STATISTICS
   ) ~NEWLINE* NEWLINE
;

extended_access_list_stanza
:
   (
      (
         IP ACCESS_LIST EXTENDED name = variable_permissive
      )
      |
      (
         ACCESS_LIST num = ACL_NUM_EXTENDED
      )
      |
      (
         (
            IP
            | IPV4
         ) ACCESS_LIST name = variable_permissive
      )
      |
      (
         ACCESS_LIST name = variable_permissive EXTENDED
      )
   )
   (
      (
         NEWLINE
         (
            extended_access_list_tail
            | extended_access_list_null_tail
         )*
      )
      |
      (
         extended_access_list_tail
         | extended_access_list_null_tail
      )
   ) exit_line?
;

extended_access_list_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = DEC
   )? ala = access_list_action prot = protocol srcipr = access_list_ip_range
   (
      alps_src = port_specifier
   )? dstipr = access_list_ip_range
   (
      alps_dst = port_specifier
   )? features += extended_access_list_additional_feature*
   (
      NEXTHOP1 IPV4 nexthop1 = IP_ADDRESS
      (
         NEXTHOP2 IPV4 nexthop2 = IP_ADDRESS
      )?
   )?
   (
      SEQUENCE num = DEC
   )? NEWLINE
;

extended_ipv6_access_list_stanza
:
   IPV6 ACCESS_LIST EXTENDED? name = variable_permissive
   (
      (
         NEWLINE
         (
            extended_ipv6_access_list_tail
            | extended_access_list_null_tail
         )*
      )
      |
      (
         extended_ipv6_access_list_tail
         | extended_access_list_null_tail
      )
   ) exit_line?
;

extended_ipv6_access_list_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = DEC
   )? ala = access_list_action prot = protocol srcipr = access_list_ip6_range
   (
      alps_src = port_specifier
   )? dstipr = access_list_ip6_range
   (
      alps_dst = port_specifier
   )? features += extended_access_list_additional_feature*
   (
      NEXTHOP1 IPV6 nexthop1 = IPV6_ADDRESS
      (
         NEXTHOP2 IPV6 nexthop2 = IPV6_ADDRESS
      )?
   )?
   (
      SEQUENCE num = DEC
   )? NEWLINE
;

interface_rs_stanza
:
   INTERFACE name = interface_name NEWLINE irs_stanza*
;

ip_as_path_access_list_stanza
:
   IP AS_PATH ACCESS_LIST name = variable
   (
      ip_as_path_access_list_tail
      |
      (
         NEWLINE ip_as_path_access_list_tail*
      )
   )
;

ip_as_path_access_list_tail
:
   (
      SEQ DEC
   )? action = access_list_action as_path_regex = RAW_TEXT NEWLINE
;

ip_community_list_expanded_stanza
:
   (
      (
         IP COMMUNITY_LIST name = variable NEWLINE?
      )
      |
      (
         IP COMMUNITY_LIST EXPANDED name = variable
      )
      |
      (
         IP COMMUNITY_LIST EXTENDED name = variable
      )
      |
      (
         IP COMMUNITY_LIST num = COMMUNITY_LIST_NUM_EXPANDED
      )
   )
   (
      (
         NEWLINE ip_community_list_expanded_tail*
      )
      | ip_community_list_expanded_tail
   )
;

ip_community_list_expanded_tail
:
   (
      SEQ DEC
   )? ala = access_list_action DOUBLE_QUOTE?
   (
      remainder += ~( NEWLINE | DOUBLE_QUOTE )
   )+ DOUBLE_QUOTE? NEWLINE
;

ip_community_list_standard_stanza
:
   (
      (
         IP COMMUNITY_LIST STANDARD name = variable
      )
      |
      (
         IP COMMUNITY_LIST num = COMMUNITY_LIST_NUM_STANDARD
      )
   )
   (
      (
         NEWLINE ip_community_list_standard_tail*
      )
      | ip_community_list_standard_tail
   )
;

ip_community_list_standard_tail
:
   ala = access_list_action
   (
      communities += community
   )+ NEWLINE
;

ip_prefix_list_stanza
:
   (
      IP
      | IPV4
   ) PREFIX_LIST name = variable
   (
      (
         NEWLINE
         (
            ip_prefix_list_null_tail
            | ip_prefix_list_tail
         )*
      )
      |
      (
         ip_prefix_list_tail
         | ip_prefix_list_null_tail
      )
   )
;

ipv6_prefix_list_stanza
:
   IPV6 PREFIX_LIST name = variable
   (
      (
         NEWLINE
         (
            ip_prefix_list_null_tail
            | ipv6_prefix_list_tail
         )*
      )
      |
      (
         ipv6_prefix_list_tail
         | ip_prefix_list_null_tail
      )
   )
;

ip_prefix_list_null_tail
:
   (
      description_line
   )
   |
   (
      NO SEQ DEC NEWLINE
   )
;

ip_prefix_list_tail
:
   (
      SEQ? seqnum = DEC
   )? action = access_list_action prefix = IP_PREFIX
   (
      (
         GE minpl = DEC
      )
      |
      (
         LE maxpl = DEC
      )
      |
      (
         EQ eqpl = DEC
      )
   )* NEWLINE
;

ipv6_prefix_list_tail
:
   (
      SEQ? seqnum = DEC
   )? action = access_list_action prefix6 = IPV6_PREFIX
   (
      (
         GE minpl = DEC
      )
      |
      (
         LE maxpl = DEC
      )
      |
      (
         EQ eqpl = DEC
      )
   )* NEWLINE
;

ipx_sap_access_list_null_tail
:
   action = access_list_action ~NEWLINE* NEWLINE
;

ipx_sap_access_list_stanza
:
   ACCESS_LIST name = ACL_NUM_IPX_SAP ipx_sap_access_list_null_tail
;

irs_stanza
:
   bandwidth_irs_stanza
   | null_irs_stanza
;

mac_access_list_additional_feature
:
   (
      ETYPE etype
   )
   | HEX
   | IP
   | LOG_ENABLE
   |
   (
      PRIORITY priority = DEC
   )
   |
   (
      PRIORITY_FORCE priority_force = DEC
   )
   |
   (
      PRIORITY_MAPPING priority_mapping = DEC
   )
;

no_ip_prefix_list_stanza
:
   NO IP PREFIX_LIST name = variable NEWLINE
;

null_as_path_regex
:
   ~NEWLINE*
;

null_irs_stanza
:
   NO?
   (
      SIGNALLING
   ) ~NEWLINE* NEWLINE
;

null_rs_stanza
:
   NO?
   (
      AUTHENTICATION
      | KEY_SOURCE
      | LOGGING
      | WINDOW_SIZE
   ) ~NEWLINE* NEWLINE
;

prefix_set_stanza
:
   PREFIX_SET name = variable NEWLINE prefix_set_elem_list END_SET NEWLINE
;

prefix_set_elem_list
:
// no elements

   |
   (
      (
         hash_comment
         |
         (
            prefix_set_elem COMMA
         )
      ) NEWLINE
   )*
   (
      hash_comment
      | prefix_set_elem
   ) NEWLINE
;

protocol_type_code_access_list_null_tail
:
   action = access_list_action ~NEWLINE* NEWLINE
;

protocol_type_code_access_list_stanza
:
   ACCESS_LIST name = ACL_NUM_PROTOCOL_TYPE_CODE
   protocol_type_code_access_list_null_tail
;

rs_stanza
:
   interface_rs_stanza
   | null_rs_stanza
;

rsvp_stanza
:
   RSVP NEWLINE rs_stanza*
;

s_ethernet_services
:
   ETHERNET_SERVICES ACCESS_LIST name = variable_permissive NEWLINE
   s_ethernet_services_tail*
;

s_ethernet_services_tail
:
   num = DEC? action = access_list_action src_mac = xr_mac_specifier dst_mac =
   xr_mac_specifier NEWLINE
;

s_foundry_mac_access_list
:
   ACCESS_LIST num = ACL_NUM_FOUNDRY_L2 action = access_list_action
   (
      (
         src_address = MAC_ADDRESS_LITERAL src_wildcard = MAC_ADDRESS_LITERAL
      )
      | src_any = ANY
   )
   (
      (
         dst_address = MAC_ADDRESS_LITERAL dst_wildcard = MAC_ADDRESS_LITERAL
      )
      | dst_any = ANY
   )
   (
      vlan = DEC
      | vlan_any = ANY
   )
   (
      (
         ETYPE etype
      )
      | LOG_ENABLE
      |
      (
         PRIORITY priority = DEC
      )
      |
      (
         PRIORITY_FORCE priority_force = DEC
      )
      |
      (
         PRIORITY_MAPPING priority_mapping = DEC
      )
   )* NEWLINE
;

s_mac_access_list
:
   ACCESS_LIST num = ACL_NUM_MAC action = access_list_action address =
   MAC_ADDRESS_LITERAL wildcard = MAC_ADDRESS_LITERAL NEWLINE
;

s_mac_access_list_extended
:
   (
      ACCESS_LIST num = ACL_NUM_EXTENDED_MAC s_mac_access_list_extended_tail
   )
   |
   (
      MAC ACCESS_LIST EXTENDED? name = variable_permissive EXTENDED? NEWLINE
      s_mac_access_list_extended_tail*
   )
;

s_mac_access_list_extended_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = DEC
   )? action = access_list_action src = access_list_mac_range dst =
   access_list_mac_range
   (
      vlan = DEC
      | vlan_any = ANY
   )? mac_access_list_additional_feature* NEWLINE
;

standard_access_list_additional_feature
:
   (
      DSCP dscp_type
   )
   |
   (
      ECN ecn = DEC
   )
   | LOG
;

standard_access_list_null_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = DEC
   )?
   (
      REMARK
      | STATISTICS
   ) ~NEWLINE* NEWLINE
;

standard_access_list_stanza
:
   (
      (
         IP ACCESS_LIST STANDARD name = variable_permissive
      )
      |
      (
         ACCESS_LIST num = ACL_NUM_STANDARD
      )
      |
      (
         ACCESS_LIST name = variable_permissive STANDARD
      )
   )
   (
      (
         NEWLINE
         (
            standard_access_list_tail
            | standard_access_list_null_tail
         )*
      )
      |
      (
         (
            standard_access_list_tail
            | standard_access_list_null_tail
         )
      )
   )
;

standard_ipv6_access_list_stanza
:
   IPV6 ACCESS_LIST STANDARD name = variable
   (
      (
         NEWLINE
         (
            standard_ipv6_access_list_tail
            | standard_access_list_null_tail
         )*
      )
      |
      (
         (
            standard_ipv6_access_list_tail
            | standard_access_list_null_tail
         )
      )
   )
;

standard_access_list_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = DEC
   )? ala = access_list_action ipr = access_list_ip_range
   (
      features += standard_access_list_additional_feature
   )* NEWLINE
;

standard_ipv6_access_list_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = DEC
   )? ala = access_list_action ipr = access_list_ip6_range
   (
      features += standard_access_list_additional_feature
   )* NEWLINE
;

xr_mac_specifier
:
   ANY
   |
   (
      HOST host = MAC_ADDRESS_LITERAL
   )
   |
   (
      address = MAC_ADDRESS_LITERAL mask = MAC_ADDRESS_LITERAL
   )
;
