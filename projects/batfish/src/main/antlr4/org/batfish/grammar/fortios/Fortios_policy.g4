parser grammar Fortios_policy;

options {
  tokenVocab = FortiosLexer;
}

cf_policy: POLICY newline cfp*;

cfp: cfp_clone | cfp_delete | cfp_edit | cfp_move;

cfp_clone: CLONE name = policy_number TO to = policy_number newline;

cfp_delete: DELETE name = policy_number newline;

cfp_move: MOVE name = policy_number after_or_before pivot = policy_number newline;

cfp_edit: EDIT policy_number newline cfpe* NEXT newline;

cfpe: cfp_set | cfp_append | cfp_select;

cfp_set: SET (cfp_set_singletons | cfp_set_lists);

cfp_select: SELECT cfp_set_lists;

cfp_set_singletons :     cfp_set_action
    | cfp_set_application_list
    | cfp_set_auto_asic_offload
    | cfp_set_av_profile
    | cfp_set_comments
    | cfp_set_dnsfilter_profile
    | cfp_set_file_filter_profile
    | cfp_set_ippool
    | cfp_set_ips_sensor
    | cfp_set_logtraffic
    | cfp_set_logtraffic_start
    | cfp_set_name
    | cfp_set_nat
    | cfp_set_np_acceleration
    | cfp_set_poolname
    | cfp_set_profile_protocol_options
    | cfp_set_schedule
    | cfp_set_ssl_ssh_profile
    | cfp_set_status
    | cfp_set_utm_status
    | cfp_set_uuid
    | cfp_set_webfilter_profile;

cfp_set_application_list: APPLICATION_LIST application_list = str newline;

cfp_set_auto_asic_offload: AUTO_ASIC_OFFLOAD auto_asic_offload = enable_or_disable newline;

cfp_set_av_profile: AV_PROFILE av_profile = str newline;

cfp_set_dnsfilter_profile: DNSFILTER_PROFILE dnsfilter_profile = str newline;

cfp_set_file_filter_profile: FILE_FILTER_PROFILE file_filter_profile = str newline;

cfp_set_ippool: IPPOOL ippool = enable_or_disable newline;

cfp_set_ips_sensor: IPS_SENSOR ips_sensor = str newline;

cfp_set_logtraffic: LOGTRAFFIC logtraffic = logtraffic_value newline;

logtraffic_value: ALL | UTM | DISABLE;

cfp_set_logtraffic_start: LOGTRAFFIC_START logtraffic_start = enable_or_disable newline;

cfp_set_nat: NAT nat = enable_or_disable newline;

cfp_set_np_acceleration: NP_ACCELERATION np_acceleration = enable_or_disable newline;

cfp_set_poolname: POOLNAME poolname = str newline;

cfp_set_profile_protocol_options: PROFILE_PROTOCOL_OPTIONS profile_protocol_options = str newline;

cfp_set_ssl_ssh_profile: SSL_SSH_PROFILE ssl_ssh_profile = str newline;

cfp_set_webfilter_profile: WEBFILTER_PROFILE webfilter_profile = str newline;

cfp_set_schedule: SCHEDULE schedule = str newline;

cfp_set_utm_status: UTM_STATUS utm_status = enable_or_disable newline;

cfp_set_action: ACTION action = policy_action newline;

cfp_set_comments: COMMENTS comments = str newline;

cfp_set_name: NAME name = policy_name newline;

cfp_set_status: STATUS status = policy_status newline;

cfp_set_uuid: UUID id = str newline;

cfp_set_lists
:
    cfp_set_dstaddr
    | cfp_set_dstintf
    | cfp_set_service
    | cfp_set_srcaddr
    | cfp_set_srcintf
;

cfp_set_dstaddr: DSTADDR addresses = address_names newline;

cfp_set_srcaddr: SRCADDR addresses = address_names newline;

cfp_set_service: SERVICE services = service_names newline;

cfp_set_dstintf: DSTINTF interfaces = interface_or_zone_names newline;

cfp_set_srcintf: SRCINTF interfaces = interface_or_zone_names newline;

cfp_append
:
    APPEND (
        cfp_append_dstaddr
        | cfp_append_dstintf
        | cfp_append_service
        | cfp_append_srcaddr
        | cfp_append_srcintf
    )
;

cfp_append_dstaddr: DSTADDR addresses = address_names newline;

cfp_append_srcaddr: SRCADDR addresses = address_names newline;

cfp_append_service: SERVICE services = service_names newline;

cfp_append_dstintf: DSTINTF interfaces = interface_or_zone_names newline;

cfp_append_srcintf: SRCINTF interfaces = interface_or_zone_names newline;

// 1-35
policy_name: str;

policy_status: enable_or_disable;

// 0-4294967294
policy_number: str;

policy_action: ACCEPT | DENY | IPSEC;

address_names: address_name+;

interface_or_zone_names: interface_or_zone_name+;
