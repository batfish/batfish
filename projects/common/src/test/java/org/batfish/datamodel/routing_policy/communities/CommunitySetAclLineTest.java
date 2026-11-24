package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.LineAction;
import org.junit.jupiter.api.Test;

/** Test of {@link CommunitySetAclLine}. */
final class CommunitySetAclLineTest {

  private static final CommunitySetAclLine LINE =
      new CommunitySetAclLine(LineAction.PERMIT, new CommunitySetMatchExprReference("a"));

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(LINE, CommunitySetAclLine.class), equalTo(LINE));
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(LINE), equalTo(LINE));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            LINE,
            LINE,
            new CommunitySetAclLine(LineAction.PERMIT, new CommunitySetMatchExprReference("a")))
        .addEqualityGroup(
            new CommunitySetAclLine(LineAction.DENY, new CommunitySetMatchExprReference("a")))
        .addEqualityGroup(
            new CommunitySetAclLine(LineAction.DENY, new CommunitySetMatchExprReference("b")))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
