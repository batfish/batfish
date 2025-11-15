parser grammar Cisco_device_tracking;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_device_tracking
:
  DEVICE_TRACKING
  (
    dtr_policy
  )
;

dtr_policy
:
  POLICY name = device_tracking_policy_name NEWLINE
  (
    dtrp_limit
    | dtrp_no
    | dtrp_security_level
    | dtrp_tracking
  )*
;

dtrp_limit
:
  LIMIT
  (
    dtrpl_address_count
  )
;

dtrpl_address_count
:
  ADDRESS_COUNT dtrplac_ipv6_per_mac
;

dtrplac_ipv6_per_mac
:
  IPV6_PER_MAC limit = dec NEWLINE
;

dtrp_no
:
  NO
  (
    dtrpn_protocol
  )
;

dtrpn_protocol
:
  PROTOCOL
  (
    UDP
  ) NEWLINE
;

dtrp_security_level
:
  SECURITY_LEVEL
  (
    GLBP
    | INSPECT
  ) NEWLINE
;

dtrp_tracking
:
  TRACKING
  (
    DISABLE
    | ENABLE
  ) NEWLINE
;
