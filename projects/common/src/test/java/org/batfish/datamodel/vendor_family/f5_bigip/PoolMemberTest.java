package org.batfish.datamodel.vendor_family.f5_bigip;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link PoolMember}. */
public final class PoolMemberTest {

  private PoolMember.Builder _builder;

  @Before
  public void setup() {
    _builder =
        PoolMember.builder()
            .setAddress(Ip.ZERO)
            .setAddress6(Ip6.ZERO)
            .setDescription("d")
            .setName("n")
            .setNode("n")
            .setPort(1);
  }

  @Test
  public void testEquals() {
    PoolMember obj = _builder.build();
    new EqualsTester()
        .addEqualityGroup(obj, obj, _builder.build())
        .addEqualityGroup(_builder.setAddress(null).build())
        .addEqualityGroup(_builder.setAddress6(null).build())
        .addEqualityGroup(_builder.setDescription(null).build())
        .addEqualityGroup(_builder.setName("n2").build())
        .addEqualityGroup(_builder.setNode("n2").build())
        .addEqualityGroup(_builder.setPort(2).build())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    PoolMember obj = _builder.build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }
}
