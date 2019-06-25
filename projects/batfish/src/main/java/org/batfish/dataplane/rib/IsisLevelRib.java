package org.batfish.dataplane.rib;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IsisRoute;

/** Rib for storing {@link IsisRoute}s */
@ParametersAreNonnullByDefault
public class IsisLevelRib extends AbstractRib<IsisRoute> {

  public IsisLevelRib(boolean withBackups) {
    super(withBackups);
  }

  @Override
  public int comparePreference(IsisRoute lhs, IsisRoute rhs) {
    return IsisRib.routePreferenceComparator.compare(lhs, rhs);
  }
}
