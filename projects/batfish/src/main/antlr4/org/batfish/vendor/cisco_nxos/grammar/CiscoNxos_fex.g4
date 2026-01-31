parser grammar CiscoNxos_fex;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_fex
:
  FEX fex_id NEWLINE
  (
    fex_description
    | fex_no
    | fex_pinning
  )*
;

fex_description
:
  DESCRIPTION desc = REMARK_TEXT NEWLINE
;

fex_no
:
  NO fex_no_description
;

fex_no_description
:
  DESCRIPTION NEWLINE
;

fex_pinning
:
  PINNING MAX_LINKS fex_pinning_max_links_values NEWLINE
;

fex_pinning_max_links_values
:
// 1-8: https://www.cisco.com/c/m/en_us/techdoc/dc/reference/cli/n5k/commands/pinning-max-links.html
 uint8
;