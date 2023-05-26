package org.batfish.datamodel.routing_policy.communities;

import static org.batfish.datamodel.routing_policy.communities.CommunitySetExprs.fromRegex;
import static org.junit.Assert.assertEquals;

import org.apache.commons.text.StringEscapeUtils;
import org.batfish.datamodel.routing_policy.Common;
import org.junit.Test;

public final class CommunitySetExprsTest {

  @Test
  public void testCommunitySetMatchRegexUnoptimized() {
    assertEquals(
        fromRegex("^65000:123 65011:12[3]$"),
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()),
            "^65000:123 65011:12[3]$"));
    assertEquals(
        fromRegex("^$"),
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()), "^$"));
  }

  @Test
  public void testVisitCommunitySetMatchRegexOptimized() {

    String underscore = StringEscapeUtils.unescapeJava(Common.DEFAULT_UNDERSCORE_REPLACEMENT);

    assertEquals(
        fromRegex("53"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "53")));
    assertEquals(
        fromRegex("53:"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "53:")));
    assertEquals(
        fromRegex(underscore + "53:"),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), underscore + "53:")));
    assertEquals(
        fromRegex(":53"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":53")));
    assertEquals(
        fromRegex(":53" + underscore),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":53" + underscore)));
    assertEquals(
        fromRegex("[0-9]+:"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "[0-9]+:")));
    assertEquals(
        fromRegex("[0-9]+:[123]*"),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), "[0-9]+:[123]*")));
  }
}
