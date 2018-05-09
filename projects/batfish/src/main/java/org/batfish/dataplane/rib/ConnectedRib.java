package org.batfish.dataplane.rib;

import org.batfish.datamodel.ConnectedRoute;

/** Rib for storing {@link ConnectedRoute}s */
public class ConnectedRib extends AbstractRib<ConnectedRoute> {

  private static final long serialVersionUID = 1L;

  public ConnectedRib() {
    super(null);
  }

  @Override
  public int comparePreference(ConnectedRoute lhs, ConnectedRoute rhs) {
    return 0;
  }
}
