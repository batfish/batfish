package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link HasCommunity}. */
public final class HasCommunityTest {

  private static final HasCommunity EXPR = new HasCommunity(AllStandardCommunities.instance());

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, HasCommunity.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new HasCommunity(AllStandardCommunities.instance()))
        .addEqualityGroup(new HasCommunity(AllExtendedCommunities.instance()))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
