package org.batfish.dataplane.rib;

import java.util.Comparator;
import org.batfish.datamodel.EigrpRoute;

/** Rib that stores internal and external EIGRP routes */
public class EigrpRib extends AbstractRib<EigrpRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpRib() {
    super(null);
  }

  @Override
  public int comparePreference(EigrpRoute lhs, EigrpRoute rhs) {
    // Flipped rhs & lhs because lower values are more preferred.
    return Comparator.comparing(EigrpRoute::getAdministrativeCost)
        .thenComparing(EigrpRoute::getMetric)
        .compare(rhs, lhs);
  }
}
