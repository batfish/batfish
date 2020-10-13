package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunityIs}. */
public final class CommunityIsTest {

  private static final CommunityIs EXPR = new CommunityIs(StandardCommunity.of(1L));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunityIs.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new CommunityIs(StandardCommunity.of(1L)))
        .addEqualityGroup(new CommunityIs(StandardCommunity.of(2L)))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
