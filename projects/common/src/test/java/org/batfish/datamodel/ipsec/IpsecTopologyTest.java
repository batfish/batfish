package org.batfish.datamodel.ipsec;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.testing.EqualsTester;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.junit.Test;

/** Test of {@link IpsecTopology}. */
@ParametersAreNonnullByDefault
public final class IpsecTopologyTest {

  private static @Nonnull IpsecTopology nonTrivialTopology() {
    MutableValueGraph<IpsecPeerConfigId, IpsecSession> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    graph.putEdgeValue(
        new IpsecPeerConfigId("a", "b"),
        new IpsecPeerConfigId("d", "e"),
        IpsecSession.builder()
            .setNegotiatedIpsecP2Proposal(new IpsecPhase2Proposal())
            .setNegotiatedIkeP1Proposal(new IkePhase1Proposal("ike"))
            .build());
    return new IpsecTopology(graph);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            IpsecTopology.EMPTY,
            IpsecTopology.EMPTY,
            new IpsecTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build()))
        .addEqualityGroup(nonTrivialTopology())
        .testEquals();
  }
}
