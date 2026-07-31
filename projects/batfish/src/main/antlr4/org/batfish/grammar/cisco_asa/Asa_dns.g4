parser grammar Asa_dns;

import Asa_common;

options {
   tokenVocab = AsaLexer;
}

s_dns
:
   DNS
   (
      dns_domain_lookup
      | dns_name_server
      | dns_server_group
   )
;

dns_domain_lookup
:
   DOMAIN_LOOKUP iname = variable NEWLINE
;

dns_name_server
:
   NAME_SERVER (servers += ip_hostname)+ NEWLINE
;

dns_server_group
:
   SERVER_GROUP name = variable NEWLINE
   (
      dnssg_name_server
      | dnssg_domain_name
      | dnssg_timeout
      | dnssg_retries
      | dnssg_poll_timer
      | dnssg_expire_entry_timer
   )*
;

dnssg_name_server
:
   NAME_SERVER (servers += ip_hostname)+ (iface = variable)? NEWLINE
;

dnssg_domain_name
:
   DOMAIN_NAME name = variable_hostname NEWLINE
;

dnssg_timeout
:
   TIMEOUT secs = uint8 NEWLINE
;

dnssg_retries
:
   RETRIES count = uint8 NEWLINE
;

dnssg_poll_timer
:
   POLL_TIMER MINUTES mins = uint16 NEWLINE
;

dnssg_expire_entry_timer
:
   EXPIRE_ENTRY_TIMER MINUTES mins = uint16 NEWLINE
;

s_dns_group
:
   DNS_GROUP name = variable NEWLINE
;

s_dns_group_map
:
   DNS_GROUP_MAP NEWLINE
   dnsgm_dns_to_domain*
;

dnsgm_dns_to_domain
:
   DNS_TO_DOMAIN group = variable domain = variable_hostname NEWLINE
;
