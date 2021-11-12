package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** Passive BGP neighbor identified by a listen range prefix. */
public class BgpPassiveNeighbor extends BgpNeighbor {
  private final @Nonnull Prefix _listenRange;

  public BgpPassiveNeighbor(String name, Prefix listenRange) {
    super(name);
    _listenRange = listenRange;
  }

  public @Nonnull Prefix getListenRange() {
    return _listenRange;
  }
}
