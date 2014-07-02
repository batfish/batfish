parser grammar CiscoGrammar_acl;

import CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

access_list_stanza
:
	standard_access_list_stanza
	| extended_access_list_stanza
;

access_list_ip_range
:
	(
		ip = IP_ADDRESS wildcard = IP_ADDRESS
	)
	| ANY
	| HOST ip = IP_ADDRESS
;

extended_access_list_null_tail
:
	(
		(
			access_list_action protocol access_list_ip_range port_specifier?
			access_list_ip_range port_specifier? REFLECT
		)
		| DYNAMIC
		| EVALUATE
		| REMARK
	) ~NEWLINE* NEWLINE
;

extended_access_list_stanza
:
	(
		ACCESS_LIST firstnum = ACL_NUM_EXTENDED
		(
			extended_access_list_tail
			| extended_access_list_null_tail
		)
		(
			ACCESS_LIST num = ACL_NUM_EXTENDED
			{$firstnum.text.equals($num.text)}?

			(
				extended_access_list_tail
				| extended_access_list_null_tail
			)
		)*
	)
	| ip_access_list_extended_stanza
;

extended_access_list_tail
:
	ala = access_list_action prot = protocol srcipr = access_list_ip_range
	(
		alps_src = port_specifier
	)? dstipr = access_list_ip_range
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
		| REDIRECT
		| TIME_EXCEEDED
		| TTL_EXCEEDED
		| UNREACHABLE
	)? NEWLINE
;

ip_access_list_extended_stanza
:
	IP ACCESS_LIST EXTENDED
	(
		name = VARIABLE
		| name = ACL_NUM_EXTENDED
	) NEWLINE
	(
		extended_access_list_tail
		| extended_access_list_null_tail
	)*
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
			standard_access_list_tail
			| standard_access_list_null_tail
		)+
		| closing_comment
	)
;

ip_as_path_access_list_stanza
:
	IP AS_PATH ACCESS_LIST firstname = DEC ip_as_path_access_list_tail
	(
		IP AS_PATH ACCESS_LIST name = DEC
		{$firstname.text.equals($name.text)}?

		ip_as_path_access_list_tail
	)*
;

ip_as_path_access_list_tail
:
	action = access_list_action
	(
		remainder += ~NEWLINE
	)+ NEWLINE
;

ip_community_list_expanded_stanza
:
	named = ip_community_list_expanded_named_stanza
	| numbered = ip_community_list_expanded_numbered_stanza
;

ip_community_list_expanded_named_stanza
locals [boolean again]
:
	IP COMMUNITY_LIST EXPANDED name = VARIABLE ip_community_list_expanded_tail
	{
		$again = _input.LT(1).getType() == IP &&
		_input.LT(2).getType() == COMMUNITY_LIST &&
		_input.LT(3).getType() == EXPANDED &&
		_input.LT(4).getType() == VARIABLE &&
		_input.LT(4).getText().equals($name);
	}

	(
		{$again}?

		ip_community_list_expanded_named_stanza
		|
		{!$again}?

	)
;

ip_community_list_expanded_numbered_stanza
locals [boolean again]
:
	IP COMMUNITY_LIST name = COMMUNITY_LIST_NUM_EXPANDED
	ip_community_list_expanded_tail
	{
		$again = _input.LT(1).getType() == IP &&
		_input.LT(2).getType() == COMMUNITY_LIST &&
		_input.LT(3).getType() == COMMUNITY_LIST_NUM_EXPANDED &&
		_input.LT(3).getText().equals($name);
	}

	(
		{$again}?

		ip_community_list_expanded_numbered_stanza
		|
		{!$again}?

	)
;

ip_community_list_expanded_tail
:
	ala = access_list_action
	(
		remainder += ~NEWLINE
	)+ NEWLINE
;

ip_community_list_standard_stanza
:
	(
		IP COMMUNITY_LIST
		(
			STANDARD firstname = VARIABLE
		)
		| firstname = COMMUNITY_LIST_NUM_STANDARD
	) ip_community_list_standard_tail
	(
		(
			IP COMMUNITY_LIST
			(
				STANDARD name = VARIABLE
			)
			| name = COMMUNITY_LIST_NUM_STANDARD
		)
		{$firstname.text.equals($name.text)}?

		ip_community_list_standard_tail
	)*
;

ip_community_list_standard_tail
:
	ala = access_list_action
	(
		communities += community
	)+ NEWLINE
;

ip_prefix_list_stanza
:
	IP PREFIX_LIST firstname = VARIABLE ip_prefix_list_tail
	(
		IP PREFIX_LIST name = VARIABLE
		{$firstname.text.equals($name.text)}?

		ip_prefix_list_tail
	)*
;

ip_prefix_list_tail
:
	(
		SEQ seqnum = DEC
	)? action = access_list_action prefix = IP_ADDRESS FORWARD_SLASH prefix_length
	= DEC
	(
		(
			GE minpl = DEC
		)
		|
		(
			LE maxpl = DEC
		)
	)*
;

standard_access_list_null_tail
:
	REMARK ~NEWLINE* NEWLINE
;

standard_access_list_stanza
:
	(
		ACCESS_LIST firstnum = ACL_NUM_STANDARD
		(
			standard_access_list_tail
			| standard_access_list_null_tail
		)
		(
			ACCESS_LIST num = ACL_NUM_STANDARD
			{$firstnum.text.equals($num.text)}?

			(
				standard_access_list_tail
				| standard_access_list_null_tail
			)
		)*
	)
	| ip_access_list_standard_stanza
;

standard_access_list_tail
:
	ala = access_list_action ipr = access_list_ip_range LOG? NEWLINE
;

