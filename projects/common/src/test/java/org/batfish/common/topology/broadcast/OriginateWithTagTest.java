package org.batfish.common.topology.broadcast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L3Interface.Unit;
import org.junit.Test;

public class OriginateWithTagTest {
  @Test
  public void testOriginateWithTag() {
    OriginateWithTag testing = new OriginateWithTag(EthernetTag.untagged());
    assertThat(testing.traverse(Unit.VALUE), equalTo(Optional.of(EthernetTag.untagged())));
  }
}
