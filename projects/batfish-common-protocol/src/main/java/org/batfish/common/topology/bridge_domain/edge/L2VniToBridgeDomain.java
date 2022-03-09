package org.batfish.common.topology.bridge_domain.edge;

import java.util.Optional;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L2Vni.Unit;

/** Models {@link BridgeDomain} and {@link L2Vni} connections */
public final class L2VniToBridgeDomain {

  /** Returns the VLAN that the {@link L2Vni} is attachd to. */
  public Optional<Integer> receiveFromVxlan(L2Vni.Unit unit) {
    return Optional.of(_vlan);
  }

  /** Returns {@link L2Vni.Unit} if this vlan is connected to the given {@link L2Vni}. */
  public Optional<L2Vni.Unit> sendToVxlan(int vlan) {
    if (vlan == _vlan) {
      return Optional.of(Unit.VALUE);
    }
    return Optional.empty();
  }

  public L2VniToBridgeDomain(int vlan) {
    _vlan = vlan;
  }

  private final int _vlan;
}
