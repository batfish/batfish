package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.OspfInterAreaRoute;

@ParametersAreNonnullByDefault
public class OspfInterAreaRib extends AbstractRib<OspfInterAreaRoute> {

  private static final long serialVersionUID = 1L;

  public OspfInterAreaRib() {
    super(null);
  }

  @Override
  public int comparePreference(OspfInterAreaRoute lhs, OspfInterAreaRoute rhs) {
    // reversed on purpose
    return Long.compare(rhs.getMetric(), lhs.getMetric());
  }
}
