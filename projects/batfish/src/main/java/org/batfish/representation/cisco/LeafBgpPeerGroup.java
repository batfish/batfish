package org.batfish.representation.cisco;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public abstract class LeafBgpPeerGroup extends BgpPeerGroup {

  private static final long serialVersionUID = 1L;

  public abstract Prefix getNeighborPrefix();

  public abstract Prefix6 getNeighborPrefix6();
}
