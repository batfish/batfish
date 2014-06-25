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

integer
:
	num = DEC
	| num = HEX
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
		op = EQ
		(
			args += port
		)+
	)
	|
	(
		op = GT arg = port
	)
	|
	(
		op = NEQ arg = port
	)
	|
	(
		op = LT arg = port
	)
	|
	(
		op = RANGE arg1 = port arg2 = port
	)
;

port
:
	p = DEC
	| p = BOOTPC
	| p = BOOTPS
	| p = BGP
	| p = CMD
	| p = DOMAIN
	| p = FTP
	| p = FTP_DATA
	| p = ISAKMP
	| p = LPD
	| p = NETBIOS_DGM
	| p = NETBIOS_NS
	| p = NETBIOS_SS
	| p = NON500_ISAKMP
	| p = NTP
	| p = PIM_AUTO_RP
	| p = POP3
	| p = SMTP
	| p = SNMP
	| p = SNMPTRAP
	| p = SYSLOG
	| p = TACACS
	| p = TELNET
	| p = TFTP
	| p = WWW
;

protocol
:
	p = DEC
	| p = ESP
	| p = GRE
	| p = ICMP
	| p = IGMP
	| p = IP
	| p = OSPF
	| p = PIM
	| p = SCTP
	| p = TCP
	| p = UDP
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
	low = integer
	(
		DASH high = integer
	)?
;

