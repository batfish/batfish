package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link UnicastAddress}. */
public final class UnicastAddressTest {

  @Before
  public void setup() {
    _builder =
        UnicastAddress.builder()
            .setEffectiveIp(ManagementIp.instance())
            .setEffectivePort(1234)
            .setIp(ManagementIp.instance())
            .setPort(1234);
  }

  private UnicastAddress.Builder _builder;

  @Test
  public void testEquals() {
    UnicastAddress obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setEffectiveIp(null).build())
        .addEqualityGroup(_builder.setEffectivePort(1).build())
        .addEqualityGroup(_builder.setIp(null).build())
        .addEqualityGroup(_builder.setPort(null).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    UnicastAddress obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
