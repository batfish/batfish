package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link FibNullRoute}. */
public final class FibNullRouteTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(FibNullRoute.INSTANCE, FibNullRoute.INSTANCE)
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    assertEquals(FibNullRoute.INSTANCE, SerializationUtils.clone(FibNullRoute.INSTANCE));
  }
}
