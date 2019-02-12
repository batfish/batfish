package org.batfish.dataplane.rib;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.Prefix;

/** Rib for storing {@link IsisRoute}s */
public class IsisLevelRib extends AbstractRib<IsisRoute> {

  private static final long serialVersionUID = 1L;

  public IsisLevelRib(@Nullable Map<Prefix, SortedSet<IsisRoute>> backupRoutes) {
    super(backupRoutes);
  }

  @Override
  public int comparePreference(@Nonnull IsisRoute lhs, @Nonnull IsisRoute rhs) {
    return IsisRib.routePreferenceComparator.compare(lhs, rhs);
  }

  @Override
  public AbstractRoute getAbstractRoute(IsisRoute route) {
    return route;
  }
}
