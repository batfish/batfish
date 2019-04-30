package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.CommonParser.nameNeedsEscaping;
import static org.batfish.specifier.parboiled.Parser.initAnchors;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Test;
import org.parboiled.Rule;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;

public class CommonParserTest {

  private static boolean matches(String query, Rule rule) {
    return new BasicParseRunner<AstNode>(rule).run(query).matched;
  }

  @Test
  public void testNameNeedsEscaping() {
    assertFalse("null", nameNeedsEscaping(null));
    assertFalse("empty", nameNeedsEscaping(""));
    assertFalse("normal", nameNeedsEscaping("abc"));

    assertTrue("digit start", nameNeedsEscaping("1abc"));
    assertTrue("quote start", nameNeedsEscaping("\"abc"));
    assertTrue("slash start", nameNeedsEscaping("/abc"));
    assertTrue("special char", nameNeedsEscaping("a bc"));
  }

  @Test
  public void testInitAnchors() {
    assertThat(
        initAnchors(TestParser.class),
        equalTo(
            ImmutableMap.<String, Type>builder()
                .put("EOI", Type.EOI)
                .put("AsciiButNot", Type.IGNORE)
                .put("EscapedSlash", Type.IGNORE)
                .put("EscapedQuote", Type.IGNORE)
                .put("TestAddressGroupName", Type.ADDRESS_GROUP_NAME)
                .put("TestIpAddress", Type.IP_ADDRESS)
                .put("TestIpRange", Type.IP_RANGE)
                .put("TestName", Type.NODE_NAME)
                .put("TestNameRegex", Type.NODE_NAME_REGEX)
                .put("TestNameRegexDeprecated", Type.DEPRECATED)
                .put("TestReferenceBookName", Type.REFERENCE_BOOK_NAME)
                .put("TestSpecifierInput", Type.REFERENCE_BOOK_AND_ADDRESS_GROUP)
                .put("WhiteSpace", Type.WHITESPACE)
                .build()));
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
        equalTo(ImmutableList.of(new StringAstNode("a1"), new StringAstNode("b"))));
  }

  @Test
  public void testSavedStackInvalidInputAddressGroupAndEscapedReferenceBook() {
    TestParser parser = TestParser.instance();
    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, \"b");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("a1"), new StringAstNode("b"))));
  }

  @Test
  public void testSavedStackInvalidInputAddressGroupAndEscapedReferenceBookTrailingSpace() {
    TestParser parser = TestParser.instance();
    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, \"b ");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("a1"), new StringAstNode("b "))));
  }

  @Test
  public void testSavedStackValidInput() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, b1)");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(ImmutableList.of(new StringAstNode("a1"), new StringAstNode("b1"))));
  }

  @Test
  public void testSavedStackSetInput() {
    TestParser parser = TestParser.instance();

    new ReportingParseRunner<AstNode>(parser.getInputRule()).run("@specifier(a1, b1), 1.1.1.1");

    assertThat(
        ImmutableList.copyOf(parser.getShadowStack().getValueStack()),
        equalTo(
            ImmutableList.of(
                new StringAstNode("a1"), new StringAstNode("b1"), new IpAstNode("1.1.1.1"))));
  }
}
