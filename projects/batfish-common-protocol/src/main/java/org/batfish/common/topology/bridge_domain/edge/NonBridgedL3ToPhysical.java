package org.batfish.common.topology.bridge_domain.edge;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.EthernetTag;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;

/** An L3 physical sub/interface puts un/tagged packets directly on the wire. */
public final class NonBridgedL3ToPhysical implements Edge<Unit, EthernetTag> {
  public NonBridgedL3ToPhysical(EthernetTag tag) {
    _tag = tag;
  }

  @Override
  public Optional<EthernetTag> traverse(Unit data) {
    return Optional.of(_tag);
  }

  private final EthernetTag _tag;
}
