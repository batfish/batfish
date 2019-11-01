package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link ConcreteUnicastAddressIp}. */
public final class ConcreteUnicastAddressIpTest {

  private static final ConcreteUnicastAddressIp OBJ = new ConcreteUnicastAddressIp(Ip.ZERO);

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(OBJ, OBJ, new ConcreteUnicastAddressIp(Ip.ZERO))
        .addEqualityGroup(new Object())
        .addEqualityGroup(new ConcreteUnicastAddressIp(Ip.MAX))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    assertEquals(OBJ, SerializationUtils.clone(OBJ));
  }
}
