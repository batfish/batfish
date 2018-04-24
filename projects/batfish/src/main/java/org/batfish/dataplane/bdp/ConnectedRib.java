package org.batfish.dataplane.bdp;

import org.batfish.datamodel.ConnectedRoute;

public class ConnectedRib extends AbstractRib<ConnectedRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public ConnectedRib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(ConnectedRoute lhs, ConnectedRoute rhs) {
    return 0;
  }
}
