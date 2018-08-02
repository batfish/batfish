package org.batfish.dataplane.rib;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.Prefix;

/** Rib that stores external EIGRP routes */
public class EigrpExternalRib extends AbstractRib<EigrpExternalRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpExternalRib(@Nullable Map<Prefix, SortedSet<EigrpExternalRoute>> backupRoutes) {
    super(backupRoutes);
  }

  @Override
  public int comparePreference(EigrpExternalRoute lhs, EigrpExternalRoute rhs) {
    return Comparator.comparing(EigrpExternalRoute::getCompositeCost)
        .thenComparing(EigrpExternalRoute::getAsn)
        .compare(rhs, lhs);
  }
}
