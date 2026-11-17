package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link CommunityNot}. */
final class CommunityNotTest {

  private static final CommunityNot EXPR = new CommunityNot(AllStandardCommunities.instance());

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunityNot.class), equalTo(EXPR));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new CommunityNot(AllStandardCommunities.instance()))
        .addEqualityGroup(new CommunityNot(AllExtendedCommunities.instance()))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
