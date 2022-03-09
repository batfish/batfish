package org.batfish.common.topology.bridge_domain;

import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.bridge_domain.node.EthernetHub;
import org.junit.Test;

public class NodeAndStateTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new NodeAndState<>(new EthernetHub("1"), EthernetTag.untagged()),
            new NodeAndState<>(new EthernetHub("1"), EthernetTag.untagged()))
        .addEqualityGroup(new NodeAndState<>(new EthernetHub("2"), EthernetTag.untagged()))
        .addEqualityGroup(new NodeAndState<>(new EthernetHub("2"), EthernetTag.tagged(3)))
        .testEquals();
  }
}
