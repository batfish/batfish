package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link RouteTargetExtendedCommunities}. */
final class RouteTargetExtendedCommunitiesTest {

  @Test
  void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(
            RouteTargetExtendedCommunities.instance(), RouteTargetExtendedCommunities.class),
        equalTo(RouteTargetExtendedCommunities.instance()));
  }

  @Test
  void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(RouteTargetExtendedCommunities.instance()),
        equalTo(RouteTargetExtendedCommunities.instance()));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(RouteTargetExtendedCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
