package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link HaGroupPool}. */
public final class HaGroupPoolTest {

  private HaGroupPool.Builder _builder;

  @Before
  public void setup() {
    _builder = HaGroupPool.builder().setName("n").setWeight(1);
  }

  @Test
  public void testEquals() {
    HaGroupPool obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setWeight(null).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    HaGroupPool obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
