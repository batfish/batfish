parser grammar Huawei_acl;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// ACL configuration

// ACL stanza - matches "acl <number> [basic|advanced]" or "acl name> [basic|advanced]" or "acl number <number> [basic|advanced]"
s_acl
:
   ACL
   (
      acl_num = uint16 (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
      |
      NUMBER acl_num = uint16 (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
      |
      acl_name = variable (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
   )
   (
      acl_substanza
   )*
;

// ACL sub-stanza
acl_substanza
:
   acl_rule
   |
   acl_null
;

// ACL rule - permit/deny statements
acl_rule
:
   RULE uint16
   (
      action = PERMIT
      |
      action = DENY
   )
   (
      // Protocol specification
      TCP
      |
      UDP
      |
      ICMP
      |
      IP
      |
      variable
   )?
   (
      // Source address
      SOURCE src_addr = IPV4_ADDRESS_PATTERN (src_wildcard = IPV4_ADDRESS_PATTERN)?
      |
      SOURCE src_addr = IPV4_ADDRESS_PATTERN src_prefix_len = FORWARD_SLASH uint8
      |
      SOURCE src_any = ANY
   )?
   (
      // Destination address
      DESTINATION dest_addr = IPV4_ADDRESS_PATTERN (dest_wildcard = IPV4_ADDRESS_PATTERN)?
      |
      DESTINATION dest_addr = IPV4_ADDRESS_PATTERN dest_prefix_len = FORWARD_SLASH uint8
      |
      DESTINATION dest_any = ANY
   )?
   (
      // Source port (for TCP/UDP)
      SOURCE_PORT
      (
         eq = EQ src_port = uint16
         |
         gt = GT src_port = uint16
         |
         lt = LT src_port = uint16
         |
         range = RANGE src_port_start = uint16 src_port_end = uint16
      )
   )?
   (
      // Destination port (for TCP/UDP)
      DESTINATION_PORT
      (
         eq2 = EQ dest_port = uint16
         |
         gt2 = GT dest_port = uint16
         |
         lt2 = LT dest_port = uint16
         |
         range2 = RANGE dest_port_start = uint16 dest_port_end = uint16
      )
   )?
   (
      // Other options
      log = LOG
      |
      frag = FRAGMENT
   )?
;

// Null ACL configuration (parse but ignore)
acl_null
:
   NO?
   (
      null_rest_of_line
   )
;
