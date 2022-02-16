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
  // The first inner line sets the type, and decides what other inner lines are in scope.
  // On re-entry, the type line cannot be re-entered or modified (with limited exceptions) and the
  // scope is fixed. Since re-entry cannot be detected in the parser without semantic predicates
  // that tend to break operation, we just allow all scopes here. The extractor is responsible for
  // rejecting out-of-scope lines and type redefinition attempts. Note that it is impossible to
  // detect scope, since the first alternative that matches will dominate. The individual scopes are
  // commented for reference.
  ip_slai_null
  | no_ip_slai_null
  | ip_slai_dest_ipaddr
  | ip_slai_dest_port
  | ip_slai_vrf
;

no_ip_slai_null: NO ip_slai_null;

ip_slai_null
:
  (
    AGGREGATE
    | CLOCK
    | CLOCK_TOLERANCE
    | CONTROL
    | COS
    | DATA_PATTERN
    | DISTRIBUTION
    | EXP
    | FRAME
    | FREQUENCY
    | HISTORY
    | HOPS_OF_STATISTICS_KEPT
    | LSR_PATH
    | MAX_DELAY
    | OPERATION_PACKET_PRIORITY
    | OPTIMIZE
    | OWNER
    | PATHS_OF_STATISTICS_KEPT
    | PERCENTILE
    | PRECISION
    | REQUEST_DATA_SIZE
    | RESPONSE_DATA_SIZE
    | SAMPLES_OF_HISTORY_KEPT
    | SECONDARY_FREQUENCY
    | TAG
    | THRESHOLD
    | TIMEOUT
    | TOS
    | TTL
    | VERIFY_DATA
  ) null_rest_of_line
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
  ipslaag_null
  | no_ipslaag_null
;

ipslaag_null
:
  (
    DESCRIPTION
    | DESTINATION
    | SCHEDULE
    | TEMPLATE
  ) null_rest_of_line
;

no_ipslaag_null: NO ipslaag_null;

ip_slaa_schedule
:
  SCHEDULE name = variable NEWLINE
  ip_slaas_inner*
;

no_ip_slaa_schedule: SCHEDULE name = variable NEWLINE;

ip_slaas_inner
:
  ip_slaas_null
  | no_ip_slaas_null
;

ip_slaas_null
:
  (
    AGEOUT
    | FREQUENCY
    | LIFE
    | PROBE_INTERVAL
    | START_TIME
  ) null_rest_of_line
;

no_ip_slaas_null: NO ip_slaas_null;

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
  ipslaati_null
  | no_ipslaati_null
;

ipslaati_null
:
  (
    DESCRIPTION
    | PARAMETERS
    | REACT
    | SOURCE_IP
    | TOS
    | VRF
  ) null_rest_of_line
;

no_ipslaati_null: NO ipslaati_null;

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
  ip_slaatt_null
  | no_ip_slaatt_null
;

ip_slaatt_null
:
  (
    CONTROL
    | DESCRIPTION
    | PARAMETERS
    | REACT
    | SOURCE_IP
    | SOURCE_PORT
    | TOS
    | VRF
  ) null_rest_of_line
;

no_ip_slaatt_null: NO ip_slaatt_null;

ip_slaat_udp_echo
:
  UDP_ECHO name = variable NEWLINE
  ip_slaatue_inner*
;

no_ip_slaat_udp_echo: UDP_ECHO name = variable NEWLINE;

ip_slaatue_inner
:
  ip_slaatue_null
  | no_ip_slaatue_null
;

ip_slaatue_null
:
  (
    CONTROL
    | DESCRIPTION
    | PARAMETERS
    | REACT
    | SOURCE_IP
    | SOURCE_PORT
    | TOS
    | VRF
  ) null_rest_of_line
;

no_ip_slaatue_null: NO ip_slaatue_null;

ip_slaat_udp_jitter
:
  UDP_JITTER name = variable NEWLINE
  ip_slaatuj_inner*
;

no_ip_slaat_udp_jitter: UDP_JITTER name = variable NEWLINE;

ip_slaatuj_inner
:
  ip_slaatuj_null
  | no_ip_slaatuj_null
;

ip_slaatuj_null
:
  (
    CODEC
    | CONTROL
    | DESCRIPTION
    | PARAMETERS
    | REACT
    | SOURCE_IP
    | SOURCE_PORT
    | TOS
    | VRF
  ) null_rest_of_line
;

no_ip_slaatuj_null: NO ip_slaatuj_null;

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
    | ip_slaem_null
  )
;

no_ip_sla_ethernet_monitor
:
  ETHERNET_MONITOR
  (
    no_ip_slaem_entry
    | ip_slaem_null
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
  ip_slaeme_null
  | no_ip_slaeme_null
;

ip_slaeme_null
:
  // Here you specify a type if the entry is new, then are taken to another context.
  // If the entry already exists and the type has been specified, you are brought directly to the
  // new context. Since we currently do not care about the contents, just allow lines from all
  // contexts.
  (
    // for new entry
    TYPE
    // below are for existing entries or after type has been specified
    | COS
    | OWNER
    | REQUEST_DATA_SIZE
    | TAG
    | THRESHOLD
    | TIMEOUT
  ) null_rest_of_line
;

no_ip_slaeme_null: NO ip_slaeme_null;

ip_slaem_null
:
  (
    REACTION_CONFIGURATION
    | SCHEDULE
  ) null_rest_of_line
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
  ip_slas_null
  | no_ip_slas_null
;

ip_slas_null
:
  (
    PORT
    | TIMER
  ) null_rest_of_line
;

no_ip_slas_null: NO ip_slas_null;
