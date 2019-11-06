package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link DeviceGroupDevice}. */
public final class DeviceGroupDeviceTest {

  private DeviceGroupDevice.Builder _builder;

  @Before
  public void setup() {
    _builder = DeviceGroupDevice.builder().setName("n").setSetSyncLeader(true);
  }

  @Test
  public void testEquals() {
    DeviceGroupDevice obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setSetSyncLeader(false).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    DeviceGroupDevice obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
