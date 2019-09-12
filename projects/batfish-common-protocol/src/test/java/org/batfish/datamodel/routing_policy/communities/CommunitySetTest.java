package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunitySet}. */
public final class CommunitySetTest {

  private static final CommunitySet SET = CommunitySet.of();

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(BatfishObjectMapper.clone(SET, CommunitySet.class), equalTo(SET));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(SET), equalTo(SET));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(SET, SET, CommunitySet.empty())
        .addEqualityGroup(CommunitySet.of(StandardCommunity.of(1L)))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
