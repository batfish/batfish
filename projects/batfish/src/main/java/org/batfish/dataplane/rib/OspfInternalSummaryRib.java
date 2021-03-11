package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.OspfInternalSummaryRoute;

@ParametersAreNonnullByDefault
public class OspfInternalSummaryRib extends AbstractRib<OspfInternalSummaryRoute> {

  public OspfInternalSummaryRib() {
    super();
  }

  @Override
  public int comparePreference(OspfInternalSummaryRoute lhs, OspfInternalSummaryRoute rhs) {
    // Only way for these routes to differ is by area, which should not impact preference.
    return 0;
  }
}
