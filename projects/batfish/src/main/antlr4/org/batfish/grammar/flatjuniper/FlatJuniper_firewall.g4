parser grammar FlatJuniper_firewall;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

f_common
:
   f_filter
   | f_null
;

f_family
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
   ) f_common
;

f_filter
:
   FILTER name = variable
   (
      apply
      | ff_interface_specific
      | ff_term
   )
;

f_null
:
   (
      POLICER
      | SERVICE_FILTER
   ) null_filler
;

ff_interface_specific
:
   INTERFACE_SPECIFIC
;

ff_term
:
   TERM name = junos_name
   (
      fft_from
      | fft_then
   )
;

fft_from
:
   FROM
   (
      fftf_address
      | fftf_destination_address
      | fftf_destination_port
      | fftf_destination_port_except
      | fftf_destination_prefix_list
      | fftf_dscp
      | fftf_exp
      | fftf_extension_header
      | fftf_first_fragment
      | fftf_forwarding_class
      | fftf_fragment_offset
      | fftf_icmp_code
      | fftf_icmp_type
      | fftf_ip_options
      | fftf_ip_protocol
      | fftf_is_fragment
      | fftf_learn_vlan_1p_priority
      | fftf_next_header
      | fftf_null
      | fftf_packet_length
      | fftf_port
      | fftf_precedence
      | fftf_prefix_list
      | fftf_protocol
      | fftf_source_address
      | fftf_source_mac_address
      | fftf_source_port
      | fftf_source_prefix_list
      | fftf_tcp_established
      | fftf_tcp_flags
      | fftf_tcp_initial
      | fftf_vlan
   )
;

fft_then
:
   THEN
   (
      fftt_accept
      | fftt_discard
      | fftt_loss_priority
      | fftt_next_ip
      | fftt_next_term
      | fftt_nop
      | fftt_port_mirror
      | fftt_reject
      | fftt_routing_instance
   )
;

fftfa_address_mask_prefix
:
    ip_address = IP_ADDRESS (FORWARD_SLASH wildcard_mask = IP_ADDRESS)?
    | IP_PREFIX

;

fftf_address
:
   ADDRESS
   (
      IP_PREFIX
      | IPV6_PREFIX
   )
;

fftf_destination_address
:
   (DESTINATION_ADDRESS | IP_DESTINATION_ADDRESS)
   (
      fftfa_address_mask_prefix
      | IPV6_ADDRESS
      | IPV6_PREFIX
   ) EXCEPT?
;

fftf_destination_port
:
   DESTINATION_PORT
   (
      port
      | range
   )
;

fftf_destination_port_except
:
   DESTINATION_PORT_EXCEPT
   (
      port
      | range
   )
;

fftf_destination_prefix_list
:
   DESTINATION_PREFIX_LIST name = variable EXCEPT?
;

fftf_dscp
:
   DSCP variable
;

fftf_exp
:
   EXP dec
;

fftf_extension_header
:
   EXTENSION_HEADER FRAGMENT
;

fftf_first_fragment
:
   FIRST_FRAGMENT
;

fftf_forwarding_class
:
   FORWARDING_CLASS variable
;

fftf_fragment_offset
:
   FRAGMENT_OFFSET subrange
;

fftf_fragment_offset_except
:
   FRAGMENT_OFFSET_EXCEPT subrange
;

fftf_icmp_code
:
   ICMP_CODE
   (
      icmp_code
      | subrange
   )
;

fftf_icmp_type
:
   ICMP_TYPE
   (
      icmp_type
      | icmp6_only_type
      | subrange
   )
;

fftf_ip_options
:
  IP_OPTIONS option = ip_option
;

fftf_ip_protocol
:
   IP_PROTOCOL ip_protocol
;

fftf_is_fragment
:
   IS_FRAGMENT
;

fftf_learn_vlan_1p_priority
:
   LEARN_VLAN_1P_PRIORITY dec
;

fftf_next_header
:
   NEXT_HEADER ip_protocol
;

fftf_null
:
   (
      ETHER_TYPE
      | PAYLOAD_PROTOCOL
   ) null_filler
;

fftf_packet_length
:
   PACKET_LENGTH range
;

fftf_packet_length_except
:
   PACKET_LENGTH_EXCEPT range
;

fftf_port
:
   PORT
   (
      port
      | range
   )
;

fftf_precedence
:
   PRECEDENCE precedence = dec
;

fftf_prefix_list
:
   PREFIX_LIST name = variable
;

fftf_protocol
:
   PROTOCOL ip_protocol
;

fftf_source_address
:
   (SOURCE_ADDRESS | IP_SOURCE_ADDRESS)
   (
      fftfa_address_mask_prefix
      | IPV6_ADDRESS
      | IPV6_PREFIX
   ) EXCEPT?
;

fftf_source_mac_address
:
   SOURCE_MAC_ADDRESS address = MAC_ADDRESS FORWARD_SLASH length = dec
;

fftf_source_port
:
   SOURCE_PORT
   (
      port
      | range
   )
;

fftf_source_prefix_list
:
   SOURCE_PREFIX_LIST name = variable EXCEPT?
;

fftf_tcp_established
:
   TCP_ESTABLISHED
;

fftf_tcp_flags
:
   TCP_FLAGS tcp_flags
;

fftf_tcp_initial
:
   TCP_INITIAL
;

fftf_vlan
:
   VLAN name = variable
;

fftt_accept
:
   ACCEPT
;

fftt_discard
:
   DISCARD
;

fftt_loss_priority
:
   LOSS_PRIORITY
   (
      HIGH
      | MEDIUM_HIGH
      | MEDIUM_LOW
      | LOW
   )
;

fftt_next_ip
:
   NEXT_IP
   (
      ip = IP_ADDRESS
      | prefix = IP_PREFIX
   )
;

fftt_next_term
:
   NEXT TERM
;

fftt_nop
:
   (
      COUNT
      | DSCP
      | FORWARDING_CLASS
      | LOG
      | NEXT_IP6
      | POLICER
      | SAMPLE
      | SYSLOG
   ) null_filler
;

fftt_port_mirror
:
   PORT_MIRROR
;

fftt_reject
:
   REJECT
;

fftt_routing_instance
:
   ROUTING_INSTANCE name = variable
;

s_firewall
:
   FIREWALL
   (
      f_common
      | f_family
   )
;

tcp_flags
:
   alternatives += tcp_flags_alternative
   (
      PIPE alternatives += tcp_flags_alternative
   )*
;

tcp_flags_alternative
:
   (
      OPEN_PAREN literals += tcp_flags_literal
      (
         AMPERSAND literals += tcp_flags_literal
      )* CLOSE_PAREN
   )
   |
   (
      literals += tcp_flags_literal
      (
         AMPERSAND literals += tcp_flags_literal
      )*
   )
;

tcp_flags_atom
:
   ACK
   | CWR
   | ECE
   | FIN
   | PSH
   | RST
   | SYN
   | URG
;

tcp_flags_literal
:
   BANG? tcp_flags_atom
;
