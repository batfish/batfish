package org.batfish.common.topology.broadcast;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L3Interface.Unit;

/** An L3 physical sub/interface puts un/tagged packets directly on the wire. */
public final class OriginateWithTag implements Edge<Unit, EthernetTag> {
  public OriginateWithTag(EthernetTag tag) {
    _tag = tag;
  }

  @Override
  public Optional<EthernetTag> traverse(Unit data) {
    return Optional.of(_tag);
  }

  private final EthernetTag _tag;
}
