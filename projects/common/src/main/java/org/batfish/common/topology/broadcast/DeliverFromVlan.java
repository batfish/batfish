package org.batfish.common.topology.broadcast;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L3Interface.Unit;

/** Only switched packets in a specific vlan are delivered. */
public final class DeliverFromVlan implements Edge<Integer, Unit> {
  public DeliverFromVlan(int vlan) {
    _vlan = vlan;
  }

  @Override
  public Optional<Unit> traverse(Integer data) {
    if (_vlan == data) {
      return Optional.of(Unit.VALUE);
    }
    return Optional.empty();
  }

  private final int _vlan;
}
