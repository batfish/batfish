package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test of {@link HaGroupTrunk}. */
final class HaGroupTrunkTest {

  private HaGroupTrunk.Builder _builder;

  @BeforeEach
  void setup() {
    _builder = HaGroupTrunk.builder().setName("n").setWeight(1);
  }

  @Test
  void testEquals() {
    HaGroupTrunk obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setWeight(null).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    HaGroupTrunk obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
