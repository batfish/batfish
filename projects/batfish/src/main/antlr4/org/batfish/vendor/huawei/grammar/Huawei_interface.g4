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

// Null interface configuration (parse but ignore)
// Only matches commands that DON'T start with INTERFACE
// This prevents consuming subsequent interface statements
if_null
:
   NO?
   (
      // First token must not be INTERFACE
      // Match one token that's definitely not a stanza-starting keyword
      (DESCRIPTION | NAME | SHUTDOWN | DOT1Q | TERMINATION | VID | PORT | COMMAND | VARIABLE)
      // Then optionally match more tokens (including INTERFACE for descriptions)
      null_token*
   )
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
