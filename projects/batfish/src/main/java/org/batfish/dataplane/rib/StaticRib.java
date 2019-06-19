package org.batfish.dataplane.rib;

import org.batfish.datamodel.StaticRoute;

public class StaticRib extends AbstractRib<StaticRoute> {

  private static final long serialVersionUID = 1L;

  public StaticRib() {
    super();
  }

  @Override
  public int comparePreference(StaticRoute lhs, StaticRoute rhs) {
    // Treat all static routes equally
    return 0;
  }
}
