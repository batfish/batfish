package org.batfish.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link FibNullRoute}. */
final class FibNullRouteTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(FibNullRoute.INSTANCE, FibNullRoute.INSTANCE)
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    assertEquals(FibNullRoute.INSTANCE, SerializationUtils.clone(FibNullRoute.INSTANCE));
  }
}
