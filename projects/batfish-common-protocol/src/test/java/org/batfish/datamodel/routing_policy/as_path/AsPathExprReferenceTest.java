package org.batfish.datamodel.routing_policy.as_path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AsPathExprReference}. */
public final class AsPathExprReferenceTest {

  @Test
  public void testJavaSerialization() {
    AsPathExprReference obj = AsPathExprReference.of("a");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    AsPathExprReference obj = AsPathExprReference.of("a");
    assertThat(BatfishObjectMapper.clone(obj, AsPathExprReference.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    AsPathExprReference obj = AsPathExprReference.of("a");
    new EqualsTester()
        .addEqualityGroup(AsPathExprReference.of("a"), obj)
        .addEqualityGroup(AsPathExprReference.of("b"))
        .testEquals();
  }
}
