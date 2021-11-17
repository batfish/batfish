package org.batfish.representation.frr;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix6;

/** Passive BGP neighbor identified by a IPv6 listen range prefix. */
public class BgpDynamic6Neighbor extends BgpNeighbor {
  private final @Nonnull Prefix6 _listenRange;

  public BgpDynamic6Neighbor(String name, Prefix6 listenRange) {
    super(name);
    _listenRange = listenRange;
  }

  public @Nonnull Prefix6 getListenRange() {
    return _listenRange;
  }
}
