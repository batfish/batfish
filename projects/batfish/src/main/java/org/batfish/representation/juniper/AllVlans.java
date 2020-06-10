package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A vlan member representing the full range of vlans */
@ParametersAreNonnullByDefault
public final class AllVlans implements VlanMember {

  public static @Nonnull AllVlans instance() {
    return INSTANCE;
  }

  @Override
  public @Nonnull String toString() {
    return "all";
  }

  private static final AllVlans INSTANCE = new AllVlans();

  private AllVlans() {}
}
