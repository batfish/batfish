package org.batfish.datamodel.vxlan;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.vxlan.Layer3Vni.Builder;
import org.junit.Test;

/** Tests of {@link Layer3Vni} */
public class Layer3VniTest {
  @Test
  public void testEquals() {
    Builder builder =
        Layer3Vni.builder()
            .setSourceAddress(Ip.parse("1.1.1.1"))
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setUdpPort(4096)
            .setVni(100001)
            .setLearnedNexthopVtepIps(ImmutableSet.of());
    Layer3Vni vni = builder.build();
    new EqualsTester()
        .addEqualityGroup(vni, vni, builder.build())
        .addEqualityGroup(builder.setSourceAddress(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(builder.setSrcVrf("vrf1").build())
        .addEqualityGroup(builder.setUdpPort(4444).build())
        .addEqualityGroup(builder.setVni(200000).build())
        .addEqualityGroup(
            builder.setLearnedNexthopVtepIps(ImmutableSet.of(Ip.parse("3.3.3.3"))).build())
        .testEquals();
  }
}
