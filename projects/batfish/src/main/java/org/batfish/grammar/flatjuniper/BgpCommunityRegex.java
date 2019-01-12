package org.batfish.grammar.flatjuniper;

import org.batfish.common.WellKnownCommunity;
import org.batfish.common.util.CommonUtil;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SkipNode;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.common.StringBuilderSink;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

/** A class that converts a Juniper community regex to a Java regex. */
@BuildParseTree
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class BgpCommunityRegex extends BaseParser<String> {

  // Helper to convert Juniper constants to BGP community constants.
  static String wellKnownToRegex(String s) {
    long wellKnownValue;
    switch (s) {
      case "no-advertise":
        wellKnownValue = WellKnownCommunity.NO_ADVERTISE;
        break;
      case "no-export":
        wellKnownValue = WellKnownCommunity.NO_EXPORT;
        break;
      case "no-export-subconfed":
        wellKnownValue = WellKnownCommunity.NO_EXPORT_SUBCONFED;
        break;
      default:
        throw new IllegalArgumentException(s);
    }
    return '^' + CommonUtil.longToCommunity(wellKnownValue) + '$';
  }

  Rule TopLevel() {
    return Sequence(FirstOf(LiteralCommunity(), RegexCommunity()), EOI);
  }

  Rule LiteralCommunity() {
    return FirstOf(RegularCommunity(), WellKnownCommunity());
  }

  Rule Wildcard() {
    return Sequence(Ch('*'), push(".*"));
  }

  Rule RegularCommunity() {
    return Sequence(Digits(), ':', Digits(), push(String.format("^%s:%s$", pop(1), pop(0))));
  }

  Rule WellKnownCommunity() {
    return Sequence(
        FirstOf("no-advertise", "no-export-subconfed", "no-export"),
        push(wellKnownToRegex(match())));
  }

  Rule RegexCommunity() {
    return Sequence(
        Optional('^'),
        push(matchOrDefault("")),
        Term(),
        ':',
        Term(),
        Optional('$'),
        push(matchOrDefault("")),
        push(String.format("%s%s:%s%s", pop(3), pop(2), pop(1), pop())));
  }

  @SkipNode
  Rule Operator() {
    return Sequence(
        Optional(
            FirstOf(
                Op_Asterisk(), Op_Plus(), Op_QuestionMark(), Op_Exact(), Op_OrMore(), Op_Range())),
        push(matchOrDefault("")));
  }

  @SuppressSubnodes
  Rule Op_Asterisk() {
    return Ch('*');
  }

  @SuppressSubnodes
  Rule Op_Plus() {
    return Ch('+');
  }

  @SuppressSubnodes
  Rule Op_QuestionMark() {
    return Ch('?');
  }

  @SuppressSubnodes
  Rule Op_Exact() {
    return Sequence('{', Digits(), '}');
  }

  @SuppressSubnodes
  Rule Op_Range() {
    return Sequence('{', Digits(), ',', Digits(), '}');
  }

  @SuppressSubnodes
  Rule Op_OrMore() {
    return Sequence('{', Digits(), ',', '}');
  }

  Rule Term_Inner() {
    return Sequence(T_TopLevel(), Operator(), push(String.format("%s%s", pop(1), pop())));
  }

  Rule Term() {
    return FirstOf(
        Wildcard(), // wildcard is only valid if it's the entire term
        Sequence(
            Term_Inner(), // first term, will be pop(1) below
            ZeroOrMore(
                Term_Inner(), // pop()
                push(String.format("%s%s", pop(1), pop())))));
  }

  Rule T_TopLevel() {
    return FirstOf(T_Group(), T_Or(), SetOfDigits(), Digits(), T_Dot());
  }

  @SuppressSubnodes
  Rule T_Dot() {
    return Sequence(Ch('.'), push(match()));
  }

  Rule T_Group() {
    return Sequence(
        '(',
        IgnoreSpace(),
        Term(),
        ZeroOrMore(Term(), push(String.format("%s%s", pop(1), pop()))),
        IgnoreSpace(),
        ')',
        push(String.format("(%s)", pop())));
  }

  Rule T_Or() {
    return Sequence(
        '(',
        IgnoreSpace(),
        Term(), // pop(1)
        IgnoreSpace(),
        '|',
        IgnoreSpace(),
        Term(), // pop()
        IgnoreSpace(),
        ')',
        push(String.format("(%s|%s)", pop(1), pop())));
  }

  @SuppressSubnodes
  Rule Digit() {
    return CharRange('0', '9');
  }

  @SuppressSubnodes
  Rule Digits() {
    return Sequence(OneOrMore(Digit()), push(match()));
  }

  @SuppressSubnodes
  Rule DigitRange() {
    return Sequence(
        Sequence(Digit(), '-', Digit()), // preserve as-is
        push(match()));
  }

  @SuppressNode
  Rule IgnoreSpace() {
    return ZeroOrMore(' ');
  }

  Rule SetOfDigits() {
    return Sequence(
        '[',
        Optional('^'),
        push(matchOrDefault("")), // pop(1)
        FirstOf(DigitRange(), Digits()), // pop()
        ']',
        push(String.format("[%s%s]", pop(1), pop())));
  }

  /** Converts the given Juniper regular expression to a Java regular expression. */
  public static String convertToJavaRegex(String regex) {
    BgpCommunityRegex parser = Parboiled.createParser(BgpCommunityRegex.class);
    BasicParseRunner<String> runner = new BasicParseRunner<>(parser.TopLevel());
    ParsingResult<String> result = runner.run(regex);
    if (!result.matched) {
      throw new IllegalArgumentException("Unhandled input: " + regex);
    }
    return result.resultValue;
  }

  /** Like {@link #convertToJavaRegex(String)}, but for debugging. */
  @SuppressWarnings("unused") // leaving here for future debugging.
  static String debugConvertToJavaRegex(String regex) {
    BgpCommunityRegex parser = Parboiled.createParser(BgpCommunityRegex.class);
    TracingParseRunner<String> runner =
        new TracingParseRunner<String>(parser.TopLevel()).withLog(new StringBuilderSink());
    ParsingResult<String> result = runner.run(regex);
    if (!result.matched) {
      throw new IllegalArgumentException("Unhandled input: " + regex + "\n" + runner.getLog());
    }
    return result.resultValue;
  }
}
