package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link VpnDistinguisherExtendedCommunities}. */
public final class VpnDistinguisherExtendedCommunitiesTest {

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(
            VpnDistinguisherExtendedCommunities.instance(),
            VpnDistinguisherExtendedCommunities.class),
        equalTo(VpnDistinguisherExtendedCommunities.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(VpnDistinguisherExtendedCommunities.instance()),
        equalTo(VpnDistinguisherExtendedCommunities.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(VpnDistinguisherExtendedCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
