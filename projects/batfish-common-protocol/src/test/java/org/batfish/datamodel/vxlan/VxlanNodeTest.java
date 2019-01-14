package org.batfish.datamodel.vxlan;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class VxlanNodeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuilderMissingHostname() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanNode.builder().setSourceAddress(Ip.ZERO).setVlan(1).setVrf("v").build();
  }

  @Test
  public void testBuilderMissingSourceAddress() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanNode.builder().setHostname("h").setVlan(1).setVrf("v").build();
  }

  @Test
  public void testBuilderMissingVlan() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanNode.builder().setHostname("h").setSourceAddress(Ip.ZERO).setVrf("v").build();
  }

  @Test
  public void testBuilderMissingVrf() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanNode.builder().setHostname("h").setSourceAddress(Ip.ZERO).setVlan(1).build();
  }

  @Test
  public void testEquals() {
    VxlanNode.Builder builder =
        VxlanNode.builder().setHostname("h").setSourceAddress(Ip.ZERO).setVlan(1).setVrf("v");
    VxlanNode n = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(n, n, builder.build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setHostname("h2").build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setSourceAddress(Ip.MAX).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setVlan(5).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setVrf("v2").build())
        .addEqualityGroup(builder.build().toString())
        .testEquals();
  }
}
