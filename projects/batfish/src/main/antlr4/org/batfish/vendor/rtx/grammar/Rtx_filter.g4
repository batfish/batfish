parser grammar Rtx_filter;

// `ip filter <num> <action> <src> [<dst> [<protocol> [<src-ports> [<dst-ports>]]]]`
//
// The arguments are returned by the lexer as opaque words (see M_FilterArgs in RtxLexer.g4) and
// interpreted in RtxConfigurationBuilder. Omitted trailing arguments default to `*`.
ip_filter
:
  FILTER
  (
    ipf_definition
    | ipf_null
  )
;

ipf_definition
:
  num = UINT action = filter_action args += FILTER_WORD* NEWLINE
;

// `ip filter dynamic ...` and other sub-commands that Batfish does not model. Dynamic filters are
// stateful and have no meaning in a static analysis.
ipf_null
:
  IGNORED_TEXT* NEWLINE
;

filter_action
:
  FILTER_PASS
  | FILTER_REJECT
  | FILTER_RESTRICT
;
