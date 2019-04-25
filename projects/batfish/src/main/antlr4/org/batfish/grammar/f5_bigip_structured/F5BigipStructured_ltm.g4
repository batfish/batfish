parser grammar F5BigipStructured_ltm;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

l_monitor
:
  MONITOR
  (
    lm_http
    | lm_https
    | unrecognized
  )
;

lm_http
:
  HTTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lmh_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lmh_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lm_https
:
  HTTPS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lmhs_defaults_from
      | lmhs_ssl_profile
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lmhs_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lmhs_ssl_profile
:
  SSL_PROFILE name = structure_name NEWLINE
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

l_persistence
:
  PERSISTENCE
  (
    lper_source_addr
    | lper_ssl
    | unrecognized
  )
;

lper_source_addr
:
  SOURCE_ADDR name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lpersa_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lpersa_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lper_ssl
:
  SSL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lperss_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lperss_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
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
  MONITOR name = structure_name NEWLINE
;

lpmm_description
:
  DESCRIPTION text = word NEWLINE
;

l_profile
:
  PROFILE
  (
    lprof_client_ssl
    | lprof_http
    | lprof_ocsp_stapling_params
    | lprof_one_connect
    | lprof_server_ssl
    | lprof_tcp
    | unrecognized
  )
;

lprof_client_ssl
:
  CLIENT_SSL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprofcs_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprofcs_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_http
:
  HTTP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprofh_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprofh_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_ocsp_stapling_params
:
  OCSP_STAPLING_PARAMS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprofoc_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprofoc_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_one_connect
:
  ONE_CONNECT name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprofon_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprofon_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_server_ssl
:
  SERVER_SSL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lprofss_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lprofss_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

lprof_tcp
:
  TCP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      lproft_defaults_from
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

lproft_defaults_from
:
  DEFAULTS_FROM name = structure_name NEWLINE
;

l_rule
:
  RULE name = structure_name BRACE_LEFT
  (
    NEWLINE unrecognized*
  )? BRACE_RIGHT NEWLINE
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
      | lv_source
      | lv_source6
      | lv_source_address_translation
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
  MASK mask = ip_address NEWLINE
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
    NEWLINE unrecognized*
  )? BRACE_RIGHT NEWLINE
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
    l_monitor
    | l_node
    | l_persistence
    | l_pool
    | l_profile
    | l_rule
    | l_snat
    | l_snat_translation
    | l_snatpool
    | l_virtual
    | l_virtual_address
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
