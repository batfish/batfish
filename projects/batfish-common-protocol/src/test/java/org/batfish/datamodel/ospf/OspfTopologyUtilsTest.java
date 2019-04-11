package org.batfish.datamodel.ospf;

import static org.batfish.datamodel.ospf.OspfTopologyUtils.trimLinks;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;
import org.junit.Test;

/** Test of {@link org.batfish.datamodel.ospf.OspfTopologyUtils} */
public class OspfTopologyUtilsTest {

  @Test
  public void testTrimLinks() {
    // Setup
    MutableValueGraph<OspfNeighborConfigId, OspfSessionProperties> graph =
        ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    OspfNeighborConfigId n1 = new OspfNeighborConfigId("h1", "v", "p", "i");
    OspfNeighborConfigId n2 = new OspfNeighborConfigId("h2", "v", "p", "i");
    OspfNeighborConfigId n3 = new OspfNeighborConfigId("h3", "v", "p", "i");
    graph.addNode(n1);
    graph.addNode(n2);
    graph.addNode(n3);
    // n1 <--> n2. Link values (IPs) don't matter
    Ip ip = Ip.parse("1.1.1.1");
    OspfSessionProperties s = new OspfSessionProperties(0, new IpLink(ip, ip));
    graph.putEdgeValue(n1, n2, s);
    graph.putEdgeValue(n2, n1, s);

    // n1  --> n3
    graph.putEdgeValue(n1, n3, s);

    // Test: resulting edges should only be n1 <--> n2
    trimLinks(graph);

    assertThat(
        graph.edges(),
        equalTo(ImmutableSet.of(EndpointPair.ordered(n1, n2), EndpointPair.ordered(n2, n1))));
  }
}
