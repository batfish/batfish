package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link SiteOfOriginExtendedCommunities}. */
public final class SiteOfOriginExtendedCommunitiesTest {

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(
            SiteOfOriginExtendedCommunities.instance(), SiteOfOriginExtendedCommunities.class),
        equalTo(SiteOfOriginExtendedCommunities.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(SiteOfOriginExtendedCommunities.instance()),
        equalTo(SiteOfOriginExtendedCommunities.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(SiteOfOriginExtendedCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
