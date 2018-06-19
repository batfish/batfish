package org.batfish.dataplane.rib;

import java.util.Comparator;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IsisRoute;

/** Rib for storing {@link IsisRoute}s */
public class IsisRib extends AbstractRib<IsisRoute> {

  private static final long serialVersionUID = 1L;

  public IsisRib() {
    super(null);
  }

  @Override
  public int comparePreference(IsisRoute lhs, IsisRoute rhs) {
    // Flipped rhs & lhs because lower values are more preferred.
    return Comparator.comparing(IsisRoute::getAdministrativeCost)
        .thenComparing(IsisRib::levelCost)
        .thenComparing(IsisRoute::getMetric)
        .compare(rhs, lhs);
  }

  private static int levelCost(IsisRoute isisRoute) {
    // Lower value is more preferred.
    switch (isisRoute.getLevel()) {
      case LEVEL_1:
        return 1;
      case LEVEL_2:
        return 2;
      default:
        throw new BatfishException(String.format("Invalid route level: %s", isisRoute.getLevel()));
    }
  }
}
