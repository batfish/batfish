package org.batfish.representation.f5_bigip;

import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a BGP neighbor peer-group. */
@ParametersAreNonnullByDefault
public final class BgpPeerGroup extends AbstractBgpNeighbor {

  private static final long serialVersionUID = 1L;

  public BgpPeerGroup(String name) {
    super(name);
  }
}
