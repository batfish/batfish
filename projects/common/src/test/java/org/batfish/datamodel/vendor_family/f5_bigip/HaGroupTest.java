package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link HaGroup}. */
public final class HaGroupTest {

  private HaGroup.Builder _builder;

  @Before
  public void setup() {
    _builder =
        HaGroup.builder()
            .setActiveBonus(100)
            .setName("n")
            .addPool(HaGroupPool.builder().setName("n").build())
            .addTrunk(HaGroupTrunk.builder().setName("n").build());
  }

  @Test
  public void testEquals() {
    HaGroup obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setActiveBonus(null).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setPools(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setTrunks(ImmutableMap.of()).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    HaGroup obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
