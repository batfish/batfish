package org.batfish.representation.cisco;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public abstract class LeafBgpPeerGroup extends BgpPeerGroup {

  public abstract Prefix getNeighborPrefix();

  public abstract Prefix6 getNeighborPrefix6();
}
