parser grammar CiscoGrammar_ospf;

import CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

area_ipv6_ro_stanza
:
	AREA ~NEWLINE* NEWLINE
;

area_nssa_ro_stanza
:
	AREA num = integer NSSA NO_SUMMARY? NEWLINE
;

default_information_ipv6_ro_stanza
:
	DEFAULT_INFORMATION ~NEWLINE* NEWLINE
;

default_information_ro_stanza
:
	DEFAULT_INFORMATION ORIGINATE
	(
		(
			METRIC metric = DEC
		)
		|
		(
			METRIC_TYPE metric_type = DEC
		)
		| ALWAYS
		|
		(
			ROUTE_MAP map = VARIABLE
		)
	)* NEWLINE
;

ipv6_ro_stanza
:
	null_ipv6_ro_stanza
	| passive_interface_ipv6_ro_stanza
	| redistribute_ipv6_ro_stanza
;

ipv6_router_ospf_stanza
:
	IPV6 ROUTER OSPF procnum = integer NEWLINE
	(
		rosl += ipv6_ro_stanza
	)+ closing_comment
;

log_adjacency_changes_ipv6_ro_stanza
:
	LOG_ADJACENCY_CHANGES NEWLINE
;

maximum_paths_ro_stanza
:
	MAXIMUM_PATHS ~NEWLINE* NEWLINE
;

network_ro_stanza
:
	NETWORK ip = IP_ADDRESS sub = IP_ADDRESS AREA
	(
		area_int = integer
		| area_ip = IP_ADDRESS
	) NEWLINE
;

null_ipv6_ro_stanza
:
	area_ipv6_ro_stanza
	| comment_stanza
	| default_information_ipv6_ro_stanza
	| log_adjacency_changes_ipv6_ro_stanza
	| router_id_ipv6_ro_stanza
;

null_ro_stanza
:
	comment_stanza
	| null_standalone_ro_stanza
;

null_standalone_ro_stanza
:
	NO?
	(
		(
			AREA DEC AUTHENTICATION
		)
		| BFD
		| DISTRIBUTE_LIST
		| LOG_ADJACENCY_CHANGES
		| NSF
	) ~NEWLINE* NEWLINE
;

passive_interface_ipv6_ro_stanza
:
	NO? PASSIVE_INTERFACE ~NEWLINE* NEWLINE
;

passive_interface_default_ro_stanza
:
	PASSIVE_INTERFACE DEFAULT NEWLINE
;

passive_interface_ro_stanza
:
	NO? PASSIVE_INTERFACE i = VARIABLE NEWLINE
;

redistribute_bgp_ro_stanza
:
	REDISTRIBUTE BGP DEC
	(
		METRIC DEC
	)?
	(
		METRIC_TYPE DEC
	)? SUBNETS? NEWLINE
;

redistribute_ipv6_ro_stanza
:
	REDISTRIBUTE ~NEWLINE* NEWLINE
;

redistribute_connected_ro_stanza
:
	REDISTRIBUTE CONNECTED
	(
		(
			METRIC metric = DEC
		)
		| subnets = SUBNETS
	)* NEWLINE
;

redistribute_static_ro_stanza
:
	REDISTRIBUTE STATIC
	(
		(
			METRIC cost = integer
		)
		| subnets = SUBNETS
		|
		(
			ROUTE_MAP map = VARIABLE
		)
	)* NEWLINE
;

ro_stanza
:
	area_nssa_ro_stanza
	| default_information_ro_stanza
	| maximum_paths_ro_stanza
	| network_ro_stanza
	| null_ro_stanza
	| passive_interface_default_ro_stanza
	| passive_interface_ro_stanza
	| redistribute_bgp_ro_stanza
	| redistribute_connected_ro_stanza
	| redistribute_static_ro_stanza
	| router_id_ro_stanza
;

router_id_ipv6_ro_stanza
:
	ROUTER_ID ~NEWLINE* NEWLINE
;

router_id_ro_stanza
:
	ROUTER_ID ip = IP_ADDRESS NEWLINE
;

router_ospf_stanza
:
	ROUTER OSPF procnum = integer NEWLINE
	(
		rosl += ro_stanza
	)+ closing_comment
;
