package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AllStandardCommunities}. */
public final class AllStandardCommunitiesTest {

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(AllStandardCommunities.instance(), AllStandardCommunities.class),
        equalTo(AllStandardCommunities.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(AllStandardCommunities.instance()),
        equalTo(AllStandardCommunities.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(AllStandardCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
