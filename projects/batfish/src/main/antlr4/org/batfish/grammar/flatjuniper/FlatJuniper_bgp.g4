parser grammar FlatJuniper_bgp;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

// Common stuff

send_path_count
:
  // 2-64
  uint8
;

//

b_advertise_external
:
   ADVERTISE_EXTERNAL
;

b_advertise_inactive
:
   ADVERTISE_INACTIVE
;

b_advertise_peer_as
:
   ADVERTISE_PEER_AS
;

b_authentication_algorithm
:
   AUTHENTICATION_ALGORITHM
   (
      AES_128_CMAC_96
      | HMAC_SHA_1_96
      | MD5
   )
;

b_authentication_key
:
   AUTHENTICATION_KEY key = secret_string
;

b_authentication_key_chain
:
   AUTHENTICATION_KEY_CHAIN name = junos_name
;

b_allow
:
   ALLOW
   (
      ip_prefix
      | ipv6_prefix
      | ALL
   )
;

b_as_override
:
   AS_OVERRIDE
;

b_bmp
:
   BMP
   (
      bbmp_route_monitoring
   )
;

bbmp_route_monitoring
:
   ROUTE_MONITORING
   (
      bbmprm_pre_policy
      | bbmprm_post_policy
      | bbmprm_rib_out
   )
;

bbmprm_pre_policy: PRE_POLICY;
bbmprm_post_policy: POST_POLICY EXCLUDE_NON_ELIGIBLE;
bbmprm_rib_out: RIB_OUT POST_POLICY;

b_cluster
:
   CLUSTER id = ip_address
;

b_common
:
   apply
   | b_advertise_external
   | b_advertise_inactive
   | b_advertise_peer_as
   | b_as_override
   | b_authentication_algorithm
   | b_authentication_key
   | b_authentication_key_chain
   | b_bmp
   | b_cluster
   | b_damping
   | b_description
   | b_disable_4byte_as
   | b_drop_path_attributes
   | b_enforce_first_as
   | b_export
   | b_family
   | b_import
   | b_local_address
   | b_local_as
   | b_multihop
   | b_multipath
   | b_no_client_reflect
   | b_null
   | b_passive
   | b_path_selection
   | b_peer_as
   | b_preference
   | b_remove_private
   | b_tcp_mss
   | b_type
;

b_damping
:
   DAMPING
;

b_description
:
   description
;

b_disable
:
   DISABLE
;

b_disable_4byte_as
:
   DISABLE_4BYTE_AS
;

b_drop_path_attributes
:
   DROP_PATH_ATTRIBUTES attr = dec
;

b_enable
:
   ENABLE
;

b_enforce_first_as
:
   ENFORCE_FIRST_AS
;

b_export
:
   EXPORT expr = policy_expression
;

b_family
:
   FAMILY
   (
      bf_evpn
      | bf_inet
      | bf_inet6
      | bf_null
      | bf_route_target
   ) bf_accepted_prefix_limit?
;

b_group
:
   GROUP name = junos_name
   (
      b_common
      | b_neighbor
      | b_allow
   )
;

b_import
:
   IMPORT expr = policy_expression
;

b_local_address
:
   LOCAL_ADDRESS
   (
      ip_address
      | ipv6_address
   )
;

b_local_as
:
   LOCAL_AS bl_number? bl_common*
;

b_multihop
:
   MULTIHOP
   (
      apply
      | bm_no_nexthop_change
      | bm_ttl
   )
;

b_multipath
:
   MULTIPATH MULTIPLE_AS?
;

b_neighbor
:
   NEIGHBOR
   (
      ip_address
      | ipv6_address
      | wildcard
   ) b_common
;

b_output_queue_priority
:
   OUTPUT_QUEUE_PRIORITY
   (
      boqp_defaults_null
      | boqp_queue_id
   )
;

boqp_defaults_null: DEFAULTS null_filler;
boqp_queue_id: bgp_priority_queue_id null_filler;

b_no_client_reflect
:
   NO_CLIENT_REFLECT
;

b_null
:
   (
      AUTHENTICATION_KEY
      | BFD_LIVENESS_DETECTION
      | GRACEFUL_RESTART
      | HOLD_TIME
      | KEEP
      | LOG_UPDOWN
      | MTU_DISCOVERY
      | OUT_DELAY
      | PRECISION_TIMERS
      | TRACEOPTIONS
   ) null_filler
;

b_passive
:
   PASSIVE
;

b_path_selection
:
   PATH_SELECTION
   (
      bps_always_compare_med
      | bps_external_router_id
   )
;

b_peer_as
:
   PEER_AS
   (
      apply
      | bpa_as
   )
;

b_preference
:
  PREFERENCE pref = dec
;

b_remove_private
:
   REMOVE_PRIVATE
   (
     ALL
     | NEAREST
     | REPLACE
     | NO_PEER_LOOP_CHECK
   )?
;

b_tcp_mss
:
   TCP_MSS dec
;

b_type
:
   TYPE
   (
      EXTERNAL
      | INTERNAL
   )
;

bf_accepted_prefix_limit
:
   ACCEPTED_PREFIX_LIMIT
   (
      (
         MAXIMUM max = dec
      )
      |
      (
         TEARDOWN limit_threshold = dec?
         (
            IDLE_TIMEOUT
            (
               idle_timeout = dec
               | FOREVER
            )
         )?
      )
   )*
;

bf_evpn
:
   EVPN SIGNALING
;

bf_inet
:
   INET
   (
      bfi_any
      | bfi_flow
      | bfi_labeled_unicast
      | bfi_multicast
      | bfi_unicast
   )
;

bf_inet6
:
   INET6
   (
      bfi6_any
      | bfi6_null
      | bfi6_unicast
   )
;

bf_null
:
   (
      INET_MDT
      | INET_MVPN
      | INET_VPN
      | INET6_VPN
      | L2VPN
   ) null_filler
;

bf_route_target
:
   ROUTE_TARGET
;


bfi_any
:
   ANY null_filler
;

bfi_flow
:
   FLOW null_filler
;

bfi_labeled_unicast
:
   LABELED_UNICAST null_filler
;

bfi_multicast
:
   MULTICAST null_filler
;

bfi_unicast
:
   UNICAST
   (
      apply
      | bfiu_add_path
      | bfiu_extended_nexthop_tunnel_null
      | bfiu_loops
      | bfiu_prefix_limit
      | bfiu_rib_group
      | bfiu_withdraw_priority
   )
;

bfi6_any
:
   ANY null_filler
;

bfi6_null
:
   (
      LABELED_UNICAST
      | MULTICAST
   ) null_filler
;

bfi6_unicast
:
   UNICAST
   (
      apply
      | bfi6u_add_path
      | bfi6u_extended_nexthop_tunnel_null
      | bfi6u_loops
      | bfi6u_prefix_limit
      | bfi6u_withdraw_priority
   )
;

bfi6u_add_path
:
   ADD_PATH
   (
      bfi6ua_receive
      | bfi6ua_send
   )
;

bfi6ua_receive
:
   RECEIVE
;

bfi6ua_send
:
  SEND
  (
    apply
    | bfi6uas_multipath
    | bfi6uas_path_count
    | bfi6uas_path_selection_mode
    | bfi6uas_prefix_policy
  )
;

bfi6uas_multipath
:
  MULTIPATH
;

bfi6uas_path_count
:
   PATH_COUNT count = send_path_count
;

bfi6uas_path_selection_mode
:
  PATH_SELECTION_MODE
  (
    ALL_PATHS
    | EQUAL_COST_PATHS
  )
;

bfi6uas_prefix_policy
:
   PREFIX_POLICY policy = junos_name
;

bfi6u_extended_nexthop_tunnel_null: EXTENDED_NEXTHOP_TUNNEL;

bfi6u_loops
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/loops-edit-protocols-bgp-family.html
   // 1-10
   LOOPS count = uint8
;

bfi6u_prefix_limit
:
   PREFIX_LIMIT null_filler
;

bfi6u_withdraw_priority: WITHDRAW_PRIORITY bgp_priority_queue_id;

bfiu_add_path
:
   ADD_PATH
   (
      bfiua_receive
      | bfiua_send
   )
;

bfiu_extended_nexthop_tunnel_null: EXTENDED_NEXTHOP_TUNNEL;

bfiu_loops
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/loops-edit-protocols-bgp-family.html
   // 1-10
   LOOPS count = uint8
;

bfiu_prefix_limit
:
   PREFIX_LIMIT null_filler
;

bfiu_rib_group
:
   RIB_GROUP name = junos_name
;

bfiu_withdraw_priority: WITHDRAW_PRIORITY bgp_priority_queue_id;

bfiua_receive
:
   RECEIVE
;

bfiua_send
:
  SEND
  (
    apply
    | bfiuas_multipath
    | bfiuas_path_count
    | bfiuas_path_selection_mode
    | bfiuas_prefix_policy
  )
;

bfiuas_multipath
:
  MULTIPATH
;

bfiuas_path_count
:
   PATH_COUNT count = send_path_count
;

bfiuas_path_selection_mode
:
  PATH_SELECTION_MODE
  (
    ALL_PATHS
    | EQUAL_COST_PATHS
  )
;

bfiuas_prefix_policy
:
   PREFIX_POLICY policy = junos_name
;

bl_alias
:
   ALIAS
;

bl_common
:
   bl_alias
   | bl_loops
   | bl_no_prepend_global_as
   | bl_private
;

bl_loops
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/loops-edit-protocols-bgp-family.html
   // 1-10
   LOOPS count = uint8
;

bl_no_prepend_global_as
:
   NO_PREPEND_GLOBAL_AS
;

bl_number
:
   asn = bgp_asn
;

bl_private
:
   PRIVATE
;

bm_no_nexthop_change
:
   NO_NEXTHOP_CHANGE
;

bm_ttl
:
   TTL dec
;

bpa_as
:
   asn = bgp_asn
;

bps_always_compare_med
:
   ALWAYS_COMPARE_MED
;

bps_external_router_id
:
   EXTERNAL_ROUTER_ID
;

p_bgp
:
   BGP
   (
      b_advertise_from_main_vpn_tables_null
      | b_bgp_error_tolerance_null
      | b_common
      | b_disable
      | b_enable
      | b_forwarding_context_null
      | b_group
      | b_neighbor
      | b_output_queue_priority
   )
;

// Protocol-wide config only
b_advertise_from_main_vpn_tables_null: ADVERTISE_FROM_MAIN_VPN_TABLES;
b_bgp_error_tolerance_null: BGP_ERROR_TOLERANCE;
b_forwarding_context_null: FORWARDING_CONTEXT name = junos_name;