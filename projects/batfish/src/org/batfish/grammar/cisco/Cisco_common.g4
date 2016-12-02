parser grammar Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

access_list_action
:
   PERMIT
   | DENY
;

address_family_footer
:
   (
      (
         EXIT_ADDRESS_FAMILY
         | EXIT
      ) NEWLINE
   )?
;

banner
:
   (
      (
         (
            ESCAPE_C ~ESCAPE_C* ESCAPE_C
         )
         |
         (
            POUND ~POUND* POUND
         )
         |
         (
            NEWLINE ~EOF_LITERAL* EOF_LITERAL
         )
      )
   ) NEWLINE?
;

banner_type
:
   CONFIG_SAVE
   | EXEC
   | INCOMING
   | LOGIN
   | MOTD
   | PROMPT_TIMEOUT
   | SLIP_PPP
;

community
:
   com = COMMUNITY_NUMBER
   | com = DEC
   | com = GSHUT
   | com = INTERNET
   | com = LOCAL_AS
   | com = NO_ADVERTISE
   | com = NO_EXPORT
;

description_line
:
   DESCRIPTION text = RAW_TEXT? NEWLINE
;

dscp_type
:
   DEC
   | AF11
   | AF12
   | AF13
   | AF21
   | AF22
   | AF23
   | AF31
   | AF32
   | AF33
   | AF41
   | AF42
   | AF43
   | CS1
   | CS2
   | CS3
   | CS4
   | CS5
   | CS6
   | CS7
   | DEFAULT
   | EF
;

ec_literal
:
   DEC COLON DEC
;

exact_match [String matchText]
:
   {(_input.LT(1).getType() == VARIABLE || _input.LT(1).getType() == COMMUNITY_LIST_NUM_EXPANDED) && _input.LT(1).getText().equals($matchText)}?

   (
      VARIABLE
      | COMMUNITY_LIST_NUM_EXPANDED
   )
;

exit_line
:
   EXIT NEWLINE
;

extended_community
:
   ec_literal
;

hash_comment
:
   POUND RAW_TEXT
;

icmp_object_type
:
   DEC
   | ALTERNATE_ADDRESS
   | CONVERSION_ERROR
   | ECHO
   | ECHO_REPLY
   | INFORMATION_REPLY
   | INFORMATION_REQUEST
   | MASK_REPLY
   | MASK_REQUEST
   | MOBILE_REDIRECT
   | PARAMETER_PROBLEM
   | REDIRECT
   | ROUTER_ADVERTISEMENT
   | ROUTER_SOLICITATION
   | SOURCE_QUENCH
   | TIME_EXCEEDED
   | TIMESTAMP_REPLY
   | TIMESTAMP_REQUEST
   | TRACEROUTE
   | UNREACHABLE
;

int_expr
:
   (
      (
         PLUS
         | DASH
      )? DEC
   )
   | IGP_COST
   | RP_VARIABLE
;

interface_name
:
   (
      name_prefix_alpha = M_Interface_PREFIX
      (
         (
            (
               name_middle_parts += M_Interface_PREFIX
            )? name_middle_parts += DEC
            (
               name_middle_parts += FORWARD_SLASH
               | name_middle_parts += PERIOD
               | name_middle_parts += COLON
            )
         )*
         | name_middle_parts += MODULE
      ) range?
   )
   |
   (
      name = variable
      (
         (
            COLON
            | FORWARD_SLASH
            | PERIOD
         ) DEC
      )*
   )
;

isis_level
:
   LEVEL_1
   | LEVEL_1_2
   | LEVEL_2
;

line_type
:
// intentional blank

   | AUX
   | CON
   | CONSOLE
   | DEFAULT
   |
   (
      TEMPLATE name = variable
   )
   | TTY
   | VTY
;

ospf_route_type
:
   (
      EXTERNAL DEC
   )
   | INTERNAL
   |
   (
      NSSA_EXTERNAL DEC
   )
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
   | AOL
   | BGP
   | BIFF
   | BOOTPC
   | BOOTPS
   | CHARGEN
   | CITRIX_ICA
   | CMD
   | CTIQBE
   | DAYTIME
   | DISCARD
   | DNSIX
   | DOMAIN
   | ECHO
   | EXEC
   | FINGER
   | FTP
   | FTP_DATA
   | GOPHER
   | H323
   | HTTPS
   | HOSTNAME
   | IDENT
   | IMAP4
   | IRC
   | ISAKMP
   | KERBEROS
   | KLOGIN
   | KSHELL
   | LDAP
   | LDAPS
   | LPD
   | LOGIN
   | LOTUSNOTES
   | MLAG
   | MOBILE_IP
   | MSRPC
   | NAMESERVER
   | NETBIOS_DGM
   | NETBIOS_NS
   | NETBIOS_SS
   | NETBIOS_SSN
   | NNTP
   | NON500_ISAKMP
   | NTP
   | PCANYWHERE_DATA
   | PCANYWHERE_STATUS
   | PIM_AUTO_RP
   | POP2
   | POP3
   | PPTP
   | RADIUS
   | RADIUS_ACCT
   | RIP
   | SECUREID_UDP
   | SMTP
   | SNMP
   | SNMP_TRAP
   | SNMPTRAP
   | SQLNET
   | SSH
   | SUNRPC
   | SYSLOG
   | TACACS
   | TACACS_DS
   | TALK
   | TELNET
   | TFTP
   | TIME
   | UUCP
   | WHO
   | WHOIS
   | WWW
   | XDMCP
;

prefix_set_elem
:
   (
      ipa = IP_ADDRESS
      | prefix = IP_PREFIX
      | ipv6a = IPV6_ADDRESS
      | ipv6_prefix = IPV6_PREFIX
   )
   (
      (
         GE minpl = DEC
      )
      |
      (
         LE maxpl = DEC
      )
      |
      (
         EQ eqpl = DEC
      )
   )*
;

protocol
:
   AHP
   | DEC
   | EIGRP
   | ESP
   | GRE
   | ICMP
   | ICMP6
   | ICMPV6
   | IGMP
   | IGRP
   | IP
   | IPINIP
   | IPSEC
   | IPV4
   | IPV6
   | NOS
   | OSPF
   | PIM
   | PPTP
   | SCTP
   | SNP
   | TCP
   | TCP_UDP
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

route_policy_params_list
:
   params_list += variable
   (
      COMMA params_list += variable
   )*
;

rp_community_set_elem
:
   (
      prefix = rp_community_set_elem_half COLON suffix =
      rp_community_set_elem_half
   )
   | community
;

rp_community_set_elem_half
:
   value = DEC
   | var = RP_VARIABLE
   |
   (
      BRACKET_LEFT first = DEC PERIOD PERIOD last = DEC BRACKET_RIGHT
   )
   | ASTERISK
;

rp_subrange
:
   first = int_expr
   |
   (
      BRACKET_LEFT first = int_expr PERIOD PERIOD last = int_expr BRACKET_RIGHT
   )
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

unrecognized_line
:
   NO? VARIABLE ~NEWLINE* NEWLINE
;

variable
:
   ~NEWLINE
;

variable_permissive
:
   (
      ~( EXTENDED | NEWLINE | STANDARD | WS )
   )+
;
