package org.batfish.vendor.arista.representation.eos;

import javax.annotation.Nonnull;

public final class AristaBgpPeerGroupNeighbor extends AristaBgpNeighbor {
  private final @Nonnull String _name;

  public AristaBgpPeerGroupNeighbor(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
