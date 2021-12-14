package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class MgmtVrfTest {

  @Test
  public void testJavaSerialization() {
    MgmtVrf obj = MgmtVrf.builder().setMgmtVrfEnabled(true).build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    MgmtVrf.Builder builder = MgmtVrf.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setMgmtVrfEnabled(true).build())
        .testEquals();
  }
}
