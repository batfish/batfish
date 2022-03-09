package org.batfish.common.topology.bridge_domain.edge;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.node.L3Interface.Unit;

/** An IRB or SVI L3 interface originates in the corresponding VLAN on the device's switch. */
public final class BridgedL3ToBridgeDomain implements Edge<Unit, Integer> {
  public BridgedL3ToBridgeDomain(int vlan) {
    _vlan = vlan;
  }

  @Override
  public Optional<Integer> traverse(Unit data) {
    return Optional.of(_vlan);
  }

  private final int _vlan;
}
