package org.batfish.bdp;

import org.batfish.datamodel.OspfIntraAreaRoute;

public class OspfIntraAreaRib extends AbstractRib<OspfIntraAreaRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public OspfIntraAreaRib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(OspfIntraAreaRoute lhs, OspfIntraAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }
}
