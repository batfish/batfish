parser grammar Arista_bgp;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

eos_neighbor_id
:
  v4 = IP_ADDRESS
  | v6 = IPV6_ADDRESS
  | pg = variable
;

router_bgp_stanza
:
   BGP
   (
      procnum = bgp_asn
   )? NEWLINE
   eos_router_bgp_tail*
;

eos_as_range
:
  lo = bgp_asn
  (
    DASH hi = bgp_asn
  )?
;

eos_as_range_list
:
  aslist += eos_as_range
  (
    COMMA aslist += eos_as_range
  )*
;

eos_bgp_community
:
  EXTENDED
  | STANDARD
  //    TODO: support for link-bandwidth
  //    | LINK_BANDWIDTH
  //      (
  //        AGGREGATE "0.0-4294967295.0 or nn.nn(K|M|G)  Reference link speed in bits/second"
  //        | DIVIDE (EQUAL | RATIO)
  //      )
;

eos_router_bgp_tail
:
  eos_rb_address_family
  | eos_rb_inner
  | eos_rb_monitoring
  | eos_rb_no
  | eos_rb_vlan
  | eos_rb_vlan_aware_bundle
  | eos_rb_vrf
;

eos_rb_address_family
:
  ADDRESS_FAMILY
  (
    eos_rb_af_evpn
    | eos_rb_af_flow_spec
    | eos_rb_af_ipv4
    | eos_rb_af_ipv6
    | eos_rb_af_vpn_v4
    | eos_rb_af_vpn_v6
  )
;

eos_rb_af_flow_spec
:
  FLOW_SPEC
  (
    eos_rb_af_flow_spec_ipv4
    | eos_rb_af_flow_spec_ipv6
  )
;

eos_rb_af_flow_spec_ipv4
:
  IPV4 NEWLINE
  (
    eos_rbaffs4_default
    | eos_rbaffs4_neighbor
    | eos_rbaffs4_no
  )*
;

eos_rbaffs4_default
:
  DEFAULT
  eos_rbaffs4_default_neighbor
;

eos_rbaffs4_default_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafdnc_activate
  )
;

eos_rbaffs4_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
  )
;

eos_rbaffs4_no
:
  NO
  eos_rbaffs4no_neighbor
;

eos_rbaffs4no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
  )
;

eos_rb_af_flow_spec_ipv6
:
  IPV6 NEWLINE
  (
    eos_rbaffs6_default
    | eos_rbaffs6_neighbor
    | eos_rbaffs6_no
  )*
;

eos_rbaffs6_default
:
  DEFAULT
  eos_rbaffs6_default_neighbor
;

eos_rbaffs6_default_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafdnc_activate
  )
;

eos_rbaffs6_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
  )
;

eos_rbaffs6_no
:
  NO
  eos_rbaffs6no_neighbor
;

eos_rbaffs6no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
  )
;

eos_rb_af_ipv4
:
  IPV4
  (
    eos_rb_af_ipv4_unicast
    | eos_rb_af_ipv4_labeled_unicast
    | eos_rb_af_ipv4_multicast
    | eos_rb_af_ipv4_sr_te
  )
;

eos_rb_af_ipv4_unicast
:
  NEWLINE
  (
    eos_rbafipv4u_bgp
    | eos_rbafipv4u_default
//    | eos_rbafipv4u_graceful_restart
    | eos_rbafipv4u_neighbor
    | eos_rbafipv4u_next_hop
    | eos_rbafipv4u_no
    | eos_rbafipv4u_network
//    | eos_rbafipv4u_redistribute
  )*
;

eos_rbafipv4u_bgp
:
  BGP
  (
    eos_rb_af_bgp_common
    | eos_rbafipv4ub_next_hop
    | eos_rbafipv4ub_redistribute_internal
    | eos_rbafipv4ub_route
  )
;

eos_rbafipv4ub_next_hop
:
  NEXT_HOP ADDRESS_FAMILY IPV6 NEWLINE
;

eos_rbafipv4ub_redistribute_internal
:
  REDISTRIBUTE_INTERNAL NEWLINE
;

eos_rbafipv4ub_route
:
  ROUTE INSTALL name = variable NEWLINE
;

eos_rbafipv4u_default
:
  DEFAULT
  (
    eos_rbafipv4ud_neighbor
  )
;

eos_rbafipv4ud_neighbor
:
  NEIGHBOR nid = eos_neighbor_id eos_rb_af_default_neighbor_common
;

eos_rbafipv4u_neighbor
:
  NEIGHBOR nid = eos_neighbor_id eos_rb_af_neighbor_common
;

eos_rbafipv4u_next_hop
:
  NEXT_HOP RESOLUTION RIBS TUNNEL_RIB SYSTEM_TUNNEL_RIB SYSTEM_UNICAST_RIB NEWLINE
;

eos_rbafipv4u_no
:
  NO
  (
    eos_rbafipv4u_no_bgp
    | eos_rbafipv4u_no_neighbor
  )
;

eos_rbafipv4u_no_bgp
:
  BGP
  (
    eos_rb_af_no_bgp_common
    | eos_rbafipv4u_no_bgp_next_hop
  )
;

eos_rbafipv4u_no_bgp_next_hop
:
  NEXT_HOP ADDRESS_FAMILY IPV6 NEWLINE
;

eos_rbafipv4u_no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id eos_rb_af_no_neighbor_common
;

eos_rbafipv4u_network
:
  NETWORK
  (
    eos_rbi_network4
    | eos_rbi_network6
  )
;

eos_rb_af_ipv4_labeled_unicast
:
  LABELED_UNICAST NEWLINE
  (
    eos_rbafipv4labu_bgp
    | eos_rbafipv4labu_default
    | eos_rbafipv4labu_neighbor
    | eos_rbafipv4labu_no
  )*
;

eos_rbafipv4labu_bgp
:
  BGP
  (
    eos_rbafbc_additional_paths
    | eos_rbafbc_next_hop_unchanged
  )
;

eos_rbafipv4labu_default
:
  DEFAULT
  eos_rbafipv4labud_neighbor
;

eos_rbafipv4labud_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  eos_rbafdnc_activate
;

eos_rbafipv4labu_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
  )
;

eos_rbafipv4labu_no
:
  NO
  (
    eos_rbafipv4labuno_bgp
    | eos_rbafipv4labuno_neighbor
  )
;

eos_rbafipv4labuno_bgp
:
  BGP
  (
     eos_rbafnobc_additional_paths
     | eos_rbafnobc_next_hop_unchanged
  )
;

eos_rbafipv4labuno_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
    | eos_rbafnonc_additional_paths
    | eos_rbafipv4labunon_next_hop_self
    | eos_rbafnonc_next_hop_unchanged
    | eos_rbafnonc_route_map
  )
;

eos_rbafipv4labunon_next_hop_self
:
  NEXT_HOP_SELF SOURCE_INTERFACE NEWLINE
;

eos_rb_af_ipv4_multicast
:
  MULTICAST NEWLINE
  (
    eos_rbafipv4m_bgp
    | eos_rbafipv4m_default
    | eos_rbafipv4m_neighbor
    | eos_rbafipv4m_no
  )*
;

eos_rbafipv4m_bgp
:
  BGP
  (
    eos_rbafbc_additional_paths
    | eos_rbafbc_next_hop_unchanged
  )
;

eos_rbafipv4m_default
:
  DEFAULT
  (
    eos_rbafipv4md_neighbor
  )
;

eos_rbafipv4md_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafdnc_activate
  )
;

eos_rbafipv4m_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
  )
;

eos_rbafipv4m_no
:
  NO
  (
    eos_rbafipv4m_no_bgp
    | eos_rbafipv4m_no_neighbor
  )
;

eos_rbafipv4m_no_bgp
:
  BGP
  (
    eos_rbafnobc_additional_paths
    | eos_rbafnobc_next_hop_unchanged
  )
;

eos_rbafipv4m_no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
    | eos_rbafnonc_additional_paths
    | eos_rbafnonc_next_hop_unchanged
    | eos_rbafnonc_route_map
  )
;

eos_rb_af_ipv4_sr_te
 :
   SR_TE NEWLINE
   (
     eos_rbafipv4srte_default
     | eos_rbafipv4srte_neighbor
     | eos_rbafipv4srte_no
   )*
 ;

 eos_rbafipv4srte_default
 :
   DEFAULT
   (
     eos_rbafipv4srte_default_neighbor
   )
 ;

 eos_rbafipv4srte_default_neighbor
 :
   NEIGHBOR nid = eos_neighbor_id
   (
     eos_rbafdnc_activate
   )
 ;

 eos_rbafipv4srte_neighbor
 :
   NEIGHBOR nid = eos_neighbor_id
   (
     eos_rbafnc_activate
     | eos_rbafnc_route_map
   )
 ;

 eos_rbafipv4srte_no
 :
   NO
   eos_rbafipv4srte_no_neighbor
 ;

 eos_rbafipv4srte_no_neighbor
 :
   NEIGHBOR nid = eos_neighbor_id
   (
     eos_rbafnonc_activate
     | eos_rbafnonc_route_map
   )
 ;

eos_rb_af_ipv6
:
  IPV6
  (
    eos_rb_af_ipv6_labeled_unicast
    | eos_rb_af_ipv6_multicast
    | eos_rb_af_ipv6_sr_te
    | eos_rb_af_ipv6_unicast
  )
;

eos_rb_af_ipv6_labeled_unicast
:
  LABELED_UNICAST NEWLINE
  (
    eos_rbafipv6labu_bgp
    | eos_rbafipv6labu_default
    | eos_rbafipv6labu_neighbor
    | eos_rbafipv6labu_no
  )*
;

eos_rbafipv6labu_bgp
:
  BGP
  (
    eos_rbafbc_additional_paths
    | eos_rbafbc_next_hop_unchanged
  )
;

eos_rbafipv6labu_default
:
  DEFAULT
  eos_rbafipv6labud_neighbor
;

eos_rbafipv6labud_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  eos_rbafdnc_activate
;

eos_rbafipv6labu_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
  )
;

eos_rbafipv6labu_no
:
  NO
  (
    eos_rbafipv6labuno_bgp
    | eos_rbafipv6labuno_neighbor
  )
;

eos_rbafipv6labuno_bgp
:
  BGP
  (
     eos_rbafnobc_additional_paths
     | eos_rbafnobc_next_hop_unchanged
  )
;

eos_rbafipv6labuno_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
    | eos_rbafnonc_additional_paths
    | eos_rbafipv6labunon_next_hop_self
    | eos_rbafnonc_next_hop_unchanged
    | eos_rbafnonc_route_map
  )
;

eos_rbafipv6labunon_next_hop_self
:
  NEXT_HOP_SELF SOURCE_INTERFACE NEWLINE
;

eos_rb_af_ipv6_multicast
:
  MULTICAST NEWLINE
  (
    eos_rbafipv6m_bgp
    | eos_rbafipv6m_default
    | eos_rbafipv6m_neighbor
    | eos_rbafipv6m_no
  )*
;

eos_rbafipv6m_bgp
:
  BGP
  (
    eos_rbafbc_additional_paths
    | eos_rbafbc_next_hop_unchanged
  )
;

eos_rbafipv6m_default
:
  DEFAULT
  (
    eos_rbafipv6md_neighbor
  )
;

eos_rbafipv6md_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafdnc_activate
  )
;

eos_rbafipv6m_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
  )
;

eos_rbafipv6m_no
:
  NO
  (
    eos_rbafipv6m_no_bgp
    | eos_rbafipv6m_no_neighbor
  )
;

eos_rbafipv6m_no_bgp
:
  BGP
  (
    eos_rbafnobc_additional_paths
    | eos_rbafnobc_next_hop_unchanged
  )
;

eos_rbafipv6m_no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
    | eos_rbafnonc_additional_paths
    | eos_rbafnonc_next_hop_unchanged
    | eos_rbafnonc_route_map
  )
;

eos_rb_af_ipv6_sr_te
 :
   SR_TE NEWLINE
   (
     eos_rbafipv6srte_default
     | eos_rbafipv6srte_neighbor
     | eos_rbafipv6srte_no
   )*
 ;

 eos_rbafipv6srte_default
 :
   DEFAULT
   (
     eos_rbafipv6srte_default_neighbor
   )
 ;

 eos_rbafipv6srte_default_neighbor
 :
   NEIGHBOR nid = eos_neighbor_id
   (
     eos_rbafdnc_activate
   )
 ;

 eos_rbafipv6srte_neighbor
 :
   NEIGHBOR nid = eos_neighbor_id
   (
     eos_rbafnc_activate
     | eos_rbafnc_route_map
   )
 ;

 eos_rbafipv6srte_no
 :
   NO
   eos_rbafipv6srte_no_neighbor
 ;

 eos_rbafipv6srte_no_neighbor
 :
   NEIGHBOR nid = eos_neighbor_id
   (
     eos_rbafnonc_activate
     | eos_rbafnonc_route_map
   )
 ;

eos_rb_af_ipv6_unicast
:
  NEWLINE
  (
    eos_rbafipv6u_bgp
    | eos_rbafipv6u_default
//    | eos_rbafipv6u_graceful_restart
    | eos_rbafipv6u_neighbor
//    | eos_rbafipv6u_network
    | eos_rbafipv6u_next_hop
    | eos_rbafipv6u_no
//    | eos_rbafipv64u_redistribute
  )*
;

eos_rbafipv6u_bgp
:
  BGP eos_rb_af_bgp_common
;

eos_rbafipv6u_default
:
  DEFAULT
  (
    eos_rbafipv6ud_neighbor
  )
;

eos_rbafipv6ud_neighbor
:
  NEIGHBOR nid = eos_neighbor_id eos_rb_af_default_neighbor_common
;

eos_rbafipv6u_neighbor
:
  NEIGHBOR nid = eos_neighbor_id eos_rb_af_neighbor_common
;

eos_rbafipv6u_next_hop
:
  NEXT_HOP
  (
    eos_rbafipv6u_next_hop_sixpe
    | eos_rbafipv6u_next_hop_resolution
  )
;

eos_rbafipv6u_next_hop_sixpe
:
  SIXPE RESOLUTION RIBS TUNNEL_RIB SYSTEM_TUNNEL_RIB NEWLINE
;

eos_rbafipv6u_next_hop_resolution
:
  RESOLUTION RIBS TUNNEL_RIB SYSTEM_TUNNEL_RIB SYSTEM_UNICAST_RIB NEWLINE
;

eos_rbafipv6u_no
:
  NO
  (
    eos_rbafipv6u_no_bgp
    | eos_rbafipv6u_no_neighbor
  )
;

eos_rbafipv6u_no_bgp
:
  BGP eos_rb_af_no_bgp_common
;

eos_rbafipv6u_no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id eos_rb_af_no_neighbor_common
;

eos_rb_af_evpn
:
  EVPN NEWLINE
  (
    eos_rb_af_evpn_bgp
    | eos_rb_af_evpn_default
    | eos_rb_af_evpn_graceful_restart
    | eos_rb_af_evpn_host_flap
    | eos_rb_af_evpn_neighbor
    | eos_rb_af_evpn_no
  )*
;

eos_rb_af_evpn_bgp
:
  BGP
  (
    eos_rbafbc_additional_paths
    | eos_rbafbc_next_hop_unchanged
  )
;

eos_rb_af_evpn_default
:
  DEFAULT eos_rb_afed_neighbor
;

eos_rb_afed_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafdnc_activate
  )
;

eos_rb_af_evpn_graceful_restart
:
  GRACEFUL_RESTART NEWLINE
;

eos_rb_af_evpn_host_flap
:
  HOST_FLAP null_rest_of_line
;

eos_rb_af_evpn_neighbor
:
  NEIGHBOR
  (
    eos_rb_af_evpn_neighbor_default
    | eos_rb_af_evpn_neighbor_nid
  )
;

eos_rb_af_evpn_neighbor_default
:
  DEFAULT ENCAPSULATION VXLAN NEWLINE
;

eos_rb_af_evpn_neighbor_nid
:
  nid = eos_neighbor_id
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_graceful_restart
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
  )
;

eos_rb_af_evpn_no:
  NO
  (
    eos_rb_af_evpn_no_bgp
    | eos_rb_af_evpn_no_neighbor
    | eos_rb_af_evpn_no_next_hop
  )
;

eos_rb_af_evpn_no_bgp
:
  BGP
  (
    eos_rbafnobc_additional_paths
    | eos_rbafnobc_next_hop_unchanged
  )
;

eos_rb_af_evpn_no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_additional_paths
    | eos_rbafnonc_activate
    | eos_rbafnonc_next_hop_unchanged
    | eos_rbafnonc_route_map
  )
;

eos_rb_af_evpn_no_next_hop
:
  NEXT_HOP RESOLUTION DISABLED NEWLINE
;

eos_rb_af_vpn_v4
:
  VPN_IPV4 NEWLINE
  (
    eos_rbafvpn4_bgp
    | eos_rbafvpn4_default
    | eos_rbafvpn4_neighbor
    | eos_rbafvpn4_next_hop
    | eos_rbafvpn4_no
  )*
;

eos_rbafvpn4_bgp
:
  BGP
  (
    eos_rbafbc_additional_paths
    | eos_rbafbc_next_hop_unchanged
  )
;

eos_rbafvpn4_default
:
  DEFAULT
  eos_rbafvpn4d_neighbor
;

eos_rbafvpn4d_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafdnc_activate
  )
;
eos_rbafvpn4_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
  )
;

eos_rbafvpn4_next_hop
:
  NEXT_HOP RESOLUTION RIBS TUNNEL_RIB SYSTEM_TUNNEL_RIB SYSTEM_CONNECTED NEWLINE
;

eos_rbafvpn4_no
:
  NO
  (
    eos_rbafvpn4no_bgp
    | eos_rbafvpn4no_mpls
    | eos_rbafvpn4no_neighbor
    | eos_rbafvpn4no_next_hop
  )
;

eos_rbafvpn4no_bgp
:
  BGP
  (
    eos_rbafnobc_additional_paths
    | eos_rbafnobc_next_hop_unchanged
  )
;

eos_rbafvpn4no_mpls
:
  MPLS LABEL ALLOCATION DISABLED NEWLINE
;

eos_rbafvpn4no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
    | eos_rbafnonc_additional_paths
    | eos_rbafnonc_next_hop_unchanged
    | eos_rbafnonc_route_map
  )
;

eos_rbafvpn4no_next_hop
:
  NEXT_HOP RESOLUTION RIBS VRF_UNICAST_RIB NEWLINE
;

eos_rb_af_vpn_v6
:
  VPN_IPV6 NEWLINE
  (
    eos_rbafvpn6_bgp
    | eos_rbafvpn6_default
    | eos_rbafvpn6_neighbor
    | eos_rbafvpn6_next_hop
    | eos_rbafvpn6_no
  )*
;

eos_rbafvpn6_bgp
:
  BGP
  (
    eos_rbafbc_additional_paths
    | eos_rbafbc_next_hop_unchanged
  )
;

eos_rbafvpn6_default
:
  DEFAULT
  eos_rbafvpn6d_neighbor
;

eos_rbafvpn6d_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafdnc_activate
  )
;
eos_rbafvpn6_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
  )
;

eos_rbafvpn6_next_hop
:
  NEXT_HOP RESOLUTION RIBS TUNNEL_RIB SYSTEM_TUNNEL_RIB SYSTEM_CONNECTED NEWLINE
;

eos_rbafvpn6_no
:
  NO
  (
    eos_rbafvpn6no_bgp
    | eos_rbafvpn6no_mpls
    | eos_rbafvpn6no_neighbor
    | eos_rbafvpn6no_next_hop
  )
;

eos_rbafvpn6no_bgp
:
  BGP
  (
    eos_rbafnobc_additional_paths
    | eos_rbafnobc_next_hop_unchanged
  )
;

eos_rbafvpn6no_mpls
:
  MPLS LABEL ALLOCATION DISABLED NEWLINE
;

eos_rbafvpn6no_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbafnonc_activate
    | eos_rbafnonc_additional_paths
    | eos_rbafnonc_next_hop_unchanged
    | eos_rbafnonc_route_map
  )
;

eos_rbafvpn6no_next_hop
:
  NEXT_HOP RESOLUTION RIBS VRF_UNICAST_RIB NEWLINE
;

// Common to ipv4 unicast and ipv6 unicast. Others should just copy the relevant afdnc rules.
eos_rb_af_default_neighbor_common
:
  eos_rbafdnc_activate
;

eos_rbafdnc_activate
:
  ACTIVATE NEWLINE
;

// Common to ipv4 unicast and ipv6 unicast. Others should just copy the relevant afnc rules.
eos_rb_af_neighbor_common
:
  (
    eos_rbafnc_activate
    | eos_rbafnc_additional_paths
    | eos_rbafnc_default_originate
    | eos_rbafnc_graceful_restart
    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_prefix_list
    | eos_rbafnc_route_map
    | eos_rbafnc_weight
  )
;

eos_rbafnc_activate
:
  ACTIVATE NEWLINE
;

eos_rbafnc_additional_paths
:
  ADDITIONAL_PATHS (SEND ANY | RECEIVE) NEWLINE
;

eos_rbafnc_default_originate
:
  DEFAULT_ORIGINATE (
    always = ALWAYS
    | ROUTE_MAP rm = variable
  )* NEWLINE
;

eos_rbafnc_graceful_restart
:
  GRACEFUL_RESTART NEWLINE
;

eos_rbafnc_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbafnc_prefix_list
:
  PREFIX_LIST name = variable (IN | OUT) NEWLINE
;

eos_rbafnc_route_map
:
  ROUTE_MAP name = variable (IN | OUT) NEWLINE
;

eos_rbafnc_weight
:
  WEIGHT weight = uint16 NEWLINE  // weight = 1..65535
;

// Common to ipv4/ipv6 unicast > bgp. Other address families should just use the rbafbc rules.
eos_rb_af_bgp_common
:
  eos_rbafbc_additional_paths
  | eos_rbafbc_next_hop_unchanged
;

eos_rbafbc_additional_paths
:
  ADDITIONAL_PATHS
  (
    INSTALL
    | RECEIVE
    | SEND ANY
  )
  NEWLINE
;

eos_rbafbc_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

// Common to ipv4/ipv6 unicast > no bgp. Other address families should just use the rbafnobc rules.
eos_rb_af_no_bgp_common
:
  eos_rbafnobc_additional_paths
  | eos_rbafnobc_route
  | eos_rbafnobc_next_hop_unchanged

;

eos_rbafnobc_additional_paths
:
  ADDITIONAL_PATHS (INSTALL | RECEIVE | SEND ANY) NEWLINE
;

eos_rbafnobc_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbafnobc_route
:
  ROUTE INSTALL_MAP NEWLINE
;

// Common to ipv4/ipv6 unicast. Other address families should just use the rbafnonc rules.
eos_rb_af_no_neighbor_common
:
  eos_rbafnonc_activate
  | eos_rbafnonc_additional_paths
  | eos_rbafnonc_default_originate
  | eos_rbafnonc_next_hop
  | eos_rbafnonc_next_hop_unchanged
  | eos_rbafnonc_prefix_list
  | eos_rbafnonc_route_map
  | eos_rbafnonc_weight
;

eos_rbafnonc_activate
:
  ACTIVATE NEWLINE
;

eos_rbafnonc_additional_paths
:
  ADDITIONAL_PATHS (SEND ANY | RECEIVE) NEWLINE
;

eos_rbafnonc_default_originate
:
  DEFAULT_ORIGINATE NEWLINE
;

eos_rbafnonc_next_hop
:
  NEXT_HOP ADDRESS_FAMILY IPV6 NEWLINE
;

eos_rbafnonc_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbafnonc_prefix_list
:
  PREFIX_LIST (IN | OUT) NEWLINE
;

eos_rbafnonc_route_map
:
  ROUTE_MAP (IN | OUT) NEWLINE
;

eos_rbafnonc_weight
:
  WEIGHT NEWLINE
;

eos_rb_inner
:
  eos_rbi_aggregate_address
  | eos_rbi_bgp
  | eos_rbi_default
  | eos_rbi_default_metric
  | eos_rbi_distance
  | eos_rbi_dynamic
  | eos_rbi_graceful_restart
  | eos_rbi_graceful_restart_helper
  | eos_rbi_ip
  | eos_rbi_ipv6
  | eos_rbi_maximum_paths
  | eos_rbi_neighbor
  | eos_rbi_network
  | eos_rbi_no
  | eos_rbi_redistribute
  | eos_rbi_router_id
  | eos_rbi_shutdown
  | eos_rbi_timers
  | eos_rbi_ucmp
  | eos_rbi_update
;

eos_rbi_aggregate_address
:
  AGGREGATE_ADDRESS
  (
    eos_rb_aa_v4
    | eos_rb_aa_v6
  )
;

eos_rbi_bgp
:
  BGP
  (
    eos_rbib_additional_paths
    | eos_rbib_advertise_inactive
    | eos_rbib_aggregate_route
    | eos_rbib_allowas_in
    | eos_rbib_always_compare_med
    | eos_rbib_asn
    | eos_rbib_auto_local_addr
    | eos_rbib_bestpath
    | eos_rbib_client_to_client
    | eos_rbib_cluster_id
    | eos_rbib_confederation
    | eos_rbib_control_plane_filter
    | eos_rbib_convergence
    | eos_rbib_default
    | eos_rbib_enforce_first_as
//    | eos_rbib_host_routes
    | eos_rbib_labeled_unicast
    | eos_rbib_listen
    | eos_rbib_log_neighbor_changes
    | eos_rbib_missing_policy
    | eos_rbib_monitoring
    | eos_rbib_next_hop_unchanged
    | eos_rbib_peer_mac_resolution_timeout
    | eos_rbib_redistribute_internal
//    | eos_rbib_route
//    | eos_rbib_route_reflector
    | eos_rbib_trace
    | eos_rbib_transport
  )
;

eos_rbib_additional_paths
:
  ADDITIONAL_PATHS
  (
    SEND ANY
    | RECEIVE
    | INSTALL
  )
  NEWLINE
;

eos_rbib_advertise_inactive
:
  ADVERTISE_INACTIVE NEWLINE
;

eos_rbib_aggregate_route
:
  AGGREGATE_ROUTE COMMUNITY INHERITANCE LOOSE NEWLINE
;

eos_rbib_allowas_in
:
  ALLOWAS_IN (num = uint8)? NEWLINE // num = 1..10
;

eos_rbib_always_compare_med
:
  ALWAYS_COMPARE_MED NEWLINE
;

eos_rbib_asn
:
  ASN NOTATION (ASDOT | ASPLAIN) NEWLINE
;

eos_rbib_auto_local_addr
:
  AUTO_LOCAL_ADDR NEWLINE
;

eos_rbib_confederation
:
  CONFEDERATION
  (
    eos_rbibconf_identifier
    | eos_rbibconf_peers
  )
;

eos_rbibconf_identifier
:
  IDENTIFIER asn = bgp_asn NEWLINE
;

eos_rbibconf_peers
:
  PEERS asns = eos_as_range_list NEWLINE
;

eos_rbib_control_plane_filter
:
  CONTROL_PLANE_FILTER DEFAULT_ALLOW NEWLINE
;

eos_rbib_convergence
:
  CONVERGENCE
  (
    TIME time = dec
    | SLOW_PEER TIME time = dec
  )
  NEWLINE
;

eos_rbib_default
:
  DEFAULT
  (
    eos_rbibd_ipv4_unicast
    | eos_rbibd_ipv6_unicast
  )
;

eos_rbibd_ipv4_unicast
:
   IPV4_UNICAST
   (
     eos_rbibd_ipv4u_enabled
     | eos_rbibd_ipv4u_transport
   )
;

// Nothing after ipv4-unicast means "enabled by default".
eos_rbibd_ipv4u_enabled
:
  NEWLINE
;

eos_rbibd_ipv4u_transport
:
  TRANSPORT IPV6 NEWLINE
;

eos_rbibd_ipv6_unicast
:
  IPV6_UNICAST NEWLINE
;

eos_rbib_enforce_first_as
:
  ENFORCE_FIRST_AS NEWLINE
;

eos_rbib_bestpath
:
  BESTPATH
  (
    eos_rbibbp_as_path
    | eos_rbibbp_ecmp_fast
    // eos_rbibbp_med
    // eos_rbibbp_skip
    | eos_rbibbp_tie_break
  )
;

eos_rbibbp_as_path
:
  AS_PATH
  (
    // eos_rbibbpa_ignore |
    eos_rbibbpa_multipath_relax
  )
;

eos_rbibbp_ecmp_fast
:
  ECMP_FAST NEWLINE
;

eos_rbibbp_tie_break
:
  TIE_BREAK (ROUTER_ID | CLUSTER_LIST_LENGTH) NEWLINE
;

eos_rbibbpa_multipath_relax
:
  MULTIPATH_RELAX NEWLINE
;

eos_rbib_client_to_client
:
  CLIENT_TO_CLIENT REFLECTION NEWLINE
;

eos_rbib_cluster_id
:
  CLUSTER_ID ip = IP_ADDRESS NEWLINE
;

eos_rbib_labeled_unicast
:
  LABELED_UNICAST RIB (IP | TUNNEL) NEWLINE
;

eos_rbib_listen
:
  LISTEN
  (
    eos_rbibl_limit
    | eos_rbibl_range
  )
;

eos_rbibl_limit
:
  LIMIT num = dec NEWLINE
;

eos_rbibl_range
:
  RANGE
  (
    ip = IP_ADDRESS MASK mask = IP_ADDRESS
    | prefix = IP_PREFIX
    | ip6prefix = IPV6_PREFIX
  )
  (PEER_GROUP | PEER GROUP) pg = variable
  (
    PEER_FILTER peer_filter = WORD
    | REMOTE_AS asn = bgp_asn
  )
  NEWLINE
;

eos_rbib_log_neighbor_changes
:
  LOG_NEIGHBOR_CHANGES NEWLINE
;

eos_rbib_missing_policy
:
  MISSING_POLICY DIRECTION (IN | OUT) ACTION (DENY | DENY_IN_OUT | PERMIT) NEWLINE
;

eos_rbib_monitoring
:
  MONITORING NEWLINE
;

eos_rbib_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbib_peer_mac_resolution_timeout
:
  PEER_MAC_RESOLUTION_TIMEOUT dec NEWLINE
;

eos_rbib_redistribute_internal
:
  REDISTRIBUTE_INTERNAL NEWLINE
;

eos_rbib_trace
:
  TRACE
  (
    eos_rbibt_neighbor
    | eos_rbibt_route_key
  )
;

eos_rbibt_neighbor
:
  NEIGHBOR ALL NEWLINE
;

eos_rbibt_route_key
:
  ROUTE_KEY ALL NEWLINE
;

eos_rbib_transport
:
  TRANSPORT
  (
    eos_rbibtrans_ipv4
    | eos_rbibtrans_ipv6
    | eos_rbibtrans_listen_port
    | eos_rbibtrans_pmtud
    | eos_rbibtrans_qos
  )
;

eos_rbibtrans_ipv4
:
//536-16344
  IPV4 MSS mss = dec NEWLINE
;

eos_rbibtrans_ipv6
:
//516-16324
  IPV6 MSS mss = dec NEWLINE
;

eos_rbibtrans_listen_port
:
// 1-65535
  LISTEN_PORT lp = uint16 NEWLINE
;

eos_rbibtrans_pmtud
:
  PMTUD DISABLED? NEWLINE
;

eos_rbibtrans_qos
:
// 0-63
  QOS DSCP dscp = dec NEWLINE
;

eos_rbi_default
:
  DEFAULT
  eos_rbid_neighbor
;

eos_rbid_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbidn_export_localpref
    | eos_rbidn_monitoring
  )
;

eos_rbidn_export_localpref
:
  EXPORT_LOCALPREF NEWLINE
;

eos_rbidn_monitoring
:
  MONITORING NEWLINE
;

eos_rbi_default_metric
:
  DEFAULT_METRIC metric = dec NEWLINE
;

eos_rbi_distance
:
  DISTANCE BGP external = dec (internal = dec local = dec)? NEWLINE
;

eos_rbi_ip
:
  IP ACCESS_GROUP name = variable IN? NEWLINE
;

eos_rbi_ipv6
:
  IPV6 ACCESS_GROUP name = variable IN? NEWLINE
;

eos_rbi_dynamic
:
  DYNAMIC PEER MAX max = uint32 NEWLINE
;

eos_rbi_graceful_restart
:
  GRACEFUL_RESTART
  (
    RESTART_TIME dec
    | STALEPATH_TIME dec
  )* NEWLINE
;

eos_rbi_graceful_restart_helper
:
  GRACEFUL_RESTART_HELPER NEWLINE
;

eos_rbi_maximum_paths
:
  MAXIMUM_PATHS num = dec (ECMP ecmp = dec)? NEWLINE
;

eos_rbi_neighbor
:
  NEIGHBOR
  (
    eos_rbi_neighbor4
    | eos_rbi_neighbor6
    // Definition of a peer group
    | eos_rbi_peer_group
  )
;

eos_rbi_neighbor4
:
  name = IP_ADDRESS
  (
    eos_rbi_neighbor_common
    | eos_rbin_local_v6_addr
    // Assigning a peer group
    | eos_rbin_peer_group
  )
;

eos_rbi_neighbor6
:
  name = IPV6_ADDRESS
  (
    eos_rbi_neighbor_common
    //| eos_rbin_local_v4_addr
    // Assigning a peer group
    | eos_rbin_peer_group
  )
;

// Common to ipv4 ipv6 and peer-groups configured at the vrf level
eos_rbi_neighbor_common
:
  (
    eos_rbinc_additional_paths
    | eos_rbinc_allowas_in
    | eos_rbinc_auto_local_addr
    | eos_rbinc_bfd
    | eos_rbinc_default_originate
    | eos_rbinc_description
    | eos_rbinc_dont_capability_negotiate
    | eos_rbinc_ebgp_multihop
    | eos_rbinc_enforce_first_as
    | eos_rbinc_export_localpref
    | eos_rbinc_fall_over
    | eos_rbinc_graceful_restart
    | eos_rbinc_graceful_restart_helper
    | eos_rbinc_idle_restart_timer
//    | eos_rbinc_import_localpref
//    | eos_rbinc_link_bandwidth
    | eos_rbinc_local_as
    | eos_rbinc_maximum_accepted_routes
    | eos_rbinc_maximum_routes
//    | eos_rbinc_metric_out
//    | eos_rbinc_monitoring
    | eos_rbinc_next_hop_peer
    | eos_rbinc_next_hop_self
    | eos_rbinc_next_hop_unchanged
//    | eos_rbinc_out_delay
    | eos_rbinc_passive
    | eos_rbinc_password
    | eos_rbafnc_prefix_list  // intended rbafnc - it affects the generic address family
    | eos_rbinc_remote_as
    | eos_rbinc_remove_private_as
    | eos_rbinc_rib_in
    | eos_rbafnc_route_map // intended rbafnc - it affects the generic address family
    | eos_rbinc_route_reflector_client
    | eos_rbinc_route_to_peer
    | eos_rbinc_send_community
    | eos_rbinc_shutdown
    | eos_rbinc_soft_reconfiguration
    | eos_rbinc_timers
//    | eos_rbinc_transport
//    | eos_rbinc_ttl
    | eos_rbinc_update_source
  )
;

eos_rbinc_additional_paths
:
  ADDITIONAL_PATHS (SEND ANY | RECEIVE) NEWLINE
;

eos_rbinc_allowas_in
:
  ALLOWAS_IN (num = dec)? NEWLINE
;

eos_rbinc_auto_local_addr
:
  AUTO_LOCAL_ADDR NEWLINE
;

eos_rbinc_bfd
:
  BFD NEWLINE
;

eos_rbinc_default_originate
:
  DEFAULT_ORIGINATE
  (
    always = ALWAYS
    | ROUTE_MAP rm = variable
  )* NEWLINE
;

eos_rbinc_description
:
  DESCRIPTION (desc = ~NEWLINE) NEWLINE
;

eos_rbinc_dont_capability_negotiate
:
  DONT_CAPABILITY_NEGOTIATE NEWLINE
;

eos_rbinc_ebgp_multihop
:
  EBGP_MULTIHOP (num = dec)? NEWLINE
;

eos_rbinc_enforce_first_as
:
  ENFORCE_FIRST_AS NEWLINE
;

eos_rbinc_export_localpref
:
  EXPORT_LOCALPREF value = bgp_local_pref NEWLINE
;

eos_rbinc_fall_over
:
  FALL_OVER BFD NEWLINE
;

eos_rbinc_graceful_restart
:
  GRACEFUL_RESTART NEWLINE
;

eos_rbinc_graceful_restart_helper
:
  GRACEFUL_RESTART_HELPER NEWLINE
;

eos_rbinc_idle_restart_timer
:
  IDLE_RESTART_TIMER time = dec NEWLINE
;

eos_rbinc_local_as
:
  LOCAL_AS asn = bgp_asn NO_PREPEND REPLACE_AS FALLBACK? NEWLINE
;

eos_rbinc_maximum_accepted_routes
:
  MAXIMUM_ACCEPTED_ROUTES num = dec (WARNING_LIMIT warn_limit = dec)? NEWLINE
;

eos_rbinc_maximum_routes
:
  MAXIMUM_ROUTES num = dec
  (
    WARNING_ONLY
    | (WARNING_LIMIT warn_limit = dec PERCENT_LITERAL?)
  )*
  NEWLINE
;

eos_rbinc_next_hop_peer
:
  NEXT_HOP_PEER NEWLINE
;

eos_rbinc_next_hop_self
:
  NEXT_HOP_SELF NEWLINE
;

eos_rbinc_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbinc_passive
:
  PASSIVE NEWLINE
;

eos_rbinc_password
:
  PASSWORD (encrypt_level = dec)? variable NEWLINE
;

eos_rbinc_remote_as
:
  REMOTE_AS asn = bgp_asn NEWLINE
;

eos_rbinc_remove_private_as
:
  REMOVE_PRIVATE_AS (ALL REPLACE_AS?)? NEWLINE
;

eos_rbinc_rib_in
:
  RIB_IN PRE_POLICY RETAIN ALL? NEWLINE
;

eos_rbinc_route_reflector_client
:
  ROUTE_REFLECTOR_CLIENT NEWLINE
;

eos_rbinc_route_to_peer
:
  ROUTE_TO_PEER NEWLINE
;

eos_rbinc_send_community
:
  SEND_COMMUNITY
  (
    | ADD comm = eos_bgp_community NEWLINE
    | REMOVE comm = eos_bgp_community NEWLINE
    | (communities += eos_bgp_community+) NEWLINE
    | NEWLINE
  )
;

eos_rbinc_shutdown
:
  SHUTDOWN NEWLINE
;

eos_rbinc_soft_reconfiguration
:
  SOFT_RECONFIGURATION INBOUND ALL? NEWLINE
;

eos_rbinc_timers
:
  TIMERS keepalive = dec hold = dec NEWLINE
;

eos_rbinc_update_source
:
  UPDATE_SOURCE iface = interface_name NEWLINE
;

eos_rbin_local_v6_addr
:
  LOCAL_V6_ADDR IPV6_ADDRESS NEWLINE
;

// Assigning a peer group to a neighbor
eos_rbin_peer_group
:
  PEER_GROUP name = variable NEWLINE
  | PEER GROUP name = variable NEWLINE
;

eos_rbi_network
:
  NETWORK
  (
    eos_rbi_network4
    | eos_rbi_network6
  )
;

eos_rbi_network4
:
  (
    ip = IP_ADDRESS MASK mask = IP_ADDRESS
    | prefix = IP_PREFIX
  )
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbi_network6
:
  IPV6_PREFIX (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbi_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbi_no
:
  NO
  (
    eos_rbino_bgp
    | eos_rbino_default_metric
    | eos_rbino_graceful_restart
    | eos_rbino_graceful_restart_helper
    | eos_rbino_ip
    | eos_rbino_ipv6
    | eos_rbino_monitoring
    | eos_rbino_neighbor
    | eos_rbino_redistribute
    | eos_rbino_router_id
    | eos_rbino_shutdown
    | eos_rbino_ucmp
    | eos_rbino_update
  )
;

eos_rbino_bgp
:
  BGP
  (
    eos_rbino_bgp_additional_paths
    | eos_rbino_bgp_advertise_inactive
    | eos_rbino_bgp_aggregate_route
    | eos_rbino_bgp_allowas_in
    | eos_rbino_bgp_always_compare_med
    | eos_rbino_bgp_aspath_cmp_include_nexthop
    | eos_rbino_bgp_auto_local_addr
    | eos_rbino_bgp_bestpath
    | eos_rbino_bgp_client_to_client
    | eos_rbino_bgp_cluster_id
    | eos_rbino_bgp_confederation
    | eos_rbino_bgp_control_plane_filter
    | eos_rbino_bgp_default
    | eos_rbino_bgp_fec
    | eos_rbino_bgp_missing_policy
    | eos_rbino_bgp_monitoring
    | eos_rbino_bgp_next_hop_unchanged
    | eos_rbino_bgp_route
    | eos_rbino_bgp_route_reflector
    | eos_rbino_bgp_transport
  )
;

eos_rbino_bgp_additional_paths
:
  ADDITIONAL_PATHS (INSTALL | RECEIVE | SEND ANY) NEWLINE
;

eos_rbino_bgp_advertise_inactive
:
  ADVERTISE_INACTIVE NEWLINE
;

eos_rbino_bgp_aggregate_route
:
  AGGREGATE_ROUTE COMMUNITY INHERITANCE LOOSE NEWLINE
;

eos_rbino_bgp_allowas_in
:
  ALLOWAS_IN NEWLINE
;

eos_rbino_bgp_always_compare_med
:
  ALWAYS_COMPARE_MED NEWLINE
;

eos_rbino_bgp_aspath_cmp_include_nexthop
:
  ASPATH_CMP_INCLUDE_NEXTHOP NEWLINE
;

eos_rbino_bgp_auto_local_addr
:
  AUTO_LOCAL_ADDR NEWLINE
;

eos_rbino_bgp_bestpath
:
  BESTPATH
  (
    eos_rbino_bgp_bp_as_path
    | eos_rbino_bgp_bp_ecmp_fast
    | eos_rbino_bgp_bp_med
    | eos_rbino_bgp_bp_skip
    | eos_rbino_bgp_bp_tie_break
  )
;

eos_rbino_bgp_bp_as_path
:
  AS_PATH
  (
    eos_rbino_bgp_bpa_ignore
    | eos_rbino_bgp_bpa_multipath_relax
  )
;

eos_rbino_bgp_bpa_ignore
:
  IGNORE NEWLINE
;

eos_rbino_bgp_bpa_multipath_relax
:
  MULTIPATH_RELAX NEWLINE
;

eos_rbino_bgp_bp_ecmp_fast
:
  ECMP_FAST NEWLINE
;

eos_rbino_bgp_bp_med
:
  MED
  (
    eos_rbino_bgp_bpm_confed
    | eos_rbino_bgp_bpm_missing_as_worst
  )
;

eos_rbino_bgp_bpm_confed
:
  CONFED NEWLINE
;

eos_rbino_bgp_bpm_missing_as_worst
:
  MISSING_AS_WORST NEWLINE
;

eos_rbino_bgp_bp_skip
:
  SKIP_LITERAL NEXT_HOP IGP_COST NEWLINE
;

eos_rbino_bgp_bp_tie_break
:
  TIE_BREAK
  (
    eos_rbino_bgp_bpt_age
    | eos_rbino_bgp_bpt_cluster_list_length
    | eos_rbino_bgp_bpt_originator_id
    | eos_rbino_bgp_bpt_router_id
  )
;

eos_rbino_bgp_bpt_age
:
  AGE NEWLINE
;

eos_rbino_bgp_bpt_cluster_list_length
:
  CLUSTER_LIST_LENGTH NEWLINE
;

eos_rbino_bgp_bpt_originator_id
:
  ORIGINATOR_ID NEWLINE
;

eos_rbino_bgp_bpt_router_id
:
  ROUTER_ID NEWLINE
;

eos_rbino_bgp_client_to_client
:
  CLIENT_TO_CLIENT REFLECTION NEWLINE
;

eos_rbino_bgp_cluster_id
:
  CLUSTER_ID NEWLINE
;

eos_rbino_bgp_confederation
:
  CONFEDERATION eos_rbino_bc_identifier
;

eos_rbino_bc_identifier
:
  IDENTIFIER NEWLINE
;

eos_rbino_bgp_control_plane_filter
:
  CONTROL_PLANE_FILTER DEFAULT_ALLOW NEWLINE
;

eos_rbino_bgp_default
:
  DEFAULT
  (
    eos_rbino_bgp_default_ipv4_unicast
    | eos_rbino_bgp_default_ipv6_unicast
  )
;

eos_rbino_bgp_default_ipv4_unicast
:
  IPV4_UNICAST
  (
    eos_rbino_bgp_default_ipv4u_enabled
    | eos_rbino_bgp_default_ipv4u_transport
  )
;

// Nothing after ipv4-unicast means not enabled by default.
eos_rbino_bgp_default_ipv4u_enabled
:
  NEWLINE
;

eos_rbino_bgp_default_ipv4u_transport
:
  TRANSPORT IPV6 NEWLINE
;

eos_rbino_bgp_default_ipv6_unicast
:
  IPV6_UNICAST NEWLINE
;

eos_rbino_bgp_fec
:
  FEC SKIP_LITERAL IN_PLACE UPDATE NEWLINE
;

eos_rbino_bgp_missing_policy
:
  MISSING_POLICY DIRECTION (IN | OUT) ACTION NEWLINE
;

eos_rbino_bgp_monitoring
:
  MONITORING NEWLINE
;

eos_rbino_bgp_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbino_bgp_route
:
  ROUTE INSTALL_MAP NEWLINE
;

eos_rbino_bgp_route_reflector
:
  ROUTE_REFLECTOR PRESERVE_ATTRIBUTES NEWLINE
;

eos_rbino_bgp_transport
:
  TRANSPORT
  (
    eos_rbino_bgptr_ipv4
    | eos_rbino_bgptr_ipv6
    | eos_rbino_bgptr_listen_port
    | eos_rbino_bgptr_pmtud
    | eos_rbino_bgptr_qos
  )
;

eos_rbino_bgptr_ipv4
:
  IPV4 MSS NEWLINE
;

eos_rbino_bgptr_ipv6
:
  IPV6 MSS NEWLINE
;

eos_rbino_bgptr_listen_port
:
  LISTEN_PORT NEWLINE
;

eos_rbino_bgptr_pmtud
:
  PMTUD NEWLINE
;

eos_rbino_bgptr_qos
:
  QOS DSCP NEWLINE
;

eos_rbino_default_metric
:
  DEFAULT_METRIC NEWLINE
;

eos_rbino_graceful_restart
:
  GRACEFUL_RESTART NEWLINE
;

eos_rbino_graceful_restart_helper
:
  GRACEFUL_RESTART_HELPER NEWLINE
;

eos_rbino_ip
:
  IP ACCESS_GROUP IN? NEWLINE
;

eos_rbino_ipv6
:
  IPV6 ACCESS_GROUP IN? NEWLINE
;

eos_rbino_monitoring
:
  MONITORING
  (
    eos_rbino_monitoring_port
    | eos_rbino_monitoring_received
  )
;

eos_rbino_monitoring_port
:
  PORT NEWLINE
;

eos_rbino_monitoring_received
:
  RECEIVED eos_rbino_mr_routes
;

eos_rbino_mr_routes
:
  ROUTES
  (
    eos_rbino_mrr_address_family
    | eos_rbino_mrr_post_policy
    | eos_rbino_mrr_pre_policy
  )
;

eos_rbino_mrr_address_family
:
  ADDRESS_FAMILY
  (
    IPV4 UNICAST
    | IPV6 UNICAST
    | IPV6 LABELED_UNICAST
  )
  NEWLINE
;

eos_rbino_mrr_post_policy
:
  POST_POLICY NEWLINE
;

eos_rbino_mrr_pre_policy
:
  PRE_POLICY NEWLINE
;

eos_rbino_neighbor
:
  NEIGHBOR nid = eos_neighbor_id
  (
    eos_rbino_neighbor_neighbor // delete the neighbor
    | eos_rbinon_additional_paths
    | eos_rbinon_allowas_in
    | eos_rbinon_as_path
    | eos_rbinon_auto_local_addr
    | eos_rbinon_bfd
    | eos_rbinon_default_originate
    | eos_rbinon_description
    | eos_rbinon_dont_capability_negotiate
    | eos_rbinon_ebgp_multihop
    | eos_rbinon_enforce_first_as
    | eos_rbinon_export_localpref
    | eos_rbinon_fall_over
    | eos_rbinon_graceful_restart
    | eos_rbinon_graceful_restart_helper
    | eos_rbinon_idle_restart_timer
    | eos_rbinon_import_localpref
    | eos_rbinon_link_bandwidth
    | eos_rbinon_local_v4_addr
    | eos_rbinon_local_v6_addr
    | eos_rbinon_local_as
    | eos_rbinon_maximum_accepted_routes
    | eos_rbinon_metric_out
    | eos_rbinon_next_hop_peer
    | eos_rbinon_next_hop_self
    | eos_rbinon_next_hop_unchanged
    | eos_rbinon_next_hop_v6_addr
    | eos_rbinon_out_delay
    | eos_rbinon_passive
    | eos_rbinon_password
    | eos_rbinon_peer_group
    | eos_rbafnonc_prefix_list  // intended rbafnonc - it affects the generic address family
    | eos_rbinon_remote_as
    | eos_rbinon_remove_private_as
    | eos_rbafnonc_route_map  // intended rbafnonc - it affects the generic address family
    | eos_rbinon_route_reflector_client
    | eos_rbinon_route_to_peer
    | eos_rbinon_send_community
    | eos_rbinon_shutdown
    | eos_rbinon_timers
    | eos_rbinon_ttl
    | eos_rbinon_transport
    | eos_rbinon_update_source
    | eos_rbafnonc_weight  // intended rbafnonc - it affects the generic address family
  )
;

eos_rbino_neighbor_neighbor: NEWLINE;

eos_rbinon_additional_paths
:
  ADDITIONAL_PATHS (RECEIVE | SEND ANY) NEWLINE
;

eos_rbinon_allowas_in
:
  ALLOWAS_IN NEWLINE
;

eos_rbinon_as_path
:
  AS_PATH
  (
    eos_rbinonasp_prepend_own
    | eos_rbinonasp_remote_as
  )
;

eos_rbinonasp_prepend_own
:
  PREPEND_OWN DISABLED NEWLINE
;

eos_rbinonasp_remote_as
:
  REMOTE_AS REPLACE OUT NEWLINE
;

eos_rbinon_auto_local_addr
:
  AUTO_LOCAL_ADDR NEWLINE
;

eos_rbinon_bfd
:
  BFD NEWLINE
;

eos_rbinon_default_originate
:
  DEFAULT_ORIGINATE NEWLINE
;

eos_rbinon_description
:
  DESCRIPTION NEWLINE
;

eos_rbinon_dont_capability_negotiate
:
  DONT_CAPABILITY_NEGOTIATE NEWLINE
;

eos_rbinon_ebgp_multihop
:
  EBGP_MULTIHOP NEWLINE
;

eos_rbinon_enforce_first_as
:
  ENFORCE_FIRST_AS NEWLINE
;

eos_rbinon_export_localpref
:
  EXPORT_LOCALPREF NEWLINE
;

eos_rbinon_fall_over
:
  FALL_OVER BFD NEWLINE
;

eos_rbinon_graceful_restart
:
  GRACEFUL_RESTART NEWLINE
;

eos_rbinon_graceful_restart_helper
:
  GRACEFUL_RESTART_HELPER NEWLINE
;

eos_rbinon_idle_restart_timer
:
  IDLE_RESTART_TIMER NEWLINE
;

eos_rbinon_import_localpref
:
  IMPORT_LOCALPREF NEWLINE
;

eos_rbinon_link_bandwidth
:
  LINK_BANDWIDTH
  (
    eos_rbinon_lb_adjust
    | eos_rbinon_lb_lb
    | eos_rbinon_lb_update_delay
  )
;

eos_rbinon_lb_adjust
:
  ADJUST AUTO NEWLINE
;

// "no neighbor PG link-bandwidth"
eos_rbinon_lb_lb
:
  NEWLINE
;

eos_rbinon_lb_update_delay
:
  UPDATE_DELAY NEWLINE
;

eos_rbinon_local_v4_addr
:
  LOCAL_V4_ADDR NEWLINE
;

eos_rbinon_local_v6_addr
:
  LOCAL_V6_ADDR NEWLINE
;

eos_rbinon_local_as
:
  LOCAL_AS NEWLINE
;

eos_rbinon_maximum_accepted_routes
:
  MAXIMUM_ACCEPTED_ROUTES NEWLINE
;

eos_rbinon_metric_out
:
  METRIC_OUT NEWLINE
;

eos_rbinon_next_hop_peer
:
  NEXT_HOP_PEER NEWLINE
;

eos_rbinon_next_hop_self
:
  NEXT_HOP_SELF NEWLINE
;

eos_rbinon_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbinon_next_hop_v6_addr
:
  NEXT_HOP_V6_ADDR NEWLINE
;

eos_rbinon_out_delay
:
  OUT_DELAY NEWLINE
;

eos_rbinon_passive
:
  PASSIVE NEWLINE
;

eos_rbinon_password
:
  PASSWORD NEWLINE
;

eos_rbinon_peer_group
:
  (PEER GROUP | PEER_GROUP) NEWLINE
;

eos_rbinon_remote_as
:
  REMOTE_AS NEWLINE
;

eos_rbinon_remove_private_as
:
  REMOVE_PRIVATE_AS NEWLINE
;

eos_rbinon_route_reflector_client
:
  ROUTE_REFLECTOR_CLIENT NEWLINE
;

eos_rbinon_route_to_peer
:
  ROUTE_TO_PEER NEWLINE
;

eos_rbinon_send_community
:
  SEND_COMMUNITY NEWLINE
;

eos_rbinon_shutdown
:
  SHUTDOWN NEWLINE
;

eos_rbinon_timers
:
  TIMERS NEWLINE
;

eos_rbinon_ttl
:
  TTL MAXIMUM_HOPS NEWLINE
;

eos_rbinon_transport
:
  TRANSPORT
  (
    eos_rbinon_transport_connection_mode
    | eos_rbinon_transport_remote_port
  )
;

eos_rbinon_transport_connection_mode
:
  CONNECTION_MODE PASSIVE NEWLINE
;

eos_rbinon_transport_remote_port
:
  REMOTE_PORT NEWLINE
;

eos_rbinon_update_source
:
  UPDATE_SOURCE NEWLINE
;

eos_rbino_redistribute
:
  REDISTRIBUTE
  (
    eos_rbinor_aggregate
    | eos_rbinor_attached_host
    | eos_rbinor_connected
    | eos_rbinor_dynamic
    | eos_rbinor_isis
    | eos_rbinor_ospf
    | eos_rbinor_ospf3
    | eos_rbinor_ospfv3
    | eos_rbinor_rip
    | eos_rbinor_static
  )
;

eos_rbinor_aggregate
:
  AGGREGATE NEWLINE
;

eos_rbinor_attached_host
:
  ATTACHED_HOST (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_connected
:
  CONNECTED (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_dynamic
:
  DYNAMIC (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_isis
:
  ISIS (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_ospf
:
  OSPF
  (
    eos_rbinor_ospf_any
    | eos_rbinor_ospf_match
  )
;

eos_rbinor_ospf_any
:
  (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_ospf_match
:
  MATCH (INTERNAL | EXTERNAL | NSSA_EXTERNAL) (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_ospf3
:
  OSPF3
  (
    // note: rbinor_ospfv3 has same meaning as this but newer syntax. Consider which updates go in
    //       one or both places.
    eos_rbinor_ospf3_any
    | eos_rbinor_ospf3_match
  )
;

eos_rbinor_ospf3_any
:
  (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_ospf3_match
:
  MATCH (INTERNAL | EXTERNAL | NSSA_EXTERNAL) (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_ospfv3
:
  OSPFV3
  (
    // note: rbinor_ospf3 has same meaning as this but newer syntax. Consider which updates go in
    //       one or both places.
    eos_rbinor_ospf3_any
    | eos_rbinor_ospf3_match
  )
;

eos_rbinor_rip
:
  RIP (ROUTE_MAP variable)? NEWLINE
;

eos_rbinor_static
:
  STATIC (ROUTE_MAP variable)? NEWLINE
;

eos_rbino_router_id
:
  ROUTER_ID NEWLINE
;

eos_rbino_shutdown
:
  SHUTDOWN NEWLINE
;

eos_rbino_ucmp
:
  UCMP
  (
    eos_rbino_ucmp_fec
    | eos_rbino_ucmp_link_bandwidth
    | eos_rbino_ucmp_mode
  )
;

eos_rbino_ucmp_fec
:
  FEC THRESHOLD NEWLINE
;

eos_rbino_ucmp_link_bandwidth
:
  LINK_BANDWIDTH (ENCODING_WEIGHTED | RECURSIVE | UPDATE_DELAY) NEWLINE
;

eos_rbino_ucmp_mode
:
  MODE NEWLINE
;

eos_rbino_update
:
  UPDATE (WAIT_FOR_CONVERGENCE | WAIT_INSTALL) NEWLINE
;

// Defining a peer group
eos_rbi_peer_group
:
  name = variable
  (
    (PEER_GROUP | PEER GROUP) NEWLINE
    | eos_rbi_neighbor_common
    | eos_rbin_local_v6_addr
  )
;

eos_rbi_redistribute
:
  REDISTRIBUTE
  (
    eos_rbir_aggregate
    | eos_rbir_attached_host
    | eos_rbir_connected
    | eos_rbir_dynamic
    | eos_rbir_isis
    | eos_rbir_ospf
    | eos_rbir_ospf3
    | eos_rbir_rip
    | eos_rbir_static
  )
;

eos_rbir_aggregate
:
  // This should not ever show up in real configs, but some engineering builds did include it.
  // > Important! Aggregate routes are redistributed automatically, and their redistribution cannot be disabled.
  // https://www.arista.com/en/um-eos/eos-section-33-4-bgp-commands#ww1117813
  AGGREGATE NEWLINE
;

eos_rbir_attached_host
:
  ATTACHED_HOST (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_connected
:
  CONNECTED (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_dynamic
:
  DYNAMIC (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_isis
:
  ISIS
  (LEVEL_1 | LEVEL_2 | LEVEL_1_2)?
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbir_ospf
:
  OSPF
  (
    MATCH
    (
      INTERNAL
      | EXTERNAL
      | (NSSA_EXTERNAL nssa_type = dec?)
    )
  )?
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbir_ospf3
:
  OSPF3
  (
    MATCH
    (
      INTERNAL
      | EXTERNAL
      | (NSSA_EXTERNAL nssa_type = dec?)
    )
  )?
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbir_rip
:
  RIP (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_static
:
  STATIC (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbi_router_id
:
  ROUTER_ID id = IP_ADDRESS NEWLINE
;

eos_rbi_shutdown
:
  SHUTDOWN NEWLINE
;

eos_rbi_timers
:
  TIMERS BGP keepalive = dec hold = dec NEWLINE
;

eos_rbi_ucmp
:
  UCMP
  (
    eos_rbiu_fec
    | eos_rbiu_link_bandwidth
    | eos_rbiu_mode
  )
;

eos_rbiu_fec
:
  FEC THRESHOLD TRIGGER trigger = dec CLEAR clear = dec WARNING_ONLY NEWLINE
;

eos_rbiu_link_bandwidth
:
  LINK_BANDWIDTH (ENCODING_WEIGHTED | RECURSIVE | UPDATE_DELAY dec) NEWLINE
;

eos_rbiu_mode
:
  MODE mode_num = dec
  (
    next_hops = dec
    (oversubscription = FLOAT)?
  )? NEWLINE
;

eos_rbi_update
:
  UPDATE
  (
    WAIT_FOR_CONVERGENCE
    | WAIT_INSTALL (BATCH_SIZE dec)?
  ) NEWLINE
;

eos_rb_aa_modifiers
:
  ADVERTISE_ONLY
  | AS_SET
  | ATTRIBUTE_MAP attr_map = variable
  | MATCH_MAP match_map = variable
  | SUMMARY_ONLY
;

eos_rb_aa_v4
:
  (
    ip = IP_ADDRESS mask = IP_ADDRESS
    | prefix = IP_PREFIX
  ) eos_rb_aa_modifiers*
  NEWLINE
;

eos_rb_aa_v6
:
  prefix = IPV6_PREFIX eos_rb_aa_modifiers* NEWLINE
;

eos_rb_monitoring
:
  MONITORING
  (
    eos_rbm_port
    | eos_rbm_received
    | eos_rbm_station
    | eos_rbm_timestamp
  )
;

eos_rb_no
:
  NO eos_rbn_bgp
;

eos_rbn_bgp
:
  BGP eos_rbnb_host_routes
;

eos_rbnb_host_routes
:
  HOST_ROUTES FIB DIRECT_INSTALL NEWLINE
;

eos_rbm_port
:
  PORT num = dec NEWLINE
;

eos_rbm_received
:
  RECEIVED  eos_rbm_received_routes
;

eos_rbm_received_routes
:
  ROUTES
  (
    eos_rbmrr_address_family
    | eos_rbmrr_post_policy
    | eos_rbmrr_pre_policy
  )
;

eos_rbmrr_address_family
:
  ADDRESS_FAMILY
  (
    IPV4 UNICAST
    | IPV6 LABELED_UNICAST
    | IPV6 UNICAST
  )
  NEWLINE
;

eos_rbmrr_post_policy
:
  POST_POLICY NEWLINE
;

eos_rbmrr_pre_policy
:
  PRE_POLICY NEWLINE
;

eos_rbm_station
:
  STATION name = variable NEWLINE
;

eos_rbm_timestamp
:
  TIMESTAMP ( NONE | SEND_TIME ) NEWLINE
;

eos_rb_vlan
:
  VLAN id = dec NEWLINE
  eos_rb_vlan_tail*
;

eos_rb_vlan_aware_bundle
:
  VLAN_AWARE_BUNDLE name = variable NEWLINE
  (
    eos_rb_vab_vlan
    | eos_rb_vlan_tail
  )*
;

eos_rb_vlan_tail_rd
:
  RD rd = route_distinguisher NEWLINE
;

eos_rb_vlan_tail_redistribute
:
  REDISTRIBUTE
  (
    HOST_ROUTE
    | LEARNED
    | ROUTER_MAC
    | STATIC
  ) NEWLINE
;

eos_rb_vlan_tail_route_target
:
  ROUTE_TARGET
  ( BOTH | IMPORT | EXPORT )
  rt = route_target NEWLINE
;

eos_rb_vab_vlan
:
  VLAN vlans = eos_vlan_id NEWLINE
;

eos_rb_vlan_tail
:
  eos_rb_vlan_tail_rd
  | eos_rb_vlan_tail_redistribute
  | eos_rb_vlan_tail_route_target
;

eos_rbv_address_family
:
  ADDRESS_FAMILY
  (
    eos_rbv_af_ipv4
    | eos_rbv_af_ipv6
  )
;

eos_rbv_af_ipv4
:
  IPV4
  (
    eos_rb_af_ipv4_multicast
    | eos_rb_af_ipv4_unicast
  )
;

eos_rbv_af_ipv6
:
  IPV6
  (
    eos_rb_af_ipv6_multicast
    | eos_rb_af_ipv6_unicast
  )
;

eos_rbv_local_as
:
  LOCAL_AS asn = bgp_asn NEWLINE
;

eos_rbv_rd
:
  RD rd = route_distinguisher NEWLINE
;

eos_rbv_route_target
:
  ROUTE_TARGET (IMPORT | EXPORT) p = (EVPN | VPN_IPV4 | VPN_IPV6)? rt = route_target NEWLINE
;

eos_rb_vrf
:
  VRF name = vrf_name NEWLINE
  (
    eos_rbv_address_family
    | eos_rb_inner
    | eos_rbv_local_as
    | eos_rbv_rd
    | eos_rbv_route_target
  )*
;