package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AllExtendedCommunities}. */
public final class AllExtendedCommunitiesTest {

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(AllExtendedCommunities.instance(), AllExtendedCommunities.class),
        equalTo(AllExtendedCommunities.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(AllExtendedCommunities.instance()),
        equalTo(AllExtendedCommunities.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(AllExtendedCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
