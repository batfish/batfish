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

fwfromt_destination_prefix_list
:
   DESTINATION_PREFIX_LIST variable
;

fwfromt_dscp
:
   DSCP variable
;

fwfromt_exp
:
   EXP DEC
;

fwfromt_icmp_code
:
   ICMP_CODE icmp_code
;

fwfromt_icmp_type
:
   ICMP_TYPE icmp_type
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

fwt_family
:
   FAMILY
   (
      INET
      | INET6
      | MPLS
   ) fwt_family_tail
;

fwt_family_tail
:
   fwt_filter
;

fwt_filter
:
   FILTER name = variable fwt_filter_tail
;

fwt_filter_tail
:
   fwft_interface_specific
   | fwft_term
;

fwt_null
:
   (
      POLICER
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

fwthent_next_term
:
   NEXT TERM
;

fwthent_null
:
   (
      COUNT
      | DSCP
      | FORWARDING_CLASS
      | LOG
      | POLICER
      | ROUTING_INSTANCE
      | SAMPLE
      | SYSLOG
   ) s_null_filler
;

fwthent_reject
:
   REJECT
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
   | fwfromt_destination_prefix_list
   | fwfromt_dscp
   | fwfromt_exp
   | fwfromt_icmp_code
   | fwfromt_icmp_type
   | fwfromt_next_header
   | fwfromt_null
   | fwfromt_port
   | fwfromt_prefix_list
   | fwfromt_protocol
   | fwfromt_source_address
   | fwfromt_source_port
   | fwfromt_source_prefix_list
   | fwfromt_tcp_established
   | fwfromt_tcp_flags
;

fwtt_then
:
   THEN fwtt_then_tail
;

fwtt_then_tail
:
   fwthent_accept
   | fwthent_discard
   | fwthent_reject
   | fwthent_next_term
   | fwthent_null
;

s_firewall
:
   FIREWALL s_firewall_tail
;

s_firewall_tail
:
   fwt_family
   | fwt_filter
   | fwt_null
;
