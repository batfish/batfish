package org.batfish.dataplane.rib;

import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.Prefix;

public class OspfIntraAreaRib extends AbstractRib<OspfIntraAreaRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public OspfIntraAreaRib() {
    super(null, r -> r);
  }

  @Override
  public int comparePreference(OspfIntraAreaRoute lhs, OspfIntraAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }

  @Override
  public Prefix getNetwork(OspfIntraAreaRoute route) {
    return route.getNetwork();
  }
}
