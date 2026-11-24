package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link CommunitySetDifference}. */
final class CommunitySetDifferenceTest {

  private static final CommunitySetDifference EXPR =
      new CommunitySetDifference(
          new CommunitySetExprReference("a"), new CommunityMatchExprReference("a"));

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunitySetDifference.class), equalTo(EXPR));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EXPR,
            EXPR,
            new CommunitySetDifference(
                new CommunitySetExprReference("a"), new CommunityMatchExprReference("a")))
        .addEqualityGroup(
            new CommunitySetDifference(
                new CommunitySetExprReference("b"), new CommunityMatchExprReference("a")))
        .addEqualityGroup(
            new CommunitySetDifference(
                new CommunitySetExprReference("b"), new CommunityMatchExprReference("b")))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
