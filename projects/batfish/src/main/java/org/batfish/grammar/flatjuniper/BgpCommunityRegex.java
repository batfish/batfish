package org.batfish.grammar.flatjuniper;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

/** A class that converts a Juniper community regex to a Java regex. */
@BuildParseTree
@SuppressWarnings({"checkstyle:methodname", "WeakerAccess"})
public class BgpCommunityRegex extends BaseParser<String> {

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
    return Sequence(
        Term_Inner(), // first term, will be pop(1) below
        ZeroOrMore(
            Term_Inner(), // pop()
            push(String.format("%s%s", pop(1), pop()))));
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

  static void testParsingRegex(String regex, String output) {
    String result = convertToJavaRegex(regex);
    checkArgument(output.equals(result), "Expected %s, got %s", output, result);
  }

  static String convertToJavaRegex(String regex) {
    BgpCommunityRegex parser = Parboiled.createParser(BgpCommunityRegex.class);
    TracingParseRunner<String> runner =
        new TracingParseRunner<String>(parser.TopLevel()).withLog(new StringBuilderSink());
    ParsingResult<String> result = runner.run(regex);
    if (!result.matched) {
      throw new IllegalArgumentException("Unhandled input: " + regex + runner.getLog());
    }
    return result.resultValue;
  }

  static void testMatches(String regex, String... inputs) {
    String javaRegex = convertToJavaRegex(regex);
    for (String input : inputs) {
      checkArgument(
          input.matches(javaRegex),
          "%s doesn't match /%s/ (converted from /%s/)",
          input,
          javaRegex,
          regex);
    }
  }

  public static void main(String[] args) {
    // test that they parse.
    testParsingRegex("no-advertise", "^65535:65282$");
    testParsingRegex("no-export", "^65535:65281$");
    testParsingRegex("no-export-subconfed", "^65535:65283$");
    testParsingRegex("123+:123+", "123+:123+");
    testParsingRegex("^(56):123$", "^(56):123$");
    testParsingRegex("^((56) | (78)):(.*)$", "^((56)|(78)):(.*)$");
    testParsingRegex("^(.*):(.*[579])$", "^(.*):(.*[579])$");
    testParsingRegex("^((56) | (78)):(2.*[2-8])$", "^((56)|(78)):(2.*[2-8])$");

    // test that the conversion matches
    testMatches("^((56) | (78)):(.*)$", "56:1000", "78:64500");
    testMatches("^56:(2.*)$", "56:2", "56:222", "56:234");
    testMatches("^(.*):(.*[579])$", "1234:5", "78:2357", "34:64509");
    testMatches("^((56) | (78)):(2.*[2-8])$", "56:22", "56:21197", "78:2678");
  }
}
