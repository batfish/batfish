package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link F5BigipFamily}. */
public final class F5BigipFamilyTest {

  private F5BigipFamily.Builder _builder;

  @Before
  public void setup() {
    _builder =
        F5BigipFamily.builder()
            .addDevice(Device.builder().setName("n").build())
            .addDeviceGroup(DeviceGroup.builder().setName("n").build())
            .addHaGroup(HaGroup.builder().setName("n").build())
            .addPool(Pool.builder().setName("n").build())
            .addTrafficGroup(TrafficGroup.builder().setName("n").build())
            .addVirtualAddress(VirtualAddress.builder().setName("n").build())
            .addVirtual(Virtual.builder().setName("n").build());
  }

  @Test
  public void testEquals() {
    F5BigipFamily obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setDevices(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setDeviceGroups(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setHaGroups(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setPools(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setTrafficGroups(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setVirtualAddresses(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setVirtuals(ImmutableMap.of()).build())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    F5BigipFamily obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
