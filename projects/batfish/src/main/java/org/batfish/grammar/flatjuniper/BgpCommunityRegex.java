package org.batfish.grammar.flatjuniper;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

/** A class that converts a Juniper community regex to a Java regex. */
@BuildParseTree
public final class BgpCommunityRegex extends BaseParser<String> {

  Rule TopLevel() {
    return Sequence(Digits(), ':', Digits());
  }

  Rule Digits() {
    return OneOrMore(CharRange('0', '9'));
  }

  public static String convertToJavaRegex(String regex) {
    BgpCommunityRegex parser = Parboiled.createParser(BgpCommunityRegex.class);
    ParsingResult<String> result = new ReportingParseRunner<String>(parser.TopLevel()).run(regex);
    String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
    System.out.println(parseTreePrintOut);
    return "foo";
  }

  public static void main(String[] args) {
    convertToJavaRegex("123:456");
  }
}
