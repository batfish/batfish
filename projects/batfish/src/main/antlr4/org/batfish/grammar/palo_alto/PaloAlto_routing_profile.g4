parser grammar PaloAlto_routing_profile;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_routing_profile
:
    ROUTING_PROFILE
    (
        rp_bgp
        | rp_filters
    )?
;

rp_bgp
:
    BGP
    (
        rpbgp_address_family_profile
        | rpbgp_auth_profile
        | rpbgp_dampening_profile
        | rpbgp_filtering_profile
        | rpbgp_redistribution_profile
        | rpbgp_timer_profile
    )?
;

rpbgp_address_family_profile
:
    ADDRESS_FAMILY_PROFILE rpbgpafp_definition?
;

rpbgpafp_definition
:
    name = variable
    (
        rpbgpafp_description_null
        | rpbgpafp_ipv4
        | rpbgpafp_ipv6
    )?
;

rpbgpafp_description_null
:
    DESCRIPTION null_rest_of_line
;

rpbgpafp_ipv4
:
    IPV4
    (
        rpbgpafp_af_multicast
        | rpbgpafp_af_unicast
    )?
;

rpbgpafp_ipv6
:
    IPV6
    (
        rpbgpafp_af_multicast
        | rpbgpafp_af_unicast
    )?
;

rpbgpafp_af_multicast
:
    MULTICAST rpbgpafp_af_option?
;

rpbgpafp_af_unicast
:
    UNICAST rpbgpafp_af_option?
;

rpbgpafp_af_option
:
    rpbgpafpo_add_path_null
    | rpbgpafpo_allowas_in_null
    | rpbgpafpo_as_override_null
    | rpbgpafpo_default_originate_map_null
    | rpbgpafpo_default_originate_null
    | rpbgpafpo_enable_null
    | rpbgpafpo_maximum_prefix_null
    | rpbgpafpo_next_hop_null
    | rpbgpafpo_orf_null
    | rpbgpafpo_remove_private_as_null
    | rpbgpafpo_route_reflector_client_null
    | rpbgpafpo_send_community_null
    | rpbgpafpo_soft_reconfig_null
;

rpbgpafpo_add_path_null
:
    ADD_PATH null_rest_of_line
;

rpbgpafpo_allowas_in_null
:
    ALLOWAS_IN null_rest_of_line
;

rpbgpafpo_as_override_null
:
    AS_OVERRIDE yes_or_no
;

rpbgpafpo_default_originate_null
:
    DEFAULT_ORIGINATE yes_or_no
;

rpbgpafpo_default_originate_map_null
:
    DEFAULT_ORIGINATE_MAP null_rest_of_line
;

rpbgpafpo_enable_null
:
    ENABLE yes_or_no
;

rpbgpafpo_maximum_prefix_null
:
    MAXIMUM_PREFIX null_rest_of_line
;

rpbgpafpo_next_hop_null
:
    NEXT_HOP null_rest_of_line
;

rpbgpafpo_orf_null
:
    ORF null_rest_of_line
;

rpbgpafpo_remove_private_as_null
:
    REMOVE_PRIVATE_AS null_rest_of_line
;

rpbgpafpo_route_reflector_client_null
:
    ROUTE_REFLECTOR_CLIENT yes_or_no
;

rpbgpafpo_send_community_null
:
    SEND_COMMUNITY null_rest_of_line
;

rpbgpafpo_soft_reconfig_null
:
    SOFT_RECONFIG_WITH_STORED_INFO yes_or_no
;

rpbgp_auth_profile
:
    AUTH_PROFILE rpbgpap_definition?
;

rpbgpap_definition
:
    name = variable
    (
        rpbgpap_description_null
        | rpbgpap_secret_null
    )?
;

rpbgpap_description_null
:
    DESCRIPTION null_rest_of_line
;

rpbgpap_secret_null
:
    SECRET null_rest_of_line
;

rpbgp_dampening_profile
:
    DAMPENING_PROFILE rpbgpdamp_definition?
;

rpbgpdamp_definition
:
    name = variable rpbgpdamp_description_null?
;

rpbgpdamp_description_null
:
    DESCRIPTION null_rest_of_line
;

rpbgp_filtering_profile
:
    FILTERING_PROFILE rpbgpfp_definition?
;

rpbgpfp_definition
:
    name = variable
    (
        rpbgpfp_description_null
        | rpbgpfp_ipv4
        | rpbgpfp_ipv6
    )?
;

rpbgpfp_description_null
:
    DESCRIPTION null_rest_of_line
;

rpbgpfp_ipv4
:
    IPV4
    (
        rpbgpfp_af_multicast
        | rpbgpfp_af_unicast
    )?
;

rpbgpfp_ipv6
:
    IPV6
    (
        rpbgpfp_af_multicast
        | rpbgpfp_af_unicast
    )?
;

rpbgpfp_af_multicast
:
    MULTICAST rpbgpfp_af_option?
;

rpbgpfp_af_unicast
:
    UNICAST rpbgpfp_af_option?
;

rpbgpfp_af_option
:
    rpbgpfpo_conditional_advertisement_null
    | rpbgpfpo_distribute_list_null
    | rpbgpfpo_filter_list_null
    | rpbgpfpo_inbound_network_filters
    | rpbgpfpo_outbound_network_filters
    | rpbgpfpo_route_maps
    | rpbgpfpo_unsuppress_map_null
;

rpbgpfpo_conditional_advertisement_null
:
    CONDITIONAL_ADVERTISEMENT null_rest_of_line
;

rpbgpfpo_distribute_list_null
:
    DISTRIBUTE_LIST null_rest_of_line
;

rpbgpfpo_filter_list_null
:
    FILTER_LIST null_rest_of_line
;

rpbgpfpo_inbound_network_filters
:
    INBOUND_NETWORK_FILTERS
    (
        rpbgpfponf_distribute_list_null
        | rpbgpfponf_prefix_list_null
    )
;

rpbgpfpo_outbound_network_filters
:
    OUTBOUND_NETWORK_FILTERS
    (
        rpbgpfponf_distribute_list_null
        | rpbgpfponf_prefix_list_null
    )
;

rpbgpfponf_distribute_list_null
:
    DISTRIBUTE_LIST null_rest_of_line
;

rpbgpfponf_prefix_list_null
:
    PREFIX_LIST null_rest_of_line
;

rpbgpfpo_route_maps
:
    ROUTE_MAPS
    (
        rpbgpfporm_inbound_null
        | rpbgpfporm_outbound_null
    )
;

rpbgpfporm_inbound_null
:
    INBOUND null_rest_of_line
;

rpbgpfporm_outbound_null
:
    OUTBOUND null_rest_of_line
;

rpbgpfpo_unsuppress_map_null
:
    UNSUPPRESS_MAP null_rest_of_line
;

rpbgp_redistribution_profile
:
    REDISTRIBUTION_PROFILE rpbgprp_definition?
;

rpbgprp_definition
:
    name = variable
    (
        rpbgprp_connected_null
        | rpbgprp_description_null
        | rpbgprp_ipv4
        | rpbgprp_ipv6
        | rpbgprp_ospf_null
        | rpbgprp_ospfv3_null
        | rpbgprp_rip_null
        | rpbgprp_static_null
    )?
;

rpbgprp_connected_null
:
    CONNECTED null_rest_of_line
;

rpbgprp_description_null
:
    DESCRIPTION null_rest_of_line
;

rpbgprp_ipv4
:
    IPV4 rpbgprp_unicast
;

rpbgprp_ipv6
:
    IPV6 rpbgprp_unicast
;

rpbgprp_unicast
:
    UNICAST
    (
        rpbgprp_connected_null
        | rpbgprp_ospf_null
        | rpbgprp_ospfv3_null
        | rpbgprp_rip_null
        | rpbgprp_static_null
    )?
;

rpbgprp_ospf_null
:
    OSPF null_rest_of_line
;

rpbgprp_ospfv3_null
:
    OSPFV3 null_rest_of_line
;

rpbgprp_rip_null
:
    RIP null_rest_of_line
;

rpbgprp_static_null
:
    STATIC null_rest_of_line
;

rpbgp_timer_profile
:
    TIMER_PROFILE rpbgptp_definition?
;

rpbgptp_definition
:
    name = variable
    (
        rpbgptp_description_null
        | rpbgptp_hold_time_null
        | rpbgptp_keep_alive_interval_null
        | rpbgptp_min_route_adv_interval_null
        | rpbgptp_open_delay_time_null
        | rpbgptp_reconnect_retry_interval_null
    )?
;

rpbgptp_description_null
:
    DESCRIPTION null_rest_of_line
;

rpbgptp_hold_time_null
:
    HOLD_TIME null_rest_of_line
;

rpbgptp_keep_alive_interval_null
:
    KEEP_ALIVE_INTERVAL null_rest_of_line
;

rpbgptp_min_route_adv_interval_null
:
    MIN_ROUTE_ADV_INTERVAL null_rest_of_line
;

rpbgptp_open_delay_time_null
:
    OPEN_DELAY_TIME null_rest_of_line
;

rpbgptp_reconnect_retry_interval_null
:
    RECONNECT_RETRY_INTERVAL null_rest_of_line
;

rp_filters
:
    FILTERS
    (
        rpf_as_path_access_list_null
        | rpf_prefix_list
        | rpf_route_maps
    )?
;

rpf_as_path_access_list_null
:
    AS_PATH_ACCESS_LIST
;

rpf_prefix_list
:
    PREFIX_LIST rpfpl_definition?
;

rpfpl_definition
:
    name = variable
    (
        rpfpl_description_null
        | rpfpl_entry
        | rpfpl_type
    )?
;

rpfpl_description_null
:
    DESCRIPTION null_rest_of_line
;

rpfpl_entry
:
    ENTRY name = variable
    (
        rpfplte_action_null
        | rpfpltepe_greater_than_or_equal_null
        | rpfpltepe_less_than_or_equal_null
        | rpfpltep_network_null
        | rpfplte_prefix
    )?
;

rpfpl_type
:
    TYPE
    (
        rpfplt_ipv4
        | rpfplt_ipv6
    )
;

rpfplt_ipv4
:
    IPV4 rpfplt4_ipv4_entry?
;

rpfplt4_ipv4_entry
:
    IPV4_ENTRY name = variable
    (
        rpfplte_action_null
        | rpfplte_prefix
    )?
;

rpfplt_ipv6
:
    IPV6 rpfplt6_ipv6_entry?
;

rpfplt6_ipv6_entry
:
    IPV6_ENTRY name = variable
    (
        rpfplte_action_null
        | rpfplte_prefix
    )?
;

rpfplte_action_null
:
    ACTION (DENY | PERMIT)
;

rpfplte_prefix
:
    PREFIX
    (
        rpfpltep_entry
        | rpfpltep_ip_prefix_null
        | rpfpltep_network_null
    )
;

rpfpltep_entry
:
    ENTRY
    (
        rpfpltepe_greater_than_or_equal_null
        | rpfpltepe_less_than_or_equal_null
        | rpfpltepe_network_null
    )
;

rpfpltep_ip_prefix_null
:
    IP_PREFIX
;

rpfpltep_network_null
:
    NETWORK null_rest_of_line
;

rpfpltepe_greater_than_or_equal_null
:
    GREATER_THAN_OR_EQUAL uint8
;

rpfpltepe_less_than_or_equal_null
:
    LESS_THAN_OR_EQUAL uint8
;

rpfpltepe_network_null
:
    NETWORK null_rest_of_line
;

rpf_route_maps
:
    ROUTE_MAPS rpfrm_definition?
;

rpfrm_definition
:
    name = variable
    (
        rpfrm_description_null
        | rpfrm_entry
        | rpfrm_route_map
    )?
;

rpfrm_description_null
:
    DESCRIPTION null_rest_of_line
;

rpfrm_entry
:
    ENTRY seq = uint16 rpfrme_option?
;

rpfrm_route_map
:
    ROUTE_MAP seq = uint16 rpfrme_option?
;

rpfrme_option
:
    rpfrme_action_null
    | rpfrme_description_null
    | rpfrme_match
    | rpfrme_set
;

rpfrme_action_null
:
    ACTION (DENY | PERMIT)
;

rpfrme_description_null
:
    DESCRIPTION null_rest_of_line
;

rpfrme_match
:
    MATCH
    (
        rpfrmem_as_path_access_list_null
        | rpfrmem_community_null
        | rpfrmem_extended_community_null
        | rpfrmem_interface_null
        | rpfrmem_ipv4
        | rpfrmem_ipv6_null
        | rpfrmem_large_community_null
        | rpfrmem_local_preference_null
        | rpfrmem_metric_null
        | rpfrmem_origin_null
        | rpfrmem_peer_null
        | rpfrmem_prefix_list_null
        | rpfrmem_tag_null
    )
;

rpfrmem_as_path_access_list_null
:
    AS_PATH_ACCESS_LIST null_rest_of_line
;

rpfrmem_community_null
:
    COMMUNITY null_rest_of_line
;

rpfrmem_extended_community_null
:
    EXTENDED_COMMUNITY null_rest_of_line
;

rpfrmem_interface_null
:
    INTERFACE null_rest_of_line
;

rpfrmem_ipv4
:
    IPV4
    (
        rpfrmem4_address
        | rpfrmem4_next_hop_null
    )
;

rpfrmem4_address
:
    ADDRESS
    (
        rpfrmem4a_access_list_null
        | rpfrmem4a_prefix_list_null
    )
;

rpfrmem4a_access_list_null
:
    ACCESS_LIST null_rest_of_line
;

rpfrmem4a_prefix_list_null
:
    PREFIX_LIST null_rest_of_line
;

rpfrmem4_next_hop_null
:
    NEXT_HOP null_rest_of_line
;

rpfrmem_ipv6_null
:
    IPV6 null_rest_of_line
;

rpfrmem_large_community_null
:
    LARGE_COMMUNITY null_rest_of_line
;

rpfrmem_local_preference_null
:
    LOCAL_PREFERENCE null_rest_of_line
;

rpfrmem_metric_null
:
    METRIC null_rest_of_line
;

rpfrmem_origin_null
:
    ORIGIN null_rest_of_line
;

rpfrmem_peer_null
:
    PEER null_rest_of_line
;

rpfrmem_prefix_list_null
:
    PREFIX_LIST null_rest_of_line
;

rpfrmem_tag_null
:
    TAG null_rest_of_line
;

rpfrme_set
:
    SET
    (
        rpfrmes_aggregator_null
        | rpfrmes_aspath_exclude_null
        | rpfrmes_aspath_prepend_null
        | rpfrmes_atomic_aggregate_null
        | rpfrmes_community_null
        | rpfrmes_extended_community_null
        | rpfrmes_ipv4_null
        | rpfrmes_ipv6_null
        | rpfrmes_large_community_null
        | rpfrmes_local_preference_null
        | rpfrmes_metric_null
        | rpfrmes_origin_null
        | rpfrmes_originator_id_null
        | rpfrmes_overwrite_large_community_null
        | rpfrmes_overwrite_regular_community_null
        | rpfrmes_remove_large_community_null
        | rpfrmes_remove_regular_community_null
        | rpfrmes_tag_null
        | rpfrmes_weight_null
    )
;

rpfrmes_aggregator_null
:
    AGGREGATOR null_rest_of_line
;

rpfrmes_aspath_exclude_null
:
    ASPATH_EXCLUDE null_rest_of_line
;

rpfrmes_aspath_prepend_null
:
    ASPATH_PREPEND null_rest_of_line
;

rpfrmes_atomic_aggregate_null
:
    ATOMIC_AGGREGATE null_rest_of_line
;

rpfrmes_community_null
:
    COMMUNITY null_rest_of_line
;

rpfrmes_extended_community_null
:
    EXTENDED_COMMUNITY null_rest_of_line
;

rpfrmes_ipv4_null
:
    IPV4 null_rest_of_line
;

rpfrmes_ipv6_null
:
    IPV6 null_rest_of_line
;

rpfrmes_large_community_null
:
    LARGE_COMMUNITY null_rest_of_line
;

rpfrmes_local_preference_null
:
    LOCAL_PREFERENCE null_rest_of_line
;

rpfrmes_metric_null
:
    METRIC null_rest_of_line
;

rpfrmes_origin_null
:
    ORIGIN null_rest_of_line
;

rpfrmes_originator_id_null
:
    ORIGINATOR_ID null_rest_of_line
;

rpfrmes_overwrite_large_community_null
:
    OVERWRITE_LARGE_COMMUNITY null_rest_of_line
;

rpfrmes_overwrite_regular_community_null
:
    OVERWRITE_REGULAR_COMMUNITY null_rest_of_line
;

rpfrmes_remove_large_community_null
:
    REMOVE_LARGE_COMMUNITY null_rest_of_line
;

rpfrmes_remove_regular_community_null
:
    REMOVE_REGULAR_COMMUNITY null_rest_of_line
;

rpfrmes_tag_null
:
    TAG null_rest_of_line
;

rpfrmes_weight_null
:
    WEIGHT null_rest_of_line
;
