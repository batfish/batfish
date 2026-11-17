parser grammar CiscoParser;

import
Cisco_common,
Cisco_aaa,
Cisco_acl,
Cisco_bgp,
Cisco_cable,
Cisco_crypto,
Cisco_callhome,
Cisco_device_tracking,
Cisco_eigrp,
Cisco_ignored,
Cisco_interface,
Cisco_isis,
Cisco_line,
Cisco_logging,
Cisco_mpls,
Cisco_nat,
Cisco_ntp,
Cisco_ospf,
Cisco_pim,
Cisco_qos,
Cisco_rip,
Cisco_routemap,
Cisco_sla,
Cisco_snmp,
Cisco_static,
Cisco_track,
Cisco_vlan,
Cisco_vxlan,
Cisco_zone;


options {
   superClass = 'org.batfish.grammar.cisco.parsing.CiscoBaseParser';
   tokenVocab = CiscoLexer;
}

address_aiimgp_stanza
:
   ADDRESS null_rest_of_line
;

address_family_multicast_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   ) NEWLINE address_family_multicast_tail
;

address_family_multicast_tail
:
   (
      (
         MULTIPATH NEWLINE
      )
      |
      (
         INTERFACE ALL ENABLE NEWLINE
      )
      | null_af_multicast_tail
      | interface_multicast_stanza
      | ip_pim_tail
   )*
;

ags_description_null: NO? DESCRIPTION null_rest_of_line;
ags_id_null: NO? ID null_rest_of_line;

aiimgp_stanza
:
   address_aiimgp_stanza
;

al_hidekeys_null: NO? HIDEKEYS null_rest_of_line;
al_logging_null: NO? LOGGING null_rest_of_line;
al_notify_null: NO? NOTIFY null_rest_of_line;

allow_iimgp_stanza
:
   ALLOW null_rest_of_line aiimgp_stanza*
;

allowed_ip
:
   (
      (
         hostname = IP_ADDRESS mask = IP_ADDRESS
      )
      | hostname = IPV6_ADDRESS
   ) iname = variable NEWLINE
;

ap_ap_blacklist_time_null: NO? AP_BLACKLIST_TIME null_rest_of_line;
ap_enet_link_profile_null: NO? ENET_LINK_PROFILE null_rest_of_line;
ap_flush_r1_on_new_r0_null: NO? FLUSH_R1_ON_NEW_R0 null_rest_of_line;
ap_general_profile_null: NO? GENERAL_PROFILE null_rest_of_line;
ap_group_null: NO? GROUP null_rest_of_line;
ap_lldp_null: NO? LLDP null_rest_of_line;
ap_mesh_cluster_profile_null: NO? MESH_CLUSTER_PROFILE null_rest_of_line;
ap_mesh_ht_ssid_profile_null: NO? MESH_HT_SSID_PROFILE null_rest_of_line;
ap_mesh_radio_profile_null: NO? MESH_RADIO_PROFILE null_rest_of_line;
ap_provisioning_profile_null: NO? PROVISIONING_PROFILE null_rest_of_line;
ap_spectrum_null: NO? SPECTRUM null_rest_of_line;
ap_wired_ap_profile_null: NO? WIRED_AP_PROFILE null_rest_of_line;
ap_wired_port_profile_null: NO? WIRED_PORT_PROFILE null_rest_of_line;

ap_regulatory_domain_profile
:
   REGULATORY_DOMAIN_PROFILE null_rest_of_line
   (
      aprdp_country_code_null
      | aprdp_valid_11a_40mhz_channel_pair_null
      | aprdp_valid_11a_80mhz_channel_group_null
      | aprdp_valid_11a_channel_null
      | aprdp_valid_11g_40mhz_channel_pair_null
      | aprdp_valid_11g_channel_null
   )*
;

ap_system_profile
:
   SYSTEM_PROFILE null_rest_of_line
   (
      apsp_bkup_lms_ip_null
      | apsp_dns_domain_null
      | apsp_lms_ip_null
      | apsp_lms_preemption_null
   )*
;

apg_ap_system_profile_null: NO? AP_SYSTEM_PROFILE null_rest_of_line;
apg_dot11a_radio_profile_null: NO? DOT11A_RADIO_PROFILE null_rest_of_line;
apg_dot11g_radio_profile_null: NO? DOT11G_RADIO_PROFILE null_rest_of_line;
apg_ids_profile_null: NO? IDS_PROFILE null_rest_of_line;
apg_virtual_ap_null: NO? VIRTUAL_AP null_rest_of_line;

apn_null
:
   NO?
   (
      VIRTUAL_AP
   ) null_rest_of_line
;

aprdp_country_code_null: NO? COUNTRY_CODE null_rest_of_line;
aprdp_valid_11a_40mhz_channel_pair_null: NO? VALID_11A_40MHZ_CHANNEL_PAIR null_rest_of_line;
aprdp_valid_11a_80mhz_channel_group_null: NO? VALID_11A_80MHZ_CHANNEL_GROUP null_rest_of_line;
aprdp_valid_11a_channel_null: NO? VALID_11A_CHANNEL null_rest_of_line;
aprdp_valid_11g_40mhz_channel_pair_null: NO? VALID_11G_40MHZ_CHANNEL_PAIR null_rest_of_line;
aprdp_valid_11g_channel_null: NO? VALID_11G_CHANNEL null_rest_of_line;

apsp_bkup_lms_ip_null: NO? BKUP_LMS_IP null_rest_of_line;
apsp_dns_domain_null: NO? DNS_DOMAIN null_rest_of_line;
apsp_lms_ip_null: NO? LMS_IP null_rest_of_line;
apsp_lms_preemption_null: NO? LMS_PREEMPTION null_rest_of_line;

archive_log
:
   LOG null_rest_of_line
   (
      al_hidekeys_null
      | al_logging_null
      | al_notify_null
   )*
;

archive_maximum_null: NO? MAXIMUM null_rest_of_line;
archive_path_null: NO? PATH null_rest_of_line;
archive_write_memory_null: NO? WRITE_MEMORY null_rest_of_line;

av_capture_null: NO? CAPTURE null_rest_of_line;
av_interface_null: NO? INTERFACE null_rest_of_line;
av_mode_null: NO? MODE null_rest_of_line;
av_shutdown_null: NO? SHUTDOWN null_rest_of_line;
av_timesource_null: NO? TIMESOURCE null_rest_of_line;

bfd_null
:
   NO?
   (
      TRAP
   ) null_rest_of_line
;

bfd_template_echo_null: NO? ECHO null_rest_of_line;
bfd_template_interval_null: NO? INTERVAL null_rest_of_line;

cisco_configuration
:
   NEWLINE?
   (sl += stanza)+
   COLON? NEWLINE?
   EOF
;

configure_maintenance
:
   MAINTENANCE null_rest_of_line
   (
      configure_maintenance_null
      | configure_maintenance_router
   )*
;

configure_maintenance_null
:
   NO?
   (
      IP
   ) null_rest_of_line
;

configure_maintenance_router
:
   NO?
   (
      ROUTER
   ) null_rest_of_line
   (
      configure_maintenance_router_null
   )*
;

configure_maintenance_router_null
:
   NO?
   (
      ISOLATE
   ) null_rest_of_line
;

configure_session_null: NO? SESSION null_rest_of_line;
configure_terminal_null: NO? TERMINAL null_rest_of_line;

cops_listener
:
   LISTENER
   (
      copsl_access_list
   )
;

copsl_access_list
:
   ACCESS_LIST name = variable_permissive NEWLINE
;

cp_ip_access_group
:
   (
      IP
      | IPV6
   ) ACCESS_GROUP name = variable
   (
      VRF vrf = variable
   )?
   (
      IN
      | OUT
   ) NEWLINE
;

cp_ip_flow
:
   IP FLOW MONITOR name = variable
   (
      INPUT
      | OUTPUT
   ) NEWLINE
;

cp_management_plane
:
   MANAGEMENT_PLANE NEWLINE mgp_stanza*
;

cp_exit_null: NO? EXIT null_rest_of_line;
cp_scale_factor_null: NO? SCALE_FACTOR null_rest_of_line;

cp_service_policy
:
   SERVICE_POLICY
   (
      INPUT
      | OUTPUT
   ) name = variable NEWLINE
;

cps_auto_cert_allow_all_null: NO? AUTO_CERT_ALLOW_ALL null_rest_of_line;
cps_auto_cert_allowed_addrs_null: NO? AUTO_CERT_ALLOWED_ADDRS null_rest_of_line;
cps_auto_cert_prov_null: NO? AUTO_CERT_PROV null_rest_of_line;

cqg_precedence_null: NO? PRECEDENCE null_rest_of_line;
cqg_queue_null: NO? QUEUE null_rest_of_line;
cqg_random_detect_label_null: NO? RANDOM_DETECT_LABEL null_rest_of_line;

cmf_alias_null: NO? ALIAS null_rest_of_line;
cmf_call_forward_null: NO? CALL_FORWARD null_rest_of_line;
cmf_default_destination_null: NO? DEFAULT_DESTINATION null_rest_of_line;
cmf_dialplan_pattern_null: NO? DIALPLAN_PATTERN null_rest_of_line;
cmf_ip_null: NO? IP null_rest_of_line;
cmf_keepalive_null: NO? KEEPALIVE null_rest_of_line;
cmf_limit_dn_null: NO? LIMIT_DN null_rest_of_line;
cmf_max_conferences_null: NO? MAX_CONFERENCES null_rest_of_line;
cmf_max_dn_null: NO? MAX_DN null_rest_of_line;
cmf_max_ephones_null: NO? MAX_EPHONES null_rest_of_line;
cmf_secondary_dialtone_null: NO? SECONDARY_DIALTONE null_rest_of_line;
cmf_time_format_null: NO? TIME_FORMAT null_rest_of_line;
cmf_time_zone_null: NO? TIME_ZONE null_rest_of_line;
cmf_transfer_system_null: NO? TRANSFER_SYSTEM null_rest_of_line;
cmf_translation_profile_null: NO? TRANSLATION_PROFILE null_rest_of_line;

ctlf_record_entry_null: NO? RECORD_ENTRY null_rest_of_line;
ctlf_shutdown_null: NO? SHUTDOWN null_rest_of_line;

d11_accounting_null: NO? ACCOUNTING null_rest_of_line;
d11_authentication_null: NO? AUTHENTICATION null_rest_of_line;
d11_guest_mode_null: NO? GUEST_MODE null_rest_of_line;
d11_max_associations_null: NO? MAX_ASSOCIATIONS null_rest_of_line;
d11_mbssid_null: NO? MBSSID null_rest_of_line;
d11_vlan_null: NO? VLAN null_rest_of_line;

daemon_exec_null: NO? EXEC null_rest_of_line;
daemon_shutdown_null: NO? SHUTDOWN null_rest_of_line;

dapr_action_null: NO? ACTION null_rest_of_line;
dapr_user_message_null: NO? USER_MESSAGE null_rest_of_line;

dapr_webvpn
:
   WEBVPN NEWLINE
   (
      daprw_always_on_vpn_null
      | daprw_svc_null
      | daprw_url_list_null
   )*
;

daprw_always_on_vpn_null: NO? ALWAYS_ON_VPN null_rest_of_line;
daprw_svc_null: NO? SVC null_rest_of_line;
daprw_url_list_null: NO? URL_LIST null_rest_of_line;

del_stanza
:
   DEL null_rest_of_line
;

dhcp_null
:
   NO?
   (
      INTERFACE
   ) null_rest_of_line
;

dhcp_profile
:
   NO? PROFILE null_rest_of_line
   (
      dhcp_profile_default_router_null
      | dhcp_profile_dns_server_null
      | dhcp_profile_domain_name_null
      | dhcp_profile_helper_address_null
      | dhcp_profile_lease_null
      | dhcp_profile_pool_null
      | dhcp_profile_subnet_mask_null
   )*
;

dhcp_profile_default_router_null: NO? DEFAULT_ROUTER null_rest_of_line;
dhcp_profile_domain_name_null: NO? DOMAIN_NAME null_rest_of_line;
dhcp_profile_dns_server_null: NO? DNS_SERVER null_rest_of_line;
dhcp_profile_helper_address_null: NO? HELPER_ADDRESS null_rest_of_line;
dhcp_profile_lease_null: NO? LEASE null_rest_of_line;
dhcp_profile_pool_null: NO? POOL null_rest_of_line;
dhcp_profile_subnet_mask_null: NO? SUBNET_MASK null_rest_of_line;

dialer_group
:
   GROUP null_rest_of_line
   (
      dialer_group_dial_string_null
      | dialer_group_init_string_null
   )*
;

dialer_group_dial_string_null: NO? DIAL_STRING null_rest_of_line;
dialer_group_init_string_null: NO? INIT_STRING null_rest_of_line;

dialer_null
:
   NO?
   (
      WATCH_LIST
   ) null_rest_of_line
;

domain_lookup
:
   LOOKUP
   (
      SOURCE_INTERFACE iname = interface_name
      | DISABLE
   ) NEWLINE
;

domain_name
:
   NAME hostname = variable_hostname NEWLINE
;

domain_name_server
:
   NAME_SERVER hostname = variable_hostname NEWLINE
;

dspf_associate_null: NO? ASSOCIATE null_rest_of_line;
dspf_description_null: NO? DESCRIPTION null_rest_of_line;
dspf_codec_null: NO? CODEC null_rest_of_line;
dspf_maximum_null: NO? MAXIMUM null_rest_of_line;
dspf_shutdown_null: NO? SHUTDOWN null_rest_of_line;

ednt_null
:
   NO?
   (
      CALL_FORWARD
   ) null_rest_of_line
;

eh_action_null: NO? ACTION null_rest_of_line;
eh_asynchronous_null: NO? ASYNCHRONOUS null_rest_of_line;
eh_delay_null: NO? DELAY null_rest_of_line;
eh_trigger_null: NO? TRIGGER null_rest_of_line;

enable_encrypted_password_null
:
   ENCRYPTED_PASSWORD null_rest_of_line
;
enable_read_only_password_null
:
   READ_ONLY_PASSWORD null_rest_of_line
;
enable_super_user_password_null
:
   SUPER_USER_PASSWORD null_rest_of_line
;
enable_telnet_null
:
   TELNET null_rest_of_line
;

enable_password
:
   PASSWORD (LEVEL level = dec)?
   (
      ep_plaintext
      | ep_sha512
      // Do not reorder ep_cisco_encryption
      | ep_cisco_encryption
   ) NEWLINE
;

enable_secret
:
   SECRET
   (
      (
         dec pass = variable_secret
      )
      | double_quoted_string
   ) NEWLINE
;

ep_cisco_encryption
:
   (type = dec)? (pass = variable_secret) (LEVEL level = dec)? (PBKDF2 | ENCRYPTED)?
;

ep_plaintext
:
   pass = variable
;

ep_sha512
:
   (sha512pass = SHA512_PASSWORD) (seed = PASSWORD_SEED)?
;

event_action_null: NO? ACTION null_rest_of_line;
event_event_null: NO? EVENT null_rest_of_line;
event_set_null: NO? SET null_rest_of_line;

flow_cache_null: NO? CACHE null_rest_of_line;
flow_collect_null: NO? COLLECT null_rest_of_line;
flow_description_null: NO? DESCRIPTION null_rest_of_line;
flow_destination_null: NO? DESTINATION null_rest_of_line;
flow_export_protocol_null: NO? EXPORT_PROTOCOL null_rest_of_line;
flow_exporter_null: NO? EXPORTER null_rest_of_line;
flow_match_null: NO? MATCH null_rest_of_line;
flow_option_null: NO? OPTION null_rest_of_line;
flow_record_null: NO? RECORD null_rest_of_line;
flow_source_null: NO? SOURCE null_rest_of_line;
flow_statistics_null: NO? STATISTICS null_rest_of_line;
flow_transport_null: NO? TRANSPORT null_rest_of_line;

flow_version
:
   NO? VERSION null_rest_of_line
   (
      flowv_options_null
      | flowv_template_null
   )*
;

flowv_options_null: NO? OPTIONS null_rest_of_line;
flowv_template_null: NO? TEMPLATE null_rest_of_line;

gae_null
:
   NO?
   (
      SMTP_SERVER
   ) null_rest_of_line
;

gk_gw_type_prefix_null: NO? GW_TYPE_PREFIX null_rest_of_line;
gk_lrq_null: NO? LRQ null_rest_of_line;
gk_shutdown_null: NO? SHUTDOWN null_rest_of_line;
gk_zone_null: NO? ZONE null_rest_of_line;

gpsec_age_null: NO? AGE null_rest_of_line;
gpsec_delete_dynamic_learn_null: NO? DELETE_DYNAMIC_LEARN null_rest_of_line;

hardware_ifacl_null: NO? IFACL null_rest_of_line;
hardware_qos_null: NO? QOS null_rest_of_line;
hardware_rbacl_null: NO? RBACL null_rest_of_line;
hardware_span_null: NO? SPAN null_rest_of_line;
hardware_vacl_null: NO? VACL null_rest_of_line;

ids_ap_classification_rule
:
   AP_CLASSIFICATION_RULE double_quoted_string NEWLINE
   (
      ids_ap_classification_rule_conf_level_incr_null
      | ids_ap_classification_rule_discovered_ap_cnt_null
      | ids_ap_classification_rule_snr_max_null
      | ids_ap_classification_rule_snr_min_null
      | ids_ap_classification_rule_ssid_null
   )*
;

ids_ap_classification_rule_conf_level_incr_null: NO? CONF_LEVEL_INCR null_rest_of_line;
ids_ap_classification_rule_discovered_ap_cnt_null: NO? DISCOVERED_AP_CNT null_rest_of_line;
ids_ap_classification_rule_ssid_null: NO? SSID null_rest_of_line;
ids_ap_classification_rule_snr_max_null: NO? SNR_MAX null_rest_of_line;
ids_ap_classification_rule_snr_min_null: NO? SNR_MIN null_rest_of_line;

ids_ap_rule_matching
:
   AP_RULE_MATCHING NEWLINE
   (
      ids_ap_rule_matching_null
   )*
;

ids_ap_rule_matching_null
:
   NO?
   (
      RULE_NAME
   ) null_rest_of_line
;

ids_dos_profile
:
   DOS_PROFILE double_quoted_string NEWLINE
   (
      ids_dos_profile_detect_ap_flood_null
      | ids_dos_profile_detect_chopchop_attack_null
      | ids_dos_profile_detect_client_flood_null
      | ids_dos_profile_detect_cts_rate_anomaly_null
      | ids_dos_profile_detect_eap_rate_anomaly_null
      | ids_dos_profile_detect_ht_40mhz_intolerance_null
      | ids_dos_profile_detect_invalid_address_combination_null
      | ids_dos_profile_detect_malformed_association_request_null
      | ids_dos_profile_detect_malformed_auth_frame_null
      | ids_dos_profile_detect_malformed_htie_null
      | ids_dos_profile_detect_malformed_large_duration_null
      | ids_dos_profile_detect_overflow_eapol_key_null
      | ids_dos_profile_detect_overflow_ie_null
      | ids_dos_profile_detect_rate_anomalies_null
      | ids_dos_profile_detect_rts_rate_anomaly_null
      | ids_dos_profile_detect_tkip_replay_attack_null
   )*
;

ids_dos_profile_detect_ap_flood_null: NO? DETECT_AP_FLOOD null_rest_of_line;
ids_dos_profile_detect_chopchop_attack_null: NO? DETECT_CHOPCHOP_ATTACK null_rest_of_line;
ids_dos_profile_detect_client_flood_null: NO? DETECT_CLIENT_FLOOD null_rest_of_line;
ids_dos_profile_detect_cts_rate_anomaly_null: NO? DETECT_CTS_RATE_ANOMALY null_rest_of_line;
ids_dos_profile_detect_eap_rate_anomaly_null: NO? DETECT_EAP_RATE_ANOMALY null_rest_of_line;
ids_dos_profile_detect_ht_40mhz_intolerance_null: NO? DETECT_HT_40MHZ_INTOLERANCE null_rest_of_line;
ids_dos_profile_detect_invalid_address_combination_null: NO? DETECT_INVALID_ADDRESS_COMBINATION null_rest_of_line;
ids_dos_profile_detect_malformed_association_request_null: NO? DETECT_MALFORMED_ASSOCIATION_REQUEST null_rest_of_line;
ids_dos_profile_detect_malformed_auth_frame_null: NO? DETECT_MALFORMED_AUTH_FRAME null_rest_of_line;
ids_dos_profile_detect_malformed_htie_null: NO? DETECT_MALFORMED_HTIE null_rest_of_line;
ids_dos_profile_detect_malformed_large_duration_null: NO? DETECT_MALFORMED_LARGE_DURATION null_rest_of_line;
ids_dos_profile_detect_overflow_eapol_key_null: NO? DETECT_OVERFLOW_EAPOL_KEY null_rest_of_line;
ids_dos_profile_detect_overflow_ie_null: NO? DETECT_OVERFLOW_IE null_rest_of_line;
ids_dos_profile_detect_rate_anomalies_null: NO? DETECT_RATE_ANOMALIES null_rest_of_line;
ids_dos_profile_detect_rts_rate_anomaly_null: NO? DETECT_RTS_RATE_ANOMALY null_rest_of_line;
ids_dos_profile_detect_tkip_replay_attack_null: NO? DETECT_TKIP_REPLAY_ATTACK null_rest_of_line;

ids_general_profile
:
   GENERAL_PROFILE double_quoted_string NEWLINE
   (
      ids_general_profile_wired_containment_null
      | ids_general_profile_wireless_containment_null
   )*
;

ids_general_profile_wired_containment_null: NO? WIRED_CONTAINMENT null_rest_of_line;
ids_general_profile_wireless_containment_null: NO? WIRELESS_CONTAINMENT null_rest_of_line;

ids_impersonation_profile
:
   IMPERSONATION_PROFILE double_quoted_string NEWLINE
   (
      ids_impersonation_profile_detect_ap_impersonation_null
      | ids_impersonation_profile_detect_beacon_wrong_channel_null
      | ids_impersonation_profile_detect_hotspotter_null
   )*
;

ids_impersonation_profile_detect_ap_impersonation_null: NO? DETECT_AP_IMPERSONATION null_rest_of_line;
ids_impersonation_profile_detect_beacon_wrong_channel_null: NO? DETECT_BEACON_WRONG_CHANNEL null_rest_of_line;
ids_impersonation_profile_detect_hotspotter_null: NO? DETECT_HOTSPOTTER null_rest_of_line;

ids_management_profile_null: NO? MANAGEMENT_PROFILE null_rest_of_line;
ids_rate_thresholds_profile_null: NO? RATE_THRESHOLDS_PROFILE null_rest_of_line;
ids_signature_profile_null: NO? SIGNATURE_PROFILE null_rest_of_line;
ids_wms_local_system_profile_null: NO? WMS_LOCAL_SYSTEM_PROFILE null_rest_of_line;

ids_profile
:
   PROFILE double_quoted_string NEWLINE
   (
      ids_profile_dos_profile_null
      | ids_profile_general_profile_null
      | ids_profile_impersonation_profile_null
      | ids_profile_signature_matching_profile_null
      | ids_profile_unauthorized_device_profile_null
   )*
;

ids_profile_dos_profile_null: NO? DOS_PROFILE null_rest_of_line;
ids_profile_general_profile_null: NO? GENERAL_PROFILE null_rest_of_line;
ids_profile_signature_matching_profile_null: NO? SIGNATURE_MATCHING_PROFILE null_rest_of_line;
ids_profile_impersonation_profile_null: NO? IMPERSONATION_PROFILE null_rest_of_line;
ids_profile_unauthorized_device_profile_null: NO? UNAUTHORIZED_DEVICE_PROFILE null_rest_of_line;

ids_signature_matching_profile
:
   SIGNATURE_MATCHING_PROFILE double_quoted_string NEWLINE
   (
      ids_signature_matching_profile_null
   )*
;

ids_signature_matching_profile_null
:
   NO?
   (
      SIGNATURE
   ) null_rest_of_line
;

ids_unauthorized_device_profile
:
   UNAUTHORIZED_DEVICE_PROFILE double_quoted_string NEWLINE
   (
      ids_unauthorized_device_profile_detect_adhoc_network_null
      | ids_unauthorized_device_profile_detect_bad_wep_null
      | ids_unauthorized_device_profile_detect_ht_greenfield_null
      | ids_unauthorized_device_profile_detect_invalid_mac_oui_null
      | ids_unauthorized_device_profile_detect_misconfigured_ap_null
      | ids_unauthorized_device_profile_detect_valid_ssid_misuse_null
      | ids_unauthorized_device_profile_detect_wireless_bridge_null
      | ids_unauthorized_device_profile_detect_wireless_hosted_network_null
      | ids_unauthorized_device_profile_privacy_null
      | ids_unauthorized_device_profile_protect_ssid_null
      | ids_unauthorized_device_profile_protect_valid_sta_null
      | ids_unauthorized_device_profile_require_wpa_null
      | ids_unauthorized_device_profile_suspect_rogue_conf_level_null
      | ids_unauthorized_device_profile_valid_and_protected_ssid_null
   )*
;

ids_unauthorized_device_profile_detect_adhoc_network_null: NO? DETECT_ADHOC_NETWORK null_rest_of_line;
ids_unauthorized_device_profile_detect_bad_wep_null: NO? DETECT_BAD_WEP null_rest_of_line;
ids_unauthorized_device_profile_detect_ht_greenfield_null: NO? DETECT_HT_GREENFIELD null_rest_of_line;
ids_unauthorized_device_profile_detect_invalid_mac_oui_null: NO? DETECT_INVALID_MAC_OUI null_rest_of_line;
ids_unauthorized_device_profile_detect_misconfigured_ap_null: NO? DETECT_MISCONFIGURED_AP null_rest_of_line;
ids_unauthorized_device_profile_detect_valid_ssid_misuse_null: NO? DETECT_VALID_SSID_MISUSE null_rest_of_line;
ids_unauthorized_device_profile_detect_wireless_bridge_null: NO? DETECT_WIRELESS_BRIDGE null_rest_of_line;
ids_unauthorized_device_profile_detect_wireless_hosted_network_null: NO? DETECT_WIRELESS_HOSTED_NETWORK null_rest_of_line;
ids_unauthorized_device_profile_privacy_null: NO? PRIVACY null_rest_of_line;
ids_unauthorized_device_profile_protect_ssid_null: NO? PROTECT_SSID null_rest_of_line;
ids_unauthorized_device_profile_protect_valid_sta_null: NO? PROTECT_VALID_STA null_rest_of_line;
ids_unauthorized_device_profile_require_wpa_null: NO? REQUIRE_WPA null_rest_of_line;
ids_unauthorized_device_profile_suspect_rogue_conf_level_null: NO? SUSPECT_ROGUE_CONF_LEVEL null_rest_of_line;
ids_unauthorized_device_profile_valid_and_protected_ssid_null: NO? VALID_AND_PROTECTED_SSID null_rest_of_line;

ids_wms_general_profile
:
   WMS_GENERAL_PROFILE NEWLINE
   (
      ids_wms_general_profile_null
   )*
;

ids_wms_general_profile_null
:
   NO?
   (
      COLLECT_STATS
   ) null_rest_of_line
;

ifmap_null
:
   NO?
   (
      ENABLE
   ) null_rest_of_line
;

iimgp_stanza
:
   allow_iimgp_stanza
;

imgp_stanza
:
   interface_imgp_stanza
   | null_imgp_stanza
;

inband_mgp_stanza
:
   (
      INBAND
      | OUT_OF_BAND
   ) NEWLINE imgp_stanza*
;

interface_imgp_stanza
:
   INTERFACE null_rest_of_line iimgp_stanza*
;

interface_multicast_stanza
:
   INTERFACE interface_name NEWLINE interface_multicast_tail*
;

interface_multicast_tail
:
   (
      BOUNDARY
      | BSR_BORDER
      | DISABLE
      | DR_PRIORITY
      | ENABLE
      | ROUTER
   ) null_rest_of_line
;

ip_as_path_regex_mode_stanza
:
   IP AS_PATH REGEX_MODE
   (
      ASN
      | STRING
   ) NEWLINE
;

ip_dhcp_excluded_address_null
:
   EXCLUDED_ADDRESS null_rest_of_line
;
ip_dhcp_packet_null
:
   PACKET null_rest_of_line
;
ip_dhcp_smart_relay_null
:
   SMART_RELAY null_rest_of_line
;
ip_dhcp_snooping_null
:
   SNOOPING null_rest_of_line
;
ip_dhcp_use_null
:
   USE null_rest_of_line
;

ip_dhcp_pool
:
   POOL name = variable NEWLINE
   (
      ip_dhcp_pool_authoritative_null
      | ip_dhcp_pool_bootfile_null
      | ip_dhcp_pool_client_identifier_null
      | ip_dhcp_pool_client_name_null
      | ip_dhcp_pool_default_router_null
      | ip_dhcp_pool_dns_server_null
      | ip_dhcp_pool_domain_name_null
      | ip_dhcp_pool_hardware_address_null
      | ip_dhcp_pool_host_null
      | ip_dhcp_pool_lease_null
      | ip_dhcp_pool_network_null
      | ip_dhcp_pool_next_server_null
      | ip_dhcp_pool_option_null
   )*
;

ip_dhcp_pool_authoritative_null: NO? AUTHORITATIVE null_rest_of_line;
ip_dhcp_pool_bootfile_null: NO? BOOTFILE null_rest_of_line;
ip_dhcp_pool_client_identifier_null: NO? CLIENT_IDENTIFIER null_rest_of_line;
ip_dhcp_pool_client_name_null: NO? CLIENT_NAME null_rest_of_line;
ip_dhcp_pool_default_router_null: NO? DEFAULT_ROUTER null_rest_of_line;
ip_dhcp_pool_dns_server_null: NO? DNS_SERVER null_rest_of_line;
ip_dhcp_pool_domain_name_null: NO? DOMAIN_NAME null_rest_of_line;
ip_dhcp_pool_hardware_address_null: NO? HARDWARE_ADDRESS null_rest_of_line;
ip_dhcp_pool_host_null: NO? HOST null_rest_of_line;
ip_dhcp_pool_lease_null: NO? LEASE null_rest_of_line;
ip_dhcp_pool_network_null: NO? NETWORK null_rest_of_line;
ip_dhcp_pool_next_server_null: NO? NEXT_SERVER null_rest_of_line;
ip_dhcp_pool_option_null: NO? OPTION null_rest_of_line;

ip_dhcp_relay
:
   RELAY
   (
      ip_dhcp_relay_always_on_null
      | ip_dhcp_relay_information_null
      | ip_dhcp_relay_option_null
      | ip_dhcp_relay_server
      | ip_dhcp_relay_source_address_null
      | ip_dhcp_relay_source_interface_null
      | ip_dhcp_relay_sub_option_null
      | ip_dhcp_relay_use_link_address_null
      | NEWLINE
   )
;

ip_dhcp_relay_always_on_null
:
   ALWAYS_ON null_rest_of_line
;
ip_dhcp_relay_information_null
:
   INFORMATION null_rest_of_line
;
ip_dhcp_relay_option_null
:
   OPTION null_rest_of_line
;
ip_dhcp_relay_source_address_null
:
   SOURCE_ADDRESS null_rest_of_line
;
ip_dhcp_relay_source_interface_null
:
   SOURCE_INTERFACE null_rest_of_line
;
ip_dhcp_relay_sub_option_null
:
   SUB_OPTION null_rest_of_line
;
ip_dhcp_relay_use_link_address_null
:
   USE_LINK_ADDRESS null_rest_of_line
;

ip_dhcp_relay_server
:
   SERVER
   (
      ip = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
   ) NEWLINE
;

ip_domain_lookup
:
   LOOKUP
   (VRF vrf = variable)?
   (SOURCE_INTERFACE iname = interface_name)?
   NEWLINE
;

ip_domain_name
:
   NAME
   (VRF vrf = variable)?
   hostname = variable_hostname NEWLINE
;

ip_domain_null
:
   (
      LIST
   ) null_rest_of_line
;

ip_probe_burst_size_null: NO? BURST_SIZE null_rest_of_line;
ip_probe_frequency_null: NO? FREQUENCY null_rest_of_line;
ip_probe_mode_null: NO? MODE null_rest_of_line;
ip_probe_retries_null: NO? RETRIES null_rest_of_line;

ip_route_stanza
:
   (
      IP
      | MANAGEMENT
   ) ROUTE
   (
      VRF vrf = variable
   )? ip_route_tail
;

ip_route_tail
:
   (
      (
         address = IP_ADDRESS mask = IP_ADDRESS
      )
      | prefix = IP_PREFIX
   )
   (
      nexthopip = IP_ADDRESS
      | nexthopprefix = IP_PREFIX
      | GLOBAL
      | nexthopint = interface_name_unstructured
   )*
   (
      (
         (
            ADMIN_DIST
            | ADMIN_DISTANCE
         )? distance = dec
      )
      |
      (
         METRIC metric = dec
      )
      |
      (
         TAG tag = dec
      )
      | perm = PERMANENT
      |
      (
         TRACK track = track_number
      )
      |
      (
         NAME variable
      )
   )* NEWLINE
;

ip_ssh_authentication_retries_null: AUTHENTICATION_RETRIES null_rest_of_line;
ip_ssh_client_null: CLIENT null_rest_of_line;
ip_ssh_logging_null: LOGGING null_rest_of_line;
ip_ssh_maxstartups_null: MAXSTARTUPS null_rest_of_line;
ip_ssh_port_null: PORT null_rest_of_line;
ip_ssh_rsa_null: RSA null_rest_of_line;
ip_ssh_server_null: SERVER null_rest_of_line;
ip_ssh_no_null: NO SHUTDOWN null_rest_of_line;
ip_ssh_source_interface_null: SOURCE_INTERFACE null_rest_of_line;
ip_ssh_time_out_null: TIME_OUT null_rest_of_line;

ip_ssh_pubkey_chain
:
   PUBKEY_CHAIN NEWLINE
   (
      (
         KEY_HASH
         | QUIT
         | USERNAME
      ) null_rest_of_line
   )+
;

ip_ssh_version
:
   VERSION version = dec NEWLINE
;

ipc_association
:
   ASSOCIATION null_rest_of_line
   (
      ipca_assoc_retransmit_null
      | ipca_local_ip_null
      | ipca_local_port_null
      | ipca_path_retransmit_null
      | ipca_protocol_null
      | ipca_remote_ip_null
      | ipca_remote_port_null
      | ipca_retransmit_timeout_null
      | ipca_shutdown_null
   )*
;

ipca_assoc_retransmit_null: NO? ASSOC_RETRANSMIT null_rest_of_line;
ipca_local_ip_null: NO? LOCAL_IP null_rest_of_line;
ipca_local_port_null: NO? LOCAL_PORT null_rest_of_line;
ipca_path_retransmit_null: NO? PATH_RETRANSMIT null_rest_of_line;
ipca_protocol_null: NO? PROTOCOL null_rest_of_line;
ipca_remote_ip_null: NO? REMOTE_IP null_rest_of_line;
ipca_remote_port_null: NO? REMOTE_PORT null_rest_of_line;
ipca_retransmit_timeout_null: NO? RETRANSMIT_TIMEOUT null_rest_of_line;
ipca_shutdown_null: NO? SHUTDOWN null_rest_of_line;

ipdg_address
:
   ip = IP_ADDRESS NEWLINE
;

ipdg_null
:
   (
      IMPORT
   ) null_rest_of_line
;

l2_bridge_domain_null: NO? BRIDGE_DOMAIN null_rest_of_line;
l2_mtu_null: NO? MTU null_rest_of_line;
l2_neighbor_null: NO? NEIGHBOR null_rest_of_line;
l2_vpn_null: NO? VPN null_rest_of_line;

l2tpc_authentication_null: NO? DEFAULT? AUTHENTICATION null_rest_of_line;
l2tpc_cookie_null: NO? DEFAULT? COOKIE null_rest_of_line;
l2tpc_hello_null: NO? DEFAULT? HELLO null_rest_of_line;
l2tpc_hidden_literal_null: NO? DEFAULT? HIDDEN_LITERAL null_rest_of_line;
l2tpc_hostname_null: NO? DEFAULT? HOSTNAME null_rest_of_line;
l2tpc_password_null: NO? DEFAULT? PASSWORD null_rest_of_line;
l2tpc_receive_window_null: NO? DEFAULT? RECEIVE_WINDOW null_rest_of_line;
l2tpc_retransmit_null: NO? DEFAULT? RETRANSMIT null_rest_of_line;
l2tpc_timeout_null: NO? DEFAULT? TIMEOUT null_rest_of_line;

l2vpn_bridge_group
:
   BRIDGE GROUP name = variable NEWLINE
   (
      lbg_bridge_domain
   )*
;

l2vpn_logging
:
   LOGGING NEWLINE
   (
      (
         BRIDGE_DOMAIN
         | PSEUDOWIRE
         | VFI
      ) NEWLINE
   )+
;

l2vpn_xconnect
:
   XCONNECT GROUP variable NEWLINE
   (
      l2vpn_xconnect_p2p
   )*
;

l2vpn_xconnect_p2p
:
   NO? P2P null_rest_of_line
   (
      lxp_interface_null
      | lxp_monitor_session_null
      | lxp_neighbor
   )*
;

lbg_bridge_domain
:
   BRIDGE_DOMAIN name = variable NEWLINE
   (
      lbgbd_interface_null
      | lbgbd_mac
      | lbgbd_mtu_null
      | lbgbd_neighbor_null
      | lbgbd_routed_null
      | lbgbd_vfi
   )*
;

lbgbd_mac
:
   NO? MAC null_rest_of_line
   (
      lbgbdm_limit
   )*
;

lbgbd_interface_null: NO? INTERFACE null_rest_of_line;
lbgbd_mtu_null: NO? MTU null_rest_of_line;
lbgbd_neighbor_null: NO? NEIGHBOR null_rest_of_line;
lbgbd_routed_null: NO? ROUTED null_rest_of_line;

lbgbd_vfi
:
   NO? VFI null_rest_of_line
   (
      lbgbdv_null
   )*
;

lbgbdm_limit
:
   NO? LIMIT null_rest_of_line
   (
      lbgbdml_action_null
      | lbgbdml_maximum_null
   )*
;

lbgbdml_action_null: NO? ACTION null_rest_of_line;
lbgbdml_maximum_null: NO? MAXIMUM null_rest_of_line;

lbgbdv_null
:
   NO?
   (
      NEIGHBOR
   ) null_rest_of_line
;

license_null
:
   NO?
   (
      CENTRALIZED_LICENSING_ENABLE
   ) null_rest_of_line
;

lpts_null
:
   NO?
   (
      FLOW
   ) null_rest_of_line
;

lxp_neighbor
:
   NO? NEIGHBOR null_rest_of_line
   (
      lxpn_l2tp
      | lxpn_null
   )*
;

lxp_interface_null: NO? INTERFACE null_rest_of_line;
lxp_monitor_session_null: NO? MONITOR_SESSION null_rest_of_line;

lxpn_null
:
   NO?
   (
      SOURCE
   ) null_rest_of_line
;

lxpn_l2tp
:
   NO? L2TP null_rest_of_line
   (
      lxpnl_local_null
      | lxpnl_remote_null
   )*
;

lxpnl_local_null: NO? LOCAL null_rest_of_line;
lxpnl_remote_null: NO? REMOTE null_rest_of_line;

map_class_null
:
   NO?
   (
      DIALER
   ) null_rest_of_line
;

management_api
:
   API HTTP_COMMANDS NEWLINE
   (
      management_api_authentication_null
      | management_api_exit_null
      | management_api_idle_timeout_null
      | management_api_protocol_null
      | management_api_shutdown_null
      | management_api_vrf
   )*
;

management_api_authentication_null: NO? AUTHENTICATION null_rest_of_line;
management_api_exit_null: NO? EXIT null_rest_of_line;
management_api_idle_timeout_null: NO? IDLE_TIMEOUT null_rest_of_line;
management_api_protocol_null: NO? PROTOCOL null_rest_of_line;
management_api_shutdown_null: NO? SHUTDOWN null_rest_of_line;

management_api_vrf
:
   VRF name = variable NEWLINE
   (
      management_api_vrf_null
   )*
;

management_api_vrf_null
:
   NO?
   (
      SHUTDOWN
   ) null_rest_of_line
;

management_console
:
   CONSOLE NEWLINE
   (
      management_console_null
   )*
;

management_console_null
:
   NO?
   (
      IDLE_TIMEOUT
   ) null_rest_of_line
;

management_egress_interface_selection
:
   MANAGEMENT EGRESS_INTERFACE_SELECTION NEWLINE
   (
      management_egress_interface_selection_null
   )*
   (
      EXIT NEWLINE
   )?
;

management_egress_interface_selection_null
:
   NO?
   (
      APPLICATION
   ) null_rest_of_line
;

management_ssh
:
   SSH NEWLINE
   (
      management_ssh_authentication_null
      | management_ssh_idle_timeout_null
      | management_ssh_ip_access_group
      | management_ssh_shutdown_null
   )*
;

management_ssh_ip_access_group
:
   IP ACCESS_GROUP name = variable
   (
      IN
      | OUT
   ) NEWLINE
;

management_ssh_authentication_null: NO? AUTHENTICATION null_rest_of_line;
management_ssh_idle_timeout_null: NO? IDLE_TIMEOUT null_rest_of_line;
management_ssh_shutdown_null: NO? SHUTDOWN null_rest_of_line;

management_telnet
:
   TELNET NEWLINE
   (
      management_telnet_idle_timeout_null
      | management_telnet_ip_access_group
      | management_telnet_shutdown_null
   )*
;

management_telnet_ip_access_group
:
   IP ACCESS_GROUP name = variable
   (
      IN
      | OUT
   ) NEWLINE
;

management_telnet_idle_timeout_null: NO? IDLE_TIMEOUT null_rest_of_line;
management_telnet_shutdown_null: NO? SHUTDOWN null_rest_of_line;

mgp_stanza
:
   inband_mgp_stanza
;

monitor_destination
:
   NO? DESTINATION null_rest_of_line
   (
      monitor_destination_erspan_id_null
      | monitor_destination_ip_null
      | monitor_destination_mtu_null
      | monitor_destination_origin_null
   )*
;

monitor_destination_erspan_id_null: NO? ERSPAN_ID null_rest_of_line;
monitor_destination_ip_null: NO? IP null_rest_of_line;
monitor_destination_mtu_null: NO? MTU null_rest_of_line;
monitor_destination_origin_null: NO? ORIGIN null_rest_of_line;

monitor_buffer_size_null: NO? BUFFER_SIZE null_rest_of_line;
monitor_description_null: NO? DESCRIPTION null_rest_of_line;
monitor_shutdown_null: NO? SHUTDOWN null_rest_of_line;
monitor_source_null: NO? SOURCE null_rest_of_line;

monitor_session_null
:
   NO?
   (
      DESTINATION
   ) null_rest_of_line
;

mp_connect_source_null: NO? CONNECT_SOURCE null_rest_of_line;
mp_description_null: NO? DESCRIPTION null_rest_of_line;
mp_mesh_group_null: NO? MESH_GROUP null_rest_of_line;
mp_remote_as_null: NO? REMOTE_AS null_rest_of_line;
mp_shutdown_null: NO? SHUTDOWN null_rest_of_line;

mt_null
:
   NO?
   (
      ADDRESS
   ) null_rest_of_line
;

multicast_routing_stanza
:
   MULTICAST_ROUTING NEWLINE
   (
      address_family_multicast_stanza
   )*
;

no_aaa_group_server_stanza
:
   NO AAA GROUP SERVER null_rest_of_line
;

no_ip_access_list_stanza
:
   NO IP ACCESS_LIST null_rest_of_line
;

null_af_multicast_tail
:
   NSF NEWLINE
;

vrfd_af_export
:
  EXPORT
  (
    vrfd_af_export_nonvpn
    | vrfd_af_export_vpn
  )
;

vrfd_af_export_nonvpn
:
  (IPV4 | IPV6) (MULTICAST | UNICAST) prefix_limit=dec? MAP name = variable NEWLINE
;

vrfd_af_export_vpn
:
  MAP name = variable NEWLINE
;

vrfd_af_import
:
   IMPORT
   vrfd_af_import_map
;

vrfd_af_import_map
:
   MAP name = variable NEWLINE
;

vrfd_af_route_target
:
   ROUTE_TARGET type = both_export_import rt = route_target NEWLINE
;

vrfd_af_maximum_null: NO? MAXIMUM null_rest_of_line;
vrfd_af_mdt_null: NO? MDT null_rest_of_line;

null_imgp_stanza
:
   NO?
   (
      VRF
   ) null_rest_of_line
;

nv_satellite
:
   NO?
   (
      SATELLITE
   ) null_rest_of_line
   (
      nvs_description_null
      | nvs_ip_null
      | nvs_serial_number_null
      | nvs_type_null
   )*
;

nvs_description_null: NO? DESCRIPTION null_rest_of_line;
nvs_ip_null: NO? IP null_rest_of_line;
nvs_serial_number_null: NO? SERIAL_NUMBER null_rest_of_line;
nvs_type_null: NO? TYPE null_rest_of_line;

of_bind_null: NO? BIND null_rest_of_line;
of_controller_null: NO? CONTROLLER null_rest_of_line;
of_default_action_null: NO? DEFAULT_ACTION null_rest_of_line;
of_description_null: NO? DESCRIPTION null_rest_of_line;
of_enable_null: NO? ENABLE null_rest_of_line;

peer_sa_filter
:
   SA_FILTER
   (
      IN
      | OUT
   )
   (
      LIST
      | RP_LIST
   ) name = variable NEWLINE
;

peer_stanza
:
   PEER IP_ADDRESS NEWLINE
   (
      mp_connect_source_null
      | mp_description_null
      | mp_mesh_group_null
      | mp_remote_as_null
      | mp_shutdown_null
      | peer_sa_filter
   )*
;

phone_proxy_cipc_null: NO? CIPC null_rest_of_line;
phone_proxy_ctl_file_null: NO? CTL_FILE null_rest_of_line;
phone_proxy_disable_null: NO? DISABLE null_rest_of_line;
phone_proxy_media_termination_null: NO? MEDIA_TERMINATION null_rest_of_line;
phone_proxy_proxy_server_null: NO? PROXY_SERVER null_rest_of_line;
phone_proxy_tftp_server_null: NO? TFTP_SERVER null_rest_of_line;
phone_proxy_tls_proxy_null: NO? TLS_PROXY null_rest_of_line;

redundancy_linecard_group
:
   LINECARD_GROUP null_rest_of_line
   (
      rlcg_member_null
      | rlcg_mode_null
      | rlcg_revertive_null
      | rlcg_rf_switch_null
   )*
;

redundancy_main_cpu
:
   MAIN_CPU null_rest_of_line
   (
      redundancy_main_cpu_null
   )*
;

redundancy_main_cpu_null
:
   NO?
   (
      AUTO_SYNC
   ) null_rest_of_line
;

redundancy_keepalive_enable_null: NO? KEEPALIVE_ENABLE null_rest_of_line;
redundancy_mode_null: NO? MODE null_rest_of_line;
redundancy_notification_timer_null: NO? NOTIFICATION_TIMER null_rest_of_line;
redundancy_protocol_null: NO? PROTOCOL null_rest_of_line;
redundancy_scheme_null: NO? SCHEME null_rest_of_line;

rf_arm_profile
:
   ARM_PROFILE double_quoted_string NEWLINE
   (
      rf_arm_profile_assignment_null
      | rf_arm_profile_backoff_time_null
      | rf_arm_profile_error_rate_threshold_null
      | rf_arm_profile_free_channel_index_null
      | rf_arm_profile_ideal_coverage_index_null
      | rf_arm_profile_max_tx_power_null
      | rf_arm_profile_min_tx_power_null
      | rf_arm_profile_rogue_ap_aware_null
      | rf_arm_profile_scanning_null
   )*
;

rf_arm_profile_assignment_null: NO? ASSIGNMENT null_rest_of_line;
rf_arm_profile_backoff_time_null: NO? BACKOFF_TIME null_rest_of_line;
rf_arm_profile_error_rate_threshold_null: NO? ERROR_RATE_THRESHOLD null_rest_of_line;
rf_arm_profile_free_channel_index_null: NO? FREE_CHANNEL_INDEX null_rest_of_line;
rf_arm_profile_ideal_coverage_index_null: NO? IDEAL_COVERAGE_INDEX null_rest_of_line;
rf_arm_profile_max_tx_power_null: NO? MAX_TX_POWER null_rest_of_line;
rf_arm_profile_min_tx_power_null: NO? MIN_TX_POWER null_rest_of_line;
rf_arm_profile_rogue_ap_aware_null: NO? ROGUE_AP_AWARE null_rest_of_line;
rf_arm_profile_scanning_null: NO? SCANNING null_rest_of_line;

rf_am_scan_profile_null: NO? AM_SCAN_PROFILE null_rest_of_line;
rf_arm_rf_domain_profile_null: NO? ARM_RF_DOMAIN_PROFILE null_rest_of_line;
rf_event_thresholds_profile_null: NO? EVENT_THRESHOLDS_PROFILE null_rest_of_line;
rf_optimization_profile_null: NO? OPTIMIZATION_PROFILE null_rest_of_line;

rf_dot11a_radio_profile
:
   DOT11A_RADIO_PROFILE double_quoted_string NEWLINE
   (
      rf_dot11a_radio_profile_arm_profile_null
      | rf_dot11a_radio_profile_mode_null
      | rf_dot11a_radio_profile_spectrum_load_balancing_null
      | rf_dot11a_radio_profile_spectrum_monitoring_null
   )*
;

rf_dot11a_radio_profile_arm_profile_null: NO? ARM_PROFILE null_rest_of_line;
rf_dot11a_radio_profile_mode_null: NO? MODE null_rest_of_line;
rf_dot11a_radio_profile_spectrum_load_balancing_null: NO? SPECTRUM_LOAD_BALANCING null_rest_of_line;
rf_dot11a_radio_profile_spectrum_monitoring_null: NO? SPECTRUM_MONITORING null_rest_of_line;

rf_dot11g_radio_profile
:
   DOT11G_RADIO_PROFILE double_quoted_string NEWLINE
   (
      rf_dot11g_radio_profile_arm_profile_null
      | rf_dot11g_radio_profile_mode_null
      | rf_dot11g_radio_profile_spectrum_load_balancing_null
      | rf_dot11g_radio_profile_spectrum_monitoring_null
   )*
;

rf_dot11g_radio_profile_arm_profile_null: NO? ARM_PROFILE null_rest_of_line;
rf_dot11g_radio_profile_mode_null: NO? MODE null_rest_of_line;
rf_dot11g_radio_profile_spectrum_load_balancing_null: NO? SPECTRUM_LOAD_BALANCING null_rest_of_line;
rf_dot11g_radio_profile_spectrum_monitoring_null: NO? SPECTRUM_MONITORING null_rest_of_line;

rlcg_member_null: NO? MEMBER null_rest_of_line;
rlcg_mode_null: NO? MODE null_rest_of_line;
rlcg_revertive_null: NO? REVERTIVE null_rest_of_line;
rlcg_rf_switch_null: NO? RF_SWITCH null_rest_of_line;

rmc_null
:
   NO?
   (
      MAXIMUM
   ) null_rest_of_line
;

role_description_null: NO? DESCRIPTION null_rest_of_line;
role_rule_null: NO? RULE null_rest_of_line;

route_tail
:
   iface = variable destination = IP_ADDRESS mask = IP_ADDRESS gateway = IP_ADDRESS
   (
      (
         (
            distance = dec+
         )?
         (
            TRACK track = dec+
         )?
      )
      |
      (
         TUNNELED
      )
   ) NEWLINE
;

router_multicast_stanza
:
   IPV6? ROUTER
   (
      IGMP
      | MLD
      | MSDP
      | PIM
   ) NEWLINE router_multicast_tail
;

router_multicast_tail
:
   (
      address_family_multicast_stanza
      |
      (
         INTERFACE ALL null_rest_of_line
      )
      | interface_multicast_stanza
      | null_inner
      | peer_stanza
      | rmc_null
   )*
;

s_access_line
:
   (
      linetype = HTTP
      | linetype = SSH
      | linetype = TELNET
   ) allowed_ip
;

s_airgroupservice
:
   AIRGROUPSERVICE null_rest_of_line
   (
      ags_description_null
      | ags_id_null
   )*
;

s_ap
:
   AP
   (
      ap_ap_blacklist_time_null
      | ap_enet_link_profile_null
      | ap_flush_r1_on_new_r0_null
      | ap_general_profile_null
      | ap_group_null
      | ap_lldp_null
      | ap_mesh_cluster_profile_null
      | ap_mesh_ht_ssid_profile_null
      | ap_mesh_radio_profile_null
      | ap_provisioning_profile_null
      | ap_regulatory_domain_profile
      | ap_spectrum_null
      | ap_system_profile
      | ap_wired_ap_profile_null
      | ap_wired_port_profile_null
   )
;

s_ap_group
:
   AP_GROUP double_quoted_string NEWLINE
   (
      apg_ap_system_profile_null
      | apg_dot11a_radio_profile_null
      | apg_dot11g_radio_profile_null
      | apg_ids_profile_null
      | apg_virtual_ap_null
   )*
;

s_ap_name
:
   AP_NAME double_quoted_string NEWLINE
   (
      apn_null
   )*
;

s_application
:
   APPLICATION NEWLINE SERVICE name = variable null_rest_of_line
   (
      PARAM null_rest_of_line
   )*
   (
      GLOBAL NEWLINE SERVICE name = variable null_rest_of_line
   )?
;

s_application_var
:
   APPLICATION name = variable NEWLINE
   (
      av_capture_null
      | av_interface_null
      | av_mode_null
      | av_shutdown_null
      | av_timesource_null
   )*
;

s_archive
:
   ARCHIVE null_rest_of_line
   (
      archive_log
      | archive_maximum_null
      | archive_path_null
      | archive_write_memory_null
   )*
;

s_authentication
:
   AUTHENTICATION null_rest_of_line
;

s_banner_ios
:
  banner_header = ios_banner_header banner = ios_delimited_banner NEWLINE
;

ios_banner_header
:
  BANNER_IOS
  | BANNER_CONFIG_SAVE_IOS
  | BANNER_EXEC_IOS
  | BANNER_INCOMING_IOS
  | BANNER_LOGIN_IOS
  | BANNER_MOTD_IOS
  | BANNER_PROMPT_TIMEOUT_IOS
  | BANNER_SLIP_PPP_IOS
;

s_bfd
:
   BFD null_rest_of_line
   (
      bfd_null
   )*
;

s_bfd_template
:
  BFD_TEMPLATE SINGLE_HOP name = variable_permissive NEWLINE (
     bfd_template_echo_null
     | bfd_template_interval_null
  )*
;

s_cluster
:
   NO? CLUSTER
   (
      ENABLE
      | RUN
   ) null_rest_of_line
;

s_call_manager_fallback
:
   NO? CALL_MANAGER_FALLBACK NEWLINE
   (
      cmf_alias_null
      | cmf_call_forward_null
      | cmf_default_destination_null
      | cmf_dialplan_pattern_null
      | cmf_ip_null
      | cmf_keepalive_null
      | cmf_limit_dn_null
      | cmf_max_conferences_null
      | cmf_max_dn_null
      | cmf_max_ephones_null
      | cmf_secondary_dialtone_null
      | cmf_time_format_null
      | cmf_time_zone_null
      | cmf_transfer_system_null
      | cmf_translation_profile_null
   )+
;

s_configure
:
   NO? CONFIGURE
   (
      configure_maintenance
      | configure_session_null
      | configure_terminal_null
   )
;

s_control_plane
:
   CONTROL_PLANE
   (
      SLOT dec
   )? NEWLINE s_control_plane_tail*
;

s_control_plane_tail
:
   cp_exit_null
   | cp_ip_access_group
   | cp_ip_flow
   | cp_management_plane
   | cp_scale_factor_null
   | cp_service_policy
;

s_control_plane_security
:
   CONTROL_PLANE_SECURITY NEWLINE
   (
      cps_auto_cert_allow_all_null
      | cps_auto_cert_allowed_addrs_null
      | cps_auto_cert_prov_null
   )*
;

s_cops
:
   COPS
   (
      cops_listener
   )
;

s_cos_queue_group
:
   COS_QUEUE_GROUP null_rest_of_line
   (
      cqg_precedence_null
      | cqg_queue_null
      | cqg_random_detect_label_null
   )*
;

s_ctl_file
:
   NO? CTL_FILE null_rest_of_line
   (
      ctlf_record_entry_null
      | ctlf_shutdown_null
   )*
;

s_daemon
:
   DAEMON null_rest_of_line
   (
      daemon_exec_null
      | daemon_shutdown_null
   )*
;

s_dhcp
:
   NO? DHCP null_rest_of_line
   (
      dhcp_null
      | dhcp_profile
   )*
;

s_dialer
:
   DIALER
   (
      dialer_group
      | dialer_null
   )
;

s_dial_peer
:
   DIAL_PEER null_rest_of_line
   (
      NO?
      (
         CALL_BLOCK
         | CODEC
         | DESCRIPTION
         | DESTINATION_PATTERN
         | DIRECT_INWARD_DIAL
         | DTMF_RELAY
         | FAX
         | FORWARD_DIGITS
         | INCOMING
         |
         (
            IP
            (
               QOS
            )
         )
         | MEDIA
         | PORT
         | PREFERENCE
         | PREFIX
         | PROGRESS_IND
         | SERVICE
         | SESSION
         | SHUTDOWN
         | SIGNALING
         | TRANSLATION_PROFILE
         | VAD
         | VOICE_CLASS
      ) null_rest_of_line
   )*
;

s_domain
:
   DOMAIN
   (
      VRF vrf = variable
   )?
   (
      domain_lookup
      | domain_name
      | domain_name_server
   )
;

s_domain_name
:
   DOMAIN_NAME hostname = variable_hostname NEWLINE
;

s_dot11
:
   DOT11 null_rest_of_line
   (
      d11_accounting_null
      | d11_authentication_null
      | d11_guest_mode_null
      | d11_max_associations_null
      | d11_mbssid_null
      | d11_vlan_null
   )*
;

s_dspfarm
:
   NO? DSPFARM null_rest_of_line
   (
      dspf_associate_null
      | dspf_codec_null
      | dspf_description_null
      | dspf_maximum_null
      | dspf_shutdown_null
   )*
;

s_dynamic_access_policy_record
:
   NO? DYNAMIC_ACCESS_POLICY_RECORD null_rest_of_line
   (
      dapr_action_null
      | dapr_user_message_null
      | dapr_webvpn
   )*
;

s_enable
:
   ENABLE
   (
      enable_encrypted_password_null
      | enable_password
      | enable_read_only_password_null
      | enable_secret
      | enable_super_user_password_null
      | enable_telnet_null
   )
;

s_ephone_dn_template
:
   EPHONE_DN_TEMPLATE null_rest_of_line
   (
      ednt_null
   )*
;

s_event
:
   NO? EVENT null_rest_of_line
   (
      event_action_null
      | event_event_null
      | event_set_null
   )*
;

s_event_handler
:
   NO? EVENT_HANDLER null_rest_of_line
   (
      eh_action_null
      | eh_asynchronous_null
      | eh_delay_null
      | eh_trigger_null
   )*
;

s_event_monitor
:
   EVENT_MONITOR NEWLINE
;

s_flow
:
   FLOW
   (
      EXPORTER
      | EXPORTER_MAP
      | HARDWARE
      | MONITOR
      | MONITOR_MAP
      | PLATFORM
      | RECORD
   ) null_rest_of_line
   (
      flow_cache_null
      | flow_collect_null
      | flow_description_null
      | flow_destination_null
      | flow_export_protocol_null
      | flow_exporter_null
      | flow_match_null
      | flow_option_null
      | flow_record_null
      | flow_source_null
      | flow_statistics_null
      | flow_transport_null
      | flow_version
   )*
;

s_flow_sampler_map
:
   NO? FLOW_SAMPLER_MAP null_rest_of_line fsm_mode?
;

fsm_mode
:
   MODE RANDOM ONE_OUT_OF dec NEWLINE
;

s_gatekeeper
:
   GATEKEEPER NEWLINE
   (
      gk_gw_type_prefix_null
      | gk_lrq_null
      | gk_shutdown_null
      | gk_zone_null
   )*
;

s_global_port_security
:
   GLOBAL_PORT_SECURITY NEWLINE
   (
      gpsec_age_null
      | gpsec_delete_dynamic_learn_null
   )*
;

s_guest_access_email
:
   GUEST_ACCESS_EMAIL NEWLINE
   (
      gae_null
   )*
;

s_hardware
:
   NO? HARDWARE null_rest_of_line
   (
      hardware_ifacl_null
      | hardware_qos_null
      | hardware_rbacl_null
      | hardware_span_null
      | hardware_vacl_null
   )*
;

s_hostname
:
   (
      HOSTNAME
      | SWITCHNAME
   )
   (
      quoted_name = double_quoted_string
      |
      (
         (
            name_parts += ~NEWLINE
         )+
      )
   ) NEWLINE
;

s_ids
:
   IDS
   (
      ids_ap_classification_rule
      | ids_ap_rule_matching
      | ids_dos_profile
      | ids_general_profile
      | ids_impersonation_profile
      | ids_management_profile_null
      | ids_profile
      | ids_rate_thresholds_profile_null
      | ids_signature_matching_profile
      | ids_signature_profile_null
      | ids_unauthorized_device_profile
      | ids_wms_general_profile
      | ids_wms_local_system_profile_null
   )
;

s_ifmap
:
   IFMAP null_rest_of_line
   (
      ifmap_null
   )*
;

s_interface_line
:
   NO? INTERFACE BREAKOUT null_rest_of_line
;

s_ip
:
  IP
  (
    ip_extcommunity_list
    | ip_local
    | ip_pim
    | ip_sla
  )
;

s_ip_default_gateway
:
   NO? IP DEFAULT_GATEWAY
   (
      ipdg_address
      | ipdg_null
   )
;

s_ip_dhcp
:
   NO?
   (
      IP
      | IPV6
   ) DHCP
   (
      ip_dhcp_excluded_address_null
      | ip_dhcp_packet_null
      | ip_dhcp_pool
      | ip_dhcp_relay
      | ip_dhcp_smart_relay_null
      | ip_dhcp_snooping_null
      | ip_dhcp_use_null
   )
;

s_ip_domain
:
   NO? IP DOMAIN
   (
      ip_domain_lookup
      | ip_domain_name
      | ip_domain_null
   )
;

s_ip_domain_name
:
   IP DOMAIN_NAME hostname = variable_hostname
   (
      USE_VRF variable
   )? NEWLINE
;

ip_local
:
  LOCAL ipl_policy
;

ipl_policy
:
  POLICY ROUTE_MAP name = variable NEWLINE
;

s_ip_name_server
:
   IP NAME_SERVER
   (
      VRF vrf = variable
   )?
   (
      hostnames += ip_hostname
   )+
   (
      USE_VRF vrf = variable
   )? NEWLINE
;

s_ip_nbar
:
   IP NBAR CUSTOM null_rest_of_line
;

s_ip_probe
:
   IP PROBE null_rest_of_line
   (
      ip_probe_burst_size_null
      | ip_probe_frequency_null
      | ip_probe_mode_null
      | ip_probe_retries_null
   )*
;

s_ip_route_mos
:
   IP ROUTE IP_ADDRESS DEV interface_name NEWLINE
;

s_ip_source_route
:
   NO? IP SOURCE_ROUTE NEWLINE
;

s_ip_ssh
:
   NO? IP SSH
   (
      ip_ssh_authentication_retries_null
      | ip_ssh_client_null
      | ip_ssh_logging_null
      | ip_ssh_maxstartups_null
      | ip_ssh_no_null
      | ip_ssh_port_null
      | ip_ssh_pubkey_chain
      | ip_ssh_rsa_null
      | ip_ssh_server_null
      | ip_ssh_source_interface_null
      | ip_ssh_time_out_null
      | ip_ssh_version
   )
;

s_ip_tacacs_source_interface
:
   IP TACACS
   (
      VRF vrf = variable
   )? SOURCE_INTERFACE iname = interface_name NEWLINE
;

s_ip_wccp
:
   NO? IP WCCP
   (
      VRF vrf = variable
   )?
   (
      wccp_check_null
      | wccp_id
      | wccp_outbound_acl_check_null
      | wccp_source_interface_null
      | wccp_version_null
      | wccp_web_cache_null
   )
;

s_ipc
:
   IPC null_rest_of_line
   (
      ipc_association
   )*
;

s_ipv6
:
  IPV6
  ipv6_local
;

ipv6_local
:
  LOCAL ipv6l_policy
;

ipv6l_policy
:
  POLICY ROUTE_MAP name = variable NEWLINE
;

s_l2
:
   NO? L2 null_rest_of_line
   (
      l2_bridge_domain_null
      | l2_mtu_null
      | l2_neighbor_null
      | l2_vpn_null
   )*
;

s_l2tp_class
:
   NO? L2TP_CLASS name = variable NEWLINE
   (
      l2tpc_authentication_null
      | l2tpc_cookie_null
      | l2tpc_hello_null
      | l2tpc_hidden_literal_null
      | l2tpc_hostname_null
      | l2tpc_password_null
      | l2tpc_receive_window_null
      | l2tpc_retransmit_null
      | l2tpc_timeout_null
   )*
;

s_l2vpn
:
   NO? L2VPN null_rest_of_line
   (
      l2vpn_bridge_group
      | l2vpn_logging
      | l2vpn_xconnect
   )*
;

s_license
:
   NO? LICENSE null_rest_of_line
   (
      license_null
   )*
;

s_lpts
:
   NO? LPTS null_rest_of_line
   (
      lpts_null
   )*
;

s_management
:
   MANAGEMENT
   (
      management_api
      | management_console
      | management_egress_interface_selection
      | management_ssh
      | management_telnet
   )
;

s_map_class
:
   NO? MAP_CLASS null_rest_of_line
   (
      map_class_null
   )*
;

s_media_termination
:
   NO? MEDIA_TERMINATION null_rest_of_line
   (
      mt_null
   )*
;

s_monitor
:
   NO? MONITOR null_rest_of_line
   (
      monitor_buffer_size_null
      | monitor_description_null
      | monitor_destination
      | monitor_shutdown_null
      | monitor_source_null
   )*
;

s_monitor_session
:
   NO? MONITOR_SESSION null_rest_of_line
   (
      monitor_session_null
   )*
;

s_name
:
   NAME variable variable null_rest_of_line
;

s_no_access_list_extended
:
   NO ACCESS_LIST ACL_NUM_EXTENDED NEWLINE
;

s_no_access_list_standard
:
   NO ACCESS_LIST ACL_NUM_STANDARD NEWLINE
;

s_no_bfd
:
   NO BFD null_rest_of_line
;

s_no_enable
:
   NO ENABLE PASSWORD (LEVEL level = dec)? NEWLINE
;

s_nv
:
   NO? NV NEWLINE
   (
      nv_satellite
   )*
;

s_openflow
:
   NO? OPENFLOW null_rest_of_line
   (
      of_bind_null
      | of_controller_null
      | of_default_action_null
      | of_description_null
      | of_enable_null
   )*
;

s_passwd
:
   NO? PASSWD pass = variable ENCRYPTED? NEWLINE
;

s_phone_proxy
:
   NO? PHONE_PROXY null_rest_of_line
   (
      phone_proxy_cipc_null
      | phone_proxy_ctl_file_null
      | phone_proxy_disable_null
      | phone_proxy_media_termination_null
      | phone_proxy_proxy_server_null
      | phone_proxy_tftp_server_null
      | phone_proxy_tls_proxy_null
   )*
;

s_privilege
:
   NO? PRIVILEGE
   (
      CLEAR
      | CMD
      | CONFIGURE
      | EXEC
      | INTERFACE
      | IPENACL
      | ROUTER
      | SHOW
   ) null_rest_of_line
;

s_process_max_time
:
   NO? PROCESS_MAX_TIME dec NEWLINE
;

s_radius_server
:
   RADIUS SERVER name = variable NEWLINE
   (
      (
         ADDRESS
         | KEY
         | RETRANSMIT
         | TIMEOUT
      ) null_rest_of_line
   )+
;

s_redundancy
:
   NO? REDUNDANCY null_rest_of_line
   (
      redundancy_keepalive_enable_null
      | redundancy_linecard_group
      | redundancy_main_cpu
      | redundancy_mode_null
      | redundancy_notification_timer_null
      | redundancy_protocol_null
      | redundancy_scheme_null
   )*
;

s_rf
:
   RF
   (
      rf_am_scan_profile_null
      | rf_arm_profile
      | rf_arm_rf_domain_profile_null
      | rf_dot11a_radio_profile
      | rf_dot11g_radio_profile
      | rf_event_thresholds_profile_null
      | rf_optimization_profile_null
   )
;

s_role
:
   NO? ROLE null_rest_of_line
   (
      role_description_null
      | role_rule_null
   )*
;

s_route
:
   ROUTE route_tail
;

s_sccp
:
   NO? SCCP null_rest_of_line
   (
      sccp_associate_null
      | sccp_bind_null
      | sccp_description_null
      | sccp_switchback_null
   )*
;

s_service
:
   NO? SERVICE
   (
      words += variable
   )+ NEWLINE
;

s_service_policy_global
:
   SERVICE_POLICY name = variable GLOBAL NEWLINE
;

s_sip_ua
:
   SIP_UA NEWLINE
   (
      sip_ua_connection_reuse_null
      | sip_ua_retry_null
      | sip_ua_set_null
      | sip_ua_sip_server_null
      | sip_ua_timers_null
   )*
;

s_sntp
:
   SNTP sntp_server
;

s_spanning_tree
:
   NO? SPANNING_TREE
   (
      spanning_tree_backbonefast_null
      | spanning_tree_bpdufilter_null
      | spanning_tree_bridge_null
      | spanning_tree_cost_null
      | spanning_tree_dispute_null
      | spanning_tree_etherchannel_null
      | spanning_tree_extend_null
      | spanning_tree_fcoe_null
      | spanning_tree_guard_null
      | spanning_tree_logging_null
      | spanning_tree_loopguard_null
      | spanning_tree_mode_null
      | spanning_tree_mst
      | spanning_tree_optimize_null
      | spanning_tree_pathcost_null
      | spanning_tree_port_null
      | spanning_tree_portfast
      | spanning_tree_pseudo_information
      | spanning_tree_uplinkfast_null
      | spanning_tree_vlan_null
      | NEWLINE
   )
;

s_ssh
:
   SSH
   (
      ssh_access_group
      | ssh_client
      | ssh_ip_address_null
      | ssh_key_exchange_null
      | ssh_key_null
      | ssh_login_attempts_null
      | ssh_mgmt_auth_null
      | ssh_server
      | ssh_stricthostkeycheck_null
      | ssh_timeout
      | ssh_version_null
   )
;

s_statistics
:
   NO? STATISTICS null_rest_of_line
   (
      statistics_extended_counters_null
      | statistics_tm_voq_collection_null
   )*
;

s_stcapp
:
   STCAPP null_rest_of_line
   (
      (
         CALL
         | CPTONE
         | FALLBACK_DN
         | PICKUP
         | PORT
         | PREFIX
      ) null_rest_of_line
   )*
;

s_switchport
:
   SWITCHPORT DEFAULT MODE
   (
      ACCESS
      | ROUTED
   ) NEWLINE
;

s_system
:
   NO? SYSTEM
   (
      system_admin_vdc_null
      | system_auto_upgrade_null
      | system_default
      | system_fabric_mode_null
      | system_fabric_null
      | system_flowcontrol_null
      | system_interface_null
      | system_jumbomtu_null
      | system_mode_null
      | system_module_type_null
      | system_mtu_null
      | system_qos
      | system_routing_null
      | system_urpf_null
      | system_vlan_null
   )
   s_system_inner*
;

s_system_inner
:
   s_system_service_policy
;

s_system_service_policy
:
   SERVICE_POLICY TYPE QUEUING (INPUT | OUTPUT) policy_map = variable NEWLINE
;

s_tacacs
:
   TACACS
   (
      t_group_null
      | t_host_null
      | t_server
      | t_source_interface
   )
;

s_tacacs_server
:
   NO? TACACS_SERVER
   (
      ts_common
      | ts_host
      |
      (
         ts_host ts_common*
      )
   )
;

s_tap
:
   NO? TAP null_rest_of_line
   (
      tap_null
   )*
;

s_telephony_service
:
   TELEPHONY_SERVICE null_rest_of_line
   (
      telephony_service_ip_null
      | telephony_service_max_conferences_null
      | telephony_service_max_ephones_null
      | telephony_service_srst_null
      | telephony_service_transfer_system_null
   )*
;

s_template
:
  TEMPLATE null_rest_of_line
  (
    template_access_session_null
    | template_authentication_null
    | template_dot1x_null
    | template_mab_null
    | template_radius_server_null
  )*
;

s_time_range
:
   TIME_RANGE name = variable PERIODIC? NEWLINE
   (
      tr_weekday_null
      | tr_weekend_null
   )*
;

s_tunnel_group
:
   NO? TUNNEL_GROUP null_rest_of_line
   (
      tg_accounting_server_group_null
      | tg_address_pool_null
      | tg_authentication_null
      | tg_authentication_server_group_null
      | tg_default_group_policy_null
      | tg_group_url_null
      | tg_ipv6_address_pool_null
      | tg_isakmp_null
   )*
;

s_user_role
:
   USER_ROLE name = variable_permissive NEWLINE
   (
      ur_access_list
      | ur_captive_portal_null
      | ur_max_sessions_null
      | ur_vlan_null
   )*
;

s_username
:
   USERNAME
   (
      quoted_user = double_quoted_string
      | user = variable
   )
   (
      (
         u+ NEWLINE
      )
      |
      (
         NEWLINE
         (
            u NEWLINE
         )*
      )
   )
;

s_username_attributes
:
   USERNAME user = variable ATTRIBUTES NEWLINE
   (
      (
         ua_group_lock_null
         | ua_vpn_group_policy_null
      )
   )*
;



s_voice
:
   NO? VOICE
   (
      voice_alg_based_cac_null
      | voice_call_null
      | voice_class
      | voice_dialplan_profile_null
      | voice_hunt_null
      | voice_iec_null
      | voice_logging_null
      | voice_real_time_config_null
      | voice_rtcp_inactivity_null
      | voice_rtp_null
      | voice_service
      | voice_sip_midcall_req_timeout_null
      | voice_sip_null
      | voice_translation_profile
      | voice_translation_rule
   )
;

s_voice_card
:
   NO? VOICE_CARD null_rest_of_line
   (
      vc_codec_null
      | vc_dsp_null
      | vc_dspfarm_null
      | vc_voice_service_null
      | vc_watchdog_null
   )*
;

s_voice_port
:
   NO? VOICE_PORT null_rest_of_line
   (
      vp_caller_id_null
      | vp_connection_null
      | vp_cptone_null
      | vp_description_null
      | vp_echo_cancel_null
      | vp_shutdown_null
      | vp_signal_null
      | vp_timeouts_null
      | vp_timing_null
   )*
;

s_vpc
:
   NO? VPC null_rest_of_line
   (
      vpc_auto_recovery_null
      | vpc_delay_null
      | vpc_dual_active_null
      | vpc_graceful_null
      | vpc_ip_null
      | vpc_peer_config_check_bypass_null
      | vpc_peer_gateway_null
      | vpc_peer_keepalive_null
      | vpc_peer_switch_null
      | vpc_role_null
      | vpc_system_priority_null
   )*
;

s_vpdn_group
:
   NO? VPDN_GROUP null_rest_of_line
   (
      vpdng_accept_dialin
      | vpdng_null
   )*
;

s_vpn
:
   NO? VPN null_rest_of_line
   (
      vpn_cluster_null
      | vpn_participate_null
      | vpn_priority_null
      | vpn_redirect_fqdn_null
   )*
;

s_vpn_dialer
:
   VPN_DIALER name = variable NEWLINE
   (
      vpn_dialer_null
   )*
;

// a way to define a VRF on IOS
s_vrf_definition
:
   // DEFINITION is for IOS
   VRF DEFINITION? name = variable NEWLINE
   (
      vrfd_address_family
      | vrfd_description
      | vrfd_no
      | vrfd_null
      | vrfd_rd
      | vrfd_route_target
   )*
   (
      EXIT_VRF NEWLINE
   )?
;

s_web_server
:
   WEB_SERVER PROFILE NEWLINE
   (
      web_server_captive_portal_cert_null
      | web_server_idp_cert_null
      | web_server_session_timeout_null
      | web_server_switch_cert_null
      | web_server_web_https_port_443_null
      | web_server_web_max_clients_null
   )*
;

s_webvpn
:
   NO? WEBVPN null_rest_of_line
   (
      webvpn_anyconnect_essentials_null
      | webvpn_anyconnect_null
      | webvpn_cache_null
      | webvpn_csd_null
      | webvpn_disable_null
      | webvpn_enable_null
      | webvpn_error_recovery_null
      | webvpn_keepout_null
      | webvpn_tunnel_group_list_null
   )*
;

s_wlan
:
   WLAN
   (
      wlan_bcn_rpt_req_profile_null
      | wlan_dot11k_profile_null
      | wlan_dot11r_profile_null
      | wlan_edca_parameters_profile_null
      | wlan_handover_trigger_profile_null
      | wlan_hotspot_null
      | wlan_ht_ssid_profile_null
      | wlan_rrm_ie_profile_null
      | wlan_ssid_profile
      | wlan_tsm_req_profile_null
      | wlan_virtual_ap
      | wlan_voip_cac_profile_null
   )
;

s_wsma
:
   WSMA null_rest_of_line
   (
      wsma_profile_null
      | wsma_transport_null
   )*
;

s_xconnect_logging
:
   NO? XCONNECT LOGGING null_rest_of_line
;

sccp_associate_null: NO? ASSOCIATE null_rest_of_line;
sccp_bind_null: NO? BIND null_rest_of_line;
sccp_description_null: NO? DESCRIPTION null_rest_of_line;
sccp_switchback_null: NO? SWITCHBACK null_rest_of_line;

sd_dce_mode_null
:
   DCE_MODE null_rest_of_line
;
sd_interface_null
:
   INTERFACE null_rest_of_line
;
sd_link_fail_null
:
   LINK_FAIL null_rest_of_line
;

sd_switchport
:
   SWITCHPORT
   (
      sd_switchport_blank
      | sd_switchport_fabricpath_null
      | sd_switchport_monitor_null
      | sd_switchport_shutdown
   )
;

sd_switchport_blank
:
   NEWLINE
;

sd_switchport_fabricpath_null
:
   FABRICPATH null_rest_of_line
;
sd_switchport_monitor_null
:
   MONITOR null_rest_of_line
;

sd_switchport_shutdown
:
   SHUTDOWN NEWLINE
;

sip_ua_connection_reuse_null: NO? CONNECTION_REUSE null_rest_of_line;
sip_ua_retry_null: NO? RETRY null_rest_of_line;
sip_ua_set_null: NO? SET null_rest_of_line;
sip_ua_sip_server_null: NO? SIP_SERVER null_rest_of_line;
sip_ua_timers_null: NO? TIMERS null_rest_of_line;

sntp_server
:
   SERVER hostname = variable
   (
      VERSION version = dec
   )? NEWLINE
;

spanning_tree_mst
:
   MST null_rest_of_line (
      spanning_tree_mst_instance_null
      | spanning_tree_mst_name_null
      | spanning_tree_mst_revision_null
   )*
;

spanning_tree_mst_instance_null: NO? INSTANCE null_rest_of_line;
spanning_tree_mst_name_null: NO? NAME null_rest_of_line;
spanning_tree_mst_revision_null: NO? REVISION null_rest_of_line;

spanning_tree_portfast
:
   PORTFAST
   (
      bpdufilter = BPDUFILTER
      | bpduguard = BPDUGUARD
      | defaultLiteral = DEFAULT
      | edge = EDGE
   )* NEWLINE
;

spanning_tree_pseudo_information
:
   PSEUDO_INFORMATION NEWLINE
   (
      spti_null
   )*
;

spanning_tree_backbonefast_null
:
   BACKBONEFAST null_rest_of_line
;
spanning_tree_bpdufilter_null
:
   BPDUFILTER null_rest_of_line
;
spanning_tree_bridge_null
:
   BRIDGE null_rest_of_line
;
spanning_tree_cost_null
:
   COST null_rest_of_line
;
spanning_tree_dispute_null
:
   DISPUTE null_rest_of_line
;
spanning_tree_etherchannel_null
:
   ETHERCHANNEL null_rest_of_line
;
spanning_tree_extend_null
:
   EXTEND null_rest_of_line
;
spanning_tree_fcoe_null
:
   FCOE null_rest_of_line
;
spanning_tree_guard_null
:
   GUARD null_rest_of_line
;
spanning_tree_logging_null
:
   LOGGING null_rest_of_line
;
spanning_tree_loopguard_null
:
   LOOPGUARD null_rest_of_line
;
spanning_tree_mode_null
:
   MODE null_rest_of_line
;
spanning_tree_optimize_null
:
   OPTIMIZE null_rest_of_line
;
spanning_tree_pathcost_null
:
   PATHCOST null_rest_of_line
;
spanning_tree_port_null
:
   PORT null_rest_of_line
;
spanning_tree_uplinkfast_null
:
   UPLINKFAST null_rest_of_line
;
spanning_tree_vlan_null
:
   VLAN null_rest_of_line
;

spti_null
:
   NO?
   (
      MST
   ) null_rest_of_line
;

srlg_interface_numeric_stanza
:
   dec null_rest_of_line
;

srlg_interface_stanza
:
   INTERFACE null_rest_of_line srlg_interface_numeric_stanza*
;

srlg_stanza
:
   SRLG NEWLINE srlg_interface_stanza*
;

ssh_access_group
:
   ACCESS_GROUP IPV6? name = variable NEWLINE
;

ssh_client
:
   CLIENT null_rest_of_line
;

ssh_ip_address_null
:
   IP_ADDRESS null_rest_of_line
;
ssh_key_null
:
   KEY null_rest_of_line
;
ssh_key_exchange_null
:
   KEY_EXCHANGE null_rest_of_line
;
ssh_login_attempts_null
:
   LOGIN_ATTEMPTS null_rest_of_line
;
ssh_mgmt_auth_null
:
   MGMT_AUTH null_rest_of_line
;
ssh_stricthostkeycheck_null
:
   STRICTHOSTKEYCHECK null_rest_of_line
;
ssh_version_null
:
   VERSION null_rest_of_line
;

ssh_server
:
   SERVER
   (
      (
         IPV4 ACCESS_LIST acl = variable
      )
      |
      (
         IPV6 ACCESS_LIST acl6 = variable
      )
      | LOGGING
      |
      (
         SESSION_LIMIT limit = dec
      )
      | V2
      |
      (
         VRF vrf = variable
      )
   )* NEWLINE
;

ssh_timeout
:
   TIMEOUT dec NEWLINE
;

stanza
:
   appletalk_access_list_stanza
   | del_stanza
   | extended_access_list_stanza
   | extended_ipv6_access_list_stanza
   | ip_as_path_access_list_stanza
   | ip_as_path_regex_mode_stanza
   | ip_community_list_expanded_stanza
   | ip_community_list_standard_stanza
   | ip_prefix_list_stanza
   | ip_route_stanza
   | ipv6_prefix_list_stanza
   | ipx_sap_access_list_stanza
   | multicast_routing_stanza
   | no_aaa_group_server_stanza
   | no_ip_access_list_stanza
   | no_ip_prefix_list_stanza
   | no_route_map_stanza
   | protocol_type_code_access_list_stanza
   | route_map_stanza
   | router_bgp_stanza
   | router_isis_stanza
   | router_multicast_stanza
   | rsvp_stanza
   | s_aaa
   | s_access_line
   | s_airgroupservice
   | s_ap
   | s_ap_group
   | s_ap_name
   | s_application
   | s_application_var
   | s_archive
   | s_arp_access_list_extended
   | s_authentication
   | s_banner_ios
   | s_bfd
   | s_bfd_template
   | s_cable
   | s_call_home
   | s_callhome
   | s_call_manager_fallback
   | s_class_map
   | s_class_map_ios
   | s_cluster
   | s_configure
   | s_control_plane
   | s_control_plane_security
   | s_controller
   | s_cops
   | s_cos_queue_group
   | s_crypto
   | s_ctl_file
   | s_daemon
   | s_depi_class
   | s_depi_tunnel
   | s_device_tracking
   | s_dhcp
   | s_dialer
   | s_dial_peer
   | s_domain
   | s_domain_name
   | s_dot11
   | s_dspfarm
   | s_dynamic_access_policy_record
   | s_enable
   | s_ephone_dn_template
   | s_ethernet_services
   | s_event
   | s_event_handler
   | s_event_monitor
   | s_flow
   | s_flow_sampler_map
   | s_gatekeeper
   | s_global_port_security
   | s_guest_access_email
   | s_hardware
   | s_hostname
   | s_ids
   | s_ifmap
   |
   // do not move below s_interface
   s_interface_line
   | s_interface
   | s_ip
   | s_ip_access_list_eth
   | s_ip_default_gateway
   | s_ip_dhcp
   | s_ip_domain
   | s_ip_domain_name
   | s_ip_name_server
   | s_ip_nat
   | s_ip_nbar
   | s_ip_probe
   | s_ip_route_mos
   | s_ip_source_route
   | s_ip_ssh
   | s_ip_tacacs_source_interface
   | s_ip_wccp
   | s_ipc
   | s_ipv6
   | s_ipv6_router_ospf
   | s_key
   | s_l2
   | s_l2tp_class
   | s_l2vpn
   | s_license
   | s_line
   | s_logging
   | s_lpts
   | s_management
   | s_mac_access_list
   | s_mac_access_list_extended
   | s_map_class
   | s_media_termination
   | s_monitor
   | s_monitor_session
   | s_mpls
   | s_name
   | s_netdestination
   | s_netdestination6
   | s_netservice
   | s_no
   | s_no_access_list_extended
   | s_no_access_list_standard
   | s_no_bfd
   | s_no_enable
   | s_ntp
   | s_null
   | s_nv
   | s_object
   | s_object_group
   | s_openflow
   | s_passwd
   | s_phone_proxy
   | s_policy_map
   | s_policy_map_ios
   | s_privilege
   | s_process_max_time
   | s_qos_mapping
   | s_radius_server
   | s_redundancy
   | s_rf
   | s_role
   | s_route
   | s_router_eigrp
   | s_router_ospf
   | s_router_ospfv3
   | s_router_rip
   | s_router_static
   | s_sccp
   | s_service
   | s_service_policy_global
   | s_service_template
   | s_sip_ua
   | s_snmp_server
   | s_sntp
   | s_spanning_tree
   | s_ssh
   | s_statistics
   | s_stcapp
   | s_switchport
   | s_system
   | s_table_map
   | s_tacacs
   | s_tacacs_server
   | s_tap
   | s_telephony_service
   | s_template
   | s_time_range
   | s_track
   | s_tunnel_group
   | s_user_role
   | s_username
   | s_username_attributes
   | s_vlan
   | s_vlan_name
   | s_voice
   | s_voice_card
   | s_voice_port
   | s_vpc
   | s_vpdn_group
   | s_vpn
   | s_vpn_dialer
   | s_vrf_definition
   | s_web_server
   | s_webvpn
   | s_wlan
   | s_wsma
   | s_xconnect_logging
   | s_zone
   | s_zone_pair
   | srlg_stanza
   | standard_access_list_stanza
   | standard_ipv6_access_list_stanza
   | switching_mode_stanza
;

s_no
:
  NO
  (
    no_ip
    | no_track
    | no_vlan
  )
;

no_ip
:
  IP
  (
    no_ip_pim
    | no_ip_sla
  )
;

statistics_extended_counters_null: NO? EXTENDED_COUNTERS null_rest_of_line;
statistics_tm_voq_collection_null: NO? TM_VOQ_COLLECTION null_rest_of_line;

switching_mode_stanza
:
   SWITCHING_MODE null_rest_of_line
;

system_default
:
   DEFAULT
   (
      sd_dce_mode_null
      | sd_interface_null
      | sd_link_fail_null
      | sd_switchport
   )
;

system_admin_vdc_null
:
   ADMIN_VDC null_rest_of_line
;
system_auto_upgrade_null
:
   AUTO_UPGRADE null_rest_of_line
;
system_fabric_null
:
   FABRIC null_rest_of_line
;
system_fabric_mode_null
:
   FABRIC_MODE null_rest_of_line
;
system_flowcontrol_null
:
   FLOWCONTROL null_rest_of_line
;
system_interface_null
:
   INTERFACE null_rest_of_line
;
system_jumbomtu_null
:
   JUMBOMTU null_rest_of_line
;
system_mode_null
:
   MODE null_rest_of_line
;
system_module_type_null
:
   MODULE_TYPE null_rest_of_line
;
system_mtu_null
:
   MTU null_rest_of_line
;
system_routing_null
:
   ROUTING null_rest_of_line
;
system_urpf_null
:
   URPF null_rest_of_line
;
system_vlan_null
:
   VLAN null_rest_of_line
;

system_qos
:
   QOS NEWLINE
   (
      system_qos_fex_null
      | system_qos_service_policy_null
   )*
;

system_qos_fex_null: NO? FEX null_rest_of_line;
system_qos_service_policy_null: NO? SERVICE_POLICY null_rest_of_line;

t_group_null
:
   GROUP null_rest_of_line
;
t_host_null
:
   HOST null_rest_of_line
;

t_server
:
   SERVER hostname = variable_hostname NEWLINE
   (
      t_server_address
      | t_key
      | t_server_single_connection_null
      | t_server_timeout_null
   )*
;

t_server_address
:
   ADDRESS
   (
      (
         IPV4 IP_ADDRESS
      )
      |
      (
         IPV6 IPV6_ADDRESS
      )
   ) NEWLINE
;

t_server_single_connection_null: NO? SINGLE_CONNECTION null_rest_of_line;
t_server_timeout_null: NO? TIMEOUT null_rest_of_line;

t_key
:
   KEY dec? variable_permissive NEWLINE
;

t_source_interface
:
   SOURCE_INTERFACE iname = interface_name
   (
      VRF name = variable
   )? NEWLINE
;

tap_null
:
   NO?
   (
      MODE
   ) null_rest_of_line
;

telephony_service_ip_null: NO? IP null_rest_of_line;
telephony_service_max_conferences_null: NO? MAX_CONFERENCES null_rest_of_line;
telephony_service_max_ephones_null: NO? MAX_EPHONES null_rest_of_line;
telephony_service_srst_null: NO? SRST null_rest_of_line;
telephony_service_transfer_system_null: NO? TRANSFER_SYSTEM null_rest_of_line;

template_access_session_null: NO? ACCESS_SESSION null_rest_of_line;
template_authentication_null: NO? AUTHENTICATION null_rest_of_line;
template_dot1x_null: NO? DOT1X null_rest_of_line;
template_mab_null: NO? MAB null_rest_of_line;
template_radius_server_null: NO? RADIUS_SERVER null_rest_of_line;

tg_accounting_server_group_null: NO? ACCOUNTING_SERVER_GROUP null_rest_of_line;
tg_address_pool_null: NO? ADDRESS_POOL null_rest_of_line;
tg_authentication_null: NO? AUTHENTICATION null_rest_of_line;
tg_authentication_server_group_null: NO? AUTHENTICATION_SERVER_GROUP null_rest_of_line;
tg_default_group_policy_null: NO? DEFAULT_GROUP_POLICY null_rest_of_line;
tg_group_url_null: NO? GROUP_URL null_rest_of_line;
tg_ipv6_address_pool_null: NO? IPV6_ADDRESS_POOL null_rest_of_line;
tg_isakmp_null: NO? ISAKMP null_rest_of_line;

tr_weekday_null: NO? WEEKDAY null_rest_of_line;
tr_weekend_null: NO? WEEKEND null_rest_of_line;

ts_common
:
   ts_deadtime_null
   | ts_directed_request_null
   | ts_key_null
   | ts_retransmit_null
   | ts_test_null
   | ts_timeout_null
;

ts_host
:
   HOST hostname =
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) null_rest_of_line t_key?
;

ts_deadtime_null
:
   DEADTIME null_rest_of_line
;
ts_directed_request_null
:
   DIRECTED_REQUEST null_rest_of_line
;
ts_key_null
:
   KEY null_rest_of_line
;
ts_retransmit_null
:
   RETRANSMIT null_rest_of_line
;
ts_test_null
:
   TEST null_rest_of_line
;
ts_timeout_null
:
   TIMEOUT null_rest_of_line
;

u
:
   u_encrypted_password
   | u_nohangup
   | u_passphrase
   | u_password
   | u_privilege
   | u_role
;

u_encrypted_password
:
   ENCRYPTED_PASSWORD pass = variable_permissive
;

u_nohangup
:
   NOHANGUP
;

u_passphrase
:
   PASSPHRASE
   (
      GRACETIME gracetime = dec
      | LIFETIME lifetime = dec
      | WARNTIME warntime = dec
   )*
;

u_password
:
   (
      (
         PASSWORD
         | SECRET
      )
      up_cisco
   )
   |
   (
      NOPASSWORD
   )
;

u_privilege
:
   PRIVILEGE privilege = variable
;

u_role
:
   (
      GROUP
      | ROLE
   ) role = variable
;

ua_group_lock_null
:
   GROUP_LOCK null_rest_of_line
;
ua_vpn_group_policy_null
:
   VPN_GROUP_POLICY null_rest_of_line
;

up_cisco
:
   dec? up_cisco_tail
;

up_cisco_tail
:
   (pass = variable_secret)
   (
      ENCRYPTED
      | MSCHAP
      | NT_ENCRYPTED
      | PBKDF2
   )?
;

ur_access_list
:
   ACCESS_LIST SESSION name = variable_permissive NEWLINE
;

ur_captive_portal_null: NO? CAPTIVE_PORTAL null_rest_of_line;
ur_max_sessions_null: NO? MAX_SESSIONS null_rest_of_line;
ur_vlan_null: NO? VLAN null_rest_of_line;

vc_codec_null: NO? CODEC null_rest_of_line;
vc_dsp_null: NO? DSP null_rest_of_line;
vc_dspfarm_null: NO? DSPFARM null_rest_of_line;
vc_voice_service_null: NO? VOICE_SERVICE null_rest_of_line;
vc_watchdog_null: NO? WATCHDOG null_rest_of_line;


voice_class
:
   CLASS
   (
      voice_class_codec
      | voice_class_dpg
      | voice_class_e164
      | voice_class_h323
      | voice_class_server_group
      | voice_class_sip_profiles
      | voice_class_uri
   )
;

voice_class_codec
:
   CODEC null_rest_of_line
   (
      voice_class_codec_null
   )*
;

voice_class_codec_null
:
   NO?
   (
      CODEC
   ) null_rest_of_line
;

voice_class_dpg
:
   DPG null_rest_of_line
   (
      voice_class_dpg_description_null
      | voice_class_dpg_dial_peer_null
   )*
;

voice_class_dpg_description_null: NO? DESCRIPTION null_rest_of_line;
voice_class_dpg_dial_peer_null: NO? DIAL_PEER null_rest_of_line;

voice_class_e164
:
   E164_PATTERN_MAP null_rest_of_line
   (
      voice_class_e164_description_null
      | voice_class_e164_e164_null
      | voice_class_e164_url_null
   )*
;

voice_class_e164_description_null: NO? DESCRIPTION null_rest_of_line;
voice_class_e164_e164_null: NO? E164 null_rest_of_line;
voice_class_e164_url_null: NO? URL null_rest_of_line;

voice_class_h323
:
   H323 null_rest_of_line
   (
      voice_class_h323_call_null
      | voice_class_h323_h225_null
      | voice_class_h323_telephony_service_null
   )*
;

voice_class_h323_call_null: NO? CALL null_rest_of_line;
voice_class_h323_h225_null: NO? H225 null_rest_of_line;
voice_class_h323_telephony_service_null: NO? TELEPHONY_SERVICE null_rest_of_line;

voice_class_server_group
:
   SERVER_GROUP null_rest_of_line
   (
      voice_class_server_group_description_null
      | voice_class_server_group_ipv4_null
   )*
;

voice_class_server_group_description_null: NO? DESCRIPTION null_rest_of_line;
voice_class_server_group_ipv4_null: NO? IPV4 null_rest_of_line;

voice_class_sip_profiles
:
   SIP_PROFILES null_rest_of_line
   (
      voice_class_sip_profiles_null
   )*
;

voice_class_sip_profiles_null
:
   NO?
   (
      REQUEST
   ) null_rest_of_line
;

voice_class_uri
:
    URI null_rest_of_line
    (
        HOST null_rest_of_line
    )
;

voice_alg_based_cac_null
:
   ALG_BASED_CAC null_rest_of_line
;
voice_call_null
:
   CALL null_rest_of_line
;
voice_dialplan_profile_null
:
   DIALPLAN_PROFILE null_rest_of_line
;
voice_hunt_null
:
   HUNT null_rest_of_line
;
voice_iec_null
:
   IEC null_rest_of_line
;
voice_logging_null
:
   LOGGING null_rest_of_line
;
voice_real_time_config_null
:
   REAL_TIME_CONFIG null_rest_of_line
;
voice_rtcp_inactivity_null
:
   RTCP_INACTIVITY null_rest_of_line
;
voice_rtp_null
:
   RTP null_rest_of_line
;
voice_sip_null
:
   SIP null_rest_of_line
;
voice_sip_midcall_req_timeout_null
:
   SIP_MIDCALL_REQ_TIMEOUT null_rest_of_line
;

voice_service
:
   SERVICE
   (
      voice_service_voip
   )
;

voice_service_voip
:
   VOIP NEWLINE
   (
      voice_service_voip_address_hiding_null
      | voice_service_voip_allow_connections_null
      | voice_service_voip_fax_null
      | voice_service_voip_h225_null
      | voice_service_voip_h323
      | voice_service_voip_ip_address_trusted_list
      | voice_service_voip_media_null
      | voice_service_voip_mode_null
      | voice_service_voip_modem_null
      | voice_service_voip_redundancy_group_null
      | voice_service_voip_rtp_port_null
      | voice_service_voip_shutdown_null
      | voice_service_voip_sip
      | voice_service_voip_supplementary_service_null
   )*
;

voice_service_voip_h323
:
   H323 NEWLINE
   (
      voice_service_voip_h323_call_null
      | voice_service_voip_h323_h225_null
   )*
;

voice_service_voip_h323_call_null: NO? CALL null_rest_of_line;
voice_service_voip_h323_h225_null: NO? H225 null_rest_of_line;

voice_service_voip_ip_address_trusted_list
:
   IP ADDRESS TRUSTED LIST NEWLINE
   (
      voice_service_voip_ip_address_trusted_list_null
   )*
;

voice_service_voip_ip_address_trusted_list_null
:
   NO?
   (
      IPV4
   ) null_rest_of_line
;

voice_service_voip_address_hiding_null: NO? ADDRESS_HIDING null_rest_of_line;
voice_service_voip_allow_connections_null: NO? ALLOW_CONNECTIONS null_rest_of_line;
voice_service_voip_fax_null: NO? FAX null_rest_of_line;
voice_service_voip_h225_null: NO? H225 null_rest_of_line;
voice_service_voip_media_null: NO? MEDIA null_rest_of_line;
voice_service_voip_mode_null: NO? MODE null_rest_of_line;
voice_service_voip_modem_null: NO? MODEM null_rest_of_line;
voice_service_voip_redundancy_group_null: NO? REDUNDANCY_GROUP null_rest_of_line;
voice_service_voip_rtp_port_null: NO? RTP_PORT null_rest_of_line;
voice_service_voip_shutdown_null: NO? SHUTDOWN null_rest_of_line;
voice_service_voip_supplementary_service_null: NO? SUPPLEMENTARY_SERVICE null_rest_of_line;

voice_service_voip_sip
:
   SIP NEWLINE
   (
      voice_service_voip_sip_bind_null
      | voice_service_voip_sip_early_offer_null
      | voice_service_voip_sip_error_passthru_null
      | voice_service_voip_sip_g729_null
      | voice_service_voip_sip_header_passing_null
      | voice_service_voip_sip_listen_port_null
      | voice_service_voip_sip_midcall_signaling_null
      | voice_service_voip_sip_sip_profiles_null
      | voice_service_voip_sip_transport_null
   )*
;

voice_service_voip_sip_bind_null: NO? BIND null_rest_of_line;
voice_service_voip_sip_early_offer_null: NO? EARLY_OFFER null_rest_of_line;
voice_service_voip_sip_error_passthru_null: NO? ERROR_PASSTHRU null_rest_of_line;
voice_service_voip_sip_g729_null: NO? G729 null_rest_of_line;
voice_service_voip_sip_header_passing_null: NO? HEADER_PASSING null_rest_of_line;
voice_service_voip_sip_listen_port_null: NO? LISTEN_PORT null_rest_of_line;
voice_service_voip_sip_midcall_signaling_null: NO? MIDCALL_SIGNALING null_rest_of_line;
voice_service_voip_sip_sip_profiles_null: NO? SIP_PROFILES null_rest_of_line;
voice_service_voip_sip_transport_null: NO? TRANSPORT null_rest_of_line;

voice_translation_profile
:
   TRANSLATION_PROFILE null_rest_of_line
   (
      voice_translation_profile_null
   )*
;

voice_translation_profile_null
:
   NO?
   (
      TRANSLATE
   ) null_rest_of_line
;

voice_translation_rule
:
   TRANSLATION_RULE null_rest_of_line
   (
      voice_translation_rule_null
   )*
;

voice_translation_rule_null
:
   NO?
   (
      RULE
   ) null_rest_of_line
;

vp_caller_id_null: NO? CALLER_ID null_rest_of_line;
vp_connection_null: NO? CONNECTION null_rest_of_line;
vp_cptone_null: NO? CPTONE null_rest_of_line;
vp_description_null: NO? DESCRIPTION null_rest_of_line;
vp_echo_cancel_null: NO? ECHO_CANCEL null_rest_of_line;
vp_shutdown_null: NO? SHUTDOWN null_rest_of_line;
vp_signal_null: NO? SIGNAL null_rest_of_line;
vp_timeouts_null: NO? TIMEOUTS null_rest_of_line;
vp_timing_null: NO? TIMING null_rest_of_line;

vpc_auto_recovery_null: NO? AUTO_RECOVERY null_rest_of_line;
vpc_delay_null: NO? DELAY null_rest_of_line;
vpc_dual_active_null: NO? DUAL_ACTIVE null_rest_of_line;
vpc_graceful_null: NO? GRACEFUL null_rest_of_line;
vpc_ip_null: NO? IP null_rest_of_line;
vpc_peer_config_check_bypass_null: NO? PEER_CONFIG_CHECK_BYPASS null_rest_of_line;
vpc_peer_gateway_null: NO? PEER_GATEWAY null_rest_of_line;
vpc_peer_keepalive_null: NO? PEER_KEEPALIVE null_rest_of_line;
vpc_peer_switch_null: NO? PEER_SWITCH null_rest_of_line;
vpc_role_null: NO? ROLE null_rest_of_line;
vpc_system_priority_null: NO? SYSTEM_PRIORITY null_rest_of_line;

vpdng_accept_dialin
:
   NO? ACCEPT_DIALIN null_rest_of_line
   (
      vpdnga_protocol_null
      | vpdnga_virtual_template_null
   )*
;

vpdng_null
:
   NO?
   (
      L2TP
   ) null_rest_of_line
;

vpdnga_protocol_null: NO? PROTOCOL null_rest_of_line;
vpdnga_virtual_template_null: NO? VIRTUAL_TEMPLATE null_rest_of_line;

vpn_dialer_null
:
   NO?
   (
      IKE
   ) null_rest_of_line
;

vpn_cluster_null: NO? CLUSTER null_rest_of_line;
vpn_participate_null: NO? PARTICIPATE null_rest_of_line;
vpn_priority_null: NO? PRIORITY null_rest_of_line;
vpn_redirect_fqdn_null: NO? REDIRECT_FQDN null_rest_of_line;

vrfd_address_family
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      MULTICAST
      | UNICAST
   )?
   (
      MAX_ROUTE dec
   )? NEWLINE
   (
      vrfd_af_export
      | vrfd_af_import
      | vrfd_af_maximum_null
      | vrfd_af_mdt_null
      | vrfd_af_route_target
   )*
   address_family_footer
;

vrfd_description
:
   description_line
;

vrfd_no
:
   NO vrfd_no_null
;

vrfd_no_null
:
  (
    AUTO_IMPORT
    | IPV4 MULTICAST
  ) null_rest_of_line
;

vrfd_null
:
   AUTO_IMPORT null_rest_of_line
;

vrfd_rd
:
   RD (AUTO | rd = route_distinguisher) NEWLINE
;

vrfd_route_target
:
   ROUTE_TARGET type = both_export_import rt = route_target NEWLINE
;

wccp_id
:
   id = dec
   (
      (
         GROUP_LIST group_list = variable
      )
      |
      (
         MODE
         (
            CLOSED
            | OPEN
         )
      )
      |
      (
         PASSWORD dec? password = variable
      )
      |
      (
         REDIRECT_LIST redirect_list = variable
      )
      |
      (
         SERVICE_LIST service_list = variable
      )
   )* NEWLINE
;

wccp_check_null
:
   CHECK null_rest_of_line
;
wccp_outbound_acl_check_null
:
   OUTBOUND_ACL_CHECK null_rest_of_line
;
wccp_source_interface_null
:
   SOURCE_INTERFACE null_rest_of_line
;
wccp_version_null
:
   VERSION null_rest_of_line
;
wccp_web_cache_null
:
   WEB_CACHE null_rest_of_line
;

web_server_captive_portal_cert_null: NO? CAPTIVE_PORTAL_CERT null_rest_of_line;
web_server_idp_cert_null: NO? IDP_CERT null_rest_of_line;
web_server_session_timeout_null: NO? SESSION_TIMEOUT null_rest_of_line;
web_server_switch_cert_null: NO? SWITCH_CERT null_rest_of_line;
web_server_web_https_port_443_null: NO? WEB_HTTPS_PORT_443 null_rest_of_line;
web_server_web_max_clients_null: NO? WEB_MAX_CLIENTS null_rest_of_line;

webvpn_anyconnect_null: NO? ANYCONNECT null_rest_of_line;
webvpn_anyconnect_essentials_null: NO? ANYCONNECT_ESSENTIALS null_rest_of_line;
webvpn_cache_null: NO? CACHE null_rest_of_line;
webvpn_csd_null: NO? CSD null_rest_of_line;
webvpn_disable_null: NO? DISABLE null_rest_of_line;
webvpn_enable_null: NO? ENABLE null_rest_of_line;
webvpn_error_recovery_null: NO? ERROR_RECOVERY null_rest_of_line;
webvpn_keepout_null: NO? KEEPOUT null_rest_of_line;
webvpn_tunnel_group_list_null: NO? TUNNEL_GROUP_LIST null_rest_of_line;

wlan_bcn_rpt_req_profile_null: NO? BCN_RPT_REQ_PROFILE null_rest_of_line;
wlan_dot11k_profile_null: NO? DOT11K_PROFILE null_rest_of_line;
wlan_dot11r_profile_null: NO? DOT11R_PROFILE null_rest_of_line;
wlan_edca_parameters_profile_null: NO? EDCA_PARAMETERS_PROFILE null_rest_of_line;
wlan_handover_trigger_profile_null: NO? HANDOVER_TRIGGER_PROFILE null_rest_of_line;
wlan_hotspot_null: NO? HOTSPOT null_rest_of_line;
wlan_ht_ssid_profile_null: NO? HT_SSID_PROFILE null_rest_of_line;
wlan_rrm_ie_profile_null: NO? RRM_IE_PROFILE null_rest_of_line;
wlan_tsm_req_profile_null: NO? TSM_REQ_PROFILE null_rest_of_line;
wlan_voip_cac_profile_null: NO? VOIP_CAC_PROFILE null_rest_of_line;

wlan_ssid_profile
:
   SSID_PROFILE double_quoted_string NEWLINE
   (
      wlan_ssid_profile_eapol_rate_opt_null
      | wlan_ssid_profile_essid_null
      | wlan_ssid_profile_ht_ssid_profile_null
      | wlan_ssid_profile_max_clients_null
      | wlan_ssid_profile_mcast_rate_opt_null
      | wlan_ssid_profile_opmode_null
      | wlan_ssid_profile_ssid_enable_null
      | wlan_ssid_profile_wmm_null
   )*
;

wlan_ssid_profile_eapol_rate_opt_null: NO? EAPOL_RATE_OPT null_rest_of_line;
wlan_ssid_profile_essid_null: NO? ESSID null_rest_of_line;
wlan_ssid_profile_ht_ssid_profile_null: NO? HT_SSID_PROFILE null_rest_of_line;
wlan_ssid_profile_max_clients_null: NO? MAX_CLIENTS null_rest_of_line;
wlan_ssid_profile_mcast_rate_opt_null: NO? MCAST_RATE_OPT null_rest_of_line;
wlan_ssid_profile_opmode_null: NO? OPMODE null_rest_of_line;
wlan_ssid_profile_ssid_enable_null: NO? SSID_ENABLE null_rest_of_line;
wlan_ssid_profile_wmm_null: NO? WMM null_rest_of_line;

wlan_virtual_ap
:
   VIRTUAL_AP double_quoted_string NEWLINE
   (
      wlan_virtual_ap_aaa_profile_null
      | wlan_virtual_ap_auth_failure_blacklist_time_null
      | wlan_virtual_ap_band_steering_null
      | wlan_virtual_ap_blacklist_null
      | wlan_virtual_ap_blacklist_time_null
      | wlan_virtual_ap_broadcast_filter_null
      | wlan_virtual_ap_deny_inter_user_traffic_null
      | wlan_virtual_ap_dynamic_mcast_optimization_null
      | wlan_virtual_ap_dynamic_mcast_optimization_thresh_null
      | wlan_virtual_ap_ssid_profile_null
      | wlan_virtual_ap_vap_enable_null
      | wlan_virtual_ap_vlan_null
   )*
;

wlan_virtual_ap_aaa_profile_null: NO? AAA_PROFILE null_rest_of_line;
wlan_virtual_ap_auth_failure_blacklist_time_null: NO? AUTH_FAILURE_BLACKLIST_TIME null_rest_of_line;
wlan_virtual_ap_band_steering_null: NO? BAND_STEERING null_rest_of_line;
wlan_virtual_ap_blacklist_null: NO? BLACKLIST null_rest_of_line;
wlan_virtual_ap_blacklist_time_null: NO? BLACKLIST_TIME null_rest_of_line;
wlan_virtual_ap_broadcast_filter_null: NO? BROADCAST_FILTER null_rest_of_line;
wlan_virtual_ap_deny_inter_user_traffic_null: NO? DENY_INTER_USER_TRAFFIC null_rest_of_line;
wlan_virtual_ap_dynamic_mcast_optimization_null: NO? DYNAMIC_MCAST_OPTIMIZATION null_rest_of_line;
wlan_virtual_ap_dynamic_mcast_optimization_thresh_null: NO? DYNAMIC_MCAST_OPTIMIZATION_THRESH null_rest_of_line;
wlan_virtual_ap_ssid_profile_null: NO? SSID_PROFILE null_rest_of_line;
wlan_virtual_ap_vap_enable_null: NO? VAP_ENABLE null_rest_of_line;
wlan_virtual_ap_vlan_null: NO? VLAN null_rest_of_line;

wsma_profile_null: NO? PROFILE null_rest_of_line;
wsma_transport_null: NO? TRANSPORT null_rest_of_line;
