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
   |
   (
      HOST?
      (
         ip = IP_ADDRESS
         | ipv6 = IPV6_ADDRESS
      )
   )
   | prefix = IP_PREFIX
   | ipv6_prefix = IPV6_PREFIX
   |
   (
      ADDRGROUP address_group = variable
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

as_path_regex
:
   CARAT?
   (
      ranges += as_path_regex_range ASTERISK?
   )* DOLLAR?
;

as_path_regex_range
:
   DEC
   | PERIOD
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
   | // no elements

   |
   (
      community_set_elem COMMA NEWLINE
   )* community_set_elem NEWLINE
;

community_set_elem
:
   COMMUNITY_SET_VALUE
   | ACCEPT_OWN
   | DFA_REGEX COMMUNITY_SET_REGEX
   | INTERNET
   | IOS_REGEX COMMUNITY_SET_REGEX
   | LOCAL_AS
   | NO_ADVERTISE
   | NO_EXPORT
   | PRIVATE_AS
;

extended_access_list_additional_feature
:
   ACK
   | BEYOND_SCOPE
   | COUNT
   |
   (
      DSCP dscp_type
   )
   |
   (
      icmpv6_message_type = DEC icmpv6_message_code = DEC?
   )
   | ECHO
   | ECHO_REPLY
   | ECHO_REQUEST
   |
   (
      ECN ecn = DEC
   )
   | ESTABLISHED
   | FRAGMENTS
   | HOP_LIMIT
   | HOST_UNKNOWN
   | HOST_UNREACHABLE
   | LOG
   | LOG_INPUT
   | MLD_QUERY
   | MLD_REDUCTION
   | MLD_REPORT
   | ND
   | ND_NA
   | ND_NS
   | NEIGHBOR
   | NETWORK_UNKNOWN
   | NET_UNREACHABLE
   | PACKET_TOO_BIG
   | PARAMETER_PROBLEM
   | PORT_UNREACHABLE
   | REDIRECT
   | ROUTER
   | ROUTER_ADVERTISEMENT
   | ROUTER_SOLICITATION
   | RST
   | SOURCE_QUENCH
   | TIME_EXCEEDED
   | TRACEROUTE
   | TRACKED
   | TTL_EXCEEDED
   | TTL EQ DEC
   | UNREACHABLE
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
      | REMARK
      | STATISTICS
   ) ~NEWLINE* NEWLINE
;

extended_access_list_stanza
:
   (
      (
         IP ACCESS_LIST EXTENDED name = variable_with_colon
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
            | IPV6
         ) ACCESS_LIST name = variable_with_colon
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
      NEXTHOP1
      (
         (
            nh4 = IPV4 nexthop1 = IP_ADDRESS
         )
         |
         (
            nh6 = IPV6 nexthop1 = IPV6_ADDRESS
         )
      )
      (
         NEXTHOP2
         (
            (
               nh4 = IPV4 nexthop2 = IP_ADDRESS
            )
            |
            (
               nh6 = IPV6 nexthop2 = IPV6_ADDRESS
            )
         )
      )?
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
   )?
   action = access_list_action
   (
      as_path_regex
      | null_as_path_regex
   ) ANY? NEWLINE
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
   )?
   ala = access_list_action
   (
      remainder += ~NEWLINE
   )+ NEWLINE
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
      | IPV6
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
      SEQ seqnum = DEC
   )? action = access_list_action
   (
      prefix = IP_PREFIX
      | ipv6_prefix = IPV6_PREFIX
   )
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
;

no_ip_prefix_list_stanza
:
   NO IP PREFIX_LIST name = variable NEWLINE
;

null_as_path_regex
:
   ~NEWLINE*
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
   | // no elements

   |
   (
      prefix_set_elem COMMA NEWLINE
   )* prefix_set_elem NEWLINE
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

s_mac_access_list
:
   ACCESS_LIST num = ACL_NUM_MAC action = access_list_action address =
   MAC_ADDRESS_LITERAL wildcard = MAC_ADDRESS_LITERAL NEWLINE
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
         (
            IP
            | IPV6
         ) ACCESS_LIST STANDARD name = variable
      )
      |
      (
         ACCESS_LIST num = ACL_NUM_STANDARD
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

