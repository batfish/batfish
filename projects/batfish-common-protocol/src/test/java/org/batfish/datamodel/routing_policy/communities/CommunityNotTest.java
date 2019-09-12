package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CommunityNot}. */
public final class CommunityNotTest {

  private static final CommunityNot EXPR = new CommunityNot(AllStandardCommunities.instance());

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunityNot.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new CommunityNot(AllStandardCommunities.instance()))
        .addEqualityGroup(new CommunityNot(AllExtendedCommunities.instance()))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
