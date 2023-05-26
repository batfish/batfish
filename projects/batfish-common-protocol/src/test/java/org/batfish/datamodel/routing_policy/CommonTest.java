package org.batfish.datamodel.routing_policy;

import static org.batfish.datamodel.routing_policy.Common.communitySetMatchRegex;
import static org.junit.Assert.assertEquals;

import org.apache.commons.text.StringEscapeUtils;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.TypesFirstAscendingSpaceSeparated;
import org.junit.Test;

public final class CommonTest {

  @Test
  public void testCommunitySetMatchRegexUnoptimized() {
    assertEquals(
        communitySetMatchRegex("^65000:123 65011:12[3]$"),
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()),
            "^65000:123 65011:12[3]$"));
    assertEquals(
        communitySetMatchRegex("^$"),
        new CommunitySetMatchRegex(
            new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()), "^$"));
  }

  @Test
  public void testVisitCommunitySetMatchRegexOptimized() {

    String underscore = StringEscapeUtils.unescapeJava(Common.DEFAULT_UNDERSCORE_REPLACEMENT);

    assertEquals(
        communitySetMatchRegex("53"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "53")));
    assertEquals(
        communitySetMatchRegex("53:"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "53:")));
    assertEquals(
        communitySetMatchRegex(underscore + "53:"),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), underscore + "53:")));
    assertEquals(
        communitySetMatchRegex(":53"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":53")));
    assertEquals(
        communitySetMatchRegex(":53" + underscore),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), ":53" + underscore)));
    assertEquals(
        communitySetMatchRegex("[0-9]+:"),
        new HasCommunity(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "[0-9]+:")));
    assertEquals(
        communitySetMatchRegex("[0-9]+:[123]*"),
        new HasCommunity(
            new CommunityMatchRegex(ColonSeparatedRendering.instance(), "[0-9]+:[123]*")));
  }
}
