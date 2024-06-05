package org.batfish.datamodel.routing_policy.as_path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AsPathMatchRegex}. */
public final class AsPathMatchRegexTest {

  @Test
  public void testJavaSerialization() {
    AsPathMatchRegex obj = AsPathMatchRegex.of("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    AsPathMatchRegex obj = AsPathMatchRegex.of("a");
    assertThat(BatfishObjectMapper.clone(obj, AsPathMatchRegex.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    AsPathMatchRegex obj = AsPathMatchRegex.of("a");
    new EqualsTester()
        .addEqualityGroup(AsPathMatchRegex.of("a"), obj)
        .addEqualityGroup(AsPathMatchRegex.of("b"))
        .testEquals();
  }
}
