package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Parser.initAnchors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.parboiled.Rule;
import org.parboiled.parserunners.BasicParseRunner;

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
  public void testEnumValue() {
    CommonParser parser = CommonParser.instance();
    Rule rule = parser.input(parser.EnumValue());

    // legal naked strings
    assertTrue(matches("a", rule));
    assertTrue(matches("A", rule));
    assertTrue(matches("abc", rule));
    assertTrue(matches("ABC", rule));
    assertTrue(matches("_startUnderscore", rule));
    assertTrue(matches("has9number", rule));
    assertTrue(matches("has-dash", rule));

    // illegal strings
    assertFalse(matches("", rule)); // empty
    assertFalse(matches("9numberstart", rule));
    assertFalse(matches("-dashstart", rule));
    assertFalse(matches("\"quoted\"", rule));
    assertFalse(matches("has,", rule));
    assertFalse(matches("has\\", rule));
    assertFalse(matches("has&", rule));
    assertFalse(matches("has(", rule));
    assertFalse(matches("has)", rule));
    assertFalse(matches("has[", rule));
    assertFalse(matches("has]", rule));
    assertFalse(matches("/startSlash", rule));
    assertFalse(matches("@startAt", rule));
    assertFalse(matches("", rule));
    assertFalse(matches("\"", rule));
    assertFalse(matches("\"\"", rule));
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
}
