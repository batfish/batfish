parser grammar CiscoGrammar_interface;

import CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

description_if_stanza
:
	description_line
;

if_stanza
:
	description_if_stanza
	| ip_access_group_if_stanza
	| ip_address_if_stanza
	| ip_address_secondary_if_stanza
	| ip_ospf_cost_if_stanza
	| ip_ospf_dead_interval_if_stanza
	| ip_ospf_dead_interval_minimal_if_stanza
	| ip_policy_if_stanza
	| no_ip_address_if_stanza
	| null_if_stanza
	| shutdown_if_stanza
	| switchport_access_if_stanza
	| switchport_trunk_native_if_stanza
	| switchport_trunk_encapsulation_if_stanza
	| switchport_trunk_allowed_if_stanza
	| switchport_mode_access_stanza
	| switchport_mode_dynamic_auto_stanza
	| switchport_mode_dynamic_desirable_stanza
	| switchport_mode_trunk_stanza
;

interface_stanza
:
	INTERFACE iname = interface_name MULTIPOINT? NEWLINE interface_stanza_tail closing_comment?
;

interface_stanza_tail
:
	(
		ifsl += if_stanza
	)*
;

ip_access_group_if_stanza
:
	IP ACCESS_GROUP name = .
	(
		IN
		| OUT
	) NEWLINE
;

ip_address_if_stanza
:
	IP ADDRESS ( ip = IP_ADDRESS subnet = IP_ADDRESS | prefix = IP_PREFIX )
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
	IP OSPF COST cost = DEC NEWLINE
;

ip_ospf_dead_interval_if_stanza
:
	IP OSPF DEAD_INTERVAL seconds = DEC NEWLINE
;

ip_ospf_dead_interval_minimal_if_stanza
:
	IP OSPF DEAD_INTERVAL MINIMAL HELLO_MULTIPLIER mult = DEC NEWLINE
;

ip_policy_if_stanza:
   IP POLICY name=~NEWLINE NEWLINE
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
		| ATM
		| AUTO
		| BANDWIDTH
		| CABLELENGTH
		| CDP
		| CHANNEL
		| CHANNEL_GROUP
		| CHANNEL_PROTOCOL
		| CLNS
		| CLOCK
		| COUNTER
		| CRYPTO
		|
		(
			DSU BANDWIDTH
		)
		| DUPLEX
		| ENCAPSULATION
		| FAIR_QUEUE
		| FLOWCONTROL
		| FRAMING
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
				| DVMRP
				|
				(
					DIRECTED_BROADCAST
				)
				| FLOW
				| HELPER_ADDRESS
				| IGMP
				| IRDP
				| LOAD_SHARING
				| MROUTE_CACHE
				| MTU
				| MULTICAST
				|
				(
					OSPF
					(
						AUTHENTICATION
						| MESSAGE_DIGEST_KEY 
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
				| ROUTER
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
		| LANE
		| LAPB
		| LLDP
		| LOAD_INTERVAL
		| LOGGING
		| LRE
		| MAC_ADDRESS
		| MACRO
		| MANAGEMENT_ONLY
		| MAP_GROUP
		| MDIX
		| MEDIA_TYPE
		| MEMBER
		| MLS
		| MOBILITY
		| MOP
		| MPLS
		| MTU
		| NAMEIF
		| NEGOTIATION
		|
		(
			NTP BROADCAST
		)
		| PEER
		| PHYSICAL_LAYER
		| PORT_CHANNEL
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
		| SONET
		| SPANNING_TREE
		| SPEED
		| SNMP
		| SRR_QUEUE
		| STACK_MIB
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
		| TCAM
		| TRUST
		| TUNNEL
		| UDLD
		| VRF
		| VRRP
		| WRR_QUEUE
		| X25
		| XCONNECT
	) ~NEWLINE* NEWLINE
;

shutdown_if_stanza
:
	NO? SHUTDOWN NEWLINE
;

switchport_access_if_stanza
:
	SWITCHPORT ACCESS VLAN vlan = DEC NEWLINE
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
	SWITCHPORT TRUNK ENCAPSULATION e = switchport_trunk_encapsulation NEWLINE
;

switchport_trunk_native_if_stanza
:
	SWITCHPORT TRUNK NATIVE VLAN vlan = DEC NEWLINE
;
