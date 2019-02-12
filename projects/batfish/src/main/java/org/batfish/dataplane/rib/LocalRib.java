package org.batfish.dataplane.rib;

import org.batfish.datamodel.LocalRoute;
import org.batfish.datamodel.Prefix;

public class LocalRib extends AbstractRib<LocalRoute> {

  private static final long serialVersionUID = 1L;

  public LocalRib() {
    super(null, r -> r);
  }

  @Override
  public int comparePreference(LocalRoute lhs, LocalRoute rhs) {
    return 0;
  }

  @Override
  public Prefix getNetwork(LocalRoute route) {
    return route.getNetwork();
  }
}
