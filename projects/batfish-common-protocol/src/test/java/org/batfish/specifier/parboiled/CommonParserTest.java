package org.batfish.specifier.parboiled;

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
  public void testInitAnchors() {
    assertThat(
        initAnchors(TestParser.class),
        equalTo(
            ImmutableMap.of(
                "TestSpecifierInput",
                Type.ADDRESS_GROUP_AND_BOOK,
                "EOI",
                Type.EOI,
                "TestIpAddress",
                Type.IP_ADDRESS,
                "TestIpRange",
                Type.IP_RANGE,
                "WhiteSpace",
                Type.WHITESPACE)));
  }

  @Test
  public void testReferenceObjectNameLiteral() {
    Rule rule = CommonParser.INSTANCE.ReferenceObjectNameLiteral();

    assertTrue(matches("_a", rule));
    assertTrue(matches("a_", rule));
    assertTrue(matches("a-", rule)); // dash is allowed
    assertTrue(matches("a1", rule)); // digits are allowed

    assertFalse(matches("1", rule)); // can't begin with a digit
    assertFalse(matches("a/b", rule)); // slash not allowed
    assertFalse(matches("a.b", rule)); // dot not allowed
    assertFalse(matches("a:b", rule)); // colon not allowed
  }
}
