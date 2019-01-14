package org.batfish.datamodel.vxlan;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class VxlanEdgeTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final VxlanNode NODE =
      VxlanNode.builder().setHostname("h").setSourceAddress(Ip.ZERO).setVlan(1).setVrf("v").build();
  private static final VxlanNode NODE2 =
      VxlanNode.builder()
          .setHostname("h2")
          .setSourceAddress(Ip.ZERO)
          .setVlan(1)
          .setVrf("v")
          .build();
  private static final int UDP_PORT = 5555;
  private static final int VNI = 2;

  @Test
  public void testBuilderMissingTail() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanEdge.builder().setHead(NODE).setUdpPort(UDP_PORT).setVni(VNI).build();
  }

  @Test
  public void testBuilderMissingHead() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanEdge.builder().setTail(NODE).setUdpPort(UDP_PORT).setVni(VNI).build();
  }

  @Test
  public void testBuilderMissingUdpPort() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanEdge.builder().setHead(NODE).setTail(NODE).setVni(VNI).build();
  }

  @Test
  public void testBuilderMissingVni() {
    _thrown.expect(IllegalArgumentException.class);
    VxlanEdge.builder().setHead(NODE).setTail(NODE).setUdpPort(UDP_PORT).build();
  }

  @Test
  public void testEquals() {
    VxlanEdge.Builder builder =
        VxlanEdge.builder().setHead(NODE).setTail(NODE).setUdpPort(UDP_PORT).setVni(VNI);
    VxlanEdge e = builder.build();

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(e, e, builder.build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setHead(NODE2).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setTail(NODE2).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setUdpPort(UDP_PORT + 1).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setVni(VNI + 1).build())
        .addEqualityGroup(builder.build().toString())
        .addEqualityGroup(builder.setMulticastGroup(Ip.ZERO).build())
        .addEqualityGroup(builder.build().toString())
        .testEquals();
  }
}
