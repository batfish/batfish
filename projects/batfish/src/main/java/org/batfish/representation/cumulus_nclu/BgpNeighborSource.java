package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;

/** Class modeling source set in bgp `update-source` */
public interface BgpNeighborSource extends Serializable {
  <T> T accept(BgpNeighborSourceVisitor<T> visitor);
}
