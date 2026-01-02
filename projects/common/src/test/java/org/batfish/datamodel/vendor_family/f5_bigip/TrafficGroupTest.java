package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.MacAddress;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link TrafficGroup}. */
public final class TrafficGroupTest {

  private TrafficGroup.Builder _builder;

  @Before
  public void setup() {
    _builder =
        TrafficGroup.builder()
            .setHaGroup("h")
            .setMac(MacAddress.parse("00:00:00:00:00:01"))
            .setName("n")
            .setUnitId(1);
  }

  @Test
  public void testEquals() {
    TrafficGroup obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setHaGroup(null).build())
        .addEqualityGroup(_builder.setMac(null).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setUnitId(null).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    TrafficGroup obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
