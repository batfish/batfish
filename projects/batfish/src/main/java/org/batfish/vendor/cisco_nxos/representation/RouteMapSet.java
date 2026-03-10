package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;

/** A set statement of a {@link RouteMapEntry}. */
public interface RouteMapSet extends Serializable {

  <T> T accept(RouteMapSetVisitor<T> visitor);
}
