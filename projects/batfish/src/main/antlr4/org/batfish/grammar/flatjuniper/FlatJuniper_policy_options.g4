parser grammar FlatJuniper_policy_options;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

metric_expression
:
   (
      METRIC
      | METRIC2
   ) MULTIPLIER multiplier = dec
   (
      OFFSET offset = dec
   )?
;

po_as_path
:
   AS_PATH name = junos_name regex = AS_PATH_REGEX
;

po_as_path_group
:
  AS_PATH_GROUP name = junos_name poapg_as_path
;

po_community
:
   COMMUNITY name = junos_name
   (
      poc_invert_match
      | poc_members
   )
;

po_condition
:
  CONDITION name = junos_name
  (
    apply
    | pocond_if_route_exists
  )
;

pocond_if_route_exists
:
  IF_ROUTE_EXISTS
  (
    pocondi_address_family
    | pocondiafi_prefix
    | pocondiafi_prefix6
    | pocondiafi_table
  )
;

pocondiafi_prefix: prefix = ip_prefix_default_32;

pocondiafi_prefix6: prefix = ipv6_prefix_default_128;

pocondiafi_table: TABLE name = junos_name;

pocondi_address_family: ADDRESS_FAMILY (
    pocondiaf_inet | pocondiaf_ccc
);

pocondiaf_inet: INET (pocondiafi_prefix | pocondiafi_table);

pocondiaf_ccc: CCC null_filler;

po_policy_statement
:
   POLICY_STATEMENT name = junos_name
   (
      pops_term
      | pops_common
   )
;

po_prefix_list
:
   PREFIX_LIST (name = junos_name | wildcard)
   (
      apply
      | poplt_apply_path
      | poplt_network
      | poplt_network6
   )
;

po_rtf_prefix_list
:
   RTF_PREFIX_LIST (name = junos_name | wildcard) portfplt_prefix
;

po_tunnel_attribute
:
   TUNNEL_ATTRIBUTE name = junos_name
   (
      pota_remote_end_point
      | pota_tunnel_type
   )
;

pota_remote_end_point: REMOTE_END_POINT ip_address;

pota_tunnel_type: TUNNEL_TYPE IPIP;

poapg_as_path
:
  AS_PATH name = junos_name regex = AS_PATH_REGEX
;

poc_invert_match
:
   INVERT_MATCH
;

poc_members
:
   MEMBERS member = poc_members_member
;

poc_members_member
:
  literal_or_regex_community
  | sc_named
;

literal_or_regex_community
:
  LITERAL_OR_REGEX_COMMUNITY
;

poplt_apply_path
:
   APPLY_PATH path = DOUBLE_QUOTED_STRING
;

poplt_network
:
   network = ip_prefix_default_32
;

poplt_network6
:
   network = ipv6_prefix_default_128
;

portfplt_prefix
:
    prefix = rtf_prefix
;

rtf_prefix:
    // TODO: are IP address variants valid here?
    asn = uint32
    COLON rt_hi = uint32 COLON rt_lo = uint32
    FORWARD_SLASH length = uint8
;

pops_common
:
   apply
   | pops_from
   | pops_then
   | pops_to
;

pops_from
:
   FROM
   (
      popsf_area
      | popsf_as_path
      | popsf_as_path_group
      | popsf_color
      | popsf_community
      | popsf_community_count
      | popsf_condition
      | popsf_external
      | popsf_family
      | popsf_instance
      | popsf_interface
      | popsf_level
      | popsf_local_preference
      | popsf_metric
      | popsf_neighbor
      | popsf_next_hop
      | popsf_origin
      | popsf_policy
      | popsf_prefix_list
      | popsf_prefix_list_filter
      | popsf_protocol
      | popsf_rib
      | popsf_route_filter
      | popsf_route_type
      | popsf_rtf_prefix_list
      | popsf_source_address_filter
      | popsf_tag
      | popsf_tag2
   )
;

pops_term
:
   TERM name = junos_name pops_common
;

pops_then
:
   THEN popst_common
;

pops_to
:
   TO
   (
      popsto_level
      | popsto_rib
   )
;

popsf_area
:
   AREA area = IP_ADDRESS
;

popsf_as_path
:
   AS_PATH
   (
      name = junos_name
   )?
;

popsf_as_path_group
:
   AS_PATH_GROUP name = junos_name
;

popsf_color
:
   COLOR color = uint32
;

popsf_community
:
   COMMUNITY name = junos_name
;

community_count_number: uint16; // todo: what is the actual number?

popsf_community_count: COMMUNITY_COUNT n = community_count_number (ORLOWER | ORHIGHER)?;

popsf_condition: CONDITION name = junos_name;

popsf_family
:
   FAMILY
   (
      EVPN
      | INET
      | INET_MDT
      | INET_MVPN
      | INET_VPN
      | INET6
      | INET6_MVPN
      | INET6_VPN
      | ISO
      | ISO_VPN
      | ROUTE_TARGET
      | TRAFFIC_ENGINEERING
   )
;

popsf_instance
:
   INSTANCE name = junos_name
;

popsf_interface
:
   INTERFACE id = interface_id
;

popsf_level
:
   LEVEL dec
;

popsf_local_preference
:
   LOCAL_PREFERENCE
   (
      localpref = uint32
      | apply_groups
   )
;

popsf_metric
:
   METRIC
   (
      metric = dec
      | apply_groups
   )
;

popsf_neighbor
:
   NEIGHBOR
   (
      v4 = ip_address
      | v6 = ipv6_address
   )
;

popsf_next_hop
:
   NEXT_HOP
   (
      v4 = ip_address
      | v6 = ipv6_address
   )
;

popsf_origin
:
   ORIGIN origin_type
;

popsf_policy
:
   POLICY expr = policy_expression
;

popsf_prefix_list
:
   PREFIX_LIST name = junos_name
;

popsf_prefix_list_filter
:
   PREFIX_LIST_FILTER name = junos_name
   (
      popsfpl_exact
      | popsfpl_longer
      | popsfpl_orlonger
   )
;

popsf_protocol
:
   PROTOCOL
   (
     ACCESS_INTERNAL
     | AGGREGATE
     | BGP
     | DIRECT
     | EVPN
     | ISIS
     | LDP
     | LOCAL
     | OSPF
     | OSPF3
     | RSVP
     | STATIC
   )
;

popsf_rib
:
   RIB name = rib_name
;

popsf_route_filter
:
   ROUTE_FILTER
   (
      prefix = ip_prefix_default_32
      | prefix6 = ipv6_prefix_default_128
   ) popsfrf_common then = popsfrf_then?
;

popsf_route_type
:
   ROUTE_TYPE
   (
      EXTERNAL
      | INTERNAL
   )
;


ospf_type
:
   // 1 or 2
   uint8
;

popsf_external
:
   EXTERNAL (TYPE ospf_type)?
;

popsf_rtf_prefix_list
:
   RTF_PREFIX_LIST name = junos_name
;

popsf_source_address_filter
:
   SOURCE_ADDRESS_FILTER
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) popsfrf_common
;

popsf_tag
:
   TAG uint32
;

popsf_tag2
:
   TAG2 uint32
;

popsfpl_exact
:
   EXACT
;

popsfpl_longer
:
   LONGER
;

popsfpl_orlonger
:
   ORLONGER
;

popsfrf_common
:
   popsfrf_address_mask
   | popsfrf_exact
   | popsfrf_longer
   | popsfrf_orlonger
   | popsfrf_prefix_length_range
   | popsfrf_through
   | popsfrf_upto
;

popsfrf_address_mask
:
   ADDRESS_MASK
   (
      ip_address
      | ipv6_address
   )
;

popsfrf_exact
:
   EXACT
;

popsfrf_longer
:
   LONGER
;

popsfrf_orlonger
:
   ORLONGER
;

popsfrf_prefix_length_range
:
   PREFIX_LENGTH_RANGE FORWARD_SLASH low = dec DASH FORWARD_SLASH high = dec
;

popsfrf_then
:
   popst_common
;

popsfrf_through
:
   THROUGH
   (
      prefix = ip_prefix_default_32
      | prefix6 = ipv6_prefix_default_128
   )
;

popsfrf_upto
:
   UPTO FORWARD_SLASH high = dec
;

popst_accept
:
   ACCEPT
;

popst_as_path_expand
:
  AS_PATH_EXPAND
  (
    LAST_AS (COUNT count = as_path_expand_count)?
    | asns += bgp_asn+
  )
;

as_path_expand_count
:
  // 1-32
  uint8
;

popst_as_path_prepend
:
   AS_PATH_PREPEND bgp_asn+
;

popst_bgp_output_queue_priority
:
  BGP_OUTPUT_QUEUE_PRIORITY bgp_priority_queue_id
;

popst_color
:
   COLOR
   (
      apply
      | (ADD | SUBTRACT)? uint32
   )
;

popst_color2
:
   COLOR2
   (
      apply
      | (ADD | SUBTRACT)? uint32
   )
;

popst_common
:
   popst_accept
   | popst_as_path_expand
   | popst_as_path_prepend
   | popst_bgp_output_queue_priority
   | popst_color
   | popst_color2
   | popst_community_add
   | popst_community_delete
   | popst_community_set
   | popst_cos_next_hop_map
   | popst_default_action_accept
   | popst_default_action_reject
   | popst_external
   | popst_forwarding_class
   | popst_install_nexthop
   | popst_local_preference
   | popst_metric
   | popst_metric_add
   | popst_metric_expression
   | popst_metric_igp
   | popst_metric2
   | popst_metric2_expression
   | popst_multipath_resolve
   | popst_next_hop
   | popst_next_policy
   | popst_next_term
   | popst_null
   | popst_origin
   | popst_preference
   | popst_priority
   | popst_reject
   | popst_tag
   | popst_tag2
   | popst_tunnel_attribute
;

popst_community_add
:
   COMMUNITY ADD name = junos_name
;

popst_community_delete
:
   COMMUNITY DELETE name = junos_name
;

popst_community_set
:
   COMMUNITY SET name = junos_name
;

popst_cos_next_hop_map
:
   COS_NEXT_HOP_MAP name = junos_name
;

popst_default_action_accept
:
   DEFAULT_ACTION ACCEPT
;

popst_default_action_reject
:
   DEFAULT_ACTION REJECT
;

popst_external
:
   EXTERNAL TYPE dec
;

popst_forwarding_class
:
   FORWARDING_CLASS junos_name
;

popst_install_nexthop
:
   INSTALL_NEXTHOP null_filler
;

popst_local_preference
:
   LOCAL_PREFERENCE
   (
      (ADD | SUBTRACT)? localpref = uint32
      | apply_groups
   )
;

popst_metric
:
   METRIC
   (
      metric = dec
      | apply_groups
   )
;

popst_metric_add
:
   METRIC ADD metric = dec
;

popst_metric2
:
   METRIC2 metric2 = dec
;

popst_metric_expression
:
   METRIC EXPRESSION metric_expression
;

popst_metric_igp
:
   METRIC IGP offset = dec?
;

popst_metric2_expression
:
   METRIC2 EXPRESSION metric_expression
;

popst_multipath_resolve
:
   MULTIPATH_RESOLVE
;

popst_next_hop
:
   NEXT_HOP
   (
      popstnh_discard
      | popstnh_ipv4
      | popstnh_ipv6
      | popstnh_peer_address
      | popstnh_reject
      | popstnh_self
   )
;

popstnh_discard
:
   DISCARD
;

popstnh_ipv4
:
   addr = ip_address
;

popstnh_ipv6
:
   addr6 = ipv6_address
;

popstnh_peer_address
:
   PEER_ADDRESS
;

popstnh_reject
:
   REJECT
;

popstnh_self
:
   SELF
;

popst_next_policy
:
   NEXT POLICY
;

popst_next_term
:
   NEXT TERM
;

popst_null
:
   LOAD_BALANCE null_filler
;

popst_origin
:
   ORIGIN
   (
      EGP
      | IGP
      | INCOMPLETE
   )
;

popst_preference
:
   PREFERENCE preference = dec
;

popst_priority
:
   PRIORITY
   (
      HIGH
      | LOW
   )
;

popst_reject
:
   REJECT
;

popst_tag
:
   TAG uint32
;

popst_tag2
:
   TAG2 uint32
;

popst_tunnel_attribute
:
   TUNNEL_ATTRIBUTE
   (
      popstta_remove
      | popstta_set
   )
;

popstta_remove: REMOVE (ALL | name = junos_name);

popstta_set: SET name = junos_name;

popsto_level
:
   LEVEL dec
;

popsto_rib
:
   RIB rib_name
;

s_policy_options
:
   POLICY_OPTIONS
   (
      apply
      | po_as_path
      | po_as_path_group
      | po_community
      | po_condition
      | po_policy_statement
      | po_prefix_list
      | po_rtf_prefix_list
      | po_tunnel_attribute
   )
;
