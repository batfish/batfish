package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.NamesTest.REFERENCE_OBJECT_INVALID_NAMES;
import static org.batfish.datamodel.NamesTest.REFERENCE_OBJECT_VALID_NAMES;
import static org.batfish.specifier.parboiled.Parser.initAnchors;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.junit.Test;
import org.parboiled.Rule;
import org.parboiled.parserunners.BasicParseRunner;

public class CommonParserTest {

  private static boolean matches(String query, Rule rule) {
    return new BasicParseRunner<AstNode>(CommonParser.INSTANCE.input(rule)).run(query).matched;
  }

  @Test
  public void testContainsChar() {
    assertTrue(matches("a", Parser.INSTANCE.ContainsChar('a')));
    assertTrue(matches("a1", Parser.INSTANCE.ContainsChar('a')));
    assertTrue(matches("1a", Parser.INSTANCE.ContainsChar('a')));
    assertTrue(matches("1a1", Parser.INSTANCE.ContainsChar('a')));

    assertFalse(matches("b", Parser.INSTANCE.ContainsChar('a')));
  }

  @Test
  public void testInitAnchors() {
    assertThat(
        initAnchors(TestParser.class),
        equalTo(
            ImmutableMap.<String, Type>builder()
                .put("TestSpecifierInput", Type.ADDRESS_GROUP_AND_BOOK)
                .put("EOI", Type.EOI)
                .put("AsciiButNot", Type.IGNORE)
                .put("EscapedSlash", Type.IGNORE)
                .put("EscapedQuote", Type.IGNORE)
                .put("TestIpAddress", Type.IP_ADDRESS)
                .put("TestIpRange", Type.IP_RANGE)
                .put("TestName", Type.NODE_NAME)
                .put("TestNameRegex", Type.NODE_NAME_REGEX)
                .put("TestNameRegexDeprecated", Type.IGNORE)
                .put("WhiteSpace", Type.WHITESPACE)
                .build()));
  }

  @Test
  public void testNameLiteral() {
    Rule rule = Parser.INSTANCE.NameLiteral();

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
  }

  @Test
  public void testReferenceObjectNameLiteral() {
    Rule rule = CommonParser.INSTANCE.ReferenceObjectNameLiteral();

    for (String name : REFERENCE_OBJECT_VALID_NAMES) {
      assertTrue(name, matches(name, rule));
    }

    for (String name : REFERENCE_OBJECT_INVALID_NAMES) {
      assertFalse(name, matches(name, rule));
    }
  }

  @Test
  public void testRegexDeprecated() {
    Rule rule = Parser.INSTANCE.RegexDeprecated();

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
