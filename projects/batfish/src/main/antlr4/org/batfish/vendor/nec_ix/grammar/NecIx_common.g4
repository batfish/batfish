parser grammar NecIx_common;

options {
  tokenVocab = NecIxLexer;
}

// Any single token: names of ACLs, prefix-lists, route-maps, VRFs and interfaces may coincide
// with keywords (e.g. a prefix-list named `default`).
variable
:
  ~NEWLINE
;

uint
:
  UINT
;

// Free text up to the end of the line, e.g. a description.
text
:
  ~NEWLINE+
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

// A line inside a mode whose contents are ignored.
null_block_line
:
  INDENT null_rest_of_line
;
