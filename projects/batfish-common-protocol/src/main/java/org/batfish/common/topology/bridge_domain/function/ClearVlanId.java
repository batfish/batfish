package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2Vni;
import org.batfish.common.topology.bridge_domain.edge.L2ToPhysical;

/** A {@link StateFunction} that clears the VLAN ID of the state. */
public final class ClearVlanId implements L2ToPhysical.Function, BridgeDomainToL2Vni.Function {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitClearVlanId(this, arg);
  }

  static @Nonnull ClearVlanId instance() {
    return INSTANCE;
  }

  private static final ClearVlanId INSTANCE = new ClearVlanId();

  private ClearVlanId() {}
}
