package org.batfish.dataplane.rib;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.Prefix;

/** Rib for storing {@link IsisRoute}s */
@ParametersAreNonnullByDefault
public class IsisLevelRib extends AbstractRib<IsisRoute> {

  private static final long serialVersionUID = 1L;

  public IsisLevelRib(@Nullable Map<Prefix, SortedSet<IsisRoute>> backupRoutes) {
    super(backupRoutes);
  }

  @Override
  public int comparePreference(IsisRoute lhs, IsisRoute rhs) {
    return IsisRib.routePreferenceComparator.compare(lhs, rhs);
  }
}
