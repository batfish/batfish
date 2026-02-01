parser grammar Huawei_ignored;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Commands to parse but ignore (Phase 1)

// Ignored stanza - consumes unknown or unsupported commands
s_ignored
:
   // Match lines with content that aren't recognized commands
   // Must have at least one non-newline token
   VARIABLE+
;
