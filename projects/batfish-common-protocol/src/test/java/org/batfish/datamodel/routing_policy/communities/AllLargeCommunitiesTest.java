package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AllLargeCommunities}. */
public final class AllLargeCommunitiesTest {

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(AllLargeCommunities.instance(), AllLargeCommunities.class),
        equalTo(AllLargeCommunities.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(AllLargeCommunities.instance()),
        equalTo(AllLargeCommunities.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(AllLargeCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
