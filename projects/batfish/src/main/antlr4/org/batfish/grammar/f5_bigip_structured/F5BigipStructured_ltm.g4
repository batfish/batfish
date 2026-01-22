parser grammar F5BigipStructured_ltm;

import
  F5BigipStructured_common,
  F5BigipStructured_ltm_data_group,
  F5BigipStructured_ltm_rule;

options {
  tokenVocab = F5BigipStructuredLexer;
}

l_monitor
:
  MONITOR
  (
    lm_dns
    | lm_gateway_icmp
    | lm_http
    | lm_https
    | lm_ldap
    | lm_tcp
    | unrecognized
  )
;

lm_dns
:
  DNS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lmd_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lmd_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lm_gateway_icmp
:
  GATEWAY_ICMP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lmg_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lmg_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lm_http
:
  HTTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lmh_defaults_from
      | lmh_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lmh_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lmh_null
:
  (
    ADAPTIVE
    | DESTINATION
    | INTERVAL
    | IP_DSCP
    | RECV
    | RECV_DISABLE
    | SEND
    | TIME_UNTIL_UP
    | TIMEOUT
  ) ignored
;

lm_https
:
  HTTPS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lmhs_defaults_from
      | lmhs_null
      | lmhs_ssl_profile
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lmhs_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lmhs_null
:
  (
    ADAPTIVE
    | CIPHERLIST
    | COMPATIBILITY
    | DESTINATION
    | INTERVAL
    | IP_DSCP
    | RECV
    | RECV_DISABLE
    | SEND
    | TIME_UNTIL_UP
    | TIMEOUT
  ) ignored
;

lmhs_ssl_profile
:
  SSL_PROFILE name = structure_name NEWLINE
;

lm_ldap
:
  LDAP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lml_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lml_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lm_tcp
:
  TCP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lmt_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lmt_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

l_node
:
  NODE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      ln_address
      | ln_address6
    )*
  )? BRACE_RIGHT NEWLINE
;

ln_address
:
  ADDRESS address = ip_address NEWLINE
;

ln_address6
:
  ADDRESS address = ipv6_address NEWLINE
;

l_null
:
  (
    DEFAULT_NODE_MONITOR
    | GLOBAL_SETTINGS
    | IFILE
  ) ignored
;

l_persistence
:
  PERSISTENCE
  (
    lper_cookie
    | lper_source_addr
    | lper_ssl
    | unrecognized
  )
;

lper_null
:
  (
    APP_SERVICE
  ) ignored
;

lper_cookie
:
  COOKIE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lper_cookie_defaults_from
      | lper_cookie_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lper_cookie_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lper_cookie_null
:
  (
    APP_SERVICE
    | EXPIRATION
  ) ignored
;

lper_source_addr
:
  SOURCE_ADDR name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lpersa_defaults_from
      | lpersa_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lpersa_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lpersa_null
:
  (
    APP_SERVICE
    | TIMEOUT
  ) ignored
;

lper_ssl
:
  SSL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lperss_defaults_from
      | lperss_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lperss_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lperss_null
:
  (
    APP_SERVICE
    | MATCH_ACROSS_POOLS
    | MATCH_ACROSS_SERVICES
    | MATCH_ACROSS_VIRTUALS
    | OVERRIDE_CONNECTION_LIMIT
    | TIMEOUT
  ) ignored
;

l_pool
:
  POOL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lp_description
      | lp_members
      | lp_monitor
      | lp_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lp_description
:
  DESCRIPTION text = word NEWLINE
;

lp_members
:
  MEMBERS BRACE_LEFT
  (
    NEWLINE lpm_member*
  )? BRACE_RIGHT NEWLINE
;

lpm_member
:
  name_with_port = structure_name_with_port BRACE_LEFT
  (
    NEWLINE
    (
      lpmm_address
      | lpmm_address6
      | lpmm_description
      | lpmm_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lpmm_address
:
  ADDRESS address = ip_address NEWLINE
;

lpmm_address6
:
  ADDRESS address6 = ipv6_address NEWLINE
;

lp_monitor
:
  MONITOR names += structure_name
  (
    AND names += structure_name
  )* NEWLINE
;

lpmm_description
:
  DESCRIPTION text = word NEWLINE
;

lpmm_null
:
  (
    PRIORITY_GROUP
  ) ignored
;

lp_null
:
  (
    LOAD_BALANCING_MODE
    | MIN_ACTIVE_MEMBERS
    | SERVICE_DOWN_ACTION
    | SLOW_RAMP_TIME
  ) ignored
;

l_profile
:
  PROFILE
  (
    lprof_analytics
    | lprof_certificate_authority
    | lprof_classification
    | lprof_client_ldap
    | lprof_client_ssl
    | lprof_dhcpv4
    | lprof_dhcpv6
    | lprof_diameter
    | lprof_dns
    | lprof_fasthttp
    | lprof_fastl4
    | lprof_fix
    | lprof_ftp
    | lprof_gtp
    | lprof_html
    | lprof_http2
    | lprof_http_compression
    | lprof_http
    | lprof_http_proxy_connect
    | lprof_icap
    | lprof_ilx
    | lprof_ipother
    | lprof_ipsecalg
    | lprof_map_t
    | lprof_mqtt
    | lprof_netflow
    | lprof_ocsp_stapling_params
    | lprof_one_connect
    | lprof_pcp
    | lprof_pptp
    | lprof_qoe
    | lprof_radius
    | lprof_request_adapt
    | lprof_request_log
    | lprof_response_adapt
    | lprof_rewrite
    | lprof_rtsp
    | lprof_sctp
    | lprof_server_ldap
    | lprof_server_ssl
    | lprof_sip
    | lprof_smtps
    | lprof_socks
    | lprof_splitsessionclient
    | lprof_splitsessionserver
    | lprof_statistics
    | lprof_stream
    | lprof_tcp_analytics
    | lprof_tcp
    | lprof_tftp
    | lprof_traffic_acceleration
    | lprof_udp
    | lprof_web_acceleration
    | lprof_web_security
    | lprof_websocket
    | lprof_xml
    | unrecognized
  )
;

lprof_analytics
:
  ANALYTICS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_analytics_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_analytics_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_certificate_authority
:
  CERTIFICATE_AUTHORITY name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_certificate_authority_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_certificate_authority_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_classification
:
  CLASSIFICATION name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_classification_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_classification_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_client_ldap
:
  CLIENT_LDAP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_client_ldap_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_client_ldap_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_client_ssl
:
  CLIENT_SSL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_client_ssl_defaults_from
      | lprof_client_ssl_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_client_ssl_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_client_ssl_null
:
  (
    ALERT_TIMEOUT
    | ALLOW_DYNAMIC_RECORD_SIZING
    | ALLOW_NON_SSL
    | APP_SERVICE
    | CACHE_SIZE
    | CACHE_TIMEOUT
    | CERT
    | CERT_EXTENSION_INCLUDES
    | CERT_KEY_CHAIN
    | CERT_LIFESPAN
    | CERT_LOOKUP_BY_IPADDR_PORT
    | CHAIN
    | CIPHER_GROUP
    | CIPHERS
    | GENERIC_ALERT
    | HANDSHAKE_TIMEOUT
    | INHERIT_CERTKEYCHAIN
    | KEY
    | MAX_ACTIVE_HANDSHAKES
    | MAX_AGGREGATE_RENEGOTIATION_PER_MINUTE
    | MAX_RENEGOTIATIONS_PER_MINUTE
    | MAXIMUM_RECORD_SIZE
    | MOD_SSL_METHODS
    | MODE
    | OCSP_STAPLING
    | OPTIONS
    | PASSPHRASE
    | PEER_NO_RENEGOTIATE_TIMEOUT
    | PROXY_CA_CERT
    | PROXY_CA_KEY
    | PROXY_SSL
    | PROXY_SSL_PASSTHROUGH
    | RENEGOTIATE_MAX_RECORD_DELAY
    | RENEGOTIATE_PERIOD
    | RENEGOTIATE_SIZE
    | RENEGOTIATION
    | SECURE_RENEGOTIATION
    | SERVER_NAME
    | SESSION_MIRRORING
    | SESSION_TICKET
    | SESSION_TICKET_TIMEOUT
    | SNI_DEFAULT
    | SNI_REQUIRE
    | SSL_FORWARD_PROXY
    | SSL_FORWARD_PROXY_BYPASS
    | SSL_SIGN_HASH
    | STRICT_RESUME
    | UNCLEAN_SHUTDOWN
  ) ignored
;

lprof_dhcpv4
:
  DHCPV4 name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_dhcpv4_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_dhcpv4_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_dhcpv6
:
  DHCPV6 name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_dhcpv6_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_dhcpv6_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_diameter
:
  DIAMETER name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_diameter_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_diameter_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_dns
:
  DNS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_dns_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_dns_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_fasthttp
:
  FASTHTTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_fasthttp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_fasthttp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_fastl4
:
  FASTL4 name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_fastl4_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_fastl4_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_fix
:
  FIX name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_fix_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_fix_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_ftp
:
  FTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_ftp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_ftp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_gtp
:
  GTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_gtp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_gtp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_html
:
  HTML name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_html_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_html_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_http2
:
  HTTP2 name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_http2_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_http2_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_http_compression
:
  HTTP_COMPRESSION name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_http_compression_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_http_compression_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_http
:
  HTTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_http_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_http_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_http_proxy_connect
:
  HTTP_PROXY_CONNECT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_http_proxy_connect_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_http_proxy_connect_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_icap
:
  ICAP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_icap_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_icap_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_ilx
:
  ILX name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_ilx_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_ilx_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_ipother
:
  IPOTHER name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_ipother_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_ipother_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_ipsecalg
:
  IPSECALG name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_ipsecalg_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_ipsecalg_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_map_t
:
  MAP_T name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_map_t_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_map_t_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_mqtt
:
  MQTT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_mqtt_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_mqtt_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_netflow
:
  NETFLOW name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_netflow_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_netflow_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_ocsp_stapling_params
:
  OCSP_STAPLING_PARAMS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_ocsp_stapling_params_defaults_from
      | lprof_ocsp_stapling_params_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_ocsp_stapling_params_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_ocsp_stapling_params_null
:
  (
    DNS_RESOLVER
    | RESPONDER_URL
    | SIGN_HASH
    | STATUS_AGE
    | TRUSTED_RESPONDERS
  ) ignored
;

lprof_one_connect
:
  ONE_CONNECT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_one_connect_defaults_from
      | lprof_one_connect_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_one_connect_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_one_connect_null
:
  (
    APP_SERVICE
    | IDLE_TIMEOUT_OVERRIDE
    | LIMIT_TYPE
    | MAX_AGE
    | MAX_REUSE
    | MAX_SIZE
    | SOURCE_MASK
  ) ignored
;

lprof_pcp
:
  PCP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_pcp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_pcp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_pptp
:
  PPTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_pptp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_pptp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_qoe
:
  QOE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_qoe_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_qoe_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_radius
:
  RADIUS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_radius_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_radius_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_request_adapt
:
  REQUEST_ADAPT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_request_adapt_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_request_adapt_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_request_log
:
  REQUEST_LOG name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_request_log_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_request_log_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_response_adapt
:
  RESPONSE_ADAPT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_response_adapt_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_response_adapt_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_rewrite
:
  REWRITE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_rewrite_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_rewrite_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_rtsp
:
  RTSP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_rtsp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_rtsp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_sctp
:
  SCTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_sctp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_sctp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_server_ldap
:
  SERVER_LDAP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_server_ldap_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_server_ldap_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_server_ssl
:
  SERVER_SSL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_server_ssl_defaults_from
      | lprof_server_ssl_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_server_ssl_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_server_ssl_null
:
  (
    ALERT_TIMEOUT
    | APP_SERVICE
    | CACHE_SIZE
    | CACHE_TIMEOUT
    | CERT
    | CHAIN
    | CIPHER_GROUP
    | CIPHERS
    | GENERIC_ALERT
    | HANDSHAKE_TIMEOUT
    | KEY
    | MAX_ACTIVE_HANDSHAKES
    | MOD_SSL_METHODS
    | MODE
    | OPTIONS
    | PROXY_SSL
    | PROXY_SSL_PASSTHROUGH
    | RENEGOTIATE_PERIOD
    | RENEGOTIATE_SIZE
    | RENEGOTIATION
    | SECURE_RENEGOTIATION
    | SERVER_NAME
    | SESSION_MIRRORING
    | SESSION_TICKET
    | SNI_DEFAULT
    | SNI_REQUIRE
    | SSL_FORWARD_PROXY
    | SSL_FORWARD_PROXY_BYPASS
    | SSL_SIGN_HASH
    | STRICT_RESUME
    | UNCLEAN_SHUTDOWN
  ) ignored
;

lprof_sip
:
  SIP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_sip_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_sip_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_smtps
:
  SMTPS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_smtps_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_smtps_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_socks
:
  SOCKS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_socks_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_socks_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_splitsessionclient
:
  SPLITSESSIONCLIENT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_splitsessionclient_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_splitsessionclient_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_splitsessionserver
:
  SPLITSESSIONSERVER name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_splitsessionserver_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_splitsessionserver_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_statistics
:
  STATISTICS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_statistics_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_statistics_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_stream
:
  STREAM name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_stream_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_stream_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_tcp_analytics
:
  TCP_ANALYTICS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_tcp_analytics_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_tcp_analytics_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_tcp
:
  TCP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_tcp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_tcp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_tftp
:
  TFTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_tftp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_tftp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_traffic_acceleration
:
  TRAFFIC_ACCELERATION name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_traffic_acceleration_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_traffic_acceleration_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_udp
:
  UDP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_udp_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_udp_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_web_acceleration
:
  WEB_ACCELERATION name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_web_acceleration_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_web_acceleration_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_web_security
:
  WEB_SECURITY name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_web_security_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_web_security_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_websocket
:
  WEBSOCKET name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_websocket_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_websocket_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_xml
:
  XML name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprof_xml_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprof_xml_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

l_snat
:
  SNAT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      ls_origins
      | ls_snatpool
      | ls_vlans
      | ls_vlans_disabled
      | ls_vlans_enabled
    )*
  )? BRACE_RIGHT NEWLINE
;

ls_origins
:
  ORIGINS BRACE_LEFT
  (
    NEWLINE
    (
      lso_origin
      | lso_origin6
    )*
  )? BRACE_RIGHT NEWLINE
;

lso_origin
:
  origin = ip_prefix BRACE_LEFT
  (
    NEWLINE unrecognized*
  )? BRACE_RIGHT NEWLINE
;

lso_origin6
:
  origin6 = ipv6_prefix BRACE_LEFT
  (
    NEWLINE unrecognized*
  )? BRACE_RIGHT NEWLINE
;

ls_snatpool
:
  SNATPOOL name = structure_name NEWLINE
;

ls_vlans
:
  VLANS BRACE_LEFT
  (
    NEWLINE lsv_vlan*
  )? BRACE_RIGHT NEWLINE
;

lsv_vlan
:
  name = structure_name NEWLINE
;

ls_vlans_disabled
:
  VLANS_DISABLED NEWLINE
;

ls_vlans_enabled
:
  VLANS_ENABLED NEWLINE
;

l_snat_translation
:
  SNAT_TRANSLATION name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lst_address
      | lst_address6
      | lst_traffic_group
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lst_address
:
  ADDRESS address = ip_address NEWLINE
;

lst_address6
:
  ADDRESS address6 = ipv6_address NEWLINE
;

lst_traffic_group
:
  TRAFFIC_GROUP name = structure_name NEWLINE
;

l_snatpool
:
  SNATPOOL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lsp_members
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lsp_members
:
  MEMBERS BRACE_LEFT
  (
    NEWLINE lspm_member*
  )? BRACE_RIGHT NEWLINE
;

lspm_member
:
  name = structure_name NEWLINE
;

l_virtual
:
  VIRTUAL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lv_description
      | lv_destination
      | lv_disabled
      | lv_enabled
      | lv_ip_forward
      | lv_ip_protocol
      | lv_mask
      | lv_mask6
      | lv_persist
      | lv_pool
      | lv_profiles
      | lv_reject
      | lv_rules
      | lv_session
      | lv_serverssl_use_sni
      | lv_source
      | lv_source6
      | lv_source_address_translation
      | lv_source_port
      | lv_translate_address
      | lv_translate_port
      | lv_vlans
      | lv_vlans_disabled
      | lv_vlans_enabled
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lv_description
:
  DESCRIPTION text = word NEWLINE
;

lv_destination
:
  DESTINATION name_with_port = structure_name_with_port NEWLINE
;

lv_disabled
:
  DISABLED NEWLINE
;

lv_enabled
:
  ENABLED NEWLINE
;

lv_ip_forward
:
  IP_FORWARD NEWLINE
;

lv_ip_protocol
:
  IP_PROTOCOL ip_protocol NEWLINE
;

lv_mask
:
  MASK (mask = ip_address | ANY) NEWLINE
;

lv_mask6
:
  MASK mask6 = ipv6_address NEWLINE
;

lv_persist
:
  PERSIST BRACE_LEFT
  (
    NEWLINE lvp_persistence*
  )? BRACE_RIGHT NEWLINE
;

lvp_persistence
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lvpp_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lvpp_null
:
  (
    DEFAULT
  ) ignored
;

lv_pool
:
  POOL name = structure_name NEWLINE
;

lv_profiles
:
  PROFILES BRACE_LEFT
  (
    NEWLINE lv_profiles_profile*
  )? BRACE_RIGHT NEWLINE
;

lv_profiles_profile
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE unrecognized*
  )? BRACE_RIGHT NEWLINE
;

lv_reject
:
  REJECT NEWLINE
;

lv_rules
:
  RULES BRACE_LEFT
  (
    NEWLINE lvr_rule*
  )? BRACE_RIGHT NEWLINE
;

lvr_rule
:
  name = structure_name NEWLINE
;

lv_session
:
  SESSION value = word NEWLINE
;

lv_source
:
  SOURCE source = ip_prefix NEWLINE
;

lv_source6
:
  SOURCE source6 = ipv6_prefix NEWLINE
;

lv_source_address_translation
:
  SOURCE_ADDRESS_TRANSLATION BRACE_LEFT
  (
    NEWLINE
    (
      lvsat_pool
      | lvsat_type
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lvsat_pool
:
  POOL name = structure_name NEWLINE
;

lvsat_type
:
  TYPE source_address_translation_type NEWLINE
;

lv_source_port
:
  SOURCE_PORT value = word NEWLINE
;

lv_translate_address
:
  TRANSLATE_ADDRESS
  (
    DISABLED
    | ENABLED
  ) NEWLINE
;

lv_translate_port
:
  TRANSLATE_PORT
  (
    DISABLED
    | ENABLED
  ) NEWLINE
;

lv_vlans
:
  VLANS BRACE_LEFT
  (
    NEWLINE lvv_vlan*
  )? BRACE_RIGHT NEWLINE
;

lvv_vlan
:
  name = structure_name NEWLINE
;

lv_vlans_disabled
:
  VLANS_DISABLED NEWLINE
;

lv_vlans_enabled
:
  VLANS_ENABLED NEWLINE
;

lv_serverssl_use_sni
:
  SERVERSSL_USE_SNI
  (
    DISABLED
    | ENABLED
  ) NEWLINE
;

l_virtual_address
:
  VIRTUAL_ADDRESS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lva_address
      | lva_address6
      | lva_arp
      | lva_icmp_echo
      | lva_mask
      | lva_mask6
      | lva_route_advertisement
      | lva_traffic_group
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lva_address
:
  ADDRESS address = ip_address NEWLINE
;

lva_address6
:
  ADDRESS address = ipv6_address NEWLINE
;

lva_arp
:
  ARP
  (
    DISABLED
    | ENABLED
  ) NEWLINE
;

lva_icmp_echo
:
  ICMP_ECHO
  (
    DISABLED
    | ENABLED
  ) NEWLINE
;

lva_mask
:
  MASK mask = ip_address NEWLINE
;

lva_mask6
:
  MASK mask6 = ipv6_address NEWLINE
;

lva_route_advertisement
:
  ROUTE_ADVERTISEMENT ramode = route_advertisement_mode NEWLINE
;

lva_traffic_group
:
  TRAFFIC_GROUP name = structure_name NEWLINE
;

s_ltm
:
  LTM
  (
    l_data_group
    | l_monitor
    | l_node
    | l_null
    | l_persistence
    | l_pool
    | l_profile
    | l_rule
    | l_snat
    | l_snat_translation
    | l_snatpool
    | l_virtual
    | l_virtual_address
    | unrecognized
  )
;

ip_protocol
:
  TCP
  | UDP
;

route_advertisement_mode
:
  ALL
  | ALWAYS
  | ANY
  | DISABLED
  | ENABLED
  | SELECTIVE
;

source_address_translation_type
:
  SNAT
;
