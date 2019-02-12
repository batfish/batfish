package org.batfish.dataplane.rib;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;

public class OspfIntraAreaRib extends AbstractRib<OspfIntraAreaRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public OspfIntraAreaRib() {
    super(null);
  }

  @Override
  public int comparePreference(OspfIntraAreaRoute lhs, OspfIntraAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }

  @Override
  public AbstractRoute getAbstractRoute(OspfIntraAreaRoute route) {
    return route;
  }
}
