parser grammar Huawei_interface;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Interface configuration

// Main interface stanza
s_interface
:
   INTERFACE iname = interface_name
   (
      if_substanza
   )*
;

// Interface sub-stanzas
if_substanza
:
   if_description
   | if_ip_address
   | if_shutdown
   | if_dot1q_termination
   | if_null
;

// Interface description
if_description
:
   description_line
;

// Interface IP address
if_ip_address
:
   IP ADDRESS
   (
      // ip address X.X.X.X Y.Y.Y.Y
      addr = IPV4_ADDRESS_PATTERN mask = IPV4_ADDRESS_PATTERN
   )
;

// Interface shutdown
if_shutdown
:
   (
      SHUTDOWN
      | UNDO SHUTDOWN
   )
;

// Subinterface dot1q termination (e.g., dot1q termination vid 100)
if_dot1q_termination
:
   DOT1Q TERMINATION VID vid = uint16
   (
      // Optional: dot1q termination vid <low> <high>
      low_vid = uint16
   )?
;

// Null interface configuration (parse but ignore)
if_null
:
   NO?
   (
      // Add interface-specific commands to ignore here
      null_rest_of_line
   )
;
