package org.batfish.datamodel.bgp;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link BgpTopology}. */
@ParametersAreNonnullByDefault
public final class BgpTopologyTest {

  @Test
  public void testEquals() {
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    graph.addNode(new BgpPeerConfigId("a", "b", "c"));

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            BgpTopology.EMPTY,
            BgpTopology.EMPTY,
            new BgpTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build()))
        .addEqualityGroup(new BgpTopology(graph))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    MutableValueGraph<BgpPeerConfigId, BgpSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    BgpPeerConfigId n1 = new BgpPeerConfigId("a", "b", "c");
    BgpPeerConfigId n2 = new BgpPeerConfigId("d", "e", "f");
    BgpSessionProperties v =
        BgpSessionProperties.builder()
            .setAdditionalPaths(true)
            .setAdvertiseExternal(true)
            .setAdvertiseInactive(true)
            .setHeadIp(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setTailIp(Ip.FIRST_CLASS_B_PRIVATE_IP)
            .setSessionType(SessionType.EBGP_SINGLEHOP)
            .build();
    graph.addNode(n1);
    graph.addNode(n2);
    graph.putEdgeValue(n1, n2, v);
    BgpTopology topology = new BgpTopology(graph);

    assertEquals(topology, SerializationUtils.clone(topology));
  }
}
