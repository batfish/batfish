parser grammar FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

s_apply_groups
:
   APPLY_GROUPS name = VARIABLE
;

s_description
:
   DESCRIPTION description = M_Description_DESCRIPTION?
;

port
:
   BGP
   | DEC
   | DOMAIN
   | LDP
   | NTP
   | SNMP
   | SSH
   | TACACS
;

protocol
:
   AGGREGATE
   | BGP
   | DIRECT
   | ISIS
   | OSPF
   | RSVP
   | STATIC
   | TCP
   | UDP
;

variable
:
   text = ~( NEWLINE | WILDCARD_OPEN )
;

wildcard
:
   WILDCARD_OPEN WILDCARD WILDCARD_CLOSE
;
