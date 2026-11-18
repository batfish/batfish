package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.vxlan.VniLayer.LAYER_2;
import static org.batfish.datamodel.vxlan.VniLayer.LAYER_3;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

final class VxlanNodeTest {

  @Test
  void testBuilderMissingHostname() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VxlanNode.builder().setVni(1).setVniLayer(LAYER_2).build());
  }

  @Test
  void testBuilderMissingVni() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VxlanNode.builder().setHostname("h").setVniLayer(LAYER_2).build());
  }

  @Test
  void testBuilderMissingVniLayer() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VxlanNode.builder().setHostname("h").setVni(1).build());
  }

  @Test
  void testEquals() {
    VxlanNode.Builder builder = VxlanNode.builder().setHostname("h").setVni(1).setVniLayer(LAYER_2);
    VxlanNode n = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(n, n, builder.build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setHostname("h2").build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setVni(5).build())
        .addEqualityGroup(builder.setVniLayer(LAYER_3).build())
        .addEqualityGroup(builder.build().toString())
        .testEquals();
  }
}
