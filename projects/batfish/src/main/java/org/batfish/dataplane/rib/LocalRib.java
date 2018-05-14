package org.batfish.dataplane.rib;

import org.batfish.datamodel.LocalRoute;

public class LocalRib extends AbstractRib<LocalRoute> {

  private static final long serialVersionUID = 1L;

  public LocalRib() {
    super(null);
  }

  @Override
  public int comparePreference(LocalRoute lhs, LocalRoute rhs) {
    return 0;
  }
}
