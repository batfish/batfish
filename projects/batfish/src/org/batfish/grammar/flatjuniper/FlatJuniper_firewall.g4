parser grammar FlatJuniper_firewall;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

fwfromt_address
:
   ADDRESS
   (
      IP_PREFIX
      | IPV6_PREFIX
   )
;

fwfromt_destination_address
:
   DESTINATION_ADDRESS
   (
      IP_PREFIX
      | IPV6_PREFIX
   )
;

fwfromt_destination_port
:
   DESTINATION_PORT
   (
      port
      | range
   )
;

fwfromt_destination_port_except
:
   DESTINATION_PORT_EXCEPT
   (
      port
      | range
   )
;

fwfromt_destination_prefix_list
:
   DESTINATION_PREFIX_LIST variable EXCEPT?
;

fwfromt_dscp
:
   DSCP variable
;

fwfromt_exp
:
   EXP DEC
;

fwfromt_first_fragment
:
   FIRST_FRAGMENT
;

fwfromt_forwarding_class
:
   FORWARDING_CLASS variable
;

fwfromt_icmp_code
:
   ICMP_CODE icmp_code
;

fwfromt_icmp_type
:
   ICMP_TYPE icmp_type
;

fwfromt_ip_options
:
   IP_OPTIONS ip_option
;

fwfromt_learn_vlan_1p_priority
:
   LEARN_VLAN_1P_PRIORITY DEC
;

fwfromt_next_header
:
   NEXT_HEADER ip_protocol
;

fwfromt_null
:
   IS_FRAGMENT s_null_filler
;

fwfromt_port
:
   PORT
   (
      port
      | range
   )
;

fwfromt_precedence
:
   PRECEDENCE precedence = DEC
;

fwfromt_prefix_list
:
   PREFIX_LIST variable
;

fwfromt_protocol
:
   PROTOCOL ip_protocol
;

fwfromt_source_address
:
   SOURCE_ADDRESS
   (
      IP_PREFIX
      | IPV6_PREFIX
   )
;

fwfromt_source_port
:
   SOURCE_PORT
   (
      port
      | range
   )
;

fwfromt_source_prefix_list
:
   SOURCE_PREFIX_LIST variable
;

fwfromt_tcp_established
:
   TCP_ESTABLISHED
;

fwfromt_tcp_flags
:
   TCP_FLAGS DOUBLE_QUOTED_STRING
;

fwfromt_tcp_initial
:
   TCP_INITIAL
;

fwfromt_vlan
:
   VLAN name = variable
;

fwft_apply_groups
:
   s_apply_groups
;

fwft_interface_specific
:
   INTERFACE_SPECIFIC
;

fwft_term
:
   TERM name = variable fwft_term_tail
;

fwft_term_tail
:
   fwtt_from
   | fwtt_then
;

fwt_common
:
   fwt_filter
   | fwt_null
;

fwt_family
:
   FAMILY
   (
      ANY
      | BRIDGE
      | CCC
      | ETHERNET_SWITCHING
      | INET
      | INET6
      | MPLS
   ) fwt_family_tail
;

fwt_family_tail
:
   fwt_common
;

fwt_filter
:
   FILTER name = variable fwt_filter_tail
;

fwt_filter_tail
:
   fwft_apply_groups
   | fwft_interface_specific
   | fwft_term
;

fwt_null
:
   (
      POLICER
      | SERVICE_FILTER
   ) s_null_filler
;

fwthent_accept
:
   ACCEPT
;

fwthent_discard
:
   DISCARD
;

fwthent_loss_priority
:
   LOSS_PRIORITY
   (
      HIGH
      | MEDIUM_HIGH
      | MEDIUM_LOW
      | LOW
   )
;

fwthent_next_term
:
   NEXT TERM
;

fwthent_nop
:
   (
      COUNT
      | DSCP
      | FORWARDING_CLASS
      | LOG
      | POLICER
      | SAMPLE
      | SYSLOG
   ) s_null_filler
;

fwthent_port_mirror
:
   PORT_MIRROR
;

fwthent_reject
:
   REJECT
;

fwthent_routing_instance
:
   ROUTING_INSTANCE s_null_filler
;

fwtt_from
:
   FROM fwtt_from_tail
;

fwtt_from_tail
:
   fwfromt_address
   | fwfromt_destination_address
   | fwfromt_destination_port
   | fwfromt_destination_port_except
   | fwfromt_destination_prefix_list
   | fwfromt_dscp
   | fwfromt_exp
   | fwfromt_first_fragment
   | fwfromt_forwarding_class
   | fwfromt_icmp_code
   | fwfromt_icmp_type
   | fwfromt_ip_options
   | fwfromt_learn_vlan_1p_priority
   | fwfromt_next_header
   | fwfromt_null
   | fwfromt_port
   | fwfromt_precedence
   | fwfromt_prefix_list
   | fwfromt_protocol
   | fwfromt_source_address
   | fwfromt_source_port
   | fwfromt_source_prefix_list
   | fwfromt_tcp_established
   | fwfromt_tcp_flags
   | fwfromt_tcp_initial
   | fwfromt_vlan
;

fwtt_then
:
   THEN fwtt_then_tail
;

fwtt_then_tail
:
   fwthent_accept
   | fwthent_discard
   | fwthent_loss_priority
   | fwthent_next_term
   | fwthent_nop
   | fwthent_port_mirror
   | fwthent_reject
   | fwthent_routing_instance
;

s_firewall
:
   FIREWALL s_firewall_tail
;

s_firewall_tail
:
   fwt_common
   | fwt_family
;
