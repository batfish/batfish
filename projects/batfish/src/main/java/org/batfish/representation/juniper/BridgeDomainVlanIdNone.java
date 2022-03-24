package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Argument {@code none} to {@code bridge-domain vlan-id}. */
public final class BridgeDomainVlanIdNone implements BridgeDomainVlanId {

  public static @Nonnull BridgeDomainVlanIdNone instance() {
    return INSTANCE;
  }

  @Override
  public void accept(BridgeDomainVlanIdVoidVisitor visitor) {
    visitor.visitBridgeDomainVlanIdNone();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof BridgeDomainVlanIdNone;
  }

  @Override
  public int hashCode() {
    return 0x3D0B6E14; // randomly generated
  }

  private BridgeDomainVlanIdNone() {}

  private static final BridgeDomainVlanIdNone INSTANCE = new BridgeDomainVlanIdNone();
}
