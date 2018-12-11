package org.batfish.grammar.flatjuniper;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SkipNode;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

/** A class that converts a Juniper community regex to a Java regex. */
@BuildParseTree
public class BgpCommunityRegex extends BaseParser<String> {

  Rule TopLevel() {
    return Sequence(FirstOf(LiteralCommunity(), RegexCommunity()), EOI);
  }

  Rule LiteralCommunity() {
    return FirstOf("no-advertise", "no-export", "no-export-confed", RegularCommunity());
  }

  Rule RegularCommunity() {
    return Sequence(Digits(), ':', Digits());
  }

  Rule RegexCommunity() {
    return Sequence(Optional('^'), Term(), ':', Term(), Optional('$'));
  }

  @SkipNode
  Rule Operator() {
    return FirstOf(
        Op_Asterisk(), Op_Plus(), Op_QuestionMark(), Op_Exact(), Op_OrMore(), Op_Range());
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

  Rule Term() {
    return OneOrMore(T_TopLevel(), Optional(Operator()));
  }

  Rule T_TopLevel() {
    return OneOrMore(FirstOf(T_Group(), T_Or(), SetOfDigits(), Digits(), T_Dot()));
  }

  Rule T_Dot() {
    return Ch('.');
  }

  Rule T_Group() {
    return Sequence('(', IgnoreSpace(), OneOrMore(Term()), IgnoreSpace(), ')');
  }

  Rule T_Or() {
    return Sequence(
        '(', IgnoreSpace(), Term(), IgnoreSpace(), '|', IgnoreSpace(), Term(), IgnoreSpace(), ')');
  }

  @SuppressSubnodes
  Rule Digit() {
    return CharRange('0', '9');
  }

  @SuppressSubnodes
  Rule Digits() {
    return OneOrMore(Digit());
  }

  @SuppressSubnodes
  Rule DigitRange() {
    return Sequence(Digit(), '-', Digit());
  }

  @SuppressNode
  Rule IgnoreSpace() {
    return ZeroOrMore(' ');
  }

  Rule SetOfDigits() {
    return Sequence('[', Optional('^'), FirstOf(DigitRange(), Digits()), ']');
  }

  public static String convertToJavaRegex(String regex) {
    BgpCommunityRegex parser = Parboiled.createParser(BgpCommunityRegex.class);
    ParsingResult<String> result = new ReportingParseRunner<String>(parser.TopLevel()).run(regex);
    String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
    System.out.println(parseTreePrintOut);
    return "foo";
  }

  public static void main(String[] args) {
    convertToJavaRegex("no-advertise");
    convertToJavaRegex("no-export");
    convertToJavaRegex("no-export-subconfed");
    convertToJavaRegex("^123:456$");
    convertToJavaRegex("^((56) | (78)):(.*)$");
    convertToJavaRegex("^(.*):(.*[579])$");
    convertToJavaRegex("^((56) | (78)):(2.*[2–8])$");
    convertToJavaRegex("no-advertise|foo");
  }
}
