package org.batfish.representation.cumulus_interfaces;

import javax.annotation.Nonnull;

/** Model of an iface block in a cumulus /etc/network/interfaces file. */
public final class Interface {
  private final @Nonnull String _name;

  public Interface(@Nonnull String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
  }
}
