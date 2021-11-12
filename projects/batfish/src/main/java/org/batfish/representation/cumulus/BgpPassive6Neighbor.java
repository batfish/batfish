package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix6;

/** Passive BGP neighbor identified by a IPv6 listen range prefix. */
public class BgpPassive6Neighbor extends BgpNeighbor {
  private final @Nonnull Prefix6 _listenRange;

  public BgpPassive6Neighbor(String name, Prefix6 listenRange) {
    super(name);
    _listenRange = listenRange;
  }

  public @Nonnull Prefix6 getListenRange() {
    return _listenRange;
  }
}
