package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class PortTest {

  @Test
  public void testJavaSerialization() {
    Port obj = Port.builder().build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    Port.Builder builder = Port.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAdminStatus(true))
        .addEqualityGroup(builder.setDescription("desc"))
        .addEqualityGroup(builder.setMtu(23))
        .testEquals();
  }
}
