package org.batfish.dataplane.bdp;

import org.batfish.datamodel.LocalRoute;

public class LocalRib extends AbstractRib<LocalRoute> {

  private static final long serialVersionUID = 1L;

  public LocalRib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(LocalRoute lhs, LocalRoute rhs) {
    return 0;
  }
}
