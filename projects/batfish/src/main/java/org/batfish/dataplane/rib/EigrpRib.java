package org.batfish.dataplane.rib;

import java.util.Comparator;
import org.batfish.datamodel.EigrpRoute;

/** Rib that stores internal and external EIGRP routes */
public class EigrpRib extends AbstractRib<EigrpRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpRib() {
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
  public int comparePreference(EigrpRoute lhs, EigrpRoute rhs) {

    return Comparator.comparing(EigrpRoute::getAdministrativeCost)
        /*
         * At this point, we are never comparing between internal and external routes.
         * TODO What happens if internal and external routes are configured to have equal AD?
         * Comparisons between two internal or two external routes are first made by metric.
         */
        .thenComparing(EigrpRoute::getMetric)
        /* If possible, break internal vs. internal and external vs. external ties */
        .thenComparing(EigrpRoute::routeCompare)
        /* Flipped rhs & lhs because lower values are more preferred. */
        .compare(rhs, lhs);
  }
}
