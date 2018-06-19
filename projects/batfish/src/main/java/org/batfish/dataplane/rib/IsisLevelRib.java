package org.batfish.dataplane.rib;

import org.batfish.datamodel.IsisRoute;

/** Rib for storing {@link IsisRoute}s */
public class IsisLevelRib extends AbstractRib<IsisRoute> {

  private static final long serialVersionUID = 1L;

  public IsisLevelRib() {
    super(null);
  }

  @Override
  public int comparePreference(IsisRoute lhs, IsisRoute rhs) {
    // Flipped rhs & lhs because lower values are more preferred.
    return rhs.getMetric().compareTo(lhs.getMetric());
  }
}
