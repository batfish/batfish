package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of {@link DeviceGroup}. */
final class DeviceGroupTest {

  private DeviceGroup.Builder _builder;

  @BeforeEach
  void setup() {
    _builder =
        DeviceGroup.builder()
            .setAutoSync(true)
            .addDevice(DeviceGroupDevice.builder().setName("a").build())
            .setHidden(true)
            .setName("n")
            .setNetworkFailover(true)
            .setType(DeviceGroupType.SYNC_FAILOVER);
  }

  @Test
  void testEquals() {
    DeviceGroup obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setAutoSync(null).build())
        .addEqualityGroup(_builder.setDevices(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setHidden(null).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setNetworkFailover(null).build())
        .addEqualityGroup(_builder.setType(null).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    DeviceGroup obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
