package org.batfish.common.topology.broadcast;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L3Interface.Unit;
import org.junit.Test;

public class OriginateInVlanTest {
  @Test
  public void testOriginateInVlan() {
    OriginateInVlan testing = new OriginateInVlan(5);
    assertThat(testing.traverse(Unit.VALUE), equalTo(Optional.of(5)));
  }
}
