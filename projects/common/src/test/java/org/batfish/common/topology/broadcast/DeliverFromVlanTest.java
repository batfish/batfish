package org.batfish.common.topology.broadcast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L3Interface.Unit;
import org.junit.Test;

public class DeliverFromVlanTest {
  @Test
  public void testTraverse() {
    DeliverFromVlan testing = new DeliverFromVlan(5);
    assertThat(testing.traverse(5), equalTo(Optional.of(Unit.VALUE)));
    assertThat(testing.traverse(4), equalTo(Optional.empty()));
  }
}
