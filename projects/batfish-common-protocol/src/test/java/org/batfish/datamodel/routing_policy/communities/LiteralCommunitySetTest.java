package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link LiteralCommunitySet}. */
public final class LiteralCommunitySetTest {

  private static final LiteralCommunitySet OBJ = new LiteralCommunitySet(CommunitySet.empty());

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, LiteralCommunitySet.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(OBJ, OBJ, new LiteralCommunitySet(CommunitySet.empty()))
        .addEqualityGroup(new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(1L))))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
