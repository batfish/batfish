package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A reference to a named {@link Vlan}. */
@ParametersAreNonnullByDefault
public final class VlanReference implements VlanMember {

  public VlanReference(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public @Nonnull String toString() {
    return _name;
  }

  private final @Nonnull String _name;
}
