parser grammar CiscoGrammar_interface;

import CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

if_stanza
:
	ip_access_group_if_stanza
	| ip_address_if_stanza
	| ip_address_secondary_if_stanza
	| ip_ospf_cost_if_stanza
	| ip_ospf_dead_interval_if_stanza
	| ip_ospf_dead_interval_minimal_if_stanza
	| no_ip_address_if_stanza
	| null_if_stanza
	| shutdown_if_stanza
	| switchport_if_stanza
;

interface_stanza
:
	INTERFACE iname = interface_name NEWLINE
	(
		ifsl += if_stanza
	)* closing_comment
;

ip_access_group_if_stanza
:
	IP ACCESS_GROUP
	(
		name = DEC
		| name = VARIABLE
	)
	(
		IN
		| OUT
	) NEWLINE
;

ip_address_if_stanza
:
	IP ADDRESS ip = IP_ADDRESS subnet = IP_ADDRESS
	(
		STANDBY IP_ADDRESS
	)? NEWLINE
;

ip_address_secondary_if_stanza
:
	IP ADDRESS ip = IP_ADDRESS subnet = IP_ADDRESS SECONDARY NEWLINE
;

ip_ospf_cost_if_stanza
:
	IP OSPF COST cost = integer NEWLINE
;

ip_ospf_dead_interval_if_stanza
:
	IP OSPF DEAD_INTERVAL seconds = integer NEWLINE
;

ip_ospf_dead_interval_minimal_if_stanza
:
	IP OSPF DEAD_INTERVAL MINIMAL HELLO_MULTIPLIER mult = integer NEWLINE
;

no_ip_address_if_stanza
:
	NO IP ADDRESS NEWLINE
;

null_if_stanza
:
	comment_stanza
	|
	(
		NO? SWITCHPORT NEWLINE
	)
	| null_standalone_if_stanza
;

null_standalone_if_stanza
:
	NO?
	(
		ARP
		| ASYNC
		| AUTO
		| BANDWIDTH
		| CDP
		| CHANNEL
		| CHANNEL_GROUP
		| CHANNEL_PROTOCOL
		| CLNS
		| CLOCK
		| CRYPTO
		| DESCRIPTION
		| DUPLEX
		| ENCAPSULATION
		| FAIR_QUEUE
		| FULL_DUPLEX
		| GROUP_RANGE
		| HALF_DUPLEX
		| HOLD_QUEUE
		|
		(
			IP
			(
				ACCOUNTING
				| ARP
				| CGMP
				| DHCP
				|
				(
					DIRECTED_BROADCAST
				)
				| FLOW
				| HELPER_ADDRESS
				| IGMP
				| IRDP
				| MROUTE_CACHE
				| MTU
				| MULTICAST
				|
				(
					OSPF
					(
						AUTHENTICATION
						| NETWORK
						| PRIORITY
					)
				)
				| NAT
				| PIM
				| POLICY
				| PROXY_ARP
				| REDIRECTS
				| RIP
				| ROUTE_CACHE
				| TCP
				| UNNUMBERED
				| UNREACHABLES
				| VERIFY
				| VIRTUAL_REASSEMBLY
				| VRF
			)
		)
		| IPV6
		| ISDN
		| KEEPALIVE
		| LAPB
		| LLDP
		| LOAD_INTERVAL
		| LOGGING
		| LRE
		| MAC_ADDRESS
		| MACRO
		| MANAGEMENT_ONLY
		| MDIX
		| MEDIA_TYPE
		| MEMBER
		| MLS
		| MOP
		| MPLS
		| MTU
		| NAMEIF
		| NEGOTIATION
		| PEER
		| PHYSICAL_LAYER
		| POWER
		| PPP
		| PRIORITY_QUEUE
		| QOS
		| QUEUE_SET
		| RCV_QUEUE
		| ROUTE_CACHE
		| SECURITY_LEVEL
		| SERIAL
		| SERVICE_MODULE
		| SERVICE_POLICY
		| SPANNING_TREE
		| SPEED
		| SNMP
		| SRR_QUEUE
		| STANDBY
		| STORM_CONTROL
		|
		(
			SWITCHPORT
			(
				EMPTY
				|
				(
					MODE PRIVATE_VLAN
				)
				| NONEGOTIATE
				| PORT_SECURITY
				| VOICE
				| VLAN
			)
		)
		| TAG_SWITCHING
		| TRUST
		| TUNNEL
		| UDLD
		| VRF
		| VRRP
		| WRR_QUEUE
		| X25
	) ~NEWLINE* NEWLINE
;

shutdown_if_stanza
:
	SHUTDOWN NEWLINE
;

switchport_access_if_stanza
:
	SWITCHPORT ACCESS VLAN i = integer NEWLINE
;

switchport_if_stanza
:
	switchport_access_if_stanza
	| switchport_trunk_if_stanza
	| switchport_mode_access_stanza
	| switchport_mode_dynamic_auto_stanza
	| switchport_mode_dynamic_desirable_stanza
	| switchport_mode_trunk_stanza
;

switchport_mode_access_stanza
:
	SWITCHPORT MODE ACCESS NEWLINE
;

switchport_mode_dynamic_auto_stanza
:
	SWITCHPORT MODE DYNAMIC AUTO NEWLINE
;

switchport_mode_dynamic_desirable_stanza
:
	SWITCHPORT MODE DYNAMIC DESIRABLE NEWLINE
;

switchport_mode_trunk_stanza
:
	SWITCHPORT MODE TRUNK NEWLINE
;

switchport_trunk_allowed_if_stanza
:
	SWITCHPORT TRUNK ALLOWED VLAN ADD? r = range NEWLINE
;

switchport_trunk_encapsulation_if_stanza
:
	SWITCHPORT TRUNK ENCAPSULATION
	(
		DOT1Q
		| ISL
		| NEGOTIATE
	) NEWLINE
;

switchport_trunk_if_stanza
:
	switchport_trunk_native_if_stanza
	| switchport_trunk_encapsulation_if_stanza
	| switchport_trunk_allowed_if_stanza
;

switchport_trunk_native_if_stanza
:
	SWITCHPORT TRUNK NATIVE VLAN i = integer NEWLINE
;
