package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CommunityIn}. */
public final class CommunityInTest {

  private static final CommunityIn EXPR = new CommunityIn(new CommunitySetReference("a"));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunityIn.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new CommunityIn(new CommunitySetReference("a")))
        .addEqualityGroup(new CommunityIn(new CommunitySetReference("b")))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
