package org.batfish.common.topology.broadcast;

import java.util.Optional;
import org.batfish.common.topology.broadcast.L2VNI.Unit;

/** Models {@link DeviceBroadcastDomain} and {@link L2VNI} connections */
public final class L2VniToVlan {

  /** Returns the VLAN that the {@link L2VNI} is attachd to. */
  Optional<Integer> receiveFromVxlan(L2VNI.Unit unit) {
    return Optional.of(_vlan);
  }

  /** Returns {@link L2VNI.Unit} if this vlan is connected to the given {@link L2VNI}. */
  Optional<L2VNI.Unit> sendToVxlan(int vlan) {
    if (vlan == _vlan) {
      return Optional.of(Unit.VALUE);
    }
    return Optional.empty();
  }

  public L2VniToVlan(int vlan) {
    _vlan = vlan;
  }

  private final int _vlan;
}
