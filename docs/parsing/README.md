# Parsing

This document explains how to write and maintain Batfish parsers for files comprising the
configuration of a network device.

For the purposes of Batfish parsing, the formats of such files are divided into two broad
categories:

- [Domain specific languages](#batfish-dsl-parsing) (DSLs) (e.g. Cisco IOS configuration,
  Linux `/etc/network/interfaces` file)
- Well-known structured [formats](#adding-support-for-structured-file-formats) (e.g. JSON, YAML)

The bulk of this document is concerned with transforming files in DSLs into parse trees, which are
then processed by an [extractor](../extraction/README.md).

## Vendor-Specific Documentation

Different vendors have unique configuration formats and parsing requirements. For vendor-specific guidance, see:

- [Juniper](vendors/juniper.md) - Juniper-specific parsing and extraction details

Most developers wanting to make changes to Batfish parsing will not need to perform all the
activities detailed in this document. Nevertheless, we recommend that you read the entire document
and review the linked resources prior to making any modifications to the parser. This way, you will
have a good foundation and be better prepared to:

- write performant, compliant changes
- understand build errors and test failures you encounter while making changes
- respond and react competently to review comments on your pull requests

## States of config support and accompanying warnings

Based on how far implementation goes, a config line should be in one of the following states:

1. **Not parsed (in the grammar) at all**: unrecognized.
2. **In the grammar, but never needs to be extracted**: silently ignored, but we add [\_null suffix](#ending-rules-in-_null) to indicate it.
3. **In the grammar, not implemented yet, but known to be wrong if used**: In this case, we warn, with things like [todo(...)](../extraction/README.md#Unimplemented-warnings-in-extraction) or [warn(...)](../extraction/README.md#Validating-and-converting-parse-tree-nodes-with-variable-text) at extraction time. See `todo` and `warn` functions in [BatfishListener.java](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/grammar/BatfishListener.java)
4. **In the grammar and extracted, but depending on how it's used may not be supported correctly**: In that case, we warn during conversion (Warnings#redFlag typically) if we can tell that it's not supported. If we can't tell, we warn unconditionally (and try to come up with a better system).
5. **Fully implemented**: No warnings.

It is important to identify which state a line should be in before parsing, so that all unimplemented constructs have appropriate indicators.

For detailed guidance on determining which state to use and how to implement each state, see the [Implementation Guide](implementation_guide.md).

## Batfish DSL parsing

Within the Batfish repository, Batfish DSL parsers are divided into several components. For the
purpose of this section, we will assume you are writing a parser for a format called "Cool NOS"
(Cool Network Operating System) for which there is no existing support in Batfish. We have provided
example [files](example_src) for this parser. In later sections we will explain how to get Batfish
to use this parser on files in the "Cool NOS" format.

- [`CoolNosLexer.g4`](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNosLexer.g4)
  - lexer grammar file
- [`CoolNosParser.g4`](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNosParser.g4)
  - main parser grammar file
- [`CoolNos_common.g4`](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNos_common.g4)
  - subordinate parser grammar file containing rules referenced by the main and other subordinate
    parser grammars
- [`CoolNos_static_routes.g4`](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNos_static_routes.g4)
  - subordinate parser grammar file containing rules for defining static routes
- [`CoolNos_system.g4`](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNos_system.g4)
  - subordinate parser grammar file containing rules for configuring system-level properties
- [`CoolNosBaseLexer.java`](../example_code/new_vendor/src/main/java/org/batfish/grammar/cool_nos/parsing/CoolNosBaseLexer.java)
  - the base class for the generated lexer java class
- [`CoolNosCombinedParser.java`](../example_code/new_vendor/src/main/java/org/batfish/grammar/cool_nos/CoolNosCombinedParser.java)
  - Java class that wraps the functionality of the generated parser and lexer classes
- [Java base lexer](../example_code/new_vendor/src/main/java/org/batfish/grammar/cool_nos/parsing/BUILD.bazel)
  - bazel package defining a library of base parser/lexer files which the generated
    parser/lexer java classes extend
- [`antlr4`](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/BUILD.bazel)
  - bazel package defining a library of generated ANTLR4 java classes
- [Java combined parser, extractor](../example_code/new_vendor/src/main/java/org/batfish/grammar/cool_nos/parsing/BUILD.bazel)
  - bazel package defining a library of the combined parser and extractor classes

Read the rest of this section to learn about:

- the pieces of the Cool NOS parser
- [how to add the Cool NOS parser to Batfish](#adding-a-new-dsl-parser-to-batfish)
- [how to write tests for each of the components](#parser-testing)

### Lexer

The excerpts in this section are from the Cool NOS example lexer file, available
[here](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNosLexer.g4).

The job of the lexer is to convert the text of a DSL into a stream of tokens to be consumed by the
parser. Each token may correspond to fixed text like a DSL keyword or piece of punctuation; or to
variable text like a number, name of a variable, or banner message. The parser does not concern
itself with the exact text of the tokens it consumes, but only with the emitted type of each token.
For tokens whose text may vary, the exact text matched may either be ignored or processed by the
[extractor](../extraction/README.md).

Note that all lexer rule names must begin with a capital letter. We use two conventions for lexer
rules names:

- `TOKEN_NAME`, i.e. all caps, words separated by underscores
  - virtual tokens
  - default mode tokens
- `M_ModeName_TOKEN_NAME`, i.e. name of mode, then underscore, then name of token within the mode.
  - tokens in non-default mode whose name is `M_ModeName`

A Batfish ANLTR4 lexer has the following structure, in this order:

1. [Grammar declaration](#lexer-grammar-declaration)
2. [Options](#lexer-options)
3. [Virtual tokens](#virtual-tokens)
4. [DEFAULT mode tokens](#default-mode-tokens)
5. [Fragments](#fragments)
6. [Non-DEFAULT lexer modes](#non-default-lexer-modes)

#### Lexer grammar declaration

The lexer grammar declaration should look like the following:

```
lexer grammar CoolNosLexer;
```

The name in the declaration line should match the name of the lexer `.g4` file minus `.g4`.

#### Lexer options

The lexer options should looks like the following:

```
options {
  superClass = 'org.batfish.grammar.cool_nos.parsing.CoolNosBaseLexer';
}
```

The `superClass` line says that the generated lexer should extend the `CoolNosBaseLexer` class,
which provides extra functionality on top of the vanilla ANTLR4 `Lexer` class.

#### Virtual tokens

In the Batfish project, we use virtual tokens as the emitted token type for non-DEFAULT mode lexer
rules where there is no existing lexer rule in the DEFAULT mode with the desired name. Virtual
tokens are not strictly necessary, but they can reduce the overhead of parser maintenance when
making changes to the lexer involving non-DEFAULT modes.

Here is the virtual tokens section from the example lexer grammar:

```
tokens {
  RIGHT_BRACKET,
  STRING
}
```

Read on for more information about when to use virtual tokens.

#### DEFAULT mode tokens

ANTLR4 lexers operate in "modes". Each mode is effectively a separate lexer with its own set of
possible tokens that can be emitted for the next piece of text to process. The lexer starts in the
default mode, but may switch temporarily to another mode as a side effect of processing a particular
lexer rule.

While in a given mode, the lexer rule that is chosen next based on the current position in the input
text is decided as follows:

- find all lexer rules in the current mode that match the largest amount text
- of these rules, choose the rule declared at the earliest position in the lexer grammar file

We place the lexer rules for the vast majority of the keywords of a language at the top of the
default mode, i.e. right after the virtual tokens section. In general, the order of keyword lexer
rules should not matter, so we prefer to alphabetize them.

Here is an excerpt of the keywords section DEFAULT mode in the example lexer grammar:

```
// BEGIN keywords
ADD: 'add';
DELETE: 'delete';
DISCARD: 'discard';
ETHERNET: 'ethernet';
GATEWAY: 'gateway';
HOST_NAME: 'host-name';
INTERFACE: 'interface';
LOGIN_BANNER: 'login-banner';
MODIFY: 'modify';
STATIC_ROUTES: 'static-routes';
SYSTEM: 'system';
VLAN: 'vlan';

// END keywords
```

Following the keywords, we place the default MODE lexer rules for punctuation, and then for variable
text.
_Note that lexer rules for fixed punctuation could technically be considered keywords, but we place
them after the keywords to avoid a debate on how they should be alphabetized._

Here is the rest of the DEFAULT mode section:

```
// BEGIN other tokens

DOUBLE_QUOTE: '"' -> pushMode(M_DoubleQuotedString);
LEFT_BRACKET: '[' -> pushMode(M_StringList);

IPV4_ADDRESS: F_Ipv4Address;
IPV4_PREFIX: F_Ipv4Prefix;

UINT8: F_Uint8;
UINT16: F_Uint16;

NEWLINE: F_Newline;
WS: F_Whitespace -> channel(HIDDEN);

// END other tokens
```

There are a few things to note here:

- When the `LEFT_BRACKET` rule matches, it emits a `LEFT_BRACKET` token, but also has the side
  effect of pushing the current (DEFAULT) mode onto the mode stack, and then changing the current
  mode to `M_StringList`. Read on for a discussion of non-DEFAULT modes.
- There is no `RIGHT_BRACKET` token defined in the DEFAULT (or any) mode. This is because we only
  expect to encounter a `]` while the lexer is in the `M_StringList` mode.
- The variable-text-matching token definitions consist of references to fragments rather than having
  an inline definition. This is because we expect to re-use these match expressions in other token
  definitions, and want to avoid duplication for maintainability. Generally, any "other" token with
  a non-trivial definition should refer to a fragment for the definition of what text it matches.
- The `UINT*` rules are ordered such that if they match the same text, the token corresponding to
  the narrowest unsigned integer type is emitted. If e.g. `UINT16` appeared above `UINT8`,
  the `UINT8` rule would never match any text.
- The `WS` (whitespace) rule has a `channel(HIDDEN)` action. This means that when it is chosen to
  match text, the token that is emitted is not seen by the parser. This helps simplify parser rule
  definitions so they do not have to refer to a WS token in between each other token.

  An alternative to `channel(HIDDEN)` is `skip`, which causes no token to be emitted at all. While
  `skip` is slightly more performant, there is an open bug that causes incorrect mode annotations
  for parse error output and parse tree annotations. While this does not affect correctness in the
  absence of a parse failure, it can be confusing to developers. So you should prefer
  `channel(HIDDEN)` at least for token definitions in non-DEFAULT modes. If you prefer
  not to think about it, always use `channel(HIDDEN)` instead of `skip`.

#### Fragments

Fragments are reusable text matching expressions that are referenced by actual lexer rules and other
fragments. They do not define tokens that may be emitted by the lexer. Note that unlike a token, an
unreferenced fragment definition has no impact on the behavior of the lexer.

By convention, we name fragments by `F_` followed by some camel case name.

Here is an excerpt of the fragments section from the example lexer grammar:

```
fragment
F_Digit
:
  [0-9]
;

fragment
F_PositiveDigit
:
  [1-9]
;

fragment
F_UnquotedStringChar
:
  F_Digit
  | [A-Za-z_.]
  | '-'
;

fragment
F_Uint8
:
  // 0-255
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
;

fragment
F_Uint16
:
  // 0-65535
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit?
  | [1-5] F_Digit F_Digit F_Digit F_Digit
  | '6' [0-4] F_Digit F_Digit F_Digit
  | '65' [0-4] F_Digit F_Digit
  | '655' [0-2] F_Digit
  | '6553' [0-5]
;


fragment
F_Ipv4Address
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
;

fragment
F_Ipv4Prefix
:
  F_IpAddress '/' F_IpPrefixLength
;

fragment
F_Ipv4PrefixLength
:
  // 0-32
  F_Digit
  | [12] F_Digit
  | [3] [012]
;

fragment
F_Newline: '\n'+;

fragment
F_NonNewline: ~'\n';

fragment
F_Whitespace: ' '+;
```

#### Non-DEFAULT lexer modes

Recall that each lexer mode is effectively a separate lexer. We use non-DEFAULT lexer modes when we
want to limit the tokens that can be emitted at a particular point in the input text such that there
will be no interference from lexer rules in the DEFAULT or other modes.

A non-DEFAULT lexer mode consists of a mode declaration followed by at least one lexer rule
definition. The definition of a non-DEFAULT modes terminates at the first of EOF or the declaration
of a different non-DEFAULT mode.

In the example lexer grammar, we use a non-DEFAULT lexer mode called `M_StringList` to process
text following a `[`, which in Cool NOS indicates the start of a string list. A string in Cool NOS
may be a sequence of alphanumeric characters, or a sequence of non-newline characters between `"`s.

Let us look at the non-DEFAULT modes in the example lexer grammar:

```
mode M_DoubleQuotedString;

M_DoubleQuotedString_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_DoubleQuotedString_STRING: F_NonNewline+ -> type(STRING);
M_DoubleQuotedString_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;

mode M_StringList;

M_StringList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_StringList_WS: F_Whitespace -> channel(HIDDEN);
M_StringList_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_StringListDoubleQuotedString);
M_StringList_UNQUOTED_STRING: F_UnquotedStringChar+ -> type(STRING);
M_StringLiteral_RIGHT_BRACKET: ']' -> type(RIGHT_BRACKET), popMode;

mode M_StringListDoubleQuotedString;

M_StringListDoubleQuotedString_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_StringListDoubleQuotedString_STRING: F_NonNewline+ -> type(STRING);
M_StringListDoubleQuotedString_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_StringList);
```

There are several things to note here:

- Several rules use the `type` side effect. This causes the lexer to emit the specified token type
  instead of the token identified by the rule name. Since lexer rule names must be globally unique
  (even across modes), this allows us to hide mode implementation details from the parser. The
  `type` action is unnecessary in the presence of a `channel(HIDDEN)` or `skip` action, since such a
  token will never be seen by the parser.
- Mode `M_StringList` may emit between `LEFT_BRACE` and `RIGHT_BRACE` a sequence of ( `STRING`
  or `DOUBLE_QUOTE` `STRING` `DOUBLE_QUOTE`)s. The parser should have a single general rule for
  strings that accepts either of these variants.
- The example uses two different modes with substantially similar implmentations for handling
  double-quoted strings:

  - `M_DoubleQuotedString`
  - `M_StringListDoubleQuotedString`

  Why not just use mode `M_DoubleQuotedString` in both the case where we encounter `"` in the
  DEFAULT mode and the case where we encounter it while in `M_StringList`?

  Take note of the `M_*_NEWLINE` rules. For valid input text, you would not expect to see a newline
  character before the apppropriate closing character. Why bother handling newlines at all? The
  reason is that we want to provide a good experience for input text that has errors. For instance,
  if a user forgets to put a closing double-quote in the input text, and we did not have these
  `M_*_NEWLINE` rules, the lexer would be stuck in the wrong mode while consuming the rest of
  the input text. In the worst case, the entire remainder of the input file could be thrown out.

  Consider:

  - If you use `mode` from `M_StringList` to enter `M_DoubleQuotedString`, then when you
    encounter the closing `"` in the valid case, you will erroneously pop back into the DEFAULT
    mode and lose the rest of the string list.
  - If you use `pushMode` from `M_StringList` to enter `M_DoubleQuotedString`, then if the closing
    `"` is missing, you will pop back into `M_StringList` when you should instead be in the
    DEFAULT mode. This will result in the next line and potentially the remainder of the file
    being thrown out.

  Takeaways:

  - One must be extra careful about entering one mode from two separate modes. It is generally not
    safe when there may be more to lex in an initial non-DEFAULT mode.
  - When handling text that might have errors, it is extremely difficult - sometimes impossible -
    to safely push more than one mode onto the mode stack and still be able to recover without
    polluting the mode stack. So Batfish lexers in general (and line-based grammars in particular)
    only use `pushMode` from the DEFAULT mode.

For more advanced lexing patterns, see:

- [Lexer Mode Patterns](lexer_mode_patterns.md) - Patterns for handling complex name structures and mode transitions

#### Lexer predicates

Sometimes it is necessary to selectively enable a token in a given mode based on some predicate on
the lexer state. Lexer predicates are expensive and should only be used sparingly. The most common
example you will see is for handling line comments that may only be a whole line, i.e. not have any
non-comment non-whitespace text preceding them.

For instance, we often want to allow:

```
! this is a comment taking up the whole line`
```

but forbid:

```
some configuration command ! this coment does not take up the whole line
```

where `!` denotes the beginning of a line comment.

For this, we can define a token that uses a lexer predicate. Since Batfish lexers are converted to
Java, lexer predicates consist of a Java boolean expression.

So for this task, we can define a whole line comment token as follows:

```
COMMENT_LINE
:
  F_Whitespace* '!' F_NonNewline*
  (
    F_Newline
    | EOF
  )
  {lastTokenType() == NEWLINE || lastTokenType() == -1}? -> channel(HIDDEN)
;
```

Note:

- The lexer predicate is `{lastTokenType() == NEWLINE || lastTokenType() == -1}?`
- Lexer predicates can appear anywhere in the token definition, but unless you have a good reason,
  always put them at the end (but before any action). This avoids premature evaluation, which can be
  extremely expensive.
- This predicate makes use of the `lastTokenType()` function, which is defined in the
  `CoolNosBaseLexer` Java class. Note that its return value is `-1` from the beginning of lexing
  until the first non-hidden token is emitted. See:

  ```
  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _lastTokenType = token.getType();
    }
  }

  protected final int lastTokenType() {
    return _lastTokenType;
  }

  private int _lastTokenType = -1;
  ```

### Parser

The excerpts in this section are from the Cool NOS example parser files:

- [CoolNosParser.g4](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNosParser.g4)
- [CoolNos_common.g4](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNos_common.g4)
- [CoolNos_static_routes.g4](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNos_static_routes.g4)
- [CoolNos_system.g4](../example_code/new_vendor/src/main/antlr4/org/batfish/grammar/cool_nos/CoolNos_system.g4)

The job of a parser to is transform a stream of tokens produced by the lexer into a parse tree, to
be acted on by the [extractor](../extraction/README.md).

The parse consists of a main parser grammar file and subordinate parser grammar files. The main and
subordinate parser grammar files may refer to parser rules defined in other parser files via import
statements.

The structure of a parser file is as follows (in order):

1. [Grammar declaration](#parser-grammar-declaration)
2. [Imports](#parser-imports)
3. [Options](#parser-options)
4. [Parser rules](#parser-rules)

#### Parser grammar declaration

The parser grammar declaration for the main parser should look like the following:

```
parser grammar CoolNosParser;
```

The name in the declaration line should match the name of the main parser `.g4` file minus `.g4`.

The same applies to a subordinate parser grammar file, except that it should be named
as `CoolNos_some_construct.g4`, where `some_construct` should be a descriptive name of some
top-level division of the grammar, e.g.:

- `common`, for common rules used by all the other parsers
- `bgp`, for constructs relating to configuring BGP

#### Parser imports

Each parser grammar may refer to rules from other parser grammars. There should be no cycles. The
main parser grammar should import all other parser grammars.

Here is the imports section of the CoolNos example main parser:

```
import
  CoolNos_common,
  CoolNos_static_routes,
  CoolNos_system;
```

The imports section of `CoolNos_common` is absent, since it does not reference rules from any other
parsers.

The `CoolNos_static_routes` grammar only imports from `CoolNos_common`. Like all subordinate
grammars, it
may not import from or reference any rules in the main parser grammar:

```
import CoolNos_common;
```

#### Parser options

Evey parser grammar must specify the lexer grammar as its token vocabulary.
Additionally, it must extend `BatfishParser`:

```
options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = CoolNosLexer;
}
```

Some existing Batfish parser grammars additionally specify a superclass providing additional
functionality. However, over time we have not found this to be beneficial, so the example does not
include this option.

#### Parser rules

The bulk of the grammar for a language is specified in parser rules. All parser rules must begin
with a lower case letter. By convention, all Batfish parser rules are purely lower case.

The main parser must have a start rule whose definition encompasses the entire token stream emitted
by the lexer, plus the special `EOF` token indicating the end of the token stream.

```
cool_nos_configuration
:
  NEWLINE?
  statement+ EOF
;
```

This means that the token stream of valid Cool NOS configuration should contain:

- an optional leading `NEWLINE` token
- a stream of tokens matched by one or more instances of the `statement` rule.
- nothing else

The `statement` rule for the Cool NOS parser is also in the main parser file:

```
statement
:
  s_static_routes
  | s_system
;
```

The `s_static_routes` and `s_system` rules are defined in the `CoolNos_static_routes.g4`
and `CoolNos_system.g4`
subordinate parser grammar files respectively.

Note that we have not referenced any tokens (other than the special `EOF` token) yet.

Since `statement` rule refers to a list of alternatives, the parser must make a choice of which
alternative to use to process subsequent tokens. Unlike the lexer with characters, the parser may
look ahead an arbitrary number of tokens to make this decision, and backtrack if needed.
Backtracking is expensive and can sometimes cause issues with Batfish's parser error recovery
infrastructure. So to minimize the potential for backtracking, we try to write all parser rules in
such a way that a decision among alternatives can be made by looking ahead only a single token (LL(
1)). That is, when you recurse down into all referenced rules until you hit the first token
reference, all such token references should be distinct.

You can see that this is the case in the definitions of the `s_system` rules:

```
// CoolNos_system.g4
import CoolNos_common;

s_system
:
  SYSTEM
  (
    ssy_host_name
    | ssy_login_banner
  )
;
...
ssy_login_banner: LOGIN_BANNER banner = string NEWLINE;
...
```

```
// CoolNos_common.g4
string
:
  STRING
  | DOUBLE_QUOTE STRING DOUBLE_QUOTE
;
```

Recursing down into the alternatives of `statement`, we see that the two alternatives have first
tokens of `STATIC` or `SYSTEM`, which do not conflict. So the choice is LL(1).

Consider the `s_system` rule and its alternatives. If we instead had:

```
// BAD

s_system
:
  ssy_host_name
  | ssy_login_banner
;

ssy_host_name: SYSTEM HOST_NAME ...;
ssy_login_banner: SYSTEM LOGIN_BANNER ...;
```

then to make a decision between `ssy_host_name` and `ssy_login_banner`, we would have to read two
tokens:

1. `SYSTEM`
2. `HOST_NAME` or `LOGIN_BANNER`

This pattern would not be LL(1), so is to be avoided.

#### Parser rule NEWLINE

Also note that `ssy_host_name` and `ssy_login_banner` both end in `NEWLINE`.
A pattern to avoid is putting the `NEWLINE` in a parent node:

```
// BAD

s_system
:
SYSTEM
  (
    ssy_host_name
    | ssy_login_banner
  ) NEWLINE
;
...
ssy_login_banner: LOGIN_BANNER banner = string;
...
```

This makes the parser more fragile and can break recovery in the event that a child rule is only partially recognized.

#### Ending rules in `_null`

If a rule is added with no current plans for further implementation (use in extraction or conversion), the rule should end in `_null`.
This allows it to be captured by the SilentSyntaxListener and prevents unnecessary parse warnings.

##### Implementation Decision Guide for Protocol Commands

When implementing a new command or syntax in Batfish, you must determine whether it should be extracted to the data model or implemented as a null rule. This decision is critical for maintaining accurate network behavior modeling while avoiding unnecessary complexity.

###### What Batfish Models

Batfish extracts and models configuration elements that affect the following:

1. **Control Plane Behavior**: Commands that influence routing decisions, next-hop selection, or path determination
2. **Forwarding Behavior**: Commands that determine how packets are forwarded through the network
3. **Security Posture**: Commands that affect which traffic is permitted or denied
4. **Protocol Establishment**: Commands that determine whether protocol adjacencies or sessions can be established

###### What Batfish Doesn't Model

Batfish typically implements as null rules (with `_null` suffix) configuration elements that:

1. **Operational Commands**: Commands that only affect logging, monitoring, or management access
2. **Performance Tuning**: Commands that only affect convergence speed but not the final converged state
3. **Protocol Optimizations**: Commands that optimize protocol operation but don't change the final routing decisions
4. **Cosmetic Settings**: Commands that affect display or formatting of output

###### Protocol Timer Decision Framework

For protocol timers specifically, use this decision framework:

| Timer Type                       | Implementation                           | Reasoning                                                       |
| -------------------------------- | ---------------------------------------- | --------------------------------------------------------------- |
| **Session Establishment Timers** | Extract to data model                    | These affect whether sessions/adjacencies can form              |
| **Convergence Timers**           | Implement as null rules                  | These only affect how quickly the network converges             |
| **Keep-alive Timers**            | Extract if they affect session stability | Extract if session teardown would occur in realistic timeframes |
| **Throttling Timers**            | Implement as null rules                  | These only affect message frequency, not final state            |

###### Step-by-Step Decision Process

1. **Identify the command's purpose**: Understand what the command does and how it affects network behavior
2. **Determine implementation level**:
   - State 1: Not parsed at all (unrecognized)
   - State 2: In grammar but never extracted (use `_null` suffix)
   - State 3: In grammar but not implemented yet (use `todo()` or `warn()`)
   - State 4: Extracted but conditionally supported (add warnings)
   - State 5: Fully implemented (no warnings)
3. **Apply the timer-specific guidance**:
   - If the timer affects session establishment (like OSPF hello-interval), extract it
   - If the timer only affects protocol performance or convergence time, implement as null rule

###### Examples Table for Routing Protocol Commands

| Command                      | Implementation | Reasoning                                                            |
| ---------------------------- | -------------- | -------------------------------------------------------------------- |
| `ospf hello-interval`        | Extract        | Affects whether OSPF adjacencies can form                            |
| `ospf dead-interval`         | Extract        | Affects whether OSPF adjacencies remain up                           |
| `ospf lsa-refresh-timer`     | Null rule      | Only affects how often LSAs are refreshed, not final SPF calculation |
| `ospf spf-delay`             | Null rule      | Only affects convergence speed, not final SPF calculation            |
| `bgp keepalive-interval`     | Extract        | Affects whether BGP sessions remain established                      |
| `bgp hold-time`              | Extract        | Affects whether BGP sessions remain established                      |
| `bgp advertisement-interval` | Null rule      | Only affects how quickly updates are sent, not final RIB             |
| `isis hello-interval`        | Extract        | Affects whether IS-IS adjacencies can form                           |
| `isis lsp-refresh-interval`  | Null rule      | Only affects how often LSPs are refreshed, not final SPF calculation |

###### Example: Protocol Timer Implementation

For example, when implementing the OSPF hello-interval command, it should be extracted because it affects whether OSPF adjacencies can form:

```
s_ospf_interface
:
  OSPF INTERFACE
  (
    soi_hello_interval
    | soi_dead_interval
    | soi_lsa_refresh_interval_null
  )
;

soi_hello_interval
:
  HELLO_INTERVAL seconds = uint16 NEWLINE
;

soi_lsa_refresh_interval_null
:
  LSA_REFRESH_INTERVAL uint16 NEWLINE
;
```

###### Testing Implementation Decisions

Even for `_null` rules, tests should verify that the parser correctly handles the syntax:

```java
@Test
public void testOspfTimerParsing() {
  // Should parse without warnings
  parseConfig("ospf-timer-test");

  // For extracted timers, verify the values are correctly extracted
  Configuration c = parseConfig("ospf-hello-interval-test");
  assertThat(c.getDefaultVrf().getOspfProcess().getAreas().get(0L).getInterfaces().get("eth0")
      .getHelloInterval(), equalTo(10));
}
```

See the [Implementation Guide](implementation_guide.md) for more detailed guidance on determining the appropriate implementation level for different types of commands.

### Grammar packages

**This section is still in progress. Check back later for more complete instructions.**

For now, if you need to write a new grammar, follow the pattern of the Cisco NX-OS grammar package.
That is, make a copy of and appropriate alterations to each of:

- [`antlr4/org/batfish/grammar/cisco_nxos/BUILD.bazel`](../../projects/batfish/src/main/antlr4/org/batfish/grammar/cisco_nxos/BUILD.bazel)
- [`java/org/batfish/grammar/cisco_nxos/BUILD.bazel`](../../projects/batfish/src/main/java/org/batfish/grammar/cisco_nxos/BUILD.bazel)
- [`java/org/batfish/grammar/cisco_nxos/parsing/BUILD.bazel`](../../projects/batfish/src/main/java/org/batfish/grammar/cisco_nxos/parsing/BUILD.bazel)

in the appropriate directories created for your new grammar.

Then, add appropriate references to the `//projects/batfish` target, also copying the pattern for
Cisco NX-OS.

For tests, copy the pattern used by:

- [`//projects/batfish/src/test/java/org/batfish/grammar/cisco_nxos:tests`](../../projects/batfish/src/test/java/org/batfish/grammar/cisco_nxos/BUILD.bazel)

### Combined parser

In Batfish, the functionality of the lexer and parser is wrapped in a "Combined" parser class that
provides an interface to the rest of Batfish to use this functionality.

The combined parser must implement a `parse` function that produces a parse tree rooted at the main
parser grammar's start rule.

It also dictates what recovery to mechanism Batfish should use in the face of errors parsing the
input text.

See [CoolNosCominbedParser.java](../example_code/new_vendor/src/main/java/org/batfish/grammar/cool_nos/CoolNosCombinedParser.java)

### Adding a new DSL parser to Batfish

In order for Batfish to use a new DSL parser on uploaded files, you must first:

- add a `ConfigurationFormat` enum for the new DSL
- provide a method for Batfish to identify files in the new format
- add cases to all relevant `switch`es on `ConfigurationFormat`

#### Adding a new ConfigurationFormat

In `ConfigurationFormat.java`, add a new enum for the new DSL.

For the Cool NOS example, add:

```
COOL_NOS("cool_nos"),
```

#### Updating VendorConfigurationFormatDetector

In order to decide which parser to use on a file, Batfish employes some heuristics implemented
in `VendorConfigurationFormat.Java`.

First, update the `checkRancid` function in that file if there is an applicable rancid content
header for your new format.

Next, add a new `checkXXX` function, and reference it in the appropriate place in
the `identifyConfigurationFormat` function.

For the Cool NOS example, we will assume that every Cool NOS file contains the text:

```
! THIS IS A COOL NOS FILE
```

So add:

```
private static final Pattern COOL_NOS_PATTERN = Pattern.compile("(?m)^! THIS IS A COOL NOS FILE$");

private @Nullable ConfigurationFormat checkCoolNos() {
    if (fileTextMatches(COOL_NOS_PATTERN)) {
      return ConfigurationFormat.COOL_NOS;
    }
}
```

#### Adding cases for your new format

In the `ParseVendorConfigurationJob.java`, add a case for your new format.

For the example, you can:

- copy the block for `case CISCO_NX` block, changing `CISCO_NX` to `COOL_NOS`
- Change `CiscoNxosCombinedParser` to `CoolNosCombinedParser`
- Change `NxosControlPlaneExtractor` to `CoolNosControlPlaneExtractor`
  (see [extraction](../extraction/README.md)).

### Parser testing

This section is still in progress. Check back later!

## Adding support for structured file formats

This section is still in progress. Check back later!
