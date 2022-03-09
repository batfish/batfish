package org.batfish.common.topology.bridge_domain.edge;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.EthernetTag;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;
import org.junit.Test;

public class PhysicalToNonBridgedL3Test {
  @Test
  public void testTraverse() {
    PhysicalToNonBridgedL3 untagged = new PhysicalToNonBridgedL3(EthernetTag.untagged());
    assertThat(untagged.traverse(EthernetTag.untagged()), equalTo(Optional.of(Unit.VALUE)));
    assertThat(untagged.traverse(EthernetTag.tagged(5)), equalTo(Optional.empty()));

    PhysicalToNonBridgedL3 tagged = new PhysicalToNonBridgedL3(EthernetTag.tagged(5));
    assertThat(tagged.traverse(EthernetTag.tagged(5)), equalTo(Optional.of(Unit.VALUE)));
    assertThat(tagged.traverse(EthernetTag.untagged()), equalTo(Optional.empty()));
  }
}
