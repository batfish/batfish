package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link InputCommunities}. */
final class InputCommunitiesTest {

  @Test
  void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(InputCommunities.instance(), InputCommunities.class),
        equalTo(InputCommunities.instance()));
  }

  @Test
  void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(InputCommunities.instance()),
        equalTo(InputCommunities.instance()));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(InputCommunities.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
