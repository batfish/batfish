package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link ManagementIp}. */
final class ManagementIpTest {

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(ManagementIp.instance(), ManagementIp.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    assertEquals(ManagementIp.instance(), SerializationUtils.clone(ManagementIp.instance()));
  }
}
