package org.batfish.datamodel.vxlan;

import com.google.common.testing.EqualsTester;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class VxlanNodeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuilderMissingHostname() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanNode.builder().setVni(1).build();
  }

  @Test
  public void testBuilderMissingVni() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanNode.builder().setHostname("h").build();
  }

  @Test
  public void testEquals() {
    VxlanNode.Builder builder = VxlanNode.builder().setHostname("h").setVni(1);
    VxlanNode n = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(n, n, builder.build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setHostname("h2").build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setVni(5).build())
        .addEqualityGroup(builder.build().toString())
        .testEquals();
  }
}
