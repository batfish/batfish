parser grammar JuniperGrammar_firewall;

import JuniperGrammarCommonParser;

options {
   tokenVocab = JuniperGrammarLexer;
}

accept_then_t_ff_stanza
:
   ACCEPT SEMICOLON
;

count_then_t_ff_stanza
:
   COUNT ~SEMICOLON SEMICOLON
;

destination_address_from_t_ff_stanza
:
   DESTINATION_ADDRESS OPEN_BRACE
   (
      IP_ADDRESS_WITH_MASK SEMICOLON
   )+ CLOSE_BRACE
;

destination_port_from_t_ff_stanza
:
   DESTINATION_PORT integer_list SEMICOLON
;

discard_then_t_ff_stanza
:
   DISCARD SEMICOLON
;

filter_f_stanza
:
   FILTER name = VARIABLE OPEN_BRACE term_f_f_stanza+ CLOSE_BRACE
;

filter_f_stanza_list
:
   filter_f_stanza+
;

firewall_stanza
:
   FIREWALL OPEN_BRACE
   (
      (
         FAMILY INET OPEN_BRACE l = filter_f_stanza_list CLOSE_BRACE
         (
            FAMILY INET6 OPEN_BRACE substanza+ CLOSE_BRACE
         )?
      )
      | nl = filter_f_stanza_list
   ) CLOSE_BRACE
;

from_t_ff_stanza
:
   destination_address_from_t_ff_stanza
   | destination_port_from_t_ff_stanza
   | icmp_type_from_t_ff_stanza
   | protocol_from_t_ff_stanza
   | source_address_from_t_ff_stanza
   | source_port_from_t_ff_stanza
;

from_t_ff_stanza_list
:
   from_t_ff_stanza+
;

icmp_type_from_t_ff_stanza
:
   ICMP_TYPE variable_list CLOSE_BRACKET SEMICOLON
;

log_then_t_ff_stanza
:
   LOG SEMICOLON
;

next_term_then_t_ff_stanza
:
   NEXT TERM SEMICOLON
;

null_then_t_ff_stanza
:
   count_then_t_ff_stanza
   | log_then_t_ff_stanza
   | sample_then_t_ff_stanza
;

port
:
   DEC
   | BGP
   | DOMAIN
   | FTP
   | NTP
   | SNMP
   | SSH
   | TACACS
   | TELNET
   | TFTP
;

protocol
:
   DEC
   | ICMP
   | IGMP
   | IP
   | OSPF
   | PIM
   | TCP
   | UDP
;

protocol_from_t_ff_stanza
:
   PROTOCOL protocol_list SEMICOLON
;

sample_then_t_ff_stanza
:
   SAMPLE SEMICOLON
;

source_address_from_t_ff_stanza
:
   SOURCE_ADDRESS OPEN_BRACE
   (
      IP_ADDRESS_WITH_MASK EXCEPT? SEMICOLON
   )+ CLOSE_BRACE
;

source_port_from_t_ff_stanza
:
   SOURCE_PORT integer_list SEMICOLON
;

term_f_f_stanza
:
   TERM name = VARIABLE OPEN_BRACE
   (
      FROM
      (
         (
            OPEN_BRACE fl = from_t_ff_stanza_list CLOSE_BRACE
         )
         | f = from_t_ff_stanza
      )
   )?
   (
      THEN
      (
         (
            OPEN_BRACE thl = then_t_ff_stanza_list CLOSE_BRACE
         )
         | th = then_t_ff_stanza
      )
   ) CLOSE_BRACE
;

then_t_ff_stanza
:
   accept_then_t_ff_stanza
   | discard_then_t_ff_stanza
   | next_term_then_t_ff_stanza
   | null_then_t_ff_stanza
;

then_t_ff_stanza_list
:
   then_t_ff_stanza+
;
