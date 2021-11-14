package org.batfish.representation.frr;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** Passive BGP neighbor identified by a listen range prefix. */
public class BgpDynamicNeighbor extends BgpNeighbor {
  private final @Nonnull Prefix _listenRange;

  public BgpDynamicNeighbor(String name, Prefix listenRange) {
    super(name);
    _listenRange = listenRange;
  }

  public @Nonnull Prefix getListenRange() {
    return _listenRange;
  }
}
