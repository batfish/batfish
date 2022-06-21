package org.batfish.common.topology.bridge_domain;

import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.bridge_domain.node.L1Hub;
import org.junit.Test;

/** Test of {@link NodeAndState}. */
public final class NodeAndStateTest {

  @Test
  public void testEquals() {
    NodeAndState obj = NodeAndState.of(new L1Hub("a"), State.empty());
    new EqualsTester()
        .addEqualityGroup(obj, NodeAndState.of(new L1Hub("a"), State.empty()))
        .addEqualityGroup(NodeAndState.of(new L1Hub("b"), State.empty()))
        .addEqualityGroup(NodeAndState.of(new L1Hub("a"), State.of(1, null)))
        .testEquals();
  }
}
