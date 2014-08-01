parser grammar CiscoGrammar_bgp;

import CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

address_family_rb_stanza
:
	ADDRESS_FAMILY
	(
		IPV4
		| IPV6
		| VPNV4
		| VPNV6
	) MULTICAST? 
	(
		VRF vrf_name = VARIABLE
	)? NEWLINE address_family_rb_stanza_tail EXIT_ADDRESS_FAMILY NEWLINE
	closing_comment
;

address_family_rb_stanza_tail
:
	(
		afsl += af_stanza
	)+
;

af_stanza
:
	aggregate_address_af_stanza
	| default_metric_af_stanza
	| neighbor_activate_af_stanza
	| neighbor_default_originate_af_stanza
	| neighbor_filter_list_af_stanza
	| neighbor_next_hop_self_af_stanza
	| neighbor_peer_group_assignment_af_stanza
	| neighbor_prefix_list_af_stanza
	| neighbor_route_map_af_stanza
	| neighbor_route_reflector_client_af_stanza
	| neighbor_send_community_af_stanza
	| network_af_stanza
	| network6_af_stanza
	| null_af_stanza
	| redistribute_connected_af_stanza
	| redistribute_static_af_stanza
;

aggregate_address_af_stanza
:
	aggregate_address_tail_bgp
;

aggregate_address_rb_stanza
:
	aggregate_address_tail_bgp
;

aggregate_address_tail_bgp
:
	AGGREGATE_ADDRESS network = IP_ADDRESS subnet = IP_ADDRESS SUMMARY_ONLY?
	NEWLINE
;

auto_summary_af_stanza
:
	NO? AUTO_SUMMARY NEWLINE
;

cluster_id_bgp_rb_stanza
:
	BGP CLUSTER_ID
	(
		id = DEC
		| id = IP_ADDRESS
	) NEWLINE
;

default_metric_af_stanza
:
	default_metric_tail_bgp
;

default_metric_rb_stanza
:
	default_metric_tail_bgp
;

default_metric_tail_bgp
:
	DEFAULT_METRIC metric = DEC NEWLINE
;

neighbor_activate_af_stanza
:
	NEIGHBOR
	(
		neighbor = IP_ADDRESS
		| pg = ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
	) ACTIVATE NEWLINE
;

neighbor_description_rb_stanza
:
   NEIGHBOR ( IP_ADDRESS | IPV6_ADDRESS ) DESCRIPTION (text+=M_DESCRIPTION_NON_NEWLINE)* M_DESCRIPTION_NEWLINE
;

neighbor_ebgp_multihop_rb_stanza
:
	NEIGHBOR
	(
		IP_ADDRESS
		| VARIABLE
	) EBGP_MULTIHOP hop = DEC NEWLINE
;

neighbor_default_originate_af_stanza
:
	NEIGHBOR
	(
		name = IP_ADDRESS
		| name = VARIABLE
	) DEFAULT_ORIGINATE
	(
		ROUTE_MAP map = VARIABLE
	)? NEWLINE
;

neighbor_filter_list_af_stanza
:
	neighbor_filter_list_tail_bgp
;

neighbor_filter_list_tail_bgp
:
	NEIGHBOR neighbor = . FILTER_LIST num = DEC
	(
		IN
		| OUT
	) NEWLINE
;

neighbor_next_hop_self_af_stanza
:
	neighbor_next_hop_self_tail_bgp
;

neighbor_next_hop_self_rb_stanza
:
	neighbor_next_hop_self_tail_bgp
;

neighbor_next_hop_self_tail_bgp
:
	NEIGHBOR
	(
		neighbor = IP_ADDRESS
		| neighbor = VARIABLE
	) NEXT_HOP_SELF NEWLINE
;

neighbor_peer_group_assignment_af_stanza
:
	neighbor_peer_group_assignment_tail_bgp
;

neighbor_peer_group_assignment_rb_stanza
:
	neighbor_peer_group_assignment_tail_bgp
;

neighbor_peer_group_assignment_tail_bgp
:
	NEIGHBOR
	(
		address = IP_ADDRESS
	) PEER_GROUP name = VARIABLE NEWLINE
;

neighbor_peer_group_creation_rb_stanza
:
	NEIGHBOR name = VARIABLE PEER_GROUP NEWLINE
;

neighbor_prefix_list_af_stanza
:
	neighbor_prefix_list_tail_bgp
;

neighbor_prefix_list_rb_stanza
:
	neighbor_prefix_list_tail_bgp
;

neighbor_prefix_list_tail_bgp
:
	NEIGHBOR
	(
		neighbor = IP_ADDRESS
		| neighbor = VARIABLE
	) PREFIX_LIST list_name = VARIABLE
	(
		IN
		| OUT
	) NEWLINE
;

neighbor_remote_as_rb_stanza
:
	NEIGHBOR
	(
		pg_ip = IP_ADDRESS
		| pg_var = VARIABLE
	) REMOTE_AS as = DEC NEWLINE
;

neighbor_route_map_af_stanza
:
	neighbor_route_map_tail_bgp
;

neighbor_route_map_rb_stanza
:
	neighbor_route_map_tail_bgp
;

neighbor_route_map_tail_bgp
:
	NEIGHBOR
	(
		pg = IP_ADDRESS
		| pg = VARIABLE
	) ROUTE_MAP name = VARIABLE
	(
		IN
		| OUT
	) NEWLINE
;

neighbor_remove_private_as_af_stanza
:
	NEIGHBOR
	(
		IP_ADDRESS
		| VARIABLE
	) REMOVE_PRIVATE_AS NEWLINE
;

neighbor_route_reflector_client_af_stanza
:
	NEIGHBOR
	(
		pg = VARIABLE
		| pg = IP_ADDRESS
	) ROUTE_REFLECTOR_CLIENT NEWLINE
;

neighbor_send_community_af_stanza
:
	neighbor_send_community_tail_bgp
;

neighbor_send_community_rb_stanza
:
	neighbor_send_community_tail_bgp
;

neighbor_send_community_tail_bgp
:
	NEIGHBOR
	(
		neighbor = IP_ADDRESS
		| neighbor = VARIABLE
	) SEND_COMMUNITY EXTENDED? BOTH? NEWLINE
;

neighbor_shutdown_rb_stanza
:
	NEIGHBOR
	(
		neighbor = IP_ADDRESS
		| neighbor = VARIABLE
	) SHUTDOWN NEWLINE
;

neighbor_update_source_rb_stanza
:
	NEIGHBOR
	(
		neighbor = IP_ADDRESS
		| neighbor = VARIABLE
	) UPDATE_SOURCE source = VARIABLE NEWLINE
;

network_af_stanza
:
	network_tail_bgp
;

network_rb_stanza
:
	network_tail_bgp
;

network_tail_bgp
:
	NETWORK ip = IP_ADDRESS
	(
		MASK mask = IP_ADDRESS
	)? NEWLINE
;

network6_af_stanza
:
	network6_tail_bgp
;

network6_rb_stanza
:
	network6_tail_bgp
;

network6_tail_bgp
:
	NETWORK ip6 = IPV6_ADDRESS
	(
		FORWARD_SLASH DEC
	)? NEWLINE
;

no_neighbor_activate_af_stanza
:
	NO NEIGHBOR
	(
		ip = IP_ADDRESS
	) ACTIVATE NEWLINE
;

null_af_stanza
:
	comment_stanza
	| neighbor_remove_private_as_af_stanza
	| no_neighbor_activate_af_stanza
	| null_standalone_af_stanza
;

null_rb_stanza
:
	comment_stanza
	| null_standalone_rb_stanza
;

null_standalone_af_stanza
:
	NO?
	(
		(
			AGGREGATE_ADDRESS IPV6_ADDRESS
		)
		| AUTO_SUMMARY
		| BGP
		| MAXIMUM_PATHS
		|
		(
			NEIGHBOR
			(
				(
					(
						IP_ADDRESS
						| VARIABLE
					)
					(
						MAXIMUM_PREFIX
						| NEXT_HOP_SELF
						| SOFT_RECONFIGURATION
					)
				)
				| IPV6_ADDRESS
			)
		)
		| SYNCHRONIZATION
	) ~NEWLINE* NEWLINE
;

null_standalone_rb_stanza
:
	NO?
	(
		AUTO_SUMMARY
		|
		(
			BGP
			(
				DAMPENING
				| DEFAULT
				| DETERMINISTIC_MED
				| GRACEFUL_RESTART
				| LOG_NEIGHBOR_CHANGES
			)
		)
		| MAXIMUM_PATHS
		|
		(
			NEIGHBOR
			(
				(
					(
						IP_ADDRESS
						| IPV6_ADDRESS
						| VARIABLE
					)
					(
						FALL_OVER
						| PASSWORD
						| REMOVE_PRIVATE_AS
						| SOFT_RECONFIGURATION
						| TIMERS
						| TRANSPORT
						| VERSION
					)
				)
				| IPV6_ADDRESS
			)
		)
		| SYNCHRONIZATION
	) ~NEWLINE* NEWLINE
;

rb_stanza
:
	address_family_rb_stanza
	| aggregate_address_rb_stanza
	| cluster_id_bgp_rb_stanza
	| default_metric_rb_stanza
	| neighbor_description_rb_stanza
	| neighbor_ebgp_multihop_rb_stanza
	| neighbor_next_hop_self_rb_stanza
	| neighbor_peer_group_creation_rb_stanza
	| neighbor_peer_group_assignment_rb_stanza
	| neighbor_prefix_list_rb_stanza
	| neighbor_remote_as_rb_stanza
	| neighbor_route_map_rb_stanza
	| neighbor_send_community_rb_stanza
	| neighbor_shutdown_rb_stanza
	| neighbor_update_source_rb_stanza
	| network_rb_stanza
	| network6_rb_stanza
	| null_rb_stanza
	| redistribute_connected_rb_stanza
	| redistribute_ospf_rb_stanza
	| redistribute_static_rb_stanza
	| router_id_bgp_rb_stanza
;

redistribute_connected_af_stanza
:
	redistribute_connected_tail_bgp
;

redistribute_connected_rb_stanza
:
	redistribute_connected_tail_bgp
;

redistribute_connected_tail_bgp
:
	REDISTRIBUTE CONNECTED
	(
		(
			ROUTE_MAP map = VARIABLE
		)
		|
		(
			METRIC metric = DEC
		)
	)* NEWLINE
;

redistribute_ospf_rb_stanza
:
	redistribute_ospf_tail_bgp
;

redistribute_ospf_af_stanza
:
	redistribute_ospf_tail_bgp
;

redistribute_ospf_tail_bgp
:
	REDISTRIBUTE OSPF procnum = DEC
	(
		(
			ROUTE_MAP map = VARIABLE
		)
		|
		(
			METRIC metric = DEC
		)
	)* NEWLINE
;

redistribute_static_af_stanza
:
	redistribute_static_tail_bgp
;

redistribute_static_rb_stanza
:
	redistribute_static_tail_bgp
;

redistribute_static_tail_bgp
:
	REDISTRIBUTE STATIC
	(
		(
			ROUTE_MAP map = VARIABLE
		)
		|
		(
			METRIC metric = DEC
		)
	)* NEWLINE
;

router_bgp_stanza
:
	ROUTER BGP procnum = DEC NEWLINE router_bgp_stanza_tail
;

router_bgp_stanza_tail
:
	(
		rbsl += rb_stanza
	)+ closing_comment
	(
		afrbsl += address_family_rb_stanza
	)*
;

router_id_bgp_rb_stanza
:
	BGP ROUTER_ID routerid = IP_ADDRESS NEWLINE
;
