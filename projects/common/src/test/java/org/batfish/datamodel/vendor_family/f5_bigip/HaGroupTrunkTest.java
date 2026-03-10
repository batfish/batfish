package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link HaGroupTrunk}. */
public final class HaGroupTrunkTest {

  private HaGroupTrunk.Builder _builder;

  @Before
  public void setup() {
    _builder = HaGroupTrunk.builder().setName("n").setWeight(1);
  }

  @Test
  public void testEquals() {
    HaGroupTrunk obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setWeight(null).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    HaGroupTrunk obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
