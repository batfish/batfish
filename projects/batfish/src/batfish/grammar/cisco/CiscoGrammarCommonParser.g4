parser grammar CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

access_list_action
:
	PERMIT
	| DENY
;

closing_comment
:
	COMMENT_CLOSING_LINE
;

comment_stanza
:
	COMMENT_LINE
;

community
:
	(
		part1 = DEC com = COLON part2 = DEC
	)
	| com = DEC
	| com = INTERNET
	| com = LOCAL_AS
	| com = NO_ADVERTISE
	| com = NO_EXPORT
;

description_line
:
	DESCRIPTION text=M_DESCRIPTION_NON_NEWLINE? NEWLINE
;

exact_match [String matchText]
:
	{(_input.LT(1).getType() == VARIABLE || _input.LT(1).getType() == COMMUNITY_LIST_NUM_EXPANDED) && _input.LT(1).getText().equals($matchText)}?

	(
		VARIABLE
		| COMMUNITY_LIST_NUM_EXPANDED
	)
;

interface_name
:
	name = VARIABLE
	(
		FORWARD_SLASH x = DEC
	)?
;

port_specifier
:
	(
		EQ
		(
			args += port
		)+
	)
	|
	(
		GT arg = port
	)
	|
	(
		NEQ arg = port
	)
	|
	(
		LT arg = port
	)
	|
	(
		RANGE arg1 = port arg2 = port
	)
;

port
:
	DEC
	| BOOTPC
	| BOOTPS
	| BGP
	| CMD
	| DOMAIN
	| EXEC
	| FTP
	| FTP_DATA
	| IDENT
	| ISAKMP
	| LPD
	| MLAG
	| NETBIOS_DGM
	| NETBIOS_NS
	| NETBIOS_SS
	| NNTP
	| NON500_ISAKMP
	| NTP
	| PIM_AUTO_RP
	| POP3
	| SMTP
	| SNMP
	| SNMPTRAP
	| SSH
	| SUNRPC
	| SYSLOG
	| TACACS
	| TELNET
	| TFTP
	| WWW
;

protocol
:
	AHP
	| DEC
	| ESP
	| GRE
	| ICMP
	| IGMP
	| IP
	| IPINIP
	| OSPF
	| PIM
	| SCTP
	| TCP
	| UDP
	| VRRP
;

range
:
	(
		range_list += subrange
		(
			COMMA range_list += subrange
		)*
	)
	| NONE
;

subrange
:
	low = DEC
	(
		DASH high = DEC
	)?
;

switchport_trunk_encapsulation
:
	DOT1Q
	| ISL
	| NEGOTIATE
;