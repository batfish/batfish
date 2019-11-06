package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link Device}. */
public final class DeviceTest {

  private Device.Builder _builder;

  @Before
  public void setup() {
    _builder =
        Device.builder()
            .setBaseMac(MacAddress.parse("00:00:00:00:00:01"))
            .setConfigSyncIp(Ip.ZERO)
            .setHostname("h")
            .setManagementIp(Ip.ZERO)
            .setName("n")
            .setSelfDevice(true)
            .addUnicastAddress(UnicastAddress.builder().build());
  }

  @Test
  public void testEquals() {
    Device obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setBaseMac(null).build())
        .addEqualityGroup(_builder.setConfigSyncIp(null).build())
        .addEqualityGroup(_builder.setHostname(null).build())
        .addEqualityGroup(_builder.setManagementIp(null).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setSelfDevice(null).build())
        .addEqualityGroup(_builder.setUnicastAddresses(ImmutableList.of()).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Device obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
