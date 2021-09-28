package org.batfish.vendor.check_point_management.parsing.parboiled;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.support.ParsingResult;

/** Parser for the {@code match} field of a CheckPoint service of type {@code service-other}. */
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class ServiceOtherMatchExpr extends BaseParser<AstNode> {

  Rule TopLevel() {
    return Sequence(
        WhiteSpace(), FirstOf(Conjunction(), push(EmptyAstNode.instance())), WhiteSpace(), EOI);
  }

  Rule Conjunction() {
    return Sequence(
        Disjunction(),
        ZeroOrMore(
            ",",
            Disjunction(),
            push(((BooleanExprAstNode) pop(1)).and((BooleanExprAstNode) pop()))));
  }

  Rule Disjunction() {
    return Sequence(
        Disjunct(),
        ZeroOrMore(
            "or", Disjunct(), push(((BooleanExprAstNode) pop(1)).or((BooleanExprAstNode) pop()))));
  }

  Rule Disjunct() {
    return FirstOf(ParentheticalBooleanExpr(), AtomicBooleanExpr());
  }

  Rule ParentheticalBooleanExpr() {
    return Sequence("(", Conjunction(), ")");
  }

  Rule AtomicBooleanExpr() {
    return FirstOf(DirectionExpr(), DportExpr(), UhDportExpr(), Tcp(), Udp(), UnhandledExpr());
  }

  Rule DportExpr() {
    return Sequence(
        "dport",
        Comparator(),
        Uint16Expr(),
        push(new DportAstNode((ComparatorAstNode) pop(1), (Uint16AstNode) pop())));
  }

  Rule UhDportExpr() {
    return Sequence(
        "uh_dport",
        Comparator(),
        Uint16Expr(),
        push(new UhDportAstNode((ComparatorAstNode) pop(1), (Uint16AstNode) pop())));
  }

  Rule Uint16Expr() {
    return FirstOf(LowUdpPort(), Uint16());
  }

  Rule LowUdpPort() {
    return Sequence("LOW_UDP_PORT", push(Uint16AstNode.of(LOW_UDP_PORT)));
  }

  Rule Uint16() {
    return Sequence(Number(), push(Uint16AstNode.of(match())), WhiteSpace());
  }

  Rule Comparator() {
    return FirstOf(Equals(), LessThanOrEquals(), LessThan(), GreaterThanOrEquals(), GreaterThan());
  }

  Rule Equals() {
    return Sequence("=", push(EqualsAstNode.instance()));
  }

  Rule GreaterThanOrEquals() {
    return Sequence(">=", push(GreaterThanOrEqualsAstNode.instance()));
  }

  Rule LessThanOrEquals() {
    return Sequence("<=", push(LessThanOrEqualsAstNode.instance()));
  }

  Rule LessThan() {
    return Sequence("<", push(LessThanAstNode.instance()));
  }

  Rule GreaterThan() {
    return Sequence(">", push(GreaterThanAstNode.instance()));
  }

  Rule Tcp() {
    return Sequence("tcp", push(TcpAstNode.instance()));
  }

  Rule Udp() {
    return Sequence("udp", push(UdpAstNode.instance()));
  }

  Rule DirectionExpr() {
    return Sequence("direction", "=", FirstOf(Incoming(), Outgoing()));
  }

  Rule Incoming() {
    return Sequence("0", push(IncomingAstNode.instance()));
  }

  Rule Outgoing() {
    return Sequence("1", push(OutgoingAstNode.instance()));
  }

  Rule UnhandledExpr() {
    return Sequence(
        FirstOf(CallExpr(), InExpr(), UnhandledComparisonExpr(), UnhandledWord()),
        push(UnhandledAstNode.of(match().trim())));
  }

  Rule InExpr() {
    return Sequence(
        "<", UnhandledWord(), ZeroOrMore(",", UnhandledWord()), ">", "in", UnhandledWord());
  }

  Rule UnhandledComparisonExpr() {
    return Sequence(UnhandledWord(), Comparator(), ACTION(pop() != null), UnhandledWord());
  }

  Rule CallExpr() {
    return Sequence(
        UnhandledWord(),
        "(",
        Optional(
            Sequence(
                Conjunction(),
                ACTION(pop() != null),
                ZeroOrMore(",", Conjunction(), ACTION(pop() != null)))),
        ")");
  }

  Rule UnhandledWord() {
    return Sequence(OneOrMore(NoneOf(" (),=<>")), WhiteSpace());
  }

  Rule WhiteSpace() {
    return ZeroOrMore(AnyOf(" "));
  }

  /** [0-9]+ */
  Rule Number() {
    return OneOrMore(Digit());
  }

  /** [0-9] */
  Rule Digit() {
    return CharRange('0', '9');
  }

  public static @Nonnull BooleanExprAstNode parse(String input) {
    return (BooleanExprAstNode) parse(input, ServiceOtherMatchExpr::TopLevel);
  }

  @VisibleForTesting
  static @Nonnull AstNode parse(
      String input, Function<ServiceOtherMatchExpr, Rule> inputRuleGetter) {
    ServiceOtherMatchExpr parser = instance();
    ParseRunner<AstNode> runner = new BasicParseRunner<>(inputRuleGetter.apply(parser));
    ParsingResult<AstNode> result = runner.run(input);
    if (!result.matched) {
      return ErrorAstNode.instance();
    }
    return result.resultValue;
  }

  static ServiceOtherMatchExpr instance() {
    return Parboiled.createParser(ServiceOtherMatchExpr.class);
  }

  @Override
  protected Rule fromStringLiteral(String string) {
    return Sequence(String(string), WhiteSpace());
  }

  @VisibleForTesting static final int LOW_UDP_PORT = 1024;
}
