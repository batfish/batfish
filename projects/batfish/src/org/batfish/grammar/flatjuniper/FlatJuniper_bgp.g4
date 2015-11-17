parser grammar FlatJuniper_bgp;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

apsendt_path_count
:
   PATH_COUNT count = DEC
;

apsendt_prefix_policy
:
   PREFIX_POLICY policy = variable
;

bfi6t_any
:
   ANY s_null_filler
;

bfi6t_null
:
   LABELED_UNICAST s_null_filler
;

bfi6t_unicast
:
   UNICAST bfi6t_unicast_tail
;

bfi6t_unicast_tail
:
//intentional blank

   | bfi6ut_prefix_limit
;

bfi6ut_prefix_limit
:
   PREFIX_LIMIT s_null_filler
;

bfit_flow
:
   FLOW s_null_filler
;

bfit_labeled_unicast
:
   LABELED_UNICAST s_null_filler
;

bfit_any
:
   ANY s_null_filler
;

bfit_multicast
:
   MULTICAST s_null_filler
;

bfit_unicast
:
   UNICAST bfit_unicast_tail
;

bfit_unicast_tail
:
//intentional blank

   | bfiut_add_path
   | bfiut_prefix_limit
   | bfiut_rib_group
;

bfiuapt_receive
:
   RECEIVE
;

bfiuapt_send
:
   SEND bfiuapt_send_tail
;

bfiuapt_send_tail
:
   apsendt_path_count
   | apsendt_prefix_policy
;

bfiut_add_path
:
   ADD_PATH bfiut_add_path_tail
;

bfiut_add_path_tail
:
   bfiuapt_receive
   | bfiuapt_send
;

bfiut_prefix_limit
:
   PREFIX_LIMIT s_null_filler
;

bfiut_rib_group
:
   RIB_GROUP name = variable
;

bft_inet
:
   INET bft_inet_tail
;

bft_inet_tail
:
   bfit_any
   | bfit_flow
   | bfit_labeled_unicast
   | bfit_multicast
   | bfit_unicast
;

bft_inet6
:
   INET6 bft_inet6_tail
;

bft_inet6_tail
:
   bfi6t_any
   | bfi6t_null
   | bfi6t_unicast
;

bft_null
:
   (
      INET_MDT
      | INET_MVPN
      | INET_VPN
      | INET6_VPN
      | L2VPN
   ) s_null_filler
;

bmt_no_nexthop_change
:
   NO_NEXTHOP_CHANGE
;

bmt_ttl
:
   TTL DEC
;

bpast_as
:
   as = DEC
;

bt_advertise_inactive
:
   ADVERTISE_INACTIVE
;

bt_advertise_peer_as
:
   ADVERTISE_PEER_AS
;

bt_apply_groups
:
   s_apply_groups
;

bt_as_override
:
   AS_OVERRIDE
;

bt_cluster
:
   CLUSTER IP_ADDRESS
;

bt_common
:
   bt_advertise_inactive
   | bt_advertise_peer_as
   | bt_apply_groups
   | bt_as_override
   | bt_cluster
   | bt_damping
   | bt_description
   | bt_disable_4byte_as
   | bt_export
   | bt_family
   | bt_import
   | bt_local_address
   | bt_local_as
   | bt_multihop
   | bt_multipath
   | bt_no_client_reflect
   | bt_null
   | bt_passive
   | bt_path_selection
   | bt_peer_as
   | bt_remove_private
   | bt_tcp_mss
   | bt_type
;

bt_damping
:
   DAMPING
;

bt_description
:
   s_description
;

bt_disable_4byte_as
:
   DISABLE_4BYTE_AS
;

bt_enable
:
   ENABLE
;

bt_export
:
   EXPORT expr = policy_expression
;

bt_family
:
   FAMILY bt_family_tail
;

bt_family_tail
:
   bft_inet
   | bft_inet6
   | bft_null
;

bt_group
:
   GROUP
   (
      name = variable
      | WILDCARD
   ) bt_group_tail
;

bt_group_tail
:
// intentional blank

   | bt_common
   | bt_neighbor
;

bt_import
:
   IMPORT expr = policy_expression
;

bt_local_address
:
   LOCAL_ADDRESS
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   )?
;

bt_local_as
:
   LOCAL_AS bt_local_as_tail
;

bt_local_as_tail
:
   last_number? last_common*
;

bt_multihop
:
   MULTIHOP bt_multihop_tail
;

bt_multihop_tail
:
// intentional blank

   | bmt_no_nexthop_change
   | bmt_ttl
;

bt_multipath
:
   MULTIPATH MULTIPLE_AS?
;

bt_neighbor
:
   NEIGHBOR
   (
      IP_ADDRESS
      | IPV6_ADDRESS
      | WILDCARD
   ) bt_neighbor_tail
;

bt_neighbor_tail
:
// intentional blank

   | bt_common
;

bt_no_client_reflect
:
   NO_CLIENT_REFLECT
;

bt_null
:
   (
      AUTHENTICATION_KEY
      | BFD_LIVENESS_DETECTION
      | HOLD_TIME
      | KEEP
      | LOG_UPDOWN
      | MTU_DISCOVERY
      | OUT_DELAY
      | PRECISION_TIMERS
      | TRACEOPTIONS
   ) s_null_filler
;

bt_passive
:
   PASSIVE
;

bt_path_selection
:
   PATH_SELECTION bt_path_selection_tail
;

bt_path_selection_tail
:
   pst_always_compare_med
;

bt_peer_as
:
   PEER_AS bt_peer_as_tail
;

bt_peer_as_tail
:
// intentional blank

   | bpast_as
;

bt_remove_private
:
   'remove-private'
;

bt_tcp_mss
:
   TCP_MSS DEC
;

bt_type
:
   TYPE
   (
      EXTERNAL
      | INTERNAL
   )
;

last_alias
:
   ALIAS
;

last_common
:
   last_alias
   | last_loops
   | last_private
;

last_loops
:
   LOOPS DEC
;

last_number
:
   as = DEC
;

last_private
:
   PRIVATE
;

pst_always_compare_med
:
   ALWAYS_COMPARE_MED
;

s_protocols_bgp
:
   BGP s_protocols_bgp_tail
;

s_protocols_bgp_tail
:
   bt_common
   | bt_enable
   | bt_group
   | bt_neighbor
;