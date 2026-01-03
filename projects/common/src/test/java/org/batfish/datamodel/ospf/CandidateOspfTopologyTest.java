package org.batfish.datamodel.ospf;

import static org.batfish.datamodel.ospf.CandidateOspfTopology.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

/** Tests of {@link CandidateOspfTopology} */
public class CandidateOspfTopologyTest {

  private static OspfNeighborConfigId NODE_A =
      new OspfNeighborConfigId("a", "b", "c", "d", ConcreteInterfaceAddress.parse("192.0.2.0/31"));
  private static OspfNeighborConfigId NODE_E =
      new OspfNeighborConfigId("e", "f", "g", "h", ConcreteInterfaceAddress.parse("192.0.2.1/31"));
  private static OspfNeighborConfigId NODE_I =
      new OspfNeighborConfigId("i", "j", "k", "l", ConcreteInterfaceAddress.parse("192.0.2.4/31"));
  private static OspfNeighborConfigId NODE_M =
      new OspfNeighborConfigId("m", "n", "o", "p", ConcreteInterfaceAddress.parse("192.0.2.6/31"));

  private static OspfSessionStatus NODE_A_TO_E_STATUS = OspfSessionStatus.ESTABLISHED;
  private static OspfSessionStatus NODE_I_TO_M_STATUS = OspfSessionStatus.NETWORK_TYPE_MISMATCH;

  private @Nonnull CandidateOspfTopology nonTrivialTopology() {
    MutableValueGraph<OspfNeighborConfigId, OspfSessionStatus> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    graph.putEdgeValue(NODE_A, NODE_E, NODE_A_TO_E_STATUS);
    graph.putEdgeValue(NODE_I, NODE_M, NODE_I_TO_M_STATUS);
    return new CandidateOspfTopology(graph);
  }

  @Test
  public void testEmptyTopology() {
    CandidateOspfTopology topo = EMPTY;
    assertThat(topo.getGraph().nodes(), empty());
  }

  @Test
  public void testGetNeigbors() {
    assertThat(nonTrivialTopology().neighbors(NODE_A), contains(NODE_E));
  }

  @Test
  public void testGetNeighborsNonExistentNode() {
    OspfNeighborConfigId n =
        new OspfNeighborConfigId(
            "h1", "vrf1", "p", "i1", ConcreteInterfaceAddress.parse("192.0.2.8/31"));
    assertThat(nonTrivialTopology().neighbors(n), empty());
  }

  @Test
  public void testGetSessionStatus() {
    // Node A and E represent a valid edge, so their edge's status should be retrieved
    assertThat(
        nonTrivialTopology().getSessionStatus(NODE_A, NODE_E).orElse(null),
        equalTo(NODE_A_TO_E_STATUS));
  }

  @Test
  public void testGetSessionStatusNonExistentEdge() {
    // Node A and I do NOT represent a valid edge, so their edge's status should be empty
    assertThat(nonTrivialTopology().getSessionStatus(NODE_A, NODE_I), equalTo(Optional.empty()));
  }
}
