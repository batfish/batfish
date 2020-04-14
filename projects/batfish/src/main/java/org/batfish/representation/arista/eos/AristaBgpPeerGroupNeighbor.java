package org.batfish.representation.arista.eos;

import javax.annotation.Nonnull;

public final class AristaBgpPeerGroupNeighbor extends AristaBgpNeighbor {
  @Nonnull private final String _name;

  public AristaBgpPeerGroupNeighbor(String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
  }
}
