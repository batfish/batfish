package org.batfish.dataplane.bdp;

import org.batfish.datamodel.OspfInterAreaRoute;

public class OspfInterAreaRib extends AbstractRib<OspfInterAreaRoute> {

  /** */
  private static final long serialVersionUID = 1L;

  public OspfInterAreaRib(VirtualRouter owner) {
    super(owner);
  }

  @Override
  public int comparePreference(OspfInterAreaRoute lhs, OspfInterAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }
}
