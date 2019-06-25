package org.batfish.dataplane.rib;

import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IsisRoute;

/** Rib for storing {@link IsisRoute}s */
@ParametersAreNonnullByDefault
public class IsisRib extends AbstractRib<IsisRoute> {

  public static final Comparator<IsisRoute> routePreferenceComparator =
      Comparator.comparing(IsisRoute::getAdministrativeCost)
          .thenComparing(IsisRib::levelCost)
          .thenComparing(IsisRoute::getOverload)
          .thenComparing(IsisRoute::getMetric)
          .reversed();

  private static int levelCost(IsisRoute isisRoute) {
    // Values returned are arbitrary, but L1 routes are preferred and must have lower cost than L2.
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
    super();
    _l1Only = l1Only;
  }

  @Override
  public int comparePreference(IsisRoute lhs, IsisRoute rhs) {
    return routePreferenceComparator.compare(lhs, rhs);
  }

  @Override
  @Nonnull
  public RibDelta<IsisRoute> mergeRouteGetDelta(IsisRoute route) {
    if (route.getAttach() && !_l1Only) {
      return RibDelta.empty();
    }
    return super.mergeRouteGetDelta(route);
  }
}
