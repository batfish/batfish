package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2VniToVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L3ToVlanAwareBridgeDomain;

/** A {@link StateFunction} that sets a specific VLAN ID in the state. */
public final class SetVlanId
    implements L2ToVlanAwareBridgeDomain.Function,
        L3ToVlanAwareBridgeDomain.Function,
        L2VniToVlanAwareBridgeDomain.Function {

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

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof SetVlanId)) {
      return false;
    }
    SetVlanId setVlanId = (SetVlanId) o;
    return _vlanId == setVlanId._vlanId;
  }

  @Override
  public int hashCode() {
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
