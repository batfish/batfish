package org.batfish.representation.cisco;

import org.batfish.datamodel.Prefix;

public abstract class LeafBgpPeerGroup extends BgpPeerGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract Prefix getNeighborPrefix();

}
