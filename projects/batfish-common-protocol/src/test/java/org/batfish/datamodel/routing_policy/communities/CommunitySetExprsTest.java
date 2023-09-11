package org.batfish.datamodel.routing_policy.communities;

import static org.batfish.datamodel.routing_policy.communities.CommunitySetExprs.toMatchExpr;
import static org.junit.Assert.assertEquals;

import org.apache.commons.text.StringEscapeUtils;
import org.batfish.datamodel.routing_policy.Common;
import org.junit.Test;

public final class CommunitySetExprsTest {

  @Test
  public void testCommunitySetMatchRegexUnoptimized() {
    assertEquals(
        toMatchExpr("^65000:123 65011:12[3]$"),
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()),
            "^65000:123 65011:12[3]$"));
    assertEquals(
        toMatchExpr("^65000:123 65011:12[3]$", IntegerValueRendering.instance()),
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(IntegerValueRendering.instance()),
            "^65000:123 65011:12[3]$"));
    assertEquals(
        toMatchExpr("^$"),
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()), "^$"));
  }

  @Test
  public void testVisitCommunitySetMatchRegexOptimized() {

    String underscore = StringEscapeUtils.unescapeJava(Common.DEFAULT_UNDERSCORE_REPLACEMENT);

    assertEquals(
        toMatchExpr("53"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "53")));
    assertEquals(
        toMatchExpr("53:"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "53:")));
    assertEquals(
        toMatchExpr(underscore + "53:"),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), underscore + "53:")));
    assertEquals(
        toMatchExpr(":53"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":53")));
    assertEquals(
        toMatchExpr(":53" + underscore),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":53" + underscore)));
    assertEquals(
        toMatchExpr("[0-9]+:"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "[0-9]+:")));
    assertEquals(
        toMatchExpr("[0-9]+:[123]*"),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), "[0-9]+:[123]*")));
    assertEquals(
        toMatchExpr("[0-9]+:[123]*", IntegerValueRendering.instance()),
        new HasCommunity(
            new CommunityMatchRegex(IntegerValueRendering.instance(), "[0-9]+:[123]*")));
  }
}
