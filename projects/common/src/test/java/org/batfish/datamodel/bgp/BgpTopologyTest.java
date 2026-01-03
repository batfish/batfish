package org.batfish.datamodel.bgp;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.BgpTopology.EdgeId;
import org.junit.Test;

/** Test of {@link BgpTopology}. */
@ParametersAreNonnullByDefault
public final class BgpTopologyTest {

  private static @Nonnull BgpTopology nonTrivialTopology() {
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    graph.putEdgeValue(
        new BgpPeerConfigId("a", "b", "c"),
        new BgpPeerConfigId("d", "e", "f"),
        BgpSessionProperties.builder()
            .setRemoteAs(1L)
            .setLocalAs(2L)
            .setRemoteIp(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setLocalIp(Ip.FIRST_CLASS_B_PRIVATE_IP)
            .setSessionType(SessionType.EBGP_SINGLEHOP)
            .build());
    return new BgpTopology(graph);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            BgpTopology.EMPTY,
            BgpTopology.EMPTY,
            new BgpTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build()))
        .addEqualityGroup(nonTrivialTopology())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    assertEquals(
        nonTrivialTopology(), BatfishObjectMapper.clone(nonTrivialTopology(), BgpTopology.class));
  }

  @Test
  public void testEdgeIdEquality() {
    BgpPeerConfigId c1 = new BgpPeerConfigId("h", "vrf", "iface1");
    BgpPeerConfigId c2 = new BgpPeerConfigId("h", "vrf", "iface2");
    EdgeId edge = new EdgeId(c1, c2);
    new EqualsTester()
        .addEqualityGroup(edge, edge, new EdgeId(c1, c2))
        .addEqualityGroup(new EdgeId(c2, c1))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
