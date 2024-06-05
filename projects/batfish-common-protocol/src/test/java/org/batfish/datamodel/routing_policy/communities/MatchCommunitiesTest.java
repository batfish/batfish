package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Test of {@link MatchCommunities}. */
public final class MatchCommunitiesTest {

  private static final MatchCommunities EXPR =
      new MatchCommunities(
          new CommunitySetExprReference("a"), new CommunitySetMatchExprReference("a"));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, MatchCommunities.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EXPR,
            EXPR,
            new MatchCommunities(
                new CommunitySetExprReference("a"), new CommunitySetMatchExprReference("a")))
        .addEqualityGroup(
            new MatchCommunities(
                new CommunitySetExprReference("b"), new CommunitySetMatchExprReference("a")))
        .addEqualityGroup(
            new MatchCommunities(
                new CommunitySetExprReference("b"), new CommunitySetMatchExprReference("b")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    Configuration c = new Configuration("h", ConfigurationFormat.CISCO_IOS);
    c.setCommunitySetMatchExprs(
        ImmutableMap.of("a", new HasCommunity(new CommunityIs(StandardCommunity.of(1L)))));
    Environment env = Environment.builder(c).build();

    MatchCommunities matching =
        new MatchCommunities(
            new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(1L))),
            new CommunitySetMatchExprReference("a"));

    assertTrue(matching.evaluate(env).getBooleanValue());

    MatchCommunities notMatching =
        new MatchCommunities(
            new LiteralCommunitySet(CommunitySet.empty()), new CommunitySetMatchExprReference("a"));

    assertFalse(notMatching.evaluate(env).getBooleanValue());
  }
}
