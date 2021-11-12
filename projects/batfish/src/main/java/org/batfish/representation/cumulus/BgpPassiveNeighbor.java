package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** BGP neighbor identified by an IPv4 peer address */
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
