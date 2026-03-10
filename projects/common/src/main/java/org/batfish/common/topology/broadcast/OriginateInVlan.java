package org.batfish.common.topology.broadcast;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L3Interface.Unit;

/** An IRB or SVI L3 interface originates in the corresponding VLAN on the device's switch. */
public final class OriginateInVlan implements Edge<Unit, Integer> {
  public OriginateInVlan(int vlan) {
    _vlan = vlan;
  }

  @Override
  public Optional<Integer> traverse(Unit data) {
    return Optional.of(_vlan);
  }

  private final int _vlan;
}
