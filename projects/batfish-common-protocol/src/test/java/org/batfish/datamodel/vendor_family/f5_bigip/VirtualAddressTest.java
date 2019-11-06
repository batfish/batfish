package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link VirtualAddress}. */
public final class VirtualAddressTest {

  private VirtualAddress.Builder _builder;

  @Before
  public void setup() {
    _builder =
        VirtualAddress.builder()
            .setAddress(Ip.ZERO)
            .setAddress6(Ip6.ZERO)
            .setArpDisabled(true)
            .setIcmpEchoDisabled(true)
            .setMask(Ip.ZERO)
            .setMask6(Ip6.ZERO)
            .setName("n")
            .setRouteAdvertisementMode(RouteAdvertisementMode.ALL);
  }

  @Test
  public void testEquals() {
    VirtualAddress obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setAddress(null).build())
        .addEqualityGroup(_builder.setAddress6(null).build())
        .addEqualityGroup(_builder.setArpDisabled(null).build())
        .addEqualityGroup(_builder.setIcmpEchoDisabled(null).build())
        .addEqualityGroup(_builder.setMask(null).build())
        .addEqualityGroup(_builder.setMask6(null).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setRouteAdvertisementMode(null).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    VirtualAddress obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
