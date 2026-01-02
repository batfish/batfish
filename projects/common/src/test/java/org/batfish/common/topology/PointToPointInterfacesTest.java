package org.batfish.common.topology;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.util.Map;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class PointToPointInterfacesTest {

  @Test
  public void testPointToPointInterfaces() {
    NodeInterfacePair e1 = NodeInterfacePair.of("a", "ethernet1");
    NodeInterfacePair e1_2 = NodeInterfacePair.of("a", "ethernet1.2");
    NodeInterfacePair e1_3 = NodeInterfacePair.of("a", "ethernet1.3");
    NodeInterfacePair e2 = NodeInterfacePair.of("b", "ethernet2");
    NodeInterfacePair e2_4 = NodeInterfacePair.of("b", "ethernet2.4");
    NodeInterfacePair c = NodeInterfacePair.of("c", "c");
    Map<NodeInterfacePair, NodeInterfacePair> ifToParent =
        ImmutableMap.<NodeInterfacePair, NodeInterfacePair>builder()
            .put(e1, e1)
            .put(e1_2, e1)
            .put(e1_3, e1)
            .put(e2, e2)
            .put(e2_4, e2)
            .put(c, c)
            .build();
    PointToPointInterfaces p2p =
        PointToPointInterfaces.createForTesting(ImmutableMap.of(e1, e2, e2, e1), ifToParent);
    assertThat(p2p.pointToPointInterfaces(e1), containsInAnyOrder(e2, e2_4));
    assertThat(p2p.pointToPointInterfaces(e1_2), containsInAnyOrder(e2, e2_4));
    assertThat(p2p.pointToPointInterfaces(e1_3), containsInAnyOrder(e2, e2_4));
    assertThat(p2p.pointToPointInterfaces(e2), containsInAnyOrder(e1, e1_2, e1_3));
    assertThat(p2p.pointToPointInterfaces(c), empty());
    assertThat(p2p.pointToPointInterfaces(NodeInterfacePair.of("d", "d")), empty());
  }

  @Test
  public void testEquals() {
    NodeInterfacePair a = NodeInterfacePair.of("a", "a");
    NodeInterfacePair b = NodeInterfacePair.of("b", "b");
    new EqualsTester()
        .addEqualityGroup(
            PointToPointInterfaces.createForTesting(emptyMap(), emptyMap()),
            PointToPointInterfaces.createForTesting(emptyMap(), emptyMap()))
        .addEqualityGroup(
            PointToPointInterfaces.createForTesting(ImmutableMap.of(a, b, b, a), emptyMap()))
        .addEqualityGroup(
            PointToPointInterfaces.createForTesting(emptyMap(), ImmutableMap.of(a, b, b, a)))
        .testEquals();
  }
}
