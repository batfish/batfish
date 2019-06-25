package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.OspfInterAreaRoute;

@ParametersAreNonnullByDefault
public class OspfInterAreaRib extends AbstractRib<OspfInterAreaRoute> {

  public OspfInterAreaRib() {
    super();
  }

  @Override
  public int comparePreference(OspfInterAreaRoute lhs, OspfInterAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }
}
