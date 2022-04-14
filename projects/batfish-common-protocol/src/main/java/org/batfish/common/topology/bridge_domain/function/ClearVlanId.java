package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToL1;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2Vni;

/** A {@link StateFunction} that clears the VLAN ID of the state. */
public final class ClearVlanId implements L2ToL1.Function, VlanAwareBridgeDomainToL2Vni.Function {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitClearVlanId(this, arg);
  }

  static @Nonnull ClearVlanId instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof ClearVlanId;
  }

  @Override
  public int hashCode() {
    return 0xE830ECD1; // randomly generated
  }

  private static final ClearVlanId INSTANCE = new ClearVlanId();

  private ClearVlanId() {}
}
