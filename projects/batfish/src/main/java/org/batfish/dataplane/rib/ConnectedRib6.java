package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConnectedRoute6;

/** RIB for storing directly connected IPv6 routes. */
@ParametersAreNonnullByDefault
public final class ConnectedRib6
    extends AbstractRib6<ConnectedRoute6> {

  @Override
  public int comparePreference(
      ConnectedRoute6 lhs, ConnectedRoute6 rhs) {
    return 0;
  }
}
