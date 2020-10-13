package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link IntegerValueRendering}. */
public final class IntegerValueRenderingTest {

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(IntegerValueRendering.instance(), IntegerValueRendering.class),
        equalTo(IntegerValueRendering.instance()));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(IntegerValueRendering.instance()),
        equalTo(IntegerValueRendering.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(IntegerValueRendering.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
