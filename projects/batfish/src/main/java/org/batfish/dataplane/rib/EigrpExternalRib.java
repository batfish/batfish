package org.batfish.dataplane.rib;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.Prefix;

public class EigrpExternalRib extends AbstractRib<EigrpExternalRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpExternalRib(@Nullable Map<Prefix, SortedSet<EigrpExternalRoute>> backupRoutes) {
    super(backupRoutes);
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
  public int comparePreference(EigrpExternalRoute lhs, EigrpExternalRoute rhs) {
    return Comparator.comparing(EigrpExternalRoute::getMetric)
        .thenComparing(EigrpExternalRoute::routeCompare)
        .compare(rhs, lhs);
  }
}
