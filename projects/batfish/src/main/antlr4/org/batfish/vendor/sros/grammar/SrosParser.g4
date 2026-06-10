parser grammar SrosParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = SrosLexer;
}

// An SR-OS MD-CLI configuration. This single grammar accepts all three input forms the
// device and operators produce, because they are structurally the same token stream of
// statements:
//   - the brace/hierarchical form emitted by `admin show configuration`
//     (`configure { ... }`),
//   - the absolute-path flat form (the Junos-`set` analog), where each line is a
//     `/configure ...` statement, and
//   - a mix of the two in one file (a brace production config with appended flat
//     `/configure ...` edits).
// A flat `/configure ...` line is simply a leaf `statement` whose first word begins with
// `/`; the extractor normalizes every leaf to one canonical absolute path, so all three
// forms yield the same model. See docs/parsing/vendors/sros.md.
sros_configuration
:
  NEWLINE* statement* EOF
;

statement
:
  words += word+
  (
    block
    | bracketed_clause statement_end
    | statement_end
  )
;

// A brace-delimited block: `<words> { <newline> <statements> }`. The opening brace sits on
// the same line as the leading words; the closing brace is on its own line. A list entry with
// no body is rendered by the device on a single line as `<words> { }` (open brace, optional
// whitespace, close brace, no intervening newline) — e.g. `to-prefix 10.0.0.0/8 { }` — so an
// empty same-line block is also accepted.
block
:
  OPEN_BRACE NEWLINE+ statement* CLOSE_BRACE statement_end
  | OPEN_BRACE CLOSE_BRACE statement_end
;

// A leaf-list value, e.g. `prefix-list ["a" "b"]` or `member ["administrative"]`.
bracketed_clause
:
  OPEN_BRACKET word* RIGHT_BRACKET
;

// One or more newlines terminate a statement; runs of blank lines collapse in the lexer,
// but separate runs (e.g. around hidden comment lines) can produce adjacent NEWLINE tokens.
statement_end
:
  NEWLINE+
;

word
:
  WORD
  | string
;

string
:
  STRING
  | DOUBLE_QUOTE STRING? DOUBLE_QUOTE
;
