package org.batfish.dataplane.rib;

import java.util.Comparator;
import org.batfish.datamodel.EigrpInternalRoute;

public class EigrpInternalRib extends AbstractRib<EigrpInternalRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpInternalRib() {
    super(null);
  }

  /**
   * Used to compare preferences within a single RIB.
   *
   * @param lhs 1st route with which to compare preference
   * @param rhs 2nd route with which to compare preference
   * @return -1 if lhs route is less preferable than rhs; 0 if lhs route and rhs are equally
   *     preferable (i.e. for multipath routing); 1 if lhs route is strictly more preferred than rhs
   */
  @Override
  public int comparePreference(EigrpInternalRoute lhs, EigrpInternalRoute rhs) {
    return Comparator.comparing(EigrpInternalRoute::getMetric)
        .thenComparing(EigrpInternalRoute::routeCompare)
        .compare(rhs, lhs);
  }
}
