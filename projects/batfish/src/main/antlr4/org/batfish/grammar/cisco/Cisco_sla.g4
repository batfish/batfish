parser grammar Cisco_sla;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

ip_sla
:
   SLA
   (
      ip_sla_entry // number
      | ip_sla_auto
      | ip_sla_enable_null
      | ip_sla_endpoint_list
      | ip_sla_ethernet_monitor
      | ip_sla_group_null
      | ip_sla_keychain_null
      | ip_sla_logging_traps_null
      | ip_sla_low_memory_null
      | ip_sla_reaction_configuration_null // could not get CLI to accept anything
      | ip_sla_reaction_trigger_null // could not get CLI to accept anything
      | ip_sla_reset
      | ip_sla_responder_null
      | ip_sla_restart_null
      | ip_sla_schedule
      | ip_sla_server
   )
;

no_ip_sla
:
  SLA
  (
    no_ip_sla_entry
    | no_ip_sla_auto
    | ip_sla_enable_null
    | no_ip_sla_endpoint_list
    | no_ip_sla_ethernet_monitor
    | ip_sla_group_null
    | ip_sla_keychain_null
    | ip_sla_logging_traps_null
    | ip_sla_low_memory_null
    // no NO version of reset, since it is an operation
    | ip_sla_responder_null
    // no NO vesion of restart, since it is an operation
    | no_ip_sla_schedule
    | ip_sla_reaction_configuration_null // could not get CLI to accept anything
    | ip_sla_reaction_trigger_null // could not get CLI to accept anything
    | no_ip_sla_server
  )
;

no_ip_sla_entry: num = sla_number NEWLINE;

ip_sla_entry
:
  num = sla_number NEWLINE
  ip_sla_type? // mandatory on initial definition, rejected on re-entry
  ip_sla_inner*
;

ip_sla_type
:
  ip_slat_dhcp
  | ip_slat_dns_null
  | ip_slat_ethernet
  | ip_slat_ftp_null
  | ip_slat_http_null
  | ip_slat_icmp_echo
  | ip_slat_icmp_jitter
  | ip_slat_mpls
  | ip_slat_path_echo_null
  | ip_slat_path_jitter_null
  | ip_slat_tcp_connect
  | ip_slat_udp_echo
  | ip_slat_udp_jitter
  | ip_slat_voip_null
;

ip_sla_inner
:
  ip_slai_aggregate_null
  | ip_slai_clock_null
  | ip_slai_clock_tolerance_null
  | ip_slai_control_null
  | ip_slai_cos_null
  | ip_slai_data_pattern_null
  | ip_slai_distribution_null
  | ip_slai_exp_null
  | ip_slai_frame_null
  | ip_slai_frequency_null
  | ip_slai_history_null
  | ip_slai_hops_of_statistics_kept_null
  | ip_slai_lsr_path_null
  | ip_slai_max_delay_null
  | ip_slai_operation_packet_priority_null
  | ip_slai_optimize_null
  | ip_slai_owner_null
  | ip_slai_paths_of_statistics_kept_null
  | ip_slai_percentile_null
  | ip_slai_precision_null
  | ip_slai_request_data_size_null
  | ip_slai_response_data_size_null
  | ip_slai_samples_of_history_kept_null
  | ip_slai_secondary_frequency_null
  | ip_slai_tag_null
  | ip_slai_threshold_null
  | ip_slai_timeout_null
  | ip_slai_tos_null
  | ip_slai_ttl_null
  | ip_slai_verify_data_null
  | no_ip_slai_null
  | ip_slai_dest_ipaddr
  | ip_slai_dest_port
  | ip_slai_vrf
;

no_ip_slai_null: NO (
   ip_slai_aggregate_null
   | ip_slai_clock_null
   | ip_slai_clock_tolerance_null
   | ip_slai_control_null
   | ip_slai_cos_null
   | ip_slai_data_pattern_null
   | ip_slai_distribution_null
   | ip_slai_exp_null
   | ip_slai_frame_null
   | ip_slai_frequency_null
   | ip_slai_history_null
   | ip_slai_hops_of_statistics_kept_null
   | ip_slai_lsr_path_null
   | ip_slai_max_delay_null
   | ip_slai_operation_packet_priority_null
   | ip_slai_optimize_null
   | ip_slai_owner_null
   | ip_slai_paths_of_statistics_kept_null
   | ip_slai_percentile_null
   | ip_slai_precision_null
   | ip_slai_request_data_size_null
   | ip_slai_response_data_size_null
   | ip_slai_samples_of_history_kept_null
   | ip_slai_secondary_frequency_null
   | ip_slai_tag_null
   | ip_slai_threshold_null
   | ip_slai_timeout_null
   | ip_slai_tos_null
   | ip_slai_ttl_null
   | ip_slai_verify_data_null
);

ip_slai_aggregate_null
:
   AGGREGATE null_rest_of_line
;
ip_slai_clock_null
:
   CLOCK null_rest_of_line
;
ip_slai_clock_tolerance_null
:
   CLOCK_TOLERANCE null_rest_of_line
;
ip_slai_control_null
:
   CONTROL null_rest_of_line
;
ip_slai_cos_null
:
   COS null_rest_of_line
;
ip_slai_data_pattern_null
:
   DATA_PATTERN null_rest_of_line
;
ip_slai_distribution_null
:
   DISTRIBUTION null_rest_of_line
;
ip_slai_exp_null
:
   EXP null_rest_of_line
;
ip_slai_frame_null
:
   FRAME null_rest_of_line
;
ip_slai_frequency_null
:
   FREQUENCY null_rest_of_line
;
ip_slai_history_null
:
   HISTORY null_rest_of_line
;
ip_slai_hops_of_statistics_kept_null
:
   HOPS_OF_STATISTICS_KEPT null_rest_of_line
;
ip_slai_lsr_path_null
:
   LSR_PATH null_rest_of_line
;
ip_slai_max_delay_null
:
   MAX_DELAY null_rest_of_line
;
ip_slai_operation_packet_priority_null
:
   OPERATION_PACKET_PRIORITY null_rest_of_line
;
ip_slai_optimize_null
:
   OPTIMIZE null_rest_of_line
;
ip_slai_owner_null
:
   OWNER null_rest_of_line
;
ip_slai_paths_of_statistics_kept_null
:
   PATHS_OF_STATISTICS_KEPT null_rest_of_line
;
ip_slai_percentile_null
:
   PERCENTILE null_rest_of_line
;
ip_slai_precision_null
:
   PRECISION null_rest_of_line
;
ip_slai_request_data_size_null
:
   REQUEST_DATA_SIZE null_rest_of_line
;
ip_slai_response_data_size_null
:
   RESPONSE_DATA_SIZE null_rest_of_line
;
ip_slai_samples_of_history_kept_null
:
   SAMPLES_OF_HISTORY_KEPT null_rest_of_line
;
ip_slai_secondary_frequency_null
:
   SECONDARY_FREQUENCY null_rest_of_line
;
ip_slai_tag_null
:
   TAG null_rest_of_line
;
ip_slai_threshold_null
:
   THRESHOLD null_rest_of_line
;
ip_slai_timeout_null
:
   TIMEOUT null_rest_of_line
;
ip_slai_tos_null
:
   TOS null_rest_of_line
;
ip_slai_ttl_null
:
   TTL null_rest_of_line
;
ip_slai_verify_data_null
:
   VERIFY_DATA null_rest_of_line
;

ip_slai_dest_ipaddr: DEST_IPADDR (dstip = ip_address | dsthost = variable) NEWLINE;

ip_slai_dest_port: DEST_PORT dstport = port_number NEWLINE;

ip_slai_vrf: VRF name = variable NEWLINE;

// dhcp ip sla scope:
//   frequency
//   history
//   owner
//   tag
//   threshold
//   timeout

// dns ip sla scope:
//   frequency
//   history
//   owner
//   tag
//   threshold
//   timeout
//   vrf

// ethernet echo ip sla scope:
//   cos
//   frequency
//   history
//   owner
//   request-data-size
//   tag
//   threshold
//   timeout

// ethernet jitter ip sla scope:
//   cos
//   frequency
//   history
//   owner
//   percentile
//   request-data-size
//   tag
//   threshold
//   timeout

// ethernet y1731 ip sla scope:
//   aggregate
//   clock
//   distribution
//   frame
//   history
//   max-delay
//   owner

// ftp ip sla scope
//   frequency
//   history
//   owner
//   tag
//   threshold
//   timeout
//   tos
//   vrf

// http ip sla scope
//   frequency
//   history
//   http-raw-request
//   owner
//   tag
//   threshold
//   timeout
//   tos
//   vrf

// icmp-echo ip sla scope
//   data-pattern
//   frequency
//   history
//   owner
//   request-data-size
//   tag
//   threshold
//   timeout
//   tos
//   verify-data
//   vrf

// icmp-jitter ip sla scope
//   frequency
//   history
//   owner
//   percentile
//   tag
//   threshold
//   timeout
//   tos
//   vrf

// mpls ping ip sla scope:
//   exp
//   frequency
//   history
//   owner
//   request-data-size
//   secondary-frequency
//   tag
//   threshold
//   timeout
//   ttl

// mpls trace ip sla scope:
//   exp
//   frequency
//   history
//   hops-of-statistics-kept
//   owner
//   paths-of-statistics-kept
//   samples-of-history-kept
//   tag
//   threshold
//   timeout
//   ttl

// path-echo ip sla scope:
//   frequency
//   history
//   hops-of-statistics-kept
//   lsr-path
//   owner
//   paths-of-statistics-kept
//   request-data-size
//   samples-of-history-kept
//   tag
//   threshold
//   timeout
//   tos
//   verify-data
//   vrf

// path-jitter ip sla scope:
//   frequency
//   lsr-path
//   owner
//   request-data-size
//   tag
//   threshold
//   timeout
//   tos
//   verify-data
//   vrf

// tcp-connect ip sla scope:
//   dest-ipaddr
//   dest-port
//   frequency
//   history
//   owner
//   tag
//   threshold
//   timeout
//   tos
//   vrf

// udp-echo ip sla scope:
//   data-pattern
//   dest-ipaddr
//   dest-port
//   frequency
//   history
//   owner
//   request-data-size
//   tag
//   threshold
//   timeout
//   tos
//   verify-data
//   vrf

// udp-jitter ip sla scope:
//   clock-tolerance
//   control
//   dest-ipaddr
//   dest-port
//   frequency
//   history
//   operation-packet-priority
//   optimize
//   owner
//   percentile
//   precision
//   request-data-size
//   response-data-size
//   tag
//   threshold
//   timeout
//   tos
//   verify-data
//   vrf

// voip ip sla scope
//   frequency
//   history
//   owner
//   tag
//   threshold
//   timeout

ip_slat_dhcp
:
  DHCP (dstip = ip_address | dsthost = variable)
  (SOURCE_IP (srcip = ip_address | srchost = variable))?
  NEWLINE
;

ip_slat_dns_null
:
  DNS (queryip = ip_address | queryhost = variable)
  NAME_SERVER (nsip = ip_address | nshost = variable)
  (
    (SOURCE_IP (srcip = ip_address | srchost = variable)) (SOURCE_PORT port_number)?
    | SOURCE_PORT port_number (SOURCE_IP (srcip = ip_address | srchost = variable))?
  )?
  NEWLINE
;

ip_slat_ethernet
:
  ETHERNET
  (
    ipslat_ethernet_echo_null
    | ipslat_ethernet_jitter_null
    | ipslat_ethernet_y1731_null
  )
;

ipslat_ethernet_echo_null: ECHO null_rest_of_line;

ipslat_ethernet_jitter_null: JITTER null_rest_of_line;

ipslat_ethernet_y1731_null: Y1731 null_rest_of_line;

ip_slat_ftp_null: FTP null_rest_of_line;

ip_slat_http_null: HTTP null_rest_of_line;

ip_slat_icmp_echo
:
  ICMP_ECHO (dstip = ip_address | dsthost = variable)
  (
    SOURCE_INTERFACE iname = interface_name
    | SOURCE_IP (srcip = ip_address | srchost = variable)
  )?
  NEWLINE
;

ip_slat_icmp_jitter
:
  ICMP_JITTER (dstip = ip_address | dsthost = variable)
  // really only allowed once
  (
    INTERVAL interval = jitter_interval
    | NUM_PACKETS numpackets = num_packets
    | SOURCE_IP (srcip = ip_address | srchost = variable)
  )*
  NEWLINE
;

jitter_interval
:
  // 4-60000, default 20
  uint16
;

num_packets
:
  // 1-60000, default 10
  uint16
;

ip_slat_mpls
:
  MPLS LSP
  (
    ip_slat_mpls_ping_null
    | ip_slat_mpls_trace_null
  )
;

ip_slat_mpls_ping_null: PING null_rest_of_line;

ip_slat_mpls_trace_null: TRACE null_rest_of_line;

ip_slat_path_echo_null: PATH_ECHO null_rest_of_line;

ip_slat_path_jitter_null: PATH_JITTER null_rest_of_line;

ip_slat_tcp_connect
:
  TCP_CONNECT (dstip = ip_address | dsthost = variable) dstport = port_number
  // really only allowed once
  (
    CONTROL (ENABLE | DISABLE)
    | SOURCE_IP (srcip = ip_address | srchost = variable)
    | SOURCE_PORT srcport = port_number
  )*
  NEWLINE
;

ip_slat_udp_echo
:
  UDP_ECHO (dstip = ip_address | dsthost = variable) dstport = port_number
  // really only allowed once
  (
    CONTROL (ENABLE | DISABLE)
    | SOURCE_IP (srcip = ip_address | srchost = variable)
    | SOURCE_PORT srcport = port_number
  )*
  NEWLINE
;

ip_slat_udp_jitter
:
  UDP_JITTER (dstip = ip_address | dsthost = variable) dstport = port_number
  // really only allowed once
  (
    ADVANTAGE_FACTOR advantage_factor // must appear after codec
    | CODEC (G711ALAW | G711ULAW | G729A)
    | CODEC_INTERVAL jitter_interval // must appear after codec
    | CODEC_NUMPACKETS num_packets // must appear after codec
    | CONTROL (ENABLE | DISABLE)
    | SOURCE_IP (srcip = ip_address | srchost = variable)
    | SOURCE_PORT srcport = port_number
  )*
  NEWLINE
;

ip_slat_voip_null: VOIP null_rest_of_line;

advantage_factor
:
  // 0-20
  uint8
;


ip_sla_auto
:
  AUTO
  (
    ip_slaa_discovery_null
    | ip_slaa_group
    | ip_slaa_schedule
    | ip_slaa_template
  )
;

no_ip_sla_auto
:
  AUTO
  (
    ip_slaa_discovery_null
    | no_ip_slaa_group
    | no_ip_slaa_schedule
    | no_ip_slaa_template
  )
;

ip_slaa_discovery_null: DISCOVERY NEWLINE;

ip_slaa_group
:
  GROUP TYPE IP name = variable NEWLINE
  ipslaag_inner*
;

no_ip_slaa_group: GROUP TYPE IP name = variable NEWLINE;

ipslaag_inner
:
  ipslaag_description_null
  | ipslaag_destination_null
  | ipslaag_schedule_null
  | ipslaag_template_null
  | no_ipslaag_null
;

ipslaag_description_null
:
   DESCRIPTION null_rest_of_line
;
ipslaag_destination_null
:
   DESTINATION null_rest_of_line
;
ipslaag_schedule_null
:
   SCHEDULE null_rest_of_line
;
ipslaag_template_null
:
   TEMPLATE null_rest_of_line
;

no_ipslaag_null: NO (
   ipslaag_description_null
   | ipslaag_destination_null
   | ipslaag_schedule_null
   | ipslaag_template_null
);

ip_slaa_schedule
:
  SCHEDULE name = variable NEWLINE
  ip_slaas_inner*
;

no_ip_slaa_schedule: SCHEDULE name = variable NEWLINE;

ip_slaas_inner
:
  ip_slaas_ageout_null
  | ip_slaas_frequency_null
  | ip_slaas_life_null
  | ip_slaas_probe_interval_null
  | ip_slaas_start_time_null
  | no_ip_slaas_null
;

ip_slaas_ageout_null
:
   AGEOUT null_rest_of_line
;
ip_slaas_frequency_null
:
   FREQUENCY null_rest_of_line
;
ip_slaas_life_null
:
   LIFE null_rest_of_line
;
ip_slaas_probe_interval_null
:
   PROBE_INTERVAL null_rest_of_line
;
ip_slaas_start_time_null
:
   START_TIME null_rest_of_line
;

no_ip_slaas_null: NO (
   ip_slaas_ageout_null
   | ip_slaas_frequency_null
   | ip_slaas_life_null
   | ip_slaas_probe_interval_null
   | ip_slaas_start_time_null
);

ip_slaa_template
:
  TEMPLATE TYPE IP
  (
    ip_slaat_icmp_echo
    | ip_slaat_icmp_jitter
    | ip_slaat_tcp_connect
    | ip_slaat_udp_echo
    | ip_slaat_udp_jitter
  )
;

no_ip_slaa_template
:
  TEMPLATE TYPE IP
  (
    no_ip_slaat_icmp_echo
    | no_ip_slaat_icmp_jitter
    | no_ip_slaat_tcp_connect
    | no_ip_slaat_udp_echo
    | no_ip_slaat_udp_jitter
  )
;

ip_slaat_icmp_echo
:
  ICMP_ECHO name = variable NEWLINE
  ipslaati_inner*
;

no_ip_slaat_icmp_echo: ICMP_ECHO name = variable NEWLINE;

ipslaati_inner
:
  ipslaati_description_null
  | ipslaati_parameters_null
  | ipslaati_react_null
  | ipslaati_source_ip_null
  | ipslaati_tos_null
  | ipslaati_vrf_null
  | no_ipslaati_null
;

ipslaati_description_null
:
   DESCRIPTION null_rest_of_line
;
ipslaati_parameters_null
:
   PARAMETERS null_rest_of_line
;
ipslaati_react_null
:
   REACT null_rest_of_line
;
ipslaati_source_ip_null
:
   SOURCE_IP null_rest_of_line
;
ipslaati_tos_null
:
   TOS null_rest_of_line
;
ipslaati_vrf_null
:
   VRF null_rest_of_line
;

no_ipslaati_null: NO (
   ipslaati_description_null
   | ipslaati_parameters_null
   | ipslaati_react_null
   | ipslaati_source_ip_null
   | ipslaati_tos_null
   | ipslaati_vrf_null
);

ip_slaat_icmp_jitter
:
  ICMP_ECHO name = variable NEWLINE
  ipslaati_inner*
;

no_ip_slaat_icmp_jitter: ICMP_ECHO name = variable NEWLINE;

ip_slaat_tcp_connect
:
  TCP_CONNECT name = variable NEWLINE
  ip_slaatt_inner*
;

no_ip_slaat_tcp_connect: NO TCP_CONNECT name = variable NEWLINE;

ip_slaatt_inner
:
  ip_slaatt_control_null
  | ip_slaatt_description_null
  | ip_slaatt_parameters_null
  | ip_slaatt_react_null
  | ip_slaatt_source_ip_null
  | ip_slaatt_source_port_null
  | ip_slaatt_tos_null
  | ip_slaatt_vrf_null
  | no_ip_slaatt_null
;

ip_slaatt_control_null
:
   CONTROL null_rest_of_line
;
ip_slaatt_description_null
:
   DESCRIPTION null_rest_of_line
;
ip_slaatt_parameters_null
:
   PARAMETERS null_rest_of_line
;
ip_slaatt_react_null
:
   REACT null_rest_of_line
;
ip_slaatt_source_ip_null
:
   SOURCE_IP null_rest_of_line
;
ip_slaatt_source_port_null
:
   SOURCE_PORT null_rest_of_line
;
ip_slaatt_tos_null
:
   TOS null_rest_of_line
;
ip_slaatt_vrf_null
:
   VRF null_rest_of_line
;

no_ip_slaatt_null: NO (
   ip_slaatt_control_null
   | ip_slaatt_description_null
   | ip_slaatt_parameters_null
   | ip_slaatt_react_null
   | ip_slaatt_source_ip_null
   | ip_slaatt_source_port_null
   | ip_slaatt_tos_null
   | ip_slaatt_vrf_null
);

ip_slaat_udp_echo
:
  UDP_ECHO name = variable NEWLINE
  ip_slaatue_inner*
;

no_ip_slaat_udp_echo: UDP_ECHO name = variable NEWLINE;

ip_slaatue_inner
:
  ip_slaatue_control_null
  | ip_slaatue_description_null
  | ip_slaatue_parameters_null
  | ip_slaatue_react_null
  | ip_slaatue_source_ip_null
  | ip_slaatue_source_port_null
  | ip_slaatue_tos_null
  | ip_slaatue_vrf_null
  | no_ip_slaatue_null
;

ip_slaatue_control_null
:
   CONTROL null_rest_of_line
;
ip_slaatue_description_null
:
   DESCRIPTION null_rest_of_line
;
ip_slaatue_parameters_null
:
   PARAMETERS null_rest_of_line
;
ip_slaatue_react_null
:
   REACT null_rest_of_line
;
ip_slaatue_source_ip_null
:
   SOURCE_IP null_rest_of_line
;
ip_slaatue_source_port_null
:
   SOURCE_PORT null_rest_of_line
;
ip_slaatue_tos_null
:
   TOS null_rest_of_line
;
ip_slaatue_vrf_null
:
   VRF null_rest_of_line
;

no_ip_slaatue_null: NO (
   ip_slaatue_control_null
   | ip_slaatue_description_null
   | ip_slaatue_parameters_null
   | ip_slaatue_react_null
   | ip_slaatue_source_ip_null
   | ip_slaatue_source_port_null
   | ip_slaatue_tos_null
   | ip_slaatue_vrf_null
);

ip_slaat_udp_jitter
:
  UDP_JITTER name = variable NEWLINE
  ip_slaatuj_inner*
;

no_ip_slaat_udp_jitter: UDP_JITTER name = variable NEWLINE;

ip_slaatuj_inner
:
  ip_slaatuj_codec_null
  | ip_slaatuj_control_null
  | ip_slaatuj_description_null
  | ip_slaatuj_parameters_null
  | ip_slaatuj_react_null
  | ip_slaatuj_source_ip_null
  | ip_slaatuj_source_port_null
  | ip_slaatuj_tos_null
  | ip_slaatuj_vrf_null
  | no_ip_slaatuj_null
;

ip_slaatuj_codec_null
:
   CODEC null_rest_of_line
;
ip_slaatuj_control_null
:
   CONTROL null_rest_of_line
;
ip_slaatuj_description_null
:
   DESCRIPTION null_rest_of_line
;
ip_slaatuj_parameters_null
:
   PARAMETERS null_rest_of_line
;
ip_slaatuj_react_null
:
   REACT null_rest_of_line
;
ip_slaatuj_source_ip_null
:
   SOURCE_IP null_rest_of_line
;
ip_slaatuj_source_port_null
:
   SOURCE_PORT null_rest_of_line
;
ip_slaatuj_tos_null
:
   TOS null_rest_of_line
;
ip_slaatuj_vrf_null
:
   VRF null_rest_of_line
;

no_ip_slaatuj_null: NO (
   ip_slaatuj_codec_null
   | ip_slaatuj_control_null
   | ip_slaatuj_description_null
   | ip_slaatuj_parameters_null
   | ip_slaatuj_react_null
   | ip_slaatuj_source_ip_null
   | ip_slaatuj_source_port_null
   | ip_slaatuj_tos_null
   | ip_slaatuj_vrf_null
);

ip_sla_enable_null: ENABLE REACTION_ALERTS NEWLINE;

ip_sla_endpoint_list
:
  ENDPOINT_LIST TYPE IP name = variable NEWLINE
  ip_slael_inner*
;

no_ip_sla_endpoint_list: NO ENDPOINT_LIST TYPE IP name = variable NEWLINE;

ip_slael_inner
:
  ip_slael_description
  | no_ip_slael_description
  | ip_slael_ip_address
  | no_ip_slael_ip_address
;

ip_slael_description: description_line;

no_ip_slael_description: NO description_line;

ip_slael_ip_address
:
  IP_DASH_ADDRESS
  (
    ip_slaeli_ip_address_list
    | ip_slaeli_ip_address_range
  ) PORT port_number NEWLINE
;

no_ip_slael_ip_address
:
  NO IP_DASH_ADDRESS
  (
    ip_slaeli_ip_address_list
    | ip_slaeli_ip_address_range
  ) PORT port_number NEWLINE
;

ip_slaeli_ip_address_list
:
  // max 5
  ips += ip_address
  (COMMA ips += ip_address)?
  (COMMA ips += ip_address)?
  (COMMA ips += ip_address)?
  (COMMA ips += ip_address)?
;

ip_slaeli_ip_address_range: IP_ADDRESS_RANGE;

ip_sla_ethernet_monitor
:
  ETHERNET_MONITOR
  (
    ip_slaem_entry
    | ip_slaem_reaction_configuration_null
    | ip_slaem_schedule_null
  )
;

no_ip_sla_ethernet_monitor
:
  ETHERNET_MONITOR
  (
    ip_slaem_reaction_configuration_null
    | ip_slaem_schedule_null
    | no_ip_slaem_entry
  )
;

ip_slaem_entry
:
  num = sla_number NEWLINE
  ip_slaeme_inner*
;

no_ip_slaem_entry: num = sla_number NEWLINE;

ip_slaeme_inner
:
  ip_slaeme_cos_null
  | ip_slaeme_owner_null
  | ip_slaeme_request_data_size_null
  | ip_slaeme_tag_null
  | ip_slaeme_threshold_null
  | ip_slaeme_timeout_null
  | ip_slaeme_type_null
  | no_ip_slaeme_null
;

ip_slaeme_type_null
:
   TYPE null_rest_of_line
;
ip_slaeme_cos_null
:
   COS null_rest_of_line
;
ip_slaeme_owner_null
:
   OWNER null_rest_of_line
;
ip_slaeme_request_data_size_null
:
   REQUEST_DATA_SIZE null_rest_of_line
;
ip_slaeme_tag_null
:
   TAG null_rest_of_line
;
ip_slaeme_threshold_null
:
   THRESHOLD null_rest_of_line
;
ip_slaeme_timeout_null
:
   TIMEOUT null_rest_of_line
;

no_ip_slaeme_null: NO (
   ip_slaeme_cos_null
   | ip_slaeme_owner_null
   | ip_slaeme_request_data_size_null
   | ip_slaeme_tag_null
   | ip_slaeme_threshold_null
   | ip_slaeme_timeout_null
   | ip_slaeme_type_null
);

ip_slaem_reaction_configuration_null
:
   REACTION_CONFIGURATION null_rest_of_line
;
ip_slaem_schedule_null
:
   SCHEDULE null_rest_of_line
;

ip_sla_group_null: GROUP null_rest_of_line;

ip_sla_keychain_null: KEY_CHAIN null_rest_of_line;

ip_sla_logging_traps_null: LOGGING TRAPS NEWLINE;

ip_sla_low_memory_null: LOW_MEMORY null_rest_of_line;

ip_sla_reaction_configuration_null: REACTION_CONFIGURATION null_rest_of_line;

ip_sla_reaction_trigger_null: REACTION_TRIGGER null_rest_of_line;

ip_sla_reset: RESET NEWLINE; // results in prompt, confirm deletes all ip sla entries

ip_sla_responder_null: RESPONDER null_rest_of_line;

ip_sla_restart_null: RESTART null_rest_of_line; // operation, does not appear in config

ip_sla_schedule
:
  SCHEDULE num = sla_number
  (
    ageout = sla_schedule_ageout
    | life = sla_schedule_life
    | recurring = sla_schedule_recurring
    | starttime = sla_schedule_start_time
  )* NEWLINE
;

sla_schedule_ageout: AGEOUT ageout_seconds;

ageout_seconds
:
  // 0-2073600
  uint32
;

sla_schedule_life
:
  LIFE
  (
    FOREVER
    | secs = life_seconds
  )
;

life_seconds
:
  // 0-2147483647
  uint32
;

sla_schedule_recurring: RECURRING;

sla_schedule_start_time
:
  START_TIME
  (
    AFTER after = HH_MM_SS
    | HH_MM
    | HH_MM_SS
    | NOW
    | PENDING // never starts
    | RANDOM sla_schedule_random_ms
  )
;

sla_schedule_random_ms
:
  // 500-10000
  uint16
;

// Contents after number don't seem to matter. Just deletes the schedule, deactivating the sla.
no_ip_sla_schedule: SCHEDULE num = sla_number null_rest_of_line;

ip_sla_server
:
  SERVER TWAMP NEWLINE
  ip_slas_inner*
;

no_ip_sla_server: NO SERVER TWAMP NEWLINE;

ip_slas_inner
:
  ip_slas_port_null
  | ip_slas_timer_null
  | no_ip_slas_null
;

ip_slas_port_null
:
   PORT null_rest_of_line
;
ip_slas_timer_null
:
   TIMER null_rest_of_line
;

no_ip_slas_null: NO (
   ip_slas_port_null
   | ip_slas_timer_null
);
