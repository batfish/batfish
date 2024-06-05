package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.LineAction;
import org.junit.Test;

/** Test of {@link CommunitySetAcl}. */
public final class CommunitySetAclTest {

  private static final CommunitySetAcl ACL = new CommunitySetAcl(ImmutableList.of());

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(ACL, CommunitySetAcl.class), equalTo(ACL));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(ACL), equalTo(ACL));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(ACL, ACL, new CommunitySetAcl(ImmutableList.of()))
        .addEqualityGroup(
            new CommunitySetAcl(
                ImmutableList.of(
                    new CommunitySetAclLine(
                        LineAction.DENY, new CommunitySetMatchExprReference("a")))))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
