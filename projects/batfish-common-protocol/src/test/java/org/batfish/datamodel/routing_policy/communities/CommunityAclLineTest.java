package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.LineAction;
import org.junit.Test;

/** Test of {@link CommunityAclLine}. */
public final class CommunityAclLineTest {

  private static final CommunityAclLine LINE =
      new CommunityAclLine(LineAction.PERMIT, AllStandardCommunities.instance());

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(LINE, CommunityAclLine.class), equalTo(LINE));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(LINE), equalTo(LINE));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            LINE, LINE, new CommunityAclLine(LineAction.PERMIT, AllStandardCommunities.instance()))
        .addEqualityGroup(new CommunityAclLine(LineAction.DENY, AllStandardCommunities.instance()))
        .addEqualityGroup(new CommunityAclLine(LineAction.DENY, AllExtendedCommunities.instance()))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
