package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link ColonSeparatedRendering}. */
final class ColonSeparatedRenderingTest {

  @Test
  void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(
            ColonSeparatedRendering.instance(), ColonSeparatedRendering.class),
        equalTo(ColonSeparatedRendering.instance()));
  }

  @Test
  void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(ColonSeparatedRendering.instance()),
        equalTo(ColonSeparatedRendering.instance()));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(ColonSeparatedRendering.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
