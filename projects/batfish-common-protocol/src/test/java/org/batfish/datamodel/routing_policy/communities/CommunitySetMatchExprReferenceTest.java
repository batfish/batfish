package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CommunitySetMatchExprReference}. */
public final class CommunitySetMatchExprReferenceTest {

  private static final CommunitySetMatchExprReference EXPR =
      new CommunitySetMatchExprReference("a");

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(EXPR, CommunitySetMatchExprReference.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new CommunitySetMatchExprReference("a"))
        .addEqualityGroup(new CommunitySetMatchExprReference("b"))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
