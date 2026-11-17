package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class OpaqueExtendedCommunitiesTest {
  @Test
  void testSerialization() {
    OpaqueExtendedCommunities oec = OpaqueExtendedCommunities.of(true, 1);
    assertThat(BatfishObjectMapper.clone(oec, CommunityMatchExpr.class), equalTo(oec));
    assertThat(SerializationUtils.clone(oec), equalTo(oec));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            OpaqueExtendedCommunities.of(false, 1), OpaqueExtendedCommunities.of(false, 1))
        .addEqualityGroup(OpaqueExtendedCommunities.of(true, 1))
        .addEqualityGroup(OpaqueExtendedCommunities.of(true, 2))
        .testEquals();
  }
}
