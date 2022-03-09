package org.batfish.common.topology.bridge_domain.edge;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;

/** Only switched packets in a specific vlan are delivered. */
public final class BridgeDomainToBridgedL3 implements Edge<Integer, Unit> {
  public BridgeDomainToBridgedL3(int vlan) {
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
