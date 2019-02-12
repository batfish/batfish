package org.batfish.dataplane.rib;

import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;

/** Rib for storing {@link ConnectedRoute}s */
public class ConnectedRib extends AbstractRib<ConnectedRoute> {

  private static final long serialVersionUID = 1L;

  public ConnectedRib() {
    super(null, r -> r);
  }

  @Override
  public Prefix getNetwork(ConnectedRoute route) {
    return route.getNetwork();
  }

  @Override
  public int comparePreference(ConnectedRoute lhs, ConnectedRoute rhs) {
    return 0;
  }
}
