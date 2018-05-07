package org.batfish.dataplane.rib;

import org.batfish.datamodel.RipInternalRoute;

/** Rib that stores internal RIP routes */
public class RipInternalRib extends AbstractRib<RipInternalRoute> {

  private static final long serialVersionUID = 1L;

  public RipInternalRib() {
    super(null);
  }

  @Override
  public int comparePreference(RipInternalRoute lhs, RipInternalRoute rhs) {
    // reversed on purpose, because lower metric is more preferable
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }
}
