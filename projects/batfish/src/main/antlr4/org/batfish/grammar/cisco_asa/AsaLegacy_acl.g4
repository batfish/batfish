parser grammar AsaLegacy_acl;

import Asa_common;

options {
   tokenVocab = AsaLexer;
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

aruba_access_list_action
:
   action = access_list_action
   | CAPTIVE
   |
   (
      DST_NAT dstnat = dec
   )
   | SRC_NAT
;

aruba_app
:
   BITTORRENT
   | BITTORRENT_APPLICATION
;

aruba_appcategory
:
   PEER_TO_PEER
;

asa_access_group
:
   ACCESS_GROUP
   (
      asa_ag_interface
      | asa_ag_global
   )
   NEWLINE
;

asa_ag_interface
:
   name = variable
   (
      IN
      | OUT
   )
   INTERFACE iface = variable
;

asa_ag_global
:
   name = variable GLOBAL
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
      icmp_message_type = dec icmp_message_code = dec?
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
   | TTL EQ dec
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
      | DYNAMIC
      | EVALUATE
      | MENU
      | REMARK
      | STATISTICS
   ) null_rest_of_line
;

extended_access_list_stanza
:
   (
      (
         IP ACCESS_LIST EXTENDED name = variable_aclname
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
         ) ACCESS_LIST
         (
            shortname = variable
            | name = variable_aclname
         )
      )
      |
      (
         ACCESS_LIST name = variable_aclname EXTENDED
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
      )? num = dec
   )? ala = access_list_action
   (
      VLAN vlan = dec vmask = HEX
   )?
   (
      prot = protocol
      |
      (
         OBJECT_GROUP ogs = variable
      )
      |
      (
         OBJECT
         (
            inline_obj = protocol
            | inline_obj_icmp = icmp_inline_object_type
            | obj = variable
         )
      )
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
      SEQ dec
   )? action = access_list_action as_path_regex = RAW_TEXT NEWLINE
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

ipacleth_line
:
   action = access_list_action ipacleth_range NEWLINE
;

ipacleth_range
:
   ANY
;

ipaclsession_ip_range
:
   (
      ALIAS alias = variable
   )
   | ANY
   |
   (
      HOST hostip = IP_ADDRESS
   )
   |
   (
      NETWORK net = IP_ADDRESS mask = IP_ADDRESS
   )
   | USER
;

ipaclsession_ip6_range
:
   (
      ALIAS alias = variable
   )
   | ANY
   |
   (
      HOST hostip = IPV6_ADDRESS
   )
   |
   (
      NETWORK net = IPV6_PREFIX
   )
   | USER
;

ipaclsession_line
:
   src = ipaclsession_ip_range dst = ipaclsession_ip_range svc =
   ipaclsession_service action = aruba_access_list_action
   (
      (
         DOT1P_PRIORITY d1ppri = dec
      )
      | LOG
      |
      (
         QUEUE
         (
            HIGH
         )
      )
      |
      (
         TOS tos = dec
      )
   )* NEWLINE
;

ipaclsession_line6
:
   IPV6 src = ipaclsession_ip6_range dst = ipaclsession_ip6_range svc =
   ipaclsession_service6 action = aruba_access_list_action
   (
      LOG
      |
      (
         QUEUE
         (
            HIGH
         )
      )
   )* NEWLINE
;

ipaclsession_service
:
   (
      APP app = aruba_app
   )
   |
   (
      APPCATEGORY appcat = aruba_appcategory
   )
   |
   (
      prot = protocol ps = netservice_port_specifier?
   )
   | netsvc = variable
;

ipaclsession_service6
:
   (
      (
         (
            TCP
            | UDP
         ) ps = netservice_port_specifier?
      )
      |
      (
         ICMPV6 is = netservice_icmpv6_specifier?
      )
      |
      (
         prot = protocol
      )
   )
   | netsvc = variable
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
      PRIORITY priority = dec
   )
   |
   (
      PRIORITY_FORCE priority_force = dec
   )
   |
   (
      PRIORITY_MAPPING priority_mapping = dec
   )
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

no_ip_prefix_list_stanza
:
   NO IP PREFIX_LIST name = variable NEWLINE
;

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

s_ip_access_list_eth
:
   IP ACCESS_LIST ETH name = variable NEWLINE
   (
      ipacleth_line
   )*
;

s_ip_access_list_session
:
   IP ACCESS_LIST SESSION name = variable NEWLINE
   (
      ipaclsession_line
      | ipaclsession_line6
   )*
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
      (SEQ | SEQUENCE)? num = dec
   )?
   action = access_list_action src = access_list_mac_range dst = access_list_mac_range
   (
      vlan = dec
      | vlan_any = ANY
   )? mac_access_list_additional_feature* NEWLINE
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

remark_access_list_stanza: ACCESS_LIST name = variable_aclname REMARK null_rest_of_line;

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
      REMARK
      | STATISTICS
   ) null_rest_of_line
;

standard_access_list_stanza
:
   (
      (
         IP ACCESS_LIST STANDARD name = variable_aclname
      )
      |
      (
         ACCESS_LIST num = ACL_NUM_STANDARD
      )
      |
      (
         ACCESS_LIST name = variable_aclname STANDARD
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
      )? num = dec
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
