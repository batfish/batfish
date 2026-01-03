package org.batfish.datamodel.routing_policy.as_path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AsPathMatchExprReference}. */
public final class AsPathMatchExprReferenceTest {

  @Test
  public void testJavaSerialization() {
    AsPathMatchExprReference obj = AsPathMatchExprReference.of("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    AsPathMatchExprReference obj = AsPathMatchExprReference.of("a");
    assertThat(BatfishObjectMapper.clone(obj, AsPathMatchExprReference.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    AsPathMatchExprReference obj = AsPathMatchExprReference.of("a");
    new EqualsTester()
        .addEqualityGroup(AsPathMatchExprReference.of("a"), obj)
        .addEqualityGroup(AsPathMatchExprReference.of("b"))
        .testEquals();
  }
}
