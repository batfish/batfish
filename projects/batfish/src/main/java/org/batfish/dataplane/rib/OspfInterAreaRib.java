package org.batfish.dataplane.rib;

import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.Prefix;

public class OspfInterAreaRib extends AbstractRib<OspfInterAreaRoute> {

  private static final long serialVersionUID = 1L;

  public OspfInterAreaRib() {
    super(null, r -> r);
  }

  @Override
  public int comparePreference(OspfInterAreaRoute lhs, OspfInterAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }

  @Override
  public Prefix getNetwork(OspfInterAreaRoute route) {
    return route.getNetwork();
  }
}
