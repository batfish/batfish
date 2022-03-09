package org.batfish.common.topology.bridge_domain.edge;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.EthernetTag;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;

/** Only received packets with a specific tag are delivered. */
public final class PhysicalToNonBridgedL3 implements Edge<EthernetTag, Unit> {
  public PhysicalToNonBridgedL3(EthernetTag tag) {
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
