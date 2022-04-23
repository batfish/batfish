parser grammar Legacy_acl;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

access_list_ip_range
:
   ip = IP_ADDRESS wildcard = IP_ADDRESS
 | prefix = IP_PREFIX
 | HOST ip = IP_ADDRESS
 | ANY
;

access_list_ip6_range
:
   prefix6 = IPV6_PREFIX
 | HOST ipv6 = IPV6_ADDRESS
 | ANY
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

bandwidth_irs_stanza
:
   BANDWIDTH null_rest_of_line
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
   | ADMINISTRATIVELY_PROHIBITED
   | ALTERNATE_ADDRESS
   | BEYOND_SCOPE
   | BFD_ECHO
   | CONVERSION_ERROR
   | COUNT
   | CWR
   | DESTINATION_UNREACHABLE
   | DOD_HOST_PROHIBITED
   | DOD_NET_PROHIBITED
   |
   (
      DSCP dscp_type
   )
   |
   (
      icmp_message_type = uint8 icmp_message_code = uint8?
   )
   | ECE
   | ECHO
   | ECHO_REPLY
   | ECHO_REQUEST
   |
   (
      ECN ecn = dec
   )
   | ESTABLISHED
   | FIN
   | FRAGMENTS
   | GENERAL_PARAMETER_PROBLEM
   | HOP_LIMIT
   | HOPLIMIT
   | HOST_ISOLATED
   | HOST_PRECEDENCE_UNREACHABLE
   | HOST_REDIRECT
   | HOST_TOS_REDIRECT
   | HOST_TOS_UNREACHABLE
   | HOST_UNKNOWN
   | HOST_UNREACHABLE
   | INFORMATION_REPLY
   | INFORMATION_REQUEST
   |
   (
      LOG
      (
         DEFAULT
         | DISABLE
         | (level = dec (INTERVAL secs = dec)?)
      )?
   )
   | LOG_INPUT
   | MASK_REPLY
   | MASK_REQUEST
   | MLD_QUERY
   | MLD_REDUCTION
   | MLD_REPORT
   | MLDV2
   | MOBILE_HOST_REDIRECT
   | ND
   | ND_NA
   | ND_NS
   | ND_TYPE
   | NEIGHBOR
   | NET_REDIRECT
   | NET_TOS_REDIRECT
   | NET_TOS_UNREACHABLE
   | NET_UNREACHABLE
   | NETWORK_UNKNOWN
   | NO_ROOM_FOR_OPTION
   | OPTION_MISSING
   | PACKET_TOO_BIG
   | PARAMETER_PROBLEM
   | PORT_UNREACHABLE
   | PRECEDENCE_UNREACHABLE
   | PROTOCOL_UNREACHABLE
   | PSH
   | REASSEMBLY_TIMEOUT
   | REDIRECT
   | ROUTER
   | ROUTER_ADVERTISEMENT
   | ROUTER_SOLICITATION
   | RST
   | SOURCE_QUENCH
   | SOURCE_ROUTE_FAILED
   | SYN
   | TIME_EXCEEDED
   | TIMESTAMP_REPLY
   | TIMESTAMP_REQUEST
   | TRACEROUTE
   | TRACKED
   | TTL_EXCEEDED
   | TTL EQ uint8
   | UNREACHABLE
   | URG
;

extended_access_list_null_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = dec
   )?
   (
      (
         access_list_action protocol access_list_ip_range port_specifier?
         access_list_ip_range port_specifier? REFLECT
      )
      | COUNTERS
      | DYNAMIC
      | EVALUATE
      | FRAGMENT_RULES
      | MENU
      | NO COUNTERS
      | NO STATISTICS
      | REMARK
      | STATISTICS
   ) null_rest_of_line
;

extended_access_list_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = dec
   )? ala = access_list_action
   (
      VLAN vlan = dec vmask = HEX
   )?
   (
      prot = protocol
   ) srcipr = access_list_ip_range
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
      SEQUENCE num = dec
   )? NEWLINE
;

extended_ipv6_access_list_stanza
:
   IPV6 ACCESS_LIST name = variable_permissive
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
      )? num = dec
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
      SEQUENCE num = dec
   )? NEWLINE
;

interface_rs_stanza
:
   INTERFACE name = interface_name NEWLINE irs_stanza*
;

ip_community_list_expanded_stanza
:
// expanded on < 4.23, regexp after
   IP COMMUNITY_LIST (EXPANDED | REGEXP)
     name = WORD
     action = access_list_action
     regexp = WORD
     NEWLINE
;

ip_community_list_standard_stanza
:
// standard required on < 4.23, absent after.
   IP COMMUNITY_LIST STANDARD?
     name_cl = WORD
     action = access_list_action
     (communities += literal_standard_community)+
     NEWLINE
;

ip_prefix_list_stanza
:
   (
      IP
      | IPV4
   )? PREFIX_LIST name = variable
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
      NO SEQ dec NEWLINE
   )
;

ip_prefix_list_tail
:
   (
      SEQ? seqnum = dec
   )? action = access_list_action prefix = IP_PREFIX
   (
      (
         GE minpl = dec
      )
      |
      (
         LE maxpl = dec
      )
      |
      (
         EQ eqpl = dec
      )
   )* NEWLINE
;

ipv6_prefix_list_tail
:
   (
      SEQ? seqnum = dec
   )? action = access_list_action prefix6 = IPV6_PREFIX
   (
      (
         GE minpl = dec
      )
      |
      (
         LE maxpl = dec
      )
      |
      (
         EQ eqpl = dec
      )
   )* NEWLINE
;

ipx_sap_access_list_null_tail
:
   action = access_list_action null_rest_of_line
;

irs_stanza
:
   bandwidth_irs_stanza
   | null_irs_stanza
;

netdestination_description
:
   desc = description_line
;

netdestination_host
:
   HOST ip = IP_ADDRESS NEWLINE
;

netdestination_invert
:
   INVERT NEWLINE
;

netdestination_name
:
   NAME name = variable_permissive NEWLINE
;

netdestination_network
:
   NETWORK net = IP_ADDRESS mask = IP_ADDRESS NEWLINE
;

netdestination6_description
:
   desc = description_line
;

netdestination6_host
:
   HOST ip6 = IPV6_ADDRESS NEWLINE
;

netdestination6_invert
:
   INVERT NEWLINE
;

netdestination6_name
:
   NAME name = variable_permissive NEWLINE
;

netdestination6_network
:
   NETWORK net6 = IPV6_PREFIX NEWLINE
;

netservice_icmpv6_specifier
:
   dec
   | RTR_ADV
;

netservice_port_specifier
:
   (
      (
         start_port = dec
         (
            end_port = dec
         )?
      )
      |
      (
         LIST DOUBLE_QUOTE
         (
            elems += dec
         )+ DOUBLE_QUOTE
      )
   )
;

no_ip_prefix_list
:
   PREFIX_LIST name = variable
   (
     no_ip_prefix_list_stanza
     | no_ip_prefix_list_seq
   )
;

no_ip_prefix_list_stanza: NEWLINE;

no_ip_prefix_list_seq: SEQ seqnum = dec NEWLINE;

null_irs_stanza
:
   NO?
   (
      SIGNALLING
   ) null_rest_of_line
;

null_rs_stanza
:
   NO?
   (
      AUTHENTICATION
      | KEY_SOURCE
      | LOGGING
      | WINDOW_SIZE
   ) null_rest_of_line
;

protocol_type_code_access_list_null_tail
:
   action = access_list_action null_rest_of_line
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

s_arp_access_list_extended
:
   ARP ACCESS_LIST name = variable_permissive NEWLINE
   s_arp_access_list_extended_tail*
;

s_arp_access_list_extended_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = dec
   )? action = access_list_action
   (
      REQUEST
      | RESPONSE
   )? IP senderip = access_list_ip_range
   (
      targetip = access_list_ip_range
   )? MAC sendermac = access_list_mac_range
   (
      targetmac = access_list_mac_range
   )? LOG? NEWLINE
;

s_ethernet_services
:
   ETHERNET_SERVICES ACCESS_LIST name = variable_permissive NEWLINE
   s_ethernet_services_tail*
;

s_ethernet_services_tail
:
   num = dec? action = access_list_action src_mac = xr_mac_specifier dst_mac =
   xr_mac_specifier NEWLINE
;

s_netdestination
:
   NETDESTINATION name = variable NEWLINE
   (
      netdestination_description
      | netdestination_host
      | netdestination_invert
      | netdestination_name
      | netdestination_network
   )*
;

s_netdestination6
:
   NETDESTINATION6 name = variable NEWLINE
   (
      netdestination6_description
      | netdestination6_host
      | netdestination6_invert
      | netdestination6_name
      | netdestination6_network
   )*
;

s_netservice
:
   NETSERVICE name = variable prot = protocol ps = netservice_port_specifier?
   (
      ALG alg = netservice_alg
   )? NEWLINE
;

standard_access_list_additional_feature
:
   (
      DSCP dscp_type
   )
   |
   (
      ECN ecn = dec
   )
   | LOG
;

standard_access_list_null_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = dec
   )?
   (
      COUNTERS
      | FRAGMENT_RULES
      | NO COUNTERS
      | NO STATISTICS
      | REMARK
      | STATISTICS
   ) null_rest_of_line
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

standard_ipv6_access_list_tail
:
   (
      (
         SEQ
         | SEQUENCE
      )? num = dec
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


/// EOS VALIDATED GRAMMAR STARTS HERE
s_ip_access_list
:
  ACCESS_LIST
  (
    acl_extended
    | acl_standard
  )
;

// Represents a match on either source or dest IPv4 address.
acl_ipv4_match
:
   ANY
   | wildcard_ip = IP_ADDRESS wildcard_mask = IP_ADDRESS
   | HOST host = IP_ADDRESS
   | prefix = IP_PREFIX
;

acl_extended
:
  name = WORD NEWLINE
  (
    extended_access_list_tail // not validated
    | extended_access_list_null_tail // not validated
  )*
;

acl_standard
:
  STANDARD name = WORD NEWLINE
  aclstd_line*
;

aclstd_line
:
  aclstd_counters
  | aclstd_fragment_rules
  | aclstd_no
  // | aclstd_resequence
  | aclstd_seq
  | aclstd_statistics
;

aclstd_counters
:
  COUNTERS null_rest_of_line
;

aclstd_fragment_rules
:
  FRAGMENT_RULES NEWLINE
;

aclstd_no
:
  NO
  (
    aclstd_no_counters
    | aclstd_no_statistics
  )
;

aclstd_no_counters
:
  COUNTERS null_rest_of_line
;

aclstd_no_statistics
:
  STATISTICS null_rest_of_line
;

aclstd_seq
:
  (seq = uint32)?
  (
    aclstd_action_line
    | aclstd_remark_line
  )
;

aclstd_action_line
:
  action = access_list_action source = acl_ipv4_match LOG? NEWLINE
;

aclstd_remark_line
:
  REMARK text = RAW_TEXT NEWLINE
;

aclstd_statistics
:
  STATISTICS null_rest_of_line
;