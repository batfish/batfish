package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Argument {@code all} to {@code bridge-domain vlan-id}.
 *
 * <p>Not compatible with {@code routing-interface}.
 */
public final class BridgeDomainVlanIdAll implements BridgeDomainVlanId {

  public static @Nonnull BridgeDomainVlanIdAll instance() {
    return INSTANCE;
  }

  @Override
  public void accept(BridgeDomainVlanIdVoidVisitor visitor) {
    visitor.visitBridgeDomainVlanIdAll();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof BridgeDomainVlanIdAll;
  }

  @Override
  public int hashCode() {
    return 0x46956A49; // randomly generated
  }

  private BridgeDomainVlanIdAll() {}

  private static final BridgeDomainVlanIdAll INSTANCE = new BridgeDomainVlanIdAll();
}
