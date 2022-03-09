package org.batfish.common.topology.bridge_domain.edge;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;
import org.junit.Test;

public class BridgeDomainToBridgedL3Test {
  @Test
  public void testTraverse() {
    BridgeDomainToBridgedL3 testing = new BridgeDomainToBridgedL3(5);
    assertThat(testing.traverse(5), equalTo(Optional.of(Unit.VALUE)));
    assertThat(testing.traverse(4), equalTo(Optional.empty()));
  }
}
