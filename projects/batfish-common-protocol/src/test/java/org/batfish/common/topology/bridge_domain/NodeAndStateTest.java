package org.batfish.common.topology.bridge_domain;

import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.bridge_domain.node.EthernetHub;
import org.junit.Test;

/** Test of {@link NodeAndState}. */
public final class NodeAndStateTest {

  @Test
  public void testEquals() {
    NodeAndState obj = NodeAndState.of(new EthernetHub("a"), State.empty());
    new EqualsTester()
        .addEqualityGroup(obj, NodeAndState.of(new EthernetHub("a"), State.empty()))
        .addEqualityGroup(NodeAndState.of(new EthernetHub("b"), State.empty()))
        .addEqualityGroup(NodeAndState.of(new EthernetHub("a"), State.of(1, null)))
        .testEquals();
  }
}
