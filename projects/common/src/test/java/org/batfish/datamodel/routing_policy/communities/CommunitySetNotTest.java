package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link CommunitySetNot}. */
final class CommunitySetNotTest {

  private static final CommunitySetNot EXPR =
      new CommunitySetNot(new CommunitySetMatchExprReference("a"));

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunitySetNot.class), equalTo(EXPR));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new CommunitySetNot(new CommunitySetMatchExprReference("a")))
        .addEqualityGroup(new CommunitySetNot(new CommunitySetMatchExprReference("b")))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
