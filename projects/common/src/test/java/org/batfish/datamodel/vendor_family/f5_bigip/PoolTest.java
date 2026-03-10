package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link Pool}. */
public final class PoolTest {

  private Pool.Builder _builder;

  @Before
  public void setup() {
    _builder =
        Pool.builder()
            .setDescription("d")
            .addMember(PoolMember.builder().setName("n").setNode("n").setPort(1).build())
            .addMonitor("n")
            .setName("n");
  }

  @Test
  public void testEquals() {
    Pool obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setDescription(null).build())
        .addEqualityGroup(_builder.setMembers(ImmutableMap.of()).build())
        .addEqualityGroup(_builder.setMonitors(ImmutableList.of()).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    Pool obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
