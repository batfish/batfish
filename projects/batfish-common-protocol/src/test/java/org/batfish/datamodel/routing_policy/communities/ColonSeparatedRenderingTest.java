package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ColonSeparatedRendering}. */
public final class ColonSeparatedRenderingTest {

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(
            ColonSeparatedRendering.instance(), ColonSeparatedRendering.class),
        equalTo(ColonSeparatedRendering.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(ColonSeparatedRendering.instance()),
        equalTo(ColonSeparatedRendering.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(ColonSeparatedRendering.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
