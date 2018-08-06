package org.batfish.dataplane.rib;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.Prefix;

/** Rib that stores external EIGRP routes */
public class EigrpExternalRib extends AbstractRib<EigrpExternalRoute> {

  private static final long serialVersionUID = 1L;

  public EigrpExternalRib() {
    super(new TreeMap<>());
  }

  @Override
  public int comparePreference(EigrpExternalRoute lhs, EigrpExternalRoute rhs) {
    return Comparator.comparing(EigrpExternalRoute::getCompositeCost)
        .thenComparing(EigrpExternalRoute::getAsn)
        .compare(rhs, lhs);
  }

  @Nonnull
  public Map<Prefix, SortedSet<EigrpExternalRoute>> getBackupRoutes() {
    // Created in constructor
    return requireNonNull(_backupRoutes);
  }
}
