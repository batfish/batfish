parser grammar CiscoGrammar_acl;

import CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

access_list_remark_stanza
:
	ACCESS_LIST integer REMARK ~NEWLINE* NEWLINE
;

access_list_stanza
:
	standard_access_list_stanza
	| extended_access_list_stanza
;

extended_access_list_stanza
:
	ACCESS_LIST num = ACL_NUM_EXTENDED ala = access_list_action prot = protocol
	srcipr = extended_access_list_ip_range
	(
		alps_src = port_specifier
	)? dstipr = extended_access_list_ip_range
	(
		alps_dst = port_specifier
	)?
	(
		ECHO_REPLY
		| ECHO
		| ESTABLISHED
		| FRAGMENTS
		| LOG
		| LOG_INPUT
		| PACKET_TOO_BIG
		| PORT_UNREACHABLE
		| TTL_EXCEEDED
	)? NEWLINE
;

extended_access_list_ip_range
:
	(
		ip = IP_ADDRESS wildcard = IP_ADDRESS
	)
	| any = ANY
	| HOST ip = IP_ADDRESS
;

ip_access_list_extended_stanza
:
	IP ACCESS_LIST EXTENDED
	(
		name = VARIABLE
		| name = ACL_NUM_EXTENDED
	) NEWLINE
	(
		(
			isl += item_ip_access_list_extended_stanza
		)+
	)?
;

ip_access_list_standard_stanza
:
	IP ACCESS_LIST STANDARD
	(
		name = VARIABLE
		| name = ACL_NUM_STANDARD
	) NEWLINE
	(
		(
			isl += item_ip_access_list_standard_stanza
		)+
		| closing_comment
	)
;

ip_as_path_access_list_stanza
:
	IP AS_PATH ACCESS_LIST name = DEC action = access_list_action
	(
		remainder += ~NEWLINE
	)+ NEWLINE
;

ip_community_list_expanded_stanza
:
	(
		(
			EXPANDED name = VARIABLE
		)
		| name = COMMUNITY_LIST_NUM_EXPANDED
	) ala = access_list_action
	(
		remainder += ~NEWLINE
	)+ NEWLINE
;

ip_community_list_standard_stanza
:
	(
		(
			STANDARD name = VARIABLE
		)
		| name = COMMUNITY_LIST_NUM_STANDARD
	) ala = access_list_action
	(
		communities += community
	)+ NEWLINE
;

ip_community_list_stanza
:
	IP COMMUNITY_LIST
	(
		ip_community_list_expanded_stanza
		| ip_community_list_standard_stanza
	)
;

ip_prefix_list_line_stanza
:
	IP PREFIX_LIST name = VARIABLE
	(
		SEQ DEC
	)? action = access_list_action prefix = IP_ADDRESS FORWARD_SLASH prefix_length
	= integer
	(
		(
			GE minpl = integer
		)
		|
		(
			LE maxpl = integer
		)
	)?
;

item_ip_access_list_extended_stanza
:
	(
		(
			ala = access_list_action prot = protocol srcipr =
			extended_access_list_ip_range
			(
				alps_src = port_specifier
			)? dstipr = extended_access_list_ip_range
			(
				alps_dst = port_specifier
			)?
			(
				ECHO_REPLY
				| ECHO
				| ESTABLISHED
				| FRAGMENTS
				| LOG
				| PACKET_TOO_BIG
				| PORT_UNREACHABLE
				| REDIRECT
				| TIME_EXCEEDED
				| TTL_EXCEEDED
				| UNREACHABLE
			)? NEWLINE
		)
	)
	|
	(
		(
			(
				access_list_action protocol extended_access_list_ip_range port_specifier?
				extended_access_list_ip_range port_specifier? REFLECT
			)
			| DYNAMIC
			| EVALUATE
			| REMARK
		) ~NEWLINE* NEWLINE
	)
;

item_ip_access_list_standard_ip_range
:
	(
		ip = IP_ADDRESS
		(
			wildcard = IP_ADDRESS
		)?
	)
	| any = ANY
	|
	(
		HOST ip = IP_ADDRESS
	)
;

item_ip_access_list_standard_stanza
:
	(
		ala = access_list_action ipr = item_ip_access_list_standard_ip_range NEWLINE
	)
	|
	(
		REMARK ~NEWLINE* NEWLINE
	)
;

standard_access_list_stanza
:
	ACCESS_LIST num = ACL_NUM_STANDARD ala = access_list_action ipr =
	standard_access_list_ip_range LOG? NEWLINE
;

standard_access_list_ip_range
:
	(
		ip = IP_ADDRESS wildcard = IP_ADDRESS?
	)
	| ANY
;

