parser grammar Huawei_system;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// System-level configuration

// Sysname command (sets hostname)
s_sysname
:
   SYSNAME hostname = variable
;

// System configuration
s_system
:
   SYSTEM
   (
      system_null
   )*
;

system_null
:
   NO?
   (
      // Add system-specific commands here as needed
      null_rest_of_line
   )
;
