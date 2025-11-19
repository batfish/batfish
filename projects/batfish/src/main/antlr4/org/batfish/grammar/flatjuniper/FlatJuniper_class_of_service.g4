parser grammar FlatJuniper_class_of_service;

import FlatJuniper_common;

s_class_of_service
:
   CLASS_OF_SERVICE
   (
       apply
       | scos_classifiers
       | scos_code_point_aliases
       | scos_forwarding_classes
       | scos_host_outbound_traffic
       | scos_interfaces
       | scos_rewrite_rules
       | scos_scheduler_maps
       | scos_schedulers
       | scos_null
   )
;

scos_classifiers
:
    CLASSIFIERS
    (
        scoscl_dscp
        | scoscl_dscp_ipv6
        | scoscl_exp
        | scoscl_ieee_802_1
        | scoscl_inet_precedence
    )
;

scos_loss_priority_value
:
    HIGH
    | LOW
    | MEDIUM_HIGH
    | MEDIUM_LOW
;

scoscl_dscp
:
    DSCP name = junos_name
    (
        scoscld_forwarding_class
        | scoscld_import
    )
;

scoscld_import
:
    IMPORT
    (
        DEFAULT
        | name = junos_name
    )
;

scoscld_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scoscldfc_loss_priority
    )
;

scoscldfc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINTS code = dscp_code_point_or_alias
;

scoscle_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosclefc_loss_priority
    )
;

scosclefc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINTS code = exp_code_point_or_alias
;

scoscli_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosclifc_loss_priority
    )
;

scosclifc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINTS code = ieee_802_1_code_point_or_alias
;

scosclip_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosclipfc_loss_priority
    )
;

scosclipfc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINTS code = inet_precedence_code_point_or_alias
;

// 6-bit DSCP code point value (binary 000000-111111) for code-point-aliases definitions
dscp_code_point
:
    CODE_POINT_6_BIT
;

// 6-bit DSCP code point value or alias reference for classifiers/rewrite-rules
dscp_code_point_or_alias
:
    code = dscp_code_point
    | alias = junos_name
;

// 3-bit MPLS EXP code point value (binary 000-111) for code-point-aliases definitions
exp_code_point
:
    CODE_POINT_3_BIT
;

// 3-bit MPLS EXP code point value or alias reference for classifiers/rewrite-rules
exp_code_point_or_alias
:
    code = exp_code_point
    | alias = junos_name
;

// 3-bit IEEE 802.1p code point value (binary 000-111) for code-point-aliases definitions
ieee_802_1_code_point
:
    CODE_POINT_3_BIT
;

// 3-bit IEEE 802.1p code point value or alias reference for classifiers/rewrite-rules
ieee_802_1_code_point_or_alias
:
    code = ieee_802_1_code_point
    | alias = junos_name
;

// 3-bit IP precedence code point value (binary 000-111) for code-point-aliases definitions
inet_precedence_code_point
:
    CODE_POINT_3_BIT
;

// 3-bit IP precedence code point value or alias reference for classifiers/rewrite-rules
inet_precedence_code_point_or_alias
:
    code = inet_precedence_code_point
    | alias = junos_name
;

scoscl_dscp_ipv6
:
    DSCP_IPV6 name = junos_name
    (
        scoscld6_forwarding_class
    )
;

scoscld6_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scoscld6fc_loss_priority
    )
;

scoscld6fc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINTS code = dscp_code_point_or_alias
;

scoscl_exp
:
    EXP name = junos_name
    (
        scoscle_forwarding_class
    )
;

scoscl_ieee_802_1
:
    IEEE_802_1 name = junos_name
    (
        scoscli_forwarding_class
    )
;

scoscl_inet_precedence
:
    INET_PRECEDENCE name = junos_name
    (
        scosclip_forwarding_class
    )
;

scos_code_point_aliases
:
    CODE_POINT_ALIASES
    (
        scoscpa_dscp
        | scoscpa_dscp_ipv6
        | scoscpa_exp
        | scoscpa_ieee_802_1
        | scoscpa_inet_precedence
    )
;

scoscpa_dscp
:
    DSCP name = junos_name code = dscp_code_point
;

scoscpa_dscp_ipv6
:
    DSCP_IPV6 name = junos_name code = dscp_code_point
;

scoscpa_exp
:
    EXP name = junos_name code = exp_code_point
;

scoscpa_ieee_802_1
:
    IEEE_802_1 name = junos_name code = ieee_802_1_code_point
;

scoscpa_inet_precedence
:
    INET_PRECEDENCE name = junos_name code = inet_precedence_code_point
;

scos_forwarding_classes
:
    FORWARDING_CLASSES
    (
        scosfc_class
        | scosfc_queue
    )
;

scosfc_class
:
    CLASS name = junos_name
    (
        scosfcc_queue_num
        | scosfcc_spu_priority
    )
;

scosfcc_queue_num
:
    QUEUE_NUM num = dec
;

scosfcc_spu_priority
:
    SPU_PRIORITY
    (
        HIGH
        | LOW
    )
;

scosfc_queue
:
    QUEUE num = dec name = junos_name
;

scos_host_outbound_traffic
:
    HOST_OUTBOUND_TRAFFIC
    (
        scoshob_forwarding_class
    )
;

scoshob_forwarding_class
:
    FORWARDING_CLASS name = junos_name
;

scos_interfaces
:
    INTERFACES
    (
        scosi_interface
    )
;

scosi_interface
:
    (
        interface_wildcard
        | interface_id
    )
    (
        scosii_classifiers
        | scosii_forwarding_class
        | scosii_forwarding_class_set
        | scosii_output_traffic_control_profile
        | scosii_scheduler_map
        | scosii_unit
    )
;

scosii_classifiers
:
    CLASSIFIERS
    (
        scosiiu_dscp
        | scosiiu_dscp_ipv6
        | scosiiu_exp
        | scosiiu_ieee_802_1
        | scosiiu_inet_precedence
    )
;

scosii_forwarding_class_set
:
    FORWARDING_CLASS_SET name = junos_name
    (
        scosii_fcs_output_traffic_control_profile
    )
;

scosii_fcs_output_traffic_control_profile
:
    OUTPUT_TRAFFIC_CONTROL_PROFILE name = junos_name
;

scosii_output_traffic_control_profile
:
    OUTPUT_TRAFFIC_CONTROL_PROFILE name = junos_name
;

scosii_forwarding_class
:
    FORWARDING_CLASS name = junos_name
;

scosii_scheduler_map
:
    SCHEDULER_MAP name = junos_name
;

scosii_unit
:
    UNIT
    (
        WILDCARD
        | ASTERISK
        | unit = dec
    )
    (
        scosiiu_classifiers
        | scosiiu_forwarding_class
        | scosiiu_output_traffic_control_profile
        | scosiiu_rewrite_rules
    )
;

scosiiu_output_traffic_control_profile
:
    OUTPUT_TRAFFIC_CONTROL_PROFILE name = junos_name
;

scosiiu_classifiers
:
    CLASSIFIERS
    (
        scosiiu_dscp
        | scosiiu_dscp_ipv6
        | scosiiu_exp
        | scosiiu_ieee_802_1
        | scosiiu_inet_precedence
    )
;

scosiiu_dscp
:
    DSCP name = junos_name
;

scosiiu_dscp_ipv6
:
    DSCP_IPV6 name = junos_name
;

scosiiu_exp
:
    EXP name = junos_name
;

scosiiu_ieee_802_1
:
    IEEE_802_1 name = junos_name
;

scosiiu_inet_precedence
:
    INET_PRECEDENCE name = junos_name
;

scosiiu_forwarding_class
:
    FORWARDING_CLASS name = junos_name
;

scosiiu_rewrite_rules
:
    REWRITE_RULES
    (
        scosiiu_dscp_rw
        | scosiiu_dscp_ipv6_rw
        | scosiiu_exp_rw
        | scosiiu_ieee_802_1_rw
        | scosiiu_inet_precedence_rw
    )
;

scosiiu_dscp_rw
:
    DSCP name = junos_name
;

scosiiu_dscp_ipv6_rw
:
    DSCP_IPV6 name = junos_name
;

scosiiu_exp_rw
:
    EXP name = junos_name PROTOCOL proto = scos_protocol_type
;

scos_protocol_type
:
    MPLS_ANY
    | MPLS_INET_BOTH
    | MPLS_INET_BOTH_NON_VPN
;

scosiiu_ieee_802_1_rw
:
    IEEE_802_1 name = junos_name
;

scosiiu_inet_precedence_rw
:
    INET_PRECEDENCE name = junos_name
;

scos_rewrite_rules
:
    REWRITE_RULES
    (
        scosrr_dscp
        | scosrr_dscp_ipv6
        | scosrr_exp
        | scosrr_ieee_802_1
        | scosrr_inet_precedence
    )
;

scosrr_dscp
:
    DSCP name = junos_name
    (
        scosrrd_forwarding_class
    )
;

scosrrd_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosrrdfc_loss_priority
    )
;

scosrrdfc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINT code = dscp_code_point_or_alias
;

scosrr_exp
:
    EXP name = junos_name
    (
        scosrre_forwarding_class
    )
;

scosrre_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosrrefc_loss_priority
    )
;

scosrrefc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINT code = exp_code_point_or_alias
;

scosrr_ieee_802_1
:
    IEEE_802_1 name = junos_name
    (
        scosrri_forwarding_class
    )
;

scosrri_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosrrifc_loss_priority
    )
;

scosrrifc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINT code = ieee_802_1_code_point_or_alias
;

scosrr_inet_precedence
:
    INET_PRECEDENCE name = junos_name
    (
        scosrrip_forwarding_class
    )
;

scosrrip_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosrripfc_loss_priority
    )
;

scosrripfc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINT code = inet_precedence_code_point_or_alias
;

scosrr_dscp_ipv6
:
    DSCP_IPV6 name = junos_name
    (
        scosrrd6_forwarding_class
    )
;

scosrrd6_forwarding_class
:
    FORWARDING_CLASS fc = junos_name
    (
        scosrrd6fc_loss_priority
    )
;

scosrrd6fc_loss_priority
:
    LOSS_PRIORITY priority = scos_loss_priority_value CODE_POINT code = dscp_code_point_or_alias
;

scos_scheduler_maps
:
    SCHEDULER_MAPS name = junos_name
    (
        scossm_forwarding_class
    )
;

scossm_forwarding_class
:
    FORWARDING_CLASS fc = junos_name SCHEDULER sched = junos_name
;

scos_schedulers
:
    SCHEDULERS name = junos_name
    (
        scoss_buffer_size
        | scoss_priority
        | scoss_transmit_rate
    )
;

scoss_buffer_size
:
    BUFFER_SIZE
    (
        scossb_percent
        | scossb_remainder
    )
;

scossb_percent
:
    PERCENT num = dec
;

scossb_remainder
:
    REMAINDER
;

scoss_priority
:
    PRIORITY
    (
        HIGH
        | LOW
        | MEDIUM_HIGH
        | MEDIUM_LOW
    )
;

scoss_transmit_rate
:
    TRANSMIT_RATE
    (
        scosst_percent
        | scosst_remainder
    )
;

scosst_percent
:
    PERCENT num = dec
;

scosst_remainder
:
    REMAINDER (num = dec)?
;

scos_null
:
   (
      ADAPTIVE_SHAPERS
      | APPLICATION_TRAFFIC_CONTROL
      | DROP_PROFILES
      | FORWARDING_POLICY
      | FRAGMENTATION_MAPS
      | LOSS_PRIORITY_MAPS
      | NON_STRICT_PRIORITY_SCHEDULING
      | RESTRICTED_QUEUES
      | ROUTING_INSTANCES
      | SHARED_BUFFER
      | TRACE_OPTIONS
      | TRAFFIC_CONTROL_PROFILES
      | TRANSLATION_TABLE
      | TRI_COLOR
      | VIRTUAL_CHANNEL
   )
   null_filler
;

