package org.batfish.common.topology.broadcast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L3Interface.Unit;
import org.junit.Test;

public class DeliverTaggedFramesTest {
  @Test
  public void testTraverse() {
    DeliverTaggedFrames untagged = new DeliverTaggedFrames(EthernetTag.untagged());
    assertThat(untagged.traverse(EthernetTag.untagged()), equalTo(Optional.of(Unit.VALUE)));
    assertThat(untagged.traverse(EthernetTag.tagged(5)), equalTo(Optional.empty()));

    DeliverTaggedFrames tagged = new DeliverTaggedFrames(EthernetTag.tagged(5));
    assertThat(tagged.traverse(EthernetTag.tagged(5)), equalTo(Optional.of(Unit.VALUE)));
    assertThat(tagged.traverse(EthernetTag.untagged()), equalTo(Optional.empty()));
  }
}
