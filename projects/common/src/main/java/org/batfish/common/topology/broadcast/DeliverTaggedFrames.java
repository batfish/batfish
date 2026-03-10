package org.batfish.common.topology.broadcast;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.topology.broadcast.L3Interface.Unit;

/** Only received packets with a specific tag are delivered. */
public final class DeliverTaggedFrames implements Edge<EthernetTag, Unit> {
  public DeliverTaggedFrames(EthernetTag tag) {
    _tag = tag;
  }

  @Override
  public Optional<Unit> traverse(EthernetTag data) {
    if (data.equals(_tag)) {
      return Optional.of(Unit.VALUE);
    }
    return Optional.empty();
  }

  private final @Nonnull EthernetTag _tag;
}
