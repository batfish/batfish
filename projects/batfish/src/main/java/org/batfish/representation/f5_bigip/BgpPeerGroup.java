package org.batfish.representation.f5_bigip;

import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a BGP neighbor peer-group. */
@ParametersAreNonnullByDefault
public final class BgpPeerGroup extends AbstractBgpNeighbor {

  public BgpPeerGroup(String name) {
    super(name);
  }
}
