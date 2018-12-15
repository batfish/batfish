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
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** A class that converts a Juniper community regex to a Java regex. */
@BuildParseTree
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

  Rule Op_Asterisk() {
    return Ch('*');
  }

  Rule Op_Plus() {
    return Ch('+');
  }

  Rule Op_QuestionMark() {
    return Ch('?');
  }

  Rule Op_Exact() {
    return Sequence('{', Digits(), '}');
  }

  Rule Op_Range() {
    return Sequence('{', Digits(), ',', Digits(), '}');
  }

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
    return OneOrMore(FirstOf(T_Group(), T_Or(), SetOfDigits(), Digits(), T_Dot()));
  }

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

  Rule TermEOI() {
    return Sequence(Term(), EOI);
  }

  public static String convertToJavaRegex(String regex, String output) {
    BgpCommunityRegex parser = Parboiled.createParser(BgpCommunityRegex.class);
    ParsingResult<String> result = new ReportingParseRunner<String>(parser.TopLevel()).run(regex);

    if (!output.equals(result.resultValue)) {
      return result.resultValue;
    }
    return result.resultValue;
  }

  public static void main(String[] args) {
    convertToJavaRegex("no-advertise", "^65535:65282$");
    convertToJavaRegex("no-export", "^65535:65281$");
    convertToJavaRegex("no-export-subconfed", "^65535:65283$");
    convertToJavaRegex("123+:123+", "123+:123+");
    convertToJavaRegex("^(56):123$", "^(56):123$");
    convertToJavaRegex("^((56) | (78)):(.*)$", "^((56)|(78)):(.*)$");
    convertToJavaRegex("^(.*):(.*[579])$", "");
    //    convertToJavaRegex("^((56) | (78)):(2.*[2–8])$");
    //    convertToJavaRegex("no-advertise|foo");
  }
}
