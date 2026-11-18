package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of {@link UnicastAddress}. */
final class UnicastAddressTest {

  @BeforeEach
  void setup() {
    _builder =
        UnicastAddress.builder()
            .setEffectiveIp(ManagementIp.instance())
            .setEffectivePort(1234)
            .setIp(ManagementIp.instance())
            .setPort(1234);
  }

  private UnicastAddress.Builder _builder;

  @Test
  void testEquals() {
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
  void testJavaSerialization() {
    UnicastAddress obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
