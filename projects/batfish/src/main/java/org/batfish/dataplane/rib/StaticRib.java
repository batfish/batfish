package org.batfish.dataplane.rib;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

public class StaticRib extends AbstractRib<StaticRoute> {

  private static final long serialVersionUID = 1L;

  public StaticRib() {
    super(null, r -> r);
  }

  @Override
  public int comparePreference(StaticRoute lhs, StaticRoute rhs) {
    // Treat all static routes equally
    return 0;
  }

  @Override
  public Prefix getNetwork(StaticRoute route) {
    return route.getNetwork();
  }
}
