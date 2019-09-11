package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link InputCommunities}. */
public final class InputCommunitiesTest {

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(
        BatfishObjectMapper.clone(InputCommunities.instance(), InputCommunities.class),
        equalTo(InputCommunities.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(InputCommunities.instance()),
        equalTo(InputCommunities.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(InputCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
