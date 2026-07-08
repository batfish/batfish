package org.batfish.vendor.check_point_management.parsing.serviceother;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Parser for the {@code match} field of a CheckPoint service of type {@code service-other}.
 *
 * <p>This is a hand-written recursive-descent parser implementing the same PEG as an earlier
 * parboiled-based implementation. Ordered choice is realized by saving and restoring the input
 * position ({@link #_pos}) on a failed alternative; each {@code parseX} method returns {@code null}
 * (and restores the position) when it does not match. The produced AST is unchanged.
 */
public final class ServiceOtherMatchExpr {

  private final String _input;
  private int _pos;

  private ServiceOtherMatchExpr(String input) {
    _input = input;
    _pos = 0;
  }

  // TopLevel := WhiteSpace (Conjunction | <empty>) WhiteSpace EOI
  private @Nullable AstNode topLevel() {
    whiteSpace();
    AstNode result = conjunction();
    if (result == null) {
      result = EmptyAstNode.instance();
    }
    whiteSpace();
    if (_pos != _input.length()) {
      return null;
    }
    return result;
  }

  // Conjunction := Disjunction ("," Disjunction)*
  @Nullable
  AstNode conjunction() {
    AstNode first = disjunction();
    if (first == null) {
      return null;
    }
    BooleanExprAstNode acc = (BooleanExprAstNode) first;
    while (true) {
      int save = _pos;
      if (!literal(",")) {
        _pos = save;
        break;
      }
      AstNode next = disjunction();
      if (next == null) {
        _pos = save;
        break;
      }
      acc = acc.and((BooleanExprAstNode) next);
    }
    return acc;
  }

  // Disjunction := Disjunct ("or" Disjunct)*
  private @Nullable AstNode disjunction() {
    AstNode first = disjunct();
    if (first == null) {
      return null;
    }
    BooleanExprAstNode acc = (BooleanExprAstNode) first;
    while (true) {
      int save = _pos;
      if (!literal("or")) {
        _pos = save;
        break;
      }
      AstNode next = disjunct();
      if (next == null) {
        _pos = save;
        break;
      }
      acc = acc.or((BooleanExprAstNode) next);
    }
    return acc;
  }

  // Disjunct := ParentheticalBooleanExpr | AtomicBooleanExpr
  private @Nullable AstNode disjunct() {
    AstNode paren = parentheticalBooleanExpr();
    if (paren != null) {
      return paren;
    }
    return atomicBooleanExpr();
  }

  // ParentheticalBooleanExpr := "(" Conjunction ")"
  private @Nullable AstNode parentheticalBooleanExpr() {
    int start = _pos;
    if (!literal("(")) {
      return null;
    }
    AstNode inner = conjunction();
    if (inner == null || !literal(")")) {
      _pos = start;
      return null;
    }
    return inner;
  }

  // AtomicBooleanExpr := DirectionExpr | DportExpr | UhDportExpr | Tcp | Udp | UnhandledExpr
  private @Nullable AstNode atomicBooleanExpr() {
    AstNode result = directionExpr();
    if (result != null) {
      return result;
    }
    result = dportExpr();
    if (result != null) {
      return result;
    }
    result = uhDportExpr();
    if (result != null) {
      return result;
    }
    result = tcp();
    if (result != null) {
      return result;
    }
    result = udp();
    if (result != null) {
      return result;
    }
    return unhandledExpr();
  }

  // DportExpr := "dport" Comparator Uint16Expr
  private @Nullable AstNode dportExpr() {
    int start = _pos;
    if (!literal("dport")) {
      return null;
    }
    AstNode comparator = comparator();
    if (comparator == null) {
      _pos = start;
      return null;
    }
    AstNode value = uint16Expr();
    if (value == null) {
      _pos = start;
      return null;
    }
    return new DportAstNode(
        matchSince(start), (ComparatorAstNode) comparator, (Uint16AstNode) value);
  }

  // UhDportExpr := "uh_dport" Comparator Uint16Expr
  private @Nullable AstNode uhDportExpr() {
    int start = _pos;
    if (!literal("uh_dport")) {
      return null;
    }
    AstNode comparator = comparator();
    if (comparator == null) {
      _pos = start;
      return null;
    }
    AstNode value = uint16Expr();
    if (value == null) {
      _pos = start;
      return null;
    }
    return new UhDportAstNode(
        matchSince(start), (ComparatorAstNode) comparator, (Uint16AstNode) value);
  }

  // Uint16Expr := "LOW_UDP_PORT" | Uint16
  @Nullable
  AstNode uint16Expr() {
    int start = _pos;
    if (literal("LOW_UDP_PORT")) {
      return Uint16AstNode.of(LOW_UDP_PORT);
    }
    _pos = start;
    return uint16();
  }

  // Uint16 := Number WhiteSpace
  private @Nullable AstNode uint16() {
    String number = number();
    if (number == null) {
      return null;
    }
    Uint16AstNode result = Uint16AstNode.of(number);
    whiteSpace();
    return result;
  }

  // Comparator := "=" | "<=" | "<" | ">=" | ">"  (order matters: longest match first)
  @Nullable
  AstNode comparator() {
    if (literal("=")) {
      return EqualsAstNode.instance();
    }
    if (literal("<=")) {
      return LessThanOrEqualsAstNode.instance();
    }
    if (literal("<")) {
      return LessThanAstNode.instance();
    }
    if (literal(">=")) {
      return GreaterThanOrEqualsAstNode.instance();
    }
    if (literal(">")) {
      return GreaterThanAstNode.instance();
    }
    return null;
  }

  private @Nullable AstNode tcp() {
    if (literal("tcp")) {
      return TcpAstNode.instance();
    }
    return null;
  }

  private @Nullable AstNode udp() {
    if (literal("udp")) {
      return UdpAstNode.instance();
    }
    return null;
  }

  // DirectionExpr := Incoming | Outgoing
  private @Nullable AstNode directionExpr() {
    AstNode incoming = incoming();
    if (incoming != null) {
      return incoming;
    }
    return outgoing();
  }

  // Incoming := "direction" "=" "0"
  private @Nullable AstNode incoming() {
    int start = _pos;
    if (literal("direction") && literal("=") && literal("0")) {
      return new IncomingAstNode(matchSince(start));
    }
    _pos = start;
    return null;
  }

  // Outgoing := "direction" "=" "1"
  private @Nullable AstNode outgoing() {
    int start = _pos;
    if (literal("direction") && literal("=") && literal("1")) {
      return new OutgoingAstNode(matchSince(start));
    }
    _pos = start;
    return null;
  }

  // UnhandledExpr := (CallExpr | InExpr | UnhandledComparisonExpr | UnhandledWord)
  private @Nullable AstNode unhandledExpr() {
    int start = _pos;
    if (callExpr() || inExpr() || unhandledComparisonExpr() || unhandledWord()) {
      return UnhandledAstNode.of(matchSince(start));
    }
    _pos = start;
    return null;
  }

  // InExpr := "<" UnhandledWord ("," UnhandledWord)* ">" "in" UnhandledWord
  private boolean inExpr() {
    int start = _pos;
    if (!literal("<") || !unhandledWord()) {
      _pos = start;
      return false;
    }
    while (true) {
      int save = _pos;
      if (!literal(",") || !unhandledWord()) {
        _pos = save;
        break;
      }
    }
    if (!literal(">") || !literal("in") || !unhandledWord()) {
      _pos = start;
      return false;
    }
    return true;
  }

  // UnhandledComparisonExpr := UnhandledWord Comparator UnhandledWord
  private boolean unhandledComparisonExpr() {
    int start = _pos;
    if (unhandledWord() && comparator() != null && unhandledWord()) {
      return true;
    }
    _pos = start;
    return false;
  }

  // CallExpr := UnhandledWord "(" (Conjunction ("," Conjunction)*)? ")"
  private boolean callExpr() {
    int start = _pos;
    if (!unhandledWord() || !literal("(")) {
      _pos = start;
      return false;
    }
    int beforeArgs = _pos;
    if (conjunction() != null) {
      while (true) {
        int save = _pos;
        if (!literal(",") || conjunction() == null) {
          _pos = save;
          break;
        }
      }
    } else {
      _pos = beforeArgs;
    }
    if (!literal(")")) {
      _pos = start;
      return false;
    }
    return true;
  }

  // UnhandledWord := (not one of " (),=<>")+ WhiteSpace
  private boolean unhandledWord() {
    int start = _pos;
    while (_pos < _input.length() && " (),=<>".indexOf(_input.charAt(_pos)) < 0) {
      _pos++;
    }
    if (_pos == start) {
      return false;
    }
    whiteSpace();
    return true;
  }

  private void whiteSpace() {
    while (_pos < _input.length() && _input.charAt(_pos) == ' ') {
      _pos++;
    }
  }

  private @Nullable String number() {
    int start = _pos;
    while (_pos < _input.length() && Character.isDigit(_input.charAt(_pos))) {
      _pos++;
    }
    if (_pos == start) {
      return null;
    }
    return _input.substring(start, _pos);
  }

  /**
   * Matches a string literal followed by optional whitespace, mirroring the former parboiled {@code
   * fromStringLiteral}. Returns true and advances on success; returns false and does not advance on
   * failure.
   */
  private boolean literal(String string) {
    if (!_input.startsWith(string, _pos)) {
      return false;
    }
    _pos += string.length();
    whiteSpace();
    return true;
  }

  /** Returns the input matched from {@code start} to the current position, trimmed. */
  private String matchSince(int start) {
    return _input.substring(start, _pos).trim();
  }

  public static @Nonnull BooleanExprAstNode parse(String input) {
    return (BooleanExprAstNode) parse(input, ServiceOtherMatchExpr::topLevel);
  }

  @VisibleForTesting
  static @Nonnull AstNode parse(
      String input, Function<ServiceOtherMatchExpr, AstNode> inputRuleGetter) {
    // Mirrors parboiled's BasicParseRunner: success is "the root rule matched at the start"; it is
    // the rule's own responsibility to require EOI (TopLevel does). A null return means no match.
    ServiceOtherMatchExpr parser = new ServiceOtherMatchExpr(input);
    AstNode result = inputRuleGetter.apply(parser);
    return result == null ? ErrorAstNode.instance() : result;
  }

  @VisibleForTesting static final int LOW_UDP_PORT = 1024;
}
