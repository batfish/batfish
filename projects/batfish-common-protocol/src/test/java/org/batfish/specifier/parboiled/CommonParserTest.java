package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Parser.initAnchors;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.parboiled.Rule;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;

/** Tests for {@link CommonParser} */
public class CommonParserTest {

  private static boolean matches(String query, Rule rule) {
    return new BasicParseRunner<AstNode>(rule).run(query).matched;
  }

  /** Test that anchor annotations are being correctly picked up */
  @Test
  public void testInitAnchors() {
    assertThat(
        initAnchors(TestParser.class).keySet(),
        containsInAnyOrder(
            "EOI",
            "AsciiButNot",
            "CloseBrackets",
            "CloseParens",
            "EscapedSlash",
            "EscapedQuote",
            "TestAddressGroupName",
            "TestIpAddress",
            "TestNotOp",
            "TestIpRange",
            "TestName",
            "TestNameRegex",
            "TestNameRegexDeprecated",
            "TestParens",
            "TestReferenceBookName",
            "TestSpec",
            "TestSpecifierInput",
            "TestSpecifierInputTail",
            "WhiteSpace"));
  }

  @Test
  public void testNameLiteral() {
    CommonParser parser = CommonParser.instance();
    Rule rule = parser.input(parser.NameLiteral());

    // legal naked strings
    assertTrue(matches("a", rule));
    assertTrue(matches("has/", rule));
    assertTrue(matches("~startTilde", rule));
    assertTrue(matches(":startColon", rule));

    // legal quoted strings
    assertTrue(matches("\"a\"", rule));
    assertTrue(matches("\" \\\" \"", rule)); // escaped quote
    assertTrue(matches("\" \\t,\\&()[]@\"", rule)); // all our special chars

    // illegal strings
    assertFalse(matches("has space", rule));
    assertFalse(matches("has\\t", rule));
    assertFalse(matches("has,", rule));
    assertFalse(matches("has\\", rule));
    assertFalse(matches("has&", rule));
    assertFalse(matches("has(", rule));
    assertFalse(matches("has)", rule));
    assertFalse(matches("has[", rule));
    assertFalse(matches("has]", rule));
    assertFalse(matches("1startDigit", rule));
    assertFalse(matches("/startSlash", rule));
    assertFalse(matches("@startAt", rule));
    assertFalse(matches("", rule));
    assertFalse(matches("\"", rule));
    assertFalse(matches("\"\"", rule));
  }

  @Test
  public void testRegexDeprecated() {
    CommonParser parser = CommonParser.instance();
    Rule rule = parser.input(parser.RegexDeprecated());

    assertTrue(matches(".*", rule));
    assertTrue(matches("host.*", rule));
    assertTrue(matches("as1-.*", rule));
    assertTrue(matches(".*-b", rule));
    assertTrue(matches(".*border.*", rule));

    assertFalse(matches("has space", rule));
    assertFalse(matches("@start", rule));
    assertFalse(matches("1startDigit", rule));
    assertFalse(matches("/startSlash", rule));
    assertFalse(matches("has[", rule));
    assertFalse(matches("has(", rule));
  }

  @Test
  public void testSavedStackInvalidInputAddressGroupNoComma() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(g1");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("g1"))));
  }

  @Test
  public void testSavedStackInvalidInputAddressGroup() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(g1,");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("g1"))));
  }

  @Test
  public void testSavedStackInvalidInputAddressGroupAndReferenceBook() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, b ");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("b"), new StringAstNode("a1"))));
  }

  @Test
  public void testSavedStackInvalidInputAddressGroupAndEscapedReferenceBook() {
    TestParser parser = TestParser.instance();
    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, \"b");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("b"), new StringAstNode("a1"))));
  }

  @Test
  public void testSavedStackInvalidInputAddressGroupAndEscapedReferenceBookTrailingSpace() {
    TestParser parser = TestParser.instance();
    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, \"b ");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("b "), new StringAstNode("a1"))));
  }

  @Test
  public void testSavedStackValidInput() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, b1)");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new AddressGroupIpSpaceAstNode("a1", "b1"))));
  }

  @Test
  public void testSavedStackSetInput() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, b1), 1.1.1.1");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(
            ImmutableList.of(
                new IpAstNode("1.1.1.1"), new AddressGroupIpSpaceAstNode("a1", "b1"))));
  }

  @Test
  public void testSavedStackSetSecondInput() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("1.1.1.1, @specifier(a1, b1");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(
            ImmutableList.of(
                new StringAstNode("b1"), new StringAstNode("a1"), new IpAstNode("1.1.1.1"))));
  }
}
