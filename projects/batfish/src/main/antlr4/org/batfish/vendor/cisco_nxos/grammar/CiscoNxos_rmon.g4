parser grammar CiscoNxos_rmon;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_rmon: RMON REMARK_TEXT NEWLINE;