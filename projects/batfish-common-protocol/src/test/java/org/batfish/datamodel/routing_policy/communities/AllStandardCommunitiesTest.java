package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AllStandardCommunities}. */
public final class AllStandardCommunitiesTest {

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(
        BatfishObjectMapper.clone(AllStandardCommunities.instance(), AllStandardCommunities.class),
        equalTo(AllStandardCommunities.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(AllStandardCommunities.instance()),
        equalTo(AllStandardCommunities.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(AllStandardCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
