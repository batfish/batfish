package org.batfish.dataplane.rib;

import java.util.Comparator;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IsisRoute;

/** Rib for storing {@link IsisRoute}s */
public class IsisRib extends AbstractRib<IsisRoute> {

  private static final long serialVersionUID = 1L;

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

  private final boolean _l1Only;

  public IsisRib(boolean l1Only) {
    super(null);
    _l1Only = l1Only;
  }

  @Override
  public int comparePreference(IsisRoute lhs, IsisRoute rhs) {
    // Flipped rhs & lhs because lower values are more preferred.
    return Comparator.comparing(IsisRoute::getAdministrativeCost)
        .thenComparing(IsisRib::levelCost)
        .thenComparing(IsisRoute::getMetric)
        .compare(rhs, lhs);
  }

  @Override
  public RibDelta<IsisRoute> mergeRouteGetDelta(IsisRoute route) {
    if (route.getAttach() && !_l1Only) {
      return null;
    }
    return super.mergeRouteGetDelta(route);
  }
}
