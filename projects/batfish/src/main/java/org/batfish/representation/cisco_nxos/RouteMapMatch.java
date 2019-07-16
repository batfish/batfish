package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

/** A match condition of a {@link RouteMapEntry}. */
public interface RouteMapMatch extends Serializable {

  <T> T accept(RouteMapMatchVisitor<T> visitor);
}
