parser grammar F5BigipStructured_security;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

// Security DOS configuration
sec_dos_device_config
:
  DOS DEVICE_CONFIG name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_dos_device_vector
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_device_vector
:
  DOS_DEVICE_VECTOR BRACE_LEFT
  (
    NEWLINE
    (
      sec_dos_vector_type
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_vector_type
:
  WORD_ID BRACE_LEFT
  (
    NEWLINE
    (
      sec_dos_vector_allow_advertisement
      | sec_dos_vector_blacklist_category
      | sec_dos_vector_state_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_vector_allow_advertisement
:
  WORD_ID (ENABLED | DISABLED) NEWLINE
;

sec_dos_vector_blacklist_category
:
  WORD_ID structure_name NEWLINE
;

sec_dos_vector_state_null
:
  STATE word_id NEWLINE
;

sec_dos_ip_uncommon_protolist
:
  DOS IP_UNCOMMON_PROTOLIST name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_dos_description_null
      | sec_dos_entries_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_description_null
:
  DESCRIPTION description_text NEWLINE
;

sec_dos_entries_null
:
  ENTRIES BRACE_LEFT BRACE_RIGHT NEWLINE
  | ENTRIES BRACE_LEFT uint+ BRACE_RIGHT NEWLINE
;

sec_dos_ipv6_ext_hdr
:
  DOS IPV6_EXT_HDR name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_dos_frame_types_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_frame_types_null
:
  WORD_ID BRACE_LEFT BRACE_RIGHT NEWLINE
  | WORD_ID list NEWLINE
;

sec_dos_udp_portlist
:
  DOS UDP_PORTLIST name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_dos_udp_entries
      | sec_dos_udp_list_type_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_udp_list_type_null
:
  WORD_ID word_id NEWLINE
;

sec_dos_udp_entries
:
  ENTRIES BRACE_LEFT NEWLINE sec_dos_udp_entry+ BRACE_RIGHT NEWLINE
;

sec_dos_udp_entry
:
  WORD_ID BRACE_LEFT
  (
    NEWLINE
    (
      sec_dos_udp_entry_item
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_udp_entry_item
:
  WORD_ID word_id NEWLINE
  | WORD_ID uint NEWLINE
;

// Security Firewall configuration
sec_firewall_config_change_log
:
  FIREWALL CONFIG_CHANGE_LOG BRACE_LEFT
  (
    NEWLINE
    (
      sec_firewall_log_publisher_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_firewall_log_publisher_null
:
  LOG_PUBLISHER structure_name NEWLINE
;

// Security Firewall rule-list configuration
sec_firewall_rule_list
:
  FIREWALL RULE_LIST name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_firewall_rules
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_firewall_rules
:
  RULES BRACE_LEFT
  (
    NEWLINE
    (
      sec_firewall_rule
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_firewall_rule
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_firewall_rule_item
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_firewall_rule_item
:
  ACTION word_id NEWLINE
  | IP_PROTOCOL word_id NEWLINE
;

// Other security configurations that are not yet implemented
sec_dos_other
:
  DOS unrecognized
;

sec_firewall_other
:
  FIREWALL unrecognized
;

sec_null
:
  (
    DOS
    | FIREWALL
  ) ignored
;

sec_log_profile
:
  WORD_ID PROFILE (PARTITION? word_id | DOUBLE_QUOTED_STRING) BRACE_LEFT
  (
    NEWLINE
    (
      sec_log_profile_category
      | sec_log_profile_setting_null
      | ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_log_profile_category
:
  WORD_ID DOUBLE_QUOTED_STRING BRACE_LEFT
  (
    NEWLINE
    (
      sec_log_profile_category_item
      | sec_log_profile_category_nested
      | sec_log_profile_publisher_null
      | ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
  | word_id BRACE_LEFT
  (
    NEWLINE
    (
      sec_log_profile_category_item
      | sec_log_profile_category_nested
      | sec_log_profile_publisher_null
      | ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_log_profile_setting_null
:
  word ignored
;

sec_log_profile_category_item
:
  word (word_id | uint) NEWLINE
  | word BRACE_LEFT
  (
    NEWLINE
    (
      sec_log_profile_category_item
      | ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_log_profile_category_nested
:
  DOUBLE_QUOTED_STRING BRACE_LEFT
  (
    NEWLINE
    (
      ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
  | PARTITION word_id? BRACE_LEFT
  (
    NEWLINE
    (
      ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
  | WORD_ID list NEWLINE
  | WORD_ID BRACE_LEFT
  (
    NEWLINE
    (
      ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_log_profile_publisher_null
:
  LOG_PUBLISHER structure_name NEWLINE
;

// Scrubber profile configuration
sec_scrubber_profile
:
  SCRUBBER PROFILE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_scrubber_setting
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_scrubber_setting
:
  ADVERTISEMENT_TTL ttl = uint NEWLINE
;

s_security
:
  SECURITY
  (
    sec_log_profile
    | sec_protocol_inspection
    | sec_scrubber_profile
    | sec_dos_device_config
    | sec_dos_ip_uncommon_protolist
    | sec_dos_ipv6_ext_hdr
    | sec_dos_udp_portlist
    | sec_dos_other
    | sec_firewall_config_change_log
    | sec_firewall_rule_list
    | sec_firewall_other
    | sec_null
    | unrecognized
  )
;

sec_protocol_inspection
:
  PROTOCOL_INSPECTION WORD_ID structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sec_protocol_inspection_item
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_protocol_inspection_item
:
  DESCRIPTION description_text NEWLINE
  | word ignored
;

