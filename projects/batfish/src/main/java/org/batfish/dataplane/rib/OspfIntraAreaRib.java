package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.OspfIntraAreaRoute;

@ParametersAreNonnullByDefault
public class OspfIntraAreaRib extends AbstractRib<OspfIntraAreaRoute> {

  private static final long serialVersionUID = 1L;

  public OspfIntraAreaRib() {
    super();
  }

  @Override
  public int comparePreference(OspfIntraAreaRoute lhs, OspfIntraAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }
}
