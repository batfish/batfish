package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.jupiter.api.Test;

/** Test of {@link ConcreteUnicastAddressIp}. */
final class ConcreteUnicastAddressIpTest {

  private static final ConcreteUnicastAddressIp OBJ = new ConcreteUnicastAddressIp(Ip.ZERO);

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(OBJ, OBJ, new ConcreteUnicastAddressIp(Ip.ZERO))
        .addEqualityGroup(new Object())
        .addEqualityGroup(new ConcreteUnicastAddressIp(Ip.MAX))
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    assertEquals(OBJ, SerializationUtils.clone(OBJ));
  }
}
