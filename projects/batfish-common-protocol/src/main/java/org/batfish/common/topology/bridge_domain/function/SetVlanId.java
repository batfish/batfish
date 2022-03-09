package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.BridgedL3ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2VniToBridgeDomain;

/** A {@link StateFunction} that sets a specific VLAN ID in the state. */
public final class SetVlanId
    implements L2ToBridgeDomain.Function,
        BridgedL3ToBridgeDomain.Function,
        L2VniToBridgeDomain.Function {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitSetVlanId(this, arg);
  }

  /**
   * The VLAN ID to set.
   *
   * <p>The result is undefined if the VLAN ID is already set in the state.
   */
  public int getVlanId() {
    return _vlanId;
  }

  static @Nonnull SetVlanId of(int vlanId) {
    return new SetVlanId(vlanId);
  }

  private SetVlanId(int vlanId) {
    _vlanId = vlanId;
  }

  private final int _vlanId;
}
