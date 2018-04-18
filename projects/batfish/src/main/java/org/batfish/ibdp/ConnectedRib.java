package org.batfish.ibdp;

import org.batfish.datamodel.ConnectedRoute;

public class ConnectedRib extends AbstractRib<ConnectedRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public ConnectedRib(VirtualRouter owner) {
    super(owner, null);
  }

  @Override
  public int comparePreference(ConnectedRoute lhs, ConnectedRoute rhs) {
    return 0;
  }
}
