package org.batfish.dataplane.rib;

import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    // Flipped rhs & lhs because lower values are more preferred.
    return rhs.getMetric().compareTo(lhs.getMetric());
  }
}
