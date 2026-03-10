parser grammar Arista_igmp;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

default_ip_igmp
:
  IGMP default_igmp_null
;

default_igmp_null
:
  (
    SNOOPING
  ) null_rest_of_line
;

s_ip_igmp
:
  IGMP igmp_null
;

igmp_null
:
  (
    SNOOPING
  ) null_rest_of_line
;

no_ip_igmp
:
  IGMP no_igmp_null
;

no_igmp_null
:
  (
    SNOOPING
  ) null_rest_of_line
;
