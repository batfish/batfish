parser grammar CiscoParser;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

ip_nat_destination
:
   IP NAT INSIDE DESTINATION LIST acl = variable POOL pool = variable NEWLINE
;

ip_nat_null
:
   IP NAT (
      LOG
      | TRANSLATION
   ) null_rest_of_line
;

ip_nat_pool
:
   IP NAT POOL name = variable first = IP_ADDRESS last = IP_ADDRESS
   (
      NETMASK mask = IP_ADDRESS
      | PREFIX_LENGTH prefix_length = DEC
   )? NEWLINE
;

ip_nat_pool_range
:
   IP NAT POOL name = variable PREFIX_LENGTH prefix_length = DEC NEWLINE
   (
      RANGE first = IP_ADDRESS last = IP_ADDRESS NEWLINE
   )+
;

ip_nat_source
:
   IP NAT (INSIDE | OUTSIDE) SOURCE
   (
      (
         LIST acl = variable POOL pool = variable
      )
      |
      (
         STATIC local = IP_ADDRESS global = IP_ADDRESS
      )
      |
      (
         STATIC NETWORK local = IP_ADDRESS global = IP_ADDRESS
         (
            mask = IP_ADDRESS
            | FORWARD_SLASH prefix = DEC
         )
      )
   )
   (
      ADD_ROUTE
      | NO_ALIAS
   )* NEWLINE
;