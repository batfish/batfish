package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link ManagementIp}. */
public final class ManagementIpTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(ManagementIp.instance(), ManagementIp.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    assertEquals(ManagementIp.instance(), SerializationUtils.clone(ManagementIp.instance()));
  }
}
