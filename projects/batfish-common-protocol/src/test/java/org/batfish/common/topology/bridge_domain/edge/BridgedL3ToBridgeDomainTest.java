package org.batfish.common.topology.bridge_domain.edge;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;
import org.junit.Test;

public class BridgedL3ToBridgeDomainTest {
  @Test
  public void testOriginateInVlan() {
    BridgedL3ToBridgeDomain testing = new BridgedL3ToBridgeDomain(5);
    assertThat(testing.traverse(Unit.VALUE), equalTo(Optional.of(5)));
  }
}
