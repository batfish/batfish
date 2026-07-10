lexer grammar SpecifierLexer;

// Lexer for the Batfish flexible specifier language, replacing the scannerless
// parboiled grammar.
//
// The original was scannerless and context-sensitive; '/' in particular means
// different things by context. Two lexer members carry just enough context to
// disambiguate, matching parboiled's per-context behavior:
//
//   * slashInNames: whether '/' may appear inside a name. True for the
//     "name-heavy" grammars (node, interface, filter, location, ipSpace,
//     routingPolicy, nameSet, enumSet), where "ge-0/0/0" is a single interface
//     name. False for app/oneApp/ipProtocol grammars, where "icmp/8/0" is
//     structural (name, then /type, then /code). The consuming specifier sets
//     this from its Grammar before lexing. None of the name-heavy grammars
//     nest an app/ipProtocol grammar, so a single setting per parse is safe.
//
//   * regexAllowed(): a /-delimited REGEX is only recognized at a term-start
//     position (i.e. the previous real token does not end an operand). This
//     keeps "/8/" in "icmp/8/0" from being lexed as a regex.
//
// Soft keywords (tcp, udp, icmp, internet, enum/type/protocol values) are not
// lexer tokens: they lex as NAME and are matched by text in the parser, exactly
// as the original relied on ordered choice plus value predicates.

@members {
  /**
   * Whether '/' may appear inside a NAME. Set true for name-heavy grammars,
   * false for app/oneApp/ipProtocol grammars. Defaults to true.
   */
  public boolean slashInNames = true;

  private int _lastRealTokenType = -1;

  @Override
  public org.antlr.v4.runtime.Token nextToken() {
    org.antlr.v4.runtime.Token t = super.nextToken();
    if (t.getChannel() == org.antlr.v4.runtime.Token.DEFAULT_CHANNEL) {
      _lastRealTokenType = t.getType();
    }
    return t;
  }

  /**
   * Whether icmp/tcp/udp lex as dedicated keyword tokens. True only for the application specifiers,
   * where they are structural keywords (icmp/8, tcp/80). Elsewhere they are ordinary names.
   */
  public boolean appKeywords = false;

  /** True if a '/'-delimited regex may begin here (previous token is not an operand). */
  private boolean regexAllowed() {
    switch (_lastRealTokenType) {
      case NAME:
      case NUM:
      case IP_ADDRESS:
      case IP_PREFIX:
      case QUOTED_NAME:
      case DEPRECATED_REGEX:
      case REGEX:
      case CLOSE_PAREN:
      case CLOSE_BRACKET:
      case ICMP:
      case TCP:
      case UDP:
        return false;
      default:
        return true;
    }
  }
}

// Structural punctuation.
COMMA : ',';
BACKSLASH : '\\';
AMP : '&';
OPEN_PAREN : '(';
CLOSE_PAREN : ')';
OPEN_BRACKET : '[';
CLOSE_BRACKET : ']';
COLON : ':';
DASH : '-';
BANG : '!';
SLASH : '/';

// @-prefixed function keywords, case-insensitive (original used IgnoreCase).
AT_ROLE : '@' R O L E;
AT_DEVICE_TYPE : '@' D E V I C E T Y P E;
AT_IN : '@' I N;
AT_OUT : '@' O U T;
AT_CONNECTED_TO : '@' C O N N E C T E D T O;
AT_INTERFACE_GROUP : '@' I N T E R F A C E G R O U P;
AT_INTERFACE_TYPE : '@' I N T E R F A C E T Y P E;
AT_VRF : '@' V R F;
AT_ZONE : '@' Z O N E;
AT_ADDRESS_GROUP : '@' A D D R E S S G R O U P;
AT_ENTER : '@' E N T E R;

// Application keywords, only in application specifiers (appKeywords). Declared before NAME so they
// win there; gated off elsewhere so icmp/tcp/udp are ordinary names.
ICMP : {appKeywords}? I C M P;
TCP : {appKeywords}? T C P;
UDP : {appKeywords}? U D P;

// A /-delimited regex; \/ escapes an interior slash. Only recognized at a
// term-start position (see regexAllowed). Declared before SLASH.
REGEX : {regexAllowed()}? '/' ( '\\/' | ~[/] )+ '/';

// A double-quoted escaped name; \" escapes an interior quote.
QUOTED_NAME : '"' ( '\\"' | ~["] )+ '"';

// IP-shaped tokens start with a digit, so they never collide with NAME.
IP_PREFIX : OCTET '.' OCTET '.' OCTET '.' OCTET '/' [0-9]+;
IP_ADDRESS : OCTET '.' OCTET '.' OCTET '.' OCTET;
NUM : [0-9]+;

// A name-shaped token containing at least one '*': a deprecated (bare) regex.
// Mirrors parboiled RegexDeprecated: a leading non-special, non-star, non-digit
// char, then any non-special non-star chars, a '*', then any non-special chars.
// '/' is admitted mid-token only in slash-in-names grammars.
DEPRECATED_REGEX
  : DR_START ( DR_CHAR | {slashInNames}? '/' )* '*' ( DR_CHAR | STAR | {slashInNames}? '/' )*
  ;

// An unquoted name (no '*'; '/' only when slashInNames). First char is not a
// digit, dash, slash, quote, or special char.
NAME : NAME_START ( NAME_CHAR | {slashInNames}? '/' )*;

WS : [ \t]+ -> channel(HIDDEN);

// SPECIAL_CHARS = " \t,\\&()[]@!#$%^;?<>={}". Interior name chars may include
// '.' and ':' (e.g. "node.com-011", "ifa-ce0:1/0.0"). A name may not *start*
// with a digit (IP/number ambiguity), '-' (range), '/' (regex), ':' (wildcard
// mask separator), '.' , '"', or '*'.
fragment STAR : '*';
fragment NAME_CHAR : ~[ \t,\\&()[\]@!#$%^;?<>={}/"*];
fragment NAME_START : ~[ \t,\\&()[\]@!#$%^;?<>={}/"0-9\-*.:];
// A deprecated regex may start with '.' or ':' (e.g. ".*node.*"); it only may
// not start with a digit, '-', '/', '"', '*', or a special char.
fragment DR_CHAR : ~[ \t,\\&()[\]@!#$%^;?<>={}/"*];
fragment DR_START : ~[ \t,\\&()[\]@!#$%^;?<>={}/"0-9\-*];

fragment OCTET : [0-9]+;

fragment A : [aA];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment I : [iI];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment Y : [yY];
fragment Z : [zZ];
