package org.batfish.common.topology.bridge_domain.edge;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.EthernetTag;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;
import org.junit.Test;

public class NonBridgedL3ToPhysicalTest {
  @Test
  public void testOriginateWithTag() {
    NonBridgedL3ToPhysical testing = new NonBridgedL3ToPhysical(EthernetTag.untagged());
    assertThat(testing.traverse(Unit.VALUE), equalTo(Optional.of(EthernetTag.untagged())));
  }
}
