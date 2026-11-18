package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.LineAction;
import org.junit.jupiter.api.Test;

/** Test of {@link CommunityAcl}. */
final class CommunityAclTest {

  private static final CommunityAcl ACL = new CommunityAcl(ImmutableList.of());

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(ACL, CommunityAcl.class), equalTo(ACL));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(ACL), equalTo(ACL));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(ACL, ACL, new CommunityAcl(ImmutableList.of()))
        .addEqualityGroup(
            new CommunityAcl(
                ImmutableList.of(
                    new CommunityAclLine(LineAction.DENY, AllExtendedCommunities.instance()))))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
